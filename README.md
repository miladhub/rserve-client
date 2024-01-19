# Build

```shell
$ mvn clean install
```

# Run

```shell
$ java -cp "target/*" com.acn.RserveClient \
  localhost 16700 10 40 5 'Sys.sleep(1)'
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
