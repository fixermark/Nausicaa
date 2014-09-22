package com.mtomczak.nausicaa;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * A grid pltting graphic panel
 */
class GridPlot extends GraphicPanel {
  public static final int GRID_FRAME_COLOR = 0xFF666666;
  public static final int GRID_CROSS_COLOR = 0xFF333333;

  public static final float GRID_CROSS_THICKNESS = 0.025f;
  public static final float GRID_ICON_THICKNESS = 0.025f;
  public static final int[] GRID_ICON_COLORS = {
    0xFF660066,
    0xFF990099,
    0xFF009900
  };

  public static final float[] GRID_ICON_THRESHOLDS = {
    0.25f,
    0.1f,
    0.0f
  };

  private float plotX = 0;
  private float plotY = 0;

  public GridPlot(Context ctx, AttributeSet attrs) {
    super(ctx, attrs);
  }

  /**
   * Clamp a value between boundaries
   */
  private float clamp(float value, float min, float max) {
    return value < min ? min :
      (value > max ? max : value);
  }

  /** Update the plot measurement
   *
   * @param x X coordinate of target, ranging from -1 to 1
   * @param y Y coordinate of target, ranging from -1 to 1
   */
  public void update(float x, float y) {
    plotX = clamp(x, -1f, 1f);
    plotY = clamp(y, -1f, 1f);
  }

  @Override
    protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.drawColor(0xFF000000);

    float height = (float)getHeight();
    float width = (float)getWidth();

    // scale to (-1,1)
    canvas.scale(width / 2.0f, height / 2.0f);
    canvas.translate(1.0f, 1.0f);

    Paint p = new Paint();
    p.setStyle(Paint.Style.STROKE);
    p.setStrokeWidth(GRID_ICON_THICKNESS);
    float displacement = Math.max(Math.abs(plotX), Math.abs(plotY));
    for (int i = 0; i < GRID_ICON_THRESHOLDS.length; i++) {
      if (displacement >= GRID_ICON_THRESHOLDS[i]) {
	p.setColor(GRID_ICON_COLORS[i]);
	break;
      }
    }
    canvas.drawCircle(plotX, plotY, .05f, p);

    p.setColor(GRID_CROSS_COLOR);
    p.setStrokeWidth(GRID_CROSS_THICKNESS);
    canvas.drawLine(-1f, 0, 1f, 0, p);
    canvas.drawLine(0, -1f, 0, 1f, p);

    p.setColor(GRID_FRAME_COLOR);
    canvas.drawRect(-1f, -1f, 1f, 1f, p);
  }
}