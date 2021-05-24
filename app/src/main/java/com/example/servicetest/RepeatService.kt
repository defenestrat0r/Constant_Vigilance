package com.example.servicetest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.safetynet.SafetyNet
import java.util.concurrent.atomic.AtomicInteger

class RepeatService: Service()
{
    private val channelName = "Constant Vigilance"
    private val channelID = "com.foreground.maligncheck"

    companion object
    {
        var flag = false

        //Start the foreground service
        fun startService(context: Context, message: String, timespec: String)
        {
            flag = true
            Log.d("flagcheckTrue", flag.toString())

            val startIntent = Intent(context, RepeatService::class.java)
            when(message)
            {
                "Unverified" ->
                { startServ(context, startIntent, "Can't Verify", "Please enable app verification", timespec) }

                "Verified" ->
                { safetyNetService(context, startIntent, timespec) }

                else ->
                { startServ(context, startIntent) }
            }
        }

        //Stop the foreground service
        fun stopService(context: Context)
        {
            flag = false
            Log.d("flagcheckFalse", flag.toString())

            val stopIntent = Intent(context, RepeatService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {

        Log.d("OnStartCommand", "Home Stretch")
        val notificationTitle = intent?.getStringExtra("Title")
        val details = intent?.getStringExtra("Details")
        val timespec = intent?.getStringExtra("Time")

        if (timespec != null) { Log.d("checktime", timespec) }
        else { Log.d("checktime", "F") }

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, channelID)
            .setContentTitle(notificationTitle)
            .setSmallIcon(R.drawable.ic_stat_onesignal_default)
            .setContentText(details)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NotificationID.getID(), notification)


        if (timespec != null) { Thread.sleep(1000 * timespec.toLong()) }
        else { Thread.sleep(1000 * 5) }

        Log.d("SleepTag", "It slept")

        val stopIntent = Intent(this, RepeatService::class.java)
        this.stopService(stopIntent)

        Log.d("flagcheckOnStart", flag.toString())

        if(flag)
        {
            val runnable = Runnable { if (intent != null) { safetyNetService(this, intent, timespec.toString()) } }
            val updateHandler = Handler(Looper.getMainLooper())
            updateHandler.postDelayed(runnable, 1000)
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val serviceChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? { return null }

}

// To generate notification ID
open class NotificationID
{
    companion object
    {
        private val c: AtomicInteger = AtomicInteger(1)
        fun getID(): Int
        { return c.incrementAndGet() }
    }
}

// Utility function to start a service
fun startServ(context: Context, intent: Intent, title: String = "Oops", details: String = "Something went wrong", timespec: String = "0")
{
    intent.putExtra("Title", title)
    intent.putExtra("Details", details)
    intent.putExtra("Time", timespec)
    ContextCompat.startForegroundService(context, intent)
}

fun safetyNetService(context: Context, intent: Intent, time: String)
{
    Log.d("Ambitious", "Let's see")
    SafetyNet.getClient(context)
        .listHarmfulApps()
        .addOnCompleteListener { task ->
            Log.d(TAG, "Received listHarmfulApps() result")

            val notificationTitle : String
            val details: String

            if (task.isSuccessful) {
                val result = task.result
                val appList = result.harmfulAppsList

                if (appList?.isNotEmpty() == true)
                {
                    val body = StringBuilder()
                    body.append("Information about the harmful app(s):\n")
                    for (harmfulApp in appList)
                    {
                        val a = harmfulApp.apkPackageName
                        val b = harmfulApp.apkCategory
                        body.append(a)
                            .append(" | ")
                            .append(b)
                            .append("\n")
                    }

                    Log.e("PHAdetex", "Potentially harmful apps are installed!")
                    notificationTitle = "Unsafe!"
                    details = body.toString()

                    startServ(context, intent, notificationTitle, details, time)

                }
                else
                {
                    Log.d("PHAnope","There are no known potentially harmful apps installed.")
                    notificationTitle = "Safe!"
                    details = "There are no known potentially harmful apps installed."

                    startServ(context, intent, notificationTitle, details, time)
                }
            }
            else
            { Log.d("RuhRoh","An error occurred. Call isVerifyAppsEnabled() to ensure that the user has consented.") }
        }

}