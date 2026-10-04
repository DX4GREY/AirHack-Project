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
import com.google.android.material.button.MaterialButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;

import com.dxablack.DxaActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.TextScale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private MaterialButton refIntf;
    private FloatingActionButton fab, goScan;
    private boolean isAttackModeSelected = false;
    private TextView viewNowCommand, outputView;
    private ViewGroup content;
    private boolean lastProcessRunningState = false;
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    private int parameterID = 2;
    private ArrayList<String> nowCommand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindView();
        refreshListInterface();

        addAttackModes();

        nowCommand = new ArrayList<>();
        nowCommand.add(AttackFunction.mainCommand());
        nowCommand.add(wifiInterface);

        if(attackSelectView.getChildCount() > 0) {
            ((RadioButton) attackSelectView.getChildAt(0)).setChecked(true);
            refreshAttackParameter();
        }

        attackSelectView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                refreshAttackParameter();
            }
        });
        linterface.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Tampilkan item yang dipilih
                if (ifList != null && position < ifList.size()) {
                    String selectedItem = ifList.get(position);
                    wifiInterface = selectedItem;
                }
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
    private void refreshAttackParameter(){
        attackId = attackSelectView.getCheckedRadioButtonId();
        addAttackParameter(attackId);
        isAttackModeSelected = true;
    }
    private void bindView(){
        shell = new KaliShellExecutor(MainActivity.this);
        tickLoop = new TickLoop(250);
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
                Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                intent.putExtra("interface", wifiInterface);
                startActivity(intent);
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
                boolean running = shell.isProcessRunning();
                if (running != lastProcessRunningState) {
                    lastProcessRunningState = running;
                    if (running) {
                        fab.setImageResource(android.R.drawable.ic_media_pause);
                    } else {
                        fab.setImageResource(android.R.drawable.ic_media_play);
                    }
                    View[] views = {fab};
                    setEnabledAllView(content, views, !running);
                }
                fab.setEnabled(isAttackModeSelected);
                if (nowCommand != null && nowCommand.size() > 1 && wifiInterface != null) {
                    nowCommand.set(1, wifiInterface);
                }
                String currentCmd = getNowCommand();
                viewNowCommand.setText((currentCmd.isEmpty() ? "" : currentCmd));
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
        refIntf.setEnabled(false);
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<HashMap<String, String>> itemList = InterfaceManager.getListInterface(MainActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refIntf.setEnabled(true);
                        if (itemList != null && !itemList.isEmpty()){
                            ifList = new ArrayList<>();
                            ArrayList<String> adapterList = new ArrayList<>();
                            int monitorIndex = -1;

                            for (int i = 0; i < itemList.size(); i++) {
                                HashMap<String, String> item = itemList.get(i);
                                String iface = item.get("interface");
                                String driver = item.get("driver");
                                boolean isMon = "true".equalsIgnoreCase(item.get("isMonitor"));

                                ifList.add(iface);
                                String displayLabel = iface + " : " + driver + (isMon ? " [MONITOR]" : "");
                                adapterList.add(displayLabel);

                                if (isMon && monitorIndex == -1) {
                                    monitorIndex = i;
                                }
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, adapterList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            linterface.setAdapter(adapter);

                            if (monitorIndex != -1) {
                                linterface.setSelection(monitorIndex);
                                wifiInterface = ifList.get(monitorIndex);
                                Toast.makeText(MainActivity.this, "Monitor interface detected: " + wifiInterface, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    private void addAttackModes() {
        String[] options = AttackFunction.attackModes();
        for (String option : options) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option.split(";")[2]);
            radioButton.setId(Integer.parseInt(option.split(";")[0]));
            radioButton.setTextSize(14);
            radioButton.setPadding(12, 10, 12, 10);
            attackSelectView.addView(radioButton);
        }
    }
    private void addAttackParameter(int atkId) {
        paramLayout.setVisibility(View.VISIBLE);
        nowCommand = new ArrayList<>();
        nowCommand.add(AttackFunction.mainCommand());
        nowCommand.add(wifiInterface);
        nowCommand.add(getAttackModeFromID(atkId));

        parameterID = 3;
        attackParameterView.removeAllViews();
        String[] options = AttackFunction.attackParameter();
        for (String option : options) {
            String[] splitted = option.split(";");
            int id = Integer.parseInt(splitted[0]);
            if (atkId == id) {
                if (splitted.length >= 5) {
                    String viewName = splitted[1];
                    String paramCommand = splitted[2];
                    String label = splitted[3];
                    String helpParam = splitted[4];

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

    private String getInterfaceSuggestions() {
        String tmp = "";
        ShellExecutor shellExecutor = new ShellExecutor(false);

        for (String iface : ifList) {
            iface = iface.replaceAll("\n", "").replaceAll("\\s+", "");
            String cmd = "iw " + iface + " info";
            Log.d("main", "getInterfaceSuggestions: " + cmd);

            shellExecutor.startProcessAsRoot(cmd);

            String output = shellExecutor.getLastOutput();
            Log.d("main", "Shell output: " + output);

            if (iface.contains("0") && !iface.contains("p2p") && output.contains("phy 0")) {
                tmp = iface;
                Log.d("main", "getInterfaceSuggestions: Successfully add " + tmp);
                break;
            }
        }

        return tmp.replaceAll("\\s+", "");
    }

    private void addParamEditText(int paramId, String labelText, String helpText, String paramCommand, String examples){
        TextView label = new TextView(MainActivity.this);
        label.setText(String.join(" - ", labelText + " (%s)".replace("%s", paramCommand.replaceAll("-", "")) , helpText));
        label.setTextSize(12);
        label.setTextColor(getColor(android.R.color.tab_indicator_text));
        label.setPadding(4, 10, 4, 4);

        EditText prompt = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 4, 0, 10);
        prompt.setLayoutParams(lp);
        prompt.setHint(examples);
        prompt.setTextSize(14);
        prompt.setBackgroundResource(R.drawable.bg_param_input);
        prompt.setPadding(16, 12, 16, 12);
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
        CheckBox checkBox = new CheckBox(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 4, 0, 8);
        checkBox.setLayoutParams(lp);
        checkBox.setText(labelText + " (%s)".replace("%s", paramCommand.replaceAll("-", "")));
        checkBox.setTextSize(14);
        checkBox.setPadding(12, 8, 12, 8);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                nowCommand.set(paramId, paramCommand);
            } else {
                nowCommand.set(paramId, "");
            }
        });

        attackParameterView.addView(checkBox);
    }

    private String getNowCommand(){
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < nowCommand.size(); i++) {
            tmp.append(nowCommand.get(i)).append(" ");
        }
        return tmp.toString().replaceAll("\\s+", " ");
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
            @Override
            public void onClick(View view) {
                if (shell.isProcessRunning()){
                    shell.stopProcess();
                }else {
                    shell.runKaliRootAsync(getNowCommand());
                }
            }
        };
    }
    private void addMessage(String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                outputView.setText(message);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshListInterface();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tickLoop != null) {
            tickLoop.stop();
        }
        backgroundExecutor.shutdown();
    }
}
