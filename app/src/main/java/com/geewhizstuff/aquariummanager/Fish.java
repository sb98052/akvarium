package com.geewhizstuff.aquariummanager;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by sapanbhatia on 10/21/16.
 */

public class Fish {
    public ImageView fishImageView;
    public View v;
    public double x, y;
    public double vx, vy;
    public double xbound, ybound;
    Activity act;
    public int orientation;

    public Fish(Context context, ViewGroup vg, int startX, int startY) {
       // this.topView = vg;
        RelativeLayout rl = (RelativeLayout) vg.findViewById(R.id.fishtank);
        // View fishView = View.inflate(context, R.layout.fish, rl);

        act = (Activity) context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        View fv = inflater.inflate(R.layout.fish,null);
        for(int index=0; index<((ViewGroup)fv).getChildCount(); ++index) {
            View nextChild = ((ViewGroup)fv).getChildAt(index);
            if (nextChild instanceof ImageView) {
                fishImageView = (ImageView) nextChild;
            }
        }

        //fv.setBackgroundColor(0xffffff);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(220, 180);
        params.leftMargin = startX;
        params.topMargin = startY;
        rl.addView(fv,params);
        v = fv;
        xbound = rl.getWidth();
        ybound = rl.getHeight();
        x=startX;
        y=startY;
        vx=5.0;
        vy=0.0;

    }

    public void update_position() {
        x+=vx;
        y+=vy;
        orientation = 0;

        if (x+220>xbound) {
           // x=xbound-1;
            x--;
            vx=-vx;
            orientation = 1;
        }

        if (y>ybound) {
            y=ybound-1;
            vy=-vy;
        }

        if (x<220) {
            vx=-vx;
            x++;
            orientation = 2;
        }

        if (y<0) {
            vy=-vy;
            y=0;
        }

        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(220, 180);
                params.leftMargin = (int)x;
                params.topMargin = (int)y;

                v.setLayoutParams(params);

                if (orientation==1) {
                    fishImageView.setImageResource(R.drawable.fishleft);
                } else if (orientation==2) {
                    fishImageView.setImageResource(R.drawable.fishright);
                }
            }
        });

    }
}
