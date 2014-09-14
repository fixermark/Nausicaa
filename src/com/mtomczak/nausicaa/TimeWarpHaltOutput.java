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