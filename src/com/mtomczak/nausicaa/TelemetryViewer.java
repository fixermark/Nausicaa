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