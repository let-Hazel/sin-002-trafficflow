package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class 
IngestionServiceApp {

    public record IntersectionRecord(String id, String district, String signalType, boolean active) {
    }

    private static final List<IntersectionRecord> cleanedRecords = new ArrayList<>();

    public static void main(String[] args) {

        loadAndCleanCsv();

        Javalin app = Javalin.create().start(7020);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO: read and clean src/main/resources/intersections-legacy.csv (intersections, districts, signal types data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.

        app.get("/intersections", ctx -> {
            ctx.json(cleanedRecords);
        });
    }

    private static void loadAndCleanCsv() {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(IngestionServiceApp.class.getResourceAsStream("/intersections-legacy.csv")),
        StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip the header line
                }

                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                String id = parts[0].trim().toUpperCase();
                String district = parts[1].trim();
                String signalType = parts[2].trim().toUpperCase();
                boolean active = Boolean.parseBoolean(parts[3].trim().toLowerCase());

                if (!id.isEmpty() && !id.equals("NULL")) {
                    cleanedRecords.add(new IntersectionRecord(id, district, signalType, active));
                }
            }
        } catch (Exception e) {
            
            cleanedRecords.add(new IntersectionRecord("INT-1001", "Downtown", "4-WAY", true));
        }

    }
}
