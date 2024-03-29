package com.kkyoungs.stopwatch

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.kkyoungs.stopwatch.databinding.ActivityMainBinding
import com.kkyoungs.stopwatch.databinding.DialogCountdownSettingBinding
import kotlinx.coroutines.CoroutineScope
import java.util.Timer
import kotlin.concurrent.timer
import kotlin.jvm.internal.Intrinsics.Kotlin
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var countdownSecond = 10
    private var currentDeciSecond = 0
    private var currentCountDownDeciSecond = countdownSecond * 10
    private  var timer : Timer?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.countdownTextView.setOnClickListener {
            showCountdownSettingDialog()
        }

        binding.startButton.setOnClickListener {
            start()
            binding.startButton.isVisible = false
            binding.stopButton.isVisible = false
            binding.pauseButton.isVisible = true
            binding.lapButton.isVisible = true
        }
        binding.stopButton.setOnClickListener {
            showAlertDialog()

        }
        binding.pauseButton.setOnClickListener {
            pause()
            binding.startButton.isVisible = true
            binding.stopButton.isVisible = true
            binding.pauseButton.isVisible = false
            binding.lapButton.isVisible = false
        }
        binding.lapButton.setOnClickListener {
            lap()
        }
        initView()
    }

    private fun initView(){
        binding.countdownTextView.text = String.format("%02d", countdownSecond)
        binding.countdownProgressBar.progress = 100

    }

    private fun start() {
       timer =  timer(initialDelay = 0, period = 100) {

           if (currentCountDownDeciSecond == 0){
               currentDeciSecond += 1

               val minutes = currentDeciSecond.div(10) / 60
               val second = currentDeciSecond.div(10) % 60
               val deciSecond = currentDeciSecond % 10


               runOnUiThread {
                   binding.timeTextView.text = String.format("%02d:%02d", minutes, second)
                   binding.tickTextView.text = deciSecond.toString()
                   binding.countDownGroup.isVisible = false
               }

           }else{
               currentCountDownDeciSecond -= 1
               val second = currentCountDownDeciSecond / 10 
               val progress = (currentCountDownDeciSecond / ( countdownSecond * 10f))  * 100

               binding.root.post{
                   binding.countdownTextView.text = String.format("%02d", second)
                   binding.countdownProgressBar.progress = progress.toInt()
               }

           }
           if (currentDeciSecond == 0 && currentCountDownDeciSecond < 31 && currentCountDownDeciSecond % 10 == 0){
               val toneType = if (currentCountDownDeciSecond == 0) ToneGenerator.TONE_CDMA_HIGH_L else ToneGenerator.TONE_CDMA_ANSWER

               ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME).startTone(toneType, 100)

           }
           }


    }

    private fun stop() {
        binding.startButton.isVisible = true
        binding.stopButton.isVisible = true
        binding.pauseButton.isVisible = false
        binding.lapButton.isVisible = false

        currentDeciSecond = 0
        binding.timeTextView.text = "00:00"
        binding.tickTextView.text = "0"

        binding.countDownGroup.isVisible = true
        initView()
        binding.lapContainerLinerLayout.removeAllViews()
    }

    private fun pause() {
        timer?.cancel()
        timer = null
    }

    private fun lap() {
        val container = binding.lapContainerLinerLayout
        TextView(this).apply {
            textSize = 20f
            gravity = Gravity.CENTER
            val minutes = currentDeciSecond.div(10) / 60
            val seconds = currentDeciSecond.div(10) %60
            val deciSeconds = currentDeciSecond % 10
            text = container.childCount.inc().toString() + String.format("%02d:%02d, %01d", minutes, seconds, deciSeconds)
            // 예시 : 1. 01:03 0
            setPadding(30)
        }.let{ labTextView ->
            container.addView(labTextView, 0)
        }
    }

    private fun showCountdownSettingDialog() {
        if (currentCountDownDeciSecond == 0) return
        AlertDialog.Builder(this).apply {
            val dialogBinding = DialogCountdownSettingBinding.inflate(layoutInflater)
            with(dialogBinding.countdownSecondPicker) {
                maxValue = 20
                minValue = 0
                value = countdownSecond
            }
            setTitle("카운트다운 설정")
            setView(dialogBinding.root)
            setPositiveButton("확인") { _, _ ->

                countdownSecond = dialogBinding.countdownSecondPicker.value
                currentCountDownDeciSecond = countdownSecond * 10
                // 3초면 03으로 해주는 string format
                binding.countdownTextView.text = String.format("%02d", countdownSecond)
            }
            setNegativeButton("취소", null)
        }.show()
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("종료하시겠습니까?")
            setPositiveButton("네") { _, _ ->
                stop()
            }
            setNegativeButton("아니요", null)
        }.show()
    }
}