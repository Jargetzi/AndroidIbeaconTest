package com.jargetzi.app;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by michaellee on 3/6/14.
 */
public class GlobalState extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //  This said to do this
        //  https://parse.com/questions/android-push-notification-error-msg
        Parse.initialize(this, "gYJPKww2de65Qq5rDXXdp4C6fqZbvxFttbOqbyQg", "g5NbntohgJ2wQwNhWrEnG0nun5QrDulsDgQfTnHF");
    }
}
