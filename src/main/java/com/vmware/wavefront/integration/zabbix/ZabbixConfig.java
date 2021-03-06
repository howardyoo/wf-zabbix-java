package com.vmware.wavefront.integration.zabbix;

import java.io.InputStream;
import java.util.Properties;

/**
 * singleton config class that will load the config.properties and
 * provide configuration for zabbix integration to run properly
 */
public class ZabbixConfig {
    /**
     * automatically load the properties when
     * initialized. The config is singleton per jvm
     */
    protected static Properties config = new Properties();
    static
    {
        // using the class loader, load the properties into config.
        try {
            InputStream in = ClassLoader.getSystemResourceAsStream("config.properties");
            config.load(in);
            System.out.println(config);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String name) {
        return config.getProperty(name);
    }

    public static String setProperty(String name, String value) {
        return (String)config.setProperty(name, value);
    }

    public static boolean exists(String name) {
        if(config.containsKey(name)) return true;
        else {
            if(config.getProperty(name) == null) return false;
            else {
                if(config.getProperty(name).trim().length() == 0) return false;
                else return true;
            }
        }
    }
}
