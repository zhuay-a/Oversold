package com.example.thread_safe_online.Handler;

import com.example.thread_safe_online.config.RabbitMQConfig;
import com.example.thread_safe_online.dao.GoodsDao;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@RabbitListener(queues = {RabbitMQConfig.QUEUE})
@Component
public class BuyHandlerMQ{

    @Autowired
    private GoodsDao goodsDao;

    @RabbitHandler
    public void buyHandler(Integer id) {
        goodsDao.buyByIdLock(id);
    }
}
