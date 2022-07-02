package uz.faceid.faceidcompany.common;

import android.app.Activity;

import uz.faceid.faceidcompany.R;


public class TransitionsLibrary {
    /**
     * Execute activity transition to the right.
     */
    public static void executeToRightTransition(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
    }

    /**
     * Execute activity transition to the left.
     */
    public static void executeToLeftTransition(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
    }
}
