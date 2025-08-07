package com.genius.primavera.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
public class BatchConfiguration {

    @Value("${batch.async.enabled:true}")
    private boolean asyncEnabled;

    @Value("${batch.thread.core-pool-size:10}")
    private int corePoolSize;

    @Value("${batch.thread.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${batch.thread.queue-capacity:100}")
    private int queueCapacity;

    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("batch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("Batch TaskExecutor configured with core={}, max={}, queue={}", 
                corePoolSize, maxPoolSize, queueCapacity);
        return executor;
    }

    @Bean
    public JobLauncher asyncJobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        
        if (asyncEnabled) {
            jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
            log.info("Async JobLauncher configured");
        } else {
            log.info("Sync JobLauncher configured");
        }
        
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
}