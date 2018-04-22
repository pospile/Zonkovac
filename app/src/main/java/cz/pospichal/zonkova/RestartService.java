package cz.pospichal.zonkova;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(MarketplaceService.class.getSimpleName(), "Service Stops! Oops!!!!");
        context.startService(new Intent(context, MarketplaceService.class));
    }
}
