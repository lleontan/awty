package edu.us.ischool.tanl2.arewethereyet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URI
import java.util.*
import android.app.PendingIntent
import android.content.res.AssetManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    var text_valid: Boolean = false
    var number_valid: Boolean = false
    var duration_valid: Boolean = false
    private lateinit var timer: CountDownTimer
    lateinit var timerObj: TimerTask
    val timerMod = 60000L
    var timerRunning: Boolean = false
    var textContentType = 0
    val SEND_SMS_REQUEST_CODE = 42

    private companion object {
        private var instance: MainActivity? = null

        fun applicationContext(): MainActivity {
            return instance as MainActivity
        }
    }

    private val sendMessage = object : TimerTask() {
        override fun run() {
            runOnUiThread {
                //exec()
                mainExec()
            }
        }

        fun mainExec() {
            var targetUrl = phoneEditInput.text.toString()
            val numberValidRegex = Regex("[0-9]{1,12}")
            if (targetUrl.matches(numberValidRegex)) {
                Log.v("networks", "Message worked with regex")
                when (textContentType) {
                    1 -> sendAudio(targetUrl)
                    2 -> sendVideo(targetUrl)
                    else -> sendSMS(textEditInput.text.toString(), targetUrl)
                }
            }
        }

        fun sendAudio(targetUrl: String) {
            val smsManager = SmsManager.getDefault() as SmsManager
            val uriStr = getString(R.string.default_audio_url)
            val filePath = File(applicationContext.cacheDir,uriStr).path
            var contentUrl: Uri = Uri.parse("$filePath")
            val doesItExist=File(contentUrl.path).exists()
            Log.v("networks", "sending audio $uriStr, exists? $doesItExist")
            smsManager.sendMultimediaMessage(MainActivity.applicationContext(), contentUrl, targetUrl, null, null)
            val messageString = "Audio sent to: $targetUrl"
            var message = Toast.makeText(applicationContext, messageString, Toast.LENGTH_LONG)
            
            message.setText(messageString)
            message.show()
        }

        fun sendVideo(targetUrl: String) {
            val smsManager = SmsManager.getDefault() as SmsManager
            val defaultVideoUrl: String = getString(R.string.default_video_url)
            val fileExists = File(applicationContext.cacheDir,defaultVideoUrl).path
            var contentUrl: Uri = Uri.parse("$fileExists")
            Log.v("networks", "sending audio $defaultVideoUrl, exists? $fileExists")
            smsManager.sendMultimediaMessage(MainActivity.applicationContext(), contentUrl, targetUrl, null, null)
            val messageString = "Video sent to: $targetUrl"
            var message = Toast.makeText(applicationContext, messageString, Toast.LENGTH_LONG)
            message.setText(messageString)
            message.show()
        }

        fun sendSMS(text: String, targetUrl: String) {
            Log.v("networks", "sending to $targetUrl")
            val smsManager = SmsManager.getDefault() as SmsManager
            val sentIntent =
                PendingIntent.getBroadcast(MainActivity.applicationContext(), 0, Intent(Intent.ACTION_SENDTO), 0)
            val deliveredIntent =
                PendingIntent.getBroadcast(MainActivity.applicationContext(), 0, Intent(Intent.ACTION_SENDTO), 0)
            smsManager.sendTextMessage(targetUrl, null, text, null, null)
            val messageString = "Text sent to: $targetUrl"
            var message = Toast.makeText(applicationContext, messageString, Toast.LENGTH_LONG)
            message.setText(messageString)
            message.show()
            /*val uri=Uri.parse("smsto:$targetUrl")
            val smsIntent=Intent(Intent.ACTION_VIEW,uri)
            smsIntent.putExtra("sms_body",text)
            startActivity(smsIntent)*/
        }

        fun exec() {
            val basePhoneNumberStr = phoneEditInput.text.toString()
            var phoneNumberStr = ""
            val basePhoneLength = basePhoneNumberStr.length
            if (basePhoneLength > 3) {
                phoneNumberStr = "(${basePhoneNumberStr.substring(0, 3)})"
                if (basePhoneLength > 6) {
                    phoneNumberStr =
                        "$phoneNumberStr ${basePhoneNumberStr.substring(3, 6)}-${basePhoneNumberStr.substring(6)}"
                }
            } else {
                phoneNumberStr = basePhoneNumberStr
            }
            val messageString = "$phoneNumberStr: ${textEditInput.text.toString()}"
            var message = Toast.makeText(applicationContext, messageString, Toast.LENGTH_SHORT)
            message.setText(messageString)
            message.show()
        }
    }

    private fun makeGenericToast(messageStr: String) {
        var message = Toast.makeText(applicationContext, messageStr, Toast.LENGTH_SHORT)
        message.setText(messageStr)
        message.show()
    }

    private fun cannotStart() {
        if (text_valid) {
            if (number_valid) {
                if (duration_valid) {

                } else {
                    makeGenericToast("Invalid duration")
                }
            } else {
                makeGenericToast("Invalid phone number")
            }
            makeGenericToast("Invalid text length")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            SEND_SMS_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun writeFiles(fileName: String) {
        val cacheFile = File(applicationContext.cacheDir, fileName)
        if (!cacheFile.exists()) {
            try {
                val inputStream = applicationContext.assets.open(fileName)
                val outputStream = FileOutputStream(cacheFile)
                try {
                    inputStream.copyTo(outputStream)
                } finally {
                    inputStream.close()
                    outputStream.close()
                }
            } catch (e: IOException) {
                throw IOException("Could not open $fileName", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        writeFiles(getString(R.string.default_video_url))
        writeFiles(getString(R.string.default_audio_url))
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                SEND_SMS_REQUEST_CODE
            )
        }
        setContentView(R.layout.activity_main)
        ArrayAdapter.createFromResource(
            this,
            R.array.text_content_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            text_options_spinner.adapter = adapter
        }
        text_options_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                textContentType = position
            }

        }
        startButton.setOnClickListener {
            if (timerRunning) {
                timerRunning = false
                timer.cancel()
                startButton.setText(R.string.start_button_text)
            } else {
                startButton.setText(R.string.stop_button_text)
                timerRunning = true
                val delay: Long = timeEditInput.text.toString().toLong() * timerMod
                timerObj = sendMessage
                timer = object : CountDownTimer(delay, timerMod) {
                    override fun onFinish() {
                        //sendMessage.exec()
                        Log.v("networks", "Timer cycle finished")
                        sendMessage.mainExec()
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
                var textMatch: Regex = Regex("^(.){1,42}$")
                text_valid = s.matches(textMatch)
                tryActivateStart()
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })
        phoneEditInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                var numberMatch: Regex = Regex("^[0-9]{1,12}$")
                number_valid = s.matches(numberMatch)
                tryActivateStart()
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })
        timeEditInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                var timeMatch: Regex = Regex("^[1-9][0-9]{0,5}$")
                duration_valid = s.matches(timeMatch)
                tryActivateStart()
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })
    }

    private fun canStart(): Boolean {
        return text_valid && number_valid && duration_valid
    }

    private fun tryActivateStart() {
        if (timerRunning) {
            timerRunning = false
            timer.cancel()
            startButton.setText(R.string.start_button_text)
        }
        startButton.isEnabled = canStart()
    }
}
