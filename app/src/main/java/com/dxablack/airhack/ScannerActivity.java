package com.dxablack.airhack;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.fragment.app.FragmentManager;

import com.dxablack.DxaActivity;

import com.dxablack.ShellExecutor;
import com.dxablack.TickLoop;
import com.dxablack.airhack.databinding.ActivityScannerBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScannerActivity extends DxaActivity {

    private ActivityScannerBinding binding;
    private TickLoop tickLoop;
    private ShellExecutor shellExecutor;
    private FloatingActionButton fab;
    private ListView listView;
    private String wifiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        wifiInterface = getIntent().getExtras().get("interface").toString();

        fab = binding.scanButton;
        listView = binding.listAp;
        binding.interfaceView.setText(wifiInterface);
        shellExecutor = new ShellExecutor();
        shellExecutor.setOutputListener(new ShellExecutor.OutputListener() {
            @Override
            public void onNewOutput(String outputLine) {
                // Implementasi untuk menangani output baru jika diperlukan
            }

            @Override
            public void onError(String errorLine) {
                Log.e("Scanner", "onError: " + errorLine);
            }

            @Override
            public void onCommandFinished() {
                if (!TextUtils.isEmpty(shellExecutor.getLastOutput()) && !shellExecutor.getLastOutput().contains("Network is down")) {
                    ArrayList<HashMap<String, String>> list = parseWiFiScanOutput(shellExecutor.getLastOutput());
                    Log.d("Scanner", "onCommandFinished: " + shellExecutor.getLastOutput());
                    for (int i = 0; i < list.size(); i++) {
                        if (TextUtils.isEmpty(list.get(i).get("SSID"))){
                            list.remove(i);
                        }

                    }
                    // Pastikan pembaruan UI dilakukan di thread utama
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WiFiListAdapter adapter = new WiFiListAdapter(ScannerActivity.this, list);
                            listView.setAdapter(adapter);
                        }
                    });
                }
            }
        });

        tickLoop = new TickLoop(100);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                if (shellExecutor.isProcessRunning()) {
                    shellExecutor.stopProcess();
                } else {
                    startScan();
                }
            }
        });

        tick();
        FragmentManager fragmentManager = getSupportFragmentManager();
        TerminalDialogFragment terminalDialog = TerminalDialogFragment.newInstance("airmon-ng stop " + wifiInterface, ScannerActivity.this);
        terminalDialog.show(fragmentManager, "TerminalDialogFragment");
        terminalDialog.setCancelable(false);
        terminalDialog.setAutoClose(true);
        terminalDialog.setOnCloseClickedListener(new TerminalDialogFragment.OnCloseClickedListener() {
            @Override
            public void onClick(View view, int code) {

                startScan();
            }
        });
    }
    private void startScan(){
        shellExecutor.startProcessAsRootAsync("iw " + wifiInterface + " scan");
    }
    private void tick() {
        tickLoop.setOnTickListener(new TickLoop.OnTickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onTick() {
                if (shellExecutor.isProcessRunning()) {
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    fab.setImageResource(android.R.drawable.ic_media_play);
                }
                if (listView.getAdapter() != null){
                    if (listView.getAdapter().isEmpty()){
                        listView.setVisibility(View.GONE);
                        binding.isListGone.setVisibility(View.VISIBLE);
                    }else {
                        listView.setVisibility(View.VISIBLE);
                        binding.isListGone.setVisibility(View.GONE);
                    }
                }
            }
        });
        tickLoop.start();
    }

    public static ArrayList<HashMap<String, String>> parseWiFiScanOutput(String wifiScanOutput) {
        ArrayList<HashMap<String, String>> wifiList = new ArrayList<>();
        HashMap<String, String> wifiInfo = null;

        String[] lines = wifiScanOutput.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("BSS")) {
                if (wifiInfo != null) {
                    wifiList.add(wifiInfo);
                }
                wifiInfo = new HashMap<>();
                Pattern pattern = Pattern.compile("BSS ([0-9a-f:]+)\\(on (\\w+)\\)");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    wifiInfo.put("BSSID", matcher.group(1));
                    wifiInfo.put("Interface", matcher.group(2));
                }
            } else if (line.startsWith("SSID:")) {
                wifiInfo.put("SSID", line.substring(5).trim());
            } else if (line.startsWith("signal:")) {
                wifiInfo.put("Signal", line.substring(7).trim());
            } else if (line.startsWith("freq:")) {
                wifiInfo.put("Frequency", line.substring(5).trim());
            } else if (line.startsWith("capability:")) {
                wifiInfo.put("Capability", line.substring(11).trim());
            } else if (line.startsWith("Supported rates:")) {
                wifiInfo.put("SupportedRates", line.substring(16).trim());
            } else if (line.startsWith("Extended supported rates:")) {
                wifiInfo.put("ExtendedSupportedRates", line.substring(26).trim());
            } else if (line.startsWith("RSN:")) {
                wifiInfo.put("RSN", line.substring(4).trim());
            } else if (line.startsWith("HT capabilities:")) {
                wifiInfo.put("HTCapabilities", line.substring(16).trim());
            } else if (line.startsWith("VHT capabilities:")) {
                wifiInfo.put("VHTCapabilities", line.substring(17).trim());
            } else if (line.startsWith("last seen:")) {
                wifiInfo.put("LastSeen", line.substring(10).trim());
            } else if (line.startsWith("beacon interval:")) {
                wifiInfo.put("BeaconInterval", line.substring(16).trim());
            }
        }

        if (wifiInfo != null) {
            wifiList.add(wifiInfo);
        }

        return wifiList;
    }
}
