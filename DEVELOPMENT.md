# chy 开发与维护

面向 Minecraft Java 1.21.11 + Fabric，使用 Java 21。玩家安装和命令说明见 [README](README.md)。

## 工程与本地数据

- 当前工程目录为 `autoclick-template-1.21.11/`；模组名称、ID 和构建产物均为 `chy`。
- `backup/` 保存旧代码，`built/` 保存历史 JAR，均不参与当前构建；`built/` 不会随 build 自动更新。
- 开发客户端使用 `run-1.21.11/`，旧 `run/` 存档保留在本地。
- `.gitignore` 排除了 `run/`、`run-*/`、`build/` 和 `.gradle/`。旧 `run/` 已取消 Git 跟踪，后续普通提交不会包含测试存档和日志；已有 Git 历史中的文件仍然保留。

## Mac 开发、测试和编译

以下命令从仓库根目录开始执行。

### 1. 打开终端，进入工程

```bash
cd autoclick-template-1.21.11
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
chmod +x gradlew
```

使用 Java 21 和项目自带的 Gradle Wrapper。第一次构建需要联网下载依赖。

### 2. 自动化测试并编译 JAR

```bash
./gradlew build
```

看到 `BUILD SUCCESSFUL` 表示编译、测试和打包成功。回归测试覆盖默认 12 tick、停止后重开恢复默认、调整间隔重置计时、整数 tick 边界、非法参数不改变状态、左右键独立计时和退出重置。单独运行测试：

```bash
./gradlew test
open build/reports/tests/test/index.html
```

可安装的成品：

```text
build/libs/chy-3.2.0+mc1.21.11.jar
```

**不要安装 `-sources.jar` 或 `build/devlibs` 里的开发 JAR。** 后续改代码后执行 `./gradlew build`；需要完全重新生成产物时执行 `./gradlew clean build`。`built/` 仅为手动保存的历史副本，不会随 build 更新。

```bash
open build/libs
```

### 3. 启动开发客户端并验证游戏行为

```bash
./gradlew runClient
```

这会启动加载本模组和 Fabric API 的 Minecraft 1.21.11 开发客户端，使用独立的 `run-1.21.11/` 目录。开发客户端不应当作已登录的正式启动器客户端使用。自动化状态测试不能替代以下实际游戏验证：

1. 输入 `/chy_help`，确认包含左右键、tick 参数、持续潜行、开关方式及退出重置说明。
2. 准星对着实体输入 `/left_click`，确认显示 `自动左键，间隔12 tick` 并周期攻击；准星移开后只挥手、不挖方块。再次输入关闭。
3. 输入 `/left_click 40`，再无参数关闭、重开，应恢复 12 tick。`/right_click` 也执行同样检查。
4. 手持方块对着可放置的位置输入 `/right_click`，确认显示 `自动右键，间隔12 tick` 并周期放置；输入 `/right_click 4`，确认频率改变。移动准星继续放置，检查副手方块和方块耗尽时的行为。
5. 分别对两个命令测试参数 `1`、`20`，并测试 `0`、`-1`、`0.6`、`abc`、`2147483648`，非法参数应报错且不改变原设置。
6. 左右键同时开启、分别调整间隔，再关闭其中一个，另一个应继续。
7. 输入 `/shift` 后检查潜行移动、平台边缘防掉落、在箱子上放置方块；打开聊天/背包后应保持潜行。再次输入关闭，正常走动和手动 Shift 应恢复。也检查游戏设置中的“切换潜行”模式。
8. 在多人服务器或未暂停的世界中，打开聊天、背包、Esc 菜单后，左右键仍应按间隔执行。单人世界暂停时不执行，恢复后继续。
9. 三项功能开启后退出世界，重新进入应全部关闭；检查死亡重生后输入正常，旁观模式不自动点击。
10. `/auto_attack`、`/chy_autoattack`、`/chy_deepseek`、`/chy_say` 不再由本模组提供；日志中的模组标识为 `chy`。

日志位于 `run-1.21.11/logs/latest.log`；通过正式启动器运行时位于对应游戏目录的 `logs/latest.log`。

## 模组图标与提示颜色

- 图标文件：`autoclick-template-1.21.11/src/main/resources/assets/chy/icon.png`，由用户提供的 `chy_mod.png` 移入。
- `src/main/resources/fabric.mod.json` 中的 `icon` 字段为 `assets/chy/icon.png`，路径相对于资源根目录。
- 更新图标时替换该 PNG 并重新执行 `./gradlew build`，新图标会打包进 JAR。
- 聊天栏提示和帮助统一由 `ChyModClient.sendFeedback` 设置为 `Formatting.RED`；日志保留普通文本。

## 手动安装到 macOS 默认游戏目录

先关闭游戏，确认启动配置使用默认目录，并移走旧 `autoclick` 和旧 `chy` JAR。从工程目录执行：

```bash
mkdir -p "$HOME/Library/Application Support/minecraft/mods"
open "$HOME/Library/Application Support/minecraft/mods"
cp build/libs/chy-3.2.0+mc1.21.11.jar "$HOME/Library/Application Support/minecraft/mods/"
```

自定义游戏目录请调整目标路径。Fabric API 仍需单独安装。
