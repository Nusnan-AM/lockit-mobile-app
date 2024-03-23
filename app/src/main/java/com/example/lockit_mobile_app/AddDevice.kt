package com.example.lockit_mobile_app

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class AddDevice : AppCompatActivity() {
    private val client = OkHttpClient()

    private lateinit var deviceIdEditText: EditText

    private lateinit var backButton: Button
    private lateinit var addDeviceButton: Button

    val loadingAlert = LoadingAlert(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_device)

        backButton = findViewById(R.id.back)
        addDeviceButton = findViewById(R.id.addDevice)

        deviceIdEditText = findViewById(R.id.deiveId)

        backButton.setOnClickListener{
            val intent = Intent(applicationContext,ManageDeive::class.java)
            startActivity(intent)
            finish()
        }

        // Retrieve user details from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("userId", "")
        val user = sharedPreferences.getString("email", "")

        addDeviceButton.setOnClickListener{
            val deviceId = deviceIdEditText.text.toString()
            val user = user.toString()

            loadingAlert.startLoading()

            if (!isNetworkAvailable()) {
                Toast.makeText(this@AddDevice, "No internet connection", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                loadingAlert.stopLoading()
            }

            if (deviceId.isEmpty()) {
                Toast.makeText(this@AddDevice, "deviceID can't be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
                loadingAlert.stopLoading()
            }

            add(deviceId, user)
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
                Toast.makeText(this@AddDevice, "Offline", Toast.LENGTH_SHORT).show()
            }
        }

        val networkRequest = NetworkRequest.Builder().build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val isConnected = capabilities != null

        connectivityManager.unregisterNetworkCallback(networkCallback)

        return isConnected
    }

    private fun add(deviceId: String, user: String) {

        val getURL = "https://lockit-backend-api.onrender.com/api/devices/$deviceId"

        val request = Request.Builder().url(getURL).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@AddDevice, "Something went wrong", Toast.LENGTH_SHORT)
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
                            Toast.makeText(this@AddDevice, "Empty response", Toast.LENGTH_SHORT)
                                .show()
                            return@runOnUiThread
                            loadingAlert.stopLoading()
                        }

                        val jsonObject = JSONObject(jsonResponse)
                        val status = jsonObject.getBoolean("status")

                        if (status) {

                            // If owner match is true, parse user details from response
                            val deiveObject = jsonObject.getJSONObject("device")
                            val deviceId = deiveObject.getString("deviceID")
                            val owner = deiveObject.getString("owner")
                            val state = deiveObject.getBoolean("status")
                            val active = deiveObject.getBoolean("active")

                            if(user == owner.toString()){

                            // Store user details in SharedPreferences
                            val sharedPreferences = getSharedPreferences("MyDevice", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("deviceID", deviceId)
                            editor.putString("owner", owner)
                            editor.putBoolean("status", state)
                            editor.putBoolean("active", active)
                            editor.apply()

                            Toast.makeText(
                                this@AddDevice,
                                "device successfully added",
                                Toast.LENGTH_SHORT
                            ).show()
                                loadingAlert.stopLoading()
                            val intent = Intent(applicationContext, ManageDeive::class.java)
                            startActivity(intent)
                            finish()


                            }else{
                                Toast.makeText(
                                    this@AddDevice,
                                    "You don't have permission to add this device",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadingAlert.stopLoading()
                            }
                        } else {
                            Toast.makeText(
                                this@AddDevice,
                                jsonObject.getString("message"),
                                Toast.LENGTH_SHORT
                            ).show()
                            Toast.makeText(this@AddDevice, deviceId, Toast.LENGTH_SHORT).show()
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