## Zabbix Wavefront Integration JAVA version
### Overview
Zabbix Integration Tile existing on Wavefront is implemented with Python script that collects metrics collected in Zabbix server, by running queries to MySQL database. However, the integration script suffer from performance issues when dealing with large set of data, as python is not robust enough in terms of multi-threading and its performance on SQL extraction and reformatting.

Therefore, in order to make the data extract more efficient as well as provide added functionalities, JAVA implementation was designed and developed.
### Features
- Easier configuration via config.properties file
- Supports multi-threading of history and history_uint processing
- Profiling only will only run data point profiling which will tell how much data is incoming for each interval
- Batch size support will automtically increase / decrease the number of working threads based on the total number of records.
- Uses [micrometer](http://micrometer.io) library to monitor the JVM's performance and sends to Wavefront for easier monitoring.
### System Requirements
- JDK 1.8 and above
- 2GB RAM
### How to install
1. Download the extract zip file in an empty directory.
2. edit the config.properties to modify the settings. You must set the wavefront proxy settings such as host and port, and also mysql setting for connection to Zabbix database.
```
# zabbix integration configuration file
# wavefront proxy part
wavefront.proxy.host = localhost
wavefront.proxy.port = 2878

# mysql database part
mysql.db.host = localhost
mysql.db.port = 3308
mysql.db.name = zabbix
mysql.db.user = root
mysql.db.password = mysql

# zabbix tables to fetch data from
zabbix.tables = history, history_uint

# prefix which will be added for all metrics
global.prefix = zabbix

# enable sending out JVM metrics
jvm.metrics = true
# source name of the integration
integration.source = zabbix.integration
# whether to enable integration related metrics to be sent to wavefront
integration.metrics = true
# whether to send only profile related metrics
# this mode will NOT send any data, but only calculates number of point data in given interval
integration.profile.only = false
# whether to enable zabbix metrics to be sent to wavefront
integration.wavefront.send = true
# integration batch size - metrics will be grouped in this number
integration.batch.size = 10000
# fetch interval (in ms) - main thread will go to sleep for this interval for each fetch and send
integration.fetch.interval = 10000
```
3. Run the wfzabbix.sh to start running the application.
### Dashboards
The application also provides wfzabbix-dashboard.json which can be imported to your Wavefront to start monitoring the metrics which will be collected under zabbix.integration.*

