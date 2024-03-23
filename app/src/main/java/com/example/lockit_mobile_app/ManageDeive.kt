package com.example.lockit_mobile_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import okhttp3.OkHttpClient

class ManageDeive : AppCompatActivity() {

    private lateinit var deviceIdTextView: TextView
    private lateinit var lockedLockTextView: TextView
    private lateinit var unlockedLockTextView: TextView
    private lateinit var activeStatusTextView: TextView


    private lateinit var backButton: Button
    private lateinit var addDeviceButton: Button
    private lateinit var removeDeviceButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_deive)

        backButton = findViewById(R.id.back)
        addDeviceButton = findViewById(R.id.addDevice)
        deviceIdTextView = findViewById(R.id.deiveIDtext)
        removeDeviceButton = findViewById(R.id.removeDevice)
        lockedLockTextView = findViewById(R.id.locked_lock)
        unlockedLockTextView = findViewById(R.id.unlocked_lock)
        activeStatusTextView = findViewById(R.id.activeState)

        // Retrieve user details from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyDevice", Context.MODE_PRIVATE)

        val active = sharedPreferences.getBoolean("active", false)
        val status = sharedPreferences.getBoolean("status", false)
        val deviceID = sharedPreferences.getString("deviceID", "")


        // Display deviceId in TextView
        if (sharedPreferences.contains("deviceID")) {
            // SharedPreferences contain the device ID, so display it in the TextView
            deviceIdTextView.text = deviceID.toString()
            removeDeviceButton.visibility = View.VISIBLE
            addDeviceButton.visibility = View.GONE

            if (active){
                activeStatusTextView.text = "Active"
            }else{
                activeStatusTextView.text = "Inactive"
            }

            if (status){
                lockedLockTextView.visibility = View.VISIBLE
                unlockedLockTextView.visibility = View.GONE
            }else {
                lockedLockTextView.visibility = View.GONE
                unlockedLockTextView.visibility = View.VISIBLE
            }



        }else{
            addDeviceButton.visibility = View.VISIBLE
            removeDeviceButton.visibility = View.GONE
        }
//        deviceIdTextView.text = deviceID.toString()
        backButton.setOnClickListener{
            val intent = Intent(applicationContext,DashBoard::class.java)
            startActivity(intent)
            finish()
        }
        addDeviceButton.setOnClickListener{
            val intent = Intent(applicationContext,AddDevice::class.java)
            startActivity(intent)
            finish()
        }
        removeDeviceButton.setOnClickListener{
            // Clear SharedPreferences
            val sharedPreferences = getSharedPreferences("MyDevice", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            Toast.makeText(this@ManageDeive, "Device removed successfully", Toast.LENGTH_SHORT).show()

            addDeviceButton.visibility = View.VISIBLE
            removeDeviceButton.visibility = View.GONE

            val intent = Intent(applicationContext,AddDevice::class.java)
            startActivity(intent)
            finish()
        }
    }
}