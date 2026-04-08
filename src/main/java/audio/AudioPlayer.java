package audio;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import model.Song;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 对 JavaFX 媒体播放的轻量封装，确保所有播放器操作都在 FX 线程执行。
 */
public class AudioPlayer {
    private static final double DEFAULT_VOLUME = 0.8;

    private MediaPlayer mediaPlayer;
    private PauseTransition stopTimer;
    private Song currentSong;
    private boolean isPaused;
    private double volume;
    private PlaybackListener listener;

    /**
     * 游戏引擎使用的播放回调接口。
     */
    public interface PlaybackListener {
        void onPlaybackComplete();
        void onPlaybackError(String error);
        void onPlaybackStarted(Song song);
    }

    /**
     * 使用默认音量创建播放器。
     */
    public AudioPlayer() {
        this.volume = DEFAULT_VOLUME;
        this.isPaused = false;
    }

    /**
     * 在校验文件存在且格式受支持后开始播放。
     */
    public void play(Song song) throws Exception {
        if (song == null || !song.fileExists()) {
            throw new IllegalArgumentException("Song file does not exist.");
        }
        if (!isPlayableFormat(song.getFileName())) {
            throw new IllegalArgumentException("Unsupported audio format. Use mp3, wav, m4a, aif, or aiff.");
        }

        currentSong = song;
        File audioFile = song.getFile();
        String mediaUrl = audioFile.toURI().toString();
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        runOnFxThreadAndWait(() -> {
            try {
                stopInternal();

                Media media = new Media(mediaUrl);
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setVolume(volume);
                mediaPlayer.setOnEndOfMedia(() -> {
                    isPaused = false;
                    if (listener != null) {
                        listener.onPlaybackComplete();
                    }
                });
                mediaPlayer.setOnError(() -> {
                    if (listener != null) {
                        String error = mediaPlayer.getError() != null
                            ? mediaPlayer.getError().getMessage()
                            : "Unknown playback error";
                        listener.onPlaybackError(error);
                    }
                });
                mediaPlayer.play();
                isPaused = false;
                if (listener != null) {
                    listener.onPlaybackStarted(song);
                }
            } catch (Exception e) {
                errorRef.set(e);
            }
        });

        if (errorRef.get() != null) {
            throw new RuntimeException("Unable to play file: " + audioFile.getAbsolutePath(), errorRef.get());
        }
    }

    /**
     * 播放音频，并在指定秒数后自动停止。
     */
    public void playLimited(Song song, int durationSeconds) throws Exception {
        play(song);
        runOnFxThreadAndWait(() -> {
            if (stopTimer != null) {
                stopTimer.stop();
            }
            stopTimer = new PauseTransition(Duration.seconds(durationSeconds));
            stopTimer.setOnFinished(event -> {
                if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    stop();
                }
            });
            stopTimer.playFromStart();
        });
    }

    /**
     * 当音频正在播放时暂停播放。
     */
    public void pause() {
        runOnFxThreadAndWait(() -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                isPaused = true;
            }
        });
    }

    /**
     * 从暂停状态恢复播放。
     */
    public void resume() {
        runOnFxThreadAndWait(() -> {
            if (mediaPlayer != null && isPaused) {
                mediaPlayer.play();
                isPaused = false;
            }
        });
    }

    /**
     * 停止播放并释放当前 JavaFX 播放器实例。
     */
    public void stop() {
        runOnFxThreadAndWait(this::stopInternal);
    }

    /**
     * 停止、重播和资源释放流程共用的清理逻辑。
     */
    private void stopInternal() {
        if (stopTimer != null) {
            stopTimer.stop();
            stopTimer = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        isPaused = false;
        currentSong = null;
    }

    /**
     * 更新缓存音量，并在存在活动播放器时同步应用。
     */
    public void setVolume(double volume) {
        if (volume < 0.0 || volume > 1.0) {
            throw new IllegalArgumentException("Volume must be between 0.0 and 1.0.");
        }
        this.volume = volume;
        runOnFxThreadAndWait(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume);
            }
        });
    }

    public double getVolume() {
        return volume;
    }

    /**
     * 当播放器存在时跳转到当前音频的指定位置。
     */
    public void seek(double seconds) {
        runOnFxThreadAndWait(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(seconds));
            }
        });
    }

    public double getCurrentTime() {
        return mediaPlayer != null ? mediaPlayer.getCurrentTime().toSeconds() : 0;
    }

    public double getTotalDuration() {
        return mediaPlayer != null && mediaPlayer.getTotalDuration() != null
            ? mediaPlayer.getTotalDuration().toSeconds()
            : 0;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPlaybackListener(PlaybackListener listener) {
        this.listener = listener;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    /**
     * 释放所有播放资源。
     */
    public void dispose() {
        stop();
    }

    /**
     * 将可播放格式限制为项目统一支持的音频格式。
     */
    private boolean isPlayableFormat(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            return false;
        }
        String extension = fileName.substring(lastDot + 1).toLowerCase();
        return extension.matches("mp3|wav|m4a|aif|aiff");
    }

    /**
     * 确保 JavaFX 媒体相关操作都在 FX 应用线程执行。
     */
    private void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("FX thread execution interrupted", e);
        }
    }
}
