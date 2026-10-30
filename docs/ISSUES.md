# 后续任务与依赖索引

2026-10-30已创建并逐条读回 **34个GitHub Issue**。原仓库开放/关闭Issue列表为空，创建前已查询去重。每条正文包含背景、目标、真实路径、需求、范围/非目标、约束、验收、验证和Flash接手提示词；缺陷附复现条件及代码/实测区别。

**本轮只整理任务，没有实现游戏功能或迁移modId。** Issue优先级不等于风险已实测；所有“静态推断”先核实。当前基线为`04bc9f70670d3288dfacf39703c10122a35aa64b`，后续改名以批准映射定位，不把旧路径改回去。

类型：13缺陷、8优化、7新功能、6调研决策。P1发布前优先/阻断，P2近期，P3后续。Issue均开放；依赖在正文写明链接及启动条件，本次不声称已设置GitHub原生依赖图或已验收任何条目。

## 推荐起步

- 第一件：[M01 / #1](https://github.com/KokuuWork/refined-network/issues/1)，确认准确新modId和旧世界策略。`refinednetwork`只是候选，不是已批准值。
- 可并行核实/修复：D01供电兜底、D02空任务扣费、D05管理器、D09/D10/D11红石、D12桥缓存、D16发布流程；每项独立分支/提交，不混入namespace切换。
- 迁移链 M01→M04 已完成：标识统一为 `refinednetwork`，不提供旧存档迁移。
- 基础缺陷处理后：N01诊断、N02四终端快捷入口；随后N03命名档案。
- 后续内容：N04–N07为明确小切片；N08订单、N09票据、N10大型附属首原型先设计评审，不直接写功能。

## modId迁移

| ID / Issue | 工作 | 依赖 |
|---|---|---|
| [M01 / #1](https://github.com/KokuuWork/refined-network/issues/1) | 决策：新ID `refinednetwork`，硬重写、不做迁移 | ✅ 已完成（已关闭） |
| [M02 / #2](https://github.com/KokuuWork/refined-network/issues/2) | 标识盘点 | ✅ 随重写完成（已关闭），历史文档保留旧名 |
| [M03 / #3](https://github.com/KokuuWork/refined-network/issues/3) | modId/包名/模块/资源/产物重写 | ✅ 双平台构建通过（已关闭） |
| [M04 / #4](https://github.com/KokuuWork/refined-network/issues/4) | 旧世界兼容 | ❌ 按决定不做；旧世界须用旧 JAR 或新建世界 |

## 缺陷与质量补强

| ID / Issue | 工作 | 优先级 / 证据 | 硬依赖 |
|---|---|---|---|
| [D01 / #5](https://github.com/KokuuWork/refined-network/issues/5) | 虚拟终端电池兜底/免费预检 | P1，代码确认 | 无 |
| [D02 / #6](https://github.com/KokuuWork/refined-network/issues/6) | 空任务结果不收费 | P1，代码确认 | 无 |
| [D03 / #7](https://github.com/KokuuWork/refined-network/issues/7) | 手工欠费领取复现及收费政策 | P1，推断/待决策 | 无 |
| [D04 / #8](https://github.com/KokuuWork/refined-network/issues/8) | 样板编码整次快照/回滚 | P1，推断需复现 | D01 |
| [D05 / #9](https://github.com/KokuuWork/refined-network/issues/9) | 管理器激活/槽位显示 | P1，推断需复现 | 无 |
| [D06 / #10](https://github.com/KokuuWork/refined-network/issues/10) | 菜单身份/权限/目标复核 | P1，现入口确认，越权未实测 | D05 |
| [D07 / #11](https://github.com/KokuuWork/refined-network/issues/11) | 离线窗口入口隔离/重开 | P1，入口缺口/表现待测 | D06 |
| [D08 / #12](https://github.com/KokuuWork/refined-network/issues/12) | 同维范围卡与后台准入 | P1，代码确认/待策略 | 无 |
| [D09 / #13](https://github.com/KokuuWork/refined-network/issues/13) | 红石所有者配对/解绑 | P1，缺调用确认 | 无 |
| [D10 / #14](https://github.com/KokuuWork/refined-network/issues/14) | 红石输入实体验证/归零 | P1，坐标读确认 | 无 |
| [D11 / #15](https://github.com/KokuuWork/refined-network/issues/15) | 红石生存掉落和生成源 | P1，资源缺失确认 | 无 |
| [D12 / #16](https://github.com/KokuuWork/refined-network/issues/16) | 远端变化主动缓存更新 | P1，机制缺口/需复现 | 无 |
| [D13 / #17](https://github.com/KokuuWork/refined-network/issues/17) | 多桥/环路守恒支持矩阵 | P1，测试调研，无确认复制 | D12 |
| [D14 / #18](https://github.com/KokuuWork/refined-network/issues/18) | 插件/规则固定四槽序列 | P2，序列压缩确认 | 无 |
| [D15 / #19](https://github.com/KokuuWork/refined-network/issues/19) | 冷却按规则/终端/网/组件隔离 | P2，键缺字段确认 | D14 |
| [D16 / #20](https://github.com/KokuuWork/refined-network/issues/20) | 发布检出tag/版本/说明/产物检查 | P1，workflow缺ref确认 | 无 |
| [D17 / #21](https://github.com/KokuuWork/refined-network/issues/21) | 当前兼容矩阵/依赖范围证据 | P1，宽声明确认 | 无 |
| [D18 / #22](https://github.com/KokuuWork/refined-network/issues/22) | 语言/百科生成一致性与条目覆盖 | P2，覆盖不足确认 | 无 |
| [D19 / #23](https://github.com/KokuuWork/refined-network/issues/23) | 仅Java注释/Javadoc勘误 | P2，旧注释滞后 | 无 |
| [D20 / #34](https://github.com/KokuuWork/refined-network/issues/34) | 分拣批次/堆叠限速和费用口径 | P2，代码确认/待策略实测 | 无 |

## 新内容及设计

| ID / Issue | 最小范围，不等于已经实现 | 优先级 | 硬依赖 |
|---|---|---|---|
| [N01 / #24](https://github.com/KokuuWork/refined-network/issues/24) | 单目标权威原因与动作报价 | P2 | D01、D02、D06、D08 |
| [N02 / #25](https://github.com/KokuuWork/refined-network/issues/25) | 四终端快捷选择，复用真实物品 | P2 | D05、D06 |
| [N03 / #26](https://github.com/KokuuWork/refined-network/issues/26) | 3个命名目标/单当前绑定 | P2 | M04、D06、D08 |
| [N04 / #27](https://github.com/KokuuWork/refined-network/issues/27) | 一规则精确阈值/迟滞/预算 | P3 | D01、D08、D14、D15 |
| [N05 / #28](https://github.com/KokuuWork/refined-network/issues/28) | 样板只读批量预检，不搬运 | P3 | D04、D05、D06 |
| [N06 / #29](https://github.com/KokuuWork/refined-network/issues/29) | 网络桥只读模式/4格白名单 | P3 | D12、D13、D06 |
| [N07 / #30](https://github.com/KokuuWork/refined-network/issues/30) | 配置同型差异预演/确认，无撤销 | P3 | 无 |
| [N08 / #31](https://github.com/KokuuWork/refined-network/issues/31) | 仅待提交订单幂等/故障设计 | P3 | M04、D01、D02、D06、D07 |
| [N09 / #32](https://github.com/KokuuWork/refined-network/issues/32) | 仅保活额度/来源/恢复方案 | P3 | 无 |
| [N10 / #33](https://github.com/KokuuWork/refined-network/issues/33) | 大型RS附属下一批内容评审，选1原型规格 | P3 | 维护者批准首项后另拆实现 |

## 本地正文与接手方式

完整正文在`docs/issues/M01.md`–`M04.md`、`D01.md`–`D20.md`、`N01.md`–`N10.md`，与创建时线上Issue一致；后续以Issue最新依赖和维护者确认更新。不要只按这个索引写代码。

模型执行：读AGENTS/DEVELOPMENT→完整Issue/依赖→源码和目标API→证据/最小范围→定向回归→实现→双平台及适用游戏验证→同步文案→结果与未验证。调研Issue先得到批准或否定证据，不为了交付代码扩大范围。

已实现基础提醒、自动补货、样板复制/搜索、批量配置、跨维桥和能量显示不重复建新功能Issue；新增聚焦精确控制、诊断、预检和多目标。样板自动部署、配置撤销、保留量并发、持久订单实装等须在相关前置完成后再拆，不在现有“只读预检/设计”任务里顺带实现。
