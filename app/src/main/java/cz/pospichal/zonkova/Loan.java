package cz.pospichal.zonkova;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.ramotion.fluidslider.FluidSlider;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import in.shadowfax.proswipebutton.ProSwipeButton;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

//Loan detail activity. Lots and lots of view handlings
public class Loan extends AppCompatActivity {


    int amount;
    JSONObject loan;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan);

        //Getting json string from previos activity (MarketPlace.java)
        Intent intent = getIntent();
        try {
            //converting string to JSONObject
            loan = new JSONObject(intent.getStringExtra("loan"));

            //Huge amount of view handling to set values into it. Can be made prettier with same library handling this ugly piece of code
            final ScrollView scroller = findViewById(R.id.scrollView);
            TextView loanName = findViewById(R.id.headerView);
            ImageView loanImage = findViewById(R.id.loanImage);
            TextView loanStory = findViewById(R.id.loanStory);
            TextView loanAmount = findViewById(R.id.finAmount);
            TextView loanInterest = findViewById(R.id.finInterest);
            TextView loanTerm = findViewById(R.id.finTerm);
            TextView loanUser = findViewById(R.id.finName);
            final ProSwipeButton loanOpen = findViewById(R.id.openLoan);

            final CardView incomeCard = findViewById(R.id.incomeCard);
            final TextView incomeText = findViewById(R.id.incomeText);

            final FluidSlider slider = findViewById(R.id.investSlider);

            //investment slider handler, setting its initial value, calculating real value from its return (float [0-1] -> money [200 - 5000])
            slider.setBubbleText("2500");
            slider.setPositionListener(new Function1<Float, Unit>() {
                @Override
                public Unit invoke(Float aFloat) {
                    int investice = Math.round(200 + (4800*aFloat));
                    try {
                        //showing income card if not yet showed
                        if (incomeCard.getVisibility() == View.GONE || incomeCard.getVisibility() == View.INVISIBLE)
                        {
                            incomeCard.setVisibility(View.VISIBLE);
                        }

                        //Rounding calculated result from CalcLoanIncome (see below) to 2 decimals and rendering it into income card
                        incomeText.setText(String.format("%.2f",CalcLoanIncome())+" Kč");
                        scroller.post(new Runnable() {
                            @Override
                            public void run() {
                                //scroll the scroll view to down - to see the income card
                                scroller.fullScroll(View.FOCUS_DOWN);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    slider.setBubbleText(investice+"");
                    amount = investice;
                    return Unit.INSTANCE;
                }
            });

            loanOpen.setOnSwipeListener(new ProSwipeButton.OnSwipeListener() {
                @Override
                public void onSwipeConfirm() {
                    try {
                        //if user wants to invest into this loan, open browser with https://app.zonky.cz and let him, until this function is integrated into this app
                        //TODO:// Let user invest into loan in this app
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(loan.getString("url")));
                        startActivity(browserIntent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            //Loading image from zonky endpoint into imageview
            Ion.with(loanImage)
                    .load("https://api.zonky.cz"+loan.getJSONArray("photos").getJSONObject(0).getString("url"));

            //settings all the other data into loan view (almost the same as in MarketPlace view)
            loanName.setText(loan.getString("name"));
            loanStory.setText(loan.getString("story"));
            loanUser.setText("Údaje " + loan.getString("nickName")+"ho ");
            loanInterest.setText("který žádá " + ((int)loan.getDouble("amount"))+",-");
            loanAmount.setText("za: " + (String.format("%.2f", (loan.getDouble("interestRate")*100)))+"% p.a.");
            loanTerm.setText("na " + (loan.getInt("termInMonths")/12)+" let");



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //TODO:// Rewrite this function to be general, not tied with this class
    /**
     * Function returns expected return from loans as presented by zonky.cz (6.3% per year)
     * @return (double) amount of money made from specified amount of money
     * @throws JSONException termInMonths in json source is not specified
     */
    public double CalcLoanIncome () throws JSONException {
        double interest = 0.063;
        return amount * interest * loan.getInt("termInMonths")/12;
    }
}
