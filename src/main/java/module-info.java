/**
 * 歌牌点歌应用的模块声明。
 */
module karuta.jukebox {
    requires transitive javafx.controls;
    requires javafx.media;

    exports audio;
    exports config;
    exports game;
    exports model;
    exports ui;
}
