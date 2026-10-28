# 设计文档：无线合成终端附属模组

> **历史设计记录（0.1.x）**：本文保留早期方案的推导过程，不再作为当前实现规格。当前代码以 `docs/ARCHITECTURE.md`、`docs/AUDIT_FULL_2026-10-28.md` 和 `CHANGELOG.md` 为准。文中的 NeoForge-only、无 mixin、0.1.x 版本、旧能耗单位、旧充电站分档和旧配方均已过时；Fabric、超频 mixin、网络桥接器和新增设备的现行行为请以源码为准。

## 无线合成终端 + 四张装配式升级卡 + 充电站（Refined Storage 2 · NeoForge 1.21.1）

> **文档用途**：本报告是一份**自包含**的完整实现规格，供代码生成模型 / 开发者在**不依赖本对话上下文**的前提下，直接据此编写代码。所有关键类名、方法签名、扩展点、平衡数值均已核实到 Refined Storage 2（下称 RS 2）与官方附属 Quartz Arsenal 的真实源码。
>
> **版本对应**：§1–§13 为 **0.1.0** 的首版规格（终端 + 范围卡 + 维度卡）。**0.1.5** 新增的充电站、电池升级卡、远距离输电升级卡与「装配式升级卡」改造见 **[§14](#14-015-增量充电站--电池卡--输电卡--装配式升级)**，该节同时修正了 §2 / §12 中被实现推翻的若干条。

---

## 1. 项目概览

| 项 | 内容 |
|---|---|
| 定位 | RS 2 的**纯附属模组**（addon），不 fork、不 mixin RS 本体 |
| 目标 MC | **1.21.1** |
| 加载器 | **NeoForge**（21.1.x） |
| 依赖 | Refined Storage **2.0.x**（1.21.1 线，写作基线 **v2.0.0**，即 1.21.1 最新 release） |
| 参照源码 | Quartz Arsenal **v1.0.8**（1.21.1 线，MIT） |
| 核心产出 | ① 无线合成终端 ② 范围升级卡 ③ 维度升级卡（共 3 个 Item）；**0.1.5 追加**：④ 电池升级卡 ⑤ 远距离输电升级卡 ⑥ 充电站方块 |
| 差异化 | ① 同维度无线距离可加宽（默认 64 格，可配）；② **范围卡**→同维度无限范围；③ **维度卡**→解锁跨维度，走 RS 原生 Network Transmitter/Receiver 隧道（网卡绑 Receiver）；④ **输电卡**→终端不耗自身电池，直接花网络电；⑤ **电池卡**→扩容 + 输电卡原料；⑥ **充电站**→为 RS 全部 FE 终端提供有线/无线充电，且接受外部标准 FE 供电 |

**背景事实（已核实）**：
- 原版 RS 2 本体没有无线合成终端（只有 Wireless Grid 查看终端）；官方附属 Quartz Arsenal 补齐了无线合成终端，但仍是「同维度 + 发射器范围（默认 16 格）内」。
- RS 2 的 Network Transmitter/Receiver 是**原生跨维机制**：Receiver 把源网络暴露到目标维度，是 `NetworkItemTargetBlockEntity`（**网卡原生可绑 Receiver**）。硬约束是**两端区块都要加载**。
- 本 mod 在终端侧同时解决「更远」与「跨维度」，跨维度走**原生隧道**而非直连，忠实于 RS 平衡设计。

---

## 2. 决策记录（ADR 摘要）

| # | 决策项 | 结论 |
|---|---|---|
| D1 | 目标版本/加载器 | 1.21.1 / NeoForge（RS 2.0.x） |
| D2 | 实现架构 | **纯附属**（依赖 RS 公开 API 模块），不 fork、不 mixin |
| D3 | 物品清单 | 无线合成终端 + 范围升级卡 + 维度升级卡（**3 个 Item**，无创造版）→ **已扩至 6 件**，见 §14 |
| D4 | 跨维度机制 | **方案 B（隧道）**：网卡绑 Network Receiver，走 Network Transmitter/Receiver 隧道 |
| D5 | 卡片拆分 | **范围卡**只扩同维度距离；**维度卡**只解锁跨维度。两张卡正交、互不干扰 |
| D6 | 能量模型 | 终端耗 FE、可配置关闭；跨维度时能量消耗 +50% |
| D7 | 槽位 | 1 网卡槽 + 4 升级槽；两种卡各 1 张即生效，可堆叠 → **升级卡最终定为装配式、可插拔**，见 §14.4 |
| D8 | 命名 | modid 占位 `rswirelesscrafting`（可改，见 §4.1） |
| D9 | 同维度距离 | 终端有效距离 = `max(发射器范围, extendedRange)`，`extendedRange` 默认 **64 格**、可配置；范围卡 → 无限 |
| D10 | 区块加载 | 同维度：仅家庭网络侧（发射器）加载；跨维度：**Transmitter + Receiver 两端都加载**（隧道原生代价，本 mod 接受此取舍） |

---

## 3. 技术底座（RS 2 架构速查）

### 3.1 模块与 API/内部边界

RS 2 是多模块 Gradle 工程。**addon 只允许依赖 `*-api` 模块**：

| 模块 | 是否可被 addon 依赖 | 作用 |
|---|---|---|
| `refinedstorage-core-api` | ✅ | 基础枚举/工具 |
| `refinedstorage-network-api` | ✅ | `Network` 及网络组件契约 |
| `refinedstorage-grid-api` | ✅ | Grid 交互契约 |
| `refinedstorage-storage-api` | ✅ | 存储契约（`Storage`/`Actor`/`Action`） |
| `refinedstorage-resource-api` | ✅ | 资源类型（`ResourceKey`） |
| `refinedstorage-autocrafting-api` | ✅ | 自动合成契约 |
| `refinedstorage-common-api` | ✅ | Minecraft 集成层（**本 mod 主要依赖此层**） |
| `refinedstorage-common` / `network` / 平台模块 | ❌ 内部，禁止直接依赖 | 具体实现 |

> 依赖引入方式：在 `build.gradle` 通过 refined architect 插件 + `refinedstorageVersion` 引入 RS（见 §10）。addon 只 import `com.refinedmods.refinedstorage.common.api.*` 与 `com.refinedmods.refinedstorage.api.*` 包。

### 3.2 无线接入机制（同维度）

- 无线设备持 **Network Card**（记录目标设备坐标 + 维度）绑定到 **Wireless Transmitter**。
- 打开设备时，服务端：读卡 → 定位发射器 → 算「玩家 ↔ 发射器欧氏距离」→ ≤ 范围则连通，否则报 `no wireless transmitter in range`。
- 发射器基础范围 16 格，Range 升级卡每张 +8 格。

### 3.3 RS 原生跨维度机制（隧道；本 mod 的跨维度走这条）

- **Network Transmitter**（源网络侧，维度 A）+ **Network Receiver**（目标维度 B）用 Network Card 配对：Receiver 把源网络（家庭网络）暴露给维度 B 的本地网络节点。
- **硬约束（官方）**：Transmitter 与 Receiver **两端所在区块都必须加载**；Transmitter 侧网络须有 Controller 供电。网络在未加载区块会关停。
- **`NetworkReceiverBlockEntity` 实现 `NetworkItemTargetBlockEntity`**：网卡原生可绑 Receiver（本 mod 跨维度的落点，见 §7）。
- **本 mod 的取舍**：跨维度走**这条原生隧道**（见 D4），因此接受「两端加载」的代价。终端网卡**绑维度 B 的 Receiver**（而非绑无线发射器），省掉「维度 B 再架一个 Wireless Transmitter」这一步——这是相对社区玩法的简化。

### 3.4 关键类与扩展点（已核实到真实签名）

以下签名直接来自 RS 2 与 Quartz Arsenal 源码 / javadoc，可直接引用。

```java
// com.refinedmods.refinedstorage.common.api.RefinedStorageApi（单例 INSTANCE）
EnergyItemHelper getEnergyItemHelper();
NetworkItemHelper getNetworkItemHelper();
boolean isEnergyRequired();
EnergyStorage createItemEnergyStorage(EnergyStorage base, ItemStack stack, EnergyItemContext ctx);
void sendNoPermissionToOpenMessage(Player player, Component name);
Identifier getCreativeModeTabId();                      // 复用 RS 的创造模式 tab
PlayerSlotReference createPlayerInventorySlotReference(Player player, InteractionHand hand);
```

```java
// com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext
// @API(STABLE) —— 本 mod 范围/维度能力的核心挂载点
Optional<Network> resolveNetwork(boolean force);
default Optional<Network> resolveNetwork() { return resolveNetwork(false); }
boolean isActive();
void drainEnergy(long amount);
```

```java
// com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper
// @API(STABLE)
void addTooltip(ItemStack stack, Consumer<Component> builder);
InteractionResult bind(UseOnContext ctx);
Optional<TooltipComponent> getTooltipImage(ItemStack stack);
NetworkItemContext createContext(ItemStack stack, ServerPlayer player, PlayerSlotReference ref);
boolean isBound(ItemStack stack);
```

```java
// com.refinedmods.refinedstorage.common.api.support.network.NetworkItemTargetBlockEntity
// 网卡可绑定的方块标记接口。NetworkReceiverBlockEntity / WirelessTransmitterBlockEntity 均实现它。
// （javadoc 已确认 NetworkReceiverBlockEntity implements NetworkItemTargetBlockEntity）
```

```java
// com.refinedmods.refinedstorage.common.api.support.energy.AbstractNetworkEnergyItem
// @API(STABLE)
public abstract class AbstractNetworkEnergyItem extends AbstractEnergyItem
        implements UsablePlayerSlotReferencedItem {
    protected final NetworkItemHelper helper;
    protected AbstractNetworkEnergyItem(Properties props, EnergyItemHelper energyHelper,
                                        NetworkItemHelper helper);

    @Override public InteractionResult useOn(UseOnContext ctx) { return helper.bind(ctx); }

    // ★ 关键：此方法是 public + @Override，子类可覆写以注入自定义 NetworkItemContext
    @Override public void use(ServerPlayer player, ItemStack stack,
                              PlayerSlotReference ref) {
        NetworkItemContext context = RefinedStorageApi.INSTANCE.getNetworkItemHelper()
            .createContext(stack, player, ref);
        use(stack.get(DataComponents.CUSTOM_NAME), player, ref, context);
    }

    protected abstract void use(@Nullable Component name, ServerPlayer player,
                                PlayerSlotReference ref, NetworkItemContext context);
    public boolean isBound(ItemStack stack) { return helper.isBound(stack); }
}
```

```java
// com.refinedmods.refinedstorage.common.grid.CraftingGrid（无线终端要实现的接口）
interface CraftingGrid {
    RecipeMatrixContainer getCraftingMatrix();
    ResultContainer getCraftingResult();
    NonNullList<ItemStack> getRemainingItems(Player p, CraftingInput in);
    ExtractTransaction startExtractTransaction(Player p, boolean directCommit);
    boolean clearMatrix(Player p, boolean toPlayerInventory);
    void transferRecipe(Player p, List<List<ItemResource>> recipe);
    void acceptQuickCraft(Player p, ItemStack crafted);
    void addWatcher(GridWatcher w, Class<? extends Actor> actorType);
    void removeWatcher(GridWatcher w);
    Storage getItemStorage();
    boolean isGridActive();
    List<TrackedResourceAmount> getResources(Class<? extends Actor> actorType);
    Set<PlatformResourceKey> getAutocraftableResources();
    Map<PlatformResourceKey, Set<TaskId>> getCurrentlyAutocrafting();
    List<TaskStatus> getAutocraftingTaskStatuses(Set<TaskId> taskIds);
    GridOperations createOperations(ResourceType type, ServerPlayer player);
    boolean canMenuStayOpen(Player p);
    CompletableFuture<Optional<Preview>> getPreview(ResourceKey r, long amount, CancellationToken t);
    CompletableFuture<Optional<TreePreview>> getTreePreview(ResourceKey r, long amount, CancellationToken t);
    CompletableFuture<Long> getMaxAmount(ResourceKey r, CancellationToken t);
    Optional<TaskId> startTask(ResourceKey r, long amount, Actor a, boolean notify, CancellationToken t);
}
```

跨维相关（观察到的类/方法，签名需对照源码落实，见 §12）：

```java
// com.refinedmods.refinedstorage.common.networking.NetworkReceiverBlockEntity
//    extends AbstractBaseNetworkNodeContainerBlockEntity
//    implements NetworkItemTargetBlockEntity, ConfigurationCardTarget, PlayerAwareBlockEntity, Nameable
// 关键方法：createMainContainer() 返回 InWorldNetworkNodeContainer（内含 NetworkReceiverNetworkNode）

// com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity
//    （Receiver 的基类之一）暴露 getNetworkForItem(...) —— 从该方块所属网络节点取 Network（跨维度时即家庭网络）
```

网络组件（通过 `network.getComponent(X.class)` 获取）：
- `StorageNetworkComponent` — 存储
- `AutocraftingNetworkComponent` — 自动合成
- `EnergyNetworkComponent`（`api.network.energy`）— 能量
- `PlatformSecurityNetworkComponent`（`common.api.security`）— 安全权限

其余直接复用的工具类：`RecipeMatrix`（`RecipeMatrix.crafting(listener, levelSupplier)`）、`GridData.of(CraftingGrid)`、`AbstractCraftingGridContainerMenu`、`ExtendedMenuProvider`、`PlayerSlotReference`、`SecuredGridOperations`、`DirectCommitExtractTransaction`、`SnapshotExtractTransaction`、`EmptyGridOperations`、`GridWatcherManagerImpl`、`PlayerActor`、`SecurityHelper.isAllowed(player, BuiltinPermission.OPEN, network)`。

---

## 4. 物品设计规格

### 4.1 命名与 registry id

modid 占位 `rswirelesscrafting`，包名建议 `com.yourname.rswirelesscrafting`（交付前替换）。

| 物品 | registry id | 类 |
|---|---|---|
| 无线合成终端 | `rswirelesscrafting:wireless_crafting_grid` | `WirelessCraftingGridItem` |
| 范围升级卡 | `rswirelesscrafting:range_upgrade` | `RangeUpgradeItem` |
| 维度升级卡 | `rswirelesscrafting:interdimensional_upgrade` | `InterdimensionalUpgradeItem` |

### 4.2 无线合成终端

- 基类：`AbstractNetworkEnergyItem`。
- `Item.Properties`：`stacksTo(1)` + `.setId(ResourceKey.create(Registries.ITEM, id))`。
- 数据组件：`wireless_crafting_grid_state`（持久化合成矩阵，类型 `WirelessCraftingGridState`）+ `upgrades`（升级槽内容，`List<ItemStack>`）。
- 能量（默认，可配置，单位 FE/RS）：
  | 项 | 值 |
  |---|---|
  | 容量 | 1000 |
  | 打开 | 5 |
  | 合成一次 | 5 |
  | 自动合成 | 10 |
  | 清空矩阵 | 5 |
  | 配方转移 | 5 |
- **距离**：同维度有效距离 = `max(发射器范围, extendedRange)`，`extendedRange` 默认 **64 格**（可配）。插**范围卡**→ 同维度无限范围。跨维度走隧道（需维度卡），天然无距离概念（见 §7）。
- 跨维度时以上能量消耗 ×16（见 §7.5）。
- 配方：`refinedstorage:crafting_grids` 标签为合成核心，外围石英富集铁 + 高级处理器 + 机器外壳：

  ```
  Q A Q      Q = refinedstorage:quartz_enriched_iron
  P G P      A = refinedstorage:advanced_processor
  Q M Q      P = c:ender_pearls（标签）
             G = refinedstorage:crafting_grids（标签，兼容全部网格变体）
             M = refinedstorage:machine_casing
  ```

  合计约 14 石英铁 + 2 末影珍珠 + 2 高级处理器 + 1 合成网格。相比 RS 自带的 `wireless_grid`（6 石英铁 + 2 末影珍珠 + 1 网格 + 1 高级处理器）约 2.3 倍，体现「无线＋可合成」在原版无线查看终端之上的额外价值。

### 4.3 范围升级卡（Range Upgrade）

- 普通 `Item`（无能量、无网络绑定）。
- 判定方式：**物品 tag** `rswirelesscrafting:range_upgrades`（终端遍历升级槽，命中即视为已升级）。
- 作用：同维度绑 Wireless Transmitter 时，使 `resolveNetwork` **无视距离**（无限范围）。**不作用于**跨维度（跨维度走隧道，天然无限）。
- 配方（终端本身用 `refinedstorage:crafting_grids` 标签，兼容全部网格变体）：

  ```
  P H P      P = c:ender_pearls（标签）
  T S R      H = minecraft:heart_of_the_sea（海洋之心）
  P X P      T = refinedstorage:wireless_transmitter（无线访问点）
             S = minecraft:nether_star（下界之星）
             R = refinedstorage:network_receiver（网络接收器）
             X = refinedstorage:64k_storage_block（64k存储方块）
  ```

  **设计意图**：六个稀有件各有语义 ——
  - `heart_of_the_sea`：潮涌核心的能量象征，对应「远距离能量传输」；
  - `nether_star`：无限能量源，撑起「无限范围」；
  - `wireless_transmitter`（无线访问点）：本卡机制的物理载体，绑的就是它；
  - `network_receiver`（网络接收器）：RS 通信链路另一端，收发成对；
  - `64k_storage_block`：RS 自身「套娃」到顶的后期产物 —— 存储元件 1k→4k→16k→64k 逐级递归合成，每级都要上一级的元件。它是一个**可自动化量产、但资源吞吐极大**的终结型资源槽（单块需 64 个 1k 元件 = 256 硅 + 64 石英铁 + 256 玻璃 + 64 红石，另加处理器链）。
- 可堆叠 64。

### 4.4 维度升级卡（Interdimensional Upgrade）

- 普通 `Item`（无能量、无网络绑定）。
- 判定方式：**物品 tag** `rswirelesscrafting:interdimensional_upgrades`。
- 作用：解锁「网卡绑 Network Receiver → 走隧道」的跨维度能力（见 §7）。
- 配方：

  ```
  P B X      P = c:ender_pearls（标签）
  N S N      B = minecraft:beacon（信标）
  P E P      X = refinedstorage:64k_storage_block（64k存储方块）
             N = c:ingots/netherite（标签）
             S = minecraft:nether_star（下界之星）
             E = minecraft:elytra（鞘翅）
  ```

  **设计意图**：跨维度是本 mod 的终极能力，用原版最稀有的几件物品表达「撕开维度屏障」：
  - `elytra` 鞘翅 —— 全末地仅存于末地船的稀有品，象征「跨越」；
  - `beacon` 信标 —— 本身还需 1 颗下界之星，象征「定位与锚定」（跨维隧道需要两端坐标对齐）；
  - `nether_star` 下界之星（与信标内的合计共需 **2 颗**，即 2 只凋灵）—— 维度撕裂的能量源；
  - `64k_storage_block` —— RS「套娃」链的顶点，与范围卡同级（单块需 64 个 1k 元件 = 256 硅 + 64 石英铁 + 256 玻璃 + 64 红石）；两张卡各有各的资源槽，维度卡再叠上鞘翅/信标/凋灵链，整体仍是最贵的一张；
  - 2×下界合金锭 —— 能承受跨维度压力的外壳材料。
- 可堆叠 64。

> 说明：RS 1 曾存在同名「Interdimensional Upgrade」但 RS 2 已移除（跨维变默认）。本 mod 的维度卡是 addon 自有物品，registry id 独立，不冲突；语义是「解锁终端侧跨维访问」，与 RS 1 那张无关。

---

## 5. 架构设计

### 5.1 模块划分（照抄 Quartz Arsenal 的 3 模块结构）

```
rswirelesscrafting-common    —— 平台无关逻辑（Item/Grid/Menu/State/Config 接口）
rswirelesscrafting-neoforge  —— NeoForge 注册、能力、能量适配
rswirelesscrafting-fabric    —— （可选，本报告聚焦 NeoForge，可砍）
```

### 5.2 类图

```
RangeUpgradeItem
InterdimensionalUpgradeItem
WirelessCraftingGridItem  extends AbstractNetworkEnergyItem
    ├─ use(name, player, ref, context)   —— 覆写 4 参抽象方法，打开 GUI
    └─ use(player, stack, ref)           —— 覆写 3 参方法，注入自定义 context ★
WirelessCraftingGrid  implements CraftingGrid   —— 合成网格核心逻辑
ExtendedWirelessNetworkItemContext  implements NetworkItemContext  ★ 范围 + 维度核心
WirelessCraftingGridContainerMenu  extends AbstractCraftingGridContainerMenu
WirelessCraftingGridExtendedMenuProvider  implements ExtendedMenuProvider<...Data>
WirelessCraftingGridState / WirelessCraftingGridData   —— 数据组件 / 菜单数据
Config / DefaultEnergyUsage / Platform / Items / Menus / ContentIds / ContentNames
```

### 5.3 依赖关系

addon 仅 import：
- `com.refinedmods.refinedstorage.api.*`（各 `-api` 模块）
- `com.refinedmods.refinedstorage.common.api.*`（common-api）
- `com.refinedmods.refinedstorage.common.*`（仅 grid/support/networking 等 Quartz Arsenal 已验证可直接用的 public 类，如 `CraftingGrid`、`AbstractCraftingGridContainerMenu`、`GridData`、`NetworkReceiverBlockEntity`）

> ⚠️ 不 import `refinedstorage-network`、`refinedstorage-common` 的 `impl`/内部包。

---

## 6. 逐文件实现规格（伪代码级）

### 6.1 `WirelessCraftingGridItem`

```java
public class WirelessCraftingGridItem extends AbstractNetworkEnergyItem {

    public WirelessCraftingGridItem(EnergyItemHelper energyHelper, NetworkItemHelper networkHelper) {
        super(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM,
                  ContentIds.WIRELESS_CRAFTING_GRID)),
              energyHelper, networkHelper);
    }

    // ★ 范围 + 维度挂载点：覆写 3 参 use，恒注入自定义 context
    @Override
    public void use(ServerPlayer player, ItemStack stack, PlayerSlotReference ref) {
        NetworkItemContext context = new ExtendedWirelessNetworkItemContext(
            stack, player, ref, helper,
            hasUpgrade(stack, RangeUpgradeItem.TAG),            // 范围卡
            hasUpgrade(stack, InterdimensionalUpgradeItem.TAG)); // 维度卡
        use(stack.get(DataComponents.CUSTOM_NAME), player, ref, context);
    }

    private static boolean hasUpgrade(ItemStack stack, TagKey<Item> tag) {
        // 读升级槽组件（见 §6.2），任一槽物品命中 tag 即 true
    }

    @Override
    protected void use(@Nullable Component name, ServerPlayer player,
                       PlayerSlotReference ref, NetworkItemContext context) {
        boolean allowed = context.resolveNetwork()
            .map(n -> SecurityHelper.isAllowed(player, BuiltinPermission.OPEN, n))
            .orElse(true);
        if (!allowed) {
            RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(player, ContentNames.WIRELESS_CRAFTING_GRID);
            return;
        }
        CraftingGrid grid = new WirelessCraftingGrid(player, context, ref);
        var provider = new WirelessCraftingGridExtendedMenuProvider(
            requireNonNullElse(name, ContentNames.WIRELESS_CRAFTING_GRID), grid, ref);
        com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(player, provider);
    }

    public static EnergyStorage createEnergyStorage(ItemStack stack, EnergyItemContext context) {
        EnergyStorage s = new EnergyStorageImpl(Math.clamp(
            Platform.getConfig().getWirelessCraftingGrid().getEnergyCapacity(), 1, Long.MAX_VALUE));
        return RefinedStorageApi.INSTANCE.createItemEnergyStorage(s, stack, context);
    }
}
```

### 6.2 升级槽与卡片判定

- 在 `WirelessCraftingGridState` 基础上，新增第二个数据组件 `upgrades`（`List<ItemStack>`，CODEC + STREAM_CODEC），终端提供 4 个升级槽写入该组件。
- `hasUpgrade(stack, tag)`：读 `upgrades` 组件，任一槽物品命中给定 tag 即 true。两张卡各用独立 tag，判定互不影响。
- 升级槽 UI：V1 建议右键终端先打开升级界面，放卡后再点"打开合成"；若想单界面，参照 RS Pattern Terminal 的 tab 切换做法。

### 6.3 `WirelessCraftingGrid`（合成网格，直接抄 Quartz Arsenal）

几乎照搬 Quartz Arsenal `WirelessCraftingGrid`（本报告 §3.4 已列出 `CraftingGrid` 全接口）。核心点：
- 构造时 `RecipeMatrix.crafting(listener, player::level)` 建矩阵；从 `wireless_crafting_grid_state` 组件恢复矩阵；每次矩阵变化回写组件。
- `getStorage()/getAutocrafting()/getSecurity()` 均经 `context.resolveNetwork()` 取组件。
- `startExtractTransaction` 用 `DirectCommitExtractTransaction`/`SnapshotExtractTransaction`。
- `isGridActive()` = 能量>0（或 `!isEnergyRequired()`）且 `context.isActive()`。
- 所有会耗能的操作（open/craft/autocraft/clear/transfer）调 `context.drainEnergy(...)`，跨维度时先 ×16（见 §7.5）。

### 6.4 `ExtendedWirelessNetworkItemContext`（本 mod 唯一创新点）

统一处理「同维度加距离/无限范围」与「跨维度隧道」。核心思路：**按绑定方块的类型分派**。

```java
public class ExtendedWirelessNetworkItemContext implements NetworkItemContext {
    private final ItemStack stack;
    private final ServerPlayer player;
    private final NetworkItemHelper helper;
    private final boolean rangeUpgrade;          // 插了范围卡
    private final boolean interdimensional;      // 插了维度卡
    private final NetworkItemContext standard;   // RS 标准无线 context（fallback）

    public ExtendedWirelessNetworkItemContext(ItemStack stack, ServerPlayer player,
                                              PlayerSlotReference ref, NetworkItemHelper helper,
                                              boolean rangeUpgrade, boolean interdimensional) {
        this.stack = stack; this.player = player; this.helper = helper;
        this.rangeUpgrade = rangeUpgrade; this.interdimensional = interdimensional;
        this.standard = helper.createContext(stack, player, ref);
    }

    @Override
    public Optional<Network> resolveNetwork(boolean force) {
        // 1) 标准逻辑优先：绑 Wireless Transmitter 且在范围内（含 Range 升级卡加成）直接命中
        Optional<Network> standardResult = standard.resolveNetwork(force);
        if (standardResult.isPresent()) return standardResult;

        // 2) 读网卡绑定位置（坐标 + 维度）
        Optional<BoundPosition> bound = helper.getBinding(stack);
        if (bound.isEmpty()) return Optional.empty();

        // 3) 定位绑定方块（需所在区块加载）
        ServerLevel level = player.getServer().getLevel(bound.get().dimension());
        if (level == null) return Optional.empty();
        BlockEntity be = level.getBlockEntity(bound.get().pos());
        if (be == null) return Optional.empty();

        // 4) 绑 Network Receiver + 有维度卡 → 走隧道，取 Receiver 暴露的家庭网络
        if (be instanceof NetworkReceiverBlockEntity receiver && interdimensional) {
            return networkFromBlockEntity(receiver);           // 见 §12 V1
        }

        // 5) 绑 Wireless Transmitter + 有范围卡 → 同维度无限范围
        if (be instanceof WirelessTransmitterBlockEntity && rangeUpgrade) {
            return networkFromBlockEntity(be);                 // 无视距离，见 §12 V2
        }

        // 6) 其他情况（绑 Receiver 但无维度卡 / 绑 Transmitter 但超距离且无范围卡）→ 不可达
        return Optional.empty();
    }

    private boolean withinExtendedRange(Player player, BoundPosition bound) {
        double dist = player.position().distanceTo(centerOf(bound.pos()));
        return dist <= Platform.getConfig().getWirelessCraftingGrid().getExtendedRange(); // 默认 64
    }

    @Override
    public boolean isActive() {
        return resolveNetwork().isPresent();
    }

    @Override
    public void drainEnergy(long amount) {
        // 跨维度才加价；同维度（含范围卡）不加价
        long cost = interdimensional ? (long) Math.ceil(amount * INTERDIMENSIONAL_MULTIPLIER) : amount;
        standard.drainEnergy(cost);
    }
}
```

> 说明：
> - `helper.getBinding`、`networkFromBlockEntity`（定位方块 → 取 `Network`，即基类 `getNetworkForItem` 的封装）、`WirelessTransmitterBlockEntity` 确切类名，需对照 RS 2.0.x 源码落实（见 §12 V1/V2/V3）。
> - 步骤 1 依赖「RS 标准 context 只认 Wireless Transmitter」这一假设：绑 Receiver 时标准 context 返回 empty，从而落到步骤 4。若标准 context 对 Receiver 也做距离判定，需在步骤 1 之前先判绑定方块类型（见 §12 V4）。
> - `networkFromBlockEntity` 与 RS 无线上下文「定位发射器→取网络节点→resolveNetwork」是同一套逻辑，本 mod 复用并放宽「同维度+距离」限制。

---

## 7. 跨维度机制详细设计（隧道方案）

### 7.1 玩法流程

1. 家庭网络（维度 A）：Controller 供电 + 一个 **Network Transmitter**（网卡配到维度 B 的 Receiver）。Transmitter 所在区块**保持加载**。
2. 目标维度（维度 B）：放置已与 Transmitter 配对的 **Network Receiver**，所在区块**保持加载**（任何 chunk loader）。
3. 玩家在维度 B：手持终端，网卡**绑维度 B 的 Receiver**（Receiver 是 `NetworkItemTargetBlockEntity`，原生可绑）；终端放入**维度升级卡**。
4. 打开终端 → `resolveNetwork` 命中「Receiver + 维度卡」分支 → 走隧道取家庭网络 → 打开合成界面，可远程合成。

**距离/维度行为总表**：

| 槽位配置 | 同维度绑 Wireless Transmitter | 绑 Network Receiver（跨维度） |
|---|---|---|
| 无卡 | `max(发射器范围, 64 格)` | ❌ 不可用 |
| 范围卡 | **无限** | ❌（范围卡不作用于隧道） |
| 维度卡 | `max(发射器范围, 64 格)` | ✅ 可用（隧道，天然无限） |
| 范围卡 + 维度卡 | **无限** | ✅ 可用（隧道，天然无限） |

### 7.2 区块加载要求（D10）

| 场景 | 加载要求 |
|---|---|
| 同维度（绑 Transmitter） | 仅家庭网络侧（Transmitter 所在区块）加载；玩家所在位置零要求 |
| 跨维度（绑 Receiver，隧道） | **Transmitter 侧 + Receiver 侧两端都加载**（隧道原生硬约束）；玩家所在位置零要求 |

- 这是 RS 原生 Network Transmitter/Receiver 的固有约束（网络在未加载区块会关停）。本 mod 忠实采用，不做「直连」绕过（本项目采用 B 方案）。
- 两端均可用任何 chunk loader（如 FTB Chunks）保持常驻，属 MC 服务器常态。

### 7.3 为何走隧道（而非直连）

- 本项目采用「方案 B（隧道）」：跨维度通过 Network Transmitter/Receiver 实现，忠实 RS 原生机制与平衡。
- 相比「直连」方案的差异：直连只需家庭网络侧一端加载、但属于「改造 RS 无线语义」；隧道是 RS 原生路径，玩家理解成本低、后续 RS 升级兼容性更好，代价是两端都要加载。
- 本 mod 相对社区玩法（巴哈姆特式：目标维度要「Receiver + Wireless Transmitter」两件套）的**简化**：终端网卡直接绑 Receiver，省掉目标维度的第二台无线发射器。

### 7.4 约束与兜底

- 家庭网络侧必须有 Controller 供电；Transmitter + Receiver 两端区块必须加载；否则 `resolveNetwork()` 返回 empty，终端提示"网络不可达/未加载"。
- 安全：跨维度同样走 `SecurityHelper` 权限校验（`BuiltinPermission.OPEN`），不因跨维度放宽权限。
- 绑定 Receiver 但未插维度卡 → 走步骤 6，返回 empty（维度卡是跨维的必要开关）。

### 7.5 能量加成

- 所有 `drainEnergy(amount)` 都乘一个距离倍率：同维度按「距离 ≤ 基础范围（16 格）×1，超出后线性增长（倍率 = 距离 ÷ 16）」；**跨维度**（走隧道）时统一 ×16。初级能耗插件减半、高级能耗插件归 1（跨维度 ×16 不受能耗插件影响）。

---

## 8. 网络同步协议

- 合成矩阵状态：`WirelessCraftingGridState` 的 `CODEC`（持久化）+ `STREAM_CODEC`（网络同步），挂到 DataComponent。
- 菜单数据：`WirelessCraftingGridData.STREAM_CODEC`，经 `ExtendedMenuTypeFactory` 注册。
- 升级槽内容：`upgrades` 组件的 STREAM_CODEC（如需客户端显示）。

---

## 9. 资源与本地化

- `assets/rswirelesscrafting/lang/en_us.json`、`zh_cn.json`：物品名、`no wireless transmitter in range` 变体、`network not reachable` 提示。
- `assets/rswirelesscrafting/textures/item/*.png`：终端、范围卡、维度卡。
- `assets/rswirelesscrafting/models/item/*.json`：对应模型。
- `data/rswirelesscrafting/recipes/*.json`：§4 配方。
- `data/rswirelesscrafting/tags/item/range_upgrades.json`：范围卡 tag。
- `data/rswirelesscrafting/tags/item/interdimensional_upgrades.json`：维度卡 tag。

---

## 10. 构建与依赖

| 项 | 值 |
|---|---|
| JDK | **21**（Temurin 推荐；⚠️ 本机当前默认 JDK25、另有 JDK17，**无 JDK21**，需先装） |
| 构建 | refined architect Gradle 插件（`com.refinedmods.refinedarchitect`） |
| RS 依赖 | `gradle.properties` 里 `refinedstorageVersion=2.0.x`（1.21.1 线，基线 **v2.0.0**） |
| 参考基线 | 直接复用 Quartz Arsenal v1.0.8 的 `build.gradle.kts` / `settings.gradle.kts` / `gradle.properties`（三模块结构） |
| 版本锁定 | `refinedarchitectVersion` 以 Quartz Arsenal v1.0.8 的 gradle.properties 为准，**不要**照抄 develop 分支（那是 MC 26.1 / RS 3.2.0） |

> 环境提示：本机 Gradle 需走 JDK 21。NeoForge 1.21.1 官方要求 Java 21。Gradle 发行版如需镜像：`https://mirrors.cloud.tencent.com/gradle/`。

---

## 11. 里程碑与测试

| 阶段 | 内容 | 验收 |
|---|---|---|
| M1 | 骨架：三模块 + refined architect + RS 依赖，空 Item 可进游戏 | 客户端加载 mod，物品出现在创造 tab |
| M2 | 无线合成终端（抄 Quartz Arsenal，标准距离） | 同维度发射器范围内可远程合成，能量正确扣减 |
| M3 | 范围卡 + 同维度无限范围（context 步骤 5） | 同维度任意距离（超出发射器范围）仍可打开合成 |
| M4 | 维度卡 + 隧道跨维度（context 步骤 4） | 跨维度（Receiver+Transmitter 两端加载）打开合成、远程合成成功 |
| M5 | 配置 + 语言 + 配方 | 距离/能量/跨维度乘数可配，能量可配置关闭 |
| M6 | 边界测试 | 家庭网络未加载/无 Controller/Receiver 未加载时报错不崩；权限校验生效 |

手工验收清单（跨维度）：
1. 家庭 Overworld 放 Controller + Network Transmitter，保持该区块加载。
2. Nether 放 Network Receiver（网卡配对 Transmitter），保持该区块加载。
3. Nether 玩家终端网卡绑 Receiver + 插维度卡 → 打开终端看到家庭网络物品。
4. 从 Nether 发起合成 → 物品正确扣除/合成入网。
5. 拔掉维度卡 → 跨维度打开失败（走步骤 6 返回 empty）。
6. 仅同维度场景：绑 Transmitter + 插范围卡 → 任意距离可打开；拔范围卡 → 回退 64 格。
7. 能量耗尽 → 无法打开（`isGridActive()=false`）。

---

## 12. 风险与待验证点（实现时对照 RS 源码落实）

| # | 待验证点 | 建议动作 |
|---|---|---|
| V1 | 从 `NetworkReceiverBlockEntity` 取它暴露的 `Network` 的确切 API | grep RS `refinedstorage-common` 里 `getNetworkForItem` / `getNetwork` 封装；确认 Receiver 暴露的网络即家庭网络 |
| V2 | 从 `WirelessTransmitterBlockEntity` 取 `Network` 的 API，及无线上下文「定位发射器→取网络」写法 | 复用 RS 无线上下文封装，去掉距离限制 |
| V3 | `NetworkItemHelper.bind()` 允许绑定哪些设备、网卡数据组件（坐标+维度）读取 API | 查 `helper.bind(UseOnContext)` 实现；确认绑 Receiver 与绑 Transmitter 在卡片数据上等价 |
| V4 | RS 标准 context 绑 Receiver 时 `resolveNetwork` 的行为（是否只认 Transmitter） | 若标准 context 对 Receiver 也做距离判定，需在步骤 1 前先判绑定方块类型再分派 |
| V5 | 升级槽的承载方式（DataComponent vs RS 原生升级机制） | 优先复用 RS 升级机制（若有公开 API）；否则自建 `upgrades` 组件 |
| V6 | RS 2.0.x 具体 patch 版本 | 以 Modrinth/CurseForge 1.21.1 最新 release 为准（基线 v2.0.0），锁进 `refinedstorageVersion` |

---

## 14. 0.1.5 增量：充电站 · 电池卡 · 输电卡 · 装配式升级

本节记录 0.1.0 首版之后新增的内容，并**修正 §2 / §12 中被实现推翻的条目**。所有结论均已落到可编译代码（`./gradlew -I init-mirror.gradle build` 全绿）。

### 14.1 待解决的两个前置问题

| 问题 | 结论 |
|---|---|
| RS 2.0.7 有没有公开的**方块**基类？ | **没有**。`@API(status = STABLE)` 只覆盖 `*-api` 模块与 `refinedstorage-network` 的部分类；方块与方块实体基类全在内部模块 `refinedstorage-common`，无标注。官方附属 Quartz Arsenal 也**从不注册方块**，无样例可抄。→ 方块本体用原版 `BaseEntityBlock` |
| 那方块实体能不能复用 RS 的？ | **能**。`AbstractNetworkNodeContainerBlockEntity<T extends NetworkNode>`（在 `refinedstorage-common-api`，标注 STABLE）是公开的，构造签名 `(BlockEntityType<?>, BlockPos, BlockState, T mainNetworkNode)`，自带网络容器生命周期（`clearRemoved` / `setRemoved`）。但它**不含 tick 与耗电**，需自己补 |

### 14.2 充电站（`charging_station`）

> ⚠️ **0.1.8 起分为 400V 风冷与 800V 液冷两档**，方块 ID 变成 `air_cooled_charging_station` 与 `liquid_cooled_charging_station`。本节描述的机制（方块/方块实体/开界面路径/能量账本）全部不变，档位差异与数值见 **§15.2**。

**方块** `ChargingStationBlock extends BaseEntityBlock`：

- `getTicker` 服务端返回自定义 ticker 调 `station.tick()`（原版 tick 路径，不碰 RS 内部 ticker 体系）；
- 右键用 `com.refinedmods.refinedstorage.common.Platform.INSTANCE.getMenuOpener().openMenu(serverPlayer, station)` 开界面——注意这里用的是 **RS 的 `Platform`**，不是 NeoForge 原版 `openMenu`，因为 BE 实现的是 RS 的 `ExtendedMenuProvider<D>`（包 `com.refinedmods.refinedstorage.common.support.containermenu`），**不是** `net.minecraft.world.inventory.MenuProvider`；
- `onRemove` 掉落槽位内容物。

**方块实体** `ChargingStationBlockEntity extends AbstractNetworkNodeContainerBlockEntity<ChargingStationNetworkNode> implements Container, ExtendedMenuProvider<ChargingStationData>`：

| 方法 | 行为 |
|---|---|
| `tick()` | `refreshNetworkEnergy()` → `consumeEnergy(energyUsage)`（待机耗电，与 RS 其他机械一致） → `chargeWiredSlot()` → `chargeNearbyPlayers()` |
| `chargeWiredSlot()` | 槽位物品每 tick 按 `wiredChargeRate` 充 |
| `chargeNearbyPlayers()` | 遍历 `ServerLevel.players()`，排除旁观/死亡，`distanceToSqr` ≤ `wirelessRadius²` 的玩家，对其背包按 `wirelessChargeRate` 充 |

**能量账本**（两条供电来源，网络优先）：

```java
private void consumeEnergy(final long amount) {
    long remaining = amount;
    final Network network = mainNetworkNode.getNetwork();
    if (network != null) {
        final EnergyNetworkComponent energy = network.getComponent(EnergyNetworkComponent.class);
        if (energy.getStored() > 0) {
            remaining -= energy.extract(remaining);
        }
    }
    if (remaining > 0 && externalEnergy > 0) {
        externalEnergy -= Math.min(remaining, externalEnergy);
        setChanged();
    }
}
```

充电时**先模拟再扣电**（`simulateAccepted` → `deliver`），避免从网络白抽电：

```java
private long chargeItem(final ItemStack stack, final long rate) {
    long given = 0;
    final Platform.ItemEnergyAccess access =
        rate > 0 ? Platform.getItemEnergy(stack).orElse(null) : null;
    if (access != null) {
        given = deliver(access, simulateAccepted(access, rate));
    }
    return given;
}
```

**兼容性关键**：物品侧充电走 **NeoForge 的 `Capabilities.EnergyStorage.ITEM`**，**不**经过 RS 的 `PlatformImpl.getEnergyStorage`（那只会认 RS 自己的 `EnergyStorageAdapter`）。这是「能给 RS 的全部终端充电」和「能读其他模组的 FE 物品」的共同前提。为此在 common 侧抽了 `Platform.ItemEnergyProvider` / `Platform.ItemEnergyAccess` 两个抽象，由 neoforge 侧的 `PlatformImpl` 实现（内部是 `ForgeEnergyAccess` record）。

**外部供电**：`ChargingStationEnergyStorage implements IEnergyStorage`，注册在 `Capabilities.EnergyStorage.BLOCK` 上，`canReceive()=true` / `canExtract()=false`，容量 10000 FE。其他模组用标准 FE 输电的方块与管道可以直接给它供电。

**已知取舍**：`InWorldNetworkNodeContainerBuilder` 的默认连接策略是 `SimpleConnectionStrategy`（六向自动连），所以 **`createMainContainer` 完全不需要重写**——充电站像任何 RS 机械一样自动接入相邻网络。

### 14.3 电池卡与输电卡

| 卡 | 效果 | 实现落点 |
|---|---|---|
| `battery_upgrade` | 每张 **+1000 FE** 容量（可叠加） | `WirelessCraftingGridItem.getEnergyCapacity(stack)` 改为静态动态计算：`base + countBatteryUpgrades(stack) * batteryUpgradeCapacity`，两侧（客户端 tooltip / 服务端）走同一函数 |
| `remote_energy_upgrade` | 不再耗终端自身电池，直接花网络电 | `ExtendedWirelessNetworkItemContext` 新增 `remoteEnergy` 字段 |

输电卡只改两个覆写：

```java
@Override
public boolean isActive() {
    if (!remoteEnergy) {
        return standard.isActive();
    }
    return resolveNetwork()
        .map(network -> !RefinedStorageApi.INSTANCE.isEnergyRequired()
            || network.getComponent(EnergyNetworkComponent.class).getStored() > 0)
        .orElse(false);
}

@Override
public void drainEnergy(final long amount) {
    final long cost = isInterdimensional()
        ? amount * Platform.getConfig().getWirelessCraftingGrid().getInterdimensionalEnergyMultiplier() / 100
        : amount;
    if (remoteEnergy && payFromNetwork(cost)) {
        return;
    }
    standard.drainEnergy(cost);
}
```

`payFromNetwork` 用 `EnergyNetworkComponent.extract(cost) >= cost` 判断「网络是否足额支付」，**抽不满就整笔回落给物品电池**（不半途改道），保证「网络无电 / 未绑定」时行为与未装卡完全一致。跨维度 ×16 的倍率在**两条路径上都生效**（`isInterdimensional()` 要求「插了维度卡**且**绑定维度 ≠ 玩家当前维度」）。

**过渡设计**：电池卡刻意定价在 RS 存储元件递归链的**入门级**（1k 存储方块 + 红石块），前期即可量产；同时它是输电卡的配方原料，所以前期投入不会被浪费。

### 14.4 装配式升级卡（推翻早期的合成式方案）

**早期方案（已删除）**：自定义 `ShapelessRecipe` 子类（`InstallUpgradeRecipe` / `InstallUpgradeRecipeSerializer` + `data/.../recipe/install_upgrade.json`），在工作台把「终端 + 卡」无序合成，把卡写进数据组件。**问题**：装上去拿不下来，换卡必须重做终端，且配方会吞掉终端本体。

**现行方案**：

| 组件 | 职责 |
|---|---|
| `UpgradeContainer implements Container` | 把 4 个槽位映射到终端物品的 `upgrades` 数据组件。`snapshot()` 补齐到定长 4，`commit()` 压缩掉空位、全空时 `remove` 整个组件（保持 NBT 干净） |
| `WirelessCraftingGridUpgradeContainerMenu` | 4 个升级槽（`mayPlace` 仅接受 `Upgrades.isUpgrade(stack)`，`getMaxStackSize()=1`）+ 玩家背包 |
| `WirelessCraftingGridUpgradeMenuProvider` / `UpgradeMenuData` / `WirelessCraftingGridUpgradeScreen` | 从 `SlotReference` 侧打开界面（因为改的是**物品**而不是方块，不能用 `pos` 定位） |
| `WirelessCraftingGridItem.use(...)` | 潜行时打开装配界面，否则委托 `super.use` 走标准无线网格逻辑 |

`Upgrades` 工具类提供 `BATTERY_UPGRADES` / `REMOTE_ENERGY_UPGRADES` 等 `TagKey<Item>`、`ALL_UPGRADE_TAGS`、`isUpgrade(ItemStack)`、`countBatteryUpgrades(ItemStack)`、`hasRemoteEnergyUpgrade(ItemStack)`。装配上限 `MAX_UPGRADES = 4`。

**为什么用「物品数据组件 + 界面」而不是 RS 原生升级机制**：RS 原生升级槽是**方块设备侧**的语义（升级装在机器里），而这里要升级的是一张**物品**，两者不通用。故自建 `upgrades` 组件（`List<ItemStack>`），并在物品 tooltip 里列出已装卡。

### 14.5 配置与默认值（0.1.5 新增项）

> ⚠️ 下面的充电站常量是 **0.1.5 当时的数值**，已被 §15.2 取代（现为两个 `ChargingStationDefaults` 常量、两节配置）。

```java
public static final long CHARGING_STATION_ENERGY_USAGE  = 8;      // 待机耗电 FE/t
public static final long CHARGING_STATION_WIRED_RATE    = 100;    // 有线充电 FE/t
public static final long CHARGING_STATION_WIRELESS_RATE = 20;     // 无线充电 FE/t
public static final int  CHARGING_STATION_WIRELESS_RADIUS = 8;    // 无线半径（格）
public static final long CHARGING_STATION_EXTERNAL_CAPACITY = 10000; // 内部 FE 缓冲
public static final long BATTERY_UPGRADE_CAPACITY       = 1000;   // 每张电池卡容量加成
```

配置接口相应新增 `getBatteryUpgradeCapacity()` 与 `ChargingStationEntry getChargingStation()`（子接口 `getEnergyUsage` / `getWiredChargeRate` / `getWirelessChargeRate` / `getWirelessRadius`），由 neoforge 侧 `ConfigImpl` 的 `ChargingStationEntryImpl` 实现。

### 14.6 构建期踩坑（checkstyle）

本项目 `build` 包含 checkstyle，以下几条约束在写这些类时反复触发，编码前先记住：

| 规则 | 约束 | 应对 |
|---|---|---|
| `CustomImportOrder` | `SAME_PACKAGE(3) → STANDARD_JAVA_PACKAGE → THIRD_PARTY → static`；`com.kokuu.*` 与 `net.*` 属**同一组**，组内字典序、不可插空行 | `javax.*` 归 STANDARD_JAVA_PACKAGE，要单独成组并用空行与 `net.*` 隔开 |
| `InnerTypeLast` | 内部类必须放类末尾 | `ContainerData` 匿名类 → 提为末尾内部类 `ChargingStationDataSlots`；`Bar` record → 移到 `ChargingStationScreen` 末尾 |
| `AnonInnerLength` | 匿名类 ≤ 20 行 | 同上，改用具名内部类 |
| `ReturnCount` | 每方法 ≤ 4 个 return | 用卫语句 + 拆辅助方法（`simulateAccepted` / `deliver`） |
| `NestedIfDepth` | 嵌套 if ≤ 1 层 | 同上，提前 return |

另：**Java lambda 不能捕获非 final 的循环变量**——循环里注册回调时要先 `final int slot = i;` 再捕获。

### 14.7 对 §12 待验证点的最终裁定

| # | 裁定 |
|---|---|
| V1 | ✅ 已核实：`NetworkReceiverBlockEntity` 通过 `NetworkItemTargetBlockEntity.getNetworkForItem()` 暴露家庭网络 |
| V2 | ✅ 已核实：`WirelessTransmitterBlockEntity` 同样实现 `NetworkItemTargetBlockEntity`；距离校验在 `NetworkItemPlayerValidator`，不在取网络这一步 |
| V3 | ✅ 已核实：`NetworkItemHelper.bind()` + `DataComponents.getNetworkLocation()`（`GlobalPos` = 维度 + 坐标） |
| V4 | ✅ 已核实：标准 context 对 Receiver 不做距离判定（它按 `SimpleConnectionStrategy` 走隧道），因此「先判方块类型再分派」的策略成立 |
| V5 | ⚠️ **裁定为自建**：RS 原生升级机制是方块设备侧语义，物品侧无公开 API，故自建 `upgrades` 数据组件（见 §14.4） |
| V6 | ✅ 已锁定：基线 **2.0.7**（1.21.1 最新 release），构建脚本实际解析到 2.0.9 也能跑 |

### 14.8 GUI 槽位几何约定（踩过坑）

`AbstractContainerScreen` 把 16×16 的物品图标画在 `leftPos + slot.x` / `topPos + slot.y`，所以背景图里那个 18×18 的凹陷框必须画在 **(slot.x - 1, slot.y - 1)**——这是原版约定，`gen_gui_textures.py` 的 `slot(pixels, x, y)` 参数即「凹陷框左上角」。

**这里出过一次真实的错位 bug**：初版 `charging_station.png` 把充电槽画在 y=52，而 `ChargingStationContainerMenu.CHARGE_SLOT_Y` 写的是 35，结果放进去的终端图标直接盖在外部 FE 缓冲条上；同时两条能量条高度不一致（12 / 6），6px 高的那条装不下 9px 的文字。

**校验方法**（不需要启动游戏）：扫描背景 PNG 里 16×16 的纯 `SLOT_BG(139,139,139)` 区域，其左上角坐标应当**逐个等于**菜单里的槽位坐标。

```python
for y in range(h - 15):
    for x in range(w - 15):
        if all(px[y+r][x+c] == (139,139,139,255) for r in range(16) for c in range(16)):
            ...  # (x, y) 应为某个 slot 的菜单坐标
```

当前对齐结果：充电槽 `(80, 51)`、升级槽 `(53/71/89/107, 31)`、玩家背包 `(8 + col*18, 84 + row*18)`、快捷栏 `y = 142`，全部与菜单常量一致。

---

## 15. 0.1.7 / 0.1.8 增量：两档充电站 · 输电卡配方修正

### 15.1 0.1.7：无线充电速率 10 → 20 FE/t

0.1.5 的单座充电站无线速率偏低（10 FE/t），路过补电意义不大 → 提到 20 FE/t，与有线保持 5× 的「快 / 慢」对比。（当时是 FE/t 语义；0.1.9 起充电站的全部速率统一改为**每秒**口径，见 §16.2。）

### 15.2 0.1.8：拆成 400V 风冷与 800V 液冷两档

| | 400V 风冷 `air_cooled_charging_station` | 800V 液冷 `liquid_cooled_charging_station` |
|---|---|---|
| 有线（GUI 槽位） | 120 FE/s | 240 FE/s |
| 无线（范围内玩家，每物品） | 20 FE/s | 80 FE/s |
| 无线半径 | 4 格 | 10 格 |
| 待机耗电 | 8 FE/s | 32 FE/s |
| 外部 FE 缓冲 | 10000 FE | 40000 FE |
| 方块强度（破坏 / 抗爆） | 2.0 / 6.0 | 3.5 / 9.0 |
| 配方 | 8 石英 + 4 红石块 + 4 机器外壳 + 1 高级处理器 | 同图案：红石块→**蓝冰**、高级处理器→**下界之星** |

**单位口径**：需求里给的「120 / 20 / 240 / 80 每秒」就是**每秒（FE/s）**，0.1.8 曾误按 FE/t 落地，0.1.9 已改回每秒口径并统一实现方式（见 **§16.2**）。下面这些默认值本身没有变，变的是它们的含义与内部换算。

**关键决策：两档共用一套类，不复制**

- `ChargingStationBlock` 只多一个 `ChargingStationTier` 字段；两个注册项是**同一个类、同一个 `BlockEntityType`** 的两份实例（`BlockEntityType.Builder.of(..., 风冷方块, 液冷方块)`）。菜单类型与界面也只有一份。
- 方块实体在**构造时**从 `BlockState` 反查档位：`state.getBlock() instanceof ChargingStationBlock block → block.getTier()`。因为是静态辅助方法（`tierOf(state)` / `entryOf(state)`），可以直接写在 `super(...)` 的参数位里，用它决定 `ChargingStationNetworkNode` 的待机耗电与 `externalCapacity`。
- 档位 → 配置节的映射收敛在枚举上：`ChargingStationTier.getEntry(config)` 返回 `getAirCooledChargingStation()` 或 `getLiquidCooledChargingStation()`，方块实体其余代码不出现任何 `if (tier == ...)` 分支。
- 界面：`ChargingStationData` 增加 `tier`（网络 id，`writeVarInt`/`readVarInt`），客户端据此选背景图与能量条主色。
- **新增类**：`ChargingStationTier`（枚举：`networkId` / `byNetworkId` / `getEntry`）、`ChargingStationDefaults`（record，打包一档的 5 个默认值）、`ChargingStationBlockItem`（`BlockItem` 子类，tooltip 动态读本档配置列出数值）。

**配置**：`chargingStation` 一节拆成 `airCooledChargingStation` / `liquidCooledChargingStation`，各含 `energyUsage` / `wiredChargeRate` / `wirelessChargeRate` / `wirelessRadius` / `externalCapacity`（最后一项是 0.1.8 新增的可配项，此前硬编码在常量里）。`DefaultEnergyUsage` 中改为两个 `ChargingStationDefaults` 常量，仍是全部默认数值的单一出处。

**资源与美术**：两套 blockstate / block model / item model / loot table / 方块纹理 / GUI 背景；旧的 `charging_station.*` 资源已删除。方块纹理参数化为 `charging_station_texture(liquid_cooled)`——风冷是中性灰机壳 + 两侧散热鳍片 + 青蓝闪电，液冷压向紫灰 + 环绕面板的冷却回路 + 紫闪电；界面背景同样参数化为 `charging_station_gui(liquid_cooled)`，两条能量条加一圈 accent 外框（青蓝 / 紫）以便一眼分辨。

🚨 **破坏性变更**：方块 ID 从 `charging_station` 变成两个新 ID，**已放置的旧方块更新后会消失**。考虑到该方块 0.1.5 才引入、仓库又只保留最新 Release，按项目惯例直接替换，不做存档迁移。

### 15.3 输电卡配方：去掉末影珍珠

`remote_energy_upgrade` 四角的 `c:ender_pearls` 换成 `minecraft:copper_block`。理由：这张卡讲的是**电**（从网络远距离取电），而末影珍珠是空间 / 传送语义，两者没有关系（用户指出）。改为**铜块**后，与中心的**避雷针**构成同一组「导电材料」语言，同时把这张卡和范围卡 / 维度卡区分开——后两张才是空间语义、才该用末影珍珠。其余材料（电池卡 / 64k 存储方块 / 下界之星）不变，价位不降。

### 15.4 踩坑记录

- `simpleCodec(ChargingStationBlock::new)` 只能对应 `(Properties)` 一个构造签名 → 保留该构造器并委托到默认档位；mod 方块不走这个 codec 反序列化，无实际影响。
- 方块物品要有动态 tooltip 必须用 `BlockItem` 子类（`ChargingStationBlockItem`）；`Item.TooltipContext` 照旧裸名使用、不 import。
- 配置节改名后，旧存档的 `config/rswirelesscrafting-common.toml` 会残留失效的 `chargingStation` 段，NeoForge 会忽略它；新时段与值需重新生成（或删掉该 toml）。

---

## 16. 0.1.9 增量：方块渲染修复 · 能量口径改为每秒

首轮真机测试报回两个 bug，都是 0.1.5 起就潜伏、而构建期无法发现的。

### 16.1 方块放下后完全不可见（可透视）

**现象**：两档充电站放进世界后不渲染，直接看穿到后面的地形。

**根因**：`ChargingStationBlock extends BaseEntityBlock`，而原版 `BaseEntityBlock` **默认覆写了 `getRenderShape(BlockState)` 返回 `RenderShape.INVISIBLE`**——因为原版所有方块实体方块（箱子、熔炉、告示牌……）都由自己的 `BlockEntityRenderer` 画，不需要 json 模型。本模组的充电站是**普通 json 模型 + 方块实体只存数据**，于是模型被整体跳过。

**修法**：显式覆写

```java
@Override
protected RenderShape getRenderShape(final BlockState state) {
    return RenderShape.MODEL;   // 否则 BaseEntityBlock 默认 INVISIBLE
}
```

**教训**：凡是「原版 `BaseEntityBlock` + 静态 json 模型」的组合，`getRenderShape` 是必答题，不是可选项。构建、checkstyle、资源打包全都发现不了，只有进游戏才能看见——这也是本项目第一次真机冒烟测试就抓到的第一个问题。

### 16.2 充电速率的口径：每秒（FE/s），不是每 tick

**需求原文给的是「每秒」**，0.1.8 误按 FE/t 落地（当时判断依据是其中的「20」与 0.1.7 默认值吻合，怕按秒理解会慢 20 倍）。0.1.9 按用户明确要求改回**每秒**口径。

**实现：配置仍写每秒值，方块内部摊到 tick**

新增 `ChargingStationBlockEntity.RateMeter` 内部类，把「每秒额度」摊成每 tick 额度：

```java
private static final class RateMeter {
    private static final long TICKS_PER_SECOND = 20;
    private long carry;

    long next(final long perSecond) {
        final long total = carry + Math.max(0, perSecond);
        carry = total % TICKS_PER_SECOND;   // 余数顺延，长期平均精确
        return total / TICKS_PER_SECOND;
    }
}
```

- 三个实例：`idleMeter` / `wiredMeter` / `wirelessMeter`，分别对应待机、有线、无线。
- `tick()` 里每 tick 各推进一次，把返回值当本 tick 的额度用（`consumeEnergy(budget)` / `chargeItem(stack, budget)`）。
- 余数累积的意义：配额若被整除（20 / 80 / 120 / 240 都能整除）看不出差别，但玩家一旦把速率配成 7 这种除不尽的数，直接整除会持续漏掉 13/20 的电；累积后长期平均恰好等于配置值。
- 好处是**配置项里的数字就是玩家给的那些数**（120 / 20 / 240 / 80），语义直白；代价是内部多一个 5 行的小类。

**待机耗电也一并改为每秒**：否则会倒挂——400V 待机 8 FE/t = 160 FE/s，比它 120 FE/s 的充电输出还高，站只要在跑就净亏电。改为 FE/s 后：400V 8 FE/s（= 0.4 FE/t）、800V 32 FE/s（= 1.6 FE/t）。`RateMeter` 恰好也解决了「每秒不到 1 FE」时的整数截断。

**连带改动**：`Config.ChargingStationEntry` 接口加单位说明（全部 FE/s，`wirelessChargeRate` 指每个物品）；`ChargingStationDefaults` record 的 `@param` 全部改注 FE/s；`ConfigImpl` 的配置注释、`lang/*.json` 的 tooltip 与三个方块文案（`wired` / `wireless` / `idle`）从 `FE/t` 改成 `FE/s`；README 的中英速率表与说明同步。

### 16.3 复核方式（构建期可做）

- 反编译确认常量与逻辑真进了包：
  `javap -p -c -cp <jar> com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlock | grep -A4 getRenderShape`
- 确认内部类打包：`javap -p -cp <jar> 'com.kokuu...ChargingStationBlockEntity$RateMeter'`
- 这两项都无法替代真机验证，但能排除「改了没打进去」这一类低级失误。

---

## 17. 0.1.10 增量：GUI 背景错乱根因修复 · 升级名高亮

### 17.1 三个自定义 GUI 全部错乱的根因：blit 的 256×256 假定

真机复现「界面只显示左上角一块、物品栏区域变透明、格子和实际画的格子对不上」，两个充电站与升级装配界面全中招。根因不是槽位坐标（0.1.6 已按 `slot-1` 约定对齐），而是背景纹理的绘制调用：

```java
// 错：7 参 blit 假定纹理是 256×256，却把 176×166 的背景按 256 纹理采 UV
graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

// 对：带纹理尺寸的重载，按真实 176×166 采 UV、1:1 铺满
graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
```

`GuiGraphics.blit(loc, x, y, u, v, w, h)` 内部把 UV 按 **256×256** 归一化；我们的背景是 176×166，于是只采到纹理左上约 69%×65% 的内容、再放大到整个界面——视觉上就是「只显示四分之一、槽位框错位、底部物品栏区域根本没有背景（显得透明）」。

- 教训：**自定义 GUI 背景只要不是 256×256，就必须用带 `textureWidth/textureHeight` 的重载**（本项目背景 176×166 恰好等于 `imageWidth/imageHeight`，所以直接 `0,0,imageWidth,imageHeight,imageWidth,imageHeight`）。
- RS 自带的合成终端界面不受影响——它用的是 RS 自己的 256×256 纹理与 RS 自己的绘制，标准路径。
- 连带收益：能量条此前用 `fill()` 按屏幕坐标画、背景却是缩放后的纹理，两者错位；修掉 blit 后能量条与背景里的凹槽也重新对齐了。

### 17.2 终端 tooltip 里已安装升级的排版与高亮

- 新增 `Upgrades.getColor(ItemStack)`：按卡类型返回主题色（范围=青、电池=绿、维度=浅紫、输电=金），与各卡自身描述文字的颜色一致。
- `WirelessCraftingGridItem` 的「已安装升级」从一行逗号拼接改为逐行列出，每行 `· ` 前缀 + 该卡主题色名称，视觉上更清楚。

---

## 18. 0.2.0 增量：充电站重做为单档「终端充电站」

用户要求撤掉 400V 风冷 / 800V 液冷两档，只留一座 `terminal_charging_station`，并把能量口径从「每秒」落定。要点：

### 18.1 数值（单档，默认）

- 有线：**3 个槽位**，各 **200 FE/s**（= 10 FE/t）。
- 无线：**20 FE/s**（= 1 FE/t，每个设备），半径 **5 格**。
- 无线并发：**全局 3 个设备名额**（不是每玩家 3 个），按「每名玩家先拿 1 个、有余再轮」的次序分给离站最近的玩家——3 名玩家各充 1 个，1 名玩家独占 3 个，充满一个自动轮到下一个。
- 待机 8 FE/s；外部 FE 缓冲 10000 FE。

### 18.2 连接面板（6 灯）与多人同步

- GUI 是一张 3×2 面板：上排 3 个有线槽（功能性物品槽），下排 3 个无线链接灯（纯指示）。
- 有线槽状态由 `ChargeStatus` 枚举表达（`IDLE` / `CHARGING` / `FULL`）：充电中蓝灯呼吸闪烁、充满绿灯、空槽暗灯。
- 无线链接每 tick 重算：`level.players()` 过滤半径内非旁观、存活玩家 → 按 `distanceToSqr` 排序 → 最近 3 名各占一条链接；`chargePlayerDevices` 按背包顺序充最多 3 个非满设备。
- 状态经 `ContainerData` 同步（下标 0-3 网络/缓冲，4-6 有线状态 ordinal，7-9 无线 0/1）。呼吸闪烁是**纯客户端视觉**：`breathePhase(partialTick)` 用 `level.getGameTime() % 40` 做 2 秒正弦，不占额外网络包。

### 18.3 代码变化

- 删除 `ChargingStationTier`；`ChargingStationBlock`/`BlockItem`/`ContainerMenu`/`Screen`/`Data` 全部去档位。
- 新增 `ChargeStatus` 枚举与 `ChargingStationBlockEntity` 的 `nearbyPlayersByDistance` / `chargePlayerDevices` / `clearWirelessLinks`。
- `Config` / `ConfigImpl` / `DefaultEnergyUsage` 收敛为单节 `chargingStation`。
- 资源全部改名为 `terminal_charging_station`；`gen_gui_textures.py` 与 `gen_textures.py` 改为单张充电站 GUI/方块纹理；`gen_readme_images.py` 同步。
- checkstyle 注意：数组字面量 `{ ... }` 内不得有空格（`NoWhitespaceAfter`），单条位运算表达式运算符数 ≤3（`BooleanExpressionComplexity`），`pulse()` 为此拆成 `alpha`/`rgb` 两段。

---

## 19. 0.3.0 增量：距离耗电模型 · 六张升级插件 · mixin 接入 RS 本体终端

用户要求：让**所有无线终端**（本模组合成终端 + RS 原生的无线终端 `wireless_grid`、无线自动合成监视器 `wireless_autocrafting_monitor`）按**距离扣电**，配一套分段价目表；把升级插件做进这些设备；加两张「能耗插件」形成 RS 式套娃（初级→高级→远程输电），让电池插件成为前期刚需；充电站给这些物品充电（分段制，跨维度也加价）。用户明确**接受 mixin 路线**。另用户追加：6 张卡全部改叫「XX插件」。

### 19.1 六张卡命名（用户 2026-10-14 追加）

| id | 中文名 | 英文名 | 稀有度 |
|---|---|---|---|
| `battery_upgrade` | 电池升级插件 | Battery Upgrade | RARE |
| `energy_upgrade`（新） | 初级能耗插件 | Energy Upgrade | RARE |
| `advanced_energy_upgrade`（新） | 高级能耗插件 | Advanced Energy Upgrade | EPIC（foil） |
| `range_upgrade` | 距离升级插件 | Range Upgrade | EPIC（foil） |
| `interdimensional_upgrade` | 维度升级插件 | Interdimensional Upgrade | EPIC（foil） |
| `remote_energy_upgrade` | 远程输电插件 | Remote Energy Upgrade | EPIC（foil） |

### 19.2 距离耗电价目表

配置在 `distanceEnergy` 节（`baseRange` + `interdimensionalMultiplier` + `basicEnergyDivisor`）：

| 水平距离 | 倍率（默认） |
|---|---|
| 0–16 格（基础范围 `baseRange`） | ×1 |
| >16 格 | 按距离**线性增长**：倍率 = 距离 ÷ 16 |
| 跨维度（需维度插件） | 固定 ×16（`interdimensionalMultiplier`=1600） |

线性示例：32 格 ×2、64 格 ×4、256 格 ×16、400 格 ×25、1000 格 ≈ ×62。不再用阶梯分段，距离越远收得越狠。

- 跨维度时距离失去意义，直接套 `interdimensionalMultiplier`，**不叠加**距离档位。
- **初级能耗插件**：距离倍率 `÷ basicEnergyDivisor`（默认 2，向上取整，最低 100%）；**高级能耗插件**：距离倍率归 100%（跨维度 ×16 仍保留）；能耗插件**不影响**跨维度倍率。
- 最终 `cost = max(1, base × multiplier% / 100)`；远程输电插件时这笔 `cost` 直接从绑定网络的 `EnergyNetworkComponent.extract` 出账，抽不出才回落物品电池。
- 合成类动作基础耗电更贵：手动合成 20 FE、自动合成 40 FE（打开/清空/配方转移仍 5 FE），体现合成终端相对普通查看终端的额外成本。

### 19.3 基础容量 1000 → 400

`WIRELESS_CRAFTING_GRID_CAPACITY` 改 400，电池插件每张仍 +1000。距离倍率最高能把单次动作抬到上百 FE，400 的底舱在远距离下几下就见底——**电池插件成为前期刚需**。

### 19.4 mixin（唯一动 RS 类的地方）

RS 原生无线终端继承 `AbstractNetworkEnergyItem` 且都**没覆写** 3 参 `use(ServerPlayer, ItemStack, SlotReference)`，因此用一处窄 mixin 覆盖两个父类方法：

- `AbstractNetworkEnergyItemMixin`（`neoforge/mixin`，`@Mixin(AbstractNetworkEnergyItem.class)`）：
  1. `@Inject` 3 参 `use` 头部 → 用 `ExtendedWirelessNetworkItemContext.wrap(stack, player, slot)` 包一层（替代 RS 默认 `createContext`），再调 `@Shadow` 的 4 参抽象 `use`，`ci.cancel()`；
  2. `@Inject` 2 参 `use(Level, Player, InteractionHand)` 头部 → 潜行右键打开同一个插件装配界面（`WirelessCraftingGridUpgradeMenuProvider`）；
  3. `@Inject` `appendHoverText` 尾部 → 追加与本 mod 合成终端一致的展示：绑定位置（维度 + 坐标）、「已安装插件」列表、潜行装配提示（对本 mod 合成终端跳过，避免双写；复用 `WirelessCraftingGridItem.createBoundLine` / `createInstalledUpgradesLine`，两者已提为 public static）。

- mixin 注册：`rswirelesscrafting.mixins.json` + `neoforge.mods.toml` 的 `[[mixins]]`。
- 关键事实（已核实）：NeoForge 1.21.1 运行时用 Mojang 名，RS2 官方 jar（2.0.7）**同样无 refmap**、直接 `@Inject(method="…")` 写 Mojang 名即可，本项目照此办理；`compatibilityLevel` 用 `JAVA_21`（本项目 class 主版本 65）。
- 本 mod 合成终端自己覆写了 3 参 `use`，mixin 不会命中它；`wrap` 静态工厂保证两者走同一套距离耗电 + 卡片放宽逻辑。

### 19.5 升级舱保持 4 槽

`Upgrades.MAX_UPGRADES` 定为 4（初版曾临时扩到 6，按用户最终要求回退）；`UpgradeContainer` / `WirelessCraftingGridUpgradeContainerMenu` 自动跟随。菜单槽 X = 53（4 槽宽 72 居中），`gen_gui_textures.py` 的 `upgrades_gui()` 同步画 4 格。

### 19.6 套娃配方

- `energy_upgrade`（初级）：`CRC/PSP/CRC`，C=铜锭、R=红石块、P=末影珍珠、S=基础处理器。
- `advanced_energy_upgrade`（高级）：`QPQ/XEX/QPQ`，Q=石英富集铁、P=高级处理器、X=4k 存储方块、E=初级能耗插件。
- `remote_energy_upgrade`（远程输电）：中心由电池插件改为**高级能耗插件**，其余（铜块/64k/下界之星/避雷针）不变。

于是套娃链 `初级能耗插件 → 高级能耗插件 → 远程输电插件` 每一环回收上一环，前期投入不打水漂。

### 19.7 关于「便携终端」

RS2 的便携终端（`portable_grid`）是**方块物品**（`PortableGridBlockItem extends AbstractEnergyBlockItem`，用磁盘制），不是手持的 `AbstractNetworkEnergyItem`，也没有「绑定网络 + 距离」概念，因此**不在距离耗电模型内**。它仍能被充电站补电（走通用 FE 能力）。

### 19.8 构建期踩坑

- `CustomImportOrder`：`com.kokuu.rswirelesscrafting.*` 属于 `SAME_PACKAGE(3)` 组，必须排在 `java.*` 之前（mixin 类同样适用）。
- mixin 包要有 `package-info.java`（`JavadocPackage`）。
- `DeclarationOrder`：构造函数必须排在静态工厂方法之前。

### 19.9 充电站：待机损耗可开关 · 红石省电 · 无线 45% 效率 · 4 形态外形

用户 2026-10-14 连续追加，定稿为「充电另算、基础待机损耗单独且要高到有惩罚感」：

- **能量模型拆分**：`chargeItem` 里的充电耗电与基础待机损耗**分开**——待机损耗只由无线模块在运行时常驻扣，充电耗电按充入量实时另算。
- **基础待机损耗默认 128 FE/s**（原 8，用户两次嫌低）：空转约 4 分钟吃空一个基础网络，红石开关才有省电意义。配置总闸 `idleEnergyUsageEnabled`（默认 true）关掉后待机永不扣。
- **红石信号 = 省电开关**：`level.hasNeighborSignal(pos)` 为真时关闭无线充电并停扣待机损耗（有线槽仍工作）。
- **无线充电 45% 效率**（配置 `wirelessChargeEfficiencyPercent`，默认 45，1–100）：`deliver` 里 `cost = ceil(given * 100 / efficiency)`，消耗 100 只给设备充 45；有线恒 100% 无损耗。可用能量按 `available * efficiency / 100` 向下折算可充入量，避免低效率时超支。
- **外形 4 形态**（两个独立亮灭，用户要求「通电/断电」与「无线开/关」分开）：方块加 `POWERED` + `WIRELESS` 两个 `BooleanProperty`，服务端在状态变化时一次性 `setBlockAndUpdate`。纹理用 `gen_textures.py` 的 `terminal_charging_station_texture(powered, wireless)` 生成 4 张——面板中央是电池（通电时白色实心、断电时灰暗）+ 上方广播圆顶（无线开为青色、关为灰色），框线右侧另有两颗深槽指示灯重复这两个状态，各自独立亮灭；方块同时按多面模型出图（顶面充电线圈垫、侧面线缆导管、底面基板），四个状态各有独立顶/侧纹理。
- **关键核实**：本项目充电站用的是 STABLE API 的 `AbstractNetworkNodeContainerBlockEntity`，它**不会**自动调 `node.doWork()` 去 `extract(getEnergyUsage())`（那是非 API 包 `AbstractBaseNetworkNodeContainerBlockEntity` 的 `ticker` 行为），所以待机损耗完全由方块实体手动 `consumeEnergy` 控制，红石/配置开关得以精确生效，不存在双重扣电。

---

## 13. 参考仓库

- **Quartz Arsenal（核心参照，MIT）**：`https://github.com/refinedmods/refinedstorage-quartz-arsenal`（1.21.1 用 `v1.0.8` tag）
- **Refined Storage 2（API 参照，MIT）**：`https://github.com/refinedmods/refinedstorage2`（1.21.1 基线 `v2.0.0`）
- **Interdimensional Wireless Transmitter（社区跨维度思路参照）**：CurseForge/Modrinth 搜 `Interdimensional Wireless Transmitter`
- **RS 2 架构文档**：`https://deepwiki.com/refinedmods/refinedstorage2`
- **RS 2 javadoc**：`https://refinedmods.com/javadoc/refinedstorage2/`

---

*本文档是该模组的完整设计规格。实现时以 §6 伪代码为骨架、§3.4 真实签名为约束、§12 待验证点为核对清单。*
