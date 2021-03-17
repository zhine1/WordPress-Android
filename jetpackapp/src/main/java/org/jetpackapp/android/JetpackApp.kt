package org.jetpackapp.android

import android.util.Log
import org.wordpress.android.WordPress

class JetpackApp : WordPress() {
    @Override fun customAppLogic() { Log.i(javaClass.simpleName, "***=> Apply custom logic for Jetpack App")}
}
