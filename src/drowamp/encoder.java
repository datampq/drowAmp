/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drowamp;

import java.awt.Dimension;
import java.io.File;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javax.swing.JPanel;

/**
 * http://drow.today
 *
 * @author datampq
 */
public class encoder extends JPanel {

    private int numBands = 16;
    private int sW;
    private int sH;
    private JFXPanel jfxPanel;
    private final main main;
    MediaView mediaView;
    MediaPlayer current;
    public boolean inited = false;
    private final SpektrumListener spektara;

    public encoder(main m, int w, int h) {
        spektara = new SpektrumListener();
        correctedMagnitude = new float[numBands];
        for (int i = 0; i < correctedMagnitude.length; i++) {
            correctedMagnitude[i] = 0f;
        }
        main = m;
        sW = w;
        sH = h;
        this.setBackground(m.bg);
        this.setPreferredSize(new Dimension(sW, sH));

    }

    private void initFX(JFXPanel panel, String url) {
        File file = new File(url);
        String MEDIA_URL = file.toURI().toString();

        Media media = new Media(MEDIA_URL);
        current = new MediaPlayer(media);
        current.seek(Duration.ZERO);
        current.setOnError(() -> System.out.println("Error : " + current.getError().toString()));
        current.setVolume(main.volume);
        current.setOnEndOfMedia(() -> {
            Play(main.man.getFile());

        });
        current.currentTimeProperty().addListener((Observable ov) -> {
            updateValues();
        });

        mediaView = new MediaView(current);

        mediaView.setPreserveRatio(false);
        Group g = new Group(mediaView);
        Scene s = new Scene(g);

        panel.setScene(s);
        current.play();
        inited = true;
        mediaView.setFitWidth(sW);
        mediaView.setFitHeight(sH);
        current.setAudioSpectrumInterval(0.04);
        current.setAudioSpectrumListener(spektara);
        // player.setAudioSpectrumInterval(0.1);
        current.setAudioSpectrumThreshold(-256);
        current.setAudioSpectrumNumBands(numBands);

    }

    public void stop() {
        current.seek(Duration.ZERO);
        current.pause();
    }

    public void resume() {
        current.play();
    }
    public int numTries = 0;

    public void Play(String url) {
        if (inited) {
            try {
                File file = new File(url);
                String MEDIA_URL = file.toURI().toString();
                Media media = null;
                try {
                    media = new Media(MEDIA_URL);
                } catch (MediaException me) {
                    main.man.getNext();
                }
                current.stop();
                current.seek(Duration.ZERO);
                current = new MediaPlayer(media);
                current.setOnError(() -> Play(main.man.getFile()));
                current.setOnEndOfMedia(() -> {
                    Play(main.man.getFile());
                });

                current.setVolume(main.volume);
                current.play();
                current.currentTimeProperty().addListener((Observable ov) -> {
                    updateValues();
                });
                mediaView.setMediaPlayer(current);
                mediaView.setPreserveRatio(false);
                mediaView.setFitWidth(sW);
                mediaView.setFitHeight(sH);
              
                current.setAudioSpectrumInterval(0.1);
                current.setAudioSpectrumListener(spektara);
                // player.setAudioSpectrumInterval(0.1);
                current.setAudioSpectrumThreshold(-256);
                current.setAudioSpectrumNumBands(numBands);

            } catch (Exception e) {
                System.out.println("Error @end of block: " + e.getMessage());

            }
        } else {
            jfxPanel = new JFXPanel();
            jfxPanel.setPreferredSize(new Dimension(sW, sH));
            this.add(jfxPanel);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    initFX(jfxPanel, url);
                    revalidate();
                }
            });
        }
    }

    protected void updateValues() {
        Platform.runLater(() -> {
            Duration currentTime = current.getCurrentTime();
            Duration duration = current.getTotalDuration();
            main.setTime(formatTime(currentTime, duration));
            current.setVolume(main.volume);
            double name = (currentTime.toMillis() / duration.toMillis()) * 1260;
            main.updateSeek(name);
        });
    }
    public float[] correctedMagnitude;
    public float[] buffer;

    public class SpektrumListener implements AudioSpectrumListener {

        @Override
        public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
            //  System.out.println(magnitudes.length);
            // correctedMagnitude[0] = magnitudes[0] - mediaPlayer.getSpektrumThreshold();
            for (int i = 0; i < magnitudes.length; i++) {
                correctedMagnitude[i] = magnitudes[i];
              
            }
        }
    }

    public void setSeek(double percent) {
        Duration totalDuration = current.getTotalDuration();
        double toMillis = totalDuration.toMillis() * percent;
        Duration multiply = new Duration(toMillis);
        current.seek(multiply);
    }

    private String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60
                    - durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d / %d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d / %02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }

}
