package cz.pospichal.zonkova;

import android.content.Intent;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.gson.JsonArray;
import com.iravul.swipecardview.RecyclerViewClickListener;
import com.iravul.swipecardview.SwipeCardAdapter;
import com.iravul.swipecardview.SwipeCardModel;

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

        Intent intent = getIntent();
        try {
            array = new JSONArray(intent.getStringExtra("marketplace"));

            List<SwipeCardModel> swipeCardModels = new ArrayList<>();


            for (int i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);

                String name = row.getString("name");
                String interestRate = ((int)(row.getDouble("interestRate")*100))+" %";
                String amount = ((int)row.getDouble("amount"))+" ,-";
                String storyShort = row.getString("story").length() >= 60 ?  row.getString("story").substring(0, 60).replace("\n", "")+"..." : row.getString("story").replace("\n", "");
                String photoUrl = "https://api.zonky.cz"+row.getJSONArray("photos").getJSONObject(0).getString("url");

                SwipeCardModel swipeCardModel = new SwipeCardModel();

                swipeCardModel.setId(storyShort);
                swipeCardModel.setDescription(name);
                swipeCardModel.setPrice(interestRate + " p.a. / " + amount);
                swipeCardModel.setPhotoUrl(photoUrl);
                swipeCardModels.add(swipeCardModel);
            }
            /*
            for(int i=0;i<=10;i++){
                SwipeCardModel swipeCardModel = new SwipeCardModel();
                swipeCardModel.setId("ID-"+i);
                swipeCardModel.setTitle("Product-"+i);
                swipeCardModel.setDescription("ProductDesc-"+i);
                swipeCardModel.setPrice(i*10+" Euro");
                swipeCardModel.setPhotoUrl("https://s-media-cache-ak0.pinimg.com/736x/a3/99/24/a39924a3fcb7266ff7360af8a6ba2e98.jpg");
                swipeCardModels.add(swipeCardModel);
            }
            */

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

    }
}
