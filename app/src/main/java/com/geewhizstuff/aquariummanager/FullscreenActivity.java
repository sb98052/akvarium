package com.geewhizstuff.aquariummanager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

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
    MixpanelAPI mixpanel;

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
    private ArrayList<PendingIntent> pendingIntent = new ArrayList<>();

    private Dialog notification_dialog;

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

        notification_dialog = new Dialog(this);
        notification_dialog.setTitle("Notifications");
        notification_dialog.setContentView(R.layout.notification_dialog);
        ((Button)notification_dialog.findViewById(R.id.notification_ok)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject props = new JSONObject();
                    props.put("user action", "accept");
                    mixpanel.track("notification_dialog", props);
                } catch (JSONException e) {
                    Log.e("MYAPP", "Unable to add properties to JSONObject", e);
                }
                saveNotif();
                stopNotification();
                startNotification();
                notification_dialog.cancel();
            }
        });
        ((Button)notification_dialog.findViewById(R.id.notification_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadNotif();
                try {
                    JSONObject props = new JSONObject();
                    props.put("user action", "cancel");
                    mixpanel.track("notification_dialog", props);
                } catch (JSONException e) {
                    Log.e("MYAPP", "Unable to add properties to JSONObject", e);
                }
                notification_dialog.cancel();
            }
        });

        loadNotif();
        stopNotification();
        startNotification();

        String projectToken = "d9a1d392fe1c530a6a08454a9c4651f4"; // e.g.: "1ef7e30d2a58d27f4b90c42e31d6d7ad"
        mixpanel = MixpanelAPI.getInstance(this, projectToken);

        try {
            JSONObject props = new JSONObject();
            props.put("User action", "Application started");
            mixpanel.track("MainActivity - onCreate called", props);
        } catch (JSONException e) {
            Log.e("MYAPP", "Unable to add properties to JSONObject", e);
        }


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

    protected void saveNotif() {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput("notif.txt", Context.MODE_PRIVATE));
            if (((Switch)notification_dialog.findViewById(R.id.feed1_enabled)).isChecked()) outputStreamWriter.write("1\n");
            else outputStreamWriter.write("0\n");
            outputStreamWriter.write(((Button)notification_dialog.findViewById(R.id.feed1_time)).getText().toString()+"\n");

            if (((Switch)notification_dialog.findViewById(R.id.feed2_enabled)).isChecked()) outputStreamWriter.write("1\n");
            else outputStreamWriter.write("0\n");
            outputStreamWriter.write(((Button)notification_dialog.findViewById(R.id.feed2_time)).getText().toString()+"\n");

            if (((Switch)notification_dialog.findViewById(R.id.feed3_enabled)).isChecked()) outputStreamWriter.write("1\n");
            else outputStreamWriter.write("0\n");
            outputStreamWriter.write(((Button)notification_dialog.findViewById(R.id.feed3_time)).getText().toString()+"\n");

            if (((Switch)notification_dialog.findViewById(R.id.clean_enabled)).isChecked()) outputStreamWriter.write("1\n");
            else outputStreamWriter.write("0\n");
            outputStreamWriter.write(((Button)notification_dialog.findViewById(R.id.clean_time)).getText().toString()+"\n");
            outputStreamWriter.write(((EditText)notification_dialog.findViewById(R.id.clean_interval)).getText().toString()+"\n");

            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    protected void loadNotif() {
        try {
            InputStream inputStream = this.openFileInput("notif.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                ((Switch)notification_dialog.findViewById(R.id.feed1_enabled)).setChecked("1".compareTo(bufferedReader.readLine())==0);
                ((Button)notification_dialog.findViewById(R.id.feed1_time)).setText(bufferedReader.readLine());
                ((Switch)notification_dialog.findViewById(R.id.feed2_enabled)).setChecked("1".compareTo(bufferedReader.readLine())==0);
                ((Button)notification_dialog.findViewById(R.id.feed2_time)).setText(bufferedReader.readLine());
                ((Switch)notification_dialog.findViewById(R.id.feed3_enabled)).setChecked("1".compareTo(bufferedReader.readLine())==0);
                ((Button)notification_dialog.findViewById(R.id.feed3_time)).setText(bufferedReader.readLine());
                ((Switch)notification_dialog.findViewById(R.id.clean_enabled)).setChecked("1".compareTo(bufferedReader.readLine())==0);
                ((Button)notification_dialog.findViewById(R.id.clean_time)).setText(bufferedReader.readLine());
                ((EditText)notification_dialog.findViewById(R.id.clean_interval)).setText(bufferedReader.readLine());

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        catch (Exception e) {

        }
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
                try {
                    JSONObject props = new JSONObject();
                    props.put("Fish", "none");
                    mixpanel.track("Add fish", props);
                } catch (JSONException e) {
                    Log.e("MYAPP", "Unable to add properties to JSONObject", e);
                }
            }
        });

        Button addButton = (Button) newFishDialog.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                JSONObject props = new JSONObject();

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
                    try {
                        props.put("Fish", "Gupka");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if (((ImageView) newFishDialog.findViewById(R.id.fishImage)).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.skalar).getConstantState())) {
                    f = new Skalar(context, (ViewGroup) getWindow().getDecorView().getRootView(),x_,y_, age, gender);
                    try {
                        props.put("Fish", "Skalar");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if (((ImageView) newFishDialog.findViewById(R.id.fishImage)).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.prisavnik).getConstantState())) {
                    f = new Prisavnik(context, (ViewGroup) getWindow().getDecorView().getRootView(),x_,y_, age, gender);
                    try {
                        props.put("Fish", "Prisavnik");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if (((ImageView) newFishDialog.findViewById(R.id.fishImage)).getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.gurama).getConstantState())) {
                    f = new Gurama(context, (ViewGroup) getWindow().getDecorView().getRootView(),x_,y_, age, gender);
                    try {
                        props.put("Fish", "Gurama");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    mixpanel.track("Add fish", props);
                    props.put("Age", String.valueOf(age));
                    props.put("Gender", String.valueOf(gender));
                } catch (JSONException e) {
                    Log.e("MYAPP", "Unable to add properties to JSONObject", e);
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

    public void notification_trigger(View v) {
        JSONObject props = new JSONObject();
        mixpanel.track("MainActivity - notification_trigger called", props);
        notification_dialog.show();
    }
    public void feed1(final View v) {
        try {
            JSONObject props = new JSONObject();
            props.put("Changing time", "feed 1 time");
            mixpanel.track("MainActivity - setTime", props);
        } catch (JSONException e) {
            Log.e("MYAPP", "Unable to add properties to JSONObject", e);
        }
        setTime((Button)v.findViewById(R.id.feed1_time), v);
    }
    public void feed2(final View v) {
        try {
            JSONObject props = new JSONObject();
            props.put("Changing time", "feed 2 time");
            mixpanel.track("MainActivity - setTime", props);
        } catch (JSONException e) {
            Log.e("MYAPP", "Unable to add properties to JSONObject", e);
        }
        setTime((Button)v.findViewById(R.id.feed2_time), v);
    }
    public void feed3(final View v) {
        try {
            JSONObject props = new JSONObject();
            props.put("Changing time", "feed 3 time");
            mixpanel.track("MainActivity - setTime", props);
        } catch (JSONException e) {
            Log.e("MYAPP", "Unable to add properties to JSONObject", e);
        }
        setTime((Button)v.findViewById(R.id.feed3_time), v);
    }
    public void clean1(final View v) {
        try {
            JSONObject props = new JSONObject();
            props.put("Changing time", "clean time");
            mixpanel.track("MainActivity - setTime", props);
        } catch (JSONException e) {
            Log.e("MYAPP", "Unable to add properties to JSONObject", e);
        }
        setTime((Button)v.findViewById(R.id.clean_time), v);
    }
    public void setTime(final Button tv, final View v) {
        final Dialog time_dialog = new Dialog(this);
        time_dialog.setTitle("Select time");
        time_dialog.setContentView(R.layout.time_picker);

        Button ok = (Button) time_dialog.findViewById(R.id.time_ok);
        Button cancel = (Button) time_dialog.findViewById(R.id.time_cancel);
        final TimePicker timePicker = (TimePicker) time_dialog.findViewById(R.id.timePicker);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder time = new StringBuilder();
                if (timePicker.getCurrentHour()<10) time.append("0");
                time.append(timePicker.getCurrentHour());
                time.append(":");
                if (Integer.valueOf(timePicker.getCurrentMinute())<10) time.append("0");
                time.append(timePicker.getCurrentMinute());
                tv.setText(time.toString());
                time_dialog.cancel();
                try {
                    JSONObject props = new JSONObject();
                    props.put("time selected", time.toString());
                    mixpanel.track("MainActivity - setTime", props);
                } catch (JSONException e) {
                    Log.e("MYAPP", "Unable to add properties to JSONObject", e);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject props = new JSONObject();
                    props.put("time selected", "action canceled");
                    mixpanel.track("MainActivity - setTime", props);
                } catch (JSONException e) {
                    Log.e("MYAPP", "Unable to add properties to JSONObject", e);
                }
                time_dialog.cancel();
            }
        });
        timePicker.setCurrentHour(Integer.valueOf(tv.getText().subSequence(0,2).toString()));
        timePicker.setCurrentMinute(Integer.valueOf(tv.getText().subSequence(3,5).toString()));
        time_dialog.show();
    }
    public void startNotification() {
        if (((Switch)notification_dialog.findViewById(R.id.feed1_enabled)).isChecked()) {
            Intent alarmIntent = new Intent(FullscreenActivity.this, HungryAlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(FullscreenActivity.this, 0, alarmIntent, 0);
            pendingIntent.add(pi);

            String time = ((Button) notification_dialog.findViewById(R.id.feed1_time)).getText().toString();
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.substring(0, 2)));
            calendar.set(Calendar.MINUTE, Integer.valueOf(time.substring(3, 5)));

            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        }

        if (((Switch)notification_dialog.findViewById(R.id.feed2_enabled)).isChecked()) {
            Intent alarmIntent = new Intent(FullscreenActivity.this, HungryAlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(FullscreenActivity.this, 0, alarmIntent, 0);
            pendingIntent.add(pi);

            String time = ((Button) notification_dialog.findViewById(R.id.feed2_time)).getText().toString();
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.substring(0, 2).toString()));
            calendar.set(Calendar.MINUTE, Integer.valueOf(time.substring(3, 5).toString()));

            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        }

        if (((Switch)notification_dialog.findViewById(R.id.feed3_enabled)).isChecked()) {
            Intent alarmIntent = new Intent(FullscreenActivity.this, HungryAlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(FullscreenActivity.this, 0, alarmIntent, 0);
            pendingIntent.add(pi);

            String time = ((Button) notification_dialog.findViewById(R.id.feed3_time)).getText().toString();
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.substring(0, 2).toString()));
            calendar.set(Calendar.MINUTE, Integer.valueOf(time.substring(3, 5).toString()));

            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        }

        if (((Switch)notification_dialog.findViewById(R.id.clean_enabled)).isChecked()) {
            Intent alarmIntent = new Intent(FullscreenActivity.this, CleanAlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(FullscreenActivity.this, 0, alarmIntent, 0);
            pendingIntent.add(pi);

            String time = ((Button) notification_dialog.findViewById(R.id.clean_time)).getText().toString();
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.substring(0, 2).toString()));
            calendar.set(Calendar.MINUTE, Integer.valueOf(time.substring(3, 5).toString()));
            calendar.set(Calendar.SECOND, 0);

            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * Long.valueOf(((EditText)notification_dialog.findViewById(R.id.clean_interval)).getText().toString()), pi);
        }
    }
    public void stopNotification() {
        for (PendingIntent pi : pendingIntent) {
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            manager.cancel(pi);
        }
        pendingIntent.clear();

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
