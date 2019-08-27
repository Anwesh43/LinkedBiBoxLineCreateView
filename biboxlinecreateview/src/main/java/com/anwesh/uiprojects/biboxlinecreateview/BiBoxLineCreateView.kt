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

fun Canvas.drawBoxLine(i : Int, sc1 : Float, sc2 : Float, size : Float, h : Float, paint : Paint) {
    val sc11 : Float = sc1.divideScale(0, parts)
    val sc12 : Float = sc1.divideScale(1, parts)
    val oy : Float = (h / 2 + 2 * size)
    val dy : Float = size
    for (j in 0..(parts - 1)) {
        save()
        rotate(90f * sc2 * j * (1f - 2 * i))
        save()
        translate(0f, oy + (dy - oy) * sc11)
        drawLine(0f, 0f, 0f, 2 * size * (1 - sc12), paint)
        drawLine(-size * sc12, 0f, size * sc12, 0f, paint)
        restore()
        restore()
    }
}

fun Canvas.drawBiBoxLine(sc1 : Float, sc2 : Float, size : Float, h : Float, paint : Paint) {
    for (j in 0..(lines - 1)) {
        save()
        scale(1f, 1f - 2 * j)
        drawBoxLine(j, sc1.divideScale(j, lines), sc2.divideScale(j, lines), size, h, paint)
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
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines * parts, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BBLCNode(var i : Int, val state : State = State()) {

        private var next : BBLCNode? = null
        private var prev : BBLCNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BBLCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBBLCNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BBLCNode {
            var curr : BBLCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BiBoxLineCreate(var i : Int) {

        private val root : BBLCNode = BBLCNode(0)
        private var curr : BBLCNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BiBoxLineCreateView) {

        private val animator : Animator = Animator(view)
        private val bblc : BiBoxLineCreate = BiBoxLineCreate(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            bblc.draw(canvas, paint)
            animator.animate {
                bblc.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bblc.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BiBoxLineCreateView {
            val view : BiBoxLineCreateView = BiBoxLineCreateView(activity)
            activity.setContentView(view)
            return view
        }
    }
}