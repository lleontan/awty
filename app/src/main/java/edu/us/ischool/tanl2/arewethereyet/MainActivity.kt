package edu.us.ischool.tanl2.arewethereyet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    var text_valid:Boolean = false
    var number_valid:Boolean = false
    var duration_valid:Boolean = false
    private lateinit var timer: CountDownTimer
    lateinit var timerObj:TimerTask
    val timerMod=60000L
    var timerRunning:Boolean=false

    private val sendMessage = object : TimerTask() {

        override fun run() {
            runOnUiThread {
            exec()
            }
        }
        fun exec(){
            val basePhoneNumberStr=phoneEditInput.text.toString()
            var phoneNumberStr=""
            val basePhoneLength=basePhoneNumberStr.length
            if(basePhoneLength>3){
                phoneNumberStr="(${basePhoneNumberStr.substring(0,3)})"
                if(basePhoneLength>6){
                    phoneNumberStr="$phoneNumberStr ${basePhoneNumberStr.substring(3,6)}-${basePhoneNumberStr.substring(6)}"
                }
            }else{
                phoneNumberStr=basePhoneNumberStr
            }
            val messageString="$phoneNumberStr: ${textEditInput.text.toString()}"
            var message=Toast.makeText(applicationContext,messageString,Toast.LENGTH_SHORT)
            message.setText(messageString)
            message.show()
        }
    }
    private fun makeGenericToast(messageStr:String){
        var message=Toast.makeText(applicationContext,messageStr,Toast.LENGTH_SHORT)
        message.setText(messageStr)
        message.show()
    }
    private fun cannotStart(){
        if(text_valid){
            if(number_valid){
                if(duration_valid){

                }else{
                    makeGenericToast("Invalid duration")
                }
            }else{
                makeGenericToast("Invalid phone number")
            }
            makeGenericToast("Invalid text length")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)
        startButton.setOnClickListener {
            if(timerRunning){
                timerRunning=false
                timer.cancel()
                startButton.setText(R.string.start_button_text)
            }else{
                startButton.setText(R.string.stop_button_text)
                timerRunning=true
                val delay:Long=timeEditInput.text.toString().toLong() * timerMod
                timerObj=sendMessage
                timer=object : CountDownTimer(delay, timerMod) {
                    override fun onFinish(){
                        sendMessage.exec()
                        timer.cancel()
                        timer.start()
                    }

                    override fun onTick(millisUntilFinished: Long) {
                    }
                }
                timer.start()
            }
        }
        textEditInput.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                var textMatch:Regex=Regex("^(.){1,42}$")
                text_valid=s.matches(textMatch)
                tryActivateStart()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }
        })
        phoneEditInput.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable) {
                var numberMatch:Regex=Regex("^[0-9]{1,12}$")
                number_valid=s.matches(numberMatch)
                tryActivateStart()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }
        })
        timeEditInput.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable) {
                var timeMatch:Regex=Regex("^[1-9][0-9]{0,5}$")
                duration_valid=s.matches(timeMatch)
                tryActivateStart()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }
        })
    }
    private fun canStart():Boolean{
        return text_valid&&number_valid&&duration_valid
    }
    private fun tryActivateStart(){
        if(timerRunning){
            timerRunning=false
            timer.cancel()
            startButton.setText(R.string.start_button_text)
        }
        startButton.isEnabled=canStart()
    }
}
