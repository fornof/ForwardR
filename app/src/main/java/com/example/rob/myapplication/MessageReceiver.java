package com.example.rob.myapplication;

import android.content.Context;
import android.util.Log;

import com.xlythe.textmanager.text.Text;
import com.xlythe.textmanager.text.TextReceiver;

public class MessageReceiver extends TextReceiver {
    @Override
    public void onMessageReceived(Context context, Text text) {
        Log.d("robert", "got to message receiver");
    }
}
