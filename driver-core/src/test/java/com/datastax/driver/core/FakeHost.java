package com.datastax.driver.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.fail;

/**
 * Fake Cassandra host that will cause a given error when the driver tries to connect to it.
 */
public class FakeHost {
    public enum Behavior {THROWING_CONNECT_TIMEOUTS, THROWING_OPERATION_TIMEOUTS}

    private final String address;
    private final int port;
    private final Behavior behavior;
    private final ExecutorService executor;

    FakeHost(String address, int port, Behavior behavior) {
        this.address = address;
        this.port = port;
        this.behavior = behavior;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void start() {
        executor.execute(new AcceptClientAndWait(address, port, behavior));
    }

    public void stop() {
        executor.shutdownNow();
    }

    private static class AcceptClientAndWait implements Runnable {

        private final String address;
        private final int port;
        private final Behavior behavior;

        public AcceptClientAndWait(String address, int port, Behavior behavior) {
            this.address = address;
            this.port = port;
            this.behavior = behavior;
        }

        @Override public void run() {
            try {
                InetAddress bindAddress = InetAddress.getByName(address);
                int backlog = (behavior == Behavior.THROWING_CONNECT_TIMEOUTS)
                    ? 1
                    : -1; // default
                ServerSocket server = new ServerSocket(port, backlog, bindAddress);

                if (behavior == Behavior.THROWING_CONNECT_TIMEOUTS) {
                    // fill backlog queue
                    new Socket().connect(server.getLocalSocketAddress());
                }

                TimeUnit.MINUTES.sleep(10);
                fail("Mock host wasn't expected to live more than 10 minutes");
            } catch (IOException e) {
                fail("Unexpected I/O exception", e);
            } catch (InterruptedException e) {
                // interruption is the expected way to stop this runnable, exit
            }
        }
    }
}