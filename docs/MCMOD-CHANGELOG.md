# MC 百科「日志」板块材料

依据 [MC百科主站编辑规范 thread-646](https://bbs.mcmod.cn/thread-646-1-1.html) 中「适用于日志」一节：提交日志必须保留原文，翻译按一句原文一句译文对照。格式参照站内已有条目，例如 [冰火传说更新日志](https://www.mcmod.cn/class/version/770.html)。

使用方式：按版本分别提交。每个版本填写日期与版本号，条目按「英文原文一行、下方缩进中文译文一段」的格式录入。

## 2026-10-28

### 0.4.1

- added the Wireless Connector, a dedicated tool for binding block devices to a network

  添加无线连接器，用于将方块设备绑定到网络。

- added an on-block display to the Energy Monitor showing stored energy, net rate and time to empty

  能量监控仪方块上方新增显示，显示电量、净收支与预计耗尽时间。

- the Energy Monitor display faces the player, refreshes every 0.5 seconds and stays readable with shaders and HD font packs

  监控仪显示内容始终朝向玩家，每 0.5 秒刷新，在光影与高清字体资源包下均可辨认。

- added a particle effect on binding: rings at both ends and a spark line between them

  绑定成功时新增粒子效果：两端显示光环，同维度时两点之间有粒子连线。

- fixed a server crash when closing the Wireless Crafting Grid

  修复关闭无线合成终端时导致服务端崩溃的问题。

- fixed the Wireless Pattern Terminal closing immediately after opening

  修复无线样板终端打开后立即关闭的问题。

- fixed the Wireless Pattern Terminal showing an empty interface

  修复无线样板终端打开后界面空白的问题。

- fixed three wireless terminals having no energy capability, so they had no energy bar, could not be charged and refused to open

  修复三件无线终端没有能量能力的问题，此前它们没有电量条、无法充电、右键直接提示没电。

- fixed the Wireless Pattern Terminal and the Wireless Autocrafter Manager failing to open on Refined Storage 2.0.9

  修复在精致存储 2.0.9 下无线样板终端与无线自动合成仓管理器无法打开的问题。

- fixed the terminal charging station, wireless amplifier, wireless bridge, network bridge and smart sorter not connecting to cables

  修复终端充电站、无线增幅塔、无线桥、网络桥接器与智能分拣站无法连接线缆的问题。

- fixed the wireless bridge and the network bridge not being usable as access points

  修复无线桥与网络桥接器无法作为接入点使用的问题。

- fixed the Energy Monitor not reading a network when placed against a controller or cable

  修复能量监控仪贴着控制器或线缆放置时读不到网络的问题。

- changed the Wireless Energy Receiver to draw power from the bound network and feed the network it is placed against

  无线能量接收器改为从绑定的网络取电，供给其旁边那张网络。

- fixed the Wireless Energy Receiver charging itself when bound to the network it was placed against

  修复无线能量接收器绑定到自身旁边网络时会给自己充电的问题。

- fixed duplicate binding hints in item tooltips

  修复物品悬停说明中重复出现绑定提示的问题。

- added a chat message with dimension and coordinates when a terminal is bound

  终端绑定成功时在聊天栏提示维度与坐标。

- rewrote item and block descriptions in the wording used by Refined Storage

  按精致存储的措辞重写物品与方块描述。

- added a new icon for the Wireless Connector

  新增无线连接器的图标。

### 0.3.4

- fixed a server crash when closing the wireless crafting terminal with insufficient energy

  修复无线合成终端电量不足时关闭界面导致的服务端崩溃。

- the terminal no longer opens an empty interface when the battery cannot pay for opening it

  电量不足时不再打开空白界面，改为提示先充电。

- the terminal charging station can now be connected with cables

  终端充电站现在可以直接连接线缆。

### 0.3.2

- fixed the remote energy upgrade charging twice when the network did not have enough energy

  修复远程输电插件在网络电量不足时双重收费的问题。

- fixed the wireless charging scan running every 11 ticks instead of every 10

  修复无线充电扫描间隔为 11 tick 而非 10 tick 的问题。

## 2026-10-15

### 0.3.1

- added a Fabric build

  新增 Fabric 版本。

- added optional Curios and Trinkets support, so the terminal can sit in an accessory slot

  新增可选的 Curios 与 Trinkets 支持，终端可放入饰品栏。

- changed the distance cost from fixed tiers to a linear multiplier

  距离耗电由固定档位改为线性倍率。

- raised the cross-dimension multiplier to 16x

  跨维度倍率提高至 16 倍。

- changed wireless charging to allow three devices globally

  无线充电改为全场共 3 个设备名额。

## 2026-10-15

### 0.3.0

- added the distance-based energy cost model

  新增按距离计算的耗电模型。

- added six terminal upgrade cards

  新增六张终端升级插件。

- added the terminal charging station

  新增终端充电站。

- added redstone control to the charging station

  充电站新增红石控制。

## 2026-10-14

### 0.2.0

- reworked the charging stations into a single Terminal Charging Station with three wired slots and wireless charging

  将充电站重做为单一的终端充电站，提供 3 个有线槽与无线充电。

## 2026-10-14

### 0.1.10

- fixed the layout of all custom GUIs

  修复全部自定义界面的排版。

### 0.1.9

- fixed the charging station rendering as transparent after placement

  修复充电站放置后显示为透明的问题。

- changed charging rates to FE per second

  充电速率统一为每秒 FE。

### 0.1.5

- added the charging station, the battery upgrade and the remote energy upgrade

  新增充电站、电池升级插件与远程输电插件。

- changed upgrades from crafting ingredients to installable cards

  升级由合成材料改为可安装的插件。

### 0.1.0

- initial release with the wireless crafting grid, the range upgrade and the interdimensional upgrade

  首个版本，包含无线合成终端、距离升级插件与维度升级插件。
