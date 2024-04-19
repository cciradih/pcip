package org.eu.cciradih.pcip;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

public record Connect(String address, int timeout) implements Callable<Result> {
    @Override
    public Result call() {
        try (Socket socket = new Socket()) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(address, 7);
            long currentTimeMillis = System.currentTimeMillis();
            socket.connect(inetSocketAddress, timeout);
            currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
            return new Result(address, currentTimeMillis);
        } catch (IOException e) {
            return null;
        }
    }
}
