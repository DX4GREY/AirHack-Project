package com.dxablack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class DxaActivity extends AppCompatActivity {
    static {
        System.loadLibrary("airhack");
    }
    public boolean isRootGrant = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRootGrant = new ShellExecutor().startProcessAsRoot("su -c whoami");
        if (!isRootGrant){
            new AlertDialog.Builder(this)
                    .setTitle("Error") // Judul dialog
                    .setCancelable(false)
                    .setMessage("Root access denied!. please grant root access this app. Exit") // Pesan dialog
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                        }
                    })
                    .show();
        }
        AssetsHelper.copyAssetsToData(getApplicationContext());
        try {
            replaceInFile(new File(getFilesDir().getAbsolutePath() + "/bin/kali"), "$@ROOTFS_PATH@$", AttackFunction.rootFsPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private void replaceInFile(File file, String oldString, String newString) throws IOException {
        // Baca isi file
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }

        // Ganti string yang ditemukan
        String content = contentBuilder.toString();
        content = content.replace(oldString, newString);

        // Tulis kembali ke file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
    public int channelToFrequency(int channel) {
        if (channel >= 1 && channel <= 14) {  // Pita 2.4 GHz
            return 2407 + channel * 5;
        } else if (channel >= 32 && channel <= 177) {  // Pita 5 GHz
            return 5000 + channel * 5;
        } else if (channel == 0) {
            return 0;
        }else{
            throw new IllegalArgumentException("Nomor kanal tidak valid: " + channel);
        }
    }
    public static boolean writeDataToFile(List<HashMap<String, String>> data, String filePath) {
        String TAG = "FileHelper";
        Gson gson = new Gson();
        String jsonData = gson.toJson(data);

        ShellExecutor shellExecutor = new ShellExecutor();
        shellExecutor.setOutputListener(new ShellExecutor.OutputListener() {
            @Override
            public void onNewOutput(String outputLine) {
                Log.d(TAG, "Shell Output: " + outputLine);
            }

            @Override
            public void onError(String errorLine) {
                Log.e(TAG, "Shell Error: " + errorLine);
            }

            @Override
            public void onCommandFinished() {
                Log.d(TAG, "Shell Command Finished");
            }
        });

        // Buat perintah untuk menulis ke file sebagai root
        String command = "echo '" + jsonData.replace("'", "\\'") + "' > " + filePath;

        return shellExecutor.startProcessAsRoot(command);
    }
    public String removeFirstLine(String input) {
        // Mencari posisi baris baru (\n) pertama
        int newlineIndex = input.indexOf("\n");

        // Jika ada baris baru, ambil substring setelahnya
        if (newlineIndex != -1) {
            return input.substring(newlineIndex + 1);
        } else {
            // Jika tidak ada baris baru, kembalikan string asli
            return input;
        }
    }

}
