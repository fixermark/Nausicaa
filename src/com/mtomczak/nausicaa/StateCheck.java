/** @file Checks the state of some aspect of the telemetry.
 */

package com.mtomczak.nausicaa;

import org.json.JSONObject;
import org.json.JSONException;

public interface StateCheck {
  public boolean check(JSONObject telemetry) throws JSONException;
}