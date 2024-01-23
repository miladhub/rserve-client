package com.acn;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class RserveClient implements AutoCloseable {
    private Socket socket;
    private RConnection conn;

    public RserveClient(Socket socket, RConnection conn) {
        this.socket = socket;
        this.conn = conn;
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 7) {
            System.err.println("args: <r_host> <r_port> <n_threads> <n_jobs> <wait_sec> <so_timeout_sec> <code>");
            System.err.println("example: localhost 6700 10 40 5 3 'Sys.sleep(1)'");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int threads = Integer.parseInt(args[2]);
        int nJobs = Integer.parseInt(args[3]);
        int wait = Integer.parseInt(args[4]);
        int soTimeout = Integer.parseInt(args[5]);
        String code = args[6];

        ExecutorService executors = Executors.newFixedThreadPool(threads);
        AtomicInteger succeededCounter = new AtomicInteger(0);
        AtomicInteger failedCounter = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < nJobs; i++) {
            int current = i;
            futures.add(executors.submit(() -> invokeR(
                    host,
                    port,
                    soTimeout,
                    succeededCounter,
                    failedCounter,
                    code,
                    current)));
        }

        executors.shutdown();
        executors.awaitTermination(wait, TimeUnit.SECONDS);
        int succeeded = succeededCounter.get();
        int failed = failedCounter.get();
        System.out.println("succeeded: " + succeeded +
                           ", failed: " + failed +
                           ", not completed: " + (nJobs - succeeded - failed));

        if (!executors.isTerminated()) {
            System.out.println("hit ctrl-C to stop waiting");
            futures.forEach(f -> f.cancel(true));
        }
    }

    private static void invokeR(
            String host,
            int port,
            int soTimeout,
            AtomicInteger succeeded,
            AtomicInteger failed,
            String code,
            int i
    ) {
        try (RserveClient client = newClient(host, port, soTimeout)) {
            client.conn.eval(code);
            System.out.println(
                    "i = " + i + " | " +
                    Thread.currentThread().getName() + " | " +
                    "OK" + " | " +
                    "succeeded = " + succeeded.incrementAndGet() + " | " +
                    "failed = " + failed.get() + " | " +
                    "timestamp = " + new Date());
        }
        catch (RserveException e) {
            System.err.println(
                    "i = " + i + " | " +
                    Thread.currentThread().getName() + " | " +
                    "RserveException: " + e.getMessage() + " | " +
                    "succeeded = " + succeeded.get() + " | " +
                    "failed = " + failed.incrementAndGet() + " | " +
                    "timestamp = " + new Date());
        }
        catch (Exception e) {
            System.err.println(
                    "i = " + i + " | " +
                    Thread.currentThread().getName() + " | " +
                    "Exception: " + e.getMessage() + " | " +
                    "succeeded = " + succeeded.get() + " | " +
                    "failed = " + failed.incrementAndGet() + " | " +
                    "timestamp = " + new Date());
        }
    }

    private static RserveClient newClient(
            String host,
            int port,
            int soTimeout
    ) throws IOException, RserveException {
        Socket sock = connectToR(host, port, soTimeout);
        RConnection conn = new RConnection(sock);
        return new RserveClient(sock, conn);
    }

    private static Socket connectToR(
            String host,
            int port,
            int soTimeoutSeconds
    ) throws IOException {
        Socket sock = new Socket(host, port);
        // disable Nagle's algorithm since we really want immediate replies
        sock.setTcpNoDelay(true);
        // zero means infinite timeout
        sock.setSoTimeout(1000 * soTimeoutSeconds);
        return sock;
    }

    @Override
    public void close() throws Exception {
        if (conn != null) conn.close();
        if (socket != null) socket.close();
    }
}