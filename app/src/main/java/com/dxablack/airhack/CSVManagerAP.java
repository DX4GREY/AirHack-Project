package com.dxablack.airhack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CSVManagerAP {

    private List<HashMap<String, String>> bssidData; // Variabel instance untuk menyimpan data BSSID

    // Konstruktor
    public CSVManagerAP() {
        this.bssidData = new ArrayList<>();
    }

    // Metode untuk membaca bagian BSSID dari file CSV
    public void readSectionFromFile(FileReader fileReader) {
        String line;
        boolean readingBssidSection = true;

        try (BufferedReader br = new BufferedReader(fileReader)) {
            // Membaca header
            String[] headers = br.readLine().split(",");

            // Membaca baris-baris data BSSID
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Station MAC")) {
                    readingBssidSection = false;
                }

                if (readingBssidSection) {
                    String[] values = line.split(",");
                    HashMap<String, String> rowMap = new HashMap<>();
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        rowMap.put(headers[i].trim(), values[i].trim());
                    }
                    bssidData.add(rowMap);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metode untuk membaca bagian BSSID dari teks langsung
    public void readSectionFromText(String csvText) {
        String line;
        boolean readingBssidSection = true;

        try (BufferedReader br = new BufferedReader(new StringReader(csvText))) {
            String[] headers = br.readLine().split(",");

            while ((line = br.readLine()) != null) {
                if (line.startsWith("Station MAC")) {
                    readingBssidSection = false;
                }

                if (readingBssidSection) {
                    String[] values = line.split(",");
                    HashMap<String, String> rowMap = new HashMap<>();
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        rowMap.put(headers[i].trim(), values[i].trim());
                    }
                    bssidData.add(rowMap);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter untuk mengambil data BSSID
    public List<HashMap<String, String>> getData() {
        return bssidData;
    }
}
