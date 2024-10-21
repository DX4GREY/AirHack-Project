package com.dxablack;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class ShellExecutor {

    private Process process;
    private StringBuilder output;
    private OutputListener listener;
    private int code;
    private boolean dbg = false;

    public ShellExecutor() {
        output = new StringBuilder();
    }

    public ShellExecutor(boolean debug){
        dbg = debug;
        output = new StringBuilder();
    }

    // Setter untuk listener
    public void setOutputListener(OutputListener listener) {
        this.listener = listener;
    }

    // Interface untuk callback output shell
    public static interface OutputListener {
        void onNewOutput(String outputLine);  // Ketika ada output baru
        void onError(String errorLine);       // Ketika terjadi error
        void onCommandFinished();             // Ketika perintah selesai
    }

    // Menjalankan proses shell normal
    public boolean startProcess(String command) {
        try {
            ProcessBuilder builder = new ProcessBuilder("sh", "-c", command);
            builder.redirectErrorStream(true);  // Menggabungkan error dengan output
            process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            output.setLength(0);  // Reset output

            while ((line = reader.readLine()) != null) {
                if (dbg) System.out.println(line);
                output.append(line).append("\n");

                // Panggil listener ketika ada output baru
                if (listener != null) {
                    listener.onNewOutput(line);
                }
            }

            int exitCode = process.waitFor();
            if (listener != null) {
                listener.onCommandFinished();  // Beri tahu jika perintah selesai
            }
            code = exitCode;
            return exitCode == 0;  // Mengembalikan true jika berhasil
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onError(e.getMessage());  // Kirim error ke listener
            }
        }
        return false;
    }
    public int getCode(){
        return code;
    }
    // Menjalankan proses shell sebagai root
    public boolean startProcessAsRoot(String command) {
        try {
            ProcessBuilder builder = new ProcessBuilder("su", "-c", command);
            builder.redirectErrorStream(true);  // Menggabungkan error dengan output
            process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            output.setLength(0);  // Reset output

            while ((line = reader.readLine()) != null) {
                if (dbg) System.out.println(line);
                output.append(line).append("\n");

                // Panggil listener ketika ada output baru
                if (listener != null) {
                    listener.onNewOutput(line);
                }
            }

            int exitCode = process.waitFor();
            if (listener != null) {
                listener.onCommandFinished();  // Beri tahu jika perintah selesai
            }
            code = exitCode;
            return exitCode == 0;  // Mengembalikan true jika berhasil
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onError(e.getMessage());  // Kirim error ke listener
            }
        }
        return false;
    }

    // Menjalankan proses root secara asinkron di thread terpisah
    public void startProcessAsRootAsync(final String command) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startProcessAsRoot(command);  // Jalankan proses sinkron tapi di thread terpisah
            }
        }).start();
    }

    // Menghentikan proses yang sedang berjalan
    public void stopProcess() {
        if (process != null) {
            process.destroy();
            System.out.println("Process stopped");
        }
    }

    // Mendapatkan output terakhir
    public String getLastOutput() {
        return output.toString();
    }

    // Memeriksa apakah proses masih berjalan
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean isProcessRunning() {
        return process != null && process.isAlive();
    }
}
