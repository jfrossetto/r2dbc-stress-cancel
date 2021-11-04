## Project Testing Error / Stress cancel requests
- Configure connection in PostgresR2dbcConnectionPoolFactory
- build and run application 
```
./mvnw clean package
java -jar ./target/cancelServer.jar
```
- open a new terminal and run cancelClient (will open 10000 threads with 15 mills in timeout)  
```
java -jar ./target/cancelClient.jar
```
- after cancelClient finishes check connections in database
```sql
select pid, datname, usename, application_name, backend_start, query_start, query 
from pg_stat_activity
where application_name = 'stressCancel' 
order by query_start
```

|pid|datname|usename|application_name|backend_start|query_start|query|
|---|-------|-------|----------------|-------------|-----------|-----|
|1888|postgres|postgres|stressCancel|2021-11-04 14:17:45.318|2021-11-04 14:17:56.198|select 'request-932' as request , pg_sleep(0.2) sleep|
|1890|postgres|postgres|stressCancel|2021-11-04 14:17:55.410|2021-11-04 14:17:57.625|select 'request-1051' as request , pg_sleep(0.2) sleep|
|1891|postgres|postgres|stressCancel|2021-11-04 14:17:55.412|2021-11-04 14:18:13.962|select 'request-1208' as request , pg_sleep(0.2) sleep|

- complete log -> log_connectionStuck2.txt
