package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.Map;

public class CongestionServiceApp {

    private static volatile int currentLevel = 0;

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7022);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Tracks the city-wide Congestion Level (0-8).)
        // Add domain endpoints for congestion-service here.

        app.get("/congestion", ctx -> ctx.json(Map.of("level", currentLevel)));

        app.post("/congestion", ctx -> {
            String param = ctx.queryParam("level");
            if (param != null) {
                try {
                    int level = Integer.parseInt(param);
                    if (level >= 0 && level <= 8) {
                        currentLevel = level;
                        publishCongestionUpdate(currentLevel);
                        ctx.json(Map.of("level", currentLevel, "status", "UPDATED"));
                        return;
                    }
                } catch (NumberFormatException ignored) {}
            }
            ctx.status(400).result("Invalid congestion level. Must be an integer between 0 and 8.");
        });
    }

    private static void publishCongestionUpdate(int level) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection conn = factory.createConnection();
            conn.start();

            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic("congestion-topic");
            MessageProducer producer = session.createProducer(topic);

            producer.send(session.createTextMessage("{\"level\":" + level + "}"));

            producer.close();
            session.close();
            conn.close();
        } catch (Exception e) {
            // Level updates locally even if broker is offline
        }
    }
    
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
