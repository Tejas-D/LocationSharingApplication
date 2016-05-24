/**
 * File Name:               MessagingView.java
 * File Description:        Creating the view for potential messaging functionality
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.tejas.finalv2.R;

public class MessagingView extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_chat, container, false);
    }
}
