package com.pixonic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by t.boldyrev on 06.03.2017.
 */
@Configuration
public class Config {

    @Bean
    public ExecutorService taskExecutorService(@Value("${task_executor.pool_size}") int size) {
        return Executors.newFixedThreadPool(size);
    }

}
