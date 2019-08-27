package com.anwesh.uiprojects.biboxlinecreateview

/**
 * Created by anweshmishra on 27/08/19.
 */

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.view.View
import android.view.MotionEvent

val nodes : Int = 5
val lines : Int = 2
val scGap : Float = 0.05f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val scDiv : Double = 0.51
val foreColor : Int = Color.parseColor("#1A237E")
val backColor : Int = Color.parseColor("#BDBDBD")
val parts : Int = 2

fun Int.inverse() : Float = 1f / this
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.mirrorValue(a : Int, b : Int) : Float {
    val k : Float = scaleFactor()
    return (1 - k) * a.inverse() + k * b.inverse()
}
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawBoxLine(sc1 : Float, sc2 : Float, size : Float, h : Float, paint : Paint) {
    val sc11 : Float = sc1.divideScale(0, parts)
    val sc12 : Float = sc1.divideScale(1, parts)
    val oy : Float = (h / 2 + 2 * size)
    val dy : Float = size
    save()
    rotate(90f * sc2)
    save()
    translate(0f, oy + (dy - oy) * sc11)
    drawLine(0f, 0f, 0f, 2 * size * (1 - sc12), paint)
    drawLine(-size * sc12, 0f, size * sc12, 0f, paint)
    restore()
    restore()
}

fun Canvas.drawBiBoxLine(sc1 : Float, sc2 : Float, size : Float, h : Float, paint : Paint) {
    for (j in 0..(lines - 1)) {
        save()
        scale(1f, 1f - 2 * j)
        drawBoxLine(sc1.divideScale(j, lines), sc2.divideScale(j, lines), size, h, paint)
        restore()
    }
}

fun Canvas.drawBBLCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(gap * (i + 1), h / 2)
    drawBiBoxLine(sc1, sc2, size, h, paint)
    restore()
}

class BiBoxLineCreateView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}