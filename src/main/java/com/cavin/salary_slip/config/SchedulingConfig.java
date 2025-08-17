package com.cavin.salary_slip.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {

    @Value("${salary.slip.scheduler.pool-size:5}")
    private int schedulerPoolSize;

    @Value("${salary.slip.scheduler.thread-name-prefix:SalarySlipScheduler-}")
    private String threadNamePrefix;

    @Value("${salary.slip.scheduler.await-termination:60}")
    private int awaitTerminationSeconds;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(schedulerPoolSize);
        scheduler.setThreadNamePrefix(threadNamePrefix);
        scheduler.setAwaitTerminationSeconds(awaitTerminationSeconds);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        scheduler.setErrorHandler(throwable -> {
            // Log any unhandled exceptions in scheduled tasks
            System.err.println("Scheduled task error: " + throwable.getMessage());
            throwable.printStackTrace();
        });
        return scheduler;
    }
}
