package com.dxablack;

import android.os.Handler;
import android.os.Looper;

public class TickLoop {

    private Handler handler;
    private Runnable tickRunnable;
    private int tickDelay = 100; // 100 ms = 0,1 second
    private OnTickListener onTickListener;

    // Interface for custom onTick logic
    public interface OnTickListener {
        void onTick();
    }
    public TickLoop(int delay){
        tickDelay = delay;
        init();
    }
    public TickLoop() {
        init();
    }
    private void init(){
        handler = new Handler(Looper.getMainLooper());

        tickRunnable = new Runnable() {
            @Override
            public void run() {
                // Call the listener on every tick, if it's set
                if (onTickListener != null) {
                    onTickListener.onTick();
                }

                // Schedule the next tick
                handler.postDelayed(this, tickDelay);
            }
        };
    }

    // Set the listener from outside
    public void setOnTickListener(OnTickListener listener) {
        this.onTickListener = listener;
    }

    // Start the tick loop
    public void start() {
        handler.postDelayed(tickRunnable, tickDelay);
    }

    // Stop the tick loop
    public void stop() {
        handler.removeCallbacks(tickRunnable);
    }
}
