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
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import java.text.DecimalFormat;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * View of general ship's status information
 */

public class StatusView extends TextView implements TelemetryViewer {

  private AlertView alertOutput = null;

  public static final String TIME_WARP_KEY="nausicaa_timewarp";

  public StatusView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }


  public void setAlertOutput(AlertView view) {
    alertOutput = view;
  }

  private String formatDouble(double in) {
    return new DecimalFormat("#,000.##").format(in);
  }

  @Override
    public void update(JSONObject telemetry) {
    String out = "";
    try {
      String bodyName = telemetry.getString("v.body");
      out += "[" + bodyName + "]";
      if (telemetry.getBoolean(TIME_WARP_KEY)) {
	out += " [T]";
      }
      out += "\n";
      out += "Altitude: " + formatDouble(telemetry.getDouble("v.altitude")) + "\n";
      out += "Velocity(orbit): " +
	formatDouble(telemetry.getDouble("v.orbitalVelocity")) + "\n";
      out += "Speed(vert): " +
	formatDouble(telemetry.getDouble("v.verticalSpeed")) + "\n";
      out += "Apoapsis: ";
      double apoapsis = telemetry.getDouble("o.ApA");
      if (apoapsis < 0) {
	out += "[escaping]\n";
      } else {
	out += formatDouble(telemetry.getDouble("o.ApA")) + "\n";
      }
      double periapsis = telemetry.getDouble("o.PeA");
      out += "Periapsis: " + formatDouble(periapsis) + "\n";
      double electricCharge = telemetry.getDouble("r.resource[ElectricCharge]");
      double electricChargeMax = telemetry.getDouble("r.resourceMax[ElectricCharge]");
      double electricChargePercent = electricCharge / electricChargeMax;
      out += "Electric %: " + formatDouble(electricChargePercent * 100) + "\n";
      double fuel = telemetry.getDouble("r.resource[LiquidFuel]");
      double fuelMax = telemetry.getDouble("r.resourceMax[LiquidFuel]");
      out += "Fuel %: " + formatDouble(fuel / fuelMax * 100);

      setText(out);
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