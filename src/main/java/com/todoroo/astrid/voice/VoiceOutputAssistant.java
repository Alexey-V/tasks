/**
 *
 */
package com.todoroo.astrid.voice;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tasks.injection.ForApplication;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

/**
 * @author Arne Jans
 *
 */
public class VoiceOutputAssistant implements OnInitListener {

    private static final Logger log = LoggerFactory.getLogger(VoiceOutputAssistant.class);

    private final Context context;

    private TextToSpeech mTts;
    private boolean isTTSInitialized;
    private String lastTextToSpeak;

    @Inject
    public VoiceOutputAssistant(@ForApplication Context context) {
        this.context = context;
    }

    public boolean isTTSInitialized() {
        return isTTSInitialized;
    }

    public void initTTS() {
        if(mTts == null) {
            mTts = new TextToSpeech(context, this);
            log.debug("Inititalized {}", mTts);
        }
    }

    public void speak(String textToSpeak) {
        if (mTts != null && isTTSInitialized) {
            final String id = UUID.randomUUID().toString();
            log.debug("{}: {} ({})", mTts, textToSpeak, id);
            mTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    log.debug("{}: onUtteranceCompleted {}", utteranceId);
                    if(utteranceId.equals(id)) {
                        shutdown();
                    }
                }
            });
            mTts.speak(textToSpeak, TextToSpeech.QUEUE_ADD, new HashMap<String, String>() {{
                put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
                put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
            }});
        } else {
            lastTextToSpeak = textToSpeak;
            initTTS();
        }
    }

    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS && mTts != null) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(Locale.getDefault());
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA) {
                log.error("Language data missing");
            } else if(result == TextToSpeech.LANG_NOT_SUPPORTED) {
                log.error("Language not supported");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.

                mTts.speak("", 0, null);

                // The TTS engine has been successfully initialized.
                isTTSInitialized = true;
                // if this request came from speak, then speak it and reset the memento
                if (lastTextToSpeak != null) {
                    speak(lastTextToSpeak);
                    lastTextToSpeak = null;
                }
            }
        } else {
            log.error("Could not initialize TextToSpeech.");
        }
    }

    public void shutdown() {
        if (mTts != null && isTTSInitialized) {
            try {
                mTts.shutdown();
                log.debug("Shutdown {}", mTts);
                mTts = null;
                isTTSInitialized = false;
            } catch(VerifyError e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
