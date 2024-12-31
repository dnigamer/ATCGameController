package xyz.dnigamer.gamecontroller;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import xyz.dnigamer.gamecontroller.Adapters.HintAdapter;
import xyz.dnigamer.gamecontroller.ESPConnection.ConnectionCallback;
import xyz.dnigamer.gamecontroller.ESPConnection.ESPConnection;
import xyz.dnigamer.gamecontroller.Utils.ConnectionCheck;

public class MainActivity extends AppCompatActivity implements ConnectionCallback {

    private ConnectionCheck connCheck;

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

        connCheck = new ConnectionCheck(this);

        // Set the onClickListener for the startGameButton
        findViewById(R.id.connectButton).setOnClickListener(v -> {
            TextView connectionStateButton = findViewById(R.id.buttonViewConnectionState);
            connectionStateButton.setText(R.string.connecting_state);
            connectionStateButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light, getTheme()));

            if (ESPConnection.isConnected()) {
                System.out.println("Disconnecting from ESP");
                if (ESPConnection.disconnectFromESP()) {
                    Toast.makeText(this, "Disconnected from ESP", Toast.LENGTH_SHORT).show();
                    updateUIForDisconnectedState();
                } else {
                    Toast.makeText(this, "Error disconnecting from ESP", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            try {
                setValues();
                if (!connCheck.hasWifiEnabled()) {
                    Toast.makeText(this, "Please enable WiFi", Toast.LENGTH_SHORT).show();
                    updateUIForDisconnectedState();
                    return;
                }
                if (connCheck.getWifiIpAddress() == null) {
                    Toast.makeText(this, "No IP Address found", Toast.LENGTH_SHORT).show();
                    updateUIForDisconnectedState();
                    return;
                }
                if (connCheck.getWifiSSID() == null) {
                    Toast.makeText(this, "No WiFi SSID found", Toast.LENGTH_SHORT).show();
                    updateUIForDisconnectedState();
                    return;
                }
                if (!connCheck.getWifiSSID().equals("ESP8266_AP")) {
                    Toast.makeText(this, "Please connect to ESP8266_AP WiFi", Toast.LENGTH_SHORT).show();
                    updateUIForDisconnectedState();
                    return;
                }

                new ESPConnection(this);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error connecting to ESP", Toast.LENGTH_SHORT).show();
                updateUIForDisconnectedState();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ESPConnection.disconnectFromESP();
    }

    @Override
    public void onConnectionResult(boolean isConnected) {
        if (isConnected) {
            findViewById(R.id.startGameButton).setEnabled(true);
            findViewById(R.id.startGameButton).setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
            findViewById(R.id.restartGameButton).setEnabled(true);
            findViewById(R.id.restartGameButton).setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));

            Spinner playerSpinner = findViewById(R.id.playerSelectionSpinner);
            playerSpinner.setEnabled(true);

            findViewById(R.id.buttonViewConnectionState).setBackgroundColor(getResources().getColor(android.R.color.holo_green_light, getTheme()));
            TextView connectionStateButton = findViewById(R.id.buttonViewConnectionState);
            connectionStateButton.setText(R.string.connected_state);

            TextView v = findViewById(R.id.connectButton);
            v.setText(R.string.disconnect_action);
        } else {
            Toast.makeText(this, "Error connecting to ESP", Toast.LENGTH_SHORT).show();
            findViewById(R.id.startGameButton).setEnabled(false);
            findViewById(R.id.startGameButton).setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
            findViewById(R.id.restartGameButton).setEnabled(false);
            findViewById(R.id.restartGameButton).setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));

            Spinner playerSpinner = findViewById(R.id.playerSelectionSpinner);
            playerSpinner.setSelection(0);
            playerSpinner.setEnabled(false);

            findViewById(R.id.buttonViewConnectionState).setBackgroundColor(getResources().getColor(android.R.color.holo_red_light, getTheme()));
            TextView connectionStateButton = findViewById(R.id.buttonViewConnectionState);
            connectionStateButton.setText(R.string.disconnected_state);

            TextView v = findViewById(R.id.connectButton);
            v.setText(R.string.connect_action);
        }
    }

    private void updateUIForConnectedState() {
        findViewById(R.id.startGameButton).setEnabled(true);
        findViewById(R.id.startGameButton).setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        findViewById(R.id.restartGameButton).setEnabled(true);
        findViewById(R.id.restartGameButton).setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));

        Spinner playerSpinner = findViewById(R.id.playerSelectionSpinner);
        playerSpinner.setEnabled(true);

        findViewById(R.id.buttonViewConnectionState).setBackgroundColor(getResources().getColor(android.R.color.holo_green_light, getTheme()));
        TextView connectionStateButton = findViewById(R.id.buttonViewConnectionState);
        connectionStateButton.setText(R.string.connected_state);

        TextView v = findViewById(R.id.connectButton);
        v.setText(R.string.disconnect_action);
    }

    private void updateUIForDisconnectedState() {
        findViewById(R.id.startGameButton).setEnabled(false);
        findViewById(R.id.startGameButton).setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        findViewById(R.id.restartGameButton).setEnabled(false);
        findViewById(R.id.restartGameButton).setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));

        Spinner playerSpinner = findViewById(R.id.playerSelectionSpinner);
        playerSpinner.setSelection(0);
        playerSpinner.setEnabled(false);

        findViewById(R.id.buttonViewConnectionState).setBackgroundColor(getResources().getColor(android.R.color.holo_red_light, getTheme()));
        TextView connectionStateButton = findViewById(R.id.buttonViewConnectionState);
        connectionStateButton.setText(R.string.disconnected_state);

        TextView v = findViewById(R.id.connectButton);
        v.setText(R.string.connect_action);
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

        ConnectionCheck connCheck = new ConnectionCheck(this);

        // Set the IP address to the EditText
        editTextIPAddress.setText(connCheck.getWifiIpAddress() == null ? "No IP Address" : connCheck.getWifiIpAddress());
        editTextIPAddress.setTextColor(getResources().getColor(android.R.color.black, getTheme()));

        findViewById(R.id.startGameButton).setEnabled(false);
        findViewById(R.id.startGameButton).setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        findViewById(R.id.restartGameButton).setEnabled(false);
        findViewById(R.id.restartGameButton).setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));

        playerSpinner.setEnabled(false);
    }
}