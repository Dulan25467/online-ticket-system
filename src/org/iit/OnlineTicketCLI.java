
package org.iit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OnlineTicketCLI {

    private static final String BASE_URL = "http://localhost:8080//api/vendor";

    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Welcome to the Real-Time Event Ticketing CLI!");
            boolean running = true;

            while (running) {
                System.out.println("\nChoose an option:");
                System.out.println("1. Configure System");
                System.out.println("2. Start Ticket Operations");
                System.out.println("3. Stop Ticket Operations");
                System.out.println("4. Monitor Ticket Pool");
                System.out.println("5. Exit");

                String choice = reader.readLine();
                switch (choice) {
                    case "1":
                        configureSystem(reader);
                        break;
                    case "2":
                        sendRequest("/start", "POST", null);
                        System.out.println("Ticket operations started.");
                        break;
                    case "3":
                        sendRequest("/stop", "POST", null);
                        System.out.println("Ticket operations stopped.");
                        break;
                    case "4":
                        monitorTicketPool();
                        break;
                    case "5":
                        running = false;
                        System.out.println("Exiting... Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private void configureSystem(BufferedReader reader) throws Exception {
        System.out.println("Configuring the system...");

        System.out.print("Enter total tickets: ");
        int totalTickets = Integer.parseInt(reader.readLine());

        System.out.print("Enter ticket release rate (tickets/sec): ");
        int ticketReleaseRate = Integer.parseInt(reader.readLine());

        System.out.print("Enter customer retrieval rate (tickets/sec): ");
        int customerRetrievalRate = Integer.parseInt(reader.readLine());

        System.out.print("Enter max ticket capacity: ");
        int maxTicketCapacity = Integer.parseInt(reader.readLine());

        String jsonPayload = String.format(
                "{\"totalTickets\": %d, \"ticketReleaseRate\": %d, \"customerRetrievalRate\": %d, \"maxTicketCapacity\": %d}",
                totalTickets, ticketReleaseRate, customerRetrievalRate, maxTicketCapacity
        );


        sendRequest("/system/configure", "POST", jsonPayload);
        System.out.println("System configured successfully.");
    }

    private void monitorTicketPool() throws Exception {
        System.out.println("Fetching real-time ticket pool status...");
        String response = sendRequest("/status", "GET", null);
        System.out.println("Ticket Pool Status: " + response);
    }

    private String sendRequest(String endpoint, String method, String payload) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");

        if (payload != null) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200 || responseCode == 201) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            }
        } else {
            throw new RuntimeException("Request failed with HTTP code: " + responseCode);
        }
    }
}
