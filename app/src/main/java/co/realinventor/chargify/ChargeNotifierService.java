package co.realinventor.chargify;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

public class ChargeNotifierService extends Service {
    Notification notification;
    public Timer timer;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification.Builder builder = new Notification.Builder(this);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setTicker("Ticker text");
        builder.setContentText("Your battery level is observed!");
        builder.setContentTitle("Chargify");
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);

        notification = builder.build();
        startForeground(1000, notification);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
                boolean isEnabled = sharedPref.getBoolean("is_app_enabled", false);
                int batteryPercentage = sharedPref.getInt("battery_percentage", 100);
                Log.d("NotifierThread:", "Working..");
                Log.d("Is enabled:", ""+isEnabled);
                Log.d("userBatteryPercentage:", ""+batteryPercentage);

                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                if(isEnabled){
                    Intent batteryStatus = registerReceiver(null, ifilter);
                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                    if(status == BatteryManager.BATTERY_STATUS_CHARGING) {
                        Log.d("Batter status", "CHarging");

                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                        float batteryPct = level / (float)scale;

                        int currentBatteryPercentage = (int)(batteryPct*100);

                        Log.d("CurrentBatteryPercent:", ""+currentBatteryPercentage);

                        //check if both are equal, if equal invoke sound notification
                        if(currentBatteryPercentage == batteryPercentage) {

                            Intent i = new Intent(getApplicationContext() , NotifyActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);

                            //stop everything
                            timer.cancel();
                            stopSelf();
                        }

                        if(currentBatteryPercentage > batteryPercentage){
                            timer.cancel();
                            stopSelf();
                        }

                    }
                    else{
                        Log.d("Batter status", "Not CHarging");
                        //stop everything
                        timer.cancel();
                        stopSelf();
                    }


                }
                else {
                    timer.cancel();
                    stopSelf();
                }
            }
        },1000, 60000);

        return Service.START_STICKY;
    }

    public ChargeNotifierService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d("Service ", "Fucking garbage collector");
    }

    @Override
    public void onDestroy() {
        Log.d("Service ", "Destroyed");
        super.onDestroy();
    }
}
