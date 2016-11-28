package com.geewhizstuff.aquariummanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;


/**
 * Created by Andrej on 12.11.2016.
 */

public class Prisavnik extends Fish {
    public Prisavnik(Context context, ViewGroup vg, int startX, int startY, int age, Gender gender) {
        super(context, vg, startX, startY, age, gender, R.layout.prisavnik);


        super.vx=5;
        super.vy=5;
    }
    @Override
    public void update_position() {

        if (x + fishImageView.getLayoutParams().width > xbound) {
            vx = Math.min(-vx, vx);
            orientation = 1;
        }

        else if (y +fishImageView.getLayoutParams().height> ybound) {
            y = ybound - fishImageView.getLayoutParams().height- 1;
            vy = 0;
        }

        else if (x < 0) {
            vx = Math.max(-vx, vx);
            orientation = 2;
        }

        else if (y < 0) {
            vy = Math.max(-vy, vy);
        }

        x += vx;
        y += vy;

        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(fishImageView.getLayoutParams().width, fishImageView.getLayoutParams().height);
                params.leftMargin = (int) x;
                params.topMargin = (int) y;

                v.setLayoutParams(params);

                if (orientation == 1) {
                    fishImageView.setImageResource(R.drawable.prisavnikl);
                } else if (orientation == 2) {
                    fishImageView.setImageResource(R.drawable.prisavnikr);
                }
            }
        });
    }
}
