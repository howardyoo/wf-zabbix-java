package com.vmware.wavefront.integration.zabbix;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.wavefront.WavefrontConfig;
import io.micrometer.wavefront.WavefrontMeterRegistry;

import java.time.Duration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * zabbix integration which retrieves
 * rows from mysql database, converts them and send them to wavefront proxy.
 * it utilizes thread executor model to multi-thread the required metrics to
 * be collected and sent to zabbix.
 */
public class Zabbix implements Runnable {

    protected ContextMap ctxMap;
    protected long lastCycleTimestamp = 0;
    public Zabbix(ContextMap ctxMap) {
        this.ctxMap = ctxMap;
    }

    public long getLastCycleTimestamp(){
        return lastCycleTimestamp;
    }

    public void setLastCycleTimestamp(long timestamp) {
        this.lastCycleTimestamp = timestamp;
    }

    public static void main(String[] args) {

        ContextMap contextMap = new ContextMap();   // map will hold context for each tables.
        System.out.println("*************************************");
        System.out.println("*         Zabbix Integration        *");
        System.out.println("*************************************");

        try {
            // setup micrometer registry to monitor JVM activities
            WavefrontConfig config = null;
            MeterRegistry registry = null;

            // if jvm metrics is enabled, register and use micrometer library to send JVM metrics
            if(ZabbixConfig.exists("jvm.metrics") && ZabbixConfig.getProperty("jvm.metrics").equalsIgnoreCase("true")) {
                config = new WavefrontConfig() {
                    @Override
                    public String uri() {
                        return "proxy://" + ZabbixConfig.getProperty("wavefront.proxy.host") + ":" + ZabbixConfig.getProperty("wavefront.proxy.port");
                    }

                    @Override
                    public String source() {
                        return ZabbixConfig.getProperty("integration.source");
                    }

                    @Override
                    public String get(String key) {
                        // defaults everything else
                        return null;
                    }

                    @Override
                    public Duration step() {
                        // 10 seconds reporting interval
                        return Duration.ofSeconds(10);
                    }

                    @Override
                    public String prefix() {
                        if (ZabbixConfig.exists("global.prefix")) {
                            return ZabbixConfig.getProperty("global.prefix") + ".integration";
                        }
                        return "integration";
                    }

                    @Override
                    public String globalPrefix() {
                        if (ZabbixConfig.exists("global.prefix")) {
                            return ZabbixConfig.getProperty("global.prefix") + ".integration";
                        }
                        return "integration";
                    }
                };

                registry = new WavefrontMeterRegistry(config, Clock.SYSTEM);
                new ClassLoaderMetrics().bindTo(registry);
                new JvmMemoryMetrics().bindTo(registry);
                new JvmGcMetrics().bindTo(registry);
                new ProcessorMetrics().bindTo(registry);
                new JvmThreadMetrics().bindTo(registry);
                // now, the registry should be ready
            }

            // first, get the list of tables - defined in config.properties
            String tableList = ZabbixConfig.getProperty("zabbix.tables");
            StringTokenizer tokenizer = new StringTokenizer(tableList, ",");

            // initialize context map based on the configured tables.
            while(tokenizer.hasMoreElements()) {
                String table = tokenizer.nextToken().trim();
                Context context = new Context(table);
                contextMap.addContext(context);
            }

            // interval is defined in ms
            long interval = Long.parseLong(ZabbixConfig.getProperty("integration.fetch.interval"));

            // create a scheduled thread pool to start thread with schedules
            ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(1, new DataThreadFactory("Zabbix"));
            scheduledPool.scheduleAtFixedRate(new Zabbix(contextMap), 1000, interval, TimeUnit.MILLISECONDS);
            while(!scheduledPool.isTerminated()) {
                Thread.sleep(interval);
            }
        } catch(Exception e) {
            System.out.println("- ERROR -");
            e.printStackTrace();
        }
        System.out.println("Zabbix integration ended.");
    }

    /**
     * run method - spawns off the thread cycle.
     */
    public void run() {
        try {
            // inspect last timestamp
            long lasttime = getLastCycleTimestamp();
            long currentTime = System.currentTimeMillis() / 1000l;
            long interval = Long.parseLong(ZabbixConfig.getProperty("integration.fetch.interval"));

            // if lasttime is set to 0, start with time past the interval.
            if(lasttime == 0) {
                lasttime = currentTime - (interval/1000l);
            }

            // set the last cycle timestamp
            setLastCycleTimestamp(currentTime);

            ExecutorService executor = Executors.newCachedThreadPool(new DataThreadFactory("DataFetcher-" + lasttime));
            // setup the fetcher for each tables, and start off its process.
            List<Context> contexts = ctxMap.getAllContexts();

            System.out.println("--------- start ----------");

            for(Context ctx : contexts) {
                System.out.println("starting data fetcher on " + ctx.getTableName() + " " + lasttime + "~" + currentTime);
                DataFetcher fetcher = new DataFetcher(ctx, lasttime, currentTime);
                executor.execute(fetcher);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
