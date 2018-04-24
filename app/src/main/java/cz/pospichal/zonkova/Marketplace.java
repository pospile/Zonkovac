package cz.pospichal.zonkova;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.JsonArray;
import com.iravul.swipecardview.RecyclerViewClickListener;
import com.iravul.swipecardview.SwipeCardAdapter;
import com.iravul.swipecardview.SwipeCardModel;

import net.orange_box.storebox.StoreBox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//Application main view with all loans loaded from api
public class Marketplace extends AppCompatActivity implements RecyclerViewClickListener {

    //JSON data from zonky endpoint converted to JSONArray
    private JSONArray array;

    //View is created, all setup code in this class
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace);

        //view with cog icon for settings
        ImageButton imgButtonSettings = findViewById(R.id.settingsBtn);

        //OnClick listener which opens settings dialog with input
        imgButtonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(Marketplace.this)
                        .title("Nastavení upozornění")
                        .cancelable(false)
                        .content("Prosím zadejte počet vteřin po kterých budete upozorněni na nové půjčky na zonky tržišti.")
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("Počet vteřin", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                //Number of seconds loaded from dialog, saving it into SharedPrefs
                                Log.e("SETTINGS", "Seconds changed to: " + input.toString());
                                PreferencesStorage storage = StoreBox.create(Marketplace.this, PreferencesStorage.class);
                                storage.setLimit(Integer.parseInt(input.toString()));

                                //TODO:// Line below is here mainly because of testing, this saves id of last obtained loan from endpoint, which means 0 resets recents downloads (may be deleted after testing)
                                storage.setLastId(0);

                                //Stopping service and starting it again for changed time to take place
                                stopService(new Intent(Marketplace.this, MarketplaceService.class));
                                MarketplaceService mSensorService = new MarketplaceService(getApplicationContext());
                                Intent mServiceIntent = new Intent(getApplicationContext(), mSensorService.getClass());
                                startService(mServiceIntent);
                            }
                        }).show();
            }
        });

        //Obtaining downloaded json from MainActivity.java
        Intent intent = getIntent();
        try {
            //Converting string into JSONArray
            array = new JSONArray(intent.getStringExtra("marketplace"));

            //Variable holding all cards with loans
            List<SwipeCardModel> swipeCardModels = new ArrayList<>();

            //Saving last obtained id to SharedPrefs in order to not notify user about loans he already seen.
            PreferencesStorage storage = StoreBox.create(Marketplace.this, PreferencesStorage.class);
            storage.setLastId(array.getJSONObject(0).getInt("id"));

            //Coming throught all loans obtained from endpoint and saving them into cards.
            for (int i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);


                String name = row.getString("name");
                //Rounding interest rate for case its not in two decimal format
                //Also supressing locale warning as this is not associated to function we need here (double rounding)
                @SuppressLint("DefaultLocale") String interestRate =  (String.format("%.2f", (row.getDouble("interestRate")*100)))+"%";
                String amount = ((int)row.getDouble("amount"))+" ,-";
                //Cutting stories to small descriptions
                String storyShort = row.getString("story").length() >= 90 ?  row.getString("story").substring(0, 90).replace("\n", "")+"..." : row.getString("story").replace("\n", "");
                String photoUrl = "https://api.zonky.cz"+row.getJSONArray("photos").getJSONObject(0).getString("url");

                SwipeCardModel swipeCardModel = new SwipeCardModel();

                //Setting card properties to be shown in recycler view
                swipeCardModel.setId(storyShort);
                swipeCardModel.setDescription(name);
                swipeCardModel.setPrice(interestRate + " p.a. / " + amount);
                swipeCardModel.setPhotoUrl(photoUrl);
                swipeCardModels.add(swipeCardModel);
            }

            //Recycler view basic settings to show loan cards
            SwipeCardAdapter swipeCardAdapter = new SwipeCardAdapter(Marketplace.this, swipeCardModels, Marketplace.this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(Marketplace.this, LinearLayoutManager.HORIZONTAL, false);
            RecyclerView recyclerView = findViewById(R.id.marketplaceRecycler);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(swipeCardAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        //Decline access to back button, we dont want him to leave our app like that.
        //super.onBackPressed();
    }

    @Override
    public void recyclerViewListClicked(View view, int i) {
        try {
            //Opening loan activity with user selected loan.
            Intent intent = new Intent(Marketplace.this, Loan.class);
            //this gives over the details about selected loan to next activity.
            intent.putExtra("loan", array.getString(i));
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
