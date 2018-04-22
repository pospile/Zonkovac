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

public class MarketplaceService extends Service {

    private int counter;
    private Context context;
    private int limit;

    public MarketplaceService(Context applicationContext){
        super();
        context = applicationContext;
    }

    public MarketplaceService() {

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        PreferencesStorage storage = StoreBox.create(MarketplaceService.this, PreferencesStorage.class);
        limit = storage.getLimit();
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("EXIT", "ondestroy!");

        Intent broadcastIntent = new Intent("ac.in.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

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
                        if  (storage.getLastId() == row0.getInt("id"))
                        {
                            Log.e("No NEWS!","Nepřišla nám žádná nová půjčka, kruci!");
                        }
                        else
                        {
                            storage.setLastId(row0.getInt("id"));
                            Log.e("NEWS!", "Přišla nám nová půjčka, chceš se mrknout?");
                            NotificationHelper.getInstance().DrawNotification(MarketplaceService.this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //NotificationHelper.getInstance().DrawNotification(MarketplaceService.this);
                }
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
