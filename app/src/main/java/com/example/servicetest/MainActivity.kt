package com.example.servicetest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import com.example.servicetest.RepeatService.Companion.stopService
import com.example.servicetest.RepeatService.Companion.startService
//import com.example.servicetest.ForegroundService.Companion.startService
//import com.example.servicetest.ForegroundService.Companion.stopService
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonStart.setOnClickListener(this)
        buttonStop.setOnClickListener(this)
    }

    private fun startX(message: String)
    {
        val textbox = findViewById<EditText>(R.id.textInputEditText)
        Log.d("Textcheck", textbox.text.toString())
        startService(this, message, textbox.text.toString()) }

    override fun onClick(view: View)
    {
        when (view.id) {
            R.id.buttonStart -> {

                // First check if verify apps is on
                SafetyNet.getClient(this)
                    .isVerifyAppsEnabled
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                        {
                            if (task.result.isVerifyAppsEnabled)
                            {
                                Log.d("MY_APP_TAG", "The user gave consent to enable the Verify Apps feature.")
                                startX("Verified")
                            }
                            else
                            {
                                // If it is not on, ask user to enable it
                                SafetyNet.getClient(this)
                                    .enableVerifyApps()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful)
                                        {
                                            if (task.result.isVerifyAppsEnabled)
                                            {
                                                Log.d("MY_APP_TAG", "The user gave consent to enable the Verify Apps feature.")
                                                startX("Verified")
                                            }
                                            else
                                            {
                                                Log.d("MY_APP_TAG", "The user didn't give consent to enable the Verify Apps feature.")
                                                startX("Unverified")
                                            }
                                        }
                                    }
                            }
                        }
                        // Could not do app verification
                        else
                        {
                            Log.e("MY_APP_TAG", "A general error occurred.")
                            startX("Unverified")
                        }
                    }
            }

            R.id.buttonStop -> { stopService(this) }
        }
    }
}