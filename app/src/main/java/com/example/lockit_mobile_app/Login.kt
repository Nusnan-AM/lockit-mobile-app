package com.example.lockit_mobile_app

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executor


class Login : AppCompatActivity() {

    private lateinit var client: OkHttpClient

    private lateinit var biometricPrompt: BiometricPrompt

    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var createAccount: Button

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText


    private  lateinit var buttonSignIn: Button

    private lateinit var loading: ProgressBar

    val loadingAlert = LoadingAlert(this)

    private val getURL: String = "https://lockit-backend-api.onrender.com/api/users/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Check if the user is already logged in
        if (isLoggedIn()) {
            // If the user is already logged in, start the Dashboard activity
            startDashboardActivity()
            return
        }

        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)

        buttonSignIn = findViewById(R.id.signin)

        createAccount = findViewById(R.id.createAccount)

        loading = findViewById(R.id.progressBar)

        createAccount.setOnClickListener{
            val intent = Intent(applicationContext,SignUp::class.java)
            startActivity(intent)
            finish()
        }

        client = OkHttpClient()

        buttonSignIn.setOnClickListener{

            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()


            if (!isNetworkAvailable()) {
                Toast.makeText(this@Login, "No internet connection", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty() || !Utils.isValidEmail(email)) {
                Toast.makeText(this@Login, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this@Login, "Password can't be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadingAlert.startLoading()
            signIn(email, password)
        }

    }

    private fun isLoggedIn(): Boolean {
        // Check if user details are stored in SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        return !username.isNullOrEmpty()
    }



    private fun startDashboardActivity() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this@Login, "Device doesn't have fingerprint hardware", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this@Login, "Fingerprint hardware is unavailable", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this@Login, "No fingerprint enrolled on the device", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this@Login, "Something went wrong with fingerprint authentication", Toast.LENGTH_SHORT).show()
            }
        }

        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this@Login, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@Login, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(this@Login, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@Login, DashBoard::class.java)
                startActivity(intent)
                finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@Login, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Lockit")
            .setDescription("Authenticate is it you")
            .setDeviceCredentialAllowed(true)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Network is available
//                Toast.makeText(this@Login, "Online", Toast.LENGTH_SHORT).show()
            }

            override fun onLost(network: Network) {
                // Network is lost
                Toast.makeText(this@Login, "Offline", Toast.LENGTH_SHORT).show()

            }
        }

        val networkRequest = NetworkRequest.Builder().build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val isConnected = capabilities != null

        connectivityManager.unregisterNetworkCallback(networkCallback)

        return isConnected
    }

    private fun signIn(email: String, password: String) {
        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        val request = Request.Builder().url(getURL).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    loadingAlert.stopLoading()
                    Toast.makeText(this@Login, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    loadingAlert.stopLoading()
                    try {

                        val jsonResponse = response.body?.string()
                        Log.d("Response", jsonResponse ?: "Empty response")

                        // Convert JSON response string to JSONObject
                        val jsonObject = JSONObject(jsonResponse)

                        // Check if the response contains "status": true
                        if (jsonObject.getBoolean("status")) {
                            // If status is true, parse user details from response
                            val userObject = jsonObject.getJSONObject("user")
                            val userId = userObject.getString("_id")
                            val username = userObject.getString("username")
                            val email = userObject.getString("email")
                            val phoneNumber = userObject.getString("phoneNumber")

                            // Store user details in SharedPreferences
                            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("userId", userId)
                            editor.putString("username", username)
                            editor.putString("email", email)
                            editor.putString("phoneNumber", phoneNumber)
                            editor.apply()

                            // Start Dashboard activity
                            val intent = Intent(applicationContext, DashBoard::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // If status is false, display an error message
                            Toast.makeText(this@Login, "Login failed: ${jsonObject.getString("message")}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: JSONException) {
                        // Handle JSON parsing error
                        Log.e("JSONException", "Error parsing JSON response", e)
                        Toast.makeText(this@Login, "Error parsing JSON response", Toast.LENGTH_SHORT).show()
                        loadingAlert.stopLoading()
                    } catch (e: IOException) {
                        // Handle IO error
                        Log.e("IOException", "Error reading response body", e)
                        Toast.makeText(this@Login, "Error reading response body", Toast.LENGTH_SHORT).show()
                        loadingAlert.stopLoading()
                    }
                }
            }
        })
    }

}