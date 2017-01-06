package com.example.sic.mychat

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.example.sic.mychat.model.GeneralMessage
import com.example.sic.mychat.model.Message
import com.example.sic.mychat.model.Messages
import com.google.gson.Gson
import rx.Observable
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1
import rx.schedulers.Schedulers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.Socket
import java.util.*

class MainActivity : AppCompatActivity() {
    private val gson = Gson()
    private var mInputMessageView: EditText? = null
    private var output: PrintStream? = null
    private var input: BufferedReader? = null
    private var socket: Socket? = null
    private var messages = arrayListOf<Message>()
    private var adapter: MessagesAdapter = MessagesAdapter(messages)
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.messages_list) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = adapter

        mInputMessageView = findViewById(R.id.input_text) as EditText
        mInputMessageView!!.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendMessage()
                true
            } else {
                false
            }
        }

        Observable.just(true)
                .map(Func1<Boolean, Boolean> {
                    try {
                        socket = Socket("192.168.1.101", 9999)
                        output = PrintStream(socket!!.outputStream)
                        input = BufferedReader(InputStreamReader(socket!!.inputStream))
                        return@Func1 true
                    } catch (e: IOException) {
                        e.printStackTrace()
                        return@Func1 false
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}

                    override fun onNext(connected: Boolean) {
                        if (connected) {
                            getMessages()
                        }
                    }
                })

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            socket!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun sendMessage() {
        val text = mInputMessageView?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(text)) {
            return
        }
        mInputMessageView!!.setText("")
        val generalMessage = GeneralMessage()
        generalMessage.type = "post"
        generalMessage.data = gson.toJson(Message("test_name", text))
        output!!.println(gson.toJson(generalMessage))
    }

    private fun getMessages() {
        Observable.just(true)
                .map(Func1<Boolean, Messages> {
                    val generalMessage = GeneralMessage()
                    generalMessage.type = "get"
                    output!!.println(gson.toJson(generalMessage))
                    val answer = input!!.readLine()
                    val messages = gson.fromJson<Messages>(answer, Messages::class.java)
                    return@Func1 messages
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Messages> {
                    override fun onCompleted() {
                        observeServer()
                    }

                    override fun onError(e: Throwable) {}

                    override fun onNext(response: Messages?) {
                        messages.addAll(response!!.messages as ArrayList)
                        adapter.notifyDataSetChanged()
                        recyclerView!!.smoothScrollToPosition(messages.size - 1)
                    }
                })
    }

    private fun observeServer() {
        Observable.just(true)
                .map(Func1<Boolean, String> {
                    return@Func1 input!!.readLine()
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String> {
                    override fun onCompleted() {
                        observeServer()
                    }

                    override fun onError(e: Throwable) {}

                    override fun onNext(response: String?) {
                        System.out.println(response)
                        val message = gson.fromJson(response, Message::class.java)
                        messages.add(message)
                        adapter.notifyDataSetChanged()
                        recyclerView!!.smoothScrollToPosition(messages.size - 1)
                    }
                })
    }
}
