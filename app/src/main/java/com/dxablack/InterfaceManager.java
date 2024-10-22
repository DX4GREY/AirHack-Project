package com.dxablack;

import android.content.Context;
import android.util.Log;

public class InterfaceManager {
    private static KaliShellExecutor shell;
    public static boolean checkMonitor(Context context, String interfaceName){
        shell = new KaliShellExecutor(context);
        if (shell.runKaliRoot("iw " + interfaceName + " info")) {
            String output = shell.getLastOutput();  // Pemanggilan getLastOutput seharusnya bekerja
            if (output != null && output.contains("monitor")) {
                return true;
            }
        }
        return false;
    }
    public static boolean isInterfaceExist(Context context, String interfaceName){
        shell = new KaliShellExecutor(context);
        if (shell.runKaliRoot("iwconfig")) {
            String output = shell.getLastOutput();  // Pemanggilan getLastOutput seharusnya bekerja
            if (output != null && output.contains(interfaceName)) {
                return true;
            }
        }
        return false;
    }
    public static String[] getListInterface(Context context) {
        shell = new KaliShellExecutor(context);

        // Jalankan perintah dan pastikan berhasil
        boolean success = shell.runKaliRoot("airmon-ng");

        if (success) {
            // Ambil output setelah proses selesai
            String text = transformText(shell.getLastOutput());
            String[] options = text.replaceAll("Interface,", "").split(",");
            Log.d("Root", "getListInterface: " + text);
            return options;
        } else {
            // Jika gagal menjalankan perintah
            Log.e("Root", "Failed to run airmon-ng");
            return new String[]{};
        }
    }
    public static boolean fixInterface(KaliShellExecutor shellExecutor, String intface){
        return shellExecutor.runKaliRoot("ifconfig " + intface + " up");
    }

    private static String transformText(String input) {
        StringBuilder result = new StringBuilder();
        String[] lines = input.split("\n");
        boolean isFirst = false;
        for (String line : lines) {
            if (isFirst) {
                // Pisahkan setiap baris berdasarkan spasi
                String[] parts = line.trim().split("\\s+");
                // Cek apakah baris memiliki lebih dari satu elemen
                if (parts.length > 1) {
                    // Ambil elemen kedua (Interface)
                    String interfaceName = parts[1];
                    // Tambahkan ke hasil
                    if (result.length() > 0) {
                        result.append(","); // Tambahkan koma pemisah jika sudah ada elemen sebelumnya
                    }
                    result.append(interfaceName);
                }
            } else {
                isFirst = true;
            }
        }
        return result.toString();
    }
}
