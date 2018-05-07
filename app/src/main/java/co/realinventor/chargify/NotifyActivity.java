package co.realinventor.chargify;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Timer;
import java.util.TimerTask;

public class NotifyActivity extends AppCompatActivity {

    private LottieAnimationView imageView;
    private Ringtone r;
    private boolean stopped = false;
    private Timer timer, animTimer;
    private Vibrator v;
    private TextView textView;
    private int repeatCount = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        imageView =(LottieAnimationView) findViewById(R.id.imageView);
        imageView.setAnimation(R.raw.error);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
        int batteryPercentage = sharedPref.getInt("battery_percentage", 100);

        textView = findViewById(R.id.textView);
        textView.setText("Your battery has reached "+batteryPercentage+"%");

        //notification audio
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//                        Uri notification = Uri.fromFile(new File("//android_asset/default_alarm.mp3"));
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        //Vibrate the phone
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1500,VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //deprecated in API 26
            v.vibrate(1500);
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(stopped){
                    timer.cancel();
                }
                else{
                    if(!r.isPlaying()){
                        r.play();
                        repeatCount++;
                    }
                    v.vibrate(1000);
                }
                if(repeatCount>=3){
                    timer.cancel();
                }
            }
        },2000, 2000);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r.stop();;
                imageView.pauseAnimation();
                textView.setVisibility(View.INVISIBLE);
                imageView.setBackgroundDrawable(null);
                imageView.setAnimation(R.raw.checked_done_);
                imageView.setRepeatCount(0);
                imageView.playAnimation();
                if(stopped){
                    NotifyActivity.this.finish();
                }
                stopped = true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(r!=null){
            if(r.isPlaying())
                r.stop();
        }
        this.finish();
    }
}
