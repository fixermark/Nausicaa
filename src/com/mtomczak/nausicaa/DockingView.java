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
  private static final float[] PITCH_IMAGE = {
    70, 232.5,
    83, 226.5,
    96, 213.5,
    101, 200.5,
    281, 200.5,
    315, 152.5,
    330, 152.5,
    315, 198.5,
    307, 207.5,
    324, 204.5,
    324, 225.5,
    306, 220.5,
    314, 228.5,
    227, 228.5,
    242, 247.5,
    203, 247.5,
    170, 228.5,
    70, 234.5
    };

  private static final float[] YAW_IMAGE = {
    200, 102,
    210, 102,
    216, 118,
    216, 205,
    219, 213,
    265, 259,
    268, 268,
    267, 277,
    214, 277,
    221, 298,
    203, 298,
    205, 273,
    195, 273,
    197, 298,
    179, 298,
    186, 277,
    133, 277,
    132, 268,
    135, 259,
    181, 213,
    184, 205,
    184, 118,
    190, 102,
    200, 102,
  };

  // TODO(mtomczak): Allow dials to take in Path objects to render, and bake the paths here.
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