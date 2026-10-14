# Refined Storage - Wireless Crafting

[English](#english) | 简体中文

Refined Storage 2 的附属模组，新增一个**无线合成终端**，并支持两张升级卡：

| 物品 | 作用 |
|---|---|
| 无线合成终端 `wireless_crafting_grid` | 远程访问家庭网络并**远程合成** |
| 范围升级卡 `range_upgrade` | 同维度**无限范围**（无视无线发射器距离限制） |
| 维度升级卡 `interdimensional_upgrade` | 解锁**跨维度**访问，走 Network Transmitter/Receiver 隧道 |

原版 Refined Storage 只有无线**查看**终端，本模组补齐了无线**合成**终端。

## 环境

- Minecraft **1.21.1** / NeoForge 21.1.x
- 依赖 Refined Storage **2.0.7+**
- 构建：JDK **21** + Gradle 8.14.4

## 安装

1. 安装 NeoForge 1.21.1 与 Refined Storage 2.0.7+。
2. 把 `rswirelesscrafting-neoforge-<version>.jar` 放进 `mods/` 目录。

> 本模组是**纯附属（addon）**，不 fork、不 mixin Refined Storage 本体，只依赖其公开 API。

## 使用方式

1. 家庭网络放置 Controller + Wireless Transmitter（同维度），或 Network Transmitter + Receiver（跨维度）。
2. 手持无线合成终端，用**网卡右键**发射器/接收器完成绑定。
3. 右键终端打开合成界面。
4. **安装升级卡**：工作台无序合成「终端 + 升级卡」→ 得到装有该卡的终端（合成矩阵、能量、绑定全部保留）。

## 距离与维度行为

| 槽位 | 同维度（绑 Wireless Transmitter） | 跨维度（绑 Network Receiver） |
|---|---|---|
| 无卡 | `max(发射器范围, 64 格)` | 不可用 |
| 范围卡 | **无限** | 不可用 |
| 维度卡 | `max(发射器范围, 64 格)` | 可用（隧道，天然无限） |
| 范围卡 + 维度卡 | **无限** | 可用（隧道，天然无限） |

跨维度访问需要 Network Transmitter 与 Receiver **两端区块均已加载**——这是 RS 原生隧道的固有约束，本模组忠实采用，未做绕过。

配置项 `interdimensionalEnergyMultiplier`（默认 `150`，即 ×1.5）控制跨维度操作的额外能量开销。

## 架构

核心扩展点是覆写 `AbstractNetworkEnergyItem.use(ServerPlayer, ItemStack, SlotReference)`，注入自定义 `ExtendedWirelessNetworkItemContext`：

RS 原生的距离/维度校验并不位于 `resolveNetwork`，而在 `NetworkItemPlayerValidator`。因此本模组采用「**标准逻辑优先、卡片兜底放宽**」策略——先让 RS 原生校验跑完，仅在原生返回空时，依据已安装的升级卡判断是否放宽；同时刻意**不干预 `force=true` 路径**，因为 RS 依赖该语义移除无效的网格 watcher。

完整设计文档见 [`docs/DESIGN.md`](docs/DESIGN.md)（含 ADR 决策记录、逐文件实现规格、RS 2.0.7 API 核实结果）。

## 从源码构建

```bash
git clone <this-repo>
cd rswirelesscrafting
export JAVA_HOME="/path/to/jdk-21"
./gradlew :rswirelesscrafting-neoforge:build
```

产物位于 `rswirelesscrafting-neoforge/build/libs/`。

> 若 `maven.neoforged.net` 在你所在网络不可达，可用仓库内附带的 `init-mirror.gradle` 把依赖重写到 NeoForged CDN：
> ```bash
> ./gradlew -I init-mirror.gradle build
> ```

## 致谢

本项目大量逻辑衍生于 [Refined Storage](https://github.com/refinedmods/refinedstorage)（MIT License, Copyright © 2020 - 2025 Refined Mods），模块结构参考了官方附属样例 [Quartz Arsenal](https://github.com/refinedmods/quartz-arsenal)。在此致谢。

## 许可

[MIT](LICENSE)

---

## English

An addon for **Refined Storage 2** that adds a **Wireless Crafting Grid** plus two upgrade cards:

| Item | Effect |
|---|---|
| `wireless_crafting_grid` | Access your home network remotely **and craft** |
| `range_upgrade` | **Unlimited range** in the same dimension |
| `interdimensional_upgrade` | **Cross-dimensional** access via the Network Transmitter/Receiver link |

Vanilla Refined Storage ships only a wireless *viewing* grid. This mod fills the wireless *crafting* gap.

**Requirements**: Minecraft 1.21.1, NeoForge 21.1.x, Refined Storage 2.0.7+.

Pure addon — no forking, no mixins. The distance/dimension check lives in `NetworkItemPlayerValidator`, not `resolveNetwork`, so upgrades are applied as a *fallback relaxation* after the standard resolution fails, and the `force=true` path is deliberately left untouched.

Cross-dimensional access requires both the Transmitter and Receiver chunks to be loaded — an inherent constraint of RS's native link, faithfully preserved here.

See [`docs/DESIGN.md`](docs/DESIGN.md) for the full design document.

Licensed under [MIT](LICENSE).
