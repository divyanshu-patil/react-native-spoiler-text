package com.blurtext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt
import kotlin.random.Random

class SpoilerView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : View(context, attrs) {

  private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Int,
    var radius: Float,
    var life: Float,       // 0..1
    var maxLife: Float,
  )

  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val particles = mutableListOf<Particle>()
  private var particleColor: Int = 0xFF000000.toInt()

  private var running = false
  private val frameCallback = object : android.view.Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
      if (!running) return
      update()
      invalidate()
      android.view.Choreographer.getInstance().postFrameCallback(this)
    }
  }

  // ── Public API ───────────────────────────────────────────────────────────

  fun setParticleColor(color: Int) {
    particleColor = color
    invalidate()
  }

  fun startAnimation() {
    if (running) return
    running = true
    android.view.Choreographer.getInstance().postFrameCallback(frameCallback)
  }

  fun stopAnimation() {
    running = false
    android.view.Choreographer.getInstance().removeFrameCallback(frameCallback)
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    spawnParticles(w, h)
  }

  // ── Internal ─────────────────────────────────────────────────────────────

  private fun spawnParticles(w: Int, h: Int) {
    particles.clear()
    if (w == 0 || h == 0) return

    // ~1 particle per 12px²
    val count = (w * h / 12f).roundToInt().coerceIn(20, 800)

    repeat(count) {
      particles.add(randomParticle(w.toFloat(), h.toFloat()))
    }
  }

  private fun randomParticle(w: Float, h: Float): Particle {
    val maxLife = Random.nextFloat() * 60f + 30f  // frames
    return Particle(
      x = Random.nextFloat() * w,
      y = Random.nextFloat() * h,
      vx = (Random.nextFloat() - 0.5f) * 0.8f,
      vy = (Random.nextFloat() - 0.5f) * 0.8f,
      alpha = Random.nextInt(80, 200),
      radius = Random.nextFloat() * 1.2f + 0.4f,
      life = Random.nextFloat() * maxLife,
      maxLife = maxLife,
    )
  }

  private fun update() {
    val w = width.toFloat()
    val h = height.toFloat()
    if (w == 0f || h == 0f) return

    val iterator = particles.iterator()
    val toAdd = mutableListOf<Particle>()

    while (iterator.hasNext()) {
      val p = iterator.next()
      p.x += p.vx
      p.y += p.vy
      p.life += 1f

      // fade in/out over lifetime
      val progress = p.life / p.maxLife
      p.alpha = when {
        progress < 0.2f -> (progress / 0.2f * 200).roundToInt()
        progress > 0.8f -> ((1f - (progress - 0.8f) / 0.2f) * 200).roundToInt()
        else -> 200
      }.coerceIn(0, 255)

      if (p.life >= p.maxLife) {
        iterator.remove()
        toAdd.add(randomParticle(w, h))
      }
    }

    particles.addAll(toAdd)
  }

  override fun onDraw(canvas: Canvas) {
    val r = (particleColor shr 16) and 0xFF
    val g = (particleColor shr 8) and 0xFF
    val b = particleColor and 0xFF

    for (p in particles) {
      paint.color = android.graphics.Color.argb(p.alpha, r, g, b)
      canvas.drawCircle(p.x, p.y, p.radius, paint)
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    stopAnimation()
  }
}
