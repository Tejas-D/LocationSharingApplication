/**
 * File Name:               CustomCenterButton.java
 * File Description:        Repositioning the center location button on the Map.
 *
 * Author:                  Tejas Dwarkaram
 * Credit:                  Gönderen Sinan KOZAK zaman
 * URL:                     http://blog.kozaxinan.com/2013/08/how-to-change-position-of.html
 *
 * Information:             The following piece of code for repositioning the MyLocation
 *                          Button is not my own.
 */

package com.example.tejas.finalv2.utils;

import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;

public class CustomCenterButton {

    public CustomCenterButton(View view){

        if (view != null && view.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            // ZoomControl is inside of RelativeLayout
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
            // Align it to - parent BOTTOM|LEFT
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);

            // Update margins, set to 10dp
            final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                    view.getResources().getDisplayMetrics());
            params.setMargins(margin, margin, margin, margin);

            view.setLayoutParams(params);
        }

    }

}