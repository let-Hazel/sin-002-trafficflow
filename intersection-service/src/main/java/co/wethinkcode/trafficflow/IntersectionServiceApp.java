package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IntersectionServiceApp {

    public record Intersection(String id, String district, String signalType, boolean active) {}

    private static final Map<String, Intersection> intersections = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        intersections.put("INT-1001", new Intersection("INT-1001", "Downtown", "4-WAY", true));
        intersections.put("INT-1002", new Intersection("INT-1002", "Uptown", "ROUNDABOUT", true));

        Javalin app = Javalin.create().start(7021);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Validates intersection/district names (source of truth).)
        // Add domain endpoints for intersection-service here.

        app.get("/intersections/{id}", ctx -> {
            String id = ctx.pathParam("id").toUpperCase();
            Intersection item = intersections.get(id);
            if (item != null) {
                ctx.json(item);
            } else {
                ctx.status(404).result("Intersection not found");
            }
        });
    }
}

// MQ TODO: publishes a periodic heartbeat to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at
// MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig), consumed by intersection-watchdog.
