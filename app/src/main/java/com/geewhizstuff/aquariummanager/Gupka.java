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

public class Gupka extends Fish {
    public Gupka(Context context, ViewGroup vg, int startX, int startY, int age, Gender gender) {
        super(context, vg, startX, startY, age, gender, R.layout.gupka);

    }
    @Override
    public void update_position() {
        x += vx;
        y += vy;
        orientation = 0;

        if (x + fishImageView.getLayoutParams().width > xbound) {
            // x=xbound-1;
            x--;
            vx = -vx;
            orientation = 1;
        }

        if (y +fishImageView.getLayoutParams().height> ybound) {
            y = ybound - fishImageView.getLayoutParams().height- 1;
            vy = -vy;
        }

        if (x < fishImageView.getLayoutParams().width) {
            vx = -vx;
            x++;
            orientation = 2;
        }

        if (y < 0) {
            vy = -vy;
            y = 0;
        }

        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(fishImageView.getLayoutParams().width, fishImageView.getLayoutParams().height);
                params.leftMargin = (int) x;
                params.topMargin = (int) y;

                v.setLayoutParams(params);

                if (orientation == 1) {
                    fishImageView.setImageResource(R.drawable.gupkal);
                } else if (orientation == 2) {
                    fishImageView.setImageResource(R.drawable.gupkar);
                }
            }
        });
    }
}
