package com.dxablack.airhack;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.dxablack.KaliShellExecutor;

public class TerminalDialogFragment extends DialogFragment {

    private static final String ARG_COMMAND = "command";  // Key untuk extras command
    private KaliShellExecutor shellExecutor;
    private Activity con;
    private OnCloseClickedListener listener;
    private boolean autoClose = false;

    public static interface OnCloseClickedListener{
        void onClick(View view, int code);
    }

    public static TerminalDialogFragment newInstance(String command, Activity context) {
        TerminalDialogFragment fragment = new TerminalDialogFragment(context);

        Bundle args = new Bundle();
        args.putString(ARG_COMMAND, command);  // Menyimpan command di bundle
        fragment.setArguments(args);
        return fragment;
    }

    public TerminalDialogFragment(Activity context){
        con = context;
    }
    public void setOnCloseClickedListener(OnCloseClickedListener l){
        listener = l;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Menggunakan layout custom dialog_terminal.xml tanpa input
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View terminalView = inflater.inflate(R.layout.dialog_terminal, null);

        // Output terminal
        TextView terminalOutput = terminalView.findViewById(R.id.terminal_output);
        terminalOutput.setMovementMethod(new ScrollingMovementMethod());

        // Mengambil command dari Bundle
        String command = getArguments() != null ? getArguments().getString(ARG_COMMAND) : "";
        Button button = terminalView.findViewById(R.id.frag_close);

        // Inisialisasi ShellExecutor
        shellExecutor = new KaliShellExecutor(con);
        shellExecutor.setOutputListener(new KaliShellExecutor.OutputListener() {
            @Override
            public void onNewOutput(String outputLine) {
                requireActivity().runOnUiThread(() -> {
                    terminalOutput.append(outputLine + "\n");
                    scrollToBottom(terminalOutput);
                });
            }

            @Override
            public void onError(String errorLine) {
                requireActivity().runOnUiThread(() -> {
                    terminalOutput.append("[Error] " + errorLine + "\n");
                    scrollToBottom(terminalOutput);
                    con.finishAffinity();
                    closeDialogIfAutoCloseActive();
                });
            }

            @Override
            public void onCommandFinished() {
                requireActivity().runOnUiThread(() -> {
                    terminalOutput.append("\n[Command finished - Click close to dismiss terminal]\n");
                    scrollToBottom(terminalOutput);
                    button.setVisibility(View.VISIBLE);
                    closeDialogIfAutoCloseActive();
                });
            }
        });

        // Jika ada command, langsung dieksekusi secara asinkron
        if (!command.isEmpty()) {
            shellExecutor.runKaliRootAsync(command);  // Menjalankan perintah secara asinkron
        }

        // Membuat dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setCancelable(false);
        builder.setView(terminalView)
                .setTitle("Mini Terminal");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shellExecutor.stopProcess();  // Menghentikan proses jika dialog ditutup
                getDialog().dismiss();
                if (listener != null) {
                    listener.onClick(view, shellExecutor.getCode());
                }
            }
        });
        return builder.create();
    }
    public void setAutoClose(boolean b){
        autoClose = b;
    }
    private void closeDialogIfAutoCloseActive(){
        if (autoClose){
            getDialog().dismiss();
            if (listener != null) {
                listener.onClick(null, shellExecutor.getCode());
            }
        }
    }
    // Untuk auto-scroll TextView ke bagian bawah setiap kali output baru diterima
    private void scrollToBottom(TextView terminalOutput) {
        if (terminalOutput.getLayout() != null) {  // Cek apakah layout sudah tersedia
            final int scrollAmount = terminalOutput.getLayout().getLineTop(terminalOutput.getLineCount()) - terminalOutput.getHeight();
            if (scrollAmount > 0) {
                terminalOutput.scrollTo(0, scrollAmount);
            } else {
                terminalOutput.scrollTo(0, 0);
            }
        }
    }

}
