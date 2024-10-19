package com.dxablack;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssetsHelper {

    public static void copyAssetsToData(Context context) {
        AssetManager assetManager = context.getAssets();
        File dataDir = new File(context.getFilesDir(), "");

        // Hapus semua yang ada di folder dataFiles sebelum menyalin asset baru
        deleteDirectoryContents(dataDir);

        try {
            // Mulai proses rekursif dengan menyalin semua yang ada di dalam assets
            copyAllAssets(assetManager, "", dataDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metode untuk menghapus semua isi direktori tanpa menghapus direktori itu sendiri
    private static void deleteDirectoryContents(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectoryContents(file); // Hapus konten direktori secara rekursif
                    }
                    file.delete(); // Hapus file atau direktori kosong
                }
            }
        }
    }

    private static void copyAllAssets(AssetManager assetManager, String path, File outDir) throws IOException {
        String[] assets = assetManager.list(path);
        if (assets.length == 0) {
            // Jika tidak ada file (ini adalah file, bukan direktori), salin file
            copyFile(assetManager, path, outDir);
        } else {
            // Jika ini adalah direktori, buat direktori di folder tujuan dan lanjutkan rekursi
            if (!outDir.exists()) {
                outDir.mkdirs(); // Buat direktori
                outDir.setExecutable(true, false);
                outDir.setReadable(true, false);
                outDir.setWritable(true, true);
            }
            for (String asset : assets) {
                String assetPath = path.isEmpty() ? asset : path + "/" + asset;
                File outFile = new File(outDir, asset);
                copyAllAssets(assetManager, assetPath, outFile);
            }
        }
    }

    private static void copyFile(AssetManager assetManager, String assetPath, File outFile) throws IOException {
        InputStream in = assetManager.open(assetPath);
        FileOutputStream out = new FileOutputStream(outFile);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.close();

        // Set file permissions to 755
        outFile.setExecutable(true, false);
        outFile.setReadable(true, false);
        outFile.setWritable(true, true);
    }
}
