package com.example.lockit_mobile_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class Chat : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var welcomeTextView: TextView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private var messageList: MutableList<Message> = mutableListOf()
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var backButton: Button
    private val client = OkHttpClient()
    companion object {
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help)

        recyclerView = findViewById(R.id.recycler_view)
        welcomeTextView = findViewById(R.id.welcome_text)
        messageEditText = findViewById(R.id.message_edit_text)
        sendButton = findViewById(R.id.send_btn)
        backButton = findViewById(R.id.back)

        backButton.setOnClickListener{
            val intent = Intent(applicationContext,DashBoard::class.java)
            startActivity(intent)
            finish()
        }


        // Setup recycler view
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter
        val llm = LinearLayoutManager(this)
        llm.stackFromEnd = true
        recyclerView.layoutManager = llm

        sendButton.setOnClickListener {
            val question = messageEditText.text.toString().trim()
            addToChat(question, Message.SENT_BY_ME)
            messageEditText.setText("")
            callAPI(question)
            welcomeTextView.visibility = View.GONE
        }
    }

    private fun addToChat(message: String, sentBy: String) {
        runOnUiThread {
            messageList.add(Message(message, sentBy))
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messageAdapter.itemCount)
        }
    }

    private fun addResponse(response: String) {
        messageList.removeAt(messageList.size - 1)
        addToChat(response, Message.SENT_BY_BOT)
    }

    //For send to the request
    private fun callAPI(question: String) {
        // OkHttp
        messageList.add(Message("Typing... ", Message.SENT_BY_BOT))

        val jsonBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo-instruct")
            put("prompt", question)
            put("max_tokens", 800)
            put("temperature", 0)
        }
        val body = RequestBody.create(JSON, jsonBody.toString())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/completions")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer RELPACE_API_KEY")//RELPACE_OPEN_AI_WITH_API_KEY
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to ${e.message}")
            }

            //For Get to the request
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        val jsonObject = JSONObject(it.body!!.string())
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val result = jsonArray.getJSONObject(0).getString("text")
                        addResponse(result.trim())
                    } else {
                        addResponse("Failed to load response due to ${response.body}")
                    }
                }
            }
        })
    }
}
