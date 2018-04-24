package cz.pospichal.zonkova;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//TODO:// Service will be stopped after device restart, find a way to start it on device boot if possible
//This class reacts to notification about restart sent from MarketplaceService.java
public class RestartService extends BroadcastReceiver {
    //This method is called upon death of our service, so restart the service again
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(MarketplaceService.class.getSimpleName(), "Service Stops! Oops!!!!");
        //never let the service to be stopped, restart it again
        context.startService(new Intent(context, MarketplaceService.class));
    }
}
