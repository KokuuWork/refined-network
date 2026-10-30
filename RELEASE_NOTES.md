# Refined Network 0.5.0 — 未发布说明草稿

**这是开发分支说明，不是已经发行的版本或完成验收的公告。** 发布前必须核对 tag、三模块版本、产物、依赖与验证结果。当前发布流程会原样读取本文件，手动发布的 tag 检出和版本一致性检查尚待修正；不要直接运行发布流程。

## 已进入开发分支的内容

- 显示名与注册标识统一为「精致网络 / Refined Network」（`refinednetwork`）：modId、命名空间、包名、模块与产物前缀一并重写。**不提供旧 `rswirelesscrafting` 存档迁移**，旧世界请勿原地升级。
- 无线合成终端无可用网络时可按配置打开本地 3×3 合成模式，使用背包原料，不是离线自动合成队列。开关：`wirelessCraftingGrid.offlineModeEnabled`。
- 无线红石端点以输入 / 输出配对，读取红石后输出 0 / 15，每 2 tick 采样；可跨维度，不主动保活区块。
- 文档、双语提示与语言生成源勘误；新增开发规范和可验收的后续任务。不包含游戏功能修复。

## 发布前阻断项

计费与电池兜底、失败自动合成收费、样板回滚、会话权限、虚拟管理器激活、桥接缓存和红石权限 / 失效 / 掉落等需按 [Issue 索引](docs/ISSUES.md) 处理。部分问题只有代码依据、尚未实机复现。离线窗口恢复网络时的行为也需验收。

本版为硬重写：旧世界的物品、方块、数据组件、配方与配置引用不会被转换。升级前务必备份并新建世界；需要保留旧存档时继续使用旧 JAR。

## 安装与升级草稿

目标环境：Minecraft 1.21.1、Java 21、对应加载器的 RS 2。开发编译基线 RS 2.0.7；其他小版本须独立测试。Fabric 额外依赖 Fabric API、Cloth Config API 和 Team Reborn Energy，以最终发行元数据为准。

按加载器选一个 JAR，不安装 sources JAR。升级前备份世界与配置，在隔离世界验证；保留并按需迁移自定义配置，不默认删除。卸载前处理已配对桥的区块保活。

## 验证状态

2026-10-30 本轮只修改文案、规范和任务材料，未重新构建、启动客户端 / 专服或测试旧存档。已有0.5.0本地产物与历史7项纯逻辑测试不作为完整验收。发布时填写真实测试记录，未执行的项目必须保留“未验证”。

历史发行说明：[0.4.1](docs/releases/v0.4.1.md)、[0.3.4](docs/releases/v0.3.4.md)、[0.3.2](docs/releases/v0.3.2.md)。正文勘误不会改变已有 tag、二进制、发布日期或原发行状态。

## English

**Unpublished development notes, not a release announcement.** Main contains the identity rewrite to `refinednetwork`, local offline crafting and wireless redstone endpoints. The old `rswirelesscrafting` namespace is **not** migrated; existing worlds must not be upgraded in place. Offline crafting is an inventory-backed local crafting table, not a persistent order queue. Redstone relays 0 / 15 every 2 ticks without keeping chunks loaded.

Billing, rollback, session validation, manager activation, bridge cache and redstone safety / drops need further work or verification. Check [tracked tasks](docs/ISSUES.md) before release. No new build, in-game, dedicated-server or save-migration validation was performed for this documentation update.

Use Minecraft 1.21.1, Java 21 and matching loader / RS builds; verify the final dependency metadata. Back up saves and customized configurations. Do not promise lossless namespace migration or delete configurations unconditionally. Check tag, source version, artifacts and test evidence before publishing.
