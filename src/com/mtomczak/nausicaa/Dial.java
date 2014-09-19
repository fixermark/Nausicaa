package com.mtomczak.nausicaa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * An indicator dial
 */

class Dial extends View {
  public static final int DIAL_FRAME_COLOR = 0xFF666666;
  public static final float DIAL_FRAME_THICKNESS = 3.0f;
  public static final float DIAL_NEEDLE_THICKNESS = 2.0f;
  public static final float DIAL_PADDING = 3.0f;
  public static final int[] DIAL_NEEDLE_COLORS = {
    0xFFFF0000,
    0xFFFFFF00,
    0xFF00FF00
  };
  public static final double[] DIAL_THRESHOLDS = {
    20.0,
    10.0,
    0.0
  };

  private double reading = 0;
  private double offset = 0;
  private boolean flip = false;
  private Path icon = null;

  public Dial(Context ctx, AttributeSet attrs) {
    super(ctx, attrs);
  }

  public void setOffset(double o) {
    offset = o;
  }

  public void setFlip(boolean f) {
    flip = f;
  }

  /**
   * Set the icon displayed in the center of the dial.
   * Note: It is assumed the icon image is 400 x 400.
   *
   * @param p The path to use as an icon.
   */
  public void setIcon(Path p) {
    icon = p;
  }

  public void update(double r) {
    reading = r;
  }

  @Override
    protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawColor(0xFF000000);

    int height = getHeight();
    int width = getWidth();

    float centerX = (float)(width) / 2.0f;
    float centerY = (float)(height) / 2.0f;
    float length = centerX - DIAL_PADDING;
    double angle = Math.toRadians((reading + offset) * (flip ? -1.0 : 1.0));

    Paint p = new Paint();
    p.setStrokeWidth(DIAL_FRAME_THICKNESS);
    p.setStyle(Paint.Style.STROKE);
    p.setColor(DIAL_FRAME_COLOR);
    canvas.drawCircle(centerX, centerY, length, p);

    float px = length * (float)(Math.cos(angle)) + centerX;
    float py = length * (float)(Math.sin(angle)) + centerY;

    p.setStrokeWidth(DIAL_NEEDLE_THICKNESS);
    for (int i = 0; i < DIAL_THRESHOLDS.length; i++) {
      if (Math.abs(reading) >= DIAL_THRESHOLDS[i]) {
	p.setColor(DIAL_NEEDLE_COLORS[i]);
	break;
      }
    }
    canvas.drawLine(centerX, centerY, px, py, p);

    if (icon != null) {
      float sx = (float)(width) / 400;
      float sy = (float)(height) / 400;
      canvas.scale(sx, sy);
      //      canvas.translate(-200, -200);//-centerX, -centerY);
      p.setColor(DIAL_FRAME_COLOR);
      p.setStyle(Paint.Style.FILL_AND_STROKE);
      canvas.drawPath(icon, p);
      canvas.setMatrix(null);
    }
  }

  @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Square off the dials by width
    setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
  }
}