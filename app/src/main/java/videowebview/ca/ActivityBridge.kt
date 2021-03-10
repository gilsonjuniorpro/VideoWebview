package videowebview.ca

import android.app.Activity

class ActivityBridge {

    companion object{
        private lateinit var activity: Activity

        fun setCurrentActivity(activity: Activity){
            this.activity = activity
        }

        fun getCurrentActivity(): Activity {
            return this.activity
        }
    }

}