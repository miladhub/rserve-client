package com.acn;

import org.rosuda.REngine.Rserve.RConnection;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"CallToPrintStackTrace", "ResultOfMethodCallIgnored"})
public class RserveClient {
    public static void main(String[] args) throws InterruptedException {
        if (args.length != 6) {
            System.out.println("Args: <host> <port> <n_threads> <n_jobs> <timeout_sec> <code>");
            System.out.println("Example: localhost 16700 10 40 5 'Sys.sleep(1)'");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.valueOf(args[1]);
        int threads = Integer.valueOf(args[2]);
        int nJobs = Integer.valueOf(args[3]);
        int timeout = Integer.valueOf(args[4]);
        String code = args[5];

        ExecutorService executors = Executors.newFixedThreadPool(threads);
        AtomicInteger count = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < nJobs; i++) {
            int finalI = i;
            futures.add(executors.submit(() -> invokeR(host, port, count, code, finalI)));
        }
        executors.shutdown();
        executors.awaitTermination(timeout, TimeUnit.SECONDS);
        int completed = count.get();
        System.out.println("Completed " + completed + " jobs out of " + nJobs);
        if (!executors.isTerminated()) {
            System.out.println("Timed out, cancelling");
            futures.forEach(f -> f.cancel(true));
        }
    }

    private static void invokeR(
            String host, int port, AtomicInteger count, String code, int i
    ) {
        RConnection conn = null;
        try {
            conn = new RConnection(host, port);
            conn.eval(code);
            System.out.println("i = " + i + " | " + Thread.currentThread().getName() + " | count.get() = " + count.get() + " | timestamp = " + new Date());
            count.incrementAndGet();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (conn != null) conn.close();
        }
    }
}