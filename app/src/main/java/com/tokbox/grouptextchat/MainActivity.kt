package com.tokbox.grouptextchat

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val android_id = Settings.Secure.getString(
            this.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        Log.d("Android", "Android ID : $android_id")

        val roomName = findViewById<EditText>(R.id.room_name)
        val userName = findViewById<EditText>(R.id.user_name)
        val startBtn = findViewById<Button>(R.id.startChat)

        startBtn.setOnClickListener {
            if(roomName.text.trim().length == 0 || userName.text.trim().length == 0) {
                Toast.makeText(this, resources.getString(R.string.enter_room_check), Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("username", userName.text.toString())
                intent.putExtra("roomname", roomName.text.toString())
                intent.putExtra("unique_id", android_id)

                startActivity(intent)
            }
        }



    }
}