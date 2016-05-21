package com.tarungoyaldev.android.utilitiesapplication.tools;

import java.util.Observable;

/**
 * String Observable to notify whenever there is a change in string data.
 */
public class StringObservable extends Observable {
    private String observedString;

    public StringObservable(String str) {
        this.observedString = str;
    }

    public void updateString(String str, boolean notify) {
        observedString = str;
        if (notify) {
            setChanged();
            notifyObservers(observedString);
        }
    }

    public void updateString(String str) {
        updateString(str,true);
    }

    public void concat(String str) {
        observedString = observedString.concat(str);
        setChanged();
        notifyObservers(observedString);
    }

    public String getObservedString() {
        return observedString;
    }
}
