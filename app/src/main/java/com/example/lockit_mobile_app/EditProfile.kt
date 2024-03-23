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
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class EditProfile : AppCompatActivity() {
    private val client = OkHttpClient()

    private lateinit var welcomUsernameTextView: TextView

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText

    private lateinit var cancelButton: Button
    private lateinit var saveChangesButton: Button

    val loadingAlert = LoadingAlert(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)

        welcomUsernameTextView = findViewById(R.id.welcomeuser)
        usernameEditText = findViewById(R.id.username)
        emailEditText = findViewById(R.id.email)
        phoneEditText = findViewById(R.id.phonenumber)
        cancelButton = findViewById(R.id.cancel)
        saveChangesButton = findViewById(R.id.saveChanges)

        // Retrieve user details from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        val email = sharedPreferences.getString("email", "")
        val phone = sharedPreferences.getString("phoneNumber", "")
        val id = sharedPreferences.getString("userId", "")

        // Display username in TextView
        welcomUsernameTextView.text = "hi, $username"

        // Set text for EditText fields
        usernameEditText.setText(username)
        emailEditText.setText(email)
        phoneEditText.setText(phone)

        cancelButton.setOnClickListener {
            val intent = Intent(applicationContext, Profile::class.java)
            startActivity(intent)
            finish()
        }

        saveChangesButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val phoneNumber = phoneEditText.text.toString()

            if (!isNetworkAvailable()) {
                Toast.makeText(this@EditProfile, "No internet connection", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username.isEmpty()) {
                Toast.makeText(this@EditProfile, "Username can't be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty() || !Utils.isValidEmail(email)) {
                Toast.makeText(this@EditProfile, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this@EditProfile, "Phone Number can't be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loadingAlert.startLoading()
            save(username, email, phoneNumber, id.toString())
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
                Toast.makeText(this@EditProfile, "Offline", Toast.LENGTH_SHORT).show()
            }
        }

        val networkRequest = NetworkRequest.Builder().build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val isConnected = capabilities != null

        connectivityManager.unregisterNetworkCallback(networkCallback)

        return isConnected
    }

    private fun save(username: String, email: String, phoneNumber: String, id: String) {
        val requestBody = FormBody.Builder()
            .add("username", username)
            .add("email", email)
            .add("phoneNumber", phoneNumber)
            .build()

        val putURL = "https://lockit-backend-api.onrender.com/api/users/$id/profile"

        val request = Request.Builder().url(putURL).put(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@EditProfile, "Something went wrong", Toast.LENGTH_SHORT).show()
                    loadingAlert.stopLoading()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        val jsonResponse = response.body?.string()
                        Log.d("Response", jsonResponse ?: "Empty response")

                        if (jsonResponse.isNullOrEmpty()) {
                            Toast.makeText(this@EditProfile, "Empty response", Toast.LENGTH_SHORT).show()
                            loadingAlert.stopLoading()
                            return@runOnUiThread
                        }

                        val jsonObject = JSONObject(jsonResponse)
                        val status = jsonObject.getBoolean("status")

                        if (status) {
                            // Update user details in SharedPreferences
                            val userDetails = jsonObject.getJSONObject("user")
                            val updatedUsername = userDetails.getString("username")
                            val updatedEmail = userDetails.getString("email")
                            val updatedPhoneNumber = userDetails.getString("phoneNumber")

                            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("username", updatedUsername)
                            editor.putString("email", updatedEmail)
                            editor.putString("phoneNumber", updatedPhoneNumber)
                            editor.apply()

                            Toast.makeText(this@EditProfile, jsonObject.getString("message"), Toast.LENGTH_SHORT).show()
                            loadingAlert.stopLoading()
                            val intent = Intent(applicationContext, Profile::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@EditProfile, jsonObject.getString("message"), Toast.LENGTH_SHORT).show()
                            Toast.makeText(this@EditProfile, id, Toast.LENGTH_SHORT).show()
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
