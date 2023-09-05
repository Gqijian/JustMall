package com.kyson.mall.search.thread;

import java.util.concurrent.*;

public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);//一个固定线程数量的线程池

    public static void main(String[] args) throws ExecutionException, InterruptedException
    {

        System.out.println("main .. start ..");
        /*
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
        }, executor);
        */

        /*
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            return i;
        }, executor).whenComplete((result, exception) -> {
            //虽然能得到异常信息，但无法修改返回数据
            System.out.println("异步任务完成了：" + "结果是  " + result + "异常时 " +exception);
        }).exceptionally(throwable -> {

            //这里可以感知异常 当出现异常后，同时返回默认值
            return 0;
        });
        System.out.println("main .. end .." + future.get());
        */

        /*
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            return i;
        }, executor).handle((result, exception) -> {
            if(result != null){
                return result * 2 ;
            }
            if(exception != null){

                return 0;
            }
            return 0;
        });
        */

        /**
         * 线程串行化
         * 1、theRun 不能获取到上一步的返回结果
         * .thenRunAsync(() -> {
         *             System.out.println("任务2启动了 ");
         *         }, executor)
         *
         * 2、thenAccept 可以接受上一步返回值
         *   .thenAcceptAsync(result ->{
         *             System.out.println("上一步的返回值 " + result);
         *         },executor)
         * 3、 thenApply 可以接受返回值 并且自己也有返回值
         * .thenApplyAsync((result)->{
         *             return result + "hello";
         *         },executor);
         */
        /*
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            return i;
        }, executor);

        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            return "Hello";
        }, executor);

        future01.runAfterBothAsync(future02,()->{
            System.out.println("任务3");
        },executor);

        future01.thenAcceptBothAsync(future02,(f1, f2)->{
            System.out.println("前两个任务的返回值 " + f1 + f2);
        },executor);

        CompletableFuture<String> completableFuture = future01.thenCombineAsync(future02, (f1, f2) -> {
            return f1 + "res " + f2;
        }, executor);
         */

        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());

            return "查询的信息Img";
        }, executor);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());

            return "查询的信息Attr";
        }, executor);

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());

            return "查询的信息Desc";
        }, executor);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);

        allOf.get();    //用 get() 方法阻塞等待所有结果完成
        System.out.println("所有返回结果 " + futureImg.get() + futureAttr.get() + futureDesc.get());
    }

    public static void TheadTest() throws ExecutionException, InterruptedException
    {

        Thread01 thread01 = new Thread01();
        thread01.start();   //启动线程

        Runable01 runable01 = new Runable01();
        new Thread(runable01).start();

        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
        new Thread(futureTask).start();

        //阻塞等待 返回 futureTask 执行完之后的结果
        Integer integer = futureTask.get();
        System.out.println("main .. end .." + integer);

        //上述三种 在生产中都不用 而是使用线程池
        //当前系统中 线程池一般只有一两个 异步任务 直接提交给线程池 让他自己去执行
        /**
         * 区别在于 1 2 不能得到返回值，第三种可以获得返回值
         * 但是 1 2 3 都不能资源控制，只有 4 线程池可以资源调配
         */
        executor.execute(new Runable01());

        /**
         *
         * 给线程池提交任务
         *  service.execute(new Runable01());
         *
         * 1、创建线程池
         *      1 Executors 创建
         *      2 new ThreadPoolExecutor()
         */

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());


        /**
         *
         *  七大参数
         *
         *  int corePoolSize[5], 核心线程数 除非设置了allowCoreThreadTimeOut 否则不回收，一直存在，线程池创建好好后就准备就绪的线程，等待异步任务去执行
         *  等于 new Thread() 5 个 只是没有 start()
         *
         *  int maximumPoolSize, 最大线程数 控制资源
         *  long keepAliveTime, 存活时间 释放空闲的线程 释放的是超过核心数的部分
         *  TimeUnit unit, 时间单位
         *  BlockingQueue<Runnable> workQueue,  阻塞队列，如果任务有很多，就会将多的任务放在队列，当有线程空闲，就回去队列取出新的任务
         *  ThreadFactory threadFactory,   线程创建工厂
         *  RejectedExecutionHandler handler  如果队列满了，按照指定的拒绝策略，拒绝执行的任务
         *
         *  工作顺序
         */

    }

    public static class Thread01 extends Thread {

        @Override
        public void run()
        {

            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            super.run();
        }
    }

    public static class Runable01 implements Runnable {

        @Override
        public void run()
        {

            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception
        {

            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            return i;
        }
    }
}
