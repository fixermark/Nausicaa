/** @file Speech output pipeline
 */

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