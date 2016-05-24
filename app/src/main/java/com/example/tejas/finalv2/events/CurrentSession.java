/**
 * File Name:               CurrentSession.java
 * File Description:        Creating the class to set the current session on the RTStack
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.events;

import com.example.tejas.finalv2.rtstack.RTStack;

public class CurrentSession {

    private final RTStack.State currentState;

    public CurrentSession(RTStack.State currentState) {
        this.currentState = currentState;
    }

    public RTStack.State getCurrentState() {
        return currentState;
    }
}