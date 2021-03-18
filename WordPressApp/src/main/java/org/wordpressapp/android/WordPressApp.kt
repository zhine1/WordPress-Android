package org.wordpressapp.android

import android.util.Log
import org.wordpress.android.WordPress

class WordPressApp : WordPress() {
    @Override fun customAppLogic() { Log.i(javaClass.simpleName, "***=> Apply custom logic for WordPress App")}
}
