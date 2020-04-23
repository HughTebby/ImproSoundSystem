package com.hughtebby.improsoundsystem;

import android.app.Application;

import java.util.ArrayList;

/**
 * Manage global variables
 */
public class GlobalClass extends Application{

    private ArrayList<Sample> history = new ArrayList<>();

    public ArrayList<Sample> getHistory() {

        return history;
    }

    public void setHistory(ArrayList<Sample> aHistory) {

        history = aHistory;
    }

}
