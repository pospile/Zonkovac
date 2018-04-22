package cz.pospichal.zonkova;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.dezlum.codelabs.getjson.GetJson;


import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private int dotsCount = 0;
    private String loadingText = "NAČÍTÁM TRŽIŠTĚ";
    private Timer dotsTimer;

    private ImageView imageViewLogo;
    private TextView loadingTextView;

    private Animation running_anim;
    private boolean cont = false;

    private String jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        MarketplaceService mSensorService = new MarketplaceService(getApplicationContext());
        Intent mServiceIntent = new Intent(getApplicationContext(), mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            startService(mServiceIntent);
        }


        try {
            jsonArray = new GetJson().AsString("https://api.zonky.cz/loans/marketplace");
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
        imageViewLogo = (ImageView) findViewById(R.id.zonkyLogo);
        loadingTextView = (TextView)findViewById(R.id.loadingTextView);
        running_anim = AnimationUtils.loadAnimation(this, R.anim.pulse);
        imageViewLogo.startAnimation(running_anim);
        StartTextLoader(1);
    }

    public void StartTextLoader (int sec) {
        dotsTimer = new Timer();
        dotsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                boolean allow_cont = false;
                if (jsonArray != null) {
                    allow_cont = true;
                }

                if (dotsCount != 4) {
                    loadingText += ".";
                    dotsCount++;
                }
                else {
                    loadingText = loadingText.substring(0, loadingText.length()-4);
                    dotsCount = 0;
                    if (allow_cont)
                        cont = true;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingTextView.setText(loadingText);
                        if (cont)
                        {
                            StopTextLoader();
                            ContinueAfterLoading();
                        }
                    }
                });
            }
        }, 0, sec*1000);
    }

    public void StopTextLoader () {
        dotsTimer.cancel();
        loadingTextView.setVisibility(View.GONE);
    }

    public void ContinueAfterLoading () {
        running_anim = AnimationUtils.loadAnimation(this, R.anim.proceed);
        imageViewLogo.startAnimation(running_anim);

        running_anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageViewLogo.setVisibility(View.GONE);
                MainActivity.this.finish();
                Intent intent = new Intent(MainActivity.this, Marketplace.class);
                intent.putExtra("marketplace", jsonArray);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

}
