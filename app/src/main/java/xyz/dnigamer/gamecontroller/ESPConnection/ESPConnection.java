package xyz.dnigamer.gamecontroller.ESPConnection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ESPConnection {

    private static final String ip = "10.0.0.1";
    private static final int port = 80;

    private static DatagramSocket socket;
    private static InetAddress address;
    private static Thread listenerThread;
    private static boolean listening = false;

    public ESPConnection() {
        connectToESP();
    }

    public static void connectToESP() {
        try {
            address = InetAddress.getByName(ip);
            socket = new DatagramSocket();
            sendDataToESP("{\"type\":\"init\", \"msg\":\"Xiaomi Connected\"}");
        } catch (IOException e) {
            System.out.println("Error connecting to ESP with error: " + e.getMessage());
        }
    }

    public static boolean disconnectFromESP() {
        listening = false; // Stop the listener thread
        if (listenerThread != null && listenerThread.isAlive()) {
            listenerThread.interrupt();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        return true;
    }

    public static String readDataFromESP() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            return new String(packet.getData(), 0, packet.getLength());
        } catch (IOException e) {
            System.out.println("Error reading data from ESP");
        }
        return null;
    }

    public static void sendDataToESP(String data) {
        try {
            byte[] buffer = data.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("Error sending data to ESP");
        }
    }

    public static void startListeningForData() {
        listening = true;
        listenerThread = new Thread(() -> {
            try {
                while (listening && socket != null && !socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    String data = readDataFromESP();
                    if (data != null) {
                        System.out.println("Received data: " + data);
                        if (data.contains("\"type\":\"PING\"")) {
                            sendDataToESP("{\"type\":\"PONG\"}");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error in listener thread: " + e.getMessage());
                listening = false; // Stop the thread if an error occurs
            }
        });
        listenerThread.start();
    }

    public static boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
}