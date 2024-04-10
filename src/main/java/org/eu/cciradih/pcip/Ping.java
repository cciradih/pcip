package org.eu.cciradih.pcip;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Callable;

public record Ping(String address, int timeout) implements Callable<Result> {
    @Override
    public Result call() throws IOException {
        long currentTimeMillis = System.currentTimeMillis();
        boolean reachable = InetAddress.getByName(this.address)
                .isReachable(this.timeout);
        currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
        if (reachable) {
            return new Result(address, currentTimeMillis);
        }
        return null;
    }
}
