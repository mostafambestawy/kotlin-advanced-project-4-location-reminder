package com.udacity.project4.util

import android.os.IBinder
import android.view.WindowManager
import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

//https://stackoverflow.com/questions/47092927/testing-toast-message-using-espresso-is-not-resolved
 class ToastMatcher : TypeSafeMatcher<Root>() {
    override fun matchesSafely(item: Root?): Boolean {
            val type: Int = item!!.windowLayoutParams?.get()!!.type
            if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                val windowToken: IBinder = item.decorView.windowToken
                val appToken: IBinder = item.decorView.applicationWindowToken
                if (windowToken === appToken) {
                    return true
                    //means this window isn't contained by any other windows.
                }
            }
            return false

    }

     override fun describeTo(description: Description?) {

     }
 }