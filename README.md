# Refined Storage - Wireless Crafting

[![Build](https://github.com/KokuuWork/rswirelesscrafting/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/KokuuWork/rswirelesscrafting/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/KokuuWork/rswirelesscrafting?label=release)](https://github.com/KokuuWork/rswirelesscrafting/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-3C8527)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-E8892B)
![Refined Storage](https://img.shields.io/badge/Refined_Storage-2.0.7%2B-3E7BB6)

**简体中文** | [English](#english)

给 [Refined Storage 2](https://github.com/refinedmods/refinedstorage) 补上原版缺失的**无线合成**能力。

原版只有一个无线**查看**终端（`wireless_grid`）——你能远程看库存，但不能远程合成。本模组提供一个**无线合成终端**，外加两张升级卡，把它的范围从「发射器附近」一路推到「无限远」和「跨维度」。

![物品图标](docs/images/items.png)

## 物品

| | 物品 | 作用 |
|---|---|---|
| <img src="docs/images/item-grid.png" width="40" alt=""> | **无线合成终端**<br>`wireless_crafting_grid` | 远程访问网络并**远程合成**。稀有度 RARE。 |
| <img src="docs/images/item-range.png" width="40" alt=""> | **范围升级卡**<br>`range_upgrade` | 同维度内**无视距离**。稀有度 EPIC。 |
| <img src="docs/images/item-interdimensional.png" width="40" alt=""> | **维度升级卡**<br>`interdimensional_upgrade` | 解锁**跨维度**访问。稀有度 EPIC。 |

三件物品的鼠标悬停说明都是活的：终端会显示**当前绑定的维度与坐标**、**已安装的升级卡**；两张卡会显示效果、生效前提，以及从配置里读出的当前跨维度能量倍率。

## 环境

| | 要求 |
|---|---|
| Minecraft | **1.21.1** |
| 模组加载器 | NeoForge **21.1.x** |
| 前置 | Refined Storage **2.0.7+**（已在 2.0.9 上实测运行） |
| 构建 | JDK **21** + Gradle 8.14.4 |

## 安装

1. 装好 NeoForge 1.21.1 与 Refined Storage。
2. 把 `rswirelesscrafting-neoforge-<version>.jar`（从 [Releases](https://github.com/KokuuWork/rswirelesscrafting/releases/latest) 下载）丢进 `mods/`。
3. 启动游戏。

> 本模组是**纯附属（addon）**：不 fork、不 mixin Refined Storage 本体，只依赖其公开 API。移除本模组不会损坏存档。

## 使用

1. **建好网络**。同一维度用无线访问点（Wireless Transmitter）；跨维度需要网络变送器 + 网络接收器（Network Transmitter + Receiver）。
2. **绑定**。手持无线合成终端，对着无线访问点或网络接收器**右键**完成绑定。
3. **打开**。右键终端即可打开合成界面（也可在按键设置里绑定快捷键）。
4. **装卡**。在工作台里把「终端 + 升级卡」做**无序合成**，得到装有该卡的终端——合成矩阵、能量、网络绑定全部保留。

## 配方

三件物品都定位为**后期/终局**物品，材料取自 Refined Storage 自带的无线设备、原版最稀有的掉落，以及 RS「套娃」到顶的 64k 存储方块。

### 无线合成终端

<img src="docs/images/recipe-grid.png" width="270" alt="无线合成终端配方">

`Q` 石英富集铁 · `A` 高级处理器 · `P` 末影珍珠 · `G` `refinedstorage:crafting_grids` 标签（任意合成网格）· `M` 机器外壳

合计约 14 石英铁 + 2 末影珍珠 + 2 高级处理器 + 1 合成网格，约为 RS 自带「无线终端」的 **2.3 倍**——多出来的是「可合成」这份能力。

### 范围升级卡

<img src="docs/images/recipe-range.png" width="270" alt="范围升级卡配方">

`P` 末影珍珠 · `H` 海洋之心 · `T` 无线访问点 · `S` 下界之星 · `R` 网络接收器 · `X` 64k存储方块

每个材料都有明确语义：**海洋之心**代表远距离能量传输，**下界之星**是撑起「无限」的能量源，**无线访问点 + 网络接收器**是 RS 通信链路的两端（本卡绑的就是访问点），**64k存储方块**则是 RS 存储元件递归链（1k → 4k → 16k → 64k，每级吃上一级）的顶点，可自动化量产但吞吐极大。

### 维度升级卡

<img src="docs/images/recipe-interdimensional.png" width="270" alt="维度升级卡配方">

`P` 末影珍珠 · `B` 信标 · `X` 64k存储方块 · `N` 下界合金锭 · `S` 下界之星 · `Y` 鞘翅

**鞘翅**只存在于末地船，是「跨越」的象征；**信标**需要定位与锚定（跨维隧道要求两端坐标对齐），而它本身就要消耗一颗下界之星——所以这张卡合计要 **2 颗下界之星 = 2 只凋灵**；**64k存储方块**与范围卡同级；2 个**下界合金锭**是承受维度压力的外壳。

## 距离与维度行为

| 已装升级卡 | 同维度（绑无线访问点） | 跨维度（绑网络接收器） |
|---|---|---|
| 无 | `max(访问点范围, 64 格)` | 不可用 |
| 范围卡 | **无限** | 不可用 |
| 维度卡 | `max(访问点范围, 64 格)` | 可用（隧道，天然无限） |
| 范围卡 + 维度卡 | **无限** | 可用（隧道，天然无限） |

跨维度访问要求网络变送器与接收器**两端区块都已加载**——这是 RS 原生隧道的固有约束，本模组如实保留，没有绕过。

跨维度操作的额外能量开销由配置项 `interdimensionalEnergyMultiplier` 控制（默认 `150`，即 ×1.5）。

## 架构

唯一的扩展点是覆写 `AbstractNetworkEnergyItem.use(ServerPlayer, ItemStack, SlotReference)`，注入自定义的 `ExtendedWirelessNetworkItemContext`：

RS 的距离/维度校验并不位于 `resolveNetwork`，而在 `NetworkItemPlayerValidator`。因此本模组采用「**标准逻辑优先、升级卡兜底放宽**」策略——先让 RS 原生校验跑完，仅在原生返回空时，才根据已安装的升级卡判断是否放宽。同时刻意**不干预 `force=true` 路径**，因为 RS 依赖该语义移除失效的网格 watcher。

完整设计文档（含决策记录、逐文件实现规格、RS 2.0.7 API 核实结果）见 [`docs/DESIGN.md`](docs/DESIGN.md)。

## 从源码构建

```bash
git clone https://github.com/KokuuWork/rswirelesscrafting.git
cd rswirelesscrafting
export JAVA_HOME="/path/to/jdk-21"
./gradlew build
```

产物位于 `rswirelesscrafting-neoforge/build/libs/`。

> 若 `maven.neoforged.net` 在你所在网络不可达，仓库内附带 `init-mirror.gradle`，可把依赖重写到 NeoForged CDN：
> ```bash
> ./gradlew -I init-mirror.gradle build
> ```

`gen_textures.py` 生成模组的四张 16×16 物品纹理，`gen_readme_images.py` 生成上文的配图（两者都只用 Python 标准库，无需 Pillow）。

## 致谢

本项目大量逻辑衍生于 [Refined Storage](https://github.com/refinedmods/refinedstorage)（MIT License, Copyright © 2020 - 2025 Refined Mods），模块结构参考了官方附属样例 [Quartz Arsenal](https://github.com/refinedmods/quartz-arsenal)。在此致谢。

## 许可

[MIT](LICENSE)

---

## English

An addon for **[Refined Storage 2](https://github.com/refinedmods/refinedstorage)** that adds the wireless **crafting** grid the base mod never had.

Vanilla Refined Storage ships only a wireless *viewing* grid — you can browse your network remotely, but not craft on it. This mod adds a **Wireless Crafting Grid** plus two upgrade cards that push its reach from "near a transmitter" all the way to *unlimited* and *cross-dimensional*.

<img src="docs/images/items.png" width="560" alt="Wireless crafting grid and both upgrade cards">

### Items

| | Item | Effect |
|---|---|---|
| <img src="docs/images/item-grid.png" width="40" alt=""> | **Wireless Crafting Grid**<br>`wireless_crafting_grid` | Access your network remotely **and craft**. Rarity: rare. |
| <img src="docs/images/item-range.png" width="40" alt=""> | **Range Upgrade**<br>`range_upgrade` | **Unlimited range** within the same dimension. Rarity: epic. |
| <img src="docs/images/item-interdimensional.png" width="40" alt=""> | **Interdimensional Upgrade**<br>`interdimensional_upgrade` | **Cross-dimensional** access. Rarity: epic. |

Tooltips are live: the grid shows the **bound dimension and coordinates** plus its **installed upgrades**; each card explains its effect, its prerequisite, and the current cross-dimensional energy multiplier read from config.

### Requirements

- Minecraft **1.21.1**, NeoForge **21.1.x**, Refined Storage **2.0.7+** (verified on 2.0.9)
- Pure addon — no forking, no mixins, no permanent changes to your world

### Recipes

Both upgrade cards are **endgame crafts**, built around Refined Storage's own wireless hardware, the rarest vanilla drops, and the apex of RS's nested storage-part chain (1k → 4k → 16k → 64k).

<img src="docs/images/recipe-grid.png" width="270" alt="Wireless crafting grid recipe"> <img src="docs/images/recipe-range.png" width="270" alt="Range upgrade recipe">

<img src="docs/images/recipe-interdimensional.png" width="270" alt="Interdimensional upgrade recipe">

Upgrade cards are installed with a shapeless crafting-table recipe: grid + card.

### Range and dimensions

| Installed | Same dimension (bound to a Wireless Transmitter) | Cross-dimensional (bound to a Network Receiver) |
|---|---|---|
| — | `max(transmitter range, 64 blocks)` | unavailable |
| Range Upgrade | **unlimited** | unavailable |
| Interdimensional Upgrade | `max(transmitter range, 64 blocks)` | available (tunnel, inherently unlimited) |
| Both | **unlimited** | available |

Cross-dimensional access requires both the Transmitter and Receiver chunks to be loaded — an inherent constraint of RS's native link, faithfully preserved rather than worked around.

### How it works

The single extension point is overriding `AbstractNetworkEnergyItem.use(ServerPlayer, ItemStack, SlotReference)` to inject a custom `ExtendedWirelessNetworkItemContext`.

RS's distance/dimension check lives in `NetworkItemPlayerValidator`, not in `resolveNetwork`, so upgrades apply as a **fallback relaxation** — the standard resolution runs first, and only when it comes back empty do the installed cards get a say. The `force=true` path is deliberately left untouched, since RS relies on that semantic to remove stale grid watchers.

See [`docs/DESIGN.md`](docs/DESIGN.md) for the full design document.

### Building

```bash
git clone https://github.com/KokuuWork/rswirelesscrafting.git
cd rswirelesscrafting
export JAVA_HOME="/path/to/jdk-21"
./gradlew build
```

### Credits

Large parts of this project derive from [Refined Storage](https://github.com/refinedmods/refinedstorage) (MIT License, Copyright © 2020 - 2025 Refined Mods). The module layout follows the official addon example [Quartz Arsenal](https://github.com/refinedmods/quartz-arsenal).

Licensed under [MIT](LICENSE).
