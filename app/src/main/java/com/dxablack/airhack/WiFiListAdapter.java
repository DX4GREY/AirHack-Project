package com.dxablack.airhack;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class WiFiListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<HashMap<String, String>> wifiList;
    private LayoutInflater inflater;

    public WiFiListAdapter(Context context, ArrayList<HashMap<String, String>> wifiList) {
        this.context = context;
        this.wifiList = wifiList;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return wifiList.size();
    }

    @Override
    public Object getItem(int position) {
        return wifiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_wifi, parent, false);
        }

        // Ambil elemen UI dari layout
        TextView ssidTextView = convertView.findViewById(R.id.ssidTextView);
        TextView bssidTextView = convertView.findViewById(R.id.bssidTextView);
        TextView signalTextView = convertView.findViewById(R.id.signalTextView);
        TextView frequencyTextView = convertView.findViewById(R.id.frequencyTextView);

        LinearLayout item = convertView.findViewById(R.id.item_ap);

        // Ambil data dari wifiList berdasarkan posisi
        HashMap<String, String> wifiInfo = wifiList.get(position);

        // Set nilai ke elemen UI
        ssidTextView.setText(wifiInfo.get("SSID"));
        bssidTextView.setText(wifiInfo.get("BSSID"));
        signalTextView.setText(" (" + wifiInfo.get("Signal") + ")");
        frequencyTextView.setText(" (" + wifiInfo.get("Frequency") + "Mhz)");

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, item);
                Menu menu = popupMenu.getMenu();

                // Menambahkan item ke menu
                menu.add(Menu.NONE, 1, 1, "Copy BSSID");
                menu.add(Menu.NONE, 2, 2, "Copy SSID");
                menu.add(Menu.NONE, 3, 3, "Copy Signal");

                // Menambahkan listener untuk menangani klik item
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 1:
                                copyToClipboard(context, wifiInfo.get("BSSID"));
                                return true;
                            case 2:
                                copyToClipboard(context, wifiInfo.get("SSID"));
                                return true;
                            case 3:
                                copyToClipboard(context, wifiInfo.get("Signal"));
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                // Menampilkan popup menu
                popupMenu.show();

            }
        });

        return convertView;
    }
    public void copyToClipboard(Context context, String text) {
        // Membuat ClipboardManager
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // Membuat ClipData dengan teks yang ingin disalin
        ClipData clip = ClipData.newPlainText("Copied Text", text);

        // Menyalin teks ke clipboard
        clipboard.setPrimaryClip(clip);

        // Menampilkan Toast untuk memberikan notifikasi ke pengguna
        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }

}
