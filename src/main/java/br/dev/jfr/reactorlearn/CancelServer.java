package br.dev.jfr.reactorlearn;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class CancelServer {

    private static final Logger log = Loggers.getLogger(CancelServer.class);

    public static void main(String[] args) {

        log.info("CancelServer");
        var pool = createPoll();

        DisposableServer server =
            HttpServer
                .create()
                .host("localhost")
                .port(8080)
                .route(routes ->
                    routes
                        .get("/hello",
                                (request, response) -> response.sendString(helloMono()))
                        .get("/cancel/{param}",
                                (request, response) ->
                                        response.sendString(cancelRequest(pool, request.param("param")))
                        )
                )
                .bindNow();

        server.onDispose()
                .block();
    }

    private static Mono<String> cancelRequest(ConnectionFactory factory,
                                              String val) {
        return
                Flux
                        .usingWhen(factory.create(),
                                c -> c.createStatement("select pg_sleep(0.2) sleep, '" + val.toString() + "' as status").execute(),
                                Connection::close)
                        .flatMap(it -> it.map((r, m) -> r.get(1, String.class)))
                        .next()
                        .doOnNext(s -> log.info("onNext query {}", s))
                        .doOnCancel(() -> log.info("CANCEL query "));
    }

    private static Mono<String> helloMono() {
        return Mono.just("Hello World!");
    }

    private static ConnectionFactory createPoll() {
        final String username = "postgres";
        final String password = "admin";
        final String host = "localhost";
        final String port = "5432";
        final String database = "postgres";
        final String connectionTimeout = "15000";
        final String applicationName = "sample";

        // Pool
        final String initialSize = "1";
        final String maxSize = "3";
        final String idleTimeout = "330000";
        final String maxLifetime = "900000";

        final Map<String, String> options = new HashMap<>();
        options.put("lock_timeout", "10s");

        // Creates a ConnectionPool wrapping an underlying ConnectionFactory
        final ConnectionFactory connectionFactory =
                new PostgresqlConnectionFactory(
                        PostgresqlConnectionConfiguration.builder()
                                .host(host)
                                .port(Integer.parseInt(port))
                                .username(username)
                                .password(password)
                                .database(database)
                                .connectTimeout(Duration.ofMillis(Long.parseLong(connectionTimeout)))
                                .fetchSize(1000)
                                .preparedStatementCacheQueries(-1)
                                .schema("public")
                                .tcpKeepAlive(false)
                                .tcpNoDelay(true)
                                .options(options)
                                .applicationName(applicationName)
                                .autodetectExtensions(false)
                                .build());

        return new ConnectionPool(
                ConnectionPoolConfiguration.builder(connectionFactory)
                        .maxIdleTime(Duration.ofMillis(Long.parseLong(idleTimeout)))
                        .initialSize(Integer.parseInt(initialSize))
                        .maxSize(Integer.parseInt(maxSize))
                        .acquireRetry(3)
                        .maxLifeTime(Duration.ofMillis(Long.parseLong(maxLifetime)))
                        .validationQuery("SELECT 1")
                        .build());
    }
}
