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

/**
 * Halt time warp on any output
 */
public class TimeWarpHaltOutput implements OutputInterface {
  private boolean enabled;  // If false, time warp not sent.
  private OutputInterface telemachus;  // Telemachus output channel.

  public TimeWarpHaltOutput(OutputInterface t) {
    telemachus = t;
  }

  @Override
    public void output(String msg) {
    if (enabled && (telemachus != null)) {
      telemachus.output("{\"run\":[\"t.timeWarp[0]\"]}");
    }
  }

  public boolean getEnabled() {
    return enabled;
  }
  public void setEnabled(boolean e) {
    enabled = e;
  }
}