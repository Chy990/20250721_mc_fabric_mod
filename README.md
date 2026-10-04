# 我的世界 Fabric 自动攻击器

当前版本：**v3.0.1，适用于 Minecraft Java 1.21.11 + Fabric，Java 21**。

> 当前工程目录：`autoclick-template-1.21.11/`，代码、依赖和开发客户端均面向 **1.21.11**。
> **每次 build 后，最新可安装 JAR 都在 `autoclick-template-1.21.11/build/libs/autoclick-3.0.1+mc1.21.11.jar`。**
> `backup/` 中的旧 Java 文件、`built/` 中的历史 JAR 和工程内旧 `run/` 存档均不参与当前构建；旧存档保留原样。当前测试使用 `run-1.21.11/`。

## 20261004 更新已实现

- 移除当前模组中的 DeepSeek 聊天、HTTP 请求、API Key、JSON 依赖及 `/chy_deepseek`、`/chy_say` 命令。
- 原 `/chy_autoattack` 改为 **`/auto_attack`**；保留 `/chy_help`，更新帮助内容。
- 无参数命令用于开关：每次从关闭状态开启，均恢复 **12 tick（0.6 秒）**，不沿用上次自定义值。
- 每次开启或调整间隔，在游戏聊天栏和客户端日志输出：`自动攻击已开启。自动攻击间隔（12 tick, 0.6 秒）`。
- 按客户端 tick 计时；打开聊天、背包或 Esc 菜单时继续执行自动攻击逻辑，退出世界时关闭。攻击准星指向的实体，未指向实体时只挥手，不自动挖方块。

构建和计时逻辑由自动化测试验证；打开界面时的实际打怪行为请按下方清单验证。根目录 `built/` 是手动保存的历史副本，**不会随 build 自动更新**；请始终使用当前工程 `build/libs/` 中的新文件。

## 游戏内命令

| 命令 | 效果 |
| --- | --- |
| `/auto_attack` | 开启默认 12 tick 自动攻击；已开启时则关闭 |
| `/auto_attack 1` | 开启或调整为 20 tick（1 秒） |
| `/auto_attack 0.6` | 开启或调整为 12 tick（0.6 秒） |
| `/chy_help` | 查看帮助 |

参数仍以**秒**为单位，允许 0.01–10。按每秒 20 tick 换算，向上取整到完整 tick，最小 1 tick。例如 `0.01` 实际为 1 tick（0.05 秒），`0.06` 实际为 2 tick（0.1 秒），反馈显示实际间隔。秒数是正常 20 TPS 下的换算；游戏卡顿时实际墙钟时间可能更长。

## Mac 开发、测试和编译

### 1. 打开终端，进入工程

```bash
cd /Users/billchen/Desktop/Developer_MAC/20250721_mc_fabric_mod/autoclick-template-1.21.11
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
chmod +x gradlew
```

本机已检测到 ARM64 Java 21 JDK，足够开发和编译。上面两条 `export` 对当前终端窗口生效，每次新开终端可重新执行。使用项目自带的 Gradle Wrapper，不必安装全局 Gradle、Maven 或 IDE。第一次构建需要联网下载依赖，耗时会较长。

### 2. 自动化测试并编译 JAR

```bash
./gradlew build
```

看到 `BUILD SUCCESSFUL` 表示编译、测试和打包成功。回归测试覆盖默认 12 tick、停止后重开恢复默认、调整间隔重置计时、取整和非法参数。单独运行测试：

```bash
./gradlew test
open build/reports/tests/test/index.html
```

可安装的成品在当前工程目录下（修改 `fabric.mod.json` 后重新 build，介绍和作者也会打包到这个文件）：

```text
build/libs/autoclick-3.0.1+mc1.21.11.jar
```

**不要安装 `-sources.jar`，也不要拿 `build/devlibs` 里的开发 JAR。** 后续修改代码后重新执行 `./gradlew build` 即可；需要完全重新生成构建产物时执行 `./gradlew clean build`。

```bash
open build/libs
```

### 3. 直接启动开发游戏测试

```bash
./gradlew runClient
```

Gradle 会启动已加载本模组及 Fabric API 的 Minecraft 1.21.11 开发客户端；不需要手动复制 JAR。它使用工程内独立的 `run-1.21.11/` 目录，和你平常启动器的游戏、旧 `run/` 存档分开。开发客户端适合单人世界测试，不应当作已登录的正式启动器客户端使用。

新建一个临时单人创造世界，拿剑、用刷怪蛋生成目标，然后依次检查：

1. 输入 `/chy_help`：帮助中只应有自动攻击相关说明。
2. 输入 `/auto_attack`：提示 `12 tick, 0.6 秒`，准星指着目标时开始攻击。
3. 再输入 `/auto_attack`：提示关闭，停止攻击。
4. 输入 `/auto_attack 2`：提示 `40 tick, 2.0 秒`；再输入两次 `/auto_attack`（先关、再开），必须恢复 `12 tick, 0.6 秒`。
5. 输入 `/auto_attack 0.06`：提示 `2 tick, 0.1 秒`；输入 `0`、负数、`11` 或文字时应报参数错误且不改变现有设置。
6. 在多人服务器或未暂停的世界中，准星对着目标开启自动攻击，再打开聊天、背包或 Esc 菜单，应继续攻击，关闭界面后也应继续。退出世界再进入，应保持关闭。
7. `/chy_deepseek`、`/chy_say`、`/chy_autoattack` 不再由本模组提供。

注意：单人游戏按 Esc 时，Minecraft 本身可能暂停整个世界；模组不再因菜单主动暂停或关闭，但无法让原版已暂停的世界继续处理伤害。多人服务器的 Esc 菜单不会暂停服务器。

从终端启动时，开启信息会显示在终端；也可查看 `run-1.21.11/logs/latest.log`。通过正式启动器运行时，相同信息在游戏目录的 `logs/latest.log` 中。

### 4. 安装到你平常玩的 Minecraft 1.21.11

只有原版 1.21.11 还不能加载 Fabric 模组。关闭游戏后：

1. 从 [Fabric 官网](https://fabricmc.net/use/installer/) 下载通用 `.jar` 安装器。在终端使用 `java -jar` 加安装器完整路径运行它（可以把下载的文件拖进终端填入路径）。选择 **Client、Minecraft 1.21.11、Loader 0.19.5 或更高兼容版本**，安装启动配置。
2. 从 [Fabric API 下载页](https://modrinth.com/mod/fabric-api/versions?g=1.21.11) 下载 **Minecraft 1.21.11** 对应的 Fabric API。本项目编译使用 `0.141.6+1.21.11`。
3. 在启动器里选中 Fabric 的 1.21.11 配置。先确认该配置的“游戏目录”；默认 macOS 路径是 `~/Library/Application Support/minecraft`。如果你设过自定义目录，后续使用自定义目录。
4. 将 **Fabric API JAR** 和 **`autoclick-3.0.1+mc1.21.11.jar`** 放进该游戏目录的 `mods` 文件夹。删除或移走同一个 `autoclick` 模组的旧版本，避免重复加载。无需解压 JAR。
5. 使用这个 Fabric 配置启动，再在临时世界按上面的清单测试。

默认目录可这样打开；仅在确认使用默认游戏目录后执行复制命令：

```bash
mkdir -p "$HOME/Library/Application Support/minecraft/mods"
open "$HOME/Library/Application Support/minecraft/mods"
cp build/libs/autoclick-3.0.1+mc1.21.11.jar "$HOME/Library/Application Support/minecraft/mods/"
```

本模组只需安装在客户端。若命令不存在，检查是否启动了 Fabric 配置、两份 JAR 是否放在正确游戏目录，以及是否用错 Minecraft 版本。

参考：[Fabric 1.21.11 迁移说明](https://fabricmc.net/2025/12/05/12111.html)、[macOS 安装 Fabric](https://docs.fabricmc.net/players/installing-fabric/macos)。

# 更新记录
### v3.0.1
移除打开聊天、背包和 Esc 菜单时主动暂停自动攻击的限制，恢复后台挂机用法。

### v1.0.0
/autoclick可以使用

### v1.0.1
**指令更新：** /chy_autoattack

### v1.1.0
支持自定义攻击间隔，并且将原本的默认攻击间隔1s改为0.6s用于适配java版的剑攻速恢复
**/chy_autoattack** 每0.6s攻击一次
**/chy_autoattack [interval]** 每[interval]秒攻击一次

### v2.0.0
新增**DeepSeek**聊天功能
**/chy_deepseek** 即可开始与 **DeepSeek-V3** 模型对话，关闭则再次输入一遍

### v2.1.0
新增**帮助**功能
**/chy_help**可在游戏内查看mod使用说明

### v2.2.0
新增命令
**/chy_say [message]** 新的调用deepseek聊天的方法，使用前需要确认输入过 **/chy_deepseek** 开启

### 20261004计划更新：
1. 对v2.0以上的DeepSeek聊天功能进行移除。
2. 将原本的chy_autoclick指令触发改为auto_attack，并且保持默认为12tick。
3. 修复一个小bug：以前在autoclick的时候后面跟上一个不是0.6s(12tick)的值时，停止以后再打开都默认是之前输入的那个值了。这不对。每次打开这个功能的时候都默认12tick。
4. 每次打开auto_attck的时候，在终端显示：自动攻击间隔（xx tick, xx 秒）这样子。
