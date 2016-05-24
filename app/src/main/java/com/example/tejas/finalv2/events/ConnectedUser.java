/**
 * File Name:               ConnectedUser.java
 * File Description:        Creating the class to check who is connected
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.events;

public class ConnectedUser {
    private final String whoIsConnected;

    public ConnectedUser(String whoIsConnected) {
        this.whoIsConnected = whoIsConnected;
    }

    public String getWhoIsConnected() {
        return whoIsConnected;
    }
}