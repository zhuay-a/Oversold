package com.example.thread_safe_online.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "idChange";
    public static final String QUEUE = "idQueue";
    public static final String ROUTING_KEY = "idKey";

    @Bean
    public DirectExchange stockDirectExchange(){
        return new DirectExchange(EXCHANGE);
    }

    // 声明队列,需要几个声明几个,这里就一个
    // 方法中实例化队列对象,确定名称,保存到Spring容器
    @Bean
    public Queue stockQueue(){
        return new Queue(QUEUE);
    }

    // 声明路由Key(交换机和队列的关系),需要几个声明几个,这里就一个
    // 方法中实例化路由Key对象,确定名称,保存到Spring容器
    @Bean
    public Binding stockBinding(){
        return BindingBuilder.bind(stockQueue()).to(stockDirectExchange())
                .with(ROUTING_KEY);
    }

}
