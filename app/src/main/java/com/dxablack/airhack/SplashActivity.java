package com.dxablack.airhack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.FragmentManager;

import com.dxablack.AttackFunction;
import com.dxablack.DxaActivity;
import com.dxablack.KaliShellExecutor;
import com.dxablack.ShellExecutor;
import com.dxablack.bridge.Bridge;
import com.dxablack.bridge.CheckNethunterInstallation;

public class SplashActivity extends DxaActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        EdgeToEdge.enable(this);
        // Mulai pengecekan NetHunter dan MDK4
        if (isRootGrant) {
            removeMagiskNotification();
            CheckNethunterInstallation checker = new CheckNethunterInstallation(SplashActivity.this);
            checker.setOnTaskListener(new com.dxablack.bridge.CheckNethunterInstallation.OnTaskListener() {
                @Override
                public void onTaskInit() {
                    // TODO task init
                }

                @Override
                public void onTaskCompleted(Boolean result) {
                    if (result) {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }
            });
            checker.execute();
        }
    }
}
