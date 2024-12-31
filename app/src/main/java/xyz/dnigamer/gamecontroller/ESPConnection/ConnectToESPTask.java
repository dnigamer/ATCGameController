package xyz.dnigamer.gamecontroller.ESPConnection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConnectToESPTask {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void execute() {
        Future<Boolean> future = executorService.submit(() -> {
            ESPConnection.connectToESP();
            return ESPConnection.isConnected();
        });

        try {
            Boolean isConnected = future.get();
            onPostExecute(isConnected);
        } catch (Exception e) {
            e.printStackTrace();
            onPostExecute(false);
        }
    }

    protected void onPostExecute(Boolean isConnected) {
        if (isConnected) {
            ESPConnection.startListeningForData();
        } else {
            System.out.println("Error connecting to ESP");
        }
    }
}