# Refined Storage - Wireless Crafting

[![Build](https://github.com/KokuuWork/rswirelesscrafting/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/KokuuWork/rswirelesscrafting/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/KokuuWork/rswirelesscrafting?label=release)](https://github.com/KokuuWork/rswirelesscrafting/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-3C8527)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-E8892B)
![Refined Storage](https://img.shields.io/badge/Refined_Storage-2.0.7%2B-3E7BB6)

**简体中文** | [English](#english)

给 [Refined Storage 2](https://github.com/refinedmods/refinedstorage) 补上原版缺失的**无线合成**能力。

原版只有一个无线**查看**终端（`wireless_grid`）——你能远程看库存，但不能远程合成。本模组提供一个**无线合成终端**，用四张可插拔的**装配式升级卡**把它的续航、范围、维度与供能方式一路推到终局，再配一座**充电站**为所有 FE 终端补电。

![无线合成终端的未激活态与激活态，以及四张升级卡和充电站](docs/images/items.png)

> 上图中第 1、2 个图标是同一件**无线合成终端**的两种状态：未绑定（灰）与已接入网络（亮）。

## 内容一览

| | 名称 | 作用 | 稀有度 |
|---|---|---|---|
| <img src="docs/images/item-grid.png" width="40" alt=""> | **无线合成终端**<br>`wireless_crafting_grid` | 远程访问网络并**远程合成**。潜行 + 右键打开升级装配界面。 | RARE |
| <img src="docs/images/item-battery.png" width="40" alt=""> | **电池升级卡**<br>`battery_upgrade` | 每张提升 **+1000 FE** 电池容量（可叠加）。也是输电卡的合成原料。 | RARE |
| <img src="docs/images/item-range.png" width="40" alt=""> | **范围升级卡**<br>`range_upgrade` | 同维度内**无视距离**。 | EPIC |
| <img src="docs/images/item-interdimensional.png" width="40" alt=""> | **维度升级卡**<br>`interdimensional_upgrade` | 解锁**跨维度**访问。 | EPIC |
| <img src="docs/images/item-remote-energy.png" width="40" alt=""> | **远距离输电升级卡**<br>`remote_energy_upgrade` | **不再消耗终端自身电池**，直接花所绑定网络里的电。 | EPIC |
| <img src="docs/images/item-charging-station.png" width="40" alt=""> | **充电站**<br>`charging_station` | 为 RS 的**所有** FE 终端充电：槽位有线快充 + 附近玩家无线慢充。 | — |

所有鼠标悬停说明都是活的：终端会显示**当前绑定的维度与坐标**和**已安装的升级卡**；每张卡会显示自己的效果、生效前提，以及从配置里实时读出的数值（电池卡显示容量加成，维度卡显示当前跨维度能量倍率）。

## 升级卡是装配式的，可随时插拔

升级卡**不写死在合成配方里**。手持无线合成终端**潜行 + 右键**，打开「升级装配」界面——**4 个槽位**，把卡拖进去就装、拖出来就卸，终端本体不需要重新合成，合成矩阵、能量、网络绑定全部原样保留。

- 只有真正的升级卡能放进槽位（由物品标签判定），槽位内每格最多 1 张；
- 组件层面只在真的装了卡时才写入 `upgrades` 数据组件，拔空后组件会被移除，物品 NBT 保持干净；
- 拆下来的卡原样回到背包，不会蒸发。

> 这套装配界面取代了早期的「工作台无序合成永久改造」方案——那样装上去就拿不下来了。旧方案的自定义配方与序列化器已从代码中删除。

## 充电站

一座方块，两条充电路径，两种供电来源。**兼容 RS 的任意 FE 终端**（无线查看终端、无线合成终端、便携终端……），因为它走的是通用的 NeoForge 能量能力，而不是 RS 内部的适配器。

| 路径 | 条件 | 默认速率 |
|---|---|---|
| **有线** | 终端放进 GUI 槽位 | **100 FE/t** |
| **无线** | 玩家站在方块 **8 格**内，背包里的终端自动补电 | **10 FE/t** |

| 供电来源 | 说明 |
|---|---|
| **RS 网络**（主来源） | 与 RS 其他机械一致：方块接入网络后，待机耗 8 FE/t，充电时从 `EnergyNetworkComponent` 直接抽取 |
| **外部 FE 缓冲**（兜底） | 注册标准 `Capabilities.EnergyStorage.BLOCK` 能力，容量 10000 FE。其他模组用标准 FE 输电的方块/管道可以给它供电，纯 FE 整合包里也能用 |

取电优先级是**网络优先、缓冲兜底**；网络抽不出来（未联网或网络空）时才动内部缓冲。GUI 里两条能量条分别显示网络存量与外部缓冲。

## 能量模型

| 场景 | 电从哪来 | 单次动作耗电（默认） |
|---|---|---|
| 出厂状态 | 终端**自身电池**（容量 1000 FE） | 开界面 5 · 合成 5 · 自动合成 10 · 清空矩阵 5 · 配方转移 5 |
| 装了**电池升级卡** | 同上，但容量 **+1000 FE / 张**（4 槽叠满 = 5000 FE） | 同上 |
| 装了**远距离输电升级卡** | **直接扣所绑定网络的能量**，终端自身电池不再掉电；网络无电或未绑定时自动回落成出厂行为 | 同上 |

**跨维度时所有耗电 ×1.5**（配置项 `interdimensionalEnergyMultiplier`，默认 `150`），在输电卡与自身电池两条路径上都生效。

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

> 本模组是**纯附属（addon）**：不 fork、不 mixin Refined Storage 本体，只依赖其公开 API。移除本模组不会损坏存档（已装升级卡的终端会保留数据组件，重新装上即恢复）。

## 使用

1. **建好网络**。同一维度用无线访问点（Wireless Transmitter）；跨维度需要网络变送器 + 网络接收器（Network Transmitter + Receiver）。
2. **绑定**。手持无线合成终端，对着无线访问点或网络接收器**右键**完成绑定。
3. **装卡**。**潜行 + 右键**终端打开装配界面，把升级卡拖进 4 个槽位之一。想换卡就拖出来。
4. **打开**。右键终端即可打开合成界面（也可在按键设置里绑定快捷键）。
5. **补电**。把终端丢进充电站的槽位快充，或者站到充电站旁边慢慢回血式充电。

## 配方

六件内容都定位为**后期到终局**：材料取自 Refined Storage 自带的无线设备与递归存储元件、原版最稀有的掉落，以及为了平滑过渡而刻意压低的第一级。

### 无线合成终端

<img src="docs/images/recipe-grid.png" width="270" alt="无线合成终端配方">

`Q` 石英富集铁 · `A` 高级处理器 · `P` 末影珍珠 · `G` `refinedstorage:crafting_grids` 标签（任意合成网格）· `M` 机器外壳

合计约 14 石英铁 + 2 末影珍珠 + 2 高级处理器 + 1 合成网格，约为 RS 自带「无线终端」的 **2.3 倍**——多出来的是「可合成」这份能力。

### 电池升级卡（过渡件）

<img src="docs/images/recipe-battery.png" width="270" alt="电池升级卡配方">

`P` 末影珍珠 · `A` 高级处理器 · `R` 红石块 · `L` 1k存储方块

全表最便宜的一张。**1k 存储方块是 RS 存储元件递归链（1k → 4k → 16k → 64k）的入门级**，红石块是廉价的基础供能材料——两者都是早期就能量产的东西，所以这张卡在**游戏前期**就能做出来，让终端立刻从「开两次界面就没电」变成能长时间使用。

它同时是**远距离输电卡的合成原料**：先把这张卡做出来用着，等中期材料齐了，它不会被浪费——直接升级成输电卡。这条路线刻意避免了「前期为了过渡造的东西到后期变成垃圾」。

### 范围升级卡

<img src="docs/images/recipe-range.png" width="270" alt="范围升级卡配方">

`P` 末影珍珠 · `H` 海洋之心 · `T` 无线访问点 · `S` 下界之星 · `R` 网络接收器 · `X` 64k存储方块

每个材料都有明确语义：**海洋之心**代表远距离能量传输，**下界之星**是撑起「无限」的能量源，**无线访问点 + 网络接收器**是 RS 通信链路的两端（本卡绑的就是访问点），**64k存储方块**则是 RS 存储元件递归链的顶点，可自动化量产但吞吐极大。

### 维度升级卡

<img src="docs/images/recipe-interdimensional.png" width="270" alt="维度升级卡配方">

`P` 末影珍珠 · `B` 信标 · `X` 64k存储方块 · `N` 下界合金锭 · `S` 下界之星 · `Y` 鞘翅

**鞘翅**只存在于末地船，是「跨越」的象征；**信标**需要定位与锚定（跨维隧道要求两端坐标对齐），而它本身就要消耗一颗下界之星——所以这张卡合计要 **2 颗下界之星 = 2 只凋灵**；**64k存储方块**与范围卡同级；2 个**下界合金锭**是承受维度压力的外壳。

### 远距离输电升级卡

<img src="docs/images/recipe-remote-energy.png" width="270" alt="远距离输电升级卡配方">

`P` 末影珍珠 · `B` 电池升级卡 · `X` 64k存储方块 · `S` 下界之星 · `L` 避雷针

与范围卡、维度卡**同级价位的终局件**，但它的配方里嵌着**电池升级卡**——前期做的过渡件在这里被回收利用。**避雷针**是能量从「高空」被引下来的隐喻（对应「从网络远距离取电」），**下界之星**与 **64k存储方块**延续另外两张卡的材料语言，让三张终局卡看起来是一套东西。

### 充电站

<img src="docs/images/recipe-charging-station.png" width="270" alt="充电站配方">

`Q` `c:gems/quartz` 标签（任意石英）· `G` 红石块 · `C` 机器外壳 · `M` 高级处理器

比三张终局卡低一个量级：8 个石英 + 4 个红石块 + 4 个机器外壳 + 1 个高级处理器。它是**功能性基建**而不是升级件，所以定价落在 RS 常规机械的区间里。

## 距离与维度行为

| 已装升级卡 | 同维度（绑无线访问点） | 跨维度（绑网络接收器） |
|---|---|---|
| 无 | `max(访问点范围, 64 格)` | 不可用 |
| 范围卡 | **无限** | 不可用 |
| 维度卡 | `max(访问点范围, 64 格)` | 可用（隧道，天然无限） |
| 范围卡 + 维度卡 | **无限** | 可用（隧道，天然无限） |

跨维度访问要求网络变送器与接收器**两端区块都已加载**——这是 RS 原生隧道的固有约束，本模组如实保留，没有绕过。

## 架构

### 终端与升级卡

唯一的扩展点是覆写 `AbstractNetworkEnergyItem.use(ServerPlayer, ItemStack, SlotReference)`（RS 2.0.7 的 3 参重载），注入自定义的 `ExtendedWirelessNetworkItemContext`。

RS 的距离/维度校验并不位于 `resolveNetwork`，而在 `NetworkItemPlayerValidator`。因此本模组采用「**标准逻辑优先、升级卡兜底放宽**」策略——先让 RS 原生校验跑完，仅在原生返回空时，才根据已安装的升级卡判断是否放宽。同时刻意**不干预 `force=true` 路径**，因为 RS 依赖该语义移除失效的网格 watcher。

能量侧的扩展点是 `NetworkItemContext.isActive()` 与 `drainEnergy(long)`：装了输电卡时「有没有电」改由绑定网络回答，扣电也直接从 `EnergyNetworkComponent.extract` 出账。

### 充电站

充电站**不**沿用 RS 的方块基类——RS 2.0.7 没有公开的方块基类（方块全部在无 `@API` 标注的内部模块里），所以方块本身继承原版 `BaseEntityBlock`，tick 走原版 `getTicker`。

方块实体则继承 RS 公开且标注为 STABLE 的 `AbstractNetworkNodeContainerBlockEntity<T extends NetworkNode>`，由它负责网络容器生命周期；自定义的 `ChargingStationNetworkNode` 只声明待机耗电。网络连接用的是 RS 的默认六向连接策略，因此连 `createMainContainer` 都不用重写。

物品充电走 NeoForge 的 `Capabilities.EnergyStorage.ITEM`，**不**经过 RS 的 `PlatformImpl.getEnergyStorage`（那只会认 RS 自己的适配器），这样才能兼容其他模组的 FE 物品。

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

**美术资产**全部由脚本生成，只用 Python 标准库，不需要 Pillow：

| 脚本 | 产出 |
|---|---|
| `gen_textures.py` | 6 张物品纹理 + 1 张方块纹理 |
| `gen_gui_textures.py` | 2 张 176×166 的界面背景（充电站 / 升级装配） |
| `gen_readme_images.py` | 上文的全部配图（需 `MC_CLIENT_JAR` 与 `RS_TEXTURES` 指向 1.21.1 客户端 jar 与 RS 源码树） |

## 致谢

本项目大量逻辑衍生于 [Refined Storage](https://github.com/refinedmods/refinedstorage)（MIT License, Copyright © 2020 - 2025 Refined Mods），模块结构参考了官方附属样例 [Quartz Arsenal](https://github.com/refinedmods/quartz-arsenal)。在此致谢。

## 许可

[MIT](LICENSE)

---

## English

An addon for **[Refined Storage 2](https://github.com/refinedmods/refinedstorage)** that adds the wireless **crafting** grid the base mod never had.

Vanilla Refined Storage ships only a wireless *viewing* grid — you can browse your network remotely, but not craft on it. This mod adds a **Wireless Crafting Grid**, four swappable **upgrade cards** that extend its endurance, range, dimensions and power source all the way to endgame, plus a **Charging Station** that recharges every FE-based terminal.

<img src="docs/images/items.png" width="620" alt="Wireless crafting grid (inactive and active), the four upgrade cards, and the charging station">

> The first two icons are the *same* item — the **Wireless Crafting Grid** — in its unbound (dim) and network-connected (lit) states.

### Contents

| | Item | Effect | Rarity |
|---|---|---|---|
| <img src="docs/images/item-grid.png" width="40" alt=""> | **Wireless Crafting Grid**<br>`wireless_crafting_grid` | Access your network remotely **and craft**. Sneak + right-click to open the upgrade panel. | rare |
| <img src="docs/images/item-battery.png" width="40" alt=""> | **Battery Upgrade**<br>`battery_upgrade` | **+1000 FE** capacity each (stacks). Also the ingredient of the Remote Energy Upgrade. | rare |
| <img src="docs/images/item-range.png" width="40" alt=""> | **Range Upgrade**<br>`range_upgrade` | **Unlimited range** within the same dimension. | epic |
| <img src="docs/images/item-interdimensional.png" width="40" alt=""> | **Interdimensional Upgrade**<br>`interdimensional_upgrade` | **Cross-dimensional** access. | epic |
| <img src="docs/images/item-remote-energy.png" width="40" alt=""> | **Remote Energy Upgrade**<br>`remote_energy_upgrade` | Draws power **from the bound network** instead of the grid's own battery. | epic |
| <img src="docs/images/item-charging-station.png" width="40" alt=""> | **Charging Station**<br>`charging_station` | Recharges **every** RS FE terminal: fast wired slot + slow wireless aura. | — |

Tooltips are live: the grid shows the **bound dimension and coordinates** and its **installed upgrades**; each card explains its effect, its prerequisite, and the values read from config in real time.

### The upgrade cards are pluggable

Cards are **not** baked into a crafting recipe. **Sneak + right-click** the grid to open the upgrade panel — **4 slots**. Drop a card in to install it, pull it out to remove it. The grid is never re-crafted, and its crafting matrix, stored energy and network binding are all preserved.

- Only genuine upgrade cards fit (decided by item tags); one card per slot.
- The `upgrades` data component is written only when a card is actually installed, and removed once the last one is pulled — item NBT stays clean.
- Removed cards return to your inventory intact.

> This replaced the earlier "shapeless crafting-table, permanent modification" design, where an installed card could never come back out. Those custom recipe classes have been deleted from the codebase.

### Charging Station

One block, two charging paths, two power sources. It works with **any FE terminal from Refined Storage** (wireless grid, wireless crafting grid, portable grid, …) because it uses the generic NeoForge energy capability rather than RS's internal adapter.

| Path | Requirement | Default rate |
|---|---|---|
| **Wired** | Terminal sits in the GUI slot | **100 FE/t** |
| **Wireless** | Player stands within **8 blocks**; terminals in their inventory are topped up | **10 FE/t** |

| Power source | Notes |
|---|---|
| **RS network** (primary) | Like every other RS machine: 8 FE/t idle draw, charged straight out of the network's `EnergyNetworkComponent` |
| **External FE buffer** (fallback) | Registers the standard `Capabilities.EnergyStorage.BLOCK` capability with a 10000 FE buffer, so other mods' FE generators and cables can feed it — it works in pure-FE packs too |

Power is drawn **network first, buffer second**. The GUI shows both bars.

### Energy model

| Situation | Where the power comes from | Cost per action (default) |
|---|---|---|
| Stock | The grid's **own battery** (1000 FE) | open 5 · craft 5 · autocraft 10 · clear matrix 5 · recipe transfer 5 |
| **Battery Upgrade** installed | Same, but capacity **+1000 FE per card** (4 slots = 5000 FE) | unchanged |
| **Remote Energy Upgrade** installed | **Straight out of the bound network**; the grid's battery is untouched. Falls back to stock behaviour if the network is empty or unbound. | unchanged |

All costs are **×1.5 when cross-dimensional** (`interdimensionalEnergyMultiplier`, default `150`), on both the remote-energy and battery paths.

### Requirements

- Minecraft **1.21.1**, NeoForge **21.1.x**, Refined Storage **2.0.7+** (verified on 2.0.9)
- Pure addon — no forking, no mixins, no permanent changes to your world (grids keep their data components if you uninstall; they come back when you reinstall)

### Recipes

Each item is a late-game craft, built around Refined Storage's own wireless hardware and nested storage parts, the rarest vanilla drops, and one deliberately cheap entry-level card for a smooth progression curve.

<img src="docs/images/recipe-grid.png" width="230" alt="Wireless crafting grid recipe"> <img src="docs/images/recipe-battery.png" width="230" alt="Battery upgrade recipe"> <img src="docs/images/recipe-charging-station.png" width="230" alt="Charging station recipe">

The **Battery Upgrade** is the cheap one — 1k storage blocks and redstone blocks are early-game materials, so the grid stops running dry long before the endgame cards are affordable. It is also the ingredient of the **Remote Energy Upgrade**, so the early investment is never wasted.

<img src="docs/images/recipe-range.png" width="270" alt="Range upgrade recipe">

<img src="docs/images/recipe-interdimensional.png" width="270" alt="Interdimensional upgrade recipe">

<img src="docs/images/recipe-remote-energy.png" width="270" alt="Remote energy upgrade recipe">

The three endgame cards share one material language (nether star + 64k storage block), so they read as a set.

### Range and dimensions

| Installed | Same dimension (bound to a Wireless Transmitter) | Cross-dimensional (bound to a Network Receiver) |
|---|---|---|
| — | `max(transmitter range, 64 blocks)` | unavailable |
| Range Upgrade | **unlimited** | unavailable |
| Interdimensional Upgrade | `max(transmitter range, 64 blocks)` | available (tunnel, inherently unlimited) |
| Both | **unlimited** | available |

Cross-dimensional access requires both the Transmitter and Receiver chunks to be loaded — an inherent constraint of RS's native link, faithfully preserved rather than worked around.

### How it works

**Grid and cards.** The single extension point is overriding `AbstractNetworkEnergyItem.use(ServerPlayer, ItemStack, SlotReference)` to inject a custom `ExtendedWirelessNetworkItemContext`. RS's distance/dimension check lives in `NetworkItemPlayerValidator`, not in `resolveNetwork`, so upgrades apply as a **fallback relaxation** — the standard resolution runs first, and only when it comes back empty do the installed cards get a say. The `force=true` path is deliberately left untouched, since RS relies on that semantic to remove stale grid watchers. On the energy side, `isActive()` and `drainEnergy(long)` are overridden so the bound network answers "is there power?" and pays the bill.

**Charging Station.** RS 2.0.7 exposes no public block base class (blocks live in an internal, un-annotated module), so the block extends vanilla `BaseEntityBlock` and uses vanilla `getTicker`. The block entity extends the STABLE-annotated `AbstractNetworkNodeContainerBlockEntity<T extends NetworkNode>`, with a minimal `ChargingStationNetworkNode` declaring the idle draw; RS's default six-way connection strategy means `createMainContainer` needs no override at all. Item charging uses NeoForge's `Capabilities.EnergyStorage.ITEM` rather than RS's `PlatformImpl.getEnergyStorage` (which only recognises RS's own adapter), which is what makes it compatible with other mods' FE items.

See [`docs/DESIGN.md`](docs/DESIGN.md) for the full design document.

### Building

```bash
git clone https://github.com/KokuuWork/rswirelesscrafting.git
cd rswirelesscrafting
export JAVA_HOME="/path/to/jdk-21"
./gradlew build
```

All artwork is script-generated with the Python standard library (no Pillow): `gen_textures.py` (item and block textures), `gen_gui_textures.py` (176×166 GUI backgrounds) and `gen_readme_images.py` (the images above; needs `MC_CLIENT_JAR` and `RS_TEXTURES`).

### Credits

Large parts of this project derive from [Refined Storage](https://github.com/refinedmods/refinedstorage) (MIT License, Copyright © 2020 - 2025 Refined Mods). The module layout follows the official addon example [Quartz Arsenal](https://github.com/refinedmods/quartz-arsenal).

Licensed under [MIT](LICENSE).
