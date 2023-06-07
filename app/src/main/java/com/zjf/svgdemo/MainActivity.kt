package com.zjf.svgdemo

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zjf.svgview.SVGView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val svgView = findViewById<SVGView>(R.id.SVGView)
        svgView.zoomSpeed = 1F
        svgView.moveSpeed = 3F
        svgView.setOnClickListener(SVGView.OnSVGClickListener {
            if (it.select) {
                it.color = Color.RED
            } else {
                it.color = Color.BLUE;
            }
        })
    }
}