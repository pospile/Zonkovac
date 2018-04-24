package cz.pospichal.zonkova;

import android.app.ActivityManager;
import android.content.ComponentName;
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


//Application entry point
public class MainActivity extends AppCompatActivity {

    //Local variable for dots animation on start
    private int dotsCount = 0;
    //Local variable for string (as local variable for simplicity, to localize it later use STRING resources)
    private String loadingText = "NAČÍTÁM TRŽIŠTĚ";
    //Timer handling dotsAnimation
    private Timer dotsTimer;

    //View handled by this activity
    private ImageView imageViewLogo;
    private TextView loadingTextView;

    //Animation handler for this activity
    private Animation running_anim;


    private boolean cont = false;

    //OUTPUT of this activity. This string is sent to Marketplace.java to handle drawing of loans for user.
    private String jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Check if Notification service is running, if not start it again
        MarketplaceService mSensorService = new MarketplaceService(getApplicationContext());
        Intent mServiceIntent = new Intent(getApplicationContext(), mSensorService.getClass());
        try {
            if (!isMyServiceRunning(mSensorService.getClass())) {
                //Starting service with loan checking logic
                startService(mServiceIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //Loading all loans from api endpoint
        try {
            jsonArray = new GetJson().AsString("https://api.zonky.cz/loans/marketplace");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Showing animation to user during loading of json from endpoint.
        setContentView(R.layout.activity_main);

        //loading views into variables to start appropriate animations.
        imageViewLogo = findViewById(R.id.zonkyLogo);
        loadingTextView = findViewById(R.id.loadingTextView);

        //Starting animaton saved in res/anims/puls (animated zonky guy)
        running_anim = AnimationUtils.loadAnimation(this, R.anim.pulse);
        imageViewLogo.startAnimation(running_anim);
        StartTextLoader(1);
    }

    /**
     * Function for starting loading dots animation
     * @param sec - number of seconds before next dot appear.
     */
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

    /**
     * Function for stopping dots animation and hiding its view out of layout.
     */
    public void StopTextLoader () {
        dotsTimer.cancel();
        loadingTextView.setVisibility(View.GONE);
    }

    /**
     * Function for moving into Marketplace view after JSON data is downloaded from api endpoint
     */
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

    /**
     * Function for checking if Notification service is running
     * @param serviceClass - Class of service which should be checked
     * @return (bool) running
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) throws Exception {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    Log.i ("isMyServiceRunning?", true+"");
                    return true;
                }
            }
        }
        else {
            throw new Exception("Activity manager seems to be null");
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

}
