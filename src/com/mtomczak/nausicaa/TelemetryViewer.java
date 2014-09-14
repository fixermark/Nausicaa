package com.mtomczak.nausicaa;

import org.json.JSONObject;

/**
 * Object capable of consuming telemetry data.
 */
public interface TelemetryViewer {
  /**
   * Updates the view with new telemetry. Must be run on a UI thread.
   * @param telemetry Telemetry to evaluate.
   * @param alerts View to use to report failure to read telemetry.
   */
  public void update(JSONObject telemetry);
}