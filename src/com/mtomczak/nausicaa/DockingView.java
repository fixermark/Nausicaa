package com.mtomczak.nausicaa;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.util.AttributeSet;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * A view useful for aligning ships for docking.
 */
public class DockingView extends View implements TelemetryViewer {
  private AlertView alertOutput = null;

  public DockingView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setAlertOutput(AlertView view) {
    alertOutput = view;
  }

  @Override
    public void update(JSONObject telemetry) {
    invalidate();
  }
  @Override
    protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Paint p = new Paint();
    p.setColor(0xFFFF0000);
    canvas.drawRect(new Rect(5,5,100,100), p);

  }
}