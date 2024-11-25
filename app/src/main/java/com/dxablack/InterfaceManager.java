package com.dxablack;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

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
    public static ArrayList<HashMap<String, String>> getListInterface(Context context) {
        shell = new KaliShellExecutor(context);
        ArrayList<HashMap<String, String>> tmpList = new ArrayList<>();
        String[] tmpArr;
        // Jalankan perintah dan pastikan berhasil
        boolean success = shell.runKaliRoot("iw dev");

        if (success) {
            tmpList = parseInterfaces(shell.getLastOutput());
        } else {
            // Jika gagal menjalankan perintah
            Log.e("Root", "Failed to run iw dev");
            tmpArr = new String[]{};
        }
        if (tmpList != null){
            for (HashMap<String, String> hashMap : tmpList) {
                shell.runKaliRoot("ethtool -i " + hashMap.get("Interface") + " | grep driver | awk '{print $2}'");
                hashMap.put("interface", hashMap.get("Interface"));
                hashMap.put("driver", shell.getLastOutput().replaceAll("\n", "").replaceAll("\\s+", ""));
            }
        }
        return tmpList;
    }
    private static ArrayList<HashMap<String, String>> parseInterfaces(String input) {
        ArrayList<HashMap<String, String>> interfaceList = new ArrayList<>();
        String[] lines = input.split("\n");

        HashMap<String, String> currentInterface = null;

        for (String line : lines) {
            line = line.trim(); // Remove leading/trailing spaces
            if (line.startsWith("Interface")) {
                // Save the previous interface before starting a new one
                if (currentInterface != null) {
                    interfaceList.add(currentInterface);
                }
                // Create a new interface map
                currentInterface = new HashMap<>();
                currentInterface.put("Interface", line.split(" ")[1]); // Get the interface name
            } else if (currentInterface != null && !line.isEmpty()) {
                // Add other details to the current interface
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    currentInterface.put(parts[0], parts[1]);
                }
            }
        }

        // Add the last interface
        if (currentInterface != null) {
            interfaceList.add(currentInterface);
        }

        return interfaceList;
    }
    public static void fixInterface(KaliShellExecutor shellExecutor, String intface){
        shellExecutor.runKaliRootAsync("ifconfig " + intface + " up");
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
    public static int parseChannelFromFreq(int freq){
        if (freq >= 2412 && freq <= 2484){
            return (freq - 2407) / 5;
        }else if (freq >= 5170 && freq <= 5835){
            return  (freq - 500) / 5;
        }else if (freq <= 0) {
            return 0;
        }else {
            throw new IllegalArgumentException("Frequency not within WiFi bands");
        }
    }
    public static int parseFrequencyFromChannel(int channel){
        if (channel >= 1 && channel <= 14){
            return 2407 + channel * 5;
        }else if (channel >= 32 && channel <= 177){
            return 5000 + channel * 5;
        }else if (channel <= 0) {
            return 0;
        }else {
            throw new IllegalArgumentException("Channel not within WiFi bands");
        }
    }
}
