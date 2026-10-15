<!-- 本文件是 GitHub Release 的发布说明正文（release.yml 会读取它）。
     发新版时直接改写本文件即可；没有本文件时发布会退回 GitHub 自动生成的说明。 -->

> 🧪 **测试版声明 / Testing build**
> 本版本作为 **0.3.1 正式版**发布，但**仍处于测试阶段** —— 尤其是**首次移植的 Fabric 侧**尚未完成完整真机验证。
> 建议在测试存档中使用，**务必先备份世界**；如遇到问题欢迎直接在
> [Issues](https://github.com/KokuuWork/rswirelesscrafting/issues) 反馈（附日志 + 复现步骤）。

> 🧪 **Testing build**
> Released as **0.3.1**, but it is still a **testing build** — the brand-new **Fabric** port has not gone through
> full in-game verification yet. Use it in a test world and **back up your saves** first. Bug reports are very
> welcome in [Issues](https://github.com/KokuuWork/rswirelesscrafting/issues) (please include logs + repro steps).

---

## 下载 / Downloads

| 加载器 Loader | 文件 File | 说明 Notes |
| --- | --- | --- |
| **NeoForge** | `rswirelesscrafting-neoforge-0.3.1.jar` | Minecraft 1.21.1 |
| **Fabric** | `rswirelesscrafting-fabric-0.3.1.jar` | Minecraft 1.21.1，需 Fabric API |

两个 jar **二选一**，按你的加载器下载；不要同时放进 mods 目录。

**前置依赖 Requirements**

- Minecraft **1.21.1**
- [Refined Storage 2](https://modrinth.com/mod/refined-storage) **2.0.7+**（实测 2.0.9）
- NeoForge 侧：NeoForge for 1.21.1
- Fabric 侧：Fabric Loader + Fabric API
- 可选：Curios（NeoForge）/ Trinkets（Fabric）+ 对应的 RS 整合模组 → 终端可放进饰品栏

---

## 本次更新内容 / What's in this build

### 新增

- **Fabric 移植**：新增 `rswirelesscrafting-fabric` 模块，完整支持 **1.21.1 + Fabric**（配置走 Cloth Config，能量走 Team Reborn Energy）。common 模块零改动，与 NeoForge 侧共用同一份逻辑。
- **饰品栏兼容**（可选、纯数据驱动）：终端可放进 Curios / Trinkets 饰品槽，与 RS 原生无线终端共用同一组槽位，用本模组的快捷键即可从饰品栏直接打开。

### 平衡性调整

- **距离耗电改为线性**：16 格内 ×1，超出后按「距离 ÷ 16」线性增长 —— 32 格 ×2、256 格 ×16、400 格 ×25。
- **跨维度大幅加价**：固定 **×16**（能耗插件不减免跨维度）。
- **合成终端合成更贵**：手动合成 **20 FE**、自动合成 **40 FE**（打开 / 清空 / 配方转移仍为 5 FE）。让能耗插件与远程输电插件真正成为超远距离 / 跨维度的必需品。
- 充电站改显**自身内部缓冲电量**（默认 4000 FE），未满时自动从 RS 网络补电。
- **无线充电改为全局 3 个设备名额**（3 人各充 1 个，或 1 人独占 3 个）。

### 修复

- 充电站与插件装配界面：能量数字改 K/M/G 紧凑格式，不再溢出；「物品栏」标签不再被横线划穿；**槽位悬停提示恢复正常**（MC 1.21 起需手动调用 `renderTooltip`）。
- 旧存档缓冲电量超过新容量时自动 clamp。

### 多人 / 服务器

- 插件装配界面锁定终端所在槽位（不能拿走 / 拖走 / 数字键换掉），终端身份变化立即关闭界面。
- 无线充电扫描与充电站方块状态写入均降频到每 10 tick 一次，速率不变、开销大幅下降。

完整记录见 [CHANGELOG.md](CHANGELOG.md)。

---

## 已知限制 / Known limitations

- **专用服务器配置不同步**：NeoForge 侧配置注册为 `COMMON` 类型，服务器不会把 `rswirelesscrafting-common.toml` 下发给客户端。服主修改数值后，客户端 tooltip 显示的是本地配置值，**实际扣费以服务端为准**。
- **升级默认值改动需要重生成配置**：NeoForge 只会更新配置注释、不会覆盖已存在的值。升级后如发现「注释里的默认值和实际值不一致」，删掉 `config/rswirelesscrafting-common.toml` 让它重新生成。
- **跨维度需要两端区块加载**（与 RS 原生的 Transmitter / Receiver 一致）。
- Fabric 侧为首次移植，若遇到与 NeoForge 侧表现不一致的地方请优先反馈。

---

## 反馈 / Feedback

测试版阶段任何异常（GUI、耗电数值、多人同步、Fabric 启动）都欢迎提 Issue，附：

1. 加载器 + 版本号（NeoForge / Fabric，本模组与 RS 的版本）
2. `latest.log` 或崩溃报告
3. 复现步骤与截图

License: MIT
