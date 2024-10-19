package com.dxablack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
}
