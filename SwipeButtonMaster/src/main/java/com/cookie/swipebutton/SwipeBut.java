package com.cookie.swipebutton;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by cookie on 2017/6/22.
 */

public class SwipeBut extends FrameLayout {
    //animation
    private ValueAnimator forwardAnimator;
    private ValueAnimator backUpAnimator;

    private static final int CONTENT_HIDE_NUM = 20;
    private static final int SEEK_BAR_HIDE_NUM = 80;
    private static final int SUCCESS_NUM = 50;
    private static final int TOTAL_PROGRESS = 100;
    private static final int START_PROGRESS = 0;
    private static final int ANIMATION_DURATION = 500;
    private static final int ALLOW_START_SWIPE_NUM = 10;

    private int currentNum;

    private SeekBar mSeekBar;
    private TextView contentTextView;
    private FrameLayout frameConent;

    private SwipeListener swipeListener;
    //是否可滑动回去 true:可以
    private boolean isBack = true;


    public SwipeBut(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SwipeBut(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeBut(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SwipeBut(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {

        frameConent = new FrameLayout(context);
        frameConent.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            frameConent.setBackground(getResources().getDrawable(R.color.orange02));
        }

        contentTextView = new TextView(context);
        contentTextView.setText("到达约定地点");
        contentTextView.setGravity(Gravity.CENTER);
        contentTextView.setTextColor(getResources().getColor(android.R.color.white));
        contentTextView.setTextSize(30);
        frameConent.addView(contentTextView);

        addView(frameConent);

        addSeekBar(context);
    }


    private void addSeekBar(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.seek_bar,null);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mSeekBar.setBackground(null);
        }
        mSeekBar.setThumb(getResources().getDrawable(R.mipmap.right));
        mSeekBar.setProgressDrawable(null);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean index = true;
            private boolean first = true;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    if(first){
                        first = false;
                        if(progress>ALLOW_START_SWIPE_NUM){
                            index = false;
                            seekBar.setProgress(0);
                            return;
                        }else{
                            index = true;
                        }
                    }
                    if(!index){
                        seekBar.setProgress(0);
                        return;
                    }
                }

                currentNum = progress;

                Log.d("progress:",progress+"");
                if(fromUser){
                    if (progress >= CONTENT_HIDE_NUM) {
                        int newProgress = progress - CONTENT_HIDE_NUM;
                        float progressF = 1 - (float) newProgress / (float) (TOTAL_PROGRESS - CONTENT_HIDE_NUM);

                        frameConent.setAlpha(progressF);
                        frameConent.setScaleX(progressF);
                        frameConent.setScaleY(progressF);

                    }

                    if (progress >= SEEK_BAR_HIDE_NUM) {
                        int newProgress = progress - SEEK_BAR_HIDE_NUM;
                        float progressF = 1 - (float) newProgress / (float) (TOTAL_PROGRESS - SEEK_BAR_HIDE_NUM);
                        mSeekBar.setAlpha(progressF);
                    }
                }

                if(swipeListener!=null){
                    swipeListener.onProgress(progress);
                    if(currentNum == TOTAL_PROGRESS){
                        swipeListener.onSwipeSuccess();
                    }else if(currentNum == START_PROGRESS){
                        swipeListener.onSwipeFail();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                first = true;
                index = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setAutoProgress();
            }
        });

        addView(view, 1);
    }

    private void setAutoProgress() {
        if (currentNum > SUCCESS_NUM) {
            acutoForwardSwipe(currentNum);
        } else {
            autoBackupSwip(currentNum);
        }
    }

    private void autoBackupSwip(int progress){
        if(forwardAnimator!=null && forwardAnimator.isRunning()){
            forwardAnimator.cancel();
        }
        backUpAnimator=ValueAnimator.ofInt(progress,START_PROGRESS);
        backUpAnimator.setDuration(ANIMATION_DURATION);
        backUpAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int  progress = (int) animation.getAnimatedValue();
                if(progress> CONTENT_HIDE_NUM){
                    int newProgress = progress - CONTENT_HIDE_NUM;
                    float progressF = 1-(float) newProgress / (float) (TOTAL_PROGRESS - CONTENT_HIDE_NUM);
                    frameConent.setAlpha(progressF);
                    frameConent.setScaleX(progressF);
                    frameConent.setScaleY(progressF);
                }else{
                    frameConent.setAlpha(1);
                    frameConent.setScaleX(1);
                    frameConent.setScaleY(1);
                }
                mSeekBar.setProgress(progress);
                mSeekBar.setAlpha( 1 - ((float) (progress - CONTENT_HIDE_NUM) / (float) (TOTAL_PROGRESS - CONTENT_HIDE_NUM)));
            }
        });
        backUpAnimator.start();
    }

    private void acutoForwardSwipe(int progress) {
        if(backUpAnimator!=null && backUpAnimator.isRunning()){
            backUpAnimator.cancel();
        }
        forwardAnimator =ValueAnimator.ofInt(progress,TOTAL_PROGRESS);
        forwardAnimator.setDuration(ANIMATION_DURATION);
        forwardAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                int  progress = (int) animation.getAnimatedValue();

                int newProgress = progress - CONTENT_HIDE_NUM;
                float progressF = 1 - (float) newProgress / (float) (TOTAL_PROGRESS - CONTENT_HIDE_NUM);
                frameConent.setAlpha(progressF);
                frameConent.setScaleX(progressF);
                frameConent.setScaleY(progressF);

                mSeekBar.setProgress(progress);

                if (progress >= SEEK_BAR_HIDE_NUM) {
                    mSeekBar.setAlpha( 1 - ((float) (progress - SEEK_BAR_HIDE_NUM) / (float) (TOTAL_PROGRESS - SEEK_BAR_HIDE_NUM)));
                }
            }
        });
        forwardAnimator.start();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!isBack && currentNum>SUCCESS_NUM){
            return true;
        }else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    public void setContentText(String contentText){
        if(contentText!=null){
            contentTextView.setText(contentText);
        }
    }

    public void setThumb(Drawable drawable){
        if(mSeekBar!=null){
            mSeekBar.setThumb(drawable);
        }
    }

    //设置滑动按钮到初始位置
    public void setStart(){
        if(mSeekBar!=null){
            mSeekBar.setProgress(0);
            currentNum = 0;
            setAutoProgress();
        }
    }

    public void setIsBack(boolean isBack){
        this.isBack = isBack;
    }

    public void setSwipeListener(SwipeListener swipeListener){
        this.swipeListener = swipeListener;
    }

    public SwipeListener getSwipeListener() {
        if(swipeListener!=null){
            return swipeListener;
        }else{
            throw new RuntimeException("SwipeListener is null");
        }
    };

    public interface SwipeListener{
        public void onProgress(int num);

        public void onSwipeSuccess();

        public void onSwipeFail();
    }
}
