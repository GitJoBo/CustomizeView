package com.jdkj.customizeview

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jdkj.customizeview.app.Constant
import com.jdkj.customizeview.view.slidingRing.SlidingRingView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView();
    }

    private fun initView() {
        slidingRingView.setProgressRange(0, 700)
        slidingRingView.setProgress(0)
        slidingRingView.mEndAngle = 300
        slidingRingView.mEndAnglef = 300f
        slidingRingView.mStartAngle = -240
        slidingRingView.mProgressOffest = 60;
        slidingRingView.setOnTextFinishListener(object :
            SlidingRingView.OnCirqueProgressChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onChange(minProgress: Int, maxProgress: Int, progress: Int) {
                tv_temperature.text = progress.toString() + Constant.du
            }

            override fun onChangeEnd(minProgress: Int, maxProgress: Int, progress: Int) {
                tv_temperature.text = (progress.toString() + Constant.du)
            }
        })

        btn_add.setOnClickListener { slidingRingView.addProgress() }
        btn_cut.setOnClickListener { slidingRingView.cutProgress() }

    }


}