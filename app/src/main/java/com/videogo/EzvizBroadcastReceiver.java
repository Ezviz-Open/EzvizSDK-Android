package com.videogo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.openapi.EzvizAPI;
import com.videogo.util.Utils;
import ezviz.ezopensdkcommon.R;

public class EzvizBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "EzvizBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
     if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            EzvizAPI.getInstance().refreshNetwork();
        } else if (action.equals(Constant.ADD_DEVICE_SUCCESS_ACTION)) {
            String deviceId = intent.getStringExtra(IntentConsts.EXTRA_DEVICE_ID);
            Utils.showToast(context, context.getString(R.string.device_is_added, deviceId));
        } else if (action.equals(Constant.OAUTH_SUCCESS_ACTION)) {
            Log.i(TAG, "onReceive: OAUTH_SUCCESS_ACTION");
            Intent toIntent = new Intent(context, OptionActivity.class);
            toIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(toIntent);
        }
    }

}
