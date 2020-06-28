package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.CountDownTimer;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private
    int level = 20;

    private EditText levelEdit;

    private TextView contentText;

    private Button btnButton;

    private WifiManager wifiManager;

    private static final int MIN_RSSI = -100;

    private static final int MAX_RSSI = -55;

    CountDownTimer timer = new CountDownTimer(5000, 2500) {
        @Override
        public void onTick(long millisUntilFinished) {
            level = getLevel();

            wifiManager.startScan();

            btnButton.setText("扫描中");
        }

        @Override
        public void onFinish() {
            timer.start();
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> results = wifiManager.getScanResults();

                results.sort((o1, o2) -> {
                    int la = calculateSignalLevel(o1.level, level);
                    int lb = calculateSignalLevel(o2.level, level);
                    return lb - la;
                });

                StringBuffer text = new StringBuffer();
                for (ScanResult result : results) {
                    text.append(result.SSID);
                    text.append(":");

                    int l = calculateSignalLevel(result.level, level);
                    text.append(l);

                    text.append("\n\n");
                }

                contentText.setText(text);
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        levelEdit = findViewById(R.id.level);
        contentText = findViewById(R.id.content);
        btnButton = findViewById(R.id.btn);
        btnButton.setOnClickListener(this::onLoadClick);

        String[] PERMS_INITIAL = {Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(PERMS_INITIAL, 127);

        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        onLoadClick(null);
    }

    private void onLoadClick(View view) {
        if (btnButton.getText().equals("扫描")) {
            timer.start();
            btnButton.setText("扫描中");
        } else {
            timer.cancel();
            btnButton.setText("扫描");
        }
    }

    @SuppressLint("SetTextI18n")
    private int getLevel() {
        try {
            String levelString = levelEdit.getText().toString();

            level = Integer.parseInt(levelString);
        } catch (Exception e) {
            level = 20;
        }

        levelEdit.setText(level + "");

        return level;
    }

    public static int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= MIN_RSSI) {
            return 0;
        }
        float inputRange = (MAX_RSSI - MIN_RSSI);
        float outputRange = (numLevels - 1);
        return (int) ((float) (rssi - MIN_RSSI) * outputRange / inputRange);
    }
}