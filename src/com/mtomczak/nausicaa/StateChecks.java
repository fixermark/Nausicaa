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

import android.util.Log;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Collection of state checks.
 */
public class StateChecks {
  /**
   * Checks for circular orbit.
   */
  public static class CircularOrbit implements StateCheck {
    private static final double ECCENTRICITY_THRESHOLD = 0.09;

    @Override public boolean check(JSONObject telemetry) throws JSONException {
      double apoapsis = telemetry.getDouble("o.ApA");
      double periapsis = telemetry.getDouble("o.PeA");

      if (apoapsis + periapsis != 0) {
	double eccentricity = (apoapsis - periapsis) / (apoapsis + periapsis);
	return eccentricity > 0 && eccentricity < ECCENTRICITY_THRESHOLD;
      }
      return false;
    }
  }

  public static class AtmosphereStrike implements StateCheck {

    private HashMap<String, Double> atmosphericData = null;

    // TODO(mtomczak): This should be static data.
    public AtmosphereStrike() {
      atmosphericData = new HashMap();
      // Moho has no atmosphere
      atmosphericData.put("Eve", new Double(90000.0));
        // Gilly has no atmosphere
      atmosphericData.put("Kerbin", new Double(70000.0));
        // Mun has no atmosphere
        // Minmus has no atmosphere
      atmosphericData.put("Duna", new Double(50000.0));
        // Ike has no atmosphere
        // Dres has no atmosphere
      atmosphericData.put("Jool", new Double(200000.0));
        atmosphericData.put("Laythe", new Double(50000.0));
        // Vall has no atmosphere
        // Tylo has no atmosphere
        // Bop  has no atmosphere
        // Pol  has no atmosphere
      // Eeloo has no atmosphere
    }

    @Override public boolean check(JSONObject telemetry) throws JSONException {
      String bodyName = telemetry.getString("v.body");
      if (atmosphericData.containsKey(bodyName)) {
	double periapsis = telemetry.getDouble("o.PeA");
	return periapsis > 0 && periapsis < (atmosphericData.get(bodyName) * 0.75);
      }
      return false;
    }
  }

  public static class ElectricityCritical implements StateCheck {
    @Override
      public boolean check(JSONObject telemetry) throws JSONException {
      double electricCharge = telemetry.getDouble("r.resource[ElectricCharge]");
      double electricChargeMax = telemetry.getDouble(
	"r.resourceMax[ElectricCharge]");
      return electricChargeMax == 0 ?
	false :
	electricCharge / electricChargeMax < 0.5;
    }
  }
}