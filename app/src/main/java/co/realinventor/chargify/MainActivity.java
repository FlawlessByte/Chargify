package co.realinventor.chargify;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView textViewPercentage, textViewStatus;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        seekBar = findViewById(R.id.seekBar);
        textViewPercentage = findViewById(R.id.textViewPercentage);
        textViewStatus = findViewById(R.id.textViewStatus);

//        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isEnabled = sharedPref.getBoolean("is_app_enabled", false);
        int batteryPercentage = sharedPref.getInt("battery_percentage", 100);

        textViewPercentage.setText(batteryPercentage+"%");
        seekBar.setProgress(batteryPercentage);

        if(isEnabled){
            imageView.setBackgroundResource(R.drawable.power_button_green);
            textViewStatus.setText("Status : On");
        }
        else{
            imageView.setBackgroundResource(R.drawable.power_button_white);
            textViewStatus.setText("Status : Off");
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
                boolean isEnabled = sharedPref.getBoolean("is_app_enabled", false);

                SharedPreferences.Editor editor = sharedPref.edit();

                if(!isEnabled){
                    imageView.setBackgroundResource(R.drawable.power_button_green);
                    editor.putBoolean("is_app_enabled", true);
                    textViewStatus.setText("Status : On");

                    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = getApplication().registerReceiver(null, ifilter);
                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                    if(status == BatteryManager.BATTERY_STATUS_CHARGING) {
                        Log.d("Batter status", "CHarging");
                        Intent i = new Intent(MainActivity.this, ChargeNotifierService.class);
                        startService(i);
                    }
                    else{
                        Log.d("Batter status", "Not CHarging");
                    }
                }
                else{
                    imageView.setBackgroundResource(R.drawable.power_button_white);
                    editor.putBoolean("is_app_enabled", false);
                    textViewStatus.setText("Status : Off");
                }
                editor.commit();
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewPercentage.setText(progress+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
                SharedPreferences.Editor editor = sharedPref.edit();
                imageView.setBackgroundResource(R.drawable.power_button_white);
                editor.putBoolean("is_app_enabled", false);
                editor.commit();
                textViewStatus.setText("Status : Off");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

//                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
                SharedPreferences.Editor editor = sharedPref.edit();

                if(textViewPercentage.getText().equals("0%")){
                    seekBar.setProgress(1);
                    textViewPercentage.setText("1%");
                    editor.putInt("battery_percentage", 1);
                }
                else {
                    editor.putInt("battery_percentage", Integer.parseInt(textViewPercentage.getText().toString().replace("%", "")));
                }
                editor.commit();
            }
        });
    }

    @Override
    public void onBackPressed() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isEnabled = sharedPref.getBoolean("is_app_enabled", false);

        if(!isEnabled){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You haven't enabled the notification. You won't be notified unless you enable it. Are you sure want to exit without enabling the notification?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                            MainActivity.this.finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            builder.create().show();
        }
        else{
            super.onBackPressed();
        }
    }
}
