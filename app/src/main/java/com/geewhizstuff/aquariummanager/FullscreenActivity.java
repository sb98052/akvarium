package com.geewhizstuff.aquariummanager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.multidex.MultiDex;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.id.input;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    public ArrayList<Fish> fishes;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.

        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fishes = new ArrayList<Fish>();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
            for (Fish f:fishes) {
                f.update_position();
            }
        }}, 500, 500);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);


        // Set up the user interaction to manually show or hide the system UI.
        /*mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
*/
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar

        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void addFish(int x, int y) {
        Fish newFish = null;
        final Dialog newFishDialog = new Dialog(this);
        newFishDialog.setTitle("Add fish");
        newFishDialog.setContentView(R.layout.add_fish_dialog);
        final int x_ = x;
        final int y_ = y;
        final Context context = this;

        final ImageView fishImage = (ImageView)newFishDialog.findViewById(R.id.fishImage);
        fishImage.setClickable(true);
        fishImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog selectFishDialog = new Dialog(context);
                selectFishDialog.setTitle("Select fish");
                selectFishDialog.setContentView(R.layout.select_fish_dialog);
                ImageView.OnClickListener listener = new ImageView.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fishImage.setImageDrawable(((ImageView) v).getDrawable());
                        selectFishDialog.cancel();
                    }
                };
                selectFishDialog.findViewById(R.id.button_gupka).setOnClickListener(listener);
                selectFishDialog.findViewById(R.id.button_gurama).setOnClickListener(listener);
                selectFishDialog.findViewById(R.id.button_prisavnik).setOnClickListener(listener);
                selectFishDialog.findViewById(R.id.button_skalar).setOnClickListener(listener);
                selectFishDialog.show();
            }
        });

        Button cancelButton = (Button) newFishDialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newFishDialog.cancel();
            }
        });

        Button addButton = (Button) newFishDialog.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int age = ((SeekBar)(newFishDialog.findViewById(R.id.ageBar))).getProgress();
                Fish.Gender gender = Fish.Gender.unknown;
                switch (((RadioGroup)(newFishDialog.findViewById(R.id.genderRadio))).getCheckedRadioButtonId()) {
                    case R.id.radio_male : gender = Fish.Gender.male; break;
                    case R.id.radio_female : gender = Fish.Gender.female; break;
                    case R.id.radio_dontknow : gender = Fish.Gender.unknown; break;
                }
                Fish f = null;
                if (((ImageView) newFishDialog.findViewById(R.id.fishImage)).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.gupka).getConstantState())) {
                    f = new Gupka(context, (ViewGroup) getWindow().getDecorView().getRootView(),x_,y_, age, gender);
                }
                else if (((ImageView) newFishDialog.findViewById(R.id.fishImage)).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.skalar).getConstantState())) {
                    f = new Skalar(context, (ViewGroup) getWindow().getDecorView().getRootView(),x_,y_, age, gender);
                }
                else if (((ImageView) newFishDialog.findViewById(R.id.fishImage)).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.prisavnik).getConstantState())) {
                    f = new Prisavnik(context, (ViewGroup) getWindow().getDecorView().getRootView(),x_,y_, age, gender);
                }
                else if (((ImageView) newFishDialog.findViewById(R.id.fishImage)).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.gurama).getConstantState())) {
                    f = new Gurama(context, (ViewGroup) getWindow().getDecorView().getRootView(),x_,y_, age, gender);
                }

                fishes.add(f);
                newFishDialog.cancel();
            }
        });

        final TextView textAge= (TextView) newFishDialog.findViewById(R.id.ageText);
        SeekBar ageBar = (SeekBar) newFishDialog.findViewById(R.id.ageBar);
        ageBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textAge.setText("Age: " + String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        newFishDialog.show();


    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                return true;
            case (MotionEvent.ACTION_MOVE) :
                return true;
            case (MotionEvent.ACTION_UP) :
                this.addFish((int)event.getX(),(int) event.getY());
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }
}
