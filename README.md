# Refined Storage - Wireless Crafting（无线合成终端）

[![Build](https://github.com/KokuuWork/rswirelesscrafting/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/KokuuWork/rswirelesscrafting/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/KokuuWork/rswirelesscrafting?label=release)](https://github.com/KokuuWork/rswirelesscrafting/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-3C8527)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-E8892B)
![Fabric](https://img.shields.io/badge/Fabric-1.21.1-AAA)
![Refined Storage](https://img.shields.io/badge/Refined_Storage-2.0.7%2B-3E7BB6)
[![Downloads](https://img.shields.io/github/downloads/KokuuWork/rswirelesscrafting/total?label=downloads)](https://github.com/KokuuWork/rswirelesscrafting/releases)
[![QQ群](https://img.shields.io/badge/QQ%E7%BE%A4%E5%8F%8D%E9%A6%88-498700382-12B7F5?logo=tencentqq)](https://qm.qq.com/q/ueYTU8T3xK)

**简体中文** | [English](#english)

> ## 🐞 出问题了？来 **Bug 维修 QQ 群：498700382**
> 崩溃、耗电异常、GUI 显示问题、联机/服务器故障，或者想要新功能——**直接进群说**就行（进群随便备注来意）。
> 👉 **[点击加入群聊（群号 498700382）](https://qm.qq.com/q/ueYTU8T3xK)** ｜ 也可以在 QQ 里**搜索群号 498700382** 找到我们。
> 不方便用 QQ 的话，欢迎直接提 [Issue](https://github.com/KokuuWork/rswirelesscrafting/issues)。

> 🚦 **这个模组的正文分成两批内容，请先分清你要的是哪一批**（截至 2026-10-15）
>
> | | 包含什么 | 怎么拿到 |
> |---|---|---|
> | ✅ **已上线** | 无线合成终端 · 6 张升级插件 · 终端充电站 · 按距离扣电 · 饰品栏兼容 | [Releases](https://github.com/KokuuWork/rswirelesscrafting/releases/latest) 下载 **0.3.2** |
> | 🧪 **未上线（预览）** | 无线样板终端 · 无线自动合成仓管理器 · 无线增幅塔 · 无线桥 · 无线配置卡 · 无线监控终端 · 手持提示 HUD | 只有开发版 `0.4.0-SNAPSHOT`，**需要自己从源码构建**（见「从源码构建」） |
>
> **🧪 未上线 = 已经写好、还没发版**，不是画饼，也不是另一个分支——代码就在 `main` 上，等测试通过后会
> 和已上线内容一起打进下一个正式版。**正文与内容一览里凡是标 🧪 的都属未上线，没标的都是下载就能用。**
>
> ⚠️ 整体仍处于测试阶段：**Fabric 侧为首次移植，尚未完成完整真机验证**。请在测试存档中使用并**先备份世界**；
> 遇到问题欢迎来上面的 QQ 群反馈（附日志与复现步骤）。

给 [Refined Storage 2](https://github.com/refinedmods/refinedstorage) 补上原版缺失的**无线合成**能力。

原版只有一个无线**查看**终端（`wireless_grid`）——你能远程看库存，但不能远程合成。本模组提供一个**无线合成终端**（远程访问网络并**远程合成**），用六张可插拔的**装配式升级插件**把它的续航、范围、维度与供能方式一路推到终局，再配一座**终端充电站**为所有 FE 终端补电。

另有六件 **🧪 未上线**的扩展——**无线样板终端**（远程写样板）、**自动合成仓管理器**、**增幅塔**、**无线桥**、**无线配置卡**、**监控终端**——目前**只在开发版里**，见文首那张对照表。所有无线终端（含 RS 原生的无线终端与无线自动合成监视器）都遵循**按距离扣电**的线性价目表，离网络越远耗电越狠。

![无线合成终端的未激活态与激活态，以及四张升级卡和终端充电站](docs/images/items.png)

> 上图中第 1、2 个图标是同一件**无线合成终端**的两种状态：未绑定（灰）与已接入网络（亮）。图中也画了**未上线**的几件（🧪），完整区分见下方「内容一览」。

## 内容一览

### ✅ 已上线（从 [Releases](https://github.com/KokuuWork/rswirelesscrafting/releases/latest) 下载 0.3.2 即可用）

| | 名称 | 作用 | 稀有度 |
|---|---|---|---|
| <img src="docs/images/item-grid.png" width="40" alt=""> | **无线合成终端**<br>`wireless_crafting_grid` | 远程访问网络并**远程合成**。潜行 + 右键打开插件装配界面。 | RARE |
| <img src="docs/images/item-battery.png" width="40" alt=""> | **电池升级插件**<br>`battery_upgrade` | 每张提升 **+1000 FE** 电池容量（可叠加）。前期刚需。 | RARE |
| <img src="docs/images/item-energy.png" width="40" alt=""> | **初级能耗插件**<br>`energy_upgrade` | **距离能量倍率减半**。也是高级能耗插件的原料。 | RARE |
| <img src="docs/images/item-advanced-energy.png" width="40" alt=""> | **高级能耗插件**<br>`advanced_energy_upgrade` | **完全消除距离能量倍率**（跨维倍率保留）。也是远程输电插件的原料。 | EPIC |
| <img src="docs/images/item-range.png" width="40" alt=""> | **距离升级插件**<br>`range_upgrade` | 同维度内**无视距离**。 | EPIC |
| <img src="docs/images/item-interdimensional.png" width="40" alt=""> | **维度升级插件**<br>`interdimensional_upgrade` | 解锁**跨维度**访问。 | EPIC |
| <img src="docs/images/item-remote-energy.png" width="40" alt=""> | **远程输电插件**<br>`remote_energy_upgrade` | **不再消耗终端自身电池**，直接花所绑定网络里的电。 | EPIC |
| <img src="docs/images/item-terminal-charging-station.png" width="40" alt=""> | **终端充电站**<br>`terminal_charging_station` | 3 个有线槽位快充，5 格范围内自动无线补电（**全局 3 个设备名额**）。 | — |

### 🧪 未上线（预览，目前只在开发版 `0.4.0-SNAPSHOT` 里）

| | 名称 | 作用 | 稀有度 |
|---|---|---|---|
| <img src="docs/images/item-wireless-pattern-grid.png" width="40" alt=""> | **无线样板终端** 🧪<br>`wireless_pattern_grid` | 远程编写**自动化样板**（合成 / 处理 / 切石 / 锻造），不用跑回样板网格。 | EPIC |
| <img src="docs/images/item-wireless-autocrafter-manager.png" width="40" alt=""> | **无线自动合成仓管理器** 🧪<br>`wireless_autocrafter_manager` | 远程管理**全网合成仓里的样板**：看有哪些、取出、换入，不用回基地。 | EPIC |
| <img src="docs/images/item-wireless-amplifier.png" width="40" alt=""> | **无线增幅塔** 🧪<br>`wireless_amplifier` | 覆盖半径 **64 格内**的无线终端**距离耗电打对折**。接网络常驻耗电，跨维度不享受。 | — |
| <img src="docs/images/item-wireless-bridge.png" width="40" alt=""> | **无线桥** 🧪<br>`wireless_bridge` | 未配对 = 基点、配对后 = 据点：把网络**延伸到远处**（可跨维度），自带区块保活（很贵）。 | — |
| <img src="docs/images/item-wireless-configuration-card.png" width="40" alt=""> | **无线配置卡** 🧪<br>`wireless_configuration_card` | 就地复制设置，**批量**刷给一片同类方块；站进增幅塔覆盖可扩大范围。 | — |
| <img src="docs/images/item-wireless-monitor.png" width="40" alt=""> | **无线监控终端** 🧪<br>`wireless_monitor` | 远程盯着几样东西：**越线提醒**，或直接**自动补货**（低于目标就发起合成）。 | EPIC |
| <img src="docs/images/item-overclock-upgrade.png" width="40" alt=""> | **超频升级卡** 🧪<br>`overclock_upgrade` | 插进 **RS 原版自动合成器**，每张把等待间隔**折半**（最多 4 张）。 | EPIC |

> 上面这六件（加上「手持提示 HUD」）**在开发版里已经能玩**，只是还没随任何正式版发布；
> 等真机测试通过，它们会和已上线的部分一起进下一个正式版。

所有鼠标悬停说明都是活的：终端会显示**当前绑定的维度与坐标**、**已安装的插件**，以及**当前距离与收费倍率**（同维度显示格数，跨维度单独标注，与实际扣费完全一致）；每张插件会显示自己的效果、生效前提，以及从配置里实时读出的数值（电池插件显示容量加成，维度插件显示当前跨维度能量倍率，初级能耗插件显示除数）。

## 升级插件是装配式的，可随时插拔

升级插件**不写死在合成配方里**。手持任意无线终端（本模组合成终端，或 RS 原生的无线终端 / 无线自动合成监视器）**潜行 + 右键**，打开「插件装配」界面——**4 个槽位**，把插件拖进去就装、拖出来就卸，终端本体不需要重新合成，合成矩阵、能量、网络绑定全部原样保留。

- 只有真正的升级插件能放进槽位（由物品标签判定），槽位内每格最多 1 张；
- 组件层面只在真的装了插件时才写入 `upgrades` 数据组件，拔空后组件会被移除，物品 NBT 保持干净；
- 拆下来的插件原样回到背包，不会蒸发。

> 这套装配界面取代了早期的「工作台无序合成永久改造」方案——那样装上去就拿不下来了。旧方案的自定义配方与序列化器已从代码中删除。

## 超频升级卡 🧪 预览·未发布

这件不是终端，而是一张**插进 RS 原版自动合成器**的升级卡：每张把合成器的**等待间隔折半**，最多 4 张。

**为什么 RS 本来做不到这件事**

RS 自带的速度升级卡有硬上限——4 张封顶（间隔压到 2 tick）。再塞第 5 张，它的档位表会掉进 `default -> 0` 分支，而那个 **0** 紧接着会撞上 `ticks % tickRate`，直接**除零崩游戏**。所以「更快」在 RS 里没有出口，我们只能绕开它的档位表。

**怎么绕的**

这张卡不去动 RS 的速度档位表，而是在它判断「这一 tick 能不能推进」的那一刻（`canStep` 读 `tickRate` 的瞬间）**把读到的间隔折半**。

- 只替换**这一个值**——机器是否激活、是否轮到链式合成器，那些判断**原样保留**。所以它只会让**本来就在工作的**合成器变快，不会让没电的机器凭空动起来。
- 注册用 RS **公开的**升级注册表，所以「槽位接受它、工具提示列出它、耗电算进总账」全由 RS 自己负责，我们不碰它的槽位逻辑。
- **绝不会制造 0 间隔**：折算结果最低是 1（= 每 tick 推进），所以那个除零陷阱绕不过去也不会踩到。
- 代价明码标在卡上：每张 **+32 FE/t**，由 RS 计进合成器的总耗电。

**一个诚实的说明**：为了保证「RS 将来改了字段名也**不会崩游戏**」，注入声明了允许失败——最坏情况是这张卡**没有效果**（而不是开不起游戏）。我们选择让它在出问题时安静地失效。

配方：`QAQ / SXS / QAQ`（石英富集铁 / 高级处理器 / **RS 的速度升级卡 ×2** / 下界之星）——它是「速度升级卡的升级版」，所以以速度卡为原料。

## 无线样板终端 🧪 预览·未发布

自动化做起来之后，最烦的一步不是合成，而是**改配方**——方块版的样板网格在基地里，矿场、下界、外岛想加一条产线就得来回跑。无线样板终端把这一步也搬到手上：**右键打开的是 RS 原汁原味的样板网格界面**（左侧资源栏、四种样板类型、JEI/REI 配方转移、样板复制全都在），但那座网格其实「装在你的终端里」。

- 界面与消耗：开界面 **5 FE × 距离倍率**；**产出一张样板 40 FE × 距离倍率**（按自动合成档计价，因为它服务的是自动化本身）。电量不足时样板不会产出（终端没电就配不了样板）。
- 「网格」本身是虚拟的：没有方块、不占区块、不用给它接线，登录进去就是它绑定的那张网络。
- 编辑状态**存在终端物品里**：配方区、样板类型、模糊模式、输出槽里没取走的样板，关掉界面再打开原样还在。
- 配方见下方「配方」一节，属终局件——它需要先有一台无线合成终端。

<img src="docs/images/recipe-wireless-pattern-grid.png" width="270" alt="无线样板终端配方">

## 手持提示（HUD） 🧪 预览·未发布

**默认关闭**（它属于「看一眼就好」的信息，不该常驻在屏幕上）。想要的话在配置里把「手持提示（HUD）」打开：手持**已绑定**的无线终端时，物品栏上方显示一行小字 **电量 · 距离 · 收费倍率**——出发前就知道这一趟要花多少电，不用打开背包悬停。打开任意界面时自动让位。

> 只认**手持**（主手 / 副手）。放饰品栏里不动手的时候不显示，避免屏幕上老是挂着一行字。

## 无线自动合成仓管理器 🧪 预览·未发布

写样板已经能远程了（上一节），但「这块样板该放进哪个合成仓、从哪个仓里取出来」原本还得跑回基地开方块版管理器。这件终端补上最后一步：**远程管全网合成仓里的样板**。

- 界面就是 RS 原版的自动合成仓管理器：按名字分组的仓、每个仓的样板槽、搜索模式全在，**本模组一行界面代码都没写**（菜单类型是 RS 自己的，客户端直接命中它已注册的界面）。
- 交互是真的：槽位直接对着**真实合成仓**的样板容器，取出来就进你背包，不会被吞。
- 耗电：开界面 **5 FE × 距离倍率**，与其它终端同一档。界面内移动样板不另计费——按次拦样板槽需要再给 RS 菜单加一处注入，而「哪些槽位算钱」是菜单特有的语义，先不做，避免造一个猜出来的抽象。
- 配方要两台**无线样板终端**夹住方块版管理器，属终局。

<img src="docs/images/recipe-wireless-autocrafter-manager.png" width="270" alt="无线自动合成仓管理器配方">

## 无线增幅塔 🧪 预览·未发布

距离耗电原本是纯粹的惩罚——直到有了塔。**无线增幅塔**接在 RS 网络上、常驻耗电（默认 **128 FE/s**，红石信号可临时停机），它罩着的 **64 格**范围内，所有无线终端的**距离倍率打对折**：本来 256 格要 ×16，塔下面就只算 ×8。

- **只影响同维度**：跨维度那笔固定 ×16 不给折扣——那条线留给维度升级卡，不然维度卡就没意义了。
- **是否在罩着，一眼可见**：塔在工作时信号弧亮青色，没接网络或被红石关掉就变暗；右键还能直接问它「你现在什么状态、半径多少、几折」。
- 它是**空间决策**：矿场、下界前哨、远海岛——值得为它们各建一座塔，还是就靠能耗插件硬扛？这就是这个 mod 想让你玩的取舍。
- 塔本身很贵（无线访问点 + 下界之星 + 2 个 64k 存储方块），而且**永远挂在网络上耗电**，别乱摆。

<img src="docs/images/recipe-wireless-amplifier.png" width="270" alt="无线增幅塔配方">

## 无线桥 🧪 预览·未发布

距离耗电还有一个更彻底的解法：**别让网络离你这么远**。无线桥是**一个方块、两种身份**：

- **没配对时它是「基点」**：接在 RS 网络里，什么都不做，只是「等着被指向」。
- **配对之后它是「据点」**：把自己挂到基点那张网络上——**网络就延伸到了这里**。于是旁边贴 RS 原生的外部存储 / 输入总线 / 输出总线就都能用，和拉一根线过来等价；**跨维度也一样**。

**怎么配对**（不用任何配对物品）：
1. 在主基地选中你网络里的一座桥，**潜行 + 右键**它 → 它成为基点（聊天栏会确认）。
2. 跑到远处（哪怕在别的维度）放另一座桥，**潜行 + 右键**它 → 配对完成，方块点亮。
3. 想拆掉这层关系：对据点**潜行 + 右键**即解绑。

**它替你做的事**
- **自带区块保活**：配对后**两端**区块自动强加载（可配开关）。RS 原生那对「网络变送器 / 网络接收器」要你自己保证两端加载，这是桥和它的主要区别。
- **跨维度免配对卡**：记的是坐标而不是视线，所以在下界、末地也能配对。
- **看得见的状态**：亮 = 已接入，灭 = 没连上（或还是基点）。直接右键它会告诉你连向哪里。
- **耗电是明码计价的，而区块保活是「奢侈品」**：桥本身 **64 FE/s**，**区块保活另算 512 FE/s × 每个被保活的区块**——配对通常保两端（2 个），所以一座跨维度桥 ≈ **1088 FE/s**，比同时开八座增幅塔还贵。我们不希望"保区块"变成无脑白嫖的默认服务：**这条远端的产线值不值得常年供着，交给你自己判断**。嫌贵就把 `chunkLoadingEnergyUsage` 调低（设 0 = 免费保活）。
- **红石开关**：给据点一个红石信号就**临时挂起**——停止搬运、停止耗电，但区块照旧保活（方块灯灭，右键会告诉你「已挂起」）。

**三条要留意的**（诚实写在前面）
- **欠费 / 挂起时断的是「链路」，不是「区块」**。区块一旦被放掉，远端方块实体就不再 tick，谁也放不掉它、也永远接不回来（死锁）。所以省钱只能靠断开搬运；供电恢复后下一秒会自动接上。右键会明确告诉你当前是「已挂起」还是「**欠费停运**」，两种情况区块都仍在保活。
- 已经保活的区块，只有**解绑或拆掉据点**才释放。配置里的开关是「准入」开关：关掉后不再新保活，但**不会**把已经保活的区块放掉——同样是为了不制造死锁。
- 卸载本模组之前，**请先拆掉已配对的据点**，否则区块强加载记录会留在存档里。

配方：`QWQ / RSR / QXQ`（石英富集铁 / 无线访问点 / **网络接收器 ×2** —— 桥的两端 / 下界之星 / 64k 存储方块）。

<img src="docs/images/recipe-wireless-bridge.png" width="270" alt="无线桥配方">

## 无线配置卡 🧪 预览·未发布

RS 原版配置卡很好用，但得一个个方块点过去——一排机器、一片产线改设置就是体力活。这张卡把**粘贴**变成范围操作：

- **右键方块** = 复制它的设置（和原版一样）。
- **右键另一个方块** = 粘贴给它。
- **潜行 + 右键** = 把设置**批量刷给半径内所有同类型方块**（基础半径 8 格）。
- **站进增幅塔的覆盖范围里，批量半径直接扩到塔的覆盖半径**（上限 16 格，避免一次点击扫爆区块）——增幅塔从此多了一个用途：「这片区域归我管」。

数据格式**直接复用 RS 原版的配置卡数据组件**，所以两种卡互相认：原版卡复制的设置我们能粘，反之亦然。

**两条说明**
- **不额外收费**：它搬运的是一次性设置，不像终端那样按次消耗。真正的成本是增幅塔的常驻耗电——没塔就只能小批量。
- **本版不搬升级卡**（原版卡粘贴时会顺带把升级卡挪过去）：我们没做，避免猜错它的消耗语义；要搬升级卡请用原版卡。

配方：`QAQ / WCW / QAQ`（石英富集铁 / 高级处理器 / **无线访问点 ×2** / 原版配置卡）。

## 无线监控终端 🧪 预览·未发布

前面几件终端都是「你想用时点开」，这件不一样：**你不看它的时候它在看——而且它能动手。**

把要盯的物品放进界面左边的槽、设好数字、选好动作，之后**只要这件终端带在身上**，它就一直替你看着那张网络。界面里每行实时显示**当前存量**，触发的那一行整行转红。

**一行规则 = 一个物品 + 一个数字 + 一个动作**

| 动作 | 触发条件 | 做什么 |
|---|---|---|
| **提醒** | 低于 / 高于数字 | 在聊天栏说一声（缺料告警 / 爆仓告警） |
| **补货** | 低于数字 | **自动发起合成**，把库存补回这个数字 —— **这是 RS2 本体没有的能力** |

**怎么用**
- 右键打开界面；把物品放进左侧槽位 = 开始监控它，取出 = 取消。
- **左键**点数字 = 加大一档，**右键** = 减小一档（档位是对数的：0 / 1 / 8 / 16 / 64 / 256 / 1K / 4K / 16K / 64K）。
- **Shift + 点击**数字 = 在「**低于**」与「**高于**」之间切换（只有「提醒」动作有意义）。
- **点击右侧的「提醒 / 补货」** = 切换动作。切到「补货」时方向会强制回到「低于」，因为「高于目标就补货」讲不通。
- 规则**存在终端物品上**，关掉界面再打开还在；界面上会锁住终端所在的那一格，防止编辑途中被挪走。

**「补货」为什么值得单独说一段**

RS2 原本没有任何「保持库存」机制——唯一的近似物是构造器上的一个策略，只在**构造器自己去拉物品**且缺料时才触发，而且**只补 1 个**。这件终端把它变成了正经的自动化：给网络设目标库存，缺了自动下单补回来，你出门在外它照样跑。

- **不会重复下单、也不会重复收费**：同类任务已经在跑时，RS 会告诉我们「已经在做了」，这时**既不下单也不扣费**；只有真的建起新订单才按**自动合成档**（40 FE × 距离倍率）收费。
- **没电就静静等着**：网络供电恢复后自动继续，不报警、不刷屏。
- **补不了会明确告诉你**：网络里没有能产出它的样板、或者材料不足，都会在聊天栏说一次（带冷却）。
- 老存档里的规则没有「动作」字段，会被当作「提醒」——不会因为这次更新失效。

**两条设计说明**
- **提醒不收费**，只有**打开界面**按 5 FE × 距离倍率计费。理由：提醒是被动读取，不像合成 / 造样板那样是玩家的主动操作；而且它的频率已经被限制住了（每 20 tick 检查一次，同一条规则触发后 60 秒内不重复提醒）。
- **与 RS 的检测器（Detector）不是一回事**：RS 那个是方块版的等级发信器，必须接在网络里、红石线还得拉到用它的地方，而且它**只能出红石信号、不能发起合成**。我们做的是**远程 + 主动执行**——人在地底挖矿，基地缺料它会自己补上。

配方：`QAQ / TMT / QXQ`（石英富集铁 / 高级处理器 / **存储监控器 ×2** / 无线合成终端 / 网络接收器）。

<img src="docs/images/recipe-wireless-monitor.png" width="270" alt="无线监控终端配方">

## 充电站

一座方块，两条充电路径，两种供电来源。**兼容 RS 的任意 FE 终端**（无线查看终端、无线合成终端、便携终端……），因为它走的是通用的 NeoForge 能量能力，而不是 RS 内部的适配器。

| | 终端充电站<br>`terminal_charging_station` |
|---|---|
| **有线**（终端放进 GUI 槽位） | **3 个槽位**，各 **200 FE/s**（**无损耗**） |
| **无线**（玩家在范围内自动补电） | **20 FE/s**（每个设备，半径 **5 格**），**45% 效率**：消耗 100 只给设备充 45，其余作传输损耗 |
| 无线并发 | **全局 3 个设备名额**，按离站距离由近到远分配：3 名玩家各充 1 个、1 名玩家独占 3 个 |
| 基础待机损耗 | **128 FE/s**（常驻，充电另算）——可用**红石信号**关掉无线充电并停扣待机损耗 |
| 内部电量缓冲 | **4000 FE**（默认，配置项 `externalCapacity` 可调） |

界面是一张 **3×2 连接面板**：上排 3 个有线槽（放物品，充电中蓝灯呼吸闪烁、充满绿灯），下排 3 个无线链接灯（正在为某名玩家充电时呼吸闪烁）。

**无线充电的开关状态直接写在方块外形上**：面板的闪电指示灯在无线开启时亮青色，被红石关掉后熄灭变灰——不用打开界面就能看出无线模块是否在工作。

| 供电来源 | 说明 |
|---|---|
| **RS 网络**（主来源） | 方块接入网络后，持续把网络电量**补进自己的内部缓冲**，补满即停 |
| **外部 FE**（兜底） | 注册标准 `Capabilities.EnergyStorage.BLOCK` 能力，其他模组用标准 FE 输电的方块/管道可以直接往缓冲里供电，纯 FE 整合包里也能用 |

界面能量条显示的是**充电站自己的缓冲电量**；待机损耗与有线 / 无线充电一律从这块缓冲里出账，缓冲没满时自动找网络补。

> 表里的速率一律是**每秒（FE/s）**。Minecraft 按 **20 tick = 1 秒** 结算，方块内部就把每秒额度摊到每 tick，除不尽的部分顺延到下一 tick，所以长期平均值精确、也不会漏电。折算成每 tick 是：有线 10、无线 1（每个设备）。
>
> 基础待机损耗同样按每秒算，否则会出现「站一空转，待机就把有线充电全吃掉」的倒挂。把它定在 128 FE/s，是故意「有惩罚感」——不关红石就一直空烧，拉了红石才省电。

## 能量模型

### 按距离扣电（所有无线终端通用）

所有无线终端——本模组的无线合成终端，以及 RS 原生的**无线终端**和**无线自动合成监视器**——每执行一次动作，消耗的能量都乘上一个**距离倍率**（配置在 `distanceEnergy` 节）：

| 水平距离（玩家 ↔ 绑定网络） | 倍率（默认） |
|---|---|
| 0 – 16 格（基础范围） | **×1** |
| 16 格以上 | 按距离**线性增长**：倍率 = 距离 ÷ 16 |
| 跨维度（需维度升级插件） | 固定 **×16** |

线性示例：32 格 ×2、64 格 ×4、256 格 ×16、400 格 ×25、1000 格 ≈ ×62。距离越远收得越狠，几百格外单次动作轻松破百 FE。

三段能耗插件就是为此准备的套娃链：

- **初级能耗插件**：距离倍率**减半**（向上取整）；
- **高级能耗插件**：距离倍率**归 1**（跨维度 ×16 仍保留）；
- **远程输电插件**：扣电直接**从绑定网络出账**，终端自身电池不再掉电，距离惩罚在实际上被网络电量抹平。

套娃不浪费：`初级能耗插件 → 高级能耗插件 → 远程输电插件`，每一级都是上一级的合成原料。

### 单次动作基础耗电

| 场景 | 电从哪来 | 单次动作耗电（默认） |
|---|---|---|
| 出厂状态 | 终端**自身电池**（容量 400 FE） | 开界面 5 · 合成 20 · 自动合成 40 · 清空矩阵 5 · 配方转移 5 |
| 装了**电池升级插件** | 同上，但容量 **+1000 FE / 张** | 同上 |
| 装了**远程输电插件** | **直接扣所绑定网络的能量**，终端自身电池不再掉电；网络无电或未绑定时自动回落成出厂行为 | 同上 |

> 🧪 未上线的**无线样板终端**同价目：开界面 5 · **产出一张样板 40**。它的「创建样板」按钮按下时会先查电量，付不起就当作没发生。

基础电池容量只有 **400 FE**，而距离倍率能把单次动作抬到几十甚至上百 FE——所以**电池升级插件是前期刚需**：不装它，走远一点终端很快就见底，只能反复跑回充电站。合成类动作（合成 20 / 自动合成 40）显著贵于查看（5），这正是合成终端相对普通无线终端的额外成本。

## 环境

| | 要求 |
|---|---|
| Minecraft | **1.21.1** |
| 加载器 | NeoForge **21.1.x**（1.21.1）｜ Fabric Loader **0.14.6+** |
| 前置 | Refined Storage **2.0.7+**（已在 2.0.9 上实测运行）；Fabric 侧另需 **Fabric API ≥ 0.101.0** + **Cloth Config** + **Team Reborn Energy** |
| 可选（饰品栏） | NeoForge：**Curios** + RS Curios Integration ｜ Fabric：**Trinkets** + RS Trinkets Integration |
| 构建 | JDK **21** + Gradle 8.14.4 |

## 安装

**NeoForge**：
1. 装好 NeoForge 1.21.1 与 Refined Storage。
2. 把 `rswirelesscrafting-neoforge-<version>.jar` 丢进 `mods/`。

**Fabric**：
1. 装好 Fabric 1.21.1、Fabric API 与 Refined Storage。
2. 把 `rswirelesscrafting-fabric-<version>.jar` 丢进 `mods/`，并确保已装 Cloth Config 与 Team Reborn Energy（通常随 Refined Storage 一并提供）。

两种加载器的 jar 都从 [Releases](https://github.com/KokuuWork/rswirelesscrafting/releases/latest) 下载，**二选一**（功能与配置完全一致，不要同时放进 `mods/`）。当前 0.3.2 对应 `rswirelesscrafting-neoforge-0.3.2.jar` / `rswirelesscrafting-fabric-0.3.2.jar`。

> 🧪 **想玩未上线的预览内容？**（无线样板终端 / 管理器 / 增幅塔 / 无线桥 / 配置卡 / 监控终端 / HUD）
> 它们**不在 Release 里**，只能自己构建开发版：`main` 分支的版本号是 `0.4.0-SNAPSHOT`，步骤见
> [从源码构建](#从源码构建)。构建产物文件名同样是 `rswirelesscrafting-<loader>-0.4.0-SNAPSHOT.jar`。
>
> **两个版本的 jar 不要同时放进 `mods/`**，也不要在同一个实例里混装。

> **升级注意**：NeoForge 只会更新配置文件的**注释**、不会覆盖你已经改过的值。跨版本升级后如果发现
> `config/rswirelesscrafting-common.toml` 里「注释写的默认值」和「实际值」不一致，删掉该文件让它重新生成即可。

> 本模组是**纯附属（addon）**：只依赖 Refined Storage 的公开 API，另用一处**窄 mixin**（只包一层 `AbstractNetworkEnergyItem.use` 的上下文，让距离耗电与升级插件对 RS 原生无线终端同样生效），不 fork、不魔改 RS 本体逻辑。移除本模组不会损坏存档（已装升级插件的终端会保留数据组件，重新装上即恢复）。

## 使用

1. **建好网络**。同一维度用无线访问点（Wireless Transmitter）；跨维度需要网络变送器 + 网络接收器（Network Transmitter + Receiver）。
2. **绑定**。手持无线合成终端，对着无线访问点或网络接收器**右键**完成绑定。
3. **装插件**。**潜行 + 右键**终端打开装配界面，把升级插件拖进 4 个槽位之一。想换就拖出来。
4. **打开**。右键终端即可打开合成界面（也可在按键设置里绑定快捷键）。
5. **补电**。终端充电站摆在基地里，3 个槽位有线快充，5 格范围内的玩家自动无线补电——走过去就回电。

### 饰品栏（可选）

无线合成终端可以放进**饰品栏**，与 Refined Storage 原生无线终端**共用同一组槽位**：

| 加载器 | 额外需要安装 |
|---|---|
| NeoForge | [Curios](https://modrinth.com/mod/curios) + [RS Curios Integration](https://modrinth.com/mod/refined-storage-curios-integration) |
| Fabric | [Trinkets](https://modrinth.com/mod/trinkets) + [RS Trinkets Integration](https://modrinth.com/mod/refined-storage-trinkets-integration) |

装好后终端会出现在整合模组提供的**两个专用槽位**里，按本模组的「打开无线合成终端」快捷键即可直接开启——饰品栏里的物品不支持右键打开，这是饰品栏模组的通用约定，RS 原生终端也一样。

没装整合模组时本模组照常工作，只是没有饰品槽；**不需要它们也能玩**。

> 兼容方式是**纯数据合并**：本模组只往整合模组的槽位 tag 里追加一条 `wireless_crafting_grid`，并用 `replace: false` 保证不会顶掉 RS 原生终端的条目。这与官方为 Quartz Arsenal 的无线合成终端做兼容用的是同一套做法。

## 配方

九件内容大多定位为**后期到终局**：材料取自 Refined Storage 自带的无线设备与递归存储元件、原版最稀有的掉落。只有**电池升级插件**、**初级能耗插件**与**终端充电站**刻意压低到前中期——它们负责把曲线撑平，剩下的都是终局件。

### 无线合成终端

<img src="docs/images/recipe-grid.png" width="270" alt="无线合成终端配方">

`Q` 石英富集铁 · `A` 高级处理器 · `P` 末影珍珠 · `G` `refinedstorage:crafting_grids` 标签（任意合成网格）· `M` 机器外壳

合计约 14 石英铁 + 2 末影珍珠 + 2 高级处理器 + 1 合成网格，约为 RS 自带「无线终端」的 **2.3 倍**——多出来的是「可合成」这份能力。

### 无线样板终端 🧪

<img src="docs/images/recipe-wireless-pattern-grid.png" width="270" alt="无线样板终端配方">

`Q` 石英富集铁 · `A` 高级处理器 · `T` 空白样板 · `P` 无线合成终端 · `M` 机器外壳

它是**唯一一件「用终端造终端」**的配方：中心那格要放一台做好的无线合成终端，前后用**空白样板**（RS 的样板基底）夹住——语义直白：这台终端是「写样板的终端」。定价与合成终端同档，属终局。

### 电池升级插件（前期刚需）

<img src="docs/images/recipe-battery.png" width="270" alt="电池升级插件配方">

`P` 末影珍珠 · `A` 高级处理器 · `R` 红石块 · `L` 1k存储方块

全表最便宜的一张。**1k 存储方块是 RS 存储元件递归链（1k → 4k → 16k → 64k）的入门级**，红石块是廉价的基础供能材料——两者都是早期就能量产的东西，所以这张插件在**游戏前期**就能做出来，让终端立刻从「开两次界面就没电」变成能长时间使用。

### 初级能耗插件（过渡件）

<img src="docs/images/recipe-energy.png" width="270" alt="初级能耗插件配方">

`P` 末影珍珠 · `C` 铜锭 · `R` 红石块 · `S` 基础处理器

距离耗电从基础范围外开始爬升后，这张插件把倍率**砍一半**。它是**高级能耗插件**的原料，前期做了到中期不会浪费。

### 高级能耗插件

<img src="docs/images/recipe-advanced-energy.png" width="270" alt="高级能耗插件配方">

`Q` 石英富集铁 · `P` 高级处理器 · `X` 4k存储方块 · `E` 初级能耗插件

彻底**消除距离倍率**（跨维度 ×16 仍保留）。它又是**远程输电插件**的原料——套娃链 `初级能耗插件 → 高级能耗插件 → 远程输电插件` 每一环都回收上一环，投入不打水漂。

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

## 多人 / 专用服务器

设计上所有判定都放在服务端，客户端只负责显示：

- **扣电、距离倍率、跨维度、插件生效**全部在服务端算；客户端 tooltip 只是把同一套公式在玩家自己身上复算一遍用于展示。
- **快捷键打开终端**走 RS 的 `useSlotReferencedItem`（内含 c2s 包），客户端无法伪造「我有终端」。
- **插件装配界面**的槽位引用（`SlotReference`）由服务端创建，并且会把终端所在的那一格**锁死**——玩家不能在界面里把它拿走或换掉，避免改动写进已脱离背包的物品。
- **充电站**的 tick 只在服务端运行；无线充电有全局 3 个设备名额，多人同时靠近时按离站距离由近到远分配，互不抢占。
- **RS 权限管理器**照常生效：没有 `OPEN` 权限的玩家打不开终端。

已知限制（不影响正确性，只影响显示）：配置注册为 NeoForge 的 `COMMON` 类型，**专用服务器不会把 `rswirelesscrafting-common.toml` 下发给客户端**。若服主改了默认数值（如距离基础范围、跨维度倍率），玩家 tooltip 上看到的倍率会是本地配置的数值，而实际扣费以服务端为准。两边都用默认值时完全一致。

## 架构

### 终端与升级插件

核心扩展点是覆写 `AbstractNetworkEnergyItem.use(ServerPlayer, ItemStack, SlotReference)`（RS 2.0.7 的 3 参重载），注入自定义的 `ExtendedWirelessNetworkItemContext`。

为了让距离耗电与升级插件也对 **RS 原生无线终端**（无线终端、无线自动合成监视器）生效，本模组用一处**窄 mixin**（`AbstractNetworkEnergyItemMixin`）在同一个 3 参 `use` 上包一层同样的上下文，并在潜行右键时打开同一个插件装配界面。这是唯一动到 RS 类的地方，且只在父类方法上包一层、不改任何逻辑分支。

RS 的距离/维度校验并不位于 `resolveNetwork`，而在 `NetworkItemPlayerValidator`。因此本模组采用「**标准逻辑优先、升级插件兜底放宽**」策略——先让 RS 原生校验跑完，仅在原生返回空时，才根据已安装的升级插件判断是否放宽。同时刻意**不干预 `force=true` 路径**，因为 RS 依赖该语义移除失效的网格 watcher。

能量侧的扩展点是 `NetworkItemContext.isActive()` 与 `drainEnergy(long)`：距离倍率在这里按「距离线性价目 × 能耗插件」算出并叠加到每次扣电上；装了远程输电插件时「有没有电」改由绑定网络回答，扣电也直接从 `EnergyNetworkComponent.extract` 出账。

### 充电站

充电站**不**沿用 RS 的方块基类——RS 2.0.7 没有公开的方块基类（方块全部在无 `@API` 标注的内部模块里），所以方块本身继承原版 `BaseEntityBlock`，tick 走原版 `getTicker`。

注意 `BaseEntityBlock` 默认返回 `RenderShape.INVISIBLE`（原版方块实体如箱子、熔炉都是自己画模型的），所以带方块实体又想用普通 json 模型的方块必须显式覆写 `getRenderShape()` 返回 `RenderShape.MODEL`，否则方块放下后完全不画、直接看穿到后面的世界。本模组在 0.1.9 修掉了这个坑。

方块实体则继承 RS 公开且标注为 STABLE 的 `AbstractNetworkNodeContainerBlockEntity<T extends NetworkNode>`，由它负责网络容器生命周期；自定义的 `ChargingStationNetworkNode` 只声明待机耗电。网络连接用的是 RS 的默认六向连接策略，因此连 `createMainContainer` 都不用重写。

每 tick 里服务端先为 3 个有线槽各自按速率补电；**无线扫描每 10 tick 一次**（按 10 tick 的额度一次性发放，速率不变）——玩家多、充电站多时，「每 tick 遍历附近所有玩家背包」是服务器上的真实热点，降频后开销约为原来的十分之一。扫描时把全局 3 个「设备名额」按「每名玩家先拿 1 个、有余再轮」的次序分给最近的玩家——3 名玩家各充 1 个设备、1 名玩家独占 3 个，充满一个自动轮到下一个。方块状态（通电 / 无线）的写入同样限流为 10 tick 一次，避免网络只剩几 FE 时缓冲在「空 / 有一点点」之间抖动，演变成每 tick 一次的方块更新与邻居红石更新风暴。连接面板的 6 个灯（3 有线 + 3 无线）由服务端算好状态写进 `ContainerData` 同步给客户端，客户端用 `level.getGameTime()` 驱动蓝灯的呼吸闪烁——纯视觉，不占额外网络包。多名玩家各自独占一个名额，状态互不干扰。

无线充电带 **45% 效率**（`deliver` 里按 `cost = ceil(given * 100 / efficiency)` 出账，消耗 100 只充 45）；有线恒为 100%。基础待机损耗（128 FE/s）只有在无线模块开启时才会扣，被红石信号关掉后停扣。方块带一个 `wireless` 方块状态，服务端在状态变化时 `setBlockAndUpdate`，用亮/灭两套纹理让外形直接反映无线是否开启。

物品充电在两加载器上分别走各自的**通用**能量能力——NeoForge 用 `Capabilities.EnergyStorage.ITEM`、Fabric 用 Team Reborn Energy 的 `EnergyStorage.ITEM`——**不**经过 RS 的 `PlatformImpl.getEnergyStorage`（那只会认 RS 自己的适配器），这样才能兼容其他模组的 FE 物品。

完整设计文档（含决策记录、逐文件实现规格、RS 2.0.7 API 核实结果）见 [`docs/DESIGN.md`](docs/DESIGN.md)。

## 从源码构建

```bash
git clone https://github.com/KokuuWork/rswirelesscrafting.git
cd rswirelesscrafting
export JAVA_HOME="/path/to/jdk-21"
./gradlew build
```

产物位于 `rswirelesscrafting-neoforge/build/libs/`（NeoForge）与 `rswirelesscrafting-fabric/build/libs/`（Fabric）。

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

## 反馈 / Bug 维修群

| 渠道 | 入口 |
|---|---|
| **QQ 群（推荐）** | 群号 **498700382** · 👉 [点击加入群聊](https://qm.qq.com/q/ueYTU8T3xK)（群名「MC-玩了X年服务器终于不想玩了」） |
| GitHub | [Issues](https://github.com/KokuuWork/rswirelesscrafting/issues) |

崩溃、耗电数值不对、GUI 显示异常、多人/服务器问题、新功能建议——**来群里直接说最快**，附上 `latest.log` 或崩溃报告就行。

## 更新日志

**未发布 · 0.4.0（开发中，预览）**：无线样板终端、无线自动合成仓管理器、无线增幅塔、无线桥（跨维度延伸网络 + 区块保活）、无线配置卡、无线监控终端、手持提示 HUD。**这些只存在于开发版 `0.4.0-SNAPSHOT`，尚未随任何正式版发布。**

最新已发布 **0.3.2**（2026-10-15，修复版）：修掉远程输电插件在网络电量不足时的**双重收费**、无线充电实际间隔 11 tick（速率被压低约 9%），并修正无线名额的过时文案。

上一版 **0.3.1**（2026-10-15）：**Fabric 移植**、**饰品栏兼容**（Curios / Trinkets）、距离耗电改**线性**（16 格内 ×1，超出按距离 ÷ 16）、跨维度 **×16**、合成 **20 FE** / 自动合成 **40 FE**、充电站改显自身缓冲并改为全局 3 设备名额、GUI 与 tooltip 修复、多人三项修复。

完整记录见 [CHANGELOG.md](CHANGELOG.md)。

## 许可

[MIT](LICENSE)

---

## English

> ## 🐞 Hit a bug? Join the **QQ support group: 498700382**
> Crashes, wrong energy numbers, GUI glitches, multiplayer/server trouble, or a feature request — come and say it
> in the group. 👉 **[Click to join (group 498700382)](https://qm.qq.com/q/ueYTU8T3xK)**, or search the group
> number **498700382** inside QQ.
> No QQ? Open an [Issue](https://github.com/KokuuWork/rswirelesscrafting/issues) instead.

> 🚦 **This readme covers two batches of content — know which one you want** (as of 2026-10-15)
>
> | | What is in it | How to get it |
> |---|---|---|
> | ✅ **Released** | Wireless Crafting Grid · the six upgrade cards · Terminal Charging Station · distance-based energy tariff · trinket support | Download **0.3.2** from [Releases](https://github.com/KokuuWork/rswirelesscrafting/releases/latest) |
> | 🧪 **Unreleased (preview)** | Wireless Pattern Terminal · Wireless Autocrafter Manager · Wireless Amplifier · Wireless Bridge · Wireless Configuration Card · Wireless Monitor Terminal · held-item HUD | Only in the dev build `0.4.0-SNAPSHOT`, which you have to **build from source** (see *Building*) |
>
> **🧪 unreleased means written but not shipped** — it is not vapourware and not a side branch; the code is on
> `main` and will land in the next release together with the released parts once it has been verified in game.
> **Anything marked 🧪 below is unreleased; anything unmarked is available right now from a release download.**
>
> ⚠️ As a whole this is still a testing build: **the Fabric port is brand new and has not gone through full in-game
> verification yet**. Use it in a test world and **back up your saves** first; bug reports are welcome in
> [Issues](https://github.com/KokuuWork/rswirelesscrafting/issues).

An addon for **[Refined Storage 2](https://github.com/refinedmods/refinedstorage)** that adds the wireless **crafting** grid the base mod never had.

Vanilla Refined Storage ships only a wireless *viewing* grid — you can browse your network remotely, but not craft on it. This mod adds a **Wireless Crafting Grid** (access your network **and craft** remotely), six swappable **upgrade cards** that extend its endurance, range, dimensions and power source all the way to endgame, plus a **Terminal Charging Station** that recharges every FE-based terminal.

Six further **🧪 unreleased** additions — a **Wireless Pattern Terminal**, a **Wireless Autocrafter Manager**, a **Wireless Amplifier**, a **Wireless Bridge**, a **Wireless Configuration Card** and a **Wireless Monitor Terminal** — currently exist **only in the dev build**; see the table at the top. Every wireless terminal (including RS's own wireless grid and wireless autocrafting monitor) follows a **distance-based energy tariff** — the further you are from the network, the more each action costs.

<img src="docs/images/items.png" width="620" alt="Wireless crafting grid (inactive and active), the six upgrade cards, and the terminal charging station">

> The first two icons are the *same* item — the **Wireless Crafting Grid** — in its unbound (dim) and network-connected (lit) states. The sheet also shows the **unreleased** additions (🧪); see *Contents* below for the exact split.

### Contents

#### ✅ Released (download 0.3.2 from [Releases](https://github.com/KokuuWork/rswirelesscrafting/releases/latest))

| | Item | Effect | Rarity |
|---|---|---|---|
| <img src="docs/images/item-grid.png" width="40" alt=""> | **Wireless Crafting Grid**<br>`wireless_crafting_grid` | Access your network remotely **and craft**. Sneak + right-click to open the upgrade panel. | rare |
| <img src="docs/images/item-battery.png" width="40" alt=""> | **Battery Upgrade**<br>`battery_upgrade` | **+1000 FE** capacity each (stacks). The early-game must-have. | rare |
| <img src="docs/images/item-energy.png" width="40" alt=""> | **Energy Upgrade**<br>`energy_upgrade` | **Halves** the distance energy multiplier. Ingredient of the Advanced Energy Upgrade. | rare |
| <img src="docs/images/item-advanced-energy.png" width="40" alt=""> | **Advanced Energy Upgrade**<br>`advanced_energy_upgrade` | **Removes** the distance multiplier (cross-dimension still applies). Ingredient of the Remote Energy Upgrade. | epic |
| <img src="docs/images/item-range.png" width="40" alt=""> | **Range Upgrade**<br>`range_upgrade` | **Unlimited range** within the same dimension. | epic |
| <img src="docs/images/item-interdimensional.png" width="40" alt=""> | **Interdimensional Upgrade**<br>`interdimensional_upgrade` | **Cross-dimensional** access. | epic |
| <img src="docs/images/item-remote-energy.png" width="40" alt=""> | **Remote Energy Upgrade**<br>`remote_energy_upgrade` | Draws power **from the bound network** instead of the terminal's own battery. | epic |
| <img src="docs/images/item-terminal-charging-station.png" width="40" alt=""> | **Terminal Charging Station**<br>`terminal_charging_station` | 3 wired fast-charge slots, plus a 5-block wireless aura (**3 device slots total**). | — |

#### 🧪 Unreleased (preview — dev build `0.4.0-SNAPSHOT` only)

| | Item | Effect | Rarity |
|---|---|---|---|
| <img src="docs/images/item-wireless-pattern-grid.png" width="40" alt=""> | **Wireless Pattern Terminal** 🧪<br>`wireless_pattern_grid` | Write **autocrafting patterns** (crafting / processing / stonecutter / smithing) remotely. | epic |
| <img src="docs/images/item-wireless-autocrafter-manager.png" width="40" alt=""> | **Wireless Autocrafter Manager** 🧪<br>`wireless_autocrafter_manager` | Manage the patterns inside **every autocrafter** remotely. | epic |
| <img src="docs/images/item-wireless-amplifier.png" width="40" alt=""> | **Wireless Amplifier** 🧪<br>`wireless_amplifier` | **Halves** the distance multiplier for terminals inside a **64-block** radius. A permanent load on the network; never applies cross-dimension. | — |
| <img src="docs/images/item-wireless-bridge.png" width="40" alt=""> | **Wireless Bridge** 🧪<br>`wireless_bridge` | Unpaired = anchor, paired = endpoint: extends the network **far away** (across dimensions) with chunk keeping (pricey). | — |
| <img src="docs/images/item-wireless-configuration-card.png" width="40" alt=""> | **Wireless Configuration Card** 🧪<br>`wireless_configuration_card` | Copy settings, then **paste them in bulk**; standing inside an amplifier widens the range. | — |
| <img src="docs/images/item-wireless-monitor.png" width="40" alt=""> | **Wireless Monitor Terminal** 🧪<br>`wireless_monitor` | Watch a few resources remotely: **alert on a threshold**, or have it **restock itself** (start a craft when you drop below the target). | epic |
| <img src="docs/images/item-overclock-upgrade.png" width="40" alt=""> | **Overclock Upgrade** 🧪<br>`overclock_upgrade` | Plugs into **RS's own autocrafter** and **halves** its waiting interval per card (up to 4). | epic |

> Those six (plus the held-item HUD) **are playable in the dev build** — they simply have not been shipped
> in any release yet. Once they pass in-game testing they will go out with the next release, alongside the
> released parts above.

Tooltips are live: the terminal shows the **bound dimension and coordinates**, its **installed upgrades**, and the **current distance and energy multiplier** (distance in blocks within a dimension, a separate label when cross-dimensional — always matching what the server bills); each card explains its effect, its prerequisite, and the values read from config in real time.

### The upgrade cards are pluggable

Cards are **not** baked into a crafting recipe. **Sneak + right-click** any wireless terminal (this mod's grid, or RS's wireless grid / wireless autocrafting monitor) to open the upgrade panel — **4 slots**. Drop a card in to install it, pull it out to remove it. The terminal is never re-crafted, and its crafting matrix, stored energy and network binding are all preserved.

- Only genuine upgrade cards fit (decided by item tags); one card per slot.
- The `upgrades` data component is written only when a card is actually installed, and removed once the last one is pulled — item NBT stays clean.
- Removed cards return to your inventory intact.

> This replaced the earlier "shapeless crafting-table, permanent modification" design, where an installed card could never come back out. Those custom recipe classes have been deleted from the codebase.

### Overclock Upgrade 🧪 preview (unreleased)

This is not a terminal but an upgrade card that plugs into **RS's own autocrafter**: each card **halves** the waiting interval, up to 4.

**Why RS cannot do this itself**

RS's own speed upgrade has a hard ceiling of 4 cards (interval down to 2 ticks). A fifth card makes its table fall into the `default -> 0` branch — and that **0** immediately meets `ticks % tickRate`, which is a **division by zero that crashes the game**. So "faster" has no outlet in RS; we have to go around its table.

**How it goes around**

Instead of touching that table, the card **halves the value of `tickRate` at the moment it is read** (inside `canStep`, when RS decides whether it may advance this tick).

- Only that **single value** is replaced — whether the machine is active, whether it is a chained autocrafter's turn, all of that is **left untouched**. So it only speeds up crafters that are **already working**; it will not make an unpowered machine move.
- Registration goes through RS's **public** upgrade registry, so "the slot accepts it, the tooltip lists it, the energy is accounted for" is all RS's own doing — we never touch its slot logic.
- It **can never produce a 0 interval**: the halving bottoms out at 1 (= advance every tick), so the division-by-zero trap is unreachable.
- The cost is printed on the card: **+32 FE/t** each, counted by RS into the crafter's total drain.

**One honest note**: to guarantee that a future RS rename **cannot break the game**, the injection is declared as allowed-to-fail — the worst case is that this card **does nothing**, rather than the game not starting. We deliberately chose quiet failure over a loud crash.

Recipe: `QAQ / SXS / QAQ` (quartz enriched iron / advanced processor / **two RS speed upgrades** / nether star) — it is the upgrade *of* that card, so that card is its ingredient.

### Wireless Pattern Terminal 🧪 preview (unreleased)

Once your base is automated, the annoying part is not crafting — it is **editing recipes**: the block Pattern Grid lives back at base, so every new production line in a far-away outpost means a trip home. The Wireless Pattern Terminal brings that step to your hand: right-clicking it opens **RS's own Pattern Grid UI** (resource list, all four pattern types, JEI/REI recipe transfer, pattern copying — everything), but the grid itself "lives inside your terminal".

- Costs: opening the UI is **5 FE × distance multiplier**; **producing one pattern is 40 FE × distance multiplier** (priced like autocrafting, because that is what it feeds). With too little energy no pattern is produced — no power, no pattern.
- The grid is virtual: no block, no chunk loading, no cables. You log into whatever network the terminal is bound to.
- The editing state is **stored on the item**: matrix, pattern type, fuzzy mode and any pattern still sitting in the output slot all survive closing and reopening the UI.
- It is an endgame craft (see Recipes) — you need a Wireless Crafting Grid first.

<img src="docs/images/recipe-wireless-pattern-grid.png" width="270" alt="Wireless pattern terminal recipe">

### Held-item HUD 🧪 preview (unreleased)

**Off by default** (it is a "nice to glance at" line and should not live on your screen). Turn on "Held-item HUD" in the config and holding a bound wireless terminal shows a single small line above the hotbar: **energy · distance · cost multiplier** — so you know what a trip will cost before you set out, without opening your inventory. It steps aside whenever a screen is open.

> Held items only (main hand / offhand). Terminals worn in a trinket slot do not show it, so no line is permanently stuck on screen.

### Wireless Autocrafter Manager 🧪 preview (unreleased)

Writing patterns remotely is covered above; what still forced a trip home was deciding **which autocrafter a pattern belongs in**. This terminal closes that loop.

- It opens **RS's own Autocrafter Manager UI** — crafters grouped by name, each one's pattern slots, search mode — with **not a single line of screen code in this mod** (the menu type is RS's, so the client lands on RS's registered screen).
- The interaction is real: the slots point at the **actual autocrafters'** pattern containers, so taking a pattern out really puts it in your inventory.
- Energy: opening it costs **5 FE × distance multiplier**, the same tier as every other terminal. Moving patterns inside is not billed per move — hooking pattern-slot edits would need another injection into RS's menu, and "which slots cost money" is menu-specific semantics; better to wait for a real need than to invent an abstraction.
- Crafting it takes two **Wireless Pattern Terminals** around a block-form manager — endgame content.

<img src="docs/images/recipe-wireless-autocrafter-manager.png" width="270" alt="Wireless autocrafter manager recipe">

### Wireless Amplifier 🧪 preview (unreleased)

The distance tariff used to be pure punishment — until the tower. The **Wireless Amplifier** sits on your RS network as a permanent load (**128 FE/s** by default, switchable off with redstone) and **halves the distance multiplier** for every wireless terminal inside its **64-block** radius: a spot that would cost ×16 at 256 blocks only costs ×8 under the tower.

- **Same dimension only**: the fixed ×16 cross-dimension price is never discounted — that line belongs to the Interdimensional Upgrade, otherwise the upgrade loses its point.
- **You can see whether it covers you**: the signal arcs glow cyan while it is working and dim when it has no network or is switched off by redstone; right-clicking it tells you its state, radius and discount.
- It turns distance into a **spatial decision**: is a far-away mine, a Nether outpost or a distant island worth its own tower, or do you just eat the cost with energy upgrades? That trade-off is what this mod wants you to play with.
- It is expensive (a Wireless Transmitter, a Nether Star and two 64k Storage Blocks) and it **never stops drawing power** — do not spam them.

<img src="docs/images/recipe-wireless-amplifier.png" width="270" alt="Wireless amplifier recipe">

### Wireless Bridge 🧪 preview (unreleased)

There is a blunter answer to the distance tariff: **do not let the network be that far away**. The Wireless Bridge is **one block with two roles**:

- **Unpaired it is an anchor**: put it on your RS network; it does nothing but wait to be pointed at.
- **Paired it is an endpoint**: it attaches itself to the anchor's network, so **the network is extended to this spot**. RS's own external storage, importers and exporters work right next to it — equivalent to running a cable there. **Across dimensions too.**

**Pairing** (no pairing item involved):
1. In your base, **sneak + right-click** a bridge on the network — it becomes the anchor (the chat confirms).
2. Place another bridge far away (even in another dimension) and **sneak + right-click** it — paired, and the block lights up.
3. To break the link, **sneak + right-click** the endpoint to unbind it.

**What it does for you**
- **Keeps chunks loaded**: once paired, **both ends** stay force-loaded (configurable). RS's own transmitter/receiver pair makes you handle that yourself — that is the main difference.
- **No network card needed across dimensions**: it stores a position, not a line of sight, so pairing works from the Nether or the End.
- **Visibly obvious**: lit = connected, dark = not connected (or still an anchor). Right-clicking it tells you where it points.
- **Energy is itemised, and chunk keeping is a luxury**: the bridge itself is **64 FE/s**, while **chunk keeping is billed separately at 512 FE/s per held chunk** (a pair usually holds both ends), so a cross-dimensional bridge runs around **1088 FE/s** — more than eight amplifiers at once. Keeping chunks loaded should not be a free default: **whether that distant production line is worth powering year-round is your call.** Find it too steep? Lower `chunkLoadingEnergyUsage` (0 = free chunk keeping).
- **Redstone switch**: a redstone signal on the endpoint **suspends** it — no hauling, no drain, chunks stay kept (the block goes dark and right-clicking says "suspended").

**Three honest caveats**
- **When unpaid or suspended the link drops, not the chunks.** Once a chunk is released the far block entity stops ticking and can never ask for it back (a deadlock). So saving power can only mean stopping the haul; the link reconnects by itself a second after power returns.
- A chunk already being kept is only released by **unbinding or breaking the endpoint**. The config switch is an *admission* switch: it stops new keeping but never drops a chunk that is already held — again to avoid a deadlock.
- Break (or at least unbind) paired endpoints **before uninstalling this mod**, otherwise the force-load records stay in the save.

Recipe: `QWQ / RSR / QXQ` (quartz enriched iron / wireless transmitter / **two network receivers** — the two ends of the bridge / nether star / 64k storage block).

<img src="docs/images/recipe-wireless-bridge.png" width="270" alt="Wireless bridge recipe">

### Wireless Configuration Card 🧪 preview (unreleased)

RS's own configuration card is handy, but you click one block at a time — reconfiguring a row of machines or a whole production line is pure labour. This card turns **paste** into a range operation:

- **Right-click a block** — copies its settings (same as RS).
- **Right-click another block** — pastes them into it.
- **Sneak + right-click** — pastes the settings into **every matching block in range** (8 blocks by default).
- **Stand inside an amplifier's coverage and the batch radius expands to the amplifier's radius** (capped at 16 blocks so one click cannot scan a whole chunk region) — giving amplifiers a second purpose: "this area is mine".

It **reuses RS's own configuration-card data component**, so the two cards understand each other: settings copied by RS's card can be pasted by ours and vice versa.

**Two notes**
- **No extra energy cost**: it moves settings once, it is not a per-use terminal. The real cost is the amplifier's constant drain — without one you only get a small batch.
- **Upgrade cards are not transferred** (RS's card moves them across as well): we skipped that rather than guess its consumption semantics; use RS's card if you need it.

Recipe: `QAQ / WCW / QAQ` (quartz enriched iron / advanced processor / **two wireless transmitters** / RS's configuration card).

### Wireless Monitor Terminal 🧪 preview (unreleased)

Every terminal so far is "open it when you need it". This one is different: **it watches while you are not looking — and it can act.**

Drop the resources you care about into the slots on the left, set a number, pick an action, and from then on — **as long as the terminal is on you** — it keeps an eye on that network. Each row shows the **current amount** live, and a row that has tripped turns red.

**One rule = an item + a number + an action**

| Action | Triggers when | What it does |
|---|---|---|
| **Alert** | below / above the number | says so in chat (low stock / piling up) |
| **Restock** | below the number | **starts a crafting job by itself** to bring the stock back to that number — **something RS2 cannot do on its own** |

**How to use it**
- Right-click to open it; put an item in a left-hand slot to start watching it, take it out to stop.
- **Left-click** a number to raise it one step, **right-click** to lower it (the steps are logarithmic: 0 / 1 / 8 / 16 / 64 / 256 / 1K / 4K / 16K / 64K).
- **Shift + click** the number to switch between **below** and **above** (only meaningful for the Alert action).
- **Click the "Alert / Restock" label** at the right of a row to switch actions. Switching to Restock forces the direction back to *below* — "restock when above the target" makes no sense.
- Rules are **stored on the item**, so they survive closing the UI; the slot holding the terminal is locked while editing so it cannot be moved mid-edit.

**Why "Restock" deserves its own paragraph**

RS2 has no way to keep a stock level at all — its closest relative is a constructor strategy that only fires when the **constructor itself** pulls an item, and then it crafts exactly **one**. This terminal turns that into real automation: set a target stock, and it orders more whenever you fall short, even while you are away from base.

- **It neither double-orders nor double-charges**: when a job for the same resource is already running, RS tells us so — we then **neither order again nor charge**. Only a genuinely new job is billed, at the **autocrafting rate** (40 FE × distance multiplier).
- **No power, no problem**: with the network flat it simply waits and resumes once power returns — silently, no chat spam.
- **When it cannot restock, you are told**: no pattern produces that item, or materials are missing — either way you get one message (rate-limited).
- Rules from older saves have no action field and are treated as **Alert**, so nothing breaks on update.

**Two design notes**
- **Alerts are free**; only **opening the UI** costs 5 FE × distance multiplier. Alerts are passive reads rather than a deliberate action like crafting or writing a pattern, and their rate is already bounded (checked every 20 ticks, and the same rule will not repeat within 60 seconds).
- **This is not RS's Detector**: that one is a block-form level emitter which must sit on the network and have redstone run to wherever you need it — and it can only emit a redstone signal, never start a craft. Ours is **remote and acts on its own** — you can be mining underground and the base restocks itself.

Recipe: `QAQ / TMT / QXQ` (quartz enriched iron / advanced processor / **two storage monitors** / wireless crafting grid / network receiver).

<img src="docs/images/recipe-wireless-monitor.png" width="270" alt="Wireless monitor terminal recipe">

### Charging Station

One block, two charging paths, two power sources. It works with **any FE terminal from Refined Storage** (wireless grid, wireless crafting grid, portable grid, …) because it uses each loader's generic energy capability (NeoForge `Capabilities.EnergyStorage.ITEM`, Fabric Team Reborn Energy) rather than RS's internal adapter.

| | Terminal Charging Station<br>`terminal_charging_station` |
|---|---|
| **Wired** (terminal in the GUI slot) | **3 slots** at **200 FE/s** each (**lossless**) |
| **Wireless** (players in range) | **20 FE/s** per device (radius **5 blocks**), at **45% efficiency**: drawing 100 only puts 45 into the device, the rest is lost |
| Wireless concurrency | **3 device slots total**, assigned nearest-first: 3 players get 1 each, 1 player gets all 3 |
| Base idle drain | **128 FE/s** (constant; charging billed separately) — a **redstone signal** shuts off wireless charging and pauses the idle drain |
| Internal energy buffer | **4000 FE** (default; configurable via `externalCapacity`) |

The GUI is a **3×2 connection panel**: three wired slots up top (blue breathing light while charging, green when full) and three wireless link lights below (breathing while charging a player).

**The wireless on/off state is visible on the block itself**: the panel's lightning bolt glows cyan while wireless is on and dims to grey when a redstone signal turns it off — no need to open the GUI to see whether the wireless module is running.

| Power source | Notes |
|---|---|
| **RS network** (primary) | The block tops its **internal buffer** up from the network and stops once full |
| **External FE** (fallback) | Registers the standard `Capabilities.EnergyStorage.BLOCK` capability, so other mods' FE generators and cables can feed the buffer directly — it works in pure-FE packs too |

The GUI's energy bar shows the station's **own buffer**. Idle drain and both charging paths are billed against that buffer, which refills from the network whenever it is not full.

> Every rate above is **per second (FE/s)**. Minecraft settles on **20 ticks = 1 second**, so the block spreads the per-second budget across the ticks of each second and carries the remainder over, which keeps the long-run average exact and never leaks energy. Per tick that works out to: wired 10, wireless 1 (per device).
>
> The base idle drain is per second too — otherwise a station would burn energy doing nothing, more than it can deliver when charging. It is deliberately high (128 FE/s) so that leaving the redstone switch off actually costs you: the switch is the way to save.

### Energy model

#### Distance tariff (all wireless terminals)

Every wireless terminal — this mod's grid and RS's own **wireless grid** and **wireless autocrafting monitor** — multiplies each action's energy cost by a **distance multiplier** (configured under `distanceEnergy`):

| Horizontal distance (player ↔ bound network) | Multiplier (default) |
|---|---|
| 0 – 16 blocks (base range) | **×1** |
| beyond 16 blocks | grows **linearly**: multiplier = distance ÷ 16 |
| cross-dimensional (needs the Interdimensional Upgrade) | fixed **×16** |

Linear examples: 32 blocks ×2, 64 blocks ×4, 256 blocks ×16, 400 blocks ×25, 1000 blocks ≈ ×62. The further you are, the steeper the cost.

Three energy upgrades form a non-wasteful nesting chain for this:

- **Energy Upgrade**: halves the distance multiplier (rounding up);
- **Advanced Energy Upgrade**: removes the distance multiplier (cross-dimension ×16 still applies);
- **Remote Energy Upgrade**: pays the bill straight out of the bound network, so the terminal's battery never drains — the distance penalty is effectively absorbed by network power.

`Energy Upgrade → Advanced Energy Upgrade → Remote Energy Upgrade`: each one is the crafting ingredient of the next.

#### Cost per action

| Situation | Where the power comes from | Cost per action (default) |
|---|---|---|
| Stock | The terminal's **own battery** (400 FE) | open 5 · craft 20 · autocraft 40 · clear matrix 5 · recipe transfer 5 |
| **Battery Upgrade** installed | Same, but capacity **+1000 FE per card** | unchanged |
| **Remote Energy Upgrade** installed | **Straight out of the bound network**; the terminal's battery is untouched. Falls back to stock behaviour if the network is empty or unbound. | unchanged |

The 🧪 unreleased Wireless Pattern Terminal shares the same tariff: open 5, **produce one pattern 40**. Its "create pattern" button checks your energy first — if you cannot pay, nothing happens.

The base battery is only **400 FE**, while the distance multiplier can push a single action into the tens or hundreds of FE — so the **Battery Upgrade is an early-game necessity**: without it the terminal runs dry fast once you wander away, forcing you to trudge back to a charging station. Crafting actions (craft 20 / autocraft 40) cost far more than viewing (5), which is the extra price of a crafting terminal over a plain viewing terminal.

### Requirements

- Minecraft **1.21.1**; **NeoForge 21.1.x** or **Fabric Loader 0.14.6+**; Refined Storage **2.0.7+** (verified on 2.0.9). On Fabric you also need Fabric API **≥ 0.101.0**, Cloth Config and Team Reborn Energy (normally bundled with Refined Storage).
- Optional, for a trinket slot: Curios + RS Curios Integration (NeoForge) or Trinkets + RS Trinkets Integration (Fabric).
- Pure addon — no forking; one narrow mixin (wrapping `AbstractNetworkEnergyItem.use` so distance energy and upgrade cards also apply to RS's own wireless terminals), no permanent changes to your world (terminals keep their data components if you uninstall; they come back when you reinstall).
- Download from [Releases](https://github.com/KokuuWork/rswirelesscrafting/releases/latest) — pick **one** jar for your loader, never both. When upgrading, delete `config/rswirelesscrafting-common.toml` if its commented defaults no longer match the actual values.

> 🧪 **Want the unreleased preview content?** (pattern terminal / manager / amplifier / bridge / configuration
> card / monitor terminal / HUD) It is **not in any release** — the `main` branch is versioned `0.4.0-SNAPSHOT`
> and you have to build it yourself (see *Building*). Never mix the release jar and the dev jar in one instance.

### Trinket / Curio slots (optional)

The wireless crafting grid fits into a **trinket slot**, sharing the same two slots as Refined Storage's own wireless terminals:

| Loader | Install additionally |
|---|---|
| NeoForge | [Curios](https://modrinth.com/mod/curios) + [RS Curios Integration](https://modrinth.com/mod/refined-storage-curios-integration) |
| Fabric | [Trinkets](https://modrinth.com/mod/trinkets) + [RS Trinkets Integration](https://modrinth.com/mod/refined-storage-trinkets-integration) |

Once installed the terminal appears in the two dedicated slots and opens with this mod's "open wireless crafting grid" keybinding — items in trinket slots are not right-clickable, which is the usual trinket-mod convention and applies to stock RS terminals as well. Without the integration mod everything still works, you simply have no trinket slot.

> This is a **pure data merge**: the mod appends a single `wireless_crafting_grid` entry to the integration's slot tag with `replace: false`, so Refined Storage's own terminals keep their entries. It's the same approach the official integrations use for Quartz Arsenal's wireless crafting grid.

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

### Multiplayer / dedicated servers

Everything that decides anything runs server-side; the client only renders:

- **Energy drain, distance tariff, cross-dimension handling and card effects** are all computed on the server. The client tooltip merely re-runs the same formula for the local player to display it.
- **The hotkey** goes through RS's `useSlotReferencedItem` (which contains a c2s packet), so a client cannot fake "I have a terminal".
- **The upgrade panel's** `SlotReference` is created server-side, and the slot holding the terminal is **locked** — the player cannot pick it up or swap it out while the panel is open, so edits can never land on a stack that already left the inventory.
- **The Charging Station** only ticks on the server. Its three wireless device slots are a global quota: when several players stand nearby they are assigned nearest-first and never steal each other's slot.
- **RS's Security Manager** still applies — players without the `OPEN` permission cannot open the terminal.

Known limitation (display only, not correctness): the config is registered as a NeoForge `COMMON` config, and **dedicated servers do not push `rswirelesscrafting-common.toml` to clients**. If an admin changes the defaults (base range, cross-dimension multiplier, …), players will see their own local values in the tooltip while the server bills by its own config. With default values on both sides everything matches.

### How it works

**Grid and cards.** The core extension point is overriding `AbstractNetworkEnergyItem.use(ServerPlayer, ItemStack, SlotReference)` to inject a custom `ExtendedWirelessNetworkItemContext`. To make distance energy and upgrade cards work on RS's own wireless terminals (wireless grid, wireless autocrafting monitor) too, a single narrow mixin (`AbstractNetworkEnergyItemMixin`) wraps the very same 3-arg `use` and opens the same upgrade panel on sneak-right-click — this is the only place that touches RS classes, and it only wraps a parent method without changing any logic branch. RS's distance/dimension check lives in `NetworkItemPlayerValidator`, not in `resolveNetwork`, so upgrades apply as a **fallback relaxation** — the standard resolution runs first, and only when it comes back empty do the installed cards get a say. The `force=true` path is deliberately left untouched, since RS relies on that semantic to remove stale grid watchers. On the energy side, `isActive()` and `drainEnergy(long)` are overridden so the distance tariff is computed and applied to every drain, and the bound network answers "is there power?" and pays the bill when the Remote Energy Upgrade is installed.

**Charging Station.** RS 2.0.7 exposes no public block base class (blocks live in an internal, un-annotated module), so the block extends vanilla `BaseEntityBlock` and uses vanilla `getTicker`. Note that `BaseEntityBlock` returns `RenderShape.INVISIBLE` by default — vanilla's own block entities such as chests and furnaces draw themselves — so a block entity block that wants an ordinary json model has to override `getRenderShape()` and return `RenderShape.MODEL`, or it renders as nothing at all and you see straight through it. Fixed in 0.1.9. The block entity extends the STABLE-annotated `AbstractNetworkNodeContainerBlockEntity<T extends NetworkNode>`, with a minimal `ChargingStationNetworkNode` declaring the idle draw; RS's default six-way connection strategy means `createMainContainer` needs no override at all. Each tick it tops up the three wired slots, and every 10 ticks it scans nearby players (nearest first) and hands out three device slots round-robin — three players charge one device each, a single player charges all three. The scan is deliberately throttled: on a busy server, walking every nearby player's inventory every tick for every station is a real hot spot, and charging the 10-tick budget in one go gives the same rate for a tenth of the cost. The block's powered/wireless states are also rate-limited to one write per 10 ticks, so a nearly-empty network (where the buffer flickers between "empty" and "a few FE") can't turn into a block-update and redstone-neighbour storm. The six panel lights (3 wired + 3 wireless) are computed server-side and synced through `ContainerData`; the client drives the blue breathing pulse off `level.getGameTime()`, so it's purely visual with no extra packets. Item charging uses each loader's generic energy capability (NeoForge `Capabilities.EnergyStorage.ITEM`, Fabric Team Reborn Energy) rather than RS's `PlatformImpl.getEnergyStorage` (which only recognises RS's own adapter), which is what makes it compatible with other mods' FE items.

**GUI backgrounds.** The custom screens use 176×166 backgrounds, so they must draw with the explicit-texture-size `blit` overload — the 7-arg `blit(loc, x, y, u, v, w, h)` silently assumes a 256×256 texture, which magnifies only the top-left corner of the background and made the slots look misaligned with an "invisible" inventory. Fixed in 0.1.10.

See [`docs/DESIGN.md`](docs/DESIGN.md) for the full design document.

### Building

```bash
git clone https://github.com/KokuuWork/rswirelesscrafting.git
cd rswirelesscrafting
export JAVA_HOME="/path/to/jdk-21"
./gradlew build
```

The jars land in `rswirelesscrafting-neoforge/build/libs/` (NeoForge) and `rswirelesscrafting-fabric/build/libs/` (Fabric).

All artwork is script-generated with the Python standard library (no Pillow): `gen_textures.py` (item and block textures), `gen_gui_textures.py` (176×166 GUI backgrounds) and `gen_readme_images.py` (the images above; needs `MC_CLIENT_JAR` and `RS_TEXTURES`).

### Feedback / support group

| Channel | Where |
|---|---|
| **QQ group (fastest)** | Group **498700382** · 👉 [Click to join](https://qm.qq.com/q/ueYTU8T3xK) |
| GitHub | [Issues](https://github.com/KokuuWork/rswirelesscrafting/issues) |

Bring a `latest.log` or crash report and the group is the quickest way to get a fix.

### Credits

Large parts of this project derive from [Refined Storage](https://github.com/refinedmods/refinedstorage) (MIT License, Copyright © 2020 - 2025 Refined Mods). The module layout follows the official addon example [Quartz Arsenal](https://github.com/refinedmods/quartz-arsenal).

Licensed under [MIT](LICENSE).
