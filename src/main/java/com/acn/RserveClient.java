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
public class RserveClient {
    public static void main(String[] args) throws InterruptedException {
        if (args.length != 6) {
            System.err.println("Args: <host> <port> <n_threads> <n_jobs> <timeout_sec> <code>");
            System.err.println("Example: localhost 16700 10 40 5 'Sys.sleep(1)'");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int threads = Integer.parseInt(args[2]);
        int nJobs = Integer.parseInt(args[3]);
        int timeout = Integer.parseInt(args[4]);
        String code = args[5];

        Integer soTimeout = readTimeout();

        ExecutorService executors = Executors.newFixedThreadPool(threads);
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < nJobs; i++) {
            int finalI = i;
            futures.add(executors.submit(() -> invokeR(host, port, soTimeout, count, failed, code, finalI)));
        }
        executors.shutdown();
        executors.awaitTermination(timeout, TimeUnit.SECONDS);
        int completed = count.get();
        int nFailed = failed.get();
        System.out.println("Completed " + completed + " jobs out of " + nJobs +
                           ", failed: " + nFailed + ", not completed: " + (nJobs - completed - nFailed));
        if (!executors.isTerminated()) {
            System.out.println("Timed out, cancelling");
            futures.forEach(f -> f.cancel(true));
        }
    }

    private static Integer readTimeout() {
        String timeoutStr = System.getProperty("rserve.timeout");
        if (timeoutStr != null) {
            System.err.println("Setting timeout: " + timeoutStr);
            return Integer.parseInt(timeoutStr);
        } else return null;
    }

    private static void invokeR(
            String host,
            int port,
            Integer soTimeout,
            AtomicInteger count,
            AtomicInteger failed,
            String code,
            int i
    ) {
        Socket sock = null;
        RConnection conn = null;
        try {
            sock = connectToR(host, port, soTimeout);
            conn = new RConnection(sock);
            conn.eval(code);
            System.out.println("i = " + i + " | " + Thread.currentThread().getName() + " | count.get() = " + count.get() + " | timestamp = " + new Date());
            count.incrementAndGet();
        }
        catch (RserveException e) {
            failed.incrementAndGet();
            System.err.println("i = " + i + " | " + "RserveException: " + e.getMessage() + " | timestamp = " + new Date());
        }
        catch (Exception e) {
            System.err.println("i = " + i + " | " + "Exception: " + e.getMessage() + " | timestamp = " + new Date());
        }
        finally {
            if (conn != null) conn.close();
            if (sock != null) {
                try {
                    sock.close();
                }
                catch (IOException e) {
                    System.err.println("i = " + i + " | " + "IOException closing socket: " + e.getMessage() + " | timestamp = " + new Date());
                }
            }
        }
    }

    private static Socket connectToR(String host, int port, Integer soTimeout
    ) throws IOException {
        Socket ss = new Socket(host, port);
        // disable Nagle's algorithm since we really want immediate replies
        ss.setTcpNoDelay(true);
        if (soTimeout != null) {
            ss.setSoTimeout(soTimeout);
        }
        return ss;
    }
}