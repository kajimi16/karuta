package game;

import config.ConfigManager;
import model.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 保存可配置的游戏规则，例如片段时长和失败处理方式，
 * 以及可选的休息音乐。
 */
public class GameRules {
    /**
     * 定义引擎如何处理失败回合。
     */
    public enum FailureMode {
        PASS,
        SKIP
    }

    private static final int DEFAULT_REST_INTERVAL = 5;
    private static final Random RANDOM = new Random();

    private final List<Song> restMusicPool = new ArrayList<>();

    private int minPlaybackDuration;
    private int maxPlaybackDuration;
    private int restIntervalRounds;
    private Song restMusic;
    private FailureMode failureMode;
    private boolean enableRestMusic;

    /**
     * 根据项目配置初始化规则集。
     */
    public GameRules(ConfigManager config) {
        this.minPlaybackDuration = config.getMinDuration();
        this.maxPlaybackDuration = config.getMaxDuration();
        this.restIntervalRounds = DEFAULT_REST_INTERVAL;
        this.enableRestMusic = true;

        File restMusicFile = config.getRestMusicFile();
        if (restMusicFile.exists()) {
            this.restMusic = new Song(restMusicFile.getName(), restMusicFile);
        }

        String mode = config.getFailureMode();
        this.failureMode = FailureMode.valueOf(mode.toUpperCase());
    }

    /**
     * 返回一个落在配置范围内的播放时长。
     */
    public int getPlaybackDuration() {
        if (minPlaybackDuration >= maxPlaybackDuration) {
            return minPlaybackDuration;
        }
        return minPlaybackDuration + (int) (Math.random() * (maxPlaybackDuration - minPlaybackDuration + 1));
    }

    public int getRestIntervalRounds() {
        return restIntervalRounds;
    }

    /**
     * 允许界面调整休息音乐的插入频率。
     */
    public void setRestIntervalRounds(int interval) {
        if (interval < 0) {
            throw new IllegalArgumentException("Rest interval cannot be negative.");
        }
        this.restIntervalRounds = interval;
    }

    /**
     * 优先从候选池中选择休息曲目，否则回退到默认曲目。
     */
    public Song getRestMusic() {
        if (!restMusicPool.isEmpty()) {
            return restMusicPool.get(RANDOM.nextInt(restMusicPool.size()));
        }
        return restMusic;
    }

    public void setRestMusic(Song music) {
        this.restMusic = music;
    }

    /**
     * 替换休息时可用的动态歌曲池。
     */
    public void setRestMusicPool(List<Song> songs) {
        restMusicPool.clear();
        if (songs != null) {
            restMusicPool.addAll(songs);
        }
    }

    public FailureMode getFailureMode() {
        return failureMode;
    }

    public void setFailureMode(FailureMode mode) {
        this.failureMode = mode;
    }

    /**
     * 判断休息音乐是否已启用且资源可用。
     */
    public boolean isRestMusicEnabled() {
        return enableRestMusic && (restMusic != null || !restMusicPool.isEmpty());
    }

    public void setRestMusicEnabled(boolean enabled) {
        this.enableRestMusic = enabled;
    }

    /**
     * 校验用户提供的播放时长是否在配置范围内。
     */
    public boolean isValidDuration(int seconds) {
        return seconds >= minPlaybackDuration && seconds <= maxPlaybackDuration;
    }

    @Override
    public String toString() {
        return String.format(
            "GameRules{duration=%d-%ds, restInterval=%d, failureMode=%s}",
            minPlaybackDuration,
            maxPlaybackDuration,
            restIntervalRounds,
            failureMode
        );
    }
}
