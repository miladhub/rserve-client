# Build

```shell
$ mvn clean install
```

# Run

```shell
$/usr/local/bin/R \
  -e 'library(Rserve); Rserve(6700, args = "--vanilla --RS-conf ~/Rserve.conf")'
```

```shell
$ java -cp "target/*" com.acn.RserveClient \
  localhost 16700 100 100 300 4 'Sys.sleep(3)' 2> /dev/null
#3   | pool-1-thread-4  | OK                                           | ok = 3   | ko = 0   | timestamp = 2024-01-23T15:26:47.201015Z
#6   | pool-1-thread-7  | OK                                           | ok = 8   | ko = 0   | timestamp = 2024-01-23T15:26:47.201133Z
#4   | pool-1-thread-5  | OK                                           | ok = 6   | ko = 0   | timestamp = 2024-01-23T15:26:47.201095Z
#1   | pool-1-thread-2  | OK                                           | ok = 10  | ko = 0   | timestamp = 2024-01-23T15:26:47.201474Z
#0   | pool-1-thread-1  | OK                                           | ok = 5   | ko = 0   | timestamp = 2024-01-23T15:26:47.201099Z
#2   | pool-1-thread-3  | OK                                           | ok = 7   | ko = 0   | timestamp = 2024-01-23T15:26:47.201055Z
#9   | pool-1-thread-10 | OK                                           | ok = 2   | ko = 0   | timestamp = 2024-01-23T15:26:47.201066Z
#7   | pool-1-thread-8  | OK                                           | ok = 9   | ko = 0   | timestamp = 2024-01-23T15:26:47.201154Z
#5   | pool-1-thread-6  | OK                                           | ok = 4   | ko = 0   | timestamp = 2024-01-23T15:26:47.201073Z
#8   | pool-1-thread-9  | OK                                           | ok = 1   | ko = 0   | timestamp = 2024-01-23T15:26:47.201003Z
succeeded: 10, failed: 0, not completed: 0
```

# Monitoring

Client PID:

```shell
$ ps -ef | grep com.acn.RserveClient | grep -v grep | awk '{ print $2 }'
63674
```

Open TCP/IP connections:

```shell
$ lsof -a -i4 -i6 -itcp -p 63674
COMMAND   PID           USER   FD   TYPE             DEVICE SIZE/OFF NODE NAME
java    63674 milad.bourhani   10u  IPv6 0x8ca91e99ddecc0e7      0t0  TCP localhost:65502->localhost:16700 (ESTABLISHED)
java    63674 milad.bourhani   11u  IPv6 0x8ca91e99ddecc8e7      0t0  TCP localhost:65503->localhost:16700 (ESTABLISHED)
java    63674 milad.bourhani   12u  IPv6 0x8ca91e99ddecb0e7      0t0  TCP localhost:65498->localhost:16700 (ESTABLISHED)
java    63674 milad.bourhani   13u  IPv6 0x8ca91e99ddecd0e7      0t0  TCP localhost:65501->localhost:16700 (ESTABLISHED)
java    63674 milad.bourhani   14u  IPv6 0x8ca91e99ddecd8e7      0t0  TCP localhost:65499->localhost:16700 (ESTABLISHED)
java    63674 milad.bourhani   15u  IPv6 0x8ca91e99ddece0e7      0t0  TCP localhost:65505->localhost:16700 (ESTABLISHED)
java    63674 milad.bourhani   16u  IPv6 0x8ca91e99dd9718e7      0t0  TCP localhost:65497->localhost:16700 (ESTABLISHED)
java    63674 milad.bourhani   17u  IPv6 0x8ca91e99dd9728e7      0t0  TCP localhost:65500->localhost:16700 (ESTABLISHED)
java    63674 milad.bourhani   18u  IPv6 0x8ca91e99dd96f8e7      0t0  TCP localhost:65504->localhost:16700 (ESTABLISHED)
java    63674 milad.bourhani   19u  IPv6 0x8ca91e99dc1b48e7      0t0  TCP localhost:65506->localhost:16700 (ESTABLISHED)
```

Threads:

```shell
$ jstack 63674 | grep com.acn.RserveClient.invokeR
	at com.acn.RserveClient.invokeR(RserveClient.java:51)
	at com.acn.RserveClient.invokeR(RserveClient.java:51)
	at com.acn.RserveClient.invokeR(RserveClient.java:51)
	at com.acn.RserveClient.invokeR(RserveClient.java:51)
	at com.acn.RserveClient.invokeR(RserveClient.java:51)
	at com.acn.RserveClient.invokeR(RserveClient.java:51)
	at com.acn.RserveClient.invokeR(RserveClient.java:51)
	at com.acn.RserveClient.invokeR(RserveClient.java:51)
	at com.acn.RserveClient.invokeR(RserveClient.java:51)
	at com.acn.RserveClient.invokeR(RserveClient.java:51)
```
