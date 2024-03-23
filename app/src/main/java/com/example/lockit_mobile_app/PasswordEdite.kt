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

class PasswordEdite : AppCompatActivity() {
    private val client = OkHttpClient()

    private lateinit var welcomUsernameTextView: TextView

    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText

    private lateinit var cancelButton: Button
    private lateinit var saveChangesButton: Button

    val loadingAlert = LoadingAlert(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password_edite)

        welcomUsernameTextView = findViewById(R.id.welcomeuser)

        currentPasswordEditText = findViewById(R.id.currentPassword)
        newPasswordEditText = findViewById(R.id.newPassword)

        cancelButton = findViewById(R.id.cancel)
        saveChangesButton = findViewById(R.id.saveChanges)

        // Retrieve user details from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        val email = sharedPreferences.getString("email", "")


        // Display username in TextView
        welcomUsernameTextView.text = "hi, $username"

        cancelButton.setOnClickListener{
            val intent = Intent(applicationContext,Profile::class.java)
            startActivity(intent)
            finish()
        }

        saveChangesButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()

            if (!isNetworkAvailable()) {
                Toast.makeText(this@PasswordEdite, "No internet connection", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentPassword.isEmpty()) {
                Toast.makeText(this@PasswordEdite, "Username can't be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.isEmpty()) {
                Toast.makeText(this@PasswordEdite, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadingAlert.startLoading()
            save(currentPassword,newPassword,email.toString())
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
                Toast.makeText(this@PasswordEdite, "Offline", Toast.LENGTH_SHORT).show()
            }
        }

        val networkRequest = NetworkRequest.Builder().build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val isConnected = capabilities != null

        connectivityManager.unregisterNetworkCallback(networkCallback)

        return isConnected
    }

    private fun save(currentPassword: String,newPassword: String,email: String) {
        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("oldPassword", currentPassword)
            .add("newPassword", newPassword)
            .build()

        val putURL = "https://lockit-backend-api.onrender.com/api/users/change-password"

        val request = Request.Builder().url(putURL).put(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@PasswordEdite, "Something went wrong", Toast.LENGTH_SHORT).show()
                    loadingAlert.stopLoading()
                }

            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonResponse = response.body?.string()
                        Log.d("Response", jsonResponse ?: "Empty response")

                        if (jsonResponse.isNullOrEmpty()) {
                            Toast.makeText(this@PasswordEdite, "Empty response", Toast.LENGTH_SHORT).show()
                            loadingAlert.stopLoading()
                            return@runOnUiThread
                        }

                        val jsonObject = JSONObject(jsonResponse)
                        val status = jsonObject.getBoolean("status")

                        if (status) {
                            Toast.makeText(this@PasswordEdite, jsonObject.getString("message"), Toast.LENGTH_SHORT).show()
                            loadingAlert.stopLoading()
                            val intent = Intent(applicationContext, Profile::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@PasswordEdite, jsonObject.getString("message"), Toast.LENGTH_SHORT).show()
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