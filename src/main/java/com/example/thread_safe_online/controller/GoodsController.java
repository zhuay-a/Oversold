package com.example.thread_safe_online.controller;

import com.example.thread_safe_online.entry.dto.GoodsDTO;
import com.example.thread_safe_online.entry.result.Result;
import com.example.thread_safe_online.service.GoodsService;
import com.example.thread_safe_online.service.impl.GoodsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@Controller
@RequestMapping("/goods")
@Slf4j
public class GoodsController {

    @Autowired
    private GoodsService goodsService = new GoodsServiceImpl();

    @PostMapping("/buy")
    @ResponseBody
    public Result<String> buyGoods(int id) {
//        log.info("购买商品:{}", id);
//        return goodsService.buyById(id);       //不做任何操作，会出现超卖问题                   QPS: 2100-2400
//        return goodsService.buyByIdLock(id);   //使用互斥锁解决超卖问题,直接修改数据库            QPS: 700-900
//        return goodsService.buyByIdCAS(id);    //使用CAS乐观锁解决超卖问题，直接修改数据库        QPS: 2200-2400
//        return goodsService.buyByIdDLock(id);   //使用setNx实现分布式锁，阻塞队列异步修改数据库      QPS: 1900-2200
//        return goodsService.buyByIdRedis(id);    //使用Redis lua脚本优化请求响应速度,先改Redis数据，后改数据库  QPS: 2100-2400
//        return goodsService.buyByIdRedisson(id);    //使用Redisson实现分布式锁,阻塞队列和多线程异步修改数据库  QPS: 800-1100
        return goodsService.buyByIdRedisRabbitMQ(id);   //使用Redis Lua脚本实现乐观锁，RabbitMQ消息队列和多线程异步修改数据库 QPS: 2000-2200
    }

    @PostMapping("/add")
    @ResponseBody
    public Result<String> addGoods(@RequestBody GoodsDTO goodsDTO){
        log.info("新增商品:{}", goodsDTO);

        return goodsService.addGoods(goodsDTO);
    }

    @PostMapping("/update")
    @ResponseBody
    public Result<String> updateGoods(@RequestBody GoodsDTO goodsDTO){
        return goodsService.updateGoodsById(goodsDTO);
    }

}
