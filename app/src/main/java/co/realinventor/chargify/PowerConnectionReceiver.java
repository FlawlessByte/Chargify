package co.realinventor.chargify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

public class PowerConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        String action = intent.getAction();
        Intent i = new Intent(context, ChargeNotifierService.class);
        if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
            // Do something when power connected
            context.startService(i);
        }
        else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            // Do something when power disconnected
            context.stopService(i);
        }

    }
}
