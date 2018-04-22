package cz.pospichal.zonkova;

import android.content.Intent;
import android.media.Image;
import android.os.Debug;
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

public class Marketplace extends AppCompatActivity implements RecyclerViewClickListener {

    private JSONArray array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace);

        ImageButton imgButtonSettings = (ImageButton) findViewById(R.id.settingsBtn);

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
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                Log.e("NASTAVENI", "Pocet vterin se zmenil!" + input.toString());
                                PreferencesStorage storage = StoreBox.create(Marketplace.this, PreferencesStorage.class);
                                storage.setLimit(Integer.parseInt(input.toString()));
                                storage.setLastId(0);
                                stopService(new Intent(Marketplace.this, MarketplaceService.class));

                                MarketplaceService mSensorService = new MarketplaceService(getApplicationContext());
                                Intent mServiceIntent = new Intent(getApplicationContext(), mSensorService.getClass());
                                startService(mServiceIntent);
                            }
                        }).show();
            }
        });

        Intent intent = getIntent();
        try {
            array = new JSONArray(intent.getStringExtra("marketplace"));

            List<SwipeCardModel> swipeCardModels = new ArrayList<>();

            PreferencesStorage storage = StoreBox.create(Marketplace.this, PreferencesStorage.class);
            storage.setLastId(array.getJSONObject(0).getInt("id"));

            for (int i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);


                String name = row.getString("name");
                String interestRate =  (String.format("%.2f", (row.getDouble("interestRate")*100)))+"%";
                String amount = ((int)row.getDouble("amount"))+" ,-";
                String storyShort = row.getString("story").length() >= 90 ?  row.getString("story").substring(0, 90).replace("\n", "")+"..." : row.getString("story").replace("\n", "");
                String photoUrl = "https://api.zonky.cz"+row.getJSONArray("photos").getJSONObject(0).getString("url");

                SwipeCardModel swipeCardModel = new SwipeCardModel();

                swipeCardModel.setId(storyShort);
                swipeCardModel.setDescription(name);
                swipeCardModel.setPrice(interestRate + " p.a. / " + amount);
                swipeCardModel.setPhotoUrl(photoUrl);
                swipeCardModels.add(swipeCardModel);
            }

            SwipeCardAdapter swipeCardAdapter = new SwipeCardAdapter(Marketplace.this, swipeCardModels, Marketplace.this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(Marketplace.this, LinearLayoutManager.HORIZONTAL, false);
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.marketplaceRecycler);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(swipeCardAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        //Odepřít přístup k tlačítku zpět v této aktivitě.
        //super.onBackPressed();
    }

    @Override
    public void recyclerViewListClicked(View view, int i) {
        try {
            Intent intent = new Intent(Marketplace.this, Loan.class);
            intent.putExtra("loan", array.getString(i));
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
