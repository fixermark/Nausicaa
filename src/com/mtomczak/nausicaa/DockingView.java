package com.mtomczak.nausicaa;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.text.DecimalFormat;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * A view useful for aligning ships for docking.
 */
public class DockingView extends RelativeLayout implements TelemetryViewer {
  private AlertView alertOutput = null;
  private Dial horizontalDial = null;
  private Dial verticalDial = null;
  private TextView rcsFuel = null;

  public DockingView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
    protected void onFinishInflate() {
    horizontalDial = (Dial)findViewById(R.id.horizontaldial);
    verticalDial = (Dial)findViewById(R.id.verticaldial);
    rcsFuel = (TextView)findViewById(R.id.rcsfuel);

    horizontalDial.setOffset(-90);
    verticalDial.setOffset(180);
    verticalDial.setFlip(true);

    super.onFinishInflate();
  }

  public void setAlertOutput(AlertView view) {
    alertOutput = view;
  }

  @Override
    public void update(JSONObject telemetry) {
    // TODO(mtomczak): Need to account for all 0s, indicating no docking target
    // set.
    try {
      double rcsFuelVal = telemetry.getDouble("r.resource[MonoPropellant]");
      double rcsFuelMax = telemetry.getDouble("r.resourceMax[MonoPropellant]");
      horizontalDial.update(telemetry.getDouble("dock.ax"));
      verticalDial.update(telemetry.getDouble("dock.ay"));


      if (rcsFuelMax > 0) {
	rcsFuel.setText(
	  "RCS fuel: " +
	  new DecimalFormat("000.##").format(
	    rcsFuelVal / rcsFuelMax * 100.0) + "%");
      } else {
	rcsFuel.setText("");
      }
      invalidate();
    } catch(JSONException e) {
      Log.e("Nausicaa", e.toString());
      String trace = "";
      for (StackTraceElement el: e.getStackTrace()) {
	trace += el.toString();
      }
      Log.e("Nausicaa", trace);
      if (alertOutput != null) {
	alertOutput.alert("<<PARSE ERROR>>");
      }
    }
  }
}