package com.vmware.wavefront.integration.zabbix;

import com.wavefront.integrations.Wavefront;

import java.io.*;

/**
 * main context class to store the context of the current
 * integration works - these are shared by data fetcher and sender to help them
 * get access to information like table name and number of metrics, as well as
 * last timestamp. The context also keeps track of latest timestamp by storing them
 * into file and continuously synchronizing it.
 */
public class Context implements Cloneable{

    protected String tableName;
    protected long latesttime;
    protected File timestampFile;
    protected boolean metricsEnabled;
    protected String integrationSource;
    protected String wfhost;
    protected String wfport;
    protected String prefix;
    protected int numberOfMetrics = 0;
    protected Metrics metrics;
    boolean profileOnly = false;

    public int getNumberOfSenders() {
        return numberOfSenders;
    }

    public void setNumberOfSenders(int numberOfSenders) {
        this.numberOfSenders = numberOfSenders;
    }

    protected int numberOfSenders = 0;

    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * main constructor
     * @param tableName
     *
     * constructor sets up context using given table name.
     * also sets up initial starting time, either from the existing file, or if not found,
     * from the initial timestamp minus the interval.
     */
    public Context(String tableName) {
        metrics = new Metrics();
        this.tableName = tableName;
        this.metricsEnabled = false;

        if(ZabbixConfig.exists("integration.metrics") && ZabbixConfig.getProperty("integration.metrics").equalsIgnoreCase("true")) {
            this.metricsEnabled = true;
        }
        integrationSource = ZabbixConfig.getProperty("integration.source");
        wfhost = ZabbixConfig.getProperty("wavefront.proxy.host");
        wfport = ZabbixConfig.getProperty("wavefront.proxy.port");
        prefix = ZabbixConfig.getProperty("global.prefix");
        profileOnly = Boolean.valueOf(ZabbixConfig.getProperty("integration.profile.only")).booleanValue();

        if(prefix != null) {
            if(prefix.trim().length() == 0) prefix = "";
            else {
                prefix = prefix.trim() + ".";
            }
        } else {
            prefix = "";
        }
    }

    public Wavefront getWavefront() {
        return new Wavefront(wfhost, Integer.parseInt(wfport));
    }

    public String getFileName() {
        return tableName + ".hist";
    }

    public String getTableName() {
        return tableName;
    }

    public void refreshTimestamp() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(timestampFile));
            writer.write(Long.toString(getLatesttime()));
            writer.flush();
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getIntegrationSource() {
        return integrationSource;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public long getLatesttime() {
        return latesttime;
    }

    public void setLatesttime(long latesttime) {
        this.latesttime = latesttime;
    }

    public File getTimestampFile() {
        return timestampFile;
    }

    public void setTimestampFile(File timestampFile) {
        this.timestampFile = timestampFile;
    }

    public boolean metricsEnabled() {
        return metricsEnabled;
    }

    public void setMetricsNumber(int number) {
        numberOfMetrics = number;
    }

    public int getMetricsNumber() {
        return numberOfMetrics;
    }

    public boolean getProfileOnly() {
        return profileOnly;
    }

    public Object clone()throws CloneNotSupportedException {
        return super.clone();
    }
}
