package com.sample.huawei.pushkitjava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.push.HmsMessaging;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    // topic should match the format:[\u4e00-\u9fa5\w-_.~%]{1,900}
    private final String TOPIC_NAME = "topic_name";
    private HmsMessaging hmsMessaging;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hmsMessaging = HmsMessaging.getInstance(getApplicationContext());

        // first way to obtain token
        obtainToken();
        // second way to obtain token
//        autoInitObtainingToken();

        findViewById(R.id.subscribeToTopic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscribeToTopic(TOPIC_NAME);
            }
        });

        findViewById(R.id.unsubscribeToTopic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unsubscribeToTopic(TOPIC_NAME);
            }
        });

        findViewById(R.id.deregisteringToken).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deregisteringToken();
            }
        });

        ((SwitchMaterial) findViewById(R.id.enablingPushSwitch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isEnable) {
                enablePushMessage(isEnable);
            }
        });

    }

    private void obtainToken() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(getApplicationContext()).getString("client/app_id");
                    String token = HmsInstanceId.getInstance(getApplicationContext()).getToken(appId, "HCM");
                    if (!TextUtils.isEmpty(token)) {
                        Log.i(TAG, "obtainToken() token: " + token);
                    }
                } catch (ApiException e) {
                    Log.e(TAG, "obtainToken() failed: " + e);
                }
            }
        }.start();
    }

    private void subscribeToTopic(final String topicName) {
        hmsMessaging.subscribe(topicName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        onCompleteListener(task.isComplete(), task.isSuccessful(), task.getException());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        onSuccessListener();
                        showMessage(getString(R.string.successful_subscribe, topicName));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        onFailureListener(e.getMessage());
                    }
                });
    }

    private void unsubscribeToTopic(final String topicName) {
        hmsMessaging.unsubscribe(topicName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        onCompleteListener(task.isComplete(), task.isSuccessful(), task.getException());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        onSuccessListener();
                        showMessage(getString(R.string.successful_unsubscribe, topicName));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        onFailureListener(e.getMessage());
                    }
                });
    }

    private void deregisteringToken() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(getApplicationContext()).getString("client/app_id");
                    HmsInstanceId.getInstance(getApplicationContext()).deleteToken(appId, "HCM");
                    Log.i(TAG, "deregisteringToken success");
                    showMessage("Deregistering token was successful");
                } catch (ApiException e) {
                    Log.e(TAG, "deregisteringToken failed." + e);
                    showMessage("Deregistering token failed");
                }
            }
        }.start();
    }

    // This function is supported only on Huawei devices whose EMUI version is 5.1 or later.
    private void enablePushMessage(boolean isEnable) {
        if (isEnable) {
            hmsMessaging.turnOnPush().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    showMessage("push was turn on");
                }
            });
        } else {
            hmsMessaging.turnOffPush().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    showMessage("push was turn off");
                }
            });
        }
    }

    private void onCompleteListener(Boolean isComplete, Boolean isSuccessful, Exception e) {
        Log.i(TAG, String.format("onCompleteListener() called \n" +
                        "isComplete - %b; isSuccessful - %b; exception - $s",
                isComplete,
                isSuccessful,
                e
        ));
    }

    private void onFailureListener(String message) {
        Log.e(TAG, "onFailureListener() called \n" +
                "message - " + message);
    }

    private void onSuccessListener() {
        Log.i(TAG, "onSuccessListener() called");
    }

    private void autoInitObtainingToken() {
        Receiver receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("GET_HMS_TOKEN_ACTION");
        MainActivity.this.registerReceiver(receiver, filter);
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_SHORT).show();
    }

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("GET_HMS_TOKEN_ACTION".equals(intent.getAction())) {
                Log.i(TAG, "obtainTokenViaReceiver() token: " + intent.getStringExtra("token"));
            }
        }
    }
}