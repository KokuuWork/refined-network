# Refined Storage - Wireless Crafting（无线合成终端）

[![Build](https://github.com/KokuuWork/rswirelesscrafting/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/KokuuWork/rswirelesscrafting/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/KokuuWork/rswirelesscrafting?label=release)](https://github.com/KokuuWork/rswirelesscrafting/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-3C8527)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-E8892B)
![Refined Storage](https://img.shields.io/badge/Refined_Storage-2.0.7%2B-3E7BB6)

**简体中文** | [English](#english)

给 [Refined Storage 2](https://github.com/refinedmods/refinedstorage) 补上原版缺失的**无线合成**能力。

原版只有一个无线**查看**终端（`wireless_grid`）——你能远程看库存，但不能远程合成。本模组提供一个**无线合成终端**，用六张可插拔的**装配式升级插件**把它的续航、范围、维度与供能方式一路推到终局，再配一座**终端充电站**为所有 FE 终端补电。所有无线终端（含 RS 原生的无线终端与无线合成监视器）都遵循**按距离扣电**的分段价目表，离网络越远耗电越狠。

![无线合成终端的未激活态与激活态，以及四张升级卡和终端充电站](docs/images/items.png)

> 上图中第 1、2 个图标是同一件**无线合成终端**的两种状态：未绑定（灰）与已接入网络（亮）。

## 内容一览

| | 名称 | 作用 | 稀有度 |
|---|---|---|---|
| <img src="docs/images/item-grid.png" width="40" alt=""> | **无线合成终端**<br>`wireless_crafting_grid` | 远程访问网络并**远程合成**。潜行 + 右键打开插件装配界面。 | RARE |
| <img src="docs/images/item-battery.png" width="40" alt=""> | **电池升级插件**<br>`battery_upgrade` | 每张提升 **+1000 FE** 电池容量（可叠加）。前期刚需。 | RARE |
| <img src="docs/images/item-energy.png" width="40" alt=""> | **初级能耗插件**<br>`energy_upgrade` | **距离能量倍率减半**。也是高级能耗插件的原料。 | RARE |
| <img src="docs/images/item-advanced-energy.png" width="40" alt=""> | **高级能耗插件**<br>`advanced_energy_upgrade` | **完全消除距离能量倍率**（跨维倍率保留）。也是远程输电插件的原料。 | EPIC |
| <img src="docs/images/item-range.png" width="40" alt=""> | **距离升级插件**<br>`range_upgrade` | 同维度内**无视距离**。 | EPIC |
| <img src="docs/images/item-interdimensional.png" width="40" alt=""> | **维度升级插件**<br>`interdimensional_upgrade` | 解锁**跨维度**访问。 | EPIC |
| <img src="docs/images/item-remote-energy.png" width="40" alt=""> | **远程输电插件**<br>`remote_energy_upgrade` | **不再消耗终端自身电池**，直接花所绑定网络里的电。 | EPIC |
| <img src="docs/images/item-terminal-charging-station.png" width="40" alt=""> | **终端充电站**<br>`terminal_charging_station` | 3 个有线槽位快充，5 格范围内自动无线补电（最多 3 名玩家）。 | — |

所有鼠标悬停说明都是活的：终端会显示**当前绑定的维度与坐标**和**已安装的插件**；每张插件会显示自己的效果、生效前提，以及从配置里实时读出的数值（电池插件显示容量加成，维度插件显示当前跨维度能量倍率，初级能耗插件显示除数）。

## 升级插件是装配式的，可随时插拔

升级插件**不写死在合成配方里**。手持任意无线终端（本模组合成终端，或 RS 原生的无线终端 / 无线合成监视器）**潜行 + 右键**，打开「插件装配」界面——**4 个槽位**，把插件拖进去就装、拖出来就卸，终端本体不需要重新合成，合成矩阵、能量、网络绑定全部原样保留。

- 只有真正的升级插件能放进槽位（由物品标签判定），槽位内每格最多 1 张；
- 组件层面只在真的装了插件时才写入 `upgrades` 数据组件，拔空后组件会被移除，物品 NBT 保持干净；
- 拆下来的插件原样回到背包，不会蒸发。

> 这套装配界面取代了早期的「工作台无序合成永久改造」方案——那样装上去就拿不下来了。旧方案的自定义配方与序列化器已从代码中删除。

## 充电站

一座方块，两条充电路径，两种供电来源。**兼容 RS 的任意 FE 终端**（无线查看终端、无线合成终端、便携终端……），因为它走的是通用的 NeoForge 能量能力，而不是 RS 内部的适配器。

| | 终端充电站<br>`terminal_charging_station` |
|---|---|
| **有线**（终端放进 GUI 槽位） | **3 个槽位**，各 **200 FE/s**（**无损耗**） |
| **无线**（玩家在范围内自动补电） | **20 FE/s**（每个设备，半径 **5 格**），**45% 效率**：消耗 100 只给设备充 45，其余作传输损耗 |
| 无线并发 | 每名玩家最多同时充 **3 个设备**，最多服务 **3 名玩家**（按离站距离由近到远分配） |
| 基础待机损耗 | **128 FE/s**（常驻，充电另算）——可用**红石信号**关掉无线充电并停扣待机损耗 |
| 外部 FE 缓冲 | 10000 FE |

界面是一张 **3×2 连接面板**：上排 3 个有线槽（放物品，充电中蓝灯呼吸闪烁、充满绿灯），下排 3 个无线链接灯（正在为某名玩家充电时呼吸闪烁）。

**无线充电的开关状态直接写在方块外形上**：面板的闪电指示灯在无线开启时亮青色，被红石关掉后熄灭变灰——不用打开界面就能看出无线模块是否在工作。

| 供电来源 | 说明 |
|---|---|
| **RS 网络**（主来源） | 与 RS 其他机械一致：方块接入网络后，待机损耗直接从 `EnergyNetworkComponent` 抽取 |
| **外部 FE 缓冲**（兜底） | 注册标准 `Capabilities.EnergyStorage.BLOCK` 能力，其他模组用标准 FE 输电的方块/管道可以给它供电，纯 FE 整合包里也能用 |

取电优先级是**网络优先、缓冲兜底**；网络抽不出来（未联网或网络空）时才动内部缓冲。GUI 里的能量条显示网络存量。

> 表里的速率一律是**每秒（FE/s）**。Minecraft 按 **20 tick = 1 秒** 结算，方块内部就把每秒额度摊到每 tick，除不尽的部分顺延到下一 tick，所以长期平均值精确、也不会漏电。折算成每 tick 是：有线 10、无线 1（每个设备）。
>
> 基础待机损耗同样按每秒算，否则会出现「站一空转，待机就把有线充电全吃掉」的倒挂。把它定在 128 FE/s，是故意「有惩罚感」——不关红石就一直空烧，拉了红石才省电。

## 能量模型

### 按距离扣电（所有无线终端通用）

所有无线终端——本模组的无线合成终端，以及 RS 原生的**无线终端**和**无线合成监视器**——每执行一次动作，消耗的能量都乘上一个**距离倍率**（配置在 `distanceEnergy` 节）：

| 水平距离（玩家 ↔ 绑定网络） | 倍率（默认） |
|---|---|
| 0 – 10 格 | **×1** |
| 10 – 100 格 | **×2** |
| 100 – 1000 格 | **×4** |
| 1000 格以上（需距离升级插件） | **×8** |
| 跨维度（需维度升级插件） | **×1.5** |

三段能耗插件就是为此准备的套娃链：

- **初级能耗插件**：距离倍率**减半**（向上取整）；
- **高级能耗插件**：距离倍率**归 1**（跨维度 ×1.5 仍保留）；
- **远程输电插件**：扣电直接**从绑定网络出账**，终端自身电池不再掉电，距离惩罚在实际上被网络电量抹平。

套娃不浪费：`初级能耗插件 → 高级能耗插件 → 远程输电插件`，每一级都是上一级的合成原料。

### 单次动作基础耗电

| 场景 | 电从哪来 | 单次动作耗电（默认） |
|---|---|---|
| 出厂状态 | 终端**自身电池**（容量 400 FE） | 开界面 5 · 合成 5 · 自动合成 10 · 清空矩阵 5 · 配方转移 5 |
| 装了**电池升级插件** | 同上，但容量 **+1000 FE / 张** | 同上 |
| 装了**远程输电插件** | **直接扣所绑定网络的能量**，终端自身电池不再掉电；网络无电或未绑定时自动回落成出厂行为 | 同上 |

基础电池容量只有 **400 FE**，而距离倍率能把单次动作抬到几十甚至上百 FE——所以**电池升级插件是前期刚需**：不装它，走远一点终端很快就见底，只能反复跑回充电站。

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

> 本模组是**纯附属（addon）**：只依赖 Refined Storage 的公开 API，另用一处**窄 mixin**（只包一层 `AbstractNetworkEnergyItem.use` 的上下文，让距离耗电与升级插件对 RS 原生无线终端同样生效），不 fork、不魔改 RS 本体逻辑。移除本模组不会损坏存档（已装升级插件的终端会保留数据组件，重新装上即恢复）。

## 使用

1. **建好网络**。同一维度用无线访问点（Wireless Transmitter）；跨维度需要网络变送器 + 网络接收器（Network Transmitter + Receiver）。
2. **绑定**。手持无线合成终端，对着无线访问点或网络接收器**右键**完成绑定。
3. **装插件**。**潜行 + 右键**终端打开装配界面，把升级插件拖进 4 个槽位之一。想换就拖出来。
4. **打开**。右键终端即可打开合成界面（也可在按键设置里绑定快捷键）。
5. **补电**。终端充电站摆在基地里，3 个槽位有线快充，5 格范围内的玩家自动无线补电——走过去就回电。

## 配方

八件内容大多定位为**后期到终局**：材料取自 Refined Storage 自带的无线设备与递归存储元件、原版最稀有的掉落。只有**电池升级插件**、**初级能耗插件**与**终端充电站**刻意压低到前中期——它们负责把曲线撑平，剩下的都是终局件。

### 无线合成终端

<img src="docs/images/recipe-grid.png" width="270" alt="无线合成终端配方">

`Q` 石英富集铁 · `A` 高级处理器 · `P` 末影珍珠 · `G` `refinedstorage:crafting_grids` 标签（任意合成网格）· `M` 机器外壳

合计约 14 石英铁 + 2 末影珍珠 + 2 高级处理器 + 1 合成网格，约为 RS 自带「无线终端」的 **2.3 倍**——多出来的是「可合成」这份能力。

### 电池升级插件（前期刚需）

<img src="docs/images/recipe-battery.png" width="270" alt="电池升级插件配方">

`P` 末影珍珠 · `A` 高级处理器 · `R` 红石块 · `L` 1k存储方块

全表最便宜的一张。**1k 存储方块是 RS 存储元件递归链（1k → 4k → 16k → 64k）的入门级**，红石块是廉价的基础供能材料——两者都是早期就能量产的东西，所以这张插件在**游戏前期**就能做出来，让终端立刻从「开两次界面就没电」变成能长时间使用。

### 初级能耗插件（过渡件）

<img src="docs/images/recipe-energy.png" width="270" alt="初级能耗插件配方">

`P` 末影珍珠 · `C` 铜锭 · `R` 红石块 · `S` 基础处理器

距离耗电 ×2 起跳后，这张插件把倍率**砍一半**。它是**高级能耗插件**的原料，前期做了到中期不会浪费。

### 高级能耗插件

<img src="docs/images/recipe-advanced-energy.png" width="270" alt="高级能耗插件配方">

`Q` 石英富集铁 · `P` 高级处理器 · `X` 4k存储方块 · `E` 初级能耗插件

彻底**消除距离倍率**（跨维度 ×1.5 仍保留）。它又是**远程输电插件**的原料——套娃链 `初级能耗插件 → 高级能耗插件 → 远程输电插件` 每一环都回收上一环，投入不打水漂。

### 距离升级插件

<img src="docs/images/recipe-range.png" width="270" alt="距离升级插件配方">

`P` 末影珍珠 · `H` 海洋之心 · `T` 无线访问点 · `S` 下界之星 · `R` 网络接收器 · `X` 64k存储方块

每个材料都有明确语义：**海洋之心**代表远距离能量传输，**下界之星**是撑起「无限」的能量源，**无线访问点 + 网络接收器**是 RS 通信链路的两端（本插件绑的就是访问点），**64k存储方块**则是 RS 存储元件递归链的顶点，可自动化量产但吞吐极大。

### 维度升级插件

<img src="docs/images/recipe-interdimensional.png" width="270" alt="维度升级插件配方">

`P` 末影珍珠 · `B` 信标 · `X` 64k存储方块 · `N` 下界合金锭 · `S` 下界之星 · `Y` 鞘翅

**鞘翅**只存在于末地船，是「跨越」的象征；**信标**需要定位与锚定（跨维隧道要求两端坐标对齐），而它本身就要消耗一颗下界之星——所以这张插件合计要 **2 颗下界之星 = 2 只凋灵**；**64k存储方块**与距离插件同级；2 个**下界合金锭**是承受维度压力的外壳。

### 远程输电插件

<img src="docs/images/recipe-remote-energy.png" width="270" alt="远程输电插件配方">

`C` 铜块 · `B` 高级能耗插件 · `X` 64k存储方块 · `S` 下界之星 · `L` 避雷针

与距离插件、维度插件**同级价位的终局件**，但它的配方里嵌着**高级能耗插件**——前面两级能耗插件在这里被回收利用。

四角用**铜块**（导体的代名词），中心列是**避雷针**——这是全表唯一一组「导电材料」搭配，因为这张插件讲的是电，而不是空间或维度。**下界之星**与 **64k存储方块**延续另外两张终局件的材料语言，让三张终局件看起来是一套东西。

> 早期版本四角是末影珍珠，但末影珍珠与「电」没有任何语义关系，纯粹是为了凑稀有度——现已换成铜块。

### 终端充电站

<img src="docs/images/recipe-terminal-charging-station.png" width="270" alt="终端充电站配方">

`Q` `c:gems/quartz` 标签（任意石英）· `G` 红石块 · `C` 机器外壳 · `M` 高级处理器

比三张终局卡低一个量级：8 个石英 + 4 个红石块 + 4 个机器外壳 + 1 个高级处理器。它是**功能性基建**而不是升级件，所以定价落在 RS 常规机械的区间里，前中期就能搭起来。

## 距离与维度行为

| 已装升级插件 | 同维度（绑无线访问点） | 跨维度（绑网络接收器） |
|---|---|---|
| 无 | `max(访问点范围, 64 格)` | 不可用 |
| 距离插件 | **无限** | 不可用 |
| 维度插件 | `max(访问点范围, 64 格)` | 可用（隧道，天然无限） |
| 距离插件 + 维度插件 | **无限** | 可用（隧道，天然无限） |

跨维度访问要求网络变送器与接收器**两端区块都已加载**——这是 RS 原生隧道的固有约束，本模组如实保留，没有绕过。

## 架构

### 终端与升级插件

核心扩展点是覆写 `AbstractNetworkEnergyItem.use(ServerPlayer, ItemStack, SlotReference)`（RS 2.0.7 的 3 参重载），注入自定义的 `ExtendedWirelessNetworkItemContext`。

为了让距离耗电与升级插件也对 **RS 原生无线终端**（无线终端、无线合成监视器）生效，本模组用一处**窄 mixin**（`AbstractNetworkEnergyItemMixin`）在同一个 3 参 `use` 上包一层同样的上下文，并在潜行右键时打开同一个插件装配界面。这是唯一动到 RS 类的地方，且只在父类方法上包一层、不改任何逻辑分支。

RS 的距离/维度校验并不位于 `resolveNetwork`，而在 `NetworkItemPlayerValidator`。因此本模组采用「**标准逻辑优先、升级插件兜底放宽**」策略——先让 RS 原生校验跑完，仅在原生返回空时，才根据已安装的升级插件判断是否放宽。同时刻意**不干预 `force=true` 路径**，因为 RS 依赖该语义移除失效的网格 watcher。

能量侧的扩展点是 `NetworkItemContext.isActive()` 与 `drainEnergy(long)`：距离倍率在这里按「分段价目表 × 能耗插件」算出并叠加到每次扣电上；装了远程输电插件时「有没有电」改由绑定网络回答，扣电也直接从 `EnergyNetworkComponent.extract` 出账。

### 充电站

充电站**不**沿用 RS 的方块基类——RS 2.0.7 没有公开的方块基类（方块全部在无 `@API` 标注的内部模块里），所以方块本身继承原版 `BaseEntityBlock`，tick 走原版 `getTicker`。

注意 `BaseEntityBlock` 默认返回 `RenderShape.INVISIBLE`（原版方块实体如箱子、熔炉都是自己画模型的），所以带方块实体又想用普通 json 模型的方块必须显式覆写 `getRenderShape()` 返回 `RenderShape.MODEL`，否则方块放下后完全不画、直接看穿到后面的世界。本模组在 0.1.9 修掉了这个坑。

方块实体则继承 RS 公开且标注为 STABLE 的 `AbstractNetworkNodeContainerBlockEntity<T extends NetworkNode>`，由它负责网络容器生命周期；自定义的 `ChargingStationNetworkNode` 只声明待机耗电。网络连接用的是 RS 的默认六向连接策略，因此连 `createMainContainer` 都不用重写。

每 tick 里，服务端先为 3 个有线槽各自按速率补电，再扫描半径内的玩家（按距离由近到远排序），给最近的 3 名玩家各分配一条无线链接，逐条链接对玩家背包里的 FE 设备充电（同一玩家同时最多 3 个设备，充满一个自动轮到下一个）。连接面板的 6 个灯（3 有线 + 3 无线）由服务端算好状态写进 `ContainerData` 同步给客户端，客户端用 `level.getGameTime()` 驱动蓝灯的呼吸闪烁——纯视觉，不占额外网络包。多名玩家各自独占一条链接，状态互不干扰。

无线充电带 **45% 效率**（`deliver` 里按 `cost = ceil(given * 100 / efficiency)` 出账，消耗 100 只充 45）；有线恒为 100%。基础待机损耗（128 FE/s）只有在无线模块开启时才会扣，被红石信号关掉后停扣。方块带一个 `wireless` 方块状态，服务端在状态变化时 `setBlockAndUpdate`，用亮/灭两套纹理让外形直接反映无线是否开启。

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
| `gen_textures.py` | 8 张物品纹理 + 4 张方块纹理（终端充电站 4 形态：通电/断电 × 无线开/关） |
| `gen_gui_textures.py` | 2 张 176×166 的界面背景（终端充电站、升级装配） |
| `gen_readme_images.py` | 上文的全部配图（需 `MC_CLIENT_JAR` 与 `RS_TEXTURES` 指向 1.21.1 客户端 jar 与 RS 源码树） |

## 致谢

本项目大量逻辑衍生于 [Refined Storage](https://github.com/refinedmods/refinedstorage)（MIT License, Copyright © 2020 - 2025 Refined Mods），模块结构参考了官方附属样例 [Quartz Arsenal](https://github.com/refinedmods/quartz-arsenal)。在此致谢。

## 更新日志

见 [CHANGELOG.md](CHANGELOG.md)。

## 许可

[MIT](LICENSE)

---

## English

An addon for **[Refined Storage 2](https://github.com/refinedmods/refinedstorage)** that adds the wireless **crafting** grid the base mod never had.

Vanilla Refined Storage ships only a wireless *viewing* grid — you can browse your network remotely, but not craft on it. This mod adds a **Wireless Crafting Grid**, six swappable **upgrade cards** that extend its endurance, range, dimensions and power source all the way to endgame, plus a **Terminal Charging Station** that recharges every FE-based terminal. Every wireless terminal (including RS's own wireless grid and wireless crafting monitor) follows a **distance-based energy tariff** — the further you are from the network, the more each action costs.

<img src="docs/images/items.png" width="620" alt="Wireless crafting grid (inactive and active), the six upgrade cards, and the terminal charging station">

> The first two icons are the *same* item — the **Wireless Crafting Grid** — in its unbound (dim) and network-connected (lit) states.

### Contents

| | Item | Effect | Rarity |
|---|---|---|---|
| <img src="docs/images/item-grid.png" width="40" alt=""> | **Wireless Crafting Grid**<br>`wireless_crafting_grid` | Access your network remotely **and craft**. Sneak + right-click to open the upgrade panel. | rare |
| <img src="docs/images/item-battery.png" width="40" alt=""> | **Battery Upgrade**<br>`battery_upgrade` | **+1000 FE** capacity each (stacks). The early-game must-have. | rare |
| <img src="docs/images/item-energy.png" width="40" alt=""> | **Energy Upgrade**<br>`energy_upgrade` | **Halves** the distance energy multiplier. Ingredient of the Advanced Energy Upgrade. | rare |
| <img src="docs/images/item-advanced-energy.png" width="40" alt=""> | **Advanced Energy Upgrade**<br>`advanced_energy_upgrade` | **Removes** the distance multiplier (cross-dimension still applies). Ingredient of the Remote Energy Upgrade. | epic |
| <img src="docs/images/item-range.png" width="40" alt=""> | **Range Upgrade**<br>`range_upgrade` | **Unlimited range** within the same dimension. | epic |
| <img src="docs/images/item-interdimensional.png" width="40" alt=""> | **Interdimensional Upgrade**<br>`interdimensional_upgrade` | **Cross-dimensional** access. | epic |
| <img src="docs/images/item-remote-energy.png" width="40" alt=""> | **Remote Energy Upgrade**<br>`remote_energy_upgrade` | Draws power **from the bound network** instead of the terminal's own battery. | epic |
| <img src="docs/images/item-terminal-charging-station.png" width="40" alt=""> | **Terminal Charging Station**<br>`terminal_charging_station` | 3 wired fast-charge slots, plus a 5-block wireless aura (up to 3 players). | — |

Tooltips are live: the terminal shows the **bound dimension and coordinates** and its **installed upgrades**; each card explains its effect, its prerequisite, and the values read from config in real time.

### The upgrade cards are pluggable

Cards are **not** baked into a crafting recipe. **Sneak + right-click** any wireless terminal (this mod's grid, or RS's wireless grid / wireless crafting monitor) to open the upgrade panel — **4 slots**. Drop a card in to install it, pull it out to remove it. The terminal is never re-crafted, and its crafting matrix, stored energy and network binding are all preserved.

- Only genuine upgrade cards fit (decided by item tags); one card per slot.
- The `upgrades` data component is written only when a card is actually installed, and removed once the last one is pulled — item NBT stays clean.
- Removed cards return to your inventory intact.

> This replaced the earlier "shapeless crafting-table, permanent modification" design, where an installed card could never come back out. Those custom recipe classes have been deleted from the codebase.

### Charging Station

One block, two charging paths, two power sources. It works with **any FE terminal from Refined Storage** (wireless grid, wireless crafting grid, portable grid, …) because it uses the generic NeoForge energy capability rather than RS's internal adapter.

| | Terminal Charging Station<br>`terminal_charging_station` |
|---|---|
| **Wired** (terminal in the GUI slot) | **3 slots** at **200 FE/s** each (**lossless**) |
| **Wireless** (players in range) | **20 FE/s** per device (radius **5 blocks**), at **45% efficiency**: drawing 100 only puts 45 into the device, the rest is lost |
| Wireless concurrency | up to **3 devices per player**, up to **3 players** (nearest first) |
| Base idle drain | **128 FE/s** (constant; charging billed separately) — a **redstone signal** shuts off wireless charging and pauses the idle drain |
| External FE buffer | 10000 FE |

The GUI is a **3×2 connection panel**: three wired slots up top (blue breathing light while charging, green when full) and three wireless link lights below (breathing while charging a player).

**The wireless on/off state is visible on the block itself**: the panel's lightning bolt glows cyan while wireless is on and dims to grey when a redstone signal turns it off — no need to open the GUI to see whether the wireless module is running.

| Power source | Notes |
|---|---|
| **RS network** (primary) | Like every other RS machine: the idle drain comes straight out of the network's `EnergyNetworkComponent` |
| **External FE buffer** (fallback) | Registers the standard `Capabilities.EnergyStorage.BLOCK` capability, so other mods' FE generators and cables can feed it — it works in pure-FE packs too |

Power is drawn **network first, buffer second**. The GUI shows the network energy bar.

> Every rate above is **per second (FE/s)**. Minecraft settles on **20 ticks = 1 second**, so the block spreads the per-second budget across the ticks of each second and carries the remainder over, which keeps the long-run average exact and never leaks energy. Per tick that works out to: wired 10, wireless 1 (per device).
>
> The base idle drain is per second too — otherwise a station would burn energy doing nothing, more than it can deliver when charging. It is deliberately high (128 FE/s) so that leaving the redstone switch off actually costs you: the switch is the way to save.

### Energy model

#### Distance tariff (all wireless terminals)

Every wireless terminal — this mod's grid and RS's own **wireless grid** and **wireless crafting monitor** — multiplies each action's energy cost by a **distance multiplier** (configured under `distanceEnergy`):

| Horizontal distance (player ↔ bound network) | Multiplier (default) |
|---|---|
| 0 – 10 blocks | **×1** |
| 10 – 100 blocks | **×2** |
| 100 – 1000 blocks | **×4** |
| over 1000 blocks (needs the Range Upgrade) | **×8** |
| cross-dimensional (needs the Interdimensional Upgrade) | **×1.5** |

Three energy upgrades form a non-wasteful nesting chain for this:

- **Energy Upgrade**: halves the distance multiplier (rounding up);
- **Advanced Energy Upgrade**: removes the distance multiplier (cross-dimension ×1.5 still applies);
- **Remote Energy Upgrade**: pays the bill straight out of the bound network, so the terminal's battery never drains — the distance penalty is effectively absorbed by network power.

`Energy Upgrade → Advanced Energy Upgrade → Remote Energy Upgrade`: each one is the crafting ingredient of the next.

#### Cost per action

| Situation | Where the power comes from | Cost per action (default) |
|---|---|---|
| Stock | The terminal's **own battery** (400 FE) | open 5 · craft 5 · autocraft 10 · clear matrix 5 · recipe transfer 5 |
| **Battery Upgrade** installed | Same, but capacity **+1000 FE per card** | unchanged |
| **Remote Energy Upgrade** installed | **Straight out of the bound network**; the terminal's battery is untouched. Falls back to stock behaviour if the network is empty or unbound. | unchanged |

The base battery is only **400 FE**, while the distance multiplier can push a single action into the tens or hundreds of FE — so the **Battery Upgrade is an early-game necessity**: without it the terminal runs dry fast once you wander away, forcing you to trudge back to a charging station.

### Requirements

- Minecraft **1.21.1**, NeoForge **21.1.x**, Refined Storage **2.0.7+** (verified on 2.0.9)
- Pure addon — no forking; one narrow mixin (wrapping `AbstractNetworkEnergyItem.use` so distance energy and upgrade cards also apply to RS's own wireless terminals), no permanent changes to your world (terminals keep their data components if you uninstall; they come back when you reinstall)

### Recipes

Most of the six items are late-game crafts, built around Refined Storage's own wireless hardware and nested storage parts plus the rarest vanilla drops. Only the **Battery Upgrade** and the **Terminal Charging Station** sit in the early-to-mid game, to keep the progression curve gentle.

<img src="docs/images/recipe-grid.png" width="230" alt="Wireless crafting grid recipe"> <img src="docs/images/recipe-battery.png" width="230" alt="Battery upgrade recipe"> <img src="docs/images/recipe-terminal-charging-station.png" width="230" alt="Terminal charging station recipe">

The **Battery Upgrade** is the cheap one — 1k storage blocks and redstone blocks are early-game materials, so the grid stops running dry long before the endgame cards are affordable. It is also the ingredient of the **Remote Energy Upgrade**, so the early investment is never wasted. The **Terminal Charging Station** is priced like RS's ordinary machines: it is infrastructure, not an upgrade.

<img src="docs/images/recipe-range.png" width="270" alt="Range upgrade recipe">

<img src="docs/images/recipe-interdimensional.png" width="270" alt="Interdimensional upgrade recipe">

<img src="docs/images/recipe-remote-energy.png" width="270" alt="Remote energy upgrade recipe">

The three endgame cards share one material language (nether star + 64k storage block), so they read as a set. The **Remote Energy Upgrade** is the one that talks about electricity rather than space, so its corners are **copper blocks** — the conductor — with a lightning rod down the middle; earlier builds used ender pearls there, which had no relation to power at all.

<img src="docs/images/recipe-terminal-charging-station.png" width="270" alt="Terminal charging station recipe">

The **Terminal Charging Station** is priced like RS's ordinary machines — quartz, redstone blocks, a machine casing and an advanced processor. It is infrastructure, not an upgrade.

### Range and dimensions

| Installed | Same dimension (bound to a Wireless Transmitter) | Cross-dimensional (bound to a Network Receiver) |
|---|---|---|
| — | `max(transmitter range, 64 blocks)` | unavailable |
| Range Upgrade | **unlimited** | unavailable |
| Interdimensional Upgrade | `max(transmitter range, 64 blocks)` | available (tunnel, inherently unlimited) |
| Both | **unlimited** | available |

Cross-dimensional access requires both the Transmitter and Receiver chunks to be loaded — an inherent constraint of RS's native link, faithfully preserved rather than worked around.

### How it works

**Grid and cards.** The core extension point is overriding `AbstractNetworkEnergyItem.use(ServerPlayer, ItemStack, SlotReference)` to inject a custom `ExtendedWirelessNetworkItemContext`. To make distance energy and upgrade cards work on RS's own wireless terminals (wireless grid, wireless crafting monitor) too, a single narrow mixin (`AbstractNetworkEnergyItemMixin`) wraps the very same 3-arg `use` and opens the same upgrade panel on sneak-right-click — this is the only place that touches RS classes, and it only wraps a parent method without changing any logic branch. RS's distance/dimension check lives in `NetworkItemPlayerValidator`, not in `resolveNetwork`, so upgrades apply as a **fallback relaxation** — the standard resolution runs first, and only when it comes back empty do the installed cards get a say. The `force=true` path is deliberately left untouched, since RS relies on that semantic to remove stale grid watchers. On the energy side, `isActive()` and `drainEnergy(long)` are overridden so the distance tariff is computed and applied to every drain, and the bound network answers "is there power?" and pays the bill when the Remote Energy Upgrade is installed.

**Charging Station.** RS 2.0.7 exposes no public block base class (blocks live in an internal, un-annotated module), so the block extends vanilla `BaseEntityBlock` and uses vanilla `getTicker`. Note that `BaseEntityBlock` returns `RenderShape.INVISIBLE` by default — vanilla's own block entities such as chests and furnaces draw themselves — so a block entity block that wants an ordinary json model has to override `getRenderShape()` and return `RenderShape.MODEL`, or it renders as nothing at all and you see straight through it. Fixed in 0.1.9. The block entity extends the STABLE-annotated `AbstractNetworkNodeContainerBlockEntity<T extends NetworkNode>`, with a minimal `ChargingStationNetworkNode` declaring the idle draw; RS's default six-way connection strategy means `createMainContainer` needs no override at all. Each tick it tops up the three wired slots, then scans nearby players (nearest first) and assigns the three wireless links, charging up to three devices per player. The six panel lights (3 wired + 3 wireless) are computed server-side and synced through `ContainerData`; the client drives the blue breathing pulse off `level.getGameTime()`, so it's purely visual with no extra packets. Item charging uses NeoForge's `Capabilities.EnergyStorage.ITEM` rather than RS's `PlatformImpl.getEnergyStorage` (which only recognises RS's own adapter), which is what makes it compatible with other mods' FE items.

**GUI backgrounds.** The custom screens use 176×166 backgrounds, so they must draw with the explicit-texture-size `blit` overload — the 7-arg `blit(loc, x, y, u, v, w, h)` silently assumes a 256×256 texture, which magnifies only the top-left corner of the background and made the slots look misaligned with an "invisible" inventory. Fixed in 0.1.10.

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
