package com.example.stresscancel;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;

public class CancelClient {

    private static final Logger log = Loggers.getLogger(CancelClient.class);

    public static void main(String[] args) throws InterruptedException {

        log.info("CancelClient");
        HttpClient client = HttpClient.create().host("localhost").port(8080);
        client.warmup().block();

        int max = 10000;
        int timeout = 15;
        if(args.length > 0) {
            max = Integer.valueOf(args[0]);
            timeout = Integer.valueOf(args[1]);
        }
        int i = 0;
        while (i < max) {
            ClientThread cancel = new ClientThread(client, i++, timeout);
            new Thread(cancel).start();
            //Thread.sleep(5);
            //ClientThread complete = new ClientThread(client, i++, 60000);
            //new Thread(complete).start();
        }

    }

    public static class ClientThread implements Runnable {

        final HttpClient client;
        final Integer request;
        final Integer timeout;

        public ClientThread(HttpClient client, Integer request, Integer timeout) {
            this.client = client;
            this.request = request;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            log.info("request {}", request);
            client.get()
                    .uri("/cancel/" + request)
                    .response()
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorResume(ex -> {
                        //log.info("timeout {}", request);
                        return Mono.empty();
                    })
                    .then()
                    .block();
        }
    }
}
