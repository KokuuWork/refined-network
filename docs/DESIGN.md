# 设计文档：无线合成终端附属模组
## 无线合成终端 + 范围升级卡 + 维度升级卡（Refined Storage 2 · NeoForge 1.21.1）

> **文档用途**：本报告是一份**自包含**的完整实现规格，供代码生成模型 / 开发者在**不依赖本对话上下文**的前提下，直接据此编写代码。所有关键类名、方法签名、扩展点、平衡数值均已核实到 Refined Storage 2（下称 RS 2）与官方附属 Quartz Arsenal 的真实源码。

---

## 1. 项目概览

| 项 | 内容 |
|---|---|
| 定位 | RS 2 的**纯附属模组**（addon），不 fork、不 mixin RS 本体 |
| 目标 MC | **1.21.1** |
| 加载器 | **NeoForge**（21.1.x） |
| 依赖 | Refined Storage **2.0.x**（1.21.1 线，写作基线 **v2.0.0**，即 1.21.1 最新 release） |
| 参照源码 | Quartz Arsenal **v1.0.8**（1.21.1 线，MIT） |
| 核心产出 | ① 无线合成终端 ② 范围升级卡 ③ 维度升级卡（共 3 个 Item） |
| 差异化 | ① 同维度无线距离可加宽（默认 64 格，可配）；② **范围卡**→同维度无限范围；③ **维度卡**→解锁跨维度，走 RS 原生 Network Transmitter/Receiver 隧道（网卡绑 Receiver） |

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
| D3 | 物品清单 | 无线合成终端 + 范围升级卡 + 维度升级卡（**3 个 Item**，无创造版） |
| D4 | 跨维度机制 | **方案 B（隧道）**：网卡绑 Network Receiver，走 Network Transmitter/Receiver 隧道 |
| D5 | 卡片拆分 | **范围卡**只扩同维度距离；**维度卡**只解锁跨维度。两张卡正交、互不干扰 |
| D6 | 能量模型 | 终端耗 FE、可配置关闭；跨维度时能量消耗 +50% |
| D7 | 槽位 | 1 网卡槽 + 4 升级槽；两种卡各 1 张即生效，可堆叠 |
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
- 跨维度时以上能量消耗 ×1.5（见 §7.5）。
- 配方（占位，可调）：`refinedstorage:wireless_grid` + `refinedstorage:construction_core` + `refinedstorage:crafting_grid`（或 1× 工作台 + 3× 高级处理器），产出 1 个。

### 4.3 范围升级卡（Range Upgrade）

- 普通 `Item`（无能量、无网络绑定）。
- 判定方式：**物品 tag** `rswirelesscrafting:range_upgrades`（终端遍历升级槽，命中即视为已升级）。
- 作用：同维度绑 Wireless Transmitter 时，使 `resolveNetwork` **无视距离**（无限范围）。**不作用于**跨维度（跨维度走隧道，天然无限）。
- 配方（终端本身用 `refinedstorage:crafting_grids` 标签，兼容全部网格变体）：

  ```
  E A E      E = refinedstorage:quartz_enriched_iron
  P U P      A = refinedstorage:advanced_processor
  E M E      P = c:ender_pearls（标签）
             U = refinedstorage:wireless_transmitter
             M = refinedstorage:machine_casing
  ```

  **设计意图**：卡的效果正是「绑 Wireless Transmitter 时无视距离」，所以核心直接放一台 Wireless Transmitter —— 把设备压成一张卡。整卡合计约 12 石英铁 + 3 末影珍珠 + 2 高级处理器 + 2 机器外壳，显著高于 RS 原生 `range_upgrade`（6 石英铁 + 3 末影珍珠 + 1 升级基板），以匹配「一次到位、永久无限」而非「+1 档可叠加」的强度差。
- 可堆叠 64。

### 4.4 维度升级卡（Interdimensional Upgrade）

- 普通 `Item`（无能量、无网络绑定）。
- 判定方式：**物品 tag** `rswirelesscrafting:interdimensional_upgrades`。
- 作用：解锁「网卡绑 Network Receiver → 走隧道」的跨维度能力（见 §7）。
- 配方：

  ```
  P N P      P = c:ender_pearls（标签）
  N C N      N = c:ingots/netherite（标签）
  P N P      C = refinedstorage:network_card
  ```

  **设计意图**：跨维度是本 mod 的终极能力，价位对齐并**超过** RS 自带的跨维隧道设备（`network_transmitter` / `network_receiver`，各含 1 下界合金 + 2 高级处理器 + 机器外壳 + 2 核心 + 3 末影珍珠）。4×下界合金（≈16 远古残骸）+ 4×末影珍珠 + 1 网络卡的组合，保证它是一件真正的终局物品。
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
- 所有会耗能的操作（open/craft/autocraft/clear/transfer）调 `context.drainEnergy(...)`，跨维度时先 ×1.5（见 §7.5）。

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

- 所有 `drainEnergy(amount)` 在**跨维度**（走隧道）时统一 ×1.5，向上取整；同维度（含范围卡无限距离）走标准耗能，不加价。

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

## 13. 参考仓库

- **Quartz Arsenal（核心参照，MIT）**：`https://github.com/refinedmods/refinedstorage-quartz-arsenal`（1.21.1 用 `v1.0.8` tag）
- **Refined Storage 2（API 参照，MIT）**：`https://github.com/refinedmods/refinedstorage2`（1.21.1 基线 `v2.0.0`）
- **Interdimensional Wireless Transmitter（社区跨维度思路参照）**：CurseForge/Modrinth 搜 `Interdimensional Wireless Transmitter`
- **RS 2 架构文档**：`https://deepwiki.com/refinedmods/refinedstorage2`
- **RS 2 javadoc**：`https://refinedmods.com/javadoc/refinedstorage2/`

---

*本文档是该模组的完整设计规格。实现时以 §6 伪代码为骨架、§3.4 真实签名为约束、§12 待验证点为核对清单。*
