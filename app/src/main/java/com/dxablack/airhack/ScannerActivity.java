package com.dxablack.airhack;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import androidx.fragment.app.FragmentManager;

import com.dxablack.AttackFunction;
import com.dxablack.DxaActivity;

import com.dxablack.InterfaceManager;
import com.dxablack.KaliShellExecutor;
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
    private KaliShellExecutor shellExecutor;
    private FloatingActionButton fab;
    private ListView listView;
    private String wifiInterface;
    private boolean usingAirodump = false;
    private Switch airodumpSwitch;
    private String TAG = "ScannerActivity";

    private TickLoop airodumpTickLoop;
    private boolean isAirodumpScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle extras = getIntent().getExtras();

        fab = binding.scanButton;
        listView = binding.listAp;
        airodumpSwitch = binding.useAirodump;
        airodumpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                usingAirodump = b;
            }
        });
        shellExecutor = new KaliShellExecutor(getApplicationContext());
        shellExecutor.setOutputListener(new ShellExecutor.OutputListener() {
            @Override
            public void onNewOutput(String outputLine) {
                // Implementasi untuk menangani output baru jika diperlukan
            }

            @Override
            public void onError(String errorLine) {
                Log.e("Scanner", "onError: " + errorLine);
                refresh();
            }

            @Override
            public void onCommandFinished() {
                refresh();
            }
        });

        tickLoop = new TickLoop(100);

        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                if (shellExecutor.isProcessRunning()) {
                    shellExecutor.stopProcess();
                    if (usingAirodump){
                        isAirodumpScan = false;
                        new KaliShellExecutor(getApplicationContext()).runKaliRootAsync("rm -rf " + AttackFunction.csvPathAirodump());
                    }
                } else {
                    new KaliShellExecutor(getApplicationContext()).runKaliRootAsync("mkdir -p " + AttackFunction.csvPathAirodump());
                    if (!usingAirodump)
                        startScan();
                    else {
                        shellExecutor.runKaliRootAsync("airodump-ng --output-format csv -w " +
                                AttackFunction.csvPathAirodump() + "/csv" + " " +
                                wifiInterface);
                        isAirodumpScan = true;
                    }
                }
            }
        });
        if (extras != null && extras.containsKey("interface")) {
            wifiInterface = extras.getString("interface");
            start();
        } else {
            interfaceSelectorDialog();
        }
        binding.fixInteface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InterfaceManager.fixInterface(shellExecutor, wifiInterface);
            }
        });
    }
    private void refresh(){
        if (!TextUtils.isEmpty(shellExecutor.getLastOutput()) | !shellExecutor.getLastOutput().contains("Network is down")) {
            Log.d(TAG, "refresh: " + usingAirodump);
            if (usingAirodump) {
                Log.d(TAG, String.format("refresh: %s", "Airodump checked."));
                KaliShellExecutor csvRawReader = new KaliShellExecutor(getApplicationContext());

                // Menjalankan perintah untuk membaca file CSV jika ada
                csvRawReader.runKaliRoot("cat " +
                        AttackFunction.csvPathAirodump() + "/*.csv");

                if (!TextUtils.isEmpty(removeFirstLine(csvRawReader.getLastOutput()))) {
                    CSVManagerAP csvManagerAP = new CSVManagerAP();
                    csvManagerAP.readSectionFromText(removeFirstLine(csvRawReader.getLastOutput()));

                    ArrayList<HashMap<String, String>> tmpArray = new ArrayList<>();
                    writeDataToFile(csvManagerAP.getData(), "/data/local/scanned.json");
                    for (HashMap<String, String> row : csvManagerAP.getData()) {
                        boolean isPassToAdd = !TextUtils.isEmpty(row.get("BSSID"));
                        if (row == null) continue; // Pastikan row tidak null

                        HashMap<String, String> tmpData = new HashMap<>();

                        // Cek null untuk setiap entri pada row sebelum mengakses
                        String essid = row.get("ESSID") != null ? row.get("ESSID") : "Unknown";
                        String bssid = row.get("BSSID") != null ? row.get("BSSID") : "Unknown";
                        String power = row.get("Power") != null ? row.get("Power") + " dBm" : "N/A";
                        String channelStr = row.get("channel");

                        // Konversi channel ke frekuensi, cek null dan format
                        int frequency = 0;
                        Log.d("TAG", "Channel: " + channelStr);
                        if (channelStr != null) {
                            try {
                                int channel = Integer.parseInt(channelStr);
                                frequency = InterfaceManager.parseFrequencyFromChannel(channel);
                            } catch (NumberFormatException e) {
                                e.printStackTrace(); // Log jika format channel salah
                            }
                        }

                        tmpData.put("SSID", essid);
                        tmpData.put("BSSID", bssid);
                        tmpData.put("Signal", power);
                        tmpData.put("Frequency", frequency != -1 ? String.valueOf(frequency) : "0");

                        if (isPassToAdd) tmpArray.add(tmpData);
                    }

                    // Pastikan pembaruan UI dilakukan di thread utama
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WiFiListAdapter adapter = new WiFiListAdapter(ScannerActivity.this, tmpArray);
                            listView.setAdapter(adapter);
                        }
                    });
                }
            }else{
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
    }
    private void startScan(){
        shellExecutor.startProcessAsRootAsync("iw " + wifiInterface + " scan");
    }
    private void tick() {
        tickLoop.setOnTickListener(new TickLoop.OnTickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onTick() {
                binding.interfaceView.setText(String.format("Interface: %s", wifiInterface));
                if (shellExecutor.isProcessRunning()) {
                    binding.fixInteface.setEnabled(false);
                    airodumpSwitch.setEnabled(false);
                    binding.progressBar.setVisibility(View.VISIBLE);
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                    if (isAirodumpScan){
                        refresh();
                    }
                } else {
                    if (usingAirodump){
                        binding.fixInteface.setEnabled(false);
                    }else{
                        binding.fixInteface.setEnabled(true);
                    }
                    airodumpSwitch.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
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
    public void interfaceSelectorDialog(){
        ArrayList<HashMap<String, String>> itemList = InterfaceManager.getListInterface(this);

        if (itemList != null && !itemList.isEmpty()) {
            ArrayList<String> ifList = new ArrayList<>();
            ArrayList<String> adapterList = new ArrayList<>();

            for (HashMap<String, String> map : itemList) {
                String interfaceName = map.get("interface");
                String driverName = map.get("driver");
                ifList.add(interfaceName);
                adapterList.add(interfaceName + " : " + driverName);
            }

            // Membuat dialog pilihan
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Interface");
            builder.setCancelable(false);

            // Konversi daftar untuk AlertDialog
            String[] options = adapterList.toArray(new String[0]);

            builder.setItems(options, (dialog, which) -> {
                // Ambil nama interface berdasarkan indeks pilihan
                String selectedInterface = ifList.get(which);

                // Menampilkan interface yang dipilih
                wifiInterface = selectedInterface;
                start();
                // Lakukan sesuatu dengan nama interface
                // Misalnya, simpan ke variabel global atau gunakan dalam logika aplikasi
            });
            builder.show();
        }

    }
    private void start(){
        tick();
        FragmentManager fragmentManager = getSupportFragmentManager();
        TerminalDialogFragment terminalDialog = TerminalDialogFragment.newInstance("airmon-ng stop " + wifiInterface +
                        "; ifconfig " + wifiInterface + " up",
                ScannerActivity.this);
        terminalDialog.show(fragmentManager, "TerminalDialogFragment");
        terminalDialog.setCancelable(false);
        terminalDialog.setAutoClose(true);
        terminalDialog.setOnCloseClickedListener(new TerminalDialogFragment.OnCloseClickedListener() {
            @Override
            public void onClick(View view, int code) {
                wifiInterface = wifiInterface.replace("mon", "");
                startScan();
            }
        });
    }
}
