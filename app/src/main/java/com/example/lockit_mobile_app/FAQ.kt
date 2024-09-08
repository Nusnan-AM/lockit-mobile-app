package com.example.lockit_mobile_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Faq : AppCompatActivity() {
    private lateinit var textTextView: TextView
    private lateinit var backButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.faq)

        textTextView = findViewById(R.id.text)
        backButton = findViewById(R.id.back)

        textTextView.text = "\nQ1: How do I register my bicycle lock device with the LockIT app?\n" +
                "\n" +
                "Answer: To register your bicycle lock device, simply download the LockIT app from the App Store or Google Play Store and create an account. Once logged in, navigate to the Device Manager section and follow the prompts to add a new device. You'll need to input the unique device ID provided with your lock and follow the on-screen instructions to complete the registration process.\n" +
                "\nQ2: Can I register multiple bicycle lock devices with the LockIT app?\n" +
                "\n" +
                "Answer: Yes, you can register multiple bicycle lock devices with the LockIT app. After logging in, navigate to the Device Manager section and select the option to add a new device. You can repeat this process for each additional lock you wish to register, allowing you to manage multiple locks conveniently from a single app.\n" +
                "\nQ3: What should I do if I forget my password for the LockIT app?\n" +
                "\n" +
                "Answer: If you forget your password for the LockIT app, you can reset it by tapping on the \"Forgot Password\" link on the login screen. Follow the instructions to verify your identity, either through email or SMS, and then create a new password. Once reset, you can use your new password to log back into your account.\n" +
                "\nQ4: How can I lock or unlock my bicycle using the LockIT app?\n" +
                "\n" +
                "Answer: To lock or unlock your bicycle using the LockIT app, ensure that your smartphone is connected to the internet and within Bluetooth range of your lock device. Open the LockIT app, navigate to the Lock/Unlock section, and tap the respective button to lock or unlock your bicycle. The app will send the command to your lock device, and you'll receive visual feedback indicating the current status of the lock.\n" +
                "\nQ3: Is my personal information secure when using the LockIT app?\n" +
                "\n" +
                "Answer: Yes, we take the security and privacy of your personal information very seriously. The LockIT app employs industry-standard encryption protocols to protect your data from unauthorized access or interception. Additionally, we never store sensitive information like passwords in plaintext, and all communication between the app and our servers is encrypted to ensure your privacy.\n" +
                "\nQ4: Can I customize the settings for my bicycle lock device through the LockIT app?\n" +
                "\n" +
                "Answer: Yes, the LockIT app allows you to customize various settings for your bicycle lock device to suit your preferences. Simply navigate to the Device Manager section, select your lock device, and you'll find options to adjust settings such as sensitivity levels, alarm sounds, and notification preferences. Make changes as desired, and the app will update your lock device accordingly.\n"

        backButton.setOnClickListener{
            val intent = Intent(applicationContext,DashBoard::class.java)
            startActivity(intent)
            finish()
        }

    }
}
