package com.kyson.mall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 *
 * 1、开启定时任务 @EnableScheduling
 * 2、加入容器 @Component
 * 3、开启一个定时任务 @Scheduled(cron = "")
 *    自动配置类  TaskSchedulingAutoConfiguration
 *
 * 异步任务： 不光定时任务能用
 *      1、@EnableAsync 开启
 *      2、给需要异步执行的方法标记 @Async
 *      3、自动配置 TaskExecutionAutoConfiguration
 *
 */
@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class HelloSchedule {

    /**
     * spring 中仅允许6位 要使用年 自己加入 quartz 包
     * 在周的位置 周一 - 周日 分别为 1 - 7
     * 定时任务不应该阻塞 比如 Thread.sleep(3000)，
     *      1、可以让任务以异步方式自己提交到线程池 配合线程池 防止阻塞
     *      2、定时任务线程池 TaskSchedulingAutoConfiguration
     *          spring.task.scheduling.pool.size=5  未必会生效
     *      3、让定时任务 异步执行
     *          @EnableAsync @Async
     *
     * 解决：使用异步 + 定时任务来完成定时任务不阻塞功能
     *
     */
    @Async
    @Scheduled(cron = "*/5 * * ? * 5")
    public void hello() throws InterruptedException
    {
        log.info("hello ... ");
//        CompletableFuture.runAsync(()->{
//
//        }, executor);

        Thread.sleep(3000);
    }
}
