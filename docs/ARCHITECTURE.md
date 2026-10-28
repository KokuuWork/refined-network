# 架构设计 — 六件新内容（0.5.0 → 0.7.0）

> 本文是 `docs/DESIGN.md`（玩法与数值规格）的**结构补充**：DESIGN 管「是什么」，本文管「怎么搭」。
> 六件内容一次开工，所以先把骨架定死，避免同一个模式复制六遍。

---

## 0. 结论先行

六件内容其实是 **两条线 + 一个共享骨架**：

- **能力线**（能远程做什么）：① 无线合成管理器 · ② 无线监控与提醒
- **接入线**（怎么把远处的东西接进来）：③⑤⑥ —— 它们不是三件东西，而是**同一对方块（无线桥）的三种取向**
- **工具**：④ 无线配置卡

其中 ③⑤⑥ 合并成一个方块家族是本轮最重要的简化，见 §3.3。

---

## 1. 现有可复用资产（别重写）

| 资产 | 位置 | 复用给谁 |
|---|---|---|
| 终端基类（绑定 / 插件 / 距离上下文 / 装配界面） | `common/terminal/AbstractWirelessTerminalItem` | ①② |
| 距离倍率 + 增幅塔折扣 | `ExtendedWirelessNetworkItemContext` + `AmplifierRegistry` | 全部 |
| 虚拟方块实体 + 反射菜单 | `common/patterngrid/*`（`VirtualPatternGrid` / `WirelessPatternGridMenuProvider`） | ① |
| 网络节点方块（手动扣电 + 10 tick 限流） | `common/chargingstation/ChargingStationBlockEntity` | ②③⑤⑥ |
| 零同步包的两端一致范式（登记表 + 读方块状态） | `common/amplifier/AmplifierBlockEntity` | ⑤⑥ |
| 手持 HUD 通道（默认关） | `common/hud/WirelessTerminalHud` | ② |
| 纹理 / GUI / README 三套生成脚本 | `gen_textures.py` / `gen_gui_textures.py` / `gen_readme_images.py` | 全部 |

---

## 2. Layer 0：基础设施（先做，六件全靠它）

### 0.1 内容注册脚手架 `common/content/`

**问题**：现在加一件内容要手工改 ≈ 12 处（`ContentIds` / `ContentNames` / `Items` / `Blocks` / `BlockEntities` / `Menus` / `CreativeModeTabItems` / 两个加载器的 `ModInitializer` / 两份 lang / 模型 / 配方）。六件内容 = 72 处手工改动，必然漏。

**做法**：只抽**最薄的一层**——把「定义」与「注册」分开：

- `ContentDefinition<T>`：id、中英名、`RegistryCallback` 注册体、创造栏位置、可选 tag（curios/trinkets）
- `ContentRegistry`：按定义表统一注册，两个加载器各只调一次 `registerAll(callback)`
- **现有内容逐步迁移，不做大爆炸重构**（先让新内容走新路，旧内容在下一次碰到时顺手迁）

> 目标：**新增一件内容 = 一个定义块 + 一份资产**，而不是十二处散点。

### 0.2 虚拟终端支撑 `common/virtual/`

| 类 | 职责 |
|---|---|
| `AbstractVirtualBlockEntity<T>` | 真实方块状态构造（RS 2.0.9 起校验 BE 类型 ↔ 方块状态，AIR 会抛 `Invalid block entity`）、子类覆写 `initialize(ServerLevel, Direction)` 为空以阻止注册幽灵节点、`setLevel` 登记、`setChanged` 钩子、NBT ↔ 物品数据组件 |
| `VirtualTerminalSession` | 会话结算：距离耗电、先看余额再扣（避免双重收费）、付不起一分不扣；**所有终端共用这一份**，否则距离倍率与取电顺序一旦分叉就会出现「同类操作收费不同」 |
| `VirtualMenuHolder`（接口）+ 两个加载器各一个 mixin | 让 RS 的**方块菜单**在「挂着我们会话」时判有效 |
| ~~`VirtualMenuCharger`~~ | **推迟到 ① 一起做**：按次计费要知道「哪些槽位算钱」，那是**菜单特有**的语义；先有真实消费者再定抽象，避免拍脑袋设计 |

> **泛化原则**：这一层只收「有第二个消费者」的东西。状态快照 / 回写（`serialize` / `restore`）暂时留在各自的方块实体里——目前只有样板终端用，等第二件真正需要时再抽。

**关键依据（已核实）**：RS 自己处理无线菜单就是这么干的——

```java
// RS 源码 WirelessAutocraftingMonitorContainerMenu
@Override
public boolean stillValid(final Player player) {
    return true;
}
```

方块版菜单用 `Container.stillValidBlockEntity(...)`（含 `level.getBlockEntity(pos) != be` 恒等检查，虚拟 BE 必然过不去）；RS 的**无线版一律 `return true`**。我们照抄这个范式，用 mixin 把 `stillValid` 注入到 RS 的方块菜单上——`stillValid` / `clicked` / `removed` 都是 **MC 自己声明的方法名**，所以 Fabric 的 mixin 注解处理器收得下（这正是 `createPattern` 报 `NO_OBFDATA_FOR_TARGET`、而它能过的原因）。

### 0.3 绑定解析 `common/link/BoundTargetResolver`

统一「一个 `GlobalPos` 指向什么」，三种解：

- **网络** → `be instanceof NetworkItemTargetBlockEntity` → `getNetworkForItem()`（STABLE API，网络接收器与无线访问点都实现）
- **容器** → `Container` 能力
- **能量** → FE 能力

④⑤③⑥ 全部只认这一套语义，和终端绑定完全一致。**玩家心智模型只有一条：绑定 = 我在用那个东西。**

### 0.4 链路与保区块 `common/link/LinkRegistry`

- 配对（纠缠对）注册表：按维度缓存、两端集合、失效坐标自清理（沿用 `AmplifierRegistry` 的写法）
- 区块强加载：`ServerLevel#setChunkForced`，**引用计数**（多对桥共享区块时不能互相拆掉）
- 🚨 **铁律：强加载不依赖能量**。断电 → 区块卸载 → BE 不再 tick → 永远回不来。只有拆方块才释放。
- 只服务端维护；客户端靠方块状态（`LINKED` 之类）判显示 → 延续「零同步包」范式

### 0.5 提醒通道 `common/notify/`

`NotifyCenter`（服务端收事件：缺料 / 超量 / 合成完成 / 桥断连）+ 复用 HUD 通道渲染。
**默认关、有事才出现、有配置开关**（用户既定规矩）。

---

## 3. 六件内容落骨架

### 3.1 ① 无线合成管理器 —— 最该做，且技术成本最低

写样板已经能远程了，但把样板**放进哪台合成器、从哪取出来**还得跑回基地。

| 项 | 结论 |
|---|---|
| 形态 | 物品终端（继承 `AbstractWirelessTerminalItem`） |
| 复用 | `common/virtual/*` 全套 |
| 新增 | `VirtualAutocrafterManager extends AutocrafterManagerBlockEntity` |
| **好消息** | `AutocrafterManagerBlockEntity.createMenu(int, Inventory, Player)` 是 **public**，内部自己从网络收集 `Autocrafter` 并分组 → **连反射都不用**（样板终端那条要反射，这条不用） |
| 数据模型 | 管理器**不持有状态**（`Autocrafter` 是 API 接口：`getAutocrafterName()` / `getPatternContainer()` / `getLocalPosition()`）→ 无需 NBT 回写，虚拟 BE 只提供「网络 + level」 |
| 计费 | 开界面 20 FE×倍率 + **每次改动样板槽 5 FE×倍率**（`VirtualMenuCharger` 拦 `clicked`） |
| 风险 | 中：`stillValid`（§0.2 有解）；`addWatcher` 是包级私有，但由 RS 菜单自己调用、我们的子类继承即可 |

### 3.2 ② 无线监控与提醒 —— 定位要改

⚠️ **更正上一轮的说法**：RS 的 `detector` **就是**等级发信器（`DetectorBlockEntity` 带 filter 容器 + `SingleAmountData` 阈值 + 红石模式）。所以「加一个等级发信器」是重复劳动。

**真正缺的是**：RS 的检测器必须贴脸配置、只能驱动红石、**你人在外面根本不知道**。

| 项 | 结论 |
|---|---|
| 定位 | **远程监控 + 主动提醒**：在手上维护「关注物料 + 阈值 + 方向」，触发时推提醒 |
| 形态 | 物品终端 + **自写菜单**（第一个不复用 RS 界面的终端） |
| 复用 | 终端基类 + `common/notify/*` |
| 好处 | 自写菜单 → **完全没有 `stillValid` 问题**，风险最低；同时也验证「自写界面」这条路线（以后 ④ 之类要用） |
| 呈现 | 默认关；触发才出（HUD 一行 / 聊天栏），阈值与当前量实时来自网络 |

### 3.3 ③⑤⑥ 合并：无线桥（一对方块，三种取向）★ 架构核心

**原本的三件内容**：
- ③ 无线外部存储总线（把远处箱子挂进网络）
- ⑤ 无线桥接核心 + 维度稳定器（跨维度直连 + 保区块）
- ⑥ 无线能量中继（给远处机器供电）

**合并后**：

```
无线桥 · 基点 (Bridge Anchor)  ──── 无线链路（可跨维度）────  无线桥 · 据点 (Bridge Endpoint)
     贴网络 / 网络接收器                                            贴什么，就传什么：
     （绑一次即可，和终端同一个绑定语义）                            · 贴容器   → 库存接入（原 ③）
                                                                    · 贴 FE 机器 → 能量中继（原 ⑥）
                                                                    · 悬空      → 纯网络延伸（原 ⑤）
```

**一个方块 + 它旁边是什么，决定它是什么。** 玩家不用学三套东西，符合「零配置、开箱即用」；
保区块（原「维度稳定器」）变成据点的**内建行为**（可配开关），不再是一独立物品。

| 项 | 结论 |
|---|---|
| 复用 | `common/link/BoundTargetResolver` + `LinkRegistry` + 网络节点方块范式 |
| 新增 | 基点/据点两格方块 + **网络入网方式**（唯一的硬骨头，见 §4） |
| 差异化 | RS 已有 Network Transmitter/Receiver（= 无线线缆，需网卡配对、需两端区块加载）。我们卖的是 **免卡配对 + 自动保区块 + 按距离耗电计价** |
| 定价 | 终局档（沿用 64k / 下界之星 / 无线访问点 那套材料语言） |

### 3.4 ④ 无线配置卡

| 项 | 结论 |
|---|---|
| 现状 | RS 有 `configuration_card`，**必须贴脸用** |
| 好消息 | `ConfigurationCardTarget` 是**公开 API 接口**（带 `writeConfiguration` / `readConfiguration`，RS 方块普遍实现），且有 `ConfigurationCardItem` 可作实现范本 |
| 做法 | 存一份配置；在无线范围（增幅塔 / 绑定网络覆盖）内读或写；潜行右键 = 批量刷半径内同类方块 |
| 成本 | 低（无新方块、无界面、无菜单） |

---

## 4. 唯一的硬骨头：**真实方块怎么无线加入一张网络？**

这是 ③⑤⑥ 的地基，开工前必须先打掉。

**已知事实**：
- 物品侧有公开路径：`NetworkItemContext.resolveNetwork(force)`（我们终端就在用）
- 方块侧公开 API 只有 `NetworkItemTargetBlockEntity.getNetworkForItem()` —— 那是把网络**给出去**，不是拿进来
- 虚拟 BE 上我们验证过 `mainNetworkNode.setNetwork(network)` 可用，但那是**脱离世界**的对象

### ✅ 已定案（spike 完成）：路线 A 走通，**全是公开 API，零 mixin、零反射、零 RS 源码改动**

RS 自己加节点就是两步（`NetworkBuilderImpl` 原文）：

```java
entryNode.setNetwork(newNetwork);
newNetwork.addContainer(entry);      // ← 网络靠这句才「看见」这个容器
```

`NetworkNode#setNetwork` 与 `Network#addContainer` / `removeContainer` 都在 API 里，节点容器还有公开工厂：

```java
RefinedStorageApi.INSTANCE.createNetworkNodeContainer(blockEntity, networkNode)
```

**「把远端容器接进来」的钥匙是 `StorageProvider`（公开 API）**，它的 javadoc 明写：

> Implement this on `NetworkNode`s that can provide a storage to the network…
> **This method is called when a `NetworkNode` is added or removed from a network.**

即：节点 `implements StorageProvider` 之后，网络在 `addContainer` / `removeContainer` 时**自动**把 `getStorage()` 纳入或移除 —— 我们**不需要手动碰 `RootStorage`**（javadoc 明确禁止）。

**目标网络的解析**（原本以为「没有按位置拿网络的 API」，现在有了绕法）：

```
绑定的 GlobalPos → level.getBlockEntity(pos)
  → ① instanceof NetworkItemTargetBlockEntity  → getNetworkForItem()                    （网络接收器 / 无线访问点）
  → ② instanceof NetworkNodeContainerProvider → getContainers() 任取一个 → getNode().getNetwork()   （任意 RS 网络方块）
```

两条都是公开 API，正好覆盖「绑到访问点」与「绑到网络里任一方块」两种玩法。

**唯一必须处理的注意点**：远端区块卸载 / 方块被拆时，**必须 `network.removeContainer(our)`**，否则网络里会留下幽灵容器（白占存储、白扣电）。③ 靠 `setRemoved()` 清理；⑤ 靠保区块 + 拆方块时释放。

**原候选 B（复用 Transmitter 链路）与 C（包装 RS 原生链路）不再需要。**

---

## 5. 施工顺序

| 阶段 | 内容 | 说明 |
|---|---|---|
| 1 | Layer 0：0.1 注册脚手架 → 0.2 虚拟终端支撑（顺手把样板终端迁上去）→ 0.3 / 0.4 / 0.5 | 骨架 |
| 2 | **§4 的 spike** | 决定桥的实现路线 |
| 3 | ① 无线合成管理器 | 验证 0.2 |
| 4 | ⑤ 桥骨架 → ③ 容器端 → ⑥ 能量端 | 一对方块三取向 |
| 5 | ② 监控提醒、④ 配置卡 | 一个自写界面 + 一件工具 |

每阶段结束都 `build` + 部署测试实例；**不攒大版本**。

## 6. 版本规划

- **0.5.0** = Layer 0 + ① 无线合成管理器
- **0.6.0** = 无线桥家族（③⑤⑥）
- **0.7.0** = ② 无线监控与提醒 + ④ 无线配置卡

---

## 7. 不做的（保持身份清晰）

空间存储 · 频道限制 · 无线权限管理器 · 无线流体终端 · 再加插件卡 / 新存储元件 / 新线缆 / 新机器。
理由见 README「内容地图」：这个 mod 的身份是「**远程有代价，代价可以用基建投资改善**」，与这条无关的东西加进来就是杂物箱。

---

## 8. 完成状态与发版清单（2026-10-15）

六件内容**全部实现完毕**，全部落在 `0.4.1-beta.1`（**已作为开发测试版发布**）：

| # | 内容 | 代码位置 | 状态 |
|---|---|---|---|
| ① | 无线自动合成仓管理器 | `common/autocraftermanager/` | ✅ 已实现（复用 RS 公开 `createMenu`，零反射） |
| ② | 无线监控终端 | `common/monitor/` + `common/notify/` | ✅ 已实现（本模组第一个自写界面） |
| ③⑤⑥ | 无线桥（一个方块两种身份，含保区块 / 计费 / 红石） | `common/bridge/` | ✅ 已实现 |
| ④ | 无线配置卡 | `common/configcard/` | ✅ 已实现（复用 RS 组件，与原版卡互通） |
| — | Layer 0 骨架 | `common/link/` `common/virtual/` `common/notify/` | ✅ 每块都有真实消费者了 |

**尚未真机验证**：以上全部只经过编译期与源码级审查。方向性判断（RS API 用法、虚拟方块实体、
`stillValid` 注入、`clickMenuButton` / `ContainerData` 通道、区块强加载）都是对着
`reference/rs2-v2.0.7` 逐条核实的，但**没有一次运行时证据**；Fabric 侧更是从未启动过。

**发版前清单（0.4.0 转正时逐条做）**

1. 两个 `build.gradle.kts` 的 `version`：`0.4.1-beta.1` → `0.4.0`。
2. README：去掉全部 **🧪** 标记；把「内容一览」的两张表（✅ 已上线 / 🧪 未上线）**合并回一张**。
3. README 顶部「两批内容」对照表整块删除，换回单条「当前版本 0.4.0」声明。
4. `RELEASE_NOTES.md` 按 0.4.0 重写（顶部 QQ 群区块保留）；`CHANGELOG.md` 的
   `## [0.4.0] - 未发布（开发中）` 改成带日期的正式标题。
5. 打 tag 前先 `git pull --rebase origin main`（用户会自己往 main 推东西）。
6. 用户升级时要提醒：**旧 jar 必须删掉**（同名不同版本会 duplicate mod 报错）。
7. Fabric 侧若仍未真机启动过，README 里「Fabric 首次移植未完整验证」那句**继续保留**。

---

## 9. 本批设备与 2026-10-28 修复记录

### 9.1 无线能量接收器（`common/energyreceiver/`）

- 普通 `BlockEntity` + 自建 `InWorldNetworkNodeContainer`，不继承 RS 自动入网的方块实体基类。
- 节点实现 `EnergyProvider`：RS 网络总电量由所有 provider 求和，`EnergyNetworkComponent` 只有 `extract`，因此接收器必须成为提供者，而不是调用不存在的 `insert`。
- 外部 FE 能力只负责填充缓冲；配置关闭或红石开启时，能力也拒绝接收，避免“停机时先充电、恢复后白送”。
- `bindTo`/`unbind` 先 `NetworkAttachment.detach()`，再清 target；旧网络不能留下幽灵 provider。

### 9.2 能量监控仪（`common/energymonitor/`）

- 只读，不注册节点、不提供能量或存储。
- 净收支通过两次采样之间的 `stored` 差值和真实 `gameTime` 差值计算；第一次采样只建立基线，断网/重绑时重置。
- 低电阈值和比较器用浮点中间值，避免 `stored * 100` / `stored * 15` 溢出。
- 配置总闸在 tick 最前面生效。

### 9.3 智能分拣站（`common/smartsorter/`）

- `EXACT` 用 `ItemResource` 精确匹配，`TAG` 用 `ResourceTag.resources()` 匹配。
- 标签模式按过滤槽维护轮转游标，避免总是搬列表中的第一种物品。
- GUI 按钮由 `SmartSorterScreen.mouseClicked` 发送原版 `handleInventoryButtonClick`，服务端 `clickMenuButton` 再校验距离与权限。
- 目标容器先检查 `canPlaceItem` 和 `getMaxStackSize(stack)`，再提取；部分插入时把差额退回网络。
- 放置者 UUID 存入 NBT；tick 和打开界面都走 `LinkSecurity`，离线时用 `PlayerSecurityActor` 复核。

### 9.4 网络桥接器（`common/networkbridge/`）

- 节点同时实现 `StorageProvider` 和 `EnergyProvider`，转发到绑定网络；插入/提取只在 `Action.EXECUTE` 时消耗物品限速额度。
- `BridgeRecursionGuard` 按当前服务器线程记录正在读取的网络，拒绝自指，检测互相桥接的环并停用本次绑定。
- 绑定和每次同步都检查 `EXTRACT` + `INSERT`；owner 存 NBT，在线走 `SecurityHelper`，离线走 `PlayerSecurityActor`。
- 欠费时不允许免费转发，断开链路并在后续同步周期重试。

### 9.5 配对、区块与平台

- NeoForge 用 `PlayerInteractEvent.RightClickBlock`，Fabric 用 `UseBlockCallback`：在原生 RS 网络方块上潜行右键即可写入 `PairingClipboard`。本模组方块跳过全局入口，交给自己的 `useWithoutItem`。
- 接收器/监控仪不再把自身当作 anchor；它们是普通 BE，`BoundTargetResolver` 无法把它们解析成网络。
- 无线桥重绑时先释放旧目标区块，再设置新 target；强制加载引用计数只在真正释放时归零。
- Fabric 能量适配器使用 `SnapshotParticipant<Long>`，事务嵌套/回滚会恢复缓冲。

### 9.6 验证基线

- `./gradlew -I init-mirror.gradle build`：NeoForge、Fabric、checkstyle 全部通过。
- 公共模块 `src/test/java`：7 个 JUnit 纯逻辑测试，覆盖 `RateMeter`、`RateLimiter`、`SaturatedMath`、`SorterMatchMode`；CI 不再 `-x test`。
- 当前仍未完成：Minecraft 实机、专用服务器、RS 安全网络、跨维度、旧存档迁移测试。
