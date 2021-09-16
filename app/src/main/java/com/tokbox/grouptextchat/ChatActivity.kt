package com.tokbox.grouptextchat

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.myapplication.adapters.MessageAdapter
import com.example.myapplication.utilities.OpentokConfig.Companion.API_KEY
import com.example.myapplication.utilities.OpentokConfig.Companion.SESSION_ID
import com.example.myapplication.utilities.OpentokConfig.Companion.TOKEN
import com.example.myapplication.utilities.SERVER_URL
import com.example.myapplication.utilities.TYPE_JOIN
import com.example.myapplication.utilities.TYPE_REMOTE_TEXT
import com.example.myapplication.utilities.TYPE_SELF_TEXT
import com.opentok.android.Connection
import com.opentok.android.OpentokError
import com.opentok.android.Session
import com.opentok.android.Stream
import com.tokbox.grouptextchat.data.SignalMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity(), View.OnClickListener, Session.SessionListener, Session.SignalListener, Session.ConnectionListener {

    val mMessages: ArrayList<SignalMessage>? = ArrayList()
    lateinit var textChat: RecyclerView
    var userName: String? = null
    var roomName: String? = null
    var uniqueId: String? = null
    private var mSession: Session? = null
    lateinit var mProgressDialog: MaterialDialog
    val colorMap: HashMap<String, Int> = HashMap()

    private val TAG = "ChatActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mProgressDialog = MaterialDialog.Builder(this)
            .cancelable(false)
            .progress(true, 100)
            .content("Initialising session...")
            .build()

        mProgressDialog.show()
        findViewById<ImageView>(R.id.send_text).setOnClickListener(this)

        userName = intent.extras?.getString("username", null)
        roomName = intent.extras?.getString("roomname", null)
        uniqueId = intent.extras?.getString("unique_id", null)

        Log.d(TAG, userName + roomName + uniqueId)

        textChat = findViewById(R.id.text_chat)

        textChat.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        textChat.setLayoutManager(llm)
        //  initializePublisher()



        textChat.setAdapter(mMessages?.let { MessageAdapter(it, this) })

        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                connectToSession()
                Log.d(TAG, "inside connecttoSession after call")

            }
        }

    }

    private suspend fun connectToSession() {

        val fetchSessionUrl = SERVER_URL + roomName

        var result: String = ""

        val url = URL(fetchSessionUrl)
        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
        try {
            val input: InputStream = BufferedInputStream(urlConnection.getInputStream())

            val s = Scanner(input).useDelimiter("\\A")
            if (s.hasNext()) {
                result = s.next()
            }
            //readStream(in)
        } catch (e: Exception) {
            lifecycleScope.launch {
                Dispatchers.Main {

                    Toast.makeText(
                        baseContext,
                        "Please check internet connection and try again",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }

        } 
        finally {
            urlConnection.disconnect()
        }

        Log.d("url is ", fetchSessionUrl)
        Log.d("get result is " , result.toString())

        lifecycleScope.launch {Dispatchers.Main {

                sessionCreationResponse(result)
        }
        }



        // VolleySingletonInstance.getInstance(context).requestQueue.add(stringRequest)
    }

    private fun sessionCreationResponse(result: String) {

        Log.d(TAG, "Inside onResponse(), response is " + result)

        if(result == "") {
            Toast.makeText(this, resources.getString(R.string.session_error), Toast.LENGTH_LONG).show()
            mProgressDialog.dismiss()
            finish()
            return
        }


            var obj: JSONObject? = null
            try {
                obj = JSONObject(result)
            } catch (e: JSONException) {
                e.printStackTrace()
                //return null;
            }

            try {
                API_KEY = obj!!.getString("apiKey")
                SESSION_ID = obj!!.getString("sessionId")
                TOKEN = obj!!.getString("token")

                initializeSession()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

    }

    override fun onClick(v: View?) {

        when(v?.id) {

            R.id.send_text -> {
                sendTextMessage()
            }

        }
    }

    private fun sendTextMessage() {

        val editText = findViewById(R.id.chat_text) as EditText

        if (!editText.text.toString().trim { it <= ' ' }.isEmpty()) {
            val msg = JSONObject()
            try {
                msg.put(
                    "name",
                    userName
                )
                msg.put(
                    "code",
                    uniqueId
                )

                msg.put("data", editText.text.toString().trim { it <= ' ' })
                msg.put("type", "text")
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            mSession!!.sendSignal("default", msg.toString())

            val message = SignalMessage()
            message.data = (editText.text.toString().trim({ it <= ' ' }))
            message.type = TYPE_SELF_TEXT
            mMessages!!.add(message)

            editText.setText("")
            textChat.getAdapter()?.notifyDataSetChanged()

        }

    }

    private fun initializeSession() {
        mSession = Session.Builder(this, API_KEY, SESSION_ID).build()
        mSession?.setSessionListener(this)
        mSession?.connect(TOKEN)

        mSession!!.setSignalListener(this)
        mSession!!.setConnectionListener(this)
    }

    override fun onStreamDropped(p0: Session?, p1: Stream?) {
    }

    override fun onStreamReceived(p0: Session?, p1: Stream?) {
    }


    override fun onConnected(session: Session?) {

        if(session?.connection?.connectionId.equals(mSession?.connection?.connectionId)) {
            mProgressDialog.dismiss()

            val signal = SignalMessage()
            signal.name = "You Joined"
            signal.code = uniqueId!!
            signal.type = TYPE_JOIN
            mMessages!!.add(signal)
            textChat.getAdapter()?.notifyDataSetChanged()
        }

    }

    override fun onDisconnected(session: Session?) {
    }

    override fun onError(p0: Session?, p1: OpentokError?) {
    }

    override fun onSignalReceived(p0: Session?, p1: String?, message: String?, p3: Connection?) {



        onSignalReceived(message, uniqueId)

        textChat.getAdapter()?.notifyDataSetChanged()

        if (mMessages!!.size > 2) {
            textChat.scrollToPosition(mMessages!!.size - 1)
        }

    }

    fun onSignalReceived(message: String?, phoneNumber: String?) {

        try {
            val json = JSONObject(message)

            if(json.has("type")) {
                val type: String = json.getString("type")
                val phone: String = json.getString("code")
                val name: String = json.getString("name")

                if (type.equals("join")) {
                    val signal = SignalMessage()
                    if (phone.equals(phoneNumber)) {
                        signal.name = "You Joined"
                        signal.code = phone
                    } else {
                        signal.name = name + " Joined"
                        signal.code = phone
                    }
                    signal.type = TYPE_JOIN
                    mMessages!!.add(signal)
                    return
                } else if (type.equals("disconnect")) {
                    val signal = SignalMessage()
                    if (phone.equals(phoneNumber)) {
                        signal.name = "You left"
                        signal.code = phone
                    } else {
                        signal.name = name + " left"
                        signal.code = phone
                    }
                    signal.type = TYPE_JOIN
                    mMessages!!.add(signal)
                    return
                }
                val data: String = json.getString("data")

                if (phone != phoneNumber) {
                    val signal = SignalMessage()
                    signal.data = data
                    signal.name = name
                    signal.code = phone

                    val rnd = Random()
                    val color: Int =
                        Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))

                    if(colorMap.get(phone) != null) {
                        signal.color = colorMap.get(phone)!!
                    } else {
                        colorMap.put(phone, color)
                        signal.color = colorMap.get(phone)!!
                    }

                    signal.type = TYPE_REMOTE_TEXT
                    mMessages!!.add(signal)


                }
            }
            else {
                val signal = SignalMessage()
                signal.data = message!!
                signal.name = "Web user"
                signal.type = TYPE_JOIN
                val rnd = Random()
                val color: Int =
                    Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                signal.color = color
                mMessages!!.add(signal)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onConnectionDestroyed(p0: Session?, p1: Connection?) {

    }

    override fun onConnectionCreated(p0: Session?, connection: Connection?) {

        val msg = JSONObject()
        try {
            msg.put(
                "name",
                userName
            )
            msg.put(
                "code",
                uniqueId
            )

            msg.put("type", "join")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        mSession!!.sendSignal("default", msg.toString(), connection)

    }

    override fun onDestroy() {
        super.onDestroy()
        val msg = JSONObject()
        try {
            msg.put(
                "name",
                userName
            )
            msg.put(
                "code",
                uniqueId
            )

            // msg.put("data", editText.text.toString().trim { it <= ' ' })
            msg.put("type", "disconnect")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        if(mSession != null) {
            mSession!!.sendSignal("default", msg.toString())
            mSession?.disconnect()
        }
    }
}
