package com.vmware.wavefront.integration.zabbix;

import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduleTest implements Runnable{

    public int counter = 0;
    public String hello = null;

    public ScheduleTest() {
        counter = 0;
        hello = "hello, world";
    }

    public static void main(String[] args) {
        System.out.println("*************************************");
        System.out.println("*         Zabbix Integration        *");
        System.out.println("*************************************");

        Callable callabledelayedTask = new Callable() {
            public String call() throws Exception
            {
                return "GoodBye! See you at another invocation...";
            }
        };


        try {
            // create a scheduled thread pool to start thread with schedules
            ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(1, new DataThreadFactory("DataFetcher"));
            ScheduleTest test = new ScheduleTest();
            scheduledPool.scheduleAtFixedRate(test, 1, 5, TimeUnit.SECONDS);
            while(!scheduledPool.isTerminated()) {
            }
        } catch(Exception e) {
            System.out.println("- ERROR -");
            e.printStackTrace();
        }
        System.out.println("Schedule Pool Ended.");
    }

    public void run() {

        // run something - which will take 20 seconds
        try {
            System.out.println(Thread.currentThread().getName() + " started... ");
            Thread.sleep(20000);
            counter++;
            System.out.println("counter: " + counter);
            System.out.println(Thread.currentThread().getName() + " done processing.");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
