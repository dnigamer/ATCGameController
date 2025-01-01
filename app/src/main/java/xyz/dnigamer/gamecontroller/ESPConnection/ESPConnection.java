package xyz.dnigamer.gamecontroller.ESPConnection;

import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;

import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ESPConnection {

    private static final String ip = "10.0.0.1";
    private static final int port = 80;

    private static DatagramSocket socket;
    private static boolean connected = false;
    private static InetAddress address;
    private static boolean listening = false;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static ConnectionCallback callback;

    public ESPConnection(ConnectionCallback callback) {
        ESPConnection.callback = callback;
        connectToESPAsync();
    }

    public static DatagramSocket getSocket() {
        return socket;
    }

    public static InetAddress getAddress() {
        return address;
    }

    public static boolean isListening() {
        return listening;
    }

    public static ConnectionCallback getCallback() {
        return callback;
    }

    public static void setSocket(DatagramSocket socket) {
        ESPConnection.socket = socket;
    }

    public static void setConnected(boolean connected) {
        ESPConnection.connected = connected;
    }

    public static void setAddress(InetAddress address) {
        ESPConnection.address = address;
    }

    public static void setListening(boolean listening) {
        ESPConnection.listening = listening;
    }

    public static void setCallback(ConnectionCallback callback) {
        ESPConnection.callback = callback;
    }

    public static void connectToESPAsync() {
        executorService.submit(ESPConnection::connectToESP);
    }

    public static void connectToESP() {
        try {
            address = InetAddress.getByName(ip);
            socket = new DatagramSocket();
            sendDataToESP("{\"type\":\"init\", \"msg\":\"Android client connected\"}");
            waitForAcknowledgementAsync();
        } catch (IOException e) {
            System.out.println("Error connecting to ESP with error: " + e.getMessage());
            notifyConnectionResult(false);
        }
    }

    public static boolean disconnectFromESP() {
        listening = false; // Stop the listener thread
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        connected = false;
        return true;
    }

    public static String readDataFromESP() {
        try {
            // We can set a timeout of 1 second
            socket.setSoTimeout(1000);
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            return new String(packet.getData(), 0, packet.getLength());
        } catch (IOException e) {
            System.out.println("Error reading data from ESP: " + e.getMessage());
        }
        return null;
    }

    public static void sendDataToESP(String data) {
        executorService.submit(() -> {
            try {
                byte[] buffer = data.getBytes();
                System.out.println("Sending data to ESP: " + data);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);
            } catch (Exception e) {
                System.out.println("Error sending data to ESP: " + e.getMessage());
                e.printStackTrace(); // Print the full stack trace for more details
            }
        });
    }

    public static void waitForAcknowledgementAsync() {
        executorService.submit(ESPConnection::waitForAcknowledgement);
    }

    public static void waitForAcknowledgement() {
        listening = true;
        long startTime = System.currentTimeMillis();
        try {
            while (listening && socket != null && !socket.isClosed() && (System.currentTimeMillis() - startTime) < 5000) {
                String data = readDataFromESP();
                if (data != null) {
                    System.out.println("Received data: " + data);
                    if (isInitAcknowledged(data)) {
                        connected = true;
                        System.out.println("We are now connected to the ESP");
                        notifyConnectionResult(true);
                        return;
                    }
                }
            }
            notifyConnectionResult(false);
        } catch (Exception e) {
            System.out.println("Error in listener thread: " + e.getMessage());
            listening = false; // Stop the thread if an error occurs
            notifyConnectionResult(false);
        }
    }

    private static boolean isInitAcknowledged(String data) {
        try (JsonReader reader = new JsonReader(new StringReader(data))) {
            reader.beginObject();
            String type = null;
            String msg = null;
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("type")) {
                    type = reader.nextString();
                } else if (name.equals("msg")) {
                    msg = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            return "init".equals(type) && "acknowledged".equals(msg);
        } catch (IOException e) {
            System.out.println("Error parsing JSON data: " + e.getMessage());
            return false;
        }
    }

    private static void notifyConnectionResult(boolean isConnected) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (callback != null) {
                callback.onConnectionResult(isConnected);
            }
        });
    }

    public static boolean sendCommand(JSONObject message) {
        sendDataToESP(message.toString());
        return true;
    }

    public static boolean isConnected() {
        return connected;
    }
}