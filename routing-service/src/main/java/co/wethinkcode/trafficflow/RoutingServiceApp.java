package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;


public class RoutingServiceApp {

    private static volatile int level = 0;

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                Connection conn = new ActiveMQConnectionFactory("tcp://localhost:61616").createConnection();
                conn.start();
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageConsumer consumer = session.createConsumer(session.createTopic("congestion-topic"));
                consumer.setMessageListener(msg -> {
                    try {
                        String text = ((TextMessage) msg).getText();
                        level = Integer.parseInt(text.replaceAll("[^0-9]", ""));
                    } catch (Exception ignored) {}
                });
            } catch (Exception ignored) {}
        }).start();

        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Provides estimated travel times based on congestion and intersection.)
        // Add domain endpoints for routing-service here.
        app.get("/route", ctx -> ctx.result("ETA: " + (10 + level * 5) + " mins (Level " + level + ")"));
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
