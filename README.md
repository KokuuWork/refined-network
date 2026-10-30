# 精致网络 / Refined Network

<img src="docs/images/mod-icon.png" width="112" align="right" alt="Refined Network icon">

Minecraft 1.21.1 的精致存储 2（Refined Storage 2，简称 RS）附属模组，提供无线合成、样板编辑、库存监控及网络连接设备。

远程访问仍受绑定目标、插件、网络权限、供电和区块加载条件限制；终端动作费用随距离或跨维度倍率变化。项目计划逐步扩展为内容丰富的 RS 附属，但规划不等于已实现功能。

**[English](#english)** · [发行版本](https://github.com/KokuuWork/refined-network/releases) · [问题与建议](https://github.com/KokuuWork/refined-network/issues)

## 版本与名称

- **已发行**：0.4.1（正式发行线）、0.3.4（0.3.x 维护线）；0.3.2 保留为历史版本。发行状态以对应 Release 为准。
- **当前主线**：0.5.0，尚未发布。不要把主线源码或本地 JAR 当成已经发行并完整验证的版本。
- **显示名与注册标识**：精致网络 / Refined Network；modId、注册命名空间、包名与产物前缀均为 `refinednetwork`。本版直接重写旧 `rswirelesscrafting` 标识，**不提供旧存档 / 配置迁移**，旧世界必须新建。
- 历史发布记录称 0.4.1 在 NeoForge 验证过终端绑定、样板终端、接收器、监控仪显示屏和连接器；这不是所有功能、所有 RS 小版本或 Fabric 的完整验收证明。2026-10-30 文案复核未重新进行游戏测试。

详细历史见 [CHANGELOG.md](CHANGELOG.md)。开发任务、证据状态和依赖见 [docs/ISSUES.md](docs/ISSUES.md)。

## 安装与升级

| 环境 | 要求 |
|---|---|
| 共用 | Minecraft 1.21.1、Java 21、对应加载器的 Refined Storage 2 |
| NeoForge | 适用于 Minecraft 1.21.1 的 NeoForge；本项目开发基线为 RS 2.0.7 |
| Fabric | Fabric Loader、Fabric API、Cloth Config API、Team Reborn Energy；确切版本以目标发行包元数据为准 |
| 可选饰品支持 | Curios 或 Trinkets，加对应的 RS 饰品整合模组；需核对平台与版本组合 |

1. 按加载器下载 **一个**模组 JAR；不要混装 Fabric 和 NeoForge 产物。
2. 升级前备份世界和配置，在测试世界验证；移出旧 JAR 后再放入新 JAR，避免重复加载。
3. 自定义配置应与新默认值比较、按需迁移，**不要无条件删除配置文件**。
4. 本版重写了注册标识，旧 namespace 的物品、方块、数据组件与配方引用不会被转换。旧世界请继续使用旧 JAR，或新建世界后再升级。
5. 卸载前先处理已配对无线桥的区块保活；仅删除 JAR 不等于清除了存档中的加载记录。

元数据允许加载某个版本，不等于该版本已经实测兼容；不要将“RS 2.0.7 及以上”理解为无上限兼容承诺。

## 内容一览

以下前四类主体已在 0.4.x 出现；新增项单列为未发布内容。表中说明用途，不代表所有边界已验收。

### 手持终端

| 名称 | 用途与边界 |
|---|---|
| 无线合成终端 | 浏览网络、手动合成、配方转移和提交 RS 自动合成任务；合成矩阵保存在终端内 |
| 无线样板终端 | 远程编辑合成、处理、切石和锻造样板；复用 RS 样板界面，保留编辑状态 |
| 无线自动合成仓管理器 | 远程查看与移动网络合成仓内的样板；当前虚拟菜单激活状态需专项核实 |
| 无线监控终端 | 最多 4 条物品规则、库存高低阈值提醒和自动补货；要求玩家携带且终端被更新，不是离线托管服务 |

潜行并右键使用终端打开 4 槽插件装配界面。统一快捷键覆盖四类终端：合成 → 样板 → 管理器 → 监控，按此顺序取背包/饰品栏里第一件。手持状态提示（HUD）默认关闭，可在配置中开启。

### 终端插件

| 名称 | 作用 |
|---|---|
| 电池升级插件 | 增加终端容量，当前默认每张 +1000 FE |
| 初级能耗插件 | 降低同维度距离倍率，默认减半，最低 100% |
| 高级能耗插件 | 同维度距离倍率归 100%；跨维度倍率保留 |
| 距离升级插件 | 绑定无线访问点时放宽同维度距离限制；不是维度插件 |
| 维度升级插件 | 通过可用的网络变送器 / 接收器链路进行跨维度访问 |
| 远程输电插件 | 优先由绑定网络支付动作费用；当前不同终端的电池兜底行为仍需统一，见已知问题 |

这六种是本模组的终端插件，不等同于 RS 原生升级卡。**超频升级卡**是例外：装入 RS 自动合成仓，见下表。

### 方块设备与工具

| 名称 | 用途与边界 |
|---|---|
| 终端充电站 | 3 个有线槽与有限名额的无线充电；从网络补充内部缓冲，支持平台能量接口 |
| 无线增幅塔 | 工作范围内降低同维度终端距离费用；配置、红石和供电影响其状态 |
| 无线桥 | 基点 / 据点配对、远端连接和可选区块保活；邻接机器拓扑与重启恢复需专项验收 |
| 无线能量接收器 | 从绑定供电网络取电，经缓冲供给旁边的本地 RS 网络，不是向绑定网络送电 |
| 能量监控仪 | 读取网络电量、容量、采样净收支与预计耗尽时间；比较器输出 0–15 |
| 智能分拣站 | 按物品或标签向面前容器转移网络物品，受槽位、输入面、速率和权限限制 |
| 网络桥接器 | 向本地提供远端存储与能量的受限转发入口，不完整合并两网的安全或自动合成系统 |
| 无线连接器 | 记录 RS 网络接入点，绑定、重新绑定或解绑支持的设备 |
| 无线配置卡 | 复制设置，单点或范围内同类型批量粘贴；不转移升级卡，不支持任意模组机器 |
| 超频升级卡 | 缩短 RS 自动合成仓等待间隔，每张减半、最低 1 tick、最多 4 张；基础能耗每张 32 FE/t |

### 0.5.0 开发中，尚未发布

- **本地离线合成**：无可用网络时可退化为免费 3×3 合成台，使用背包材料；不等于离线自动合成或持久化订单。开关为 `wirelessCraftingGrid.offlineModeEnabled`。
- **无线红石端点**：一端读取信号，另一端输出 0 / 15；服务端每 2 tick 采样，支持跨维度，但不主动保活区块。配方一次制作两个。
- 离线窗口重连边界、红石端点权限 / 输入失效 / 掉落仍有待核实或修复项；不要在正式存档中无备份试用。

## 基本用法

### 终端绑定

手持终端右键一个可用于终端绑定的 RS 目标；同维度通常使用无线访问点，跨维度使用符合维度插件要求的网络接收器。目标必须可用，插件不代替权限、供电或区块加载。

绑定后右键空气打开；潜行并右键使用终端装配插件。未接入网络时，0.5.0 的合成终端可按配置进入本地离线模式，其他终端不因此获得离线功能。

### 用无线连接器绑定方块设备

1. 手持连接器，普通右键一个可解析网络的 RS 方块，记录接入点。
2. 普通右键支持绑定的设备，完成绑定；记录随后清除。
3. **没有接入点记录**时，右键已绑定设备会解绑；**已有记录**时，尝试重新绑定，不能把这两种操作混称为“一律解绑”。
4. 手持连接器潜行右键方块清除记录。

无线桥和网络桥接器本身也是连接器的目标：右键它们优先执行绑定 / 解绑，不保证能用同一手势将其记录为源。需要以无线桥基点配对时，先清除旧接入点记录，再使用已有的裸手潜行入口：潜行右键未配对基点记录，再潜行右键另一座未配对桥完成配对。已配对据点优先解绑，不用于这个记录手势；不要手持连接器潜行配对，否则会清除记录。

无线红石使用裸手潜行配对入口：先标记输入端，再配对输出端。它不是 RS 网络接入点，不使用连接器绑定。

### 无线能量接收器

将接收器放在需要用电的本地 RS 网络旁，再用连接器绑定到供电网络。可用缓冲向本地网络供电，链路费从供电方扣取；取电速率受容量、距离、整数取整和可用能量限制。绑定到同一张本地网络会停止链路，红石暂停接收并保留缓冲。

### 无线桥与区块保活

当前默认基费 64 FE/s，由已配对据点结算；开启保活时额外按其持有的两端保活记录计费，默认每记录 512 FE/s。同区块和多桥重叠的票据归属、费用与重启恢复需要专项验证，不能把默认公式当已验证的全部拓扑行为。

红石挂起、欠费或设备总开关关闭不立即释放已有保活。`chunkLoading=false` 当前阻止新增保活，不释放已有记录；解绑或拆除才走释放流程。请据此管理服务器，不把配置开关当作强载清理工具。

### 自动补货与配置复制

补货调用 RS 的 `ensureTask`，在提交需求时考虑 RS 在制任务量；是否新建任务依赖实际需求与 RS 返回结果，**不保证只要存在同类任务就永远不再建单**。需要可用样板、材料、权限及动作电费；成功提示表示已提交，不表示完成。

配置卡普通右键复制或粘贴；潜行右键向附近同类型 RS 方块批量应用，基础半径 8 格、最大 16 格、垂直上下各 4 格。现阶段没有差异预览或撤销，使用前确认范围。

## 费用与已知问题

以下是当前开发配置的默认值，不覆盖服务器自定义配置：

- 同维度水平距离 16 格以内倍率 ×1，超过后线性增长；跨维度默认 ×16。
- 打开、清空矩阵和配方转移默认 5 FE；手动合成 20 FE；自动合成或样板编码 40 FE，按对应动作路径计算。
- 终端不是按界面停留时间收费，但携带监控终端可在后台提交补货并产生动作费；不能写成“不打开终端就绝不耗电”。
- 方块持续工作费用和机器 FE/t 升级费用另计；显示提示使用本地配置，实际规则以服务端为准。

2026-10-30 代码审查发现的待处理项：虚拟终端网络欠费时电池兜底不一致、自动合成失败收费路径、样板回滚快照时机、菜单会话权限复核、管理器激活、桥接缓存主动更新、红石失效等。部分为明确调用路径问题，部分需实机复现，详细状态见 [任务清单](docs/ISSUES.md)。没有据此宣称已发生复制物品或存档损坏。

超频依赖 Mixin 和 RS 结构；`require=0` 不保证字段变化时安全失效，升级 RS 前需验证生产环境。已有构建或纯逻辑测试通过也不能替代游戏内功能、专服和迁移验收。

## 开发与反馈

需要 JDK 21。仓库根目录执行：

```bash
./gradlew build
# 如果常规依赖解析受网络影响，使用项目已有镜像方案：
./gradlew -I init-mirror.gradle build
# 仅运行公共模块的单元测试：
./gradlew :refinednetwork-common:test
```

Windows 可使用 `gradlew.bat` 对应命令。发行产物在两个平台模块的 `build/libs/`；不要把 `-sources.jar` 当游戏模组安装。

- 开发者和 AI 入口：[AGENTS.md](AGENTS.md)
- 可执行开发规范：[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)
- 当前架构与历史边界：[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- 文案术语与待核实项：[docs/TEXT_REVIEW.md](docs/TEXT_REVIEW.md)
- 实机验证清单：[docs/TESTING.md](docs/TESTING.md)
- 提交反馈：加载器、本模组及 RS 版本、步骤、实际 / 预期结果、日志；截图作辅助，日志注意移除私人信息。
- [GitHub Issues](https://github.com/KokuuWork/refined-network/issues)；既有 QQ 群号：498700382。

MIT 许可，见 [LICENSE](LICENSE)。

## English

Refined Network is a Refined Storage 2 addon for Minecraft 1.21.1. It adds wireless crafting, pattern editing, inventory monitoring and network devices. Access still requires a valid target, appropriate upgrades, permission, energy and loaded chunks. The project may grow into a broader RS addon; planned content is not shipped functionality.

### Version and identity

- Published lines: **0.4.1**, **0.3.4**; **0.3.2** is retained for history. **Main is the unpublished 0.5.0 development version.**
- Display name and registry identity: **Refined Network**, with mod ID, namespace, packages and artifact prefix all **`refinednetwork`**. The old `rswirelesscrafting` identity was rewritten without a migration path; existing worlds must not be upgraded in place.
- Original 0.4.1 notes report limited NeoForge testing. They do not prove complete Fabric, server or future RS-version compatibility. No new in-game testing was performed for this text review.

### Installation

Use Minecraft 1.21.1, Java 21 and the matching loader build of RS 2. Development targets RS 2.0.7; permissive dependency metadata is not a promise of unlimited compatibility. Fabric additionally requires Fabric API, Cloth Config API and Team Reborn Energy according to the release metadata. Curios / Trinkets require their matching RS integrations and version checks.

Install **one** loader-specific JAR. Back up saves and configurations before upgrading, remove the old JAR and test the new build separately. Compare customized configurations rather than deleting them unconditionally. An eventual namespace migration may affect saved items, blocks and components; lossless old-save migration has not been promised or implemented.

### Content and use

- Terminals: **Wireless Crafting Grid**, **Wireless Pattern Terminal**, **Wireless Autocrafter Manager**, **Wireless Monitor Terminal**. The shared hotkey covers all four terminals, trying crafting, pattern, manager and monitor in that order. The held-item HUD is optional and off by default.
- Terminal upgrades: battery, basic / advanced energy, range, interdimensional and remote energy, fitted in four slots. Remote energy prefers the bound network; battery fallback differs across current terminal paths and is tracked for correction.
- Devices: charging station, amplifier, wireless bridge, energy receiver, energy monitor, smart sorter and network bridge. Tools include the wireless connector, configuration card and RS autocrafter overclock upgrade.
- The energy receiver takes energy **from the bound supply network into its buffer and supplies the adjacent local RS network**. The network bridge proxies remote resources under rate and access constraints; it does not merge every subsystem of two networks.

Right-click an eligible RS target while holding a terminal to bind it. Use the required access-point / receiver target and upgrades; right-click air to open, sneak-use the terminal to fit upgrades.

With the connector, right-click an RS network block to record a source, then right-click a supported target device to bind. With no source recorded, right-clicking a bound device unbinds; with a source recorded it attempts rebinding. Sneak-right-click clears the record. A bridge is itself a connector target, not an unconditional source for that gesture; clear any old source record and use the existing empty-hand sneak path on an unpaired anchor and then an unpaired second bridge. An already paired endpoint is unbound instead. Redstone endpoints also use their empty-hand sneak pairing path, not the connector.

Monitoring runs while a carried terminal is ticked; it is not an offline hosted service. Restocking uses RS task accounting, not a blanket promise of one task per item. A success message means submission, not completion. Configuration cards already support same-type bulk pasting; they do not move upgrades and currently have no preview or undo.

### Unpublished 0.5.0 additions and limits

Local offline crafting uses inventory ingredients in a free 3×3 crafting table. It is not an offline order queue or remote autocrafting. Wireless redstone endpoints relay 0 / 15 with a 2-tick server sample interval, including across dimensions; they do not keep chunks loaded. Offline reconnection, endpoint permissions, source invalidation and drops require further work or verification.

Default action costs are 5 FE to open / clear / transfer recipes, 20 FE to craft and 40 FE to submit autocrafting or encode patterns. Same-dimension distance above 16 blocks increases the multiplier; cross-dimensional use defaults to ×16. Actual rules are server-authoritative while current hints use local configuration. A carried monitor may spend energy on restocking even while its screen is closed.

Wireless bridge base cost defaults to 64 FE/s, paid by the paired endpoint, plus 512 FE/s per retained chunk record when enabled. Existing records survive suspension / loss of energy; disabling new chunk keeping does not clear retained records. Ticket overlap and restart recovery need verification. Unbind or remove bridges before uninstalling, and verify the world state.

Tracked problems include inconsistent virtual-terminal fallback, failed-task billing, pattern rollback, session validation, manager activation and bridge cache refresh. Some are code-confirmed paths, others require reproduction. Mixin `require=0` is not a safety guarantee after RS changes.

See [CHANGELOG](CHANGELOG.md), [development rules](docs/DEVELOPMENT.md) and [tracked tasks](docs/ISSUES.md). Build with JDK 21 using `./gradlew build` or the documented mirror initialization above. Report loader / mod / RS versions, repro steps and sanitized logs via [Issues](https://github.com/KokuuWork/refined-network/issues). MIT licensed.
