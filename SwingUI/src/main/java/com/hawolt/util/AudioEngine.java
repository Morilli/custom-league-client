package com.hawolt.util;

import com.hawolt.LeagueClientUI;
import com.hawolt.io.RunLevel;
import com.hawolt.logger.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;

/**
 * Created: 09/08/2023 11:37
 * Author: Twitter @hawolt
 **/

public class AudioEngine {

    public static void play(String filename) {
        LeagueClientUI.service.execute(() -> {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(RunLevel.get("audio/" + filename)));
                Clip clip = AudioSystem.getClip();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception e) {
                Logger.info("Audio files are not distributed via GitHub, failed to play {}", filename);
            }
        });
    }
}
