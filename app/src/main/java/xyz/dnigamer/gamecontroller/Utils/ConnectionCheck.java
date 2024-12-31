package xyz.dnigamer.gamecontroller.Utils;

import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiManager;

import java.net.Inet4Address;
import java.util.Objects;

public class ConnectionCheck {

    private final Context context;

    public ConnectionCheck(Context context) {
        this.context = context;
    }

    public boolean hasWifiEnabled() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public String getWifiSSID() {
        if (!hasWifiEnabled())
            return null;
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (wifiManager.getConnectionInfo().getSSID() == null)
            return null;
        if (wifiManager.getConnectionInfo().getSSID().equals("<unknown ssid>"))
            return null;
        return wifiManager.getConnectionInfo().getSSID().replace("\"", "");
    }

    public String getWifiIpAddress() {
        if (!hasWifiEnabled())
            return null;
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        String wifiName = wifiManager.getConnectionInfo().getSSID();
        wifiName = wifiName.replace("\"", "");
        if (wifiName.equals("<unknown ssid>"))
            return null;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        return Objects.requireNonNull(connectivityManager.getLinkProperties(network)).getLinkAddresses().stream()
                .filter(linkAddress -> linkAddress.getAddress() instanceof Inet4Address)
                .map(linkAddress -> linkAddress.getAddress().getHostAddress())
                .findFirst()
                .orElse(null);
    }
}
