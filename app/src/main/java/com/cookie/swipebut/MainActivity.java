package com.cookie.swipebut;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.cookie.swipebutton.SwipeBut;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SwipeBut swipeBut = (SwipeBut) findViewById(R.id.swipeBut);
        swipeBut.setIsBack(false);
        swipeBut.setSwipeListener(new SwipeBut.SwipeListener() {
            @Override
            public void onProgress(int num) {
                Log.d("SwipeListener:",num+"");
            }

            @Override
            public void onSwipeSuccess() {
                Log.d("SwipeListener:","onSwipeSuccess");

            }

            @Override
            public void onSwipeFail() {
                Log.d("SwipeListener:","onSwipeFail");

            }
        });
    }
}
