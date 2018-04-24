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

public class Loan extends AppCompatActivity {

    int amount;
    JSONObject loan;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan);

        Intent intent = getIntent();
        try {
            loan = new JSONObject(intent.getStringExtra("loan"));

            final ScrollView scroller = (ScrollView)findViewById(R.id.scrollView);

            //Log.e("LOAN LOADED", loan.getString("story"));
            TextView loanName = (TextView)findViewById(R.id.headerView);
            ImageView loanImage = (ImageView)findViewById(R.id.loanImage);
            TextView loanStory = (TextView)findViewById(R.id.loanStory);
            TextView loanAmount = (TextView)findViewById(R.id.finAmount);
            TextView loanInterest = (TextView)findViewById(R.id.finInterest);
            TextView loanTerm = (TextView)findViewById(R.id.finTerm);
            TextView loanUser = (TextView)findViewById(R.id.finName);
            final ProSwipeButton loanOpen = (ProSwipeButton)findViewById(R.id.openLoan);

            final CardView incomeCard = (CardView)findViewById(R.id.incomeCard);
            final TextView incomeText = (TextView)findViewById(R.id.incomeText);

            final FluidSlider slider = findViewById(R.id.investSlider);

            slider.setBubbleText("2500");
            slider.setPositionListener(new Function1<Float, Unit>() {
                @Override
                public Unit invoke(Float aFloat) {
                    int investice = Math.round(200 + (4800*aFloat));
                    try {
                        incomeCard.setVisibility(View.VISIBLE);
                        incomeText.setText(String.format("%.2f",CalcLoanIncome())+" Kč");
                        scroller.post(new Runnable() {
                            @Override
                            public void run() {
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
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(loan.getString("url")));
                        startActivity(browserIntent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });


            Ion.with(loanImage)
                    .load("https://api.zonky.cz"+loan.getJSONArray("photos").getJSONObject(0).getString("url"));


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

    public double CalcLoanIncome () throws JSONException {
        double interest = 0.063;
        return amount * interest * loan.getInt("termInMonths")/12;
    }
}
