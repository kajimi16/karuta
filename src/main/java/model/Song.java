package model;

import java.io.File;

/**
 * 表示卡牌关联音轨的不可变元数据。
 */
public class Song {
    private final String fileName;
    private final String displayName;
    private final String fileFormat;
    private long duration;
    private final File file;

    /**
     * 创建歌曲对象，并从文件名推导显示名称。
     */
    public Song(String fileName, File file) {
        this(fileName, file, extractDisplayName(fileName));
    }

    /**
     * 使用显式显示名称创建歌曲对象。
     */
    public Song(String fileName, File file, String displayName) {
        this.fileName = fileName;
        this.file = file;
        this.displayName = (displayName == null || displayName.isBlank())
            ? extractDisplayName(fileName)
            : displayName;
        this.fileFormat = getFileFormat(fileName);
        this.duration = 0;
    }

    /**
     * 生成界面显示标题时移除文件扩展名。
     */
    private static String extractDisplayName(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    /**
     * 返回用于格式显示的小写文件扩展名。
     */
    private String getFileFormat(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "unknown";
    }

    /**
     * 判断关联的音频文件当前是否可用。
     */
    public boolean fileExists() {
        return file != null && file.exists();
    }

    /**
     * 提供绝对路径，便于诊断和媒体加载。
     */
    public String getAbsolutePath() {
        return file != null ? file.getAbsolutePath() : null;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public long getDuration() {
        return duration;
    }

    /**
     * 保存检测到或外部传入的毫秒级时长。
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return displayName + " (" + fileFormat.toUpperCase() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Song other)) {
            return false;
        }
        return this.fileName.equals(other.fileName);
    }

    @Override
    public int hashCode() {
        return fileName.hashCode();
    }
}
