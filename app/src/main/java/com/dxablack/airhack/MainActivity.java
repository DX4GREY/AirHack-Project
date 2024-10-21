package com.dxablack.airhack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dxablack.AttackFunction;
import com.dxablack.InterfaceManager;
import com.dxablack.KaliShellExecutor;
import com.dxablack.ShellExecutor;
import com.dxablack.TickLoop;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;

import com.dxablack.DxaActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.TextScale;

import java.util.ArrayList;

public class MainActivity extends DxaActivity {

    private TickLoop tickLoop;
    private KaliShellExecutor shell;
    private RadioGroup attackSelectView;
    private LinearLayout attackParameterView;
    private LinearLayout paramLayout;
    private int attackId;
    private String wifiInterface = "wlan0";
    private Spinner linterface;
    private ArrayList<String> ifList;
    private Button refIntf, goScan;
    private FloatingActionButton fab;
    private boolean isAttackModeSelected = false;
    private TextView viewNowCommand, outputView;
    private ViewGroup content;

    private int parameterID = 2;
    private ArrayList<String> nowCommand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bindView();
        refreshListInterface();

        addAttackModes();

        nowCommand = new ArrayList<>();
        nowCommand.add(AttackFunction.mainCommand());
        nowCommand.add(wifiInterface);

        attackSelectView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                attackId = attackSelectView.getCheckedRadioButtonId();
                addAttackParameter(attackId);
                isAttackModeSelected = true;
            }
        });
        linterface.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Tampilkan item yang dipilih
                String selectedItem = ifList.get(position);
                wifiInterface = selectedItem;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Lakukan sesuatu jika tidak ada item yang dipilih
            }
        });
        refIntf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshListInterface();
            }
        });
    }

    private void bindView(){
        shell = new KaliShellExecutor(MainActivity.this);
        tickLoop = new TickLoop();
        goScan = findViewById(R.id.next_scan_ap);
        outputView = findViewById(R.id.output_attack);
        fab = findViewById(R.id.fab);
        attackSelectView = findViewById(R.id.attack_modes);
        attackParameterView = findViewById(R.id.attack_parameter);
        linterface = findViewById(R.id.list_interface);
        refIntf = findViewById(R.id.refresh_interface);
        paramLayout = findViewById(R.id.param_layout);
        viewNowCommand = findViewById(R.id.now_command);
        content = findViewById(R.id.root_layout);
        goScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ifList.contains("wlan0") && !wifiInterface.contains("wlan0")){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Interface suggestions")
                            .setMessage("Use \"wlan0\" for scanning experience?")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                                    intent.putExtra("interface","wlan0");
                                    startActivity(intent);
                                }
                            })
                            .setNeutralButton("USE SELECTED INTERFACE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                                    intent.putExtra("interface",wifiInterface);
                                    startActivity(intent);
                                }
                            })
                            .show(); // Tampilkan dialog
                }else{
                    Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                    intent.putExtra("interface",wifiInterface);
                    startActivity(intent);
                }
            }
        });
        shell.setOutputListener(new ShellExecutor.OutputListener(){

            @Override
            public void onNewOutput(String outputLine) {
                addMessage(outputLine);
            }

            @Override
            public void onError(String errorLine) {
                addMessage(errorLine);
            }

            @Override
            public void onCommandFinished() {
                addMessage("[ Attack done - Created by Dx4 ]");
            }
        });
        tickLoop.setOnTickListener(new TickLoop.OnTickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onTick() {
                if (shell.isProcessRunning()){
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                }else{
                    fab.setImageResource(android.R.drawable.ic_media_play);
                }
                View[] views = {fab};
                setEnabledAllView(content, views, !shell.isProcessRunning());
                fab.setEnabled(isAttackModeSelected);
                nowCommand.set(1,wifiInterface);
                viewNowCommand.setText((getNowCommand().isEmpty() ? "" : getNowCommand()));
            }

        });
        tickLoop.start();
        fab.setOnClickListener(attackStart());
    }

    private void setEnabledAllView(View view, View[] f, boolean enabled){
        if (view instanceof ViewGroup){
            ViewGroup viewGroup = (ViewGroup) view;

            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childView = viewGroup.getChildAt(i);
                setEnabledAllView(childView, f, enabled);
            }
        }else{
            for (View r : f){
                if (view != r){
                    view.setEnabled(enabled);
                    view.setClickable(enabled);
                }
            }
        }
    }

    private void refreshListInterface(){
        String[] itemList = InterfaceManager.getListInterface(MainActivity.this);
        ifList = new ArrayList<>();
        for (int i = 0; i < itemList.length; i++) {
            ifList.add(itemList[i]);
        }
        // Membuat adapter untuk Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ifList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Menambahkan adapter ke Spinner
        linterface.setAdapter(adapter);
    }

    private void addAttackModes() {
        // Example array of options
        String[] options = AttackFunction.attackModes();
        for (String option : options) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option.split(";")[2]);
            radioButton.setId(Integer.parseInt(option.split(";")[0]));
            attackSelectView.addView(radioButton);
        }
    }
    private void addAttackParameter(int atkId) {
        paramLayout.setVisibility(View.VISIBLE);
        nowCommand = new ArrayList<>();
        nowCommand.add(AttackFunction.mainCommand());
        nowCommand.add(wifiInterface);
        nowCommand.add(getAttackModeFromID(atkId));

        // Example array of options
        parameterID = 3;
        attackParameterView.removeAllViews();
        String[] options = AttackFunction.attackParameter();
        for (String option : options) {
            String[] splitted = option.split(";");
            int id = Integer.parseInt(splitted[0]);
            if (atkId == id) {
                // Pastikan panjang array splitted cukup
                if (splitted.length >= 5) {
                    String viewName = splitted[1];
                    String paramCommand = splitted[2];
                    String label = splitted[3];
                    String helpParam = splitted[4];

                    // Cek apakah elemen ke-6 (example) ada
                    String example = (splitted.length >= 6) ? splitted[5] : "No example provided";

                    switch (viewName) {
                        case "edittext":
                            nowCommand.add("");
                            addParamEditText(parameterID, label, helpParam, paramCommand, example);
                            parameterID++;
                            break;
                        case "checkbox":
                            nowCommand.add("");
                            addParamCheckBox(parameterID, label, paramCommand);
                            parameterID++;
                            break;
                    }
                } else {
                    Log.e("MainActivity", "Invalid attack parameter definition: " + option);
                }
            }
        }
    }



    private void addParamEditText(int paramId, String labelText, String helpText, String paramCommand, String examples){
        TextView label = new TextView(MainActivity.this);
        label.setText(String.join(" - ", labelText + " (%s)".replace("%s", paramCommand.replaceAll("-", "")) , helpText));

        EditText prompt = new EditText(MainActivity.this);
        prompt.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        prompt.setHint(examples);
        prompt.setTextSize(14);
        prompt.setSingleLine();
        prompt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                nowCommand.set(paramId, paramCommand + " " + prompt.getText().toString());
                if (TextUtils.isEmpty(prompt.getText().toString())) nowCommand.set(paramId, "");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        attackParameterView.addView(label);
        attackParameterView.addView(prompt);
    }
    private void addParamCheckBox(int paramId, String labelText, String paramCommand) {
        // Membuat label untuk checkbox
//        TextView label = new TextView(MainActivity.this);
//        label.setText(labelText + " (%s)".replace("%s", paramCommand.replaceAll("-", "")));

        // Membuat checkbox
        CheckBox checkBox = new CheckBox(MainActivity.this);
        checkBox.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        checkBox.setText(labelText + " (%s)".replace("%s", paramCommand.replaceAll("-", "")));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                nowCommand.set(paramId, paramCommand);
            } else {
                nowCommand.set(paramId, ""); // Hapus command jika tidak dicentang
            }
        });

        // Menambahkan label dan checkbox ke layout parameter
//        attackParameterView.addView(label);
        attackParameterView.addView(checkBox);
    }

    private String getNowCommand(){
        String tmp = "";
        for (int i = 0; i < nowCommand.size(); i++) {
            tmp += nowCommand.get(i) + " ";
        }
        tmp = tmp.replaceAll("\\s+", " ");
        return tmp;
    }
    private String getAttackModeFromID(int id){
        String tmp = "";
        for (String listAttackModes : AttackFunction.attackModes()){
            String[] attackModeSplit = listAttackModes.split(";");
            if (String.valueOf(id).contains(attackModeSplit[0])){
                tmp = attackModeSplit[1];
            }
        }
        return tmp;
    }
    private View.OnClickListener attackStart(){
        return new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (shell.isProcessRunning()){
                    shell.stopProcess();
                }else{
                    Log.d("Command", getNowCommand());
                    startAttack();
                }
            }
        };
    }
    private void startAttack(){
        if (InterfaceManager.isInterfaceExist(MainActivity.this, wifiInterface)){
            if (InterfaceManager.checkMonitor(MainActivity.this, wifiInterface)){
                shell.runKaliRootAsync(getNowCommand());
            }else{
                showDialogAlertMonitorMode(wifiInterface);
            }
        }else{
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Error") // Judul dialog
                    .setMessage("Select another interface or replug in interface") // Pesan dialog
                    .setPositiveButton(android.R.string.ok,null)
                    .show(); // Tampilkan dialog
        }
    }
    private void showDialogAlertMonitorMode(String intf){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Warning") // Judul dialog
                .setMessage("Interface \"" + wifiInterface + "\" is not in monitor mode. Do you want change interface to monitor mode?") // Pesan dialog
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        TerminalDialogFragment terminalDialog = TerminalDialogFragment.newInstance("airmon-ng start " + intf, MainActivity.this);
                        terminalDialog.setCancelable(false);
                        terminalDialog.setAutoClose(true);
                        terminalDialog.setOnCloseClickedListener(new TerminalDialogFragment.OnCloseClickedListener() {
                            @Override
                            public void onClick(View view, int code) {
                                Toast.makeText(MainActivity.this, InterfaceManager.checkMonitor(getApplicationContext(), wifiInterface) ?
                                        "Success changed " +  wifiInterface + " to monitor!" : "Failed changed " +  wifiInterface + " to monitor!", Toast.LENGTH_SHORT).show();
                                int intf = linterface.getSelectedItemPosition();
                                refreshListInterface();
                                linterface.setSelection(intf);
                            }
                        });
                        terminalDialog.show(fragmentManager, "TerminalDialogFragment");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Aksi jika tombol Cancel ditekan
                        dialog.dismiss(); // Menutup dialog
                    }
                })
                .show(); // Tampilkan dialog
    }
    private void addMessage(String msg) {
        // append the new string
        outputView.setText(msg);
        // find the amount we need to scroll.  This works by
        // asking the TextView's internal layout for the position
        // of the final line and then subtracting the TextView's height
//        final int scrollAmount = outputView.getLayout().getLineTop(outputView.getLineCount()) - outputView.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
//        if (scrollAmount > 0)
//            outputView.scrollTo(0, scrollAmount);
//        else
//            outputView.scrollTo(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tickLoop.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        tickLoop.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tickLoop.start();
    }
}