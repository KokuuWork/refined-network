<!-- GitHub Release 正文，由 release.yml 读取。 -->

## RS Wireless Crafting 0.3.4

**0.3.x 稳定线修复版。** 修复 0.3.3 引入的崩溃回归，并让终端充电站可以正常连接线缆。不含 0.4.x 的新设备。

### 下载

| 加载器 | 文件 | 环境 |
| --- | --- | --- |
| NeoForge | `rswirelesscrafting-neoforge-0.3.4.jar` | Minecraft 1.21.1 + NeoForge |
| Fabric | `rswirelesscrafting-fabric-0.3.4.jar` | Minecraft 1.21.1 + Fabric Loader / Fabric API |

两个文件按加载器**二选一**。都需要 Refined Storage 2（2.0.7 及以上，已在 2.0.9 上验证）。建议先备份存档，再删除旧 jar 并替换。

### 本版修复

- **关闭终端导致服务端崩溃**：0.3.3 在注册界面 watcher 时检查开界面费用，余额不足时直接跳过注册。关闭界面时精致存储移除未注册的 watcher 会抛 `IllegalArgumentException`；断线时异常进入 tick 循环，服务端直接崩溃。现在无论费用是否扣成功都会完成注册，移除也改为幂等，重复关闭直接忽略。
- **电量不足时打开空界面**：打开前先检查电量，不足时提示「终端电量不足，请先充电。」，不再出现界面能开但物品列表不刷新的状态。
- **终端充电站不认线缆**：补齐方块类型上的网络容器能力，线缆与网络现在能直接发现充电站，不再只能贴着控制器或线缆方块放置。NeoForge 与 Fabric 双端均已补齐。

### 升级说明

- 存档、配方与物品栏引用与 0.3.3 一致，直接替换 jar 即可。
- 0.3.3 的崩溃只在特定条件触发（终端电量不足以支付打开费用时打开终端），但一旦触发会让服务端崩溃，建议尽快升级。

### 已知限制

- 配置文件为 COMMON 类型，专用服务器不会把配置下发到客户端；终端悬停显示的数值取自本地配置，实际扣费以服务端为准。

### 反馈

请到 [Issues](https://github.com/KokuuWork/rswirelesscrafting/issues) 反馈，也可以加 QQ 群 `498700382`。
