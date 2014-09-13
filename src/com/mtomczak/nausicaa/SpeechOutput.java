/** @file Speech output pipeline
 */
package com.mtomczak.nausicaa;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class SpeechOutput implements TextToSpeech.OnInitListener, OutputInterface {
  private TextToSpeech tts = null;

  public SpeechOutput(Context ctx) {
    tts = new TextToSpeech(ctx, this);
  }

  public void stop() {
    tts.stop();
    tts.shutdown();
    tts = null;
  }

  // init for text to speech engine
  @Override
    public void onInit(int status) {
    if (status == TextToSpeech.SUCCESS) {
      int result = tts.setLanguage(Locale.US);
    }
  }

  @Override
    public void output(String msg) {
    if (tts != null) {
      tts.speak(msg, TextToSpeech.QUEUE_ADD, null);
    }
  }
}