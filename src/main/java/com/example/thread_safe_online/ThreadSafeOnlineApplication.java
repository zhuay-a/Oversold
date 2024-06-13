package com.example.thread_safe_online;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
public class ThreadSafeOnlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThreadSafeOnlineApplication.class, args);
    }

}
