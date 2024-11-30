package com.dxablack.bridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;

import com.dxablack.AttackFunction;
import com.dxablack.KaliShellExecutor;
import com.dxablack.ShellExecutor;
import com.dxablack.airhack.MainActivity;
import com.dxablack.airhack.SplashActivity;

// AsyncTask untuk mengecek NetHunter dan MDK4
public class CheckNethunterInstallation extends AsyncTask<Void, Void, Boolean> {
    private boolean isMDK4Installed = true;
    private boolean isNethunterInstalled = true;
    private Activity activity;
    private OnTaskListener taskListener; // Tambahkan listener

    // Interface untuk listener
    public interface OnTaskListener {
        void onTaskInit();  // Fungsi yang dipanggil ketika task dimulai
        void onTaskCompleted(Boolean result);  // Fungsi yang dipanggil setelah task selesai
    }

    public CheckNethunterInstallation(Activity instance) {
        activity = instance;
    }

    // Method untuk mengatur listener
    public void setOnTaskListener(OnTaskListener listener) {
        this.taskListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Panggil listener saat task dimulai
        if (taskListener != null) {
            taskListener.onTaskInit();
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        // Mengecek apakah NetHunter full terinstall
        isNethunterInstalled = checkNethunter();
        if (!isNethunterInstalled) {
            return false; // Jika NetHunter tidak terinstal, hentikan proses
        }

        // Mengecek apakah MDK4 terinstall di NetHunter
        isMDK4Installed = checkMDK4();
        return isMDK4Installed;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        // Panggil listener saat task selesai
        if (taskListener != null) {
            taskListener.onTaskCompleted(result);
        }

        if (!result) {
            if (!isNethunterInstalled) {
                new AlertDialog.Builder(activity)
                        .setTitle("Error")
                        .setCancelable(false)
                        .setMessage("RootFS is not installed or not mounted! Install in " + AttackFunction.rootFsPath() + " then try again.")
                        .setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activity.finishAffinity();
                            }
                        })
                        .show();
            }

            if (!isMDK4Installed) {
                new AlertDialog.Builder(activity)
                        .setTitle("Installer")
                        .setCancelable(false)
                        .setMessage("Install mdk4?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = Bridge.createExecuteIntent("/data/data/com.offsec.nhterm/files/usr/bin/kali",
                                        "clear; echo \"Installing mdk4...\"; apt update -y && apt install mdk4 -y; echo \"Successfully installed mdk4...\"; exit");
                                activity.startActivity(intent);
                                activity.finishAffinity();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activity.finishAffinity();
                            }
                        })
                        .show();
            }
        }
    }

    // Fungsi untuk mengecek apakah NetHunter full terinstall
    private boolean checkNethunter() {
        return new ShellExecutor().startProcessAsRoot("[ -d " + AttackFunction.rootFsPath() + "/sys/class ] && exit 0 || exit 1");
    }

    // Fungsi untuk mengecek apakah MDK4 terinstall di NetHunter
    private boolean checkMDK4() {
        return new KaliShellExecutor(activity).runKaliRoot("which mdk4");
    }
}
