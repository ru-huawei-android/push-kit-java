package com.sample.huawei.pushkit;

import android.content.Intent;
import android.util.Log;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

import java.util.Map;

public class PushService extends HmsMessageService {

    private final String TAG = PushService.class.getSimpleName();

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        sendToken(token);
    }

    private void sendToken(String token) {
        Intent intent = new Intent("GET_HMS_TOKEN_ACTION");
        intent.putExtra("token", token);
        sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i(TAG, "onMessageReceived() data: ");
        for (Map.Entry<String, String> entry : remoteMessage.getDataOfMap().entrySet()) {
            Log.i(TAG, "key:" + entry.getKey() + "; value: " + entry.getValue());
        }
    }
}
