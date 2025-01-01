package xyz.dnigamer.gamecontroller;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
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

import org.json.JSONObject;

import xyz.dnigamer.gamecontroller.Adapters.HintAdapter;
import xyz.dnigamer.gamecontroller.ESPConnection.ConnectionCallback;
import xyz.dnigamer.gamecontroller.ESPConnection.ESPConnection;
import xyz.dnigamer.gamecontroller.Utils.ConnectionCheck;

public class MainActivity extends AppCompatActivity implements ConnectionCallback, View.OnClickListener, AdapterView.OnItemSelectedListener, View.OnTouchListener {

    private ConnectionCheck connCheck;
    private int player = 0;
    private boolean gameStarted = false;

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

        int[] buttonIds = new int[] {
                R.id.connectButton,
                R.id.startGameButton,
                R.id.restartGameButton,
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(this);
        }

        findViewById(R.id.buttonUp).setOnTouchListener(this);
        findViewById(R.id.buttonDown).setOnTouchListener(this);
        findViewById(R.id.buttonLeft).setOnTouchListener(this);
        findViewById(R.id.buttonRight).setOnTouchListener(this);

        // on spinner item selected
        Spinner playerSpinner = findViewById(R.id.playerSelectionSpinner);
        playerSpinner.setOnItemSelectedListener(this);
        playerSpinner.setEnabled(false);

        // Set up the Spinner adapter
        HintAdapter adapter = new HintAdapter(this,
                android.R.layout.simple_spinner_item, getResources().getTextArray(R.array.player_array));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerSpinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0 && position != player) { // If a player is selected
            JSONObject message = new JSONObject();
            try {
                message.put("command", "setplayer");
                message.put("player", position);
                player = position;
                ESPConnection.sendCommand(message);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error setting player", Toast.LENGTH_SHORT).show();
            }
        } else if (position == 0 && player != 0) { // If the hint is selected (player disconnected)
            JSONObject message = new JSONObject();
            try {
                message.put("command", "removeplayer");
                message.put("player", player);
                player = 0;
                ESPConnection.sendCommand(message);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error removing player", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.connectButton) {
            handleConnectButtonClick();
        } else if (id == R.id.startGameButton) {
            handleStartGameButtonClick();
        } else if (id == R.id.restartGameButton) {
            handleRestartGameButtonClick();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleButtonPress(v.getId());
                return true;
            case MotionEvent.ACTION_UP:
                handleButtonRelease(v.getId());
                v.performClick();
                return true;
        }
        return false;
    }

    private void handleButtonPress(int buttonId) {
        if (!ESPConnection.isConnected()) {
            Toast.makeText(this, "Not connected to ESP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!gameStarted) {
            Toast.makeText(this, "Game not started", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject message = new JSONObject();
        try {
            message.put("command", "move");
            message.put("player", player);
            if (buttonId == R.id.buttonUp) {
                message.put("direction", "up");
            } else if (buttonId == R.id.buttonDown) {
                message.put("direction", "down");
            } else if (buttonId == R.id.buttonLeft) {
                message.put("direction", "left");
            } else if (buttonId == R.id.buttonRight) {
                message.put("direction", "right");
            }
            message.put("pressed", true);
            ESPConnection.sendCommand(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleButtonRelease(int buttonId) {
        if (!ESPConnection.isConnected()) {
            Toast.makeText(this, "Not connected to ESP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!gameStarted) {
            Toast.makeText(this, "Game not started", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject message = new JSONObject();
        try {
            message.put("command", "move");
            message.put("player", player);
            if (buttonId == R.id.buttonUp) {
                message.put("direction", "up");
            } else if (buttonId == R.id.buttonDown) {
                message.put("direction", "down");
            } else if (buttonId == R.id.buttonLeft) {
                message.put("direction", "left");
            } else if (buttonId == R.id.buttonRight) {
                message.put("direction", "right");
            }
            message.put("pressed", false);
            ESPConnection.sendCommand(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleConnectButtonClick() {
        TextView connectionStateButton = findViewById(R.id.buttonViewConnectionState);
        connectionStateButton.setText(R.string.connecting_state);
        connectionStateButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light, getTheme()));

        if (ESPConnection.isConnected()) {
            System.out.println("Disconnecting from ESP");
            try {
                if (gameStarted) {
                    JSONObject message = new JSONObject();
                    message.put("command", "stopgame");
                    ESPConnection.sendCommand(message);

                    JSONObject messagePlayer = new JSONObject();
                    messagePlayer.put("command", "removeplayer");
                    messagePlayer.put("player", player);
                    ESPConnection.sendCommand(messagePlayer);

                    player = 0;
                    gameStarted = false;

                    Toast.makeText(this, "Game stopped", Toast.LENGTH_SHORT).show();
                }
                if (ESPConnection.disconnectFromESP()) {
                    Toast.makeText(this, "Disconnected from ESP", Toast.LENGTH_SHORT).show();
                    updateUIForDisconnectedState();
                }
            } catch (Exception e) {
                e.printStackTrace();
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
    }

    private void handleStartGameButtonClick() {
        if (!ESPConnection.isConnected()) {
            Toast.makeText(this, "Not connected to ESP", Toast.LENGTH_SHORT).show();
            return;
        }

        Spinner playerSpinner = findViewById(R.id.playerSelectionSpinner);
        int player = playerSpinner.getSelectedItemPosition();
        if (player == 0) {
            Toast.makeText(this, "Please select a player", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gameStarted) {
            JSONObject message = new JSONObject();
            try {
                message.put("command", "stopgame");
                ESPConnection.sendCommand(message);
            } catch (Exception e) {
                e.printStackTrace();
            }

            playerSpinner.setEnabled(true);

            findViewById(R.id.startGameButton).setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
            ((TextView) findViewById(R.id.startGameButton)).setText(R.string.startgame_action);

            findViewById(R.id.restartGameButton).setEnabled(false);
            findViewById(R.id.restartGameButton).setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));

            gameStarted = false;
            return;
        }

        JSONObject message = new JSONObject();
        try {
            message.put("command", "startgame");
            ESPConnection.sendCommand(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        playerSpinner.setEnabled(false);

        findViewById(R.id.startGameButton).setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark, getTheme()));
        ((TextView) findViewById(R.id.startGameButton)).setText(R.string.stopgame_action);

        findViewById(R.id.restartGameButton).setEnabled(true);
        findViewById(R.id.restartGameButton).setBackgroundColor(getResources().getColor(android.R.color.holo_red_light, getTheme()));

        gameStarted = true;
    }

    private void handleRestartGameButtonClick() {
        if (!ESPConnection.isConnected()) {
            Toast.makeText(this, "Not connected to ESP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!gameStarted) {
            Toast.makeText(this, "Game not started", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject message = new JSONObject();
        try {
            message.put("command", "restartgame");
            ESPConnection.sendCommand(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ESPConnection.disconnectFromESP();
    }

    @Override
    public void onConnectionResult(boolean isConnected) {
        if (isConnected) {
            updateUIForConnectedState();
        } else {
            Toast.makeText(this, "Error connecting to ESP", Toast.LENGTH_SHORT).show();
            updateUIForDisconnectedState();
        }
    }

    private void updateUIForConnectedState() {
        findViewById(R.id.startGameButton).setEnabled(true);
        findViewById(R.id.startGameButton).setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        findViewById(R.id.restartGameButton).setEnabled(false);
        findViewById(R.id.restartGameButton).setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));

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
        ((TextView) findViewById(R.id.startGameButton)).setText(R.string.startgame_action);
        findViewById(R.id.restartGameButton).setEnabled(false);
        findViewById(R.id.restartGameButton).setBackgroundColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        ((TextView) findViewById(R.id.restartGameButton)).setText(R.string.restartgame_action);

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
    }
}