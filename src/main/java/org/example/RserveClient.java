package org.example;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RserveClient implements AutoCloseable {
    private final Socket socket;
    private final RConnection conn;

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
        AtomicInteger succeeded = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        for (int i = 0; i < nJobs; i++) {
            int current = i;
            executors.submit(() -> {
                try (RserveClient client = newClient(host, port, soTimeout)) {
                    client.conn.eval(code);
                    logSuccess(current, succeeded, failed);
                }
                catch (Exception e) {
                    logFailure(current, succeeded, failed, e);
                }
            });
        }

        executors.shutdown();
        boolean terminated = executors.awaitTermination(wait, TimeUnit.SECONDS);

        int nSucceeded = succeeded.get();
        int nFailed = failed.get();
        int notCompleted = nJobs - nSucceeded - nFailed;
        System.out.println("succeeded: " + nSucceeded +
                           ", failed: " + nFailed +
                           ", not completed: " + notCompleted +
                           (terminated
                                   ? ""
                                   : ", hit ctrl-C to stop waiting"));
    }

    private static void logSuccess(
            int i,
            AtomicInteger succeeded,
            AtomicInteger failed
    ) {
        System.out.println(
                pad(4, "#" + i) + " | " +
                pad(16, Thread.currentThread().getName()) + " | " +
                pad(44, "OK") + " | " +
                pad(8, "ok = " + succeeded.incrementAndGet()) + " | " +
                pad(8, "ko = " + failed.get()) + " | " +
                "timestamp = " + Instant.now());
    }

    private static void logFailure(
            int i,
            AtomicInteger succeeded,
            AtomicInteger failed,
            Exception e
    ) {
        System.out.println(
                pad(4, "#" + i) + " | " +
                pad(16, Thread.currentThread().getName()) + " | " +
                pad(44, e.getMessage()) + " | " +
                pad(8, "ok = " + succeeded.get()) + " | " +
                pad(8, "ko = " + failed.incrementAndGet()) + " | " +
                "timestamp = " + Instant.now());
    }

    private static String pad(int n, String value) {
        return String.format("%-" + n + "s", value);
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