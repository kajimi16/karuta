# Karuta Jukebox

## 中文说明

Karuta Jukebox 是一个基于 JavaFX 的桌面应用，用于运行歌牌式音乐猜题流程。它支持从 CSV 卡组加载卡牌与歌曲，预览封面图，在每回合随机播放歌曲片段，并在游戏过程中提供独立的后台管理面板。

### 功能概览

- 从 `config/decks/*.csv` 读取卡组定义
- 为每张卡牌关联一张图片和一首或多首音频
- 开局前预览卡组内容
- 按回合随机抽卡并随机选歌播放
- 每回合可标记为 `Success` 或 `Failure`
- 支持回合间播放中场休息音乐
- 在独立后台面板中查看和管理激活/失活卡牌

### 技术栈

- Java 25
- Maven
- JavaFX `controls`、`fxml`、`media`
- Apache Commons Lang

项目构建配置见 [pom.xml](/F:/karuta/karuta/pom.xml)。

### 项目结构

```text
karuta/
|-- src/
|   `-- main/
|       |-- java/
|       |   |-- audio/
|       |   |-- config/
|       |   |-- game/
|       |   |-- model/
|       |   |-- ui/
|       |   `-- module-info.java
|       `-- resources/
|           `-- config/
|               |-- config.txt
|               `-- decks/
|                   `-- deck1.csv
|-- docs/
|-- pom.xml
`-- README.md
```

### 主要界面

- 卡组选择界面
  - 展示可用的 CSV 卡组
  - 预览所选卡组的图片和歌曲
  - 设置总回合数并开始游戏
- 游戏界面
  - 显示当前卡面、歌曲标题、播放进度和回合统计
  - 支持 `Success`、`Failure`、`Continue` 和返回菜单
- 后台管理面板
  - 实时显示激活与失活卡牌
  - 支持手动移动卡牌状态
  - 可将当前游戏状态导出到面板文本区

JavaFX 启动入口是 [src/main/java/ui/MainWindow.java](/F:/karuta/karuta/src/main/java/ui/MainWindow.java)。

### 环境要求

- JDK 25
- Maven 3.9+
- 已按配置路径准备好音频与图片资源

如果在新机器上运行或打包，请确保 Maven 可以正常解析 JavaFX 原生依赖。

### 本地运行

```bash
mvn clean javafx:run
```

### 构建打包

```bash
mvn clean package
```

当前构建使用 JavaFX Maven Plugin 和 Maven Shade Plugin，主类配置为 `ui.MainWindow`。

### 配置说明

应用通过 `ConfigManager` 加载配置。启动时会按以下顺序查找 `config` 目录：

1. 工作目录下直接存在的 `config/`
2. `src/main/resources/config`
3. 打包后的 classpath 资源

默认配置文件见 [src/main/resources/config/config.txt](/F:/karuta/karuta/src/main/resources/config/config.txt)。

#### `config.txt`

```properties
music_folder=config/music/
images_folder=config/images/
rest_music=rest.mp3
min_duration=10
max_duration=30
default_deck=config/decks/deck1.csv
failure_mode=PASS
enable_rest_music=true
auto_continue_after_rest=false
```

#### 配置项说明

- `music_folder`：可播放音频文件所在目录
- `images_folder`：卡牌图片所在目录
- `rest_music`：中场休息音乐文件名，路径相对于 `music_folder`
- `default_deck`：默认卡组 CSV 路径
- `failure_mode`：当前支持 `PASS` 和 `SKIP`
- 播放时长会在 `min_duration` 与 `max_duration` 之间随机选取

### 卡组格式

示例卡组见 [src/main/resources/config/decks/deck1.csv](/F:/karuta/karuta/src/main/resources/config/decks/deck1.csv)。

```csv
image_name,work_name,songs,song_display_names
card_example1.png,Example Work A,song_a1.mp3|song_a2.mp3,Example Work A - Music 1|Example Work A - Music 2
card_example2.png,Example Work B,song_b1.mp3,Example Work B - Music 1
```

#### 字段说明

- `image_name`：位于 `images_folder` 下的图片文件名
- `work_name`：界面中展示的卡牌标题
- `songs`：位于 `music_folder` 下的音频文件名，多个值使用 `|` 分隔
- `song_display_names`：可选的歌曲显示名，多个值使用 `|` 分隔

如果图片或音频文件缺失，加载器会记录问题，并跳过不可用内容。

### 支持的音频格式

`ConfigManager` 当前支持：

- `mp3`
- `wav`
- `flac`
- `ogg`
- `aac`

### 游戏流程

1. 启动应用。
2. 选择卡组并设置回合数。
3. 系统从当前激活卡池中随机抽取一张卡。
4. 从该卡关联的歌曲中随机播放一个片段。
5. 播放结束后，操作员标记本回合为 `Success` 或 `Failure`。
6. `Success` 会将该卡从激活池移除。
7. `Failure` 的处理方式取决于 `failure_mode`。
8. 达到休息间隔后，系统可能播放中场音乐，再继续下一回合。

### 当前限制

- 仓库中只提供了配置示例，没有包含可直接游玩的图片和音频资源。
- 源码里仍有部分注释和字符串存在乱码问题，这不影响 README，但建议后续单独清理。
- 中场休息间隔目前写死在代码里，没有暴露到 `config.txt`。

### 相关文档

- [QUICKSTART.md](/F:/karuta/karuta/QUICKSTART.md)
- [GET_STARTED.md](/F:/karuta/karuta/GET_STARTED.md)
- [USER_MANUAL.md](/F:/karuta/karuta/USER_MANUAL.md)
- [COMPLETE_GUIDE.md](/F:/karuta/karuta/COMPLETE_GUIDE.md)
- [PROJECT_SUMMARY.md](/F:/karuta/karuta/PROJECT_SUMMARY.md)

## English

Karuta Jukebox is a JavaFX desktop app for running a karuta-style music guessing session. It loads decks from CSV files, previews card artwork, plays a random song clip for each card, and keeps an operator-facing admin panel open during the game.

### Features

- Load deck definitions from `config/decks/*.csv`
- Map each card to one artwork image and one or more audio files
- Preview decks before starting a session
- Start a multi-round game with random card and song selection
- Mark each round as `Success` or `Failure`
- Play rest music between rounds
- Track active and inactive cards in a separate admin panel

### Tech Stack

- Java 25
- Maven
- JavaFX `controls`, `fxml`, and `media`
- Apache Commons Lang

Project metadata lives in [pom.xml](/F:/karuta/karuta/pom.xml).

### Project Layout

```text
karuta/
|-- src/
|   `-- main/
|       |-- java/
|       |   |-- audio/
|       |   |-- config/
|       |   |-- game/
|       |   |-- model/
|       |   |-- ui/
|       |   `-- module-info.java
|       `-- resources/
|           `-- config/
|               |-- config.txt
|               `-- decks/
|                   `-- deck1.csv
|-- docs/
|-- pom.xml
`-- README.md
```

### Main Screens

- Deck selection screen
  - Lists available CSV decks
  - Shows artwork and song preview for the selected deck
  - Lets you choose total rounds before starting
- Game screen
  - Shows current card art, song title, progress, and round stats
  - Supports `Success`, `Failure`, `Continue`, and returning to menu
- Admin panel
  - Shows active and inactive cards in real time
  - Lets the operator move cards between states
  - Exports current game state to the panel text area

The JavaFX entry point is [src/main/java/ui/MainWindow.java](/F:/karuta/karuta/src/main/java/ui/MainWindow.java).

### Requirements

- JDK 25
- Maven 3.9+
- Audio files and artwork files placed under the configured folders

If you plan to package or run JavaFX on a new machine, make sure JavaFX native dependencies can be resolved by Maven.

### Run Locally

```bash
mvn clean javafx:run
```

### Build

```bash
mvn clean package
```

The Maven build uses the JavaFX plugin and the Shade plugin. The configured main class is `ui.MainWindow`.

### Configuration

The app loads its configuration through `ConfigManager`. At startup it looks for a `config` directory in this order:

1. A direct `config/` folder in the working directory
2. `src/main/resources/config`
3. A bundled classpath resource

The default config file is [src/main/resources/config/config.txt](/F:/karuta/karuta/src/main/resources/config/config.txt).

#### `config.txt`

```properties
music_folder=config/music/
images_folder=config/images/
rest_music=rest.mp3
min_duration=10
max_duration=30
default_deck=config/decks/deck1.csv
failure_mode=PASS
enable_rest_music=true
auto_continue_after_rest=false
```

#### Config Notes

- `music_folder` points to the folder that contains playable audio files
- `images_folder` points to the folder that contains card artwork
- `rest_music` is resolved under `music_folder`
- `default_deck` should point to a CSV file
- `failure_mode` currently supports `PASS` and `SKIP`
- Playback duration is randomly chosen between `min_duration` and `max_duration`

### Deck Format

The sample deck is [src/main/resources/config/decks/deck1.csv](/F:/karuta/karuta/src/main/resources/config/decks/deck1.csv).

```csv
image_name,work_name,songs,song_display_names
card_example1.png,Example Work A,song_a1.mp3|song_a2.mp3,Example Work A - Music 1|Example Work A - Music 2
card_example2.png,Example Work B,song_b1.mp3,Example Work B - Music 1
```

#### Deck Rules

- `image_name`: image file under `images_folder`
- `work_name`: label shown in the UI for the card
- `songs`: one or more filenames under `music_folder`, separated by `|`
- `song_display_names`: optional display names, also separated by `|`

If an image or audio file is missing, the loader logs the issue and skips unusable content.

### Supported Audio Formats

`ConfigManager` currently accepts:

- `mp3`
- `wav`
- `flac`
- `ogg`
- `aac`

### Game Flow

1. Start the app.
2. Select a deck and choose the number of rounds.
3. The game picks a random active card.
4. A random song clip for that card is played.
5. After playback completes, mark the round as `Success` or `Failure`.
6. Success removes the card from the active pool.
7. Failure handling depends on `failure_mode`.
8. After the configured rest interval, rest music can play before the next round.

### Current Limitations

- The repository only includes config examples, not the actual music and image assets needed for a playable session.
- Some source files still contain mojibake in comments and string literals; this does not block the README but should be cleaned separately.
- The rest interval is currently defined in code rather than exposed in `config.txt`.

### Related Docs

- [QUICKSTART.md](/F:/karuta/karuta/QUICKSTART.md)
- [GET_STARTED.md](/F:/karuta/karuta/GET_STARTED.md)
- [USER_MANUAL.md](/F:/karuta/karuta/USER_MANUAL.md)
- [COMPLETE_GUIDE.md](/F:/karuta/karuta/COMPLETE_GUIDE.md)
- [PROJECT_SUMMARY.md](/F:/karuta/karuta/PROJECT_SUMMARY.md)

## License

MIT
