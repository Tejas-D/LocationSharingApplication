/**
 * File Name:               CoarseLocationOption.java
 * File Description:        Creating the class to set the accuracy request
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.events;

public class CoarseLocationOption {

    private final boolean enableCoarseLocation;

    public CoarseLocationOption(boolean enableCoarseLocation) {
        this.enableCoarseLocation = enableCoarseLocation;
    }

    public boolean isEnableCoarseLocation() {
        return enableCoarseLocation;
    }
}
