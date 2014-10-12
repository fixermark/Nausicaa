/*
Copyright 2014 Mark T. Tomczak

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.mtomczak.nausicaa;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
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
    70f, 220.5f,
    83f, 214.5f,
    96f, 201.5f,
    101f, 188.5f,
    281f, 188.5f,
    315f, 140.5f,
    330f, 140.5f,
    315f, 186.5f,
    307f, 195.5f,
    324f, 192.5f,
    324f, 213.5f,
    306f, 208.5f,
    314f, 216.5f,
    227f, 216.5f,
    242f, 235.5f,
    203f, 235.5f,
    170f, 216.5f,
    70f, 222.5f
    };

  private static final float[] YAW_IMAGE = {
    200f, 102f,
    210f, 102f,
    216f, 118f,
    216f, 205f,
    219f, 213f,
    265f, 259f,
    268f, 268f,
    267f, 277f,
    214f, 277f,
    221f, 298f,
    203f, 298f,
    205f, 273f,
    195f, 273f,
    197f, 298f,
    179f, 298f,
    186f, 277f,
    133f, 277f,
    132f, 268f,
    135f, 259f,
    181f, 213f,
    184f, 205f,
    184f, 118f,
    190f, 102f,
    200f, 102f,
  };

  // TODO(mtomczak): Allow dials to take in Path objects to render, and bake the paths here.
  private AlertView alertOutput = null;
  private Dial pitchDial = null;
  private Dial yawDial = null;
  private TextView dockData = null;
  private TextView rcsFuel = null;
  private Path pitchImage = null;
  private Path yawImage = null;

  public DockingView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
    protected void onFinishInflate() {
    pitchDial = (Dial)findViewById(R.id.horizontaldial);
    yawDial = (Dial)findViewById(R.id.verticaldial);
    dockData= (TextView)findViewById(R.id.dockdata);

    pitchDial.setOffset(-90);
    pitchDial.setIcon(encodePath(YAW_IMAGE));
    yawDial.setOffset(180);
    yawDial.setFlip(true);
    yawDial.setIcon(encodePath(PITCH_IMAGE));

    super.onFinishInflate();
  }

  /**
   * Encode a list of x,y coords into a path
   *
   * @param points Points to encode
   * @return The encoded path
   */
  private Path encodePath(float[] points) {
    Path path = new Path();
    path.moveTo(points[0], points[1]);
    for (int i = 2; i < points.length; i += 2) {
      path.lineTo(points[i], points[i + 1]);
    }
    path.close();
    path.setFillType(Path.FillType.WINDING);
    return path;
  }

  public void setAlertOutput(AlertView view) {
    alertOutput = view;
  }

  @Override
    public void update(JSONObject telemetry) {
    // TODO(mtomczak): Need to account for all 0s, indicating no docking target
    // set.
    try {
      double distance = telemetry.getDouble("tar.distance");
      double xDistance = telemetry.getDouble("dock.x");
      double yDistance = telemetry.getDouble("dock.y");
      // TODO(mtomczak): Need better window shading for no selected dock target.
      // if (xDistance == 0 && yDistance == 0) {
      // 	if (alertOutput != null) {
      // 	  alertOutput.alert("[No docking target selected]");
      // 	}
      // 	return;
      // }
      double rcsFuelVal = telemetry.getDouble("r.resource[MonoPropellant]");
      double rcsFuelMax = telemetry.getDouble("r.resourceMax[MonoPropellant]");
      pitchDial.update(telemetry.getDouble("dock.ax"));
      yawDial.update(telemetry.getDouble("dock.ay"));

      String output = "\nDistance:\n" +
	new DecimalFormat("#,###.##").format(distance) + "m";

      if (rcsFuelMax > 0) {
	output += "\n\nRCS fuel:\n" +
	  new DecimalFormat("000.##").format(
	    rcsFuelVal / rcsFuelMax * 100.0) + "%";
      }
      dockData.setText(output);
      postInvalidate();
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