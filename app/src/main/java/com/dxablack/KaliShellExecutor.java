package com.dxablack;
import android.content.Context;

public class KaliShellExecutor extends ShellExecutor{
    
    private Context con;
    
    public KaliShellExecutor(Context context){
        this.con = context;
        
    }
    public boolean runKaliRoot(String command){
        return startProcessAsRoot(con.getDataDir().getAbsolutePath() + "/files/bin/kali " + command);
    }
    public void runKaliRootAsync(String command){
        startProcessAsRootAsync(con.getDataDir().getAbsolutePath() + "/files/bin/kali " + command);
    }
}
