package com.originalstocks.missedcallnotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.util.Date;

public class CallReceiver extends BroadcastReceiver {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing

    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
            } else {
                String stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }


                onCallStateChanged(context, state, number);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        //Toast.makeText(ctx, "Received call by : " + number + "at " + start, Toast.LENGTH_SHORT).show();

    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        //Toast.makeText(ctx, "outgoing call from : " + number + "at " + start, Toast.LENGTH_SHORT).show();

    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
       // Toast.makeText(ctx, "Received call ended by : " + number + "at " + start, Toast.LENGTH_SHORT).show();

    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        //Toast.makeText(ctx, "outgoing call ended from : " + number + "at " + start, Toast.LENGTH_SHORT).show();

    }

    protected void onMissedCall(Context ctx, String number, Date start) {

        Toast.makeText(ctx, "Missed call by : " + number + "at " + start, Toast.LENGTH_LONG).show();
        // Sending Info to MainActivity:
        Intent sendInfo = new Intent(ctx, MainActivity.class);
        // adding this flag will Actually opens the activity right after the missed call, this feature is used in truecaller where they show a custom dialog instead ! :)
        sendInfo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendInfo.putExtra("phoneNumber", number);
        ctx.startActivity(sendInfo);

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
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a missed call here.........
                    onMissedCall(context, savedNumber, callStartTime);
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }

}
