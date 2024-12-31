package xyz.dnigamer.gamecontroller;

import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

import xyz.dnigamer.gamecontroller.Adapters.HintAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Request necessary permissions
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            setValues();
        } else {
            setValues();
        }
    }

    private void setValues() {
        Spinner playerSpinner = findViewById(R.id.playerSelectionSpinner);
        HintAdapter adapter = new HintAdapter(this,
                android.R.layout.simple_spinner_item, getResources().getTextArray(R.array.player_array));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerSpinner.setAdapter(adapter);

        playerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((TextView) view).setText(R.string.setplayer_hint);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        EditText editTextIPAddress = findViewById(R.id.editTextIPAddress);
        editTextIPAddress.setText(R.string.fetchingdata_title);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        String wifiName = wifiManager.getConnectionInfo().getSSID();
        String wifiIpAddress = "N/A";
        wifiName = wifiName.replace("\"", "");

        if (!wifiName.equals("<unknown ssid>")) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            Network network = Objects.requireNonNull(connectivityManager).getActiveNetwork();
            wifiIpAddress = Objects.requireNonNull(connectivityManager.getLinkProperties(network)).getLinkAddresses().toString();
            wifiIpAddress = wifiIpAddress.substring(wifiIpAddress.indexOf("/") + 1, wifiIpAddress.indexOf("]"));
            wifiIpAddress = wifiIpAddress.substring(3, wifiIpAddress.length() - 3);
        }

        // Set the IP address to the EditText
        editTextIPAddress.setText(wifiIpAddress);
        editTextIPAddress.setTextColor(getResources().getColor(android.R.color.black, getTheme()));

        findViewById(R.id.startGameButton).setEnabled(false);
        findViewById(R.id.startGameButton).setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        findViewById(R.id.restartGameButton).setEnabled(false);
        findViewById(R.id.restartGameButton).setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));

        playerSpinner.setEnabled(false);
    }
}