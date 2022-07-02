package evote.uz.evoteuz.utils

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

object AnimationUtils {
    /**bottom to Top*/
    fun animationBottomToTop(view: View) {
        val animation: Animation = TranslateAnimation(0f, 0f, 500f, 0f)
        animation.duration = 300
        animation.fillAfter = true
        view.startAnimation(animation)
    }

    /**Top to bottom*/
    fun animationTopToBottom(view: View) {
        val animation: Animation = TranslateAnimation(0f, 0f, 0f, 500f)
        animation.duration = 300
        animation.fillAfter = true
        view.startAnimation(animation)
    }

    /** left to right*/
    fun animationLeftToRight(view: View) {
        val animationTopLeft: Animation = TranslateAnimation(-500f, 0f, 0f, 0f)
        animationTopLeft.duration = 300
        animationTopLeft.fillAfter = true
        view.startAnimation(animationTopLeft)
    }

    /** right to left*/
    fun animationRightToLeft(view: View) {
        val animationTopRight: Animation = TranslateAnimation(500f, 0f, 0f, 0f)
        animationTopRight.duration = 300
        animationTopRight.fillAfter = true
        view.startAnimation(animationTopRight)
    }

    fun animationAlhpa(view: View) {
        val animation: Animation = AlphaAnimation(0f, 1f)
        animation.duration = 400
        animation.fillAfter = true
        view.startAnimation(animation)

    }
}