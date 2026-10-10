# chy — Minecraft Fabric 挂机辅助

当前版本：**v3.2.0，适用于 Minecraft Java 1.21.11 + Fabric，Java 21**。

伪键盘操作实现挂机和其他功能。

项目链接：[Chy990/20250721_mc_fabric_mod](https://github.com/Chy990/20250721_mc_fabric_mod)。

> 当前工程目录仍为 `autoclick-template-1.21.11/`；模组名称、ID 和构建产物均已改为 **chy**。
> **每次 build 后，最新可安装 JAR 都在 `autoclick-template-1.21.11/build/libs/chy-3.2.0+mc1.21.11.jar`。**
> `backup/`、`built/` 中的历史文件及旧 `run/` 存档不参与当前构建，保留原样。开发客户端使用 `run-1.21.11/`。

## 20261011 更新已实现

- `/auto_attack` 更名为 `/left_click`，保留原有攻击实体、无目标时挥手的行为，不自动挖方块。
- 新增 `/right_click`，按间隔执行原版单次右键：手持方块时尝试放置，也可操作容器、按钮或使用物品；遵循原版主副手交互规则。
- 左右键独立开关、独立计时，默认均为 **12 tick**；参数统一改为**正整数 tick**，最小 1，最大 2147483647。每次无参数重新开启均恢复 12 tick。
- 开启或调整时，聊天栏和客户端日志显示 `自动左键，间隔12 tick` 或 `自动右键，间隔12 tick`，不再显示秒数。
- 新增 `/shift`：切换持续潜行，可配合右键在容器上放置方块；关闭后恢复实际键盘输入。
- `/chy_help` 包含全部功能、参数、示例及退出世界时的行为。
- 模组名称、ID、日志标识和 JAR 名称改为 `chy`，版本升级到 **3.2.0**，介绍和项目链接已更新。
- 游戏聊天栏中的开关、间隔提示和 `/chy_help` 帮助统一使用红色文字。
- 自定义图标 `chy_mod.png` 已放到工程的 `src/main/resources/assets/chy/icon.png`，由 `fabric.mod.json` 的 `icon` 字段引用；以后替换此文件并重新 build 即可更新图标。

打开聊天、背包或菜单不会关闭功能。退出世界或断开连接后，左右键和自动潜行全部关闭。死亡或旁观期间不自动点击。单人世界暂停时暂停自动点击计时，恢复游戏后继续；多人服务器中的 Esc 菜单不暂停服务器。卡顿时实际执行频率可能降低。

右键间隔指每隔多少 tick **尝试一次右键**；能否成功放置仍取决于准星、距离、剩余方块及原版/服务器规则。正在持续使用物品时不会重复启动右键操作；本功能用于周期单击，不模拟一直按住使用键。

## 游戏内命令

| 命令 | 效果 |
| --- | --- |
| `/left_click` | 开启默认 12 tick 自动左键；已开启时关闭 |
| `/left_click 20` | 开启或调整为每 20 tick 自动左键 |
| `/right_click` | 开启默认 12 tick 自动右键；已开启时关闭 |
| `/right_click 4` | 开启或调整为每 4 tick 自动右键 |
| `/shift` | 开启/关闭持续潜行 |
| `/chy_help` | 查看全部帮助 |

`/left_click 1` 表示每 **1 tick** 执行一次。小数、0、负数、文字及超过整数上限的参数会被拒绝，不改变现有设置。调整间隔时重新计时，满一个间隔后首次执行。旧 `/auto_attack`、`/chy_autoattack`、`/chy_deepseek`、`/chy_say` 不再由本模组提供。

## Mac 开发、测试和编译

### 1. 打开终端，进入工程

```bash
cd /Users/billchen/Desktop/Developer_MAC/20250721_mc_fabric_mod/autoclick-template-1.21.11
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

### 4. 安装到 Minecraft 1.21.11

关闭游戏后：

1. 安装 [Fabric Loader](https://fabricmc.net/use/installer/) 的 Minecraft **1.21.11** 配置，本项目要求 Loader **0.19.5 或更高兼容版本**。
2. 下载 Minecraft **1.21.11** 对应的 [Fabric API](https://modrinth.com/mod/fabric-api/versions?g=1.21.11)，本项目编译使用 `0.141.6+1.21.11`。
3. 确认启动配置的“游戏目录”。macOS 默认是 `~/Library/Application Support/minecraft`，自定义配置使用对应目录。
4. **先移走旧 `autoclick` 和旧 `chy` JAR**，再把 Fabric API 和 `chy-3.2.0+mc1.21.11.jar` 放入该目录的 `mods` 文件夹。由于模组 ID 已改名，旧 `autoclick` 不会自动被新版替换，必须手动移走。
5. 使用 Fabric 1.21.11 配置启动，在临时世界按上面的清单验证。

确认使用默认目录后，可从工程目录复制：

```bash
mkdir -p "$HOME/Library/Application Support/minecraft/mods"
open "$HOME/Library/Application Support/minecraft/mods"
cp build/libs/chy-3.2.0+mc1.21.11.jar "$HOME/Library/Application Support/minecraft/mods/"
```

本模组只需安装在客户端。命令不存在时，检查 Fabric 配置、游戏版本和 mods 目录。

# 更新记录
### v3.2.0
模组更名为 chy；自动攻击命令改为 /left_click，新增 /right_click 与 /shift。左右键统一使用整数 tick，默认 12 tick；更新反馈、帮助、介绍和项目链接。

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

### 20261011计划更新：
1. 为了方便区分，将原本的auto_attack指令改为left_click，默认仍然保持为12tick，只是单独改变指令形式。
2. 新增right_click指令，默认12gt一次。这个right_click即为放置方块的作用，即每12gt放置一次方块。同时类似left_click可以在后缀更改频率。更改单位为tick。
3. 对两个click的指令触发以后文字显示改为：自动左/右键，间隔xx tick。不再用秒作为单位。
4. 新增一个/shift的指令。该指令可以模拟一直按着shift的潜行状态。当再次输入的时候即关闭潜行状态。
5. 介绍里“这是一个可以自动攻击的mod，可用于刷怪塔挂机“改为“伪键盘操作实现挂机和其他功能“。新增链接：https://github.com/Chy990/20250721_mc_fabric_mod
6. 对所有功能都在/chy_help里写好。然后这整个mod改名为chy，不再叫autoclick。
7. 这次更新以后版本定位3.2.0
