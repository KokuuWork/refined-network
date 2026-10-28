<!-- GitHub Release 正文，由 release.yml 读取。 -->

## RS Wireless Crafting 0.3.3

**0.3.x 稳定线修复版。** 基于 0.3.2，只修复旧功能的计费与权限问题，不包含 0.4.x 的新设备和开发功能。

### 下载

| 加载器 | 文件 | 环境 |
| --- | --- | --- |
| NeoForge | `rswirelesscrafting-neoforge-0.3.3.jar` | Minecraft 1.21.1 + NeoForge |
| Fabric | `rswirelesscrafting-fabric-0.3.3.jar` | Minecraft 1.21.1 + Fabric Loader / Fabric API |

两个文件按加载器**二选一**。都需要 Refined Storage 2；建议先备份存档，再直接替换旧 jar。

### 本版修复

- **终端费用预检**：打开、库存插取、手动合成、清空矩阵、配方传输和自动合成，均先按实际距离倍率检查费用，余额不足时不再先执行再扣电。
- **自动合成权限**：创建任务前检查 RS `AUTOCRAFTING` 权限。
- **充电站权限**：打开界面需要 `OPEN` 与 `EXTRACT`；后台取电和充电按授权使用者持续复核，离线时也按 UUID 检查。
- **旧存档兼容**：充电站会保存授权使用者；旧方块首次由有权限的玩家打开后自动记录。
- **隐藏插件失效**：异常数据中超过 4 张的插件不再参与数量统计或实际效果。

### 本版美术

- 终端图标、六张升级插件与充电站纹理整套重绘，向 RS2 原版风格靠拢：中性灰机壳 + 1px 深灰描边、统一的深灰内嵌面板、青色只用于「无线 / 网络」状态。
- 充电站改为多面模型：正面电池 + 广播圆顶，顶面充电线圈垫，侧面线缆导管，底面基板；四个状态各有独立顶/侧纹理，资源路径与 `blockstate` 未改动。
- **0.3.2 及更早版本保持旧美术**，不再回改；玩法与数值完全一致，只是外观不同。

### 验证与说明

- NeoForge / Fabric 完整构建及 checkstyle 均通过。
- 这是稳定线的维护版本，不代表所有多人权限和旧存档场景均已完成实机回归。
- `0.3.2` 的 Release、标签和附件继续保留作为历史记录（旧美术，不回改）；`0.4.1-beta.1` 仍是独立的开发测试版，同样使用这套新美术。

反馈：[GitHub Issues](https://github.com/KokuuWork/rswirelesscrafting/issues) · [QQ 群 498700382](https://qm.qq.com/q/ueYTU8T3xK)

---

## English

**0.3.3 is a maintenance release for the 0.3.x stable line.** It contains no 0.4.x development features.

- Checks actual terminal energy costs before opening, transferring, crafting or starting autocrafting.
- Enforces RS `AUTOCRAFTING` permission before creating tasks.
- Enforces RS `OPEN` / `EXTRACT` permission for charging-station access and background energy use.
- Limits both displayed and effective terminal upgrades to four.
- Keeps the 0.3.2 Release and tag available as historical downloads.

Both loaders build successfully. Please back up your world before updating and report remaining in-game issues with logs and reproduction steps.
