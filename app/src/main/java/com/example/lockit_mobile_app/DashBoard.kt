package com.example.lockit_mobile_app

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class DashBoard : AppCompatActivity() {

    private val client = OkHttpClient()

    private lateinit var usernameTextView: TextView
    private lateinit var manageDevicePage: TextView
    private lateinit var profilePage: TextView
    private lateinit var alertsPage: TextView
    private lateinit var helpPage: TextView
    private lateinit var lockIconBGTextview: TextView
    private lateinit var lockedIcon: TextView
    private lateinit var unlockedIcon: TextView
    private lateinit var unlockButton: Button
    private lateinit var lockButton: Button

    val loadingAlert = LoadingAlert(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // Initialize views
        usernameTextView = findViewById(R.id.username)
        manageDevicePage = findViewById(R.id.manageDevice)
        profilePage = findViewById(R.id.useraccount)
        alertsPage = findViewById(R.id.alert)
        helpPage = findViewById(R.id.help)

        lockIconBGTextview = findViewById(R.id.lockiconbg)
        lockedIcon = findViewById(R.id.locked_lock)
        unlockedIcon = findViewById(R.id.unlocked_lock)
        unlockButton = findViewById(R.id.unlock)
        lockButton = findViewById(R.id.lock)

        // Retrieve username from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        val ownerID = sharedPreferences.getString("userId", "").toString()


        // Retrieve user details from SharedPreferences
        val sharedPreferencesDevice = getSharedPreferences("MyDevice", Context.MODE_PRIVATE)
        val deviceID = sharedPreferencesDevice.getString("deviceID", "")
        val status = sharedPreferencesDevice.getBoolean("status", false)



        if (sharedPreferencesDevice.contains("deviceID")) {
            getDevice(deviceID.toString(),ownerID.toString())
            lockIconBGTextview.visibility = View.VISIBLE
            if (status) {
                unlockedIcon.visibility = View.GONE
                lockedIcon.visibility = View.VISIBLE
                unlockButton.visibility = View.VISIBLE
                lockButton.visibility = View.GONE
            } else {
                unlockedIcon.visibility = View.VISIBLE
                lockedIcon.visibility = View.GONE
                unlockButton.visibility = View.GONE
                lockButton.visibility = View.VISIBLE
            }
        }

        // Display username in TextView
        usernameTextView.text = "hi, $username"

        manageDevicePage.setOnClickListener{
            val intent = Intent(applicationContext,ManageDeive::class.java)
            startActivity(intent)
            finish()
        }

        profilePage.setOnClickListener{
            val intent = Intent(applicationContext,Profile::class.java)
            startActivity(intent)
            finish()
        }

        alertsPage.setOnClickListener{
            val intent = Intent(applicationContext,Faq::class.java)
            startActivity(intent)
            finish()
            }

        helpPage.setOnClickListener{
            val intent = Intent(applicationContext,Chat::class.java)
            startActivity(intent)
            finish()
        }

        lockButton.setOnClickListener {
            val deviceid = deviceID.toString()

            loadingAlert.startLoading()

            if (!isNetworkAvailable()) {
                Toast.makeText(this@DashBoard, "No internet connection", Toast.LENGTH_SHORT).show()
                loadingAlert.stopLoading()
                return@setOnClickListener
            }

            if (deviceid.isEmpty()) {
                Toast.makeText(this@DashBoard, "device Id is missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                loadingAlert.stopLoading()
            }
            lock(deviceid,ownerID)
        }
        unlockButton.setOnClickListener {
            val deviceid = deviceID.toString()
            loadingAlert.startLoading()
            if (!isNetworkAvailable()) {
                Toast.makeText(this@DashBoard, "No internet connection", Toast.LENGTH_SHORT).show()
                loadingAlert.stopLoading()
                return@setOnClickListener
            }

            if (deviceid.isEmpty()) {
                Toast.makeText(this@DashBoard, "device Id is missing", Toast.LENGTH_SHORT).show()
                loadingAlert.stopLoading()
                return@setOnClickListener
            }
            unlock(deviceid,ownerID)
        }


    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Network is available
            }

            override fun onLost(network: Network) {
                // Network is lost
                Toast.makeText(this@DashBoard, "Offline", Toast.LENGTH_SHORT).show()
            }
        }

        val networkRequest = NetworkRequest.Builder().build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val isConnected = capabilities != null

        connectivityManager.unregisterNetworkCallback(networkCallback)

        return isConnected
    }

    private fun lock(deviceid: String,ownerID: String) {
        val postURL = "https://lockit-backend-api.onrender.com/api/devices/$deviceid/lock"
        val requestBody = FormBody.Builder()
            .add("ownerID", ownerID)
            .build()
        val request = Request.Builder().url(postURL).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@DashBoard, "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                    loadingAlert.stopLoading()
                }

            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonResponse = response.body?.string()
                        Log.d("Response", jsonResponse ?: "Empty response")

                        if (jsonResponse.isNullOrEmpty()) {
                            Toast.makeText(this@DashBoard, "Empty response", Toast.LENGTH_SHORT)
                                .show()
                            loadingAlert.stopLoading()
                            return@runOnUiThread
                        }

                        val jsonObject = JSONObject(jsonResponse)
                        val status = jsonObject.getBoolean("status")

                        if (status) {

                            val sharedPreferences =
                                getSharedPreferences("MyDevice", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("status", true)
                            editor.apply()
                            Toast.makeText(
                                this@DashBoard,
                                jsonObject.getString("message"),
                                Toast.LENGTH_SHORT
                            ).show()
                            loadingAlert.stopLoading()
                            val intent = Intent(applicationContext, DashBoard::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            if (!jsonObject.has("message")) {
                                // Message is not present in the jsonObject
                                Toast.makeText(
                                    this@DashBoard,
                                    "Something went wrong",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Toast.makeText(
                                    this@DashBoard,
                                    deviceid,
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadingAlert.stopLoading()
                            } else {
                                // Message is present in the jsonObject
                                Toast.makeText(
                                    this@DashBoard,
                                    jsonObject.getString("message"),
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadingAlert.stopLoading()
                            }

                        }
                    } catch (e: IOException) {
                        loadingAlert.stopLoading()
                        throw RuntimeException(e)
                    }
                }
            }
        })
    }

    private fun unlock(deviceid: String,ownerID: String) {
        val postURL = "https://lockit-backend-api.onrender.com/api/devices/$deviceid/unlock"
        val requestBody = FormBody.Builder()
            .add("ownerID", ownerID)
            .build()
        val request = Request.Builder().url(postURL).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@DashBoard, "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                    loadingAlert.stopLoading()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonResponse = response.body?.string()
                        Log.d("Response", jsonResponse ?: "Empty response")

                        if (jsonResponse.isNullOrEmpty()) {
                            Toast.makeText(this@DashBoard, "Empty response", Toast.LENGTH_SHORT)
                                .show()
                            loadingAlert.stopLoading()
                            return@runOnUiThread
                        }

                        val jsonObject = JSONObject(jsonResponse)
                        val status = jsonObject.getBoolean("status")

                        if (status) {

                            val sharedPreferences =
                                getSharedPreferences("MyDevice", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("status", false)
                            editor.apply()
                            Toast.makeText(
                                this@DashBoard,
                                jsonObject.getString("message"),
                                Toast.LENGTH_SHORT
                            ).show()
                            loadingAlert.stopLoading()
                            val intent = Intent(applicationContext, DashBoard::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            if (!jsonObject.has("message")) {
                                // Message is not present in the jsonObject
                                Toast.makeText(
                                    this@DashBoard,
                                    "Something went wrong",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Toast.makeText(
                                    this@DashBoard,
                                    deviceid,
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadingAlert.stopLoading()
                            } else {
                                // Message is present in the jsonObject
                                Toast.makeText(
                                    this@DashBoard,
                                    jsonObject.getString("message"),
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadingAlert.stopLoading()
                            }

                        }
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                        loadingAlert.stopLoading()
                    }
                }
            }
        })
    }

    private fun getDevice(deviceId: String, ownerID: String) {

        val getURL = "https://lockit-backend-api.onrender.com/api/devices/$deviceId"

        val request = Request.Builder().url(getURL).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@DashBoard, "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                    loadingAlert.stopLoading()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonResponse = response.body?.string()
                        Log.d("Response", jsonResponse ?: "Empty response")

                        if (jsonResponse.isNullOrEmpty()) {
                            Toast.makeText(this@DashBoard, "Empty response", Toast.LENGTH_SHORT)
                                .show()
                            loadingAlert.stopLoading()
                            return@runOnUiThread
                        }

                        val jsonObject = JSONObject(jsonResponse)
                        val status = jsonObject.getBoolean("status")

                        if (status) {
                            // If owner match is true, parse user details from response
                            val deviceObject = jsonObject.getJSONObject("device")
                            val deviceId = deviceObject.getString("deviceID")
                            val ownerId = deviceObject.getString("owner")
                            val state = deviceObject.getBoolean("status")
                            val active = deviceObject.getBoolean("active")

                            if (ownerID == ownerId.toString()) {
                                // Store user details in SharedPreferences
                                val sharedPreferences = getSharedPreferences("MyDevice", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()

                                // Store new values in SharedPreferences
                                editor.putString("deviceID", deviceId)
                                editor.putString("owner", ownerId)
                                editor.putBoolean("status", state)
                                editor.putBoolean("active", active)

                                // Apply changes
                                editor.apply()

                                loadingAlert.stopLoading()
                            }
                            loadingAlert.stopLoading()
                        }else {
                            Toast.makeText(
                                this@DashBoard,
                                jsonObject.getString("message"),
                                Toast.LENGTH_SHORT
                            ).show()
                            Toast.makeText(this@DashBoard, deviceId, Toast.LENGTH_SHORT).show()
                            loadingAlert.stopLoading()
                        }
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                        loadingAlert.stopLoading()
                    }
                }
            }
        })
    }

}
