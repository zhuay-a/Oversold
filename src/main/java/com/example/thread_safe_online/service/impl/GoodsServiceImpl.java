package com.example.thread_safe_online.service.impl;

import com.example.thread_safe_online.dao.GoodsDao;
import com.example.thread_safe_online.entry.common.Context;
import com.example.thread_safe_online.entry.common.RedisLock;
import com.example.thread_safe_online.entry.dto.GoodsDTO;
import com.example.thread_safe_online.entry.result.Result;
import com.example.thread_safe_online.service.GoodsService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.plaf.TableHeaderUI;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.*;

@Service
@Slf4j
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsDao goodsDao;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static final BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(1024 * 1024);

    private static final ExecutorService thread_pool = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init(){
        thread_pool.submit(new BuyHandler());
    }

    private class BuyHandler implements Runnable{

        @Override
        public void run() {
            while ((true)) {
                try {
                    Integer id = blockingQueue.take();
                    handlerBuId(id);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void handlerBuId(Integer id) {
        goodsDao.buyByIdLock(id);
    }

    private static final DefaultRedisScript<Long> BUYLUA;
    static{
        BUYLUA = new DefaultRedisScript<>();
        BUYLUA.setLocation(new ClassPathResource("buyLua.lua"));
        BUYLUA.setResultType(Long.class);
    }

    @Override
    public Result<String> buyByIdRedis(int id){
        Long result = stringRedisTemplate.execute( //执行lua脚本
                BUYLUA,
                Collections.emptyList(),
                String.valueOf(id)
        );
        int i = 0;
        if (result != null) {
            i = result.intValue();
        }
//        int num = Integer.parseInt(stringRedisTemplate.opsForValue().get(String.valueOf(id)));
//        log.info("{}查询到剩余商品数量：{}",Thread.currentThread(), num);
//        if(num > 0) {
//            stringRedisTemplate.opsForValue().increment(String.valueOf(id), -1);
//            i = 1;
//        }
        if (i == 1) {
            blockingQueue.add(id);
            return Result.success("购买成功");
        }
        else {
            return Result.fail("购买失败");
        }
    }

    @Override
    public Result<String> buyById(int id) {
        int num = goodsDao.getNumById(id);
//        log.info("进程{}查询到剩余{}个商品",Thread.currentThread(), num);
        if (num <= 0) {
            log.info("无法购买商品");
            return Result.fail("商品不足");
        }
        log.info("进程{}购买商品", Thread.currentThread());
        goodsDao.buyByIdLock(id);
        return Result.fail("购买成功");
    }

    //使用互斥锁解决超卖问题   QPS 800-900
    @Override
    public Result<String> buyByIdLock(int id) {
        synchronized (GoodsServiceImpl.class) {
            int num = goodsDao.getNumById(id);
            log.info("进程{}查询到剩余{}个商品",Thread.currentThread(), num);
            if (num <= 0) {
            log.info("无法购买商品");
                return Result.fail("商品不足");
            }
            log.info("进程{}购买商品", Thread.currentThread());
            goodsDao.buyByIdLock(id);
            return Result.fail("购买成功");
        }
    }

    //使用CAS解决超卖问题
    @Override
    public Result<String> buyByIdCAS(int id) {
        int i = goodsDao.buyByIdCAS(id);

        if(i > 0) {
            log.info("{}购买成功",Thread.currentThread());
            return Result.fail("购买成功");
        }
        else {
            log.info("{}购买失败",Thread.currentThread());
            return Result.fail("购买失败");
        }
    }

    //使用分布式锁解决超卖问题
    @Override
    public Result<String> buyByIdDLock(int id) {
        RedisLock redisLock = new RedisLock(stringRedisTemplate);
        boolean result = redisLock.tryLock(id);
        if (!result) {
//            log.info("加锁失败{}",Thread.currentThread());
            return Result.fail("购买失败");
        }
//        log.info("加锁成功");
//        int num = goodsDao.getNumById(id);
//        if(num > 0)
//            goodsDao.buyByIdLock(id);
//        redisLock.unLock(id);


        try {
            int num = Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(String.valueOf(id))));
            if (num > 0) {
                stringRedisTemplate.opsForValue().increment(String.valueOf(id), -1);
                blockingQueue.add(id);
                return Result.success("购买成功");
            } else
                return Result.success("购买成功");
        } finally {
            redisLock.unLock(id);
        }
    }

    @Override
    public Result<String> buyByIdRedisson(int id) {
        RLock lock = redissonClient.getLock(Context.REDIS_LOCK_HEAD + id);
        boolean b = false;
        try {
            b = lock.tryLock(1, 10, TimeUnit.SECONDS);//超时等待时间，超时时间，时间单位
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(!b) {
            return Result.fail("加锁失败");
        }
        int num = Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(String.valueOf(id))));
        try{
            if(num > 0) {
                stringRedisTemplate.opsForValue().increment(String.valueOf(id), -1);
                blockingQueue.add(id);
                return Result.success("购买成功");
            }
            else
                return Result.fail("购买失败");
        }finally {
            lock.unlock();
        }
    }

    //新增商品
    @Override
    @Transactional
    public Result<String> addGoods(GoodsDTO goodsDTO){
        GoodsDTO goodsDTO1 = goodsDao.getById(goodsDTO.getId());
        if (goodsDTO1 != null)
            return Result.success("新增商品失败");
        goodsDao.addGoods(goodsDTO);
        stringRedisTemplate.opsForValue().set(goodsDTO.getId().toString(), goodsDTO.getNum().toString());
        return Result.success("新增商品成功");

    }

    //更新商品信息
    @Override
    @Transactional
    public Result<String> updateGoodsById(GoodsDTO goodsDTO) {
        if(goodsDTO.getId() == null)
            return Result.fail("商品id不能为空");
        int i = goodsDao.updateById(goodsDTO);
        if(i > 0) {
            stringRedisTemplate.opsForValue().set(goodsDTO.getId().toString(), goodsDTO.getNum().toString());
            return Result.success("更新商品信息成功");
        }
        else
            return Result.fail("更新商品信息失败");
    }
}
