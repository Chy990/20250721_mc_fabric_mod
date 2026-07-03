# 我的世界1.21.x Fabric自动点击器
适合在挂机打怪的时候使用。目前最稳定版本推荐使用**v2.1.0**

``
built/autoclick-v2.1.0.jar
``

目前版本暂时荒废与DeepSeek对话的功能。

# 更新记录
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
