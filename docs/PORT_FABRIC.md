# 移植计划：Fabric 加载器（MC 1.21.1）

> ✅ **已完成（v0.3.1）**。本文档保留原调研计划，实际执行结果与踩坑见文末「七、实际执行结果」。
>
> 目标：把「无线合成终端」从 **NeoForge 1.21.1** 移植到 **Fabric 1.21.1**，覆盖 RS2 2.x 的全部加载器。
> 用户已明确排除 RS 3.x（MC 26.1.2）。

## 一、调研结论（已用 Modrinth API / GitHub 核实）

**Refined Storage 2（refinedstorage2 代码库）2.x 系列只支持一个游戏版本：**

| 加载器 | MC 版本 | RS2 依赖 | 状态 |
|---|---|---|---|
| NeoForge | 1.21.1 | `refinedstorage-neoforge:2.0.9` | ✅ 已完成（v0.3.0） |
| Fabric | 1.21.1 | `refinedstorage-fabric:2.0.9` | ✅ 已完成（v0.3.1） |

- 3.x 系列（26.1.2）按用户要求**不做**。
- 早于 1.21.1 的版本（1.20.1/1.20.4 等）是 **RS1（1.x 老代码库）**，与 RS2 无关，不在范围内。

## 二、现状与有利条件

本项目已是标准的多平台架构（与官方附属 `quartz-arsenal` 完全同构）：

- 三模块：根 / `rswirelesscrafting-common`（平台无关）/ `rswirelesscrafting-neoforge`（NeoForge 实现）。
- `common/Platform.java` 已把两处平台差异抽象成接口：**配置**（`Config`）与**物品能量**（`ItemEnergyProvider`）。
- `settings.gradle.kts` 已预置 `com.refinedmods.refinedarchitect.fabric` 插件与 Fabric maven，只差 `include`。
- 官方附属 `reference/quartz-arsenal` 提供完整三模块 fabric 模板（26.1 版，但 2.0.7 写法已单独核对，见 §四）。
- RS2 2.0.7 自身的 `refinedstorage-fabric` 模块提供了全部所需 API：
  `RefinedStoragePlugin`、`DirectRegistryCallback`、`support/energy/EnergyStorageAdapter`。

**结论：common 模块一行不用改，只需新增一个 `rswirelesscrafting-fabric` 模块。**

## 三、移植工作清单

### 1. 构建脚本

- `settings.gradle.kts`：`include("rswirelesscrafting-fabric")`。
- 新建 `rswirelesscrafting-fabric/build.gradle.kts`（模板 = quartz-arsenal 的 fabric 模块，改 modId）：
  - 插件 `id("com.refinedmods.refinedarchitect.fabric")`
  - `refinedarchitect { modId = "rswirelesscrafting"; fabric() }`
  - `version = "0.3.1"`（发新小版本）
  - 依赖：`compileOnly(project(":rswirelesscrafting-common"))` + `commonJava`/`commonResources`（同 neoforge 模块）
  - `api("com.refinedmods.refinedstorage:refinedstorage-fabric:2.0.9")`
  - maven：Cloth Config（`maven.shedaniel.me`）、Team Reborn Energy（`maven.terraformersmc.com` 或 Fabric 官方）

### 2. 模块元数据 `fabric.mod.json`

模板取自 RS2 2.0.7 自己的 `fabric.mod.json`（已核对）：

```jsonc
{
  "schemaVersion": 1,
  "id": "rswirelesscrafting",
  "version": "${version}",
  "name": "Refined Network",
  "license": "MIT",
  "environment": "*",
  "entrypoints": {
    "main": ["com.kokuu.rswirelesscrafting.fabric.ModInitializerImpl"],
    "client": ["com.kokuu.rswirelesscrafting.fabric.ClientModInitializerImpl"],
    "refinedstorage_plugin": ["com.kokuu.rswirelesscrafting.fabric.ModInitializerImpl"]
  },
  "mixins": ["rswirelesscrafting.mixins.json"],
  "depends": {
    "fabricloader": ">=0.14.6",
    "fabric-api": ">=0.101.0",
    "minecraft": "~1.21",
    "java": ">=21",
    "cloth-config2": "*",
    "team_reborn_energy": "*"
  }
}
```

### 3. 平台入口（6 个 Java 文件，全部落在 `com.kokuu.rswirelesscrafting.fabric` 包）

| 文件 | NeoForge 对应 | Fabric 实现要点 |
|---|---|---|
| `ModInitializerImpl.java` | `ModInitializer` | `implements ModInitializer + RefinedStoragePlugin`；`onApiAvailable(api)` 里注册内容 + 能量 + 创造栏；`onInitialize()` 里 `AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new)` |
| `ClientModInitializerImpl.java` | `ClientModInitializer` | `MenuScreens.register`、`KeyMappingHelper.registerKeyMapping` + `ClientTickEvents.END_CLIENT_TICK`、`ItemProperties.register` |
| `ConfigImpl.java` | `ConfigImpl`（ModConfigSpec） | 改用 **Cloth Config**：`@Config` + `@ConfigEntry.BoundedDiscrete` 注解，实现 common 的 `Config` 接口 |
| `PlatformImpl.java` | `PlatformImpl`（NeoForge Capability） | 能量读取改用 `team.reborn.energy.api.EnergyStorage.ITEM.find(stack, ctx)` |
| `ChargingStationEnergyStorage` 等价物 | `ChargingStationEnergyStorage`（IEnergyStorage） | 方块能量用 Team Reborn Energy 的 block 注册（见 §四.3） |
| mixin | `AbstractNetworkEnergyItemMixin` | **原样复用**（见 §四.4），只需在 `fabric.mod.json` 声明 mixins |

### 4. 资源

- `fabric.mod.json`（上述）。
- `rswirelesscrafting.mixins.json`：**复用** neoforge 模块那份（`compatibilityLevel` 改 `JAVA_21`，mixin 类路径一致）。
- 语言/模型/纹理/配方/方块状态：全部在 common 模块，Fabric 自动继承，**无需复制**。

## 四、关键实现差异（NeoForge → Fabric 对照）

### 4.1 内容注册

- NeoForge：`DeferredRegister` + 自定义 `ForgeRegistryCallback`，`IMenuTypeExtension.create(...)`。
- Fabric：RS 提供 `DirectRegistryCallback`（直接 `Registry.register`），菜单用 **Fabric API 的** `ExtendedMenuType`：
  ```java
  registerMenus(new DirectRegistryCallback<>(BuiltInRegistries.MENU), new ExtendedMenuTypeFactory() {
      public <T extends AbstractContainerMenu, D> MenuType<T> create(MenuSupplier<T, D> s,
              StreamCodec<RegistryFriendlyByteBuf, D> codec) {
          return new ExtendedMenuType<>(s::create, codec);
      }
  });
  ```

### 4.2 配置（ConfigImpl）

NeoForge 用 `ModConfigSpec`（builder + `defineInRange`）；Fabric 用 Cloth Config：

```java
@Config(name = "rswirelesscrafting")
public class ConfigImpl implements ConfigData, Config {
    @ConfigEntry.Gui.CollapsibleObject
    private ChargingStationEntryImpl chargingStation = new ChargingStationEntryImpl();
    // ... 每项用 @ConfigEntry.BoundedDiscrete(min, max) 约束
    public static ConfigImpl get() { return AutoConfig.getConfigHolder(ConfigImpl.class).getConfig(); }
}
```

注意：common 的 `Config` 接口（`ChargingStationEntry`、`DistanceEnergyEntry` 等）字段原样照搬，只换实现层。

### 4.3 能量（PlatformImpl + 方块能量）

- **物品能量读取**（PlatformImpl，充电站扫描背包用）：
  ```java
  team.reborn.energy.api.EnergyStorage.ITEM.find(stack, ctx)
  ```
- **物品能量注册**（本 mod 无线终端的对外能量，2.0.7 单参签名，**勿照抄 26.1 的双参写法**）：
  ```java
  EnergyStorage.ITEM.registerForItems(
      (stack, context) -> new EnergyStorageAdapter(
          Items.INSTANCE.getWirelessCraftingGrid().createEnergyStorage(stack), context),
      Items.INSTANCE.getWirelessCraftingGrid());
  ```
- **方块能量**（充电站外部 FE 缓冲）：Team Reborn Energy 的 `EnergyStorage.SIDED.registerForBlocks(...)`，桥接 `ChargingStationBlockEntity.receiveExternalEnergy/getExternalEnergy/getExternalCapacity`。

### 4.4 mixin（可复用）

已核实：RS2 2.0.7 的 fabric mixin **同样用 Mojang 名**（如 `@Accessor("canLoseFocus")`），refinedarchitect 的 fabric 插件默认 Mojang mappings。因此本项目的 `AbstractNetworkEnergyItemMixin`（`@Inject` + `@Shadow` 全是 Mojang 名）**无需改方法名即可复用**，仅需：

- 类包名从 `...neoforge.mixin` 迁到 `...fabric.mixin`（或直接放 fabric 模块同包）。
- 在 `fabric.mod.json` 的 `"mixins"` 字段声明（NeoForge 是 `neoforge.mods.toml` 的 `[[mixins]]`）。

### 4.5 客户端

- 按键：NeoForge `RegisterKeyMappingsEvent` + `KeyConflictContext` → Fabric `KeyMappingHelper.registerKeyMapping` + `ClientTickEvents.END_CLIENT_TICK` 里 `handleInputEvents()`。
- 屏幕：两边都是 `MenuScreens.register`（Fabric 用 vanilla `net.minecraft.client.gui.screens.MenuScreens`）。
- item properties：`ItemProperties.register` 两边一致。

## 五、待核对点（动手时逐一确认，不阻塞启动）

1. `ContainerItemContextEnergyItemContext` 是 26.1 新增，**2.0.7 没有**——能量注册用 §4.3 的单参写法即可，勿引入该类。
2. `NetworkItemPropertyFunction`（RS common 类）在 fabric 侧的 item property 注册签名。
3. 充电站方块能量的 Team Reborn Energy 注册 API 精确签名（`EnergyStorage.SIDED.registerForBlocks`）。
4. Fabric loader / fabric-api 的精确版本（从 RS2 2.0.7 的 `gradle/libs.versions.toml` 抄，当前已知下限 `fabricloader>=0.14.6`、`fabric-api>=0.101.0`）。
5. checkstyle：fabric 包同样需要 `package-info.java` + `CustomImportOrder` 把 `com.kokuu.*` 排 `java.*` 之前。

## 六、实施顺序（建议分 4 阶段）

1. **骨架**：`include` + `build.gradle.kts` + `fabric.mod.json` + 空 `ModInitializerImpl`/`ClientModInitializerImpl`，先编译通过、物品/方块能进游戏。
2. **平台抽象**：`PlatformImpl`（能量读取）+ `ConfigImpl`（Cloth Config）+ 方块能量注册 + 物品能量注册。
3. **客户端 + mixin**：屏幕/按键/item properties + `AbstractNetworkEnergyItemMixin` 迁移与声明。
4. **验证发布**：`./gradlew build`（含 fabric 产物）→ 真机 Fabric 环境（装 `fabric-api` + `cloth-config` + `team_reborn_energy`）→ CI 加 fabric 构建 → 打 tag 发 Release（附件同时带 neoforge + fabric 两个 jar）。

## 七、版本与发布

- 版本号：**0.3.1**（新增 Fabric 加载器，功能不变）。
- CI（`.github/workflows/build.yml` / `release.yml`）已加 fabric 构建与附件；`release.yml` 附件仍用精确文件名（`rswirelesscrafting-fabric-<ver>.jar` + `rswirelesscrafting-neoforge-<ver>.jar`）。

## 八、实际执行结果（v0.3.1，构建已通过）

四个阶段全部落地，`./gradlew build -x test` 全绿，fabric 产物 `rswirelesscrafting-fabric-0.3.1.jar`（约 143 KB）正常生成，mixin refmap（`rswirelesscrafting-fabric-refmap.json`）正确。

### 8.1 与 26.1 模板的关键差异（照抄 quartz-arsenal 会编译失败）

quartz-arsenal 的 fabric 模块是 **26.1 版**，其 API 与 1.21.1 不同，以下几处必须按 2.0.7 改：

1. **依赖必须用 `modApi`，不能用 `api`**。1.21.1 的 `refinedstorage-fabric-2.0.7.jar` 是 **intermediary 映射**（26.1 已是 Mojang 映射），用 `api` 不触发 Loom remap，编译时报 103 个 `class_2378`（`Registry`）类型不匹配。同时要把 cloth / energy / modmenu 显式提升为 `modApi`，否则作为传递依赖不被 remap。
2. **按键**：1.21.1 是 `KeyBindingHelper.registerKeyBinding`（`net.fabricmc.fabric.api.client.keybinding.v1`），不是 26.1 的 `KeyMappingHelper.registerKeyMapping`；`KeyMapping` 构造第 4 参是**分类翻译键字符串**（`ContentNames.MOD_TRANSLATION_KEY`），不是 26.1 的 `KeyMapping.Category`。
3. **创造栏**：1.21.1 是 `ItemGroupEvents.modifyEntriesEvent`（`net.fabricmc.fabric.api.itemgroup.v1`），不是 26.1 的 `CreativeModeTabEvents.modifyOutputEvent`（`creativetab.v1` 包是后来才有的）。回调参数是 `FabricItemGroupEntries`（`implements CreativeModeTab.Output`），`entries::accept` 可用。
4. **菜单**：1.21.1 是 `ExtendedScreenHandlerType`（`net.fabricmc.fabric.api.screenhandler.v1`），不是 26.1 的 `ExtendedMenuType`。

### 8.2 依赖仓库补丁

`refinedstorage-fabric:2.0.7` 的 POM 把 `com.terraformersmc:modmenu:11.0.3` 声明为 `compile` scope，而 modmenu 只存在于 `maven.terraformersmc.com`。必须给 fabric 模块补该仓库（`content { includeGroup("com.terraformersmc") }`），否则依赖解析失败。

### 8.3 初始化顺序（与官方附属一致，无坑）

RS 的 `onInitialize` 会在末尾 `FabricLoader.getEntrypoints("refinedstorage_plugin")` 并同步调用本模组的 `onApiAvailable`（早于本模组自己的 `onInitialize`）。因为 `registerContent` 只注册方块/物品/菜单/数据组件、**不读配置**（Item 构造不碰 Config，能量容量在运行时查询），所以 `AutoConfig.register` 放在 `onInitialize` 的官方写法无需改动。

### 8.4 mixin 映射

fabric 侧 mixin 源码仍用 Mojang 名（与 neoforge 共用同名逻辑），Loom 自动 remap：`appendHoverText`→`method_7851`、`Item.use`(2 参)→`method_7836`、RS 自有的 3 参 `use(ServerPlayer,ItemStack,SlotReference)` 方法名不变（仅参数类型 remap）。
