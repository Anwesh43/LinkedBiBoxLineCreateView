package com.anwesh.uiprojects.linkedbiboxlinecreateview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.biboxlinecreateview.BiBoxLineCreateView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BiBoxLineCreateView.create(this)
    }
}
