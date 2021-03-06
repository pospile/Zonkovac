package cz.pospichal.zonkova;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Debug;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dezlum.codelabs.getjson.GetJson;
import com.google.gson.JsonObject;

import net.orange_box.storebox.StoreBox;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import br.com.goncalves.pugnotification.notification.PugNotification;

//Service class for handling notifications without any backend
//Nothing really hard, just request api endpoint and check if there is newer id than id saved in SharedPrefs.
public class MarketplaceService extends Service {

    //number of ticks from start of service or reset
    private int counter;

    //Timer that ticks every second
    private Timer timer;

    //Task which check if it is time to do network request
    private TimerTask timerTask;

    //number of ticks before new check is done against zonky endpoint
    private int limit;

    /**
     * Constructor for Notification service
     * @param applicationContext Just pass the context so we know that this is request from actual app
     */
    public MarketplaceService(Context applicationContext){
        super();
    }

    public MarketplaceService() {

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        PreferencesStorage storage = StoreBox.create(MarketplaceService.this, PreferencesStorage.class);
        //Check SharedPrefs for limit of ticks before zonky check
        limit = storage.getLimit();
        startTimer();
        return START_STICKY;
    }

    //Really important step. We need to be notified before our service is killed by OS, so send notification to rest of app and die peacefully.
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");

        //SEND Notification that we had been stopped
        Intent broadcastIntent = new Intent("ac.in.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        //die peacefully
        stoptimertask();
    }


    /**
     * Start timer service
     */
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    //Init timer task to check for endpoint
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.e("in timer", "in timer ++++  " + (counter++) + " " + (limit));
                if (counter > limit) {
                    counter = 0;
                    try {
                        String jsonString = new GetJson().AsString("https://api.zonky.cz/loans/marketplace");
                        JSONObject row0 = new JSONArray(jsonString).getJSONObject(0);
                        PreferencesStorage storage = StoreBox.create(MarketplaceService.this, PreferencesStorage.class);
                        //Checking saved id against remote id
                        if  (storage.getLastId() == row0.getInt("id"))
                        {
                            Log.e("No NEWS!","Check successfull, ids are identical.");
                        }
                        else
                        {
                            storage.setLastId(row0.getInt("id"));
                            Log.e("NEWS!", "Sending notification as soon as os handles it! New loan found by id.");
                            //Create new notification
                            NotificationHelper.getInstance().DrawNotification(MarketplaceService.this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * Stop timer in case this service must be restarted
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //just basic service stuff that needs to be here.
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
