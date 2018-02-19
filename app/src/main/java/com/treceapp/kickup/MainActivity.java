package com.treceapp.kickup;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends AppCompatActivity {

    RelativeLayout root;
    TextView tvCurrentKicks,tvMaxKicks;

    LinearLayout llBall;
    LinearLayout llDedo;
    Integer ballWidth;

    Display display;
    Point size;

    Handler handler,handlerAnim;

    Integer width,height,x,y,aceleracion,mov_lat,max_mov_lat;
    Integer defAceleracion = 30;
    Integer inclinacion = 5;
    Integer currentKicks = 0,maxKicks = 0;
    Integer currentLose = 0,losesToAd = 13;

    boolean touchActivo = false,gameOver = true, primerTutorialActivo = true, segundoTutorialActivo = true;

    Integer altoDedo = 0;

    AdView bannerAdView;
    InterstitialAd InterstitialAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = (RelativeLayout) findViewById(R.id.activity_main);
        tvCurrentKicks = (TextView) findViewById(R.id.currentKicks);
        tvMaxKicks = (TextView) findViewById(R.id.maxKicks);

        maxKicks = SharedPrefs.getInt(this,"maxKicks",0);
        primerTutorialActivo = SharedPrefs.getBoolean(this,"tuto",true);
        segundoTutorialActivo = SharedPrefs.getBoolean(this,"tuto",true);

        tvCurrentKicks.setText("Current: " + String.valueOf(currentKicks));
        tvMaxKicks.setText("Max: " + String.valueOf(maxKicks));

        llBall = new LinearLayout(getApplicationContext());
        llBall.setBackgroundResource(R.drawable.ic_ball0);

        llDedo = new LinearLayout(getApplicationContext());
        llDedo.setBackgroundResource(R.drawable.dedo);

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        ballWidth = width / 5;

        x = ( width / 2 ) - ( ballWidth / 2) ;
        y = x;
        aceleracion = 0;
        mov_lat = 0;

        max_mov_lat = ((x + (ballWidth / 2)) / 3);
        if(primerTutorialActivo){
            dibujarDedo();
        }

        bannerAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("CB22B9D8276A75EBCD4A818202161AC5").build();
        bannerAdView.loadAd(adRequest);

        InterstitialAdView = new InterstitialAd(this);
        InterstitialAdView.setAdUnitId("ca-app-pub-4856093486444224/7745166192");

        InterstitialAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();


        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {

                dibujarPelota();
                handler.postDelayed(this, 10);
            }
        };

        handler.postDelayed(r, 0);

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(!(!primerTutorialActivo && segundoTutorialActivo)){
                            if(gameOver){
                                gameOver = false;
                                touchActivo = true;
                                aceleracion = - 20;
                                if(primerTutorialActivo){
                                    root.removeView(llDedo);
                                    primerTutorialActivo = false;
                                }

                            }else{
                                if((event.getX() < x || event.getX() > x + ballWidth)
                                        || (event.getY() < y || event.getY() > y + ballWidth)){

                                    touchActivo = true;
                                }
                            }
                        }else{
                            touchActivo = true;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:

                        if(touchActivo)
                        if(!((event.getX() < x || event.getX() > x + ballWidth)
                                || (event.getY() < y || event.getY() > y + ballWidth))){

                            if(segundoTutorialActivo){
                                gameOver = false;
                                segundoTutorialActivo = false;
                                SharedPrefs.savePref(getApplicationContext(),"tuto",false);
                                root.removeView(llDedo);
                            }

                            mov_lat = (int)(((x + (ballWidth / 2)) - event.getX() ) / 3);
                            aceleracion = - defAceleracion + ((mov_lat / max_mov_lat) + inclinacion);
                            currentKicks ++;
                            tvCurrentKicks.setText("Current: " + String.valueOf(currentKicks));
                            touchActivo = false;
                        }

                        break;

                    case MotionEvent.ACTION_UP:
                        touchActivo = false;
                        break;
                }

                return true;
            }
        });

    }

    void dibujarDedo(){

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ballWidth, ballWidth);

        params2.leftMargin = x;
        params2.topMargin = y + ( 2 * ballWidth );

        root.addView(llDedo,params2);

    }

    void dibujarDedoAnimado(){

        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {

                root.removeView(llDedo);

                RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ballWidth, ballWidth);

                params2.leftMargin = x;
                params2.topMargin = y + ( 2 * ballWidth ) - ( altoDedo * ( ballWidth ) / 5 );

                if(altoDedo < 5){
                    altoDedo ++;
                }else{
                    altoDedo = 0;
                }

                root.addView(llDedo,params2);

                if(segundoTutorialActivo){
                    handler.postDelayed(this, 10);
                }else{
                    root.removeView(llDedo);
                }
            }
        };

        handler.postDelayed(r, 0);

    }

    void dibujarPelota(){

        root.removeView(llBall);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ballWidth, ballWidth);

        params.leftMargin = x;
        params.topMargin = y;

        root.addView(llBall,params);

        if(x <= 0 || x >= width - ballWidth){
            mov_lat = - mov_lat;
        }
        x = x + mov_lat;


        if(!gameOver){


            if(segundoTutorialActivo){
                if(y > x){
                    gameOver = true;
                    dibujarDedoAnimado();
                }

            }


            if(y >= height - ballWidth && aceleracion > 0){
                y = height - ballWidth;
                aceleracion = - aceleracion + 25;
                mov_lat = 0;

                x = ( width / 2 ) - ( ballWidth / 2) ;
                y = x;
                gameOver = true;
                touchActivo = false;

                if(currentLose >= losesToAd){
                    if (InterstitialAdView.isLoaded()) {
                        InterstitialAdView.show();
                        currentLose = 0;
                    }
                }else{
                    currentLose ++;
                }

                if(currentKicks > maxKicks){
                    maxKicks = currentKicks;
                    tvMaxKicks.setText("Max: " + String.valueOf(maxKicks));
                    SharedPrefs.save(this,"maxKicks",maxKicks);
                }
                currentKicks = 0;
                tvCurrentKicks.setText("Current: " + String.valueOf(currentKicks));
            }else{
                y = y + aceleracion;
                aceleracion = aceleracion + 1;
            }

        }

    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("CB22B9D8276A75EBCD4A818202161AC5")
                .build();

        InterstitialAdView.loadAd(adRequest);
    }

}
