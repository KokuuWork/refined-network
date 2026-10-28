# rswirelesscrafting 0.4.1-beta.1

**开发测试版。不是稳定正式版。**

这个版本把无线终端扩展成一套完整的远程工具体系，并修复了网络桥接、配对、权限、能量和 GUI 生命周期问题。两个加载器的构建、checkstyle 和 7 个纯逻辑测试已经通过，但还没有完成完整的实机、专用服务器、跨维度和旧存档验证。请先在测试存档中使用，并备份世界。

## 下载

- `rswirelesscrafting-neoforge-0.4.1-beta.1.jar`
- `rswirelesscrafting-fabric-0.4.1-beta.1.jar`

需要 Minecraft 1.21.1 和 Refined Storage 2.0.7 或兼容版本。NeoForge 与 Fabric **二选一**，不要同时安装。升级前请删除旧版本 jar。

## 新增内容

- 无线样板终端：远程编辑自动化样板。
- 无线自动合成仓管理器：远程管理全网合成仓的样板。
- 无线增幅塔：覆盖范围内降低终端距离倍率。
- 无线桥：基点/据点配对、跨维度连接和区块保活。
- 无线配置卡：单个复制/粘贴，或批量粘贴同类型方块配置。
- 无线监控终端：阈值提醒和自动补货。
- 超频升级卡：RS 自动合成仓等待间隔折半，最多 4 张。
- 无线能量接收器：把远处 FE 发电接入绑定网络。
- 能量监控仪：存量、容量、净收支和预计耗尽时间。
- 智能分拣站：按物品或标签分流到面前容器。
- 网络桥接器：在两张独立网络之间转发物品和能量。
- 手持 HUD：显示终端电量、距离和倍率，默认关闭。

## 美术重制

物品图标与方块纹理整套重绘，风格向 RS2 原版靠拢，**资源路径与 blockstate 未改动**，旧存档、配方和模型引用都不受影响。

- 机壳改为中性灰钢板 + 1px 深灰描边 + 左上斜面高光，去掉旧版的钢蓝配色与四角铆钉。
- 六面统一深灰内嵌面板：同位置、同框线、同内阴影；顶面最亮、侧面居中、底面最暗。
- 方块改用多面模型：正面功能图形、侧面机壳与结构侧视、顶面俯视功能件、底面暗色基板；侧面结构与正面共用 x 坐标，机器骨架绕过棱边。
- 信号色语义不变：**青 = 无线/网络，灰蓝 = 待机，红 = 低电，白 = 通电**，每个状态在正面、顶面和侧面都有对应元素。
- **版本分界**：0.3.3 与 0.4.1-beta.1 使用这套新美术；0.3.2 及更早版本保持旧美术，不再回改。

## 主要修复

- 网络桥接器接入 RS 复合存储缓存，避免先放后绑、远端库存变化后列表为空或过期。
- 无线桥补上实际配对流程，重新绑定和解绑会清理旧链路及旧目标区块。
- 节点进入新网络前会离开旧网络，避免同一节点同时计入两张网络。
- 无线桥停机、配置关闭和欠费时不再继续扣费；增幅塔欠费后停止提供折扣。
- 网络桥费要求实际足额扣除，不能只凭余额判断。
- 虚拟终端按实际距离倍率预检费用；自动合成和补货先检查权限与费用，不再先建单后欠费。
- 监控、充电站、无线桥、接收器、监控仪和配置卡加入权限检查。
- 充电站同时检查 OPEN 和 EXTRACT，后台无线充电也会复核授权。
- 虚拟样板终端物品移动或替换后关闭菜单，不再向旧物品写状态。
- 分拣站按 WorldlyContainer 输入面校验，避免向错误面写入物品。
- 接收器、充电站容量和网络桥接器限速支持当前配置值，不再只读取构造时的旧值。
- 能量和速率计算增加溢出保护，避免大配置产生负费用。
- 增幅塔登记按世界实例隔离，修复集成端客户端/服务端互相影响。
- 服务器停止时清理本模组的强加载记录和配对剪贴板。
- 超频 mixin 的 Shadow 字段明确禁用 remap；Fabric 能量适配器使用事务快照。
- 分拣站增加正面纹理和定向模型，README 补齐全部物品图标。

## 验证范围

- NeoForge 构建通过。
- Fabric 构建通过。
- checkstyle 通过。
- 7 个 JUnit 测试通过。
- 资源引用和语言键检查通过。

以下仍需实机确认：

- 先放桥后绑定、远端插取、断线重连和 RS 图重建后的库存一致性。
- RS 安全网络下的绑定、打开、后台补货和无线充电。
- 跨维度、区块卸载、服务器重启和旧存档迁移。
- 第三方 Capability/Storage 容器、双箱和超频 mixin 的生产环境行为。

## 反馈

- QQ 群：**498700382**
- 加群链接：<https://qm.qq.com/q/ueYTU8T3xK>
- Issues：<https://github.com/KokuuWork/rswirelesscrafting/issues>

请附上加载器、RS 版本、复现步骤和 `latest.log`。如果遇到库存或能量异常，请同时记录绑定位置和是否使用了桥接器。

---

# rswirelesscrafting 0.4.1-beta.1 — Development preview

**This is a development preview, not a stable release.**

Both loaders build successfully, checkstyle passes and all 7 unit tests pass. Full in-game, dedicated-server, cross-dimension and saved-world verification is still pending. Use a test world and make a backup first.

This preview adds wireless pattern editing, autocrafter management, wireless amplification, wireless bridges, configuration cards, monitoring and restocking, overclock upgrades, energy receivers, energy monitors, smart sorting and network bridges.

It also fixes RS composite-storage cache synchronization, bridge pairing and lifecycle, power/permission checks, virtual terminal billing, sorter input faces, configuration reload behavior, energy overflow, client/server registries and Fabric energy transactions.

Every item icon and block texture has been redrawn in an RS2-leaning style: neutral grey casings with a dark inset panel, multi-face block models (functional front, structural sides, top-down part, dark base plate) and cyan reserved for wireless/network state. Resource paths are unchanged. 0.3.3 ships the same new art; 0.3.2 and older keep the old textures.

- Minecraft 1.21.1
- Refined Storage 2.0.7 or compatible
- NeoForge **or** Fabric, not both
- Remove the old jar before upgrading

Feedback: QQ group **498700382** — <https://qm.qq.com/q/ueYTU8T3xK>, or open an issue with your loader, RS version, reproduction steps and `latest.log`.
