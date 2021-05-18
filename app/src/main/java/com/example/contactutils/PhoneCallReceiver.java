package com.example.contactutils;

import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;

import java.util.Date;
import java.util.Hashtable;
import java.util.Objects;

public class PhoneCallReceiver extends BroadcastReceiver {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (Objects.equals(intent.getAction(), "android.intent.action.NEW_OUTGOING_CALL")) {
                    savedNumber = Objects.requireNonNull(intent.getExtras()).getString("android.intent.extra.PHONE_NUMBER");
                } else {
                    String stateStr = Objects.requireNonNull(intent.getExtras()).getString(TelephonyManager.EXTRA_STATE);
                    String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    int state = 0;
                    if (stateStr != null) {
                        if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                            state = TelephonyManager.CALL_STATE_IDLE;
                        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                            state = TelephonyManager.CALL_STATE_OFFHOOK;
                        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                            state = TelephonyManager.CALL_STATE_RINGING;
                        }
                    }

                    onCallStateChanged(context, state, number);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected void onIncomingCallStarted(final Context ctx, String number, Date start) {
        if (SharedPref.getSelectedSaveContact(ctx, number) != null) {
            Hashtable<String, Hashtable<String, Object>> hashtable = SharedPref.getSelectedSaveContact(ctx, number);
            if (hashtable != null && !hashtable.isEmpty()) {
                if (hashtable.containsKey(number)) {
                    Hashtable<String, Object> contactDetailsHash = getContactDetailsHash(hashtable, number);
                    if (contactDetailsHash != null && !contactDetailsHash.isEmpty()) {
                        final Intent intent = new Intent(ctx, CustomDialog.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("phone_no", number);
                        intent.putExtra("name", SharedPref.getString(ctx, number + ctx.getString(R.string.contact_name)));

                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ctx.startActivity(intent);
                            }
                        }, 2000);
                    }
                }
            }
        }
    }


    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        System.exit(0);
    }

    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime);
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                }

            case TelephonyManager.CALL_STATE_IDLE:
                if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                }
        }
        lastState = state;
    }

    public Hashtable<String, Object> getContactDetailsHash(Hashtable<String, Hashtable<String, Object>> hashtable,
                                                           String mobileNum) {
        if (hashtable != null && !hashtable.isEmpty()) {
            if (hashtable.containsKey(mobileNum)) {
                return hashtable.get(mobileNum);
            }
        }
        return new Hashtable<>();
    }
}
