package com.dxablack.airhack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.dxablack.AttackFunction;
import com.dxablack.DxaActivity;
import com.dxablack.KaliShellExecutor;
import com.dxablack.ShellExecutor;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SplashActivity extends DxaActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        EdgeToEdge.enable(this);
        // Mulai pengecekan NetHunter dan MDK4
        if (isRootGrant) {
            new CheckNethunterInstallation().execute();
        }
    }

    // AsyncTask untuk mengecek NetHunter dan MDK4
    private class CheckNethunterInstallation extends AsyncTask<Void, Void, Boolean> {
        private boolean isMDK4Installed = true;
        private boolean isNethunterInstalled = true;
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
            if (result) {
                // Jika pengecekan berhasil, pindah ke MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Tampilkan pesan jika NetHunter atau MDK4 tidak terinstall

                if (!isNethunterInstalled){
                    new AlertDialog.Builder(SplashActivity.this)
                            .setTitle("Error")
                            .setCancelable(false)
                            .setMessage("RootFS is not installed or not mount it!, install in " + AttackFunction.rootFsPath() + " then try again")
                            .setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finishAffinity();
                                        }
                                    })
                            .show();
                }
                if (!isMDK4Installed){
                    new AlertDialog.Builder(SplashActivity.this)
                            .setTitle("Installer")
                            .setCancelable(false)
                            .setMessage("Install mdk4?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    showTerminalDialog("apt install mdk4 -y");
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finishAffinity();
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
            return new KaliShellExecutor(SplashActivity.this).runKaliRoot("which mdk4");
        }
    }
    private void showTerminalDialog(String command) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TerminalDialogFragment terminalDialog = TerminalDialogFragment.newInstance(command, SplashActivity.this);
        terminalDialog.show(fragmentManager, "TerminalDialogFragment");
        terminalDialog.setCancelable(false);
        terminalDialog.setAutoClose(true);
        terminalDialog.setOnCloseClickedListener(new TerminalDialogFragment.OnCloseClickedListener() {
            @Override
            public void onClick(View view, int code) {
                Intent intent = getIntent();
                finish(); // Menghentikan aktivitas saat ini
                startActivity(intent); // Memulai kembali aktivitas yang sama
            }
        });
    }
}
