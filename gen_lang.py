"""Append the lang entries for the four new blocks to zh_cn.json / en_us.json.

Reads both files as ordered dicts (json preserves insertion order), appends the
new keys at the end, and writes them back with the same 2-space indent style.
"""
import json
import os
from collections import OrderedDict

HERE = os.path.dirname(os.path.abspath(__file__))
LANG = os.path.join(
    HERE, "rswirelesscrafting-common", "src", "main", "resources",
    "assets", "rswirelesscrafting", "lang"
)

ZH = {
    # ------------------------------------------------------- 无线能量接收器
    "block.rswirelesscrafting.wireless_energy_receiver": "无线能量接收器",
    "block.rswirelesscrafting.wireless_energy_receiver.desc": "把远处的发电接进网络：它以能量提供者的身份挂进去，远处发的电直接算进网络电量",
    "block.rswirelesscrafting.wireless_energy_receiver.hint.bind": "潜行 + 右键接入点（无线访问点 / 网络接收器）记下它，再右键本方块完成绑定",
    "block.rswirelesscrafting.wireless_energy_receiver.capacity": "FE 缓冲：%s（同时向网络贡献这么多电量）",
    "block.rswirelesscrafting.wireless_energy_receiver.energy": "链路费：%s FE/s（按距离倍率缩放）",
    "block.rswirelesscrafting.wireless_energy_receiver.buffer": "缓冲：%s / %s FE",
    "block.rswirelesscrafting.wireless_energy_receiver.hint.redstone": "红石信号 = 停止接收并停扣链路费",
    "msg.rswirelesscrafting.energy_receiver.unbound": "已解绑",
    "msg.rswirelesscrafting.energy_receiver.bound": "已绑定接入点",
    "msg.rswirelesscrafting.energy_receiver.anchor_set": "已记为接入点：去远处右键无线能量接收器完成绑定",
    "msg.rswirelesscrafting.energy_receiver.status": "能量接收器 → %s：%s · 链路费 %s FE/s",
    "msg.rswirelesscrafting.energy_receiver.online": "工作中",
    "msg.rswirelesscrafting.energy_receiver.offline": "未工作（检查接入点是否在网络里、区块是否加载）",
    "msg.rswirelesscrafting.link.anchor_set": "已记下接入点：去目标设备潜行右键完成绑定",
    "msg.rswirelesscrafting.link.anchor_required": "请先在 RS 网络方块或无线桥上潜行右键记录接入点",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver": "无线能量接收器",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.tooltip": "把远处 FE 发电接进网络的配置项。",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.enabled": "启用能量接收器",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.enabled.tooltip": "关掉后所有接收器停止工作（已放置的全部视为未接入）。",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.capacity": "FE 缓冲容量",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.capacity.tooltip": "内部缓冲上限，同时也是它向网络贡献的那部分能量。",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.energyUsage": "链路费（FE/s）",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.energyUsage.tooltip": "链路费的基础值，按距离倍率缩放；跨维度按维度倍率。",

    # ----------------------------------------------------------- 能量监控仪
    "block.rswirelesscrafting.energy_monitor": "能量监控仪",
    "block.rswirelesscrafting.energy_monitor.desc": "让网络能量第一次可观测：存量 / 容量 / 净收支 / 预计耗尽",
    "block.rswirelesscrafting.energy_monitor.hint.place": "贴着 RS 网络方块放，自动盯着那张网",
    "block.rswirelesscrafting.energy_monitor.hint.bind": "或潜行 + 右键接入点记下它，再右键本方块绑定到远处",
    "block.rswirelesscrafting.energy_monitor.threshold": "低电红线：%s%%",
    "block.rswirelesscrafting.energy_monitor.energy": "常驻耗电：%s FE/s",
    "msg.rswirelesscrafting.energy_monitor.unbound": "已解绑",
    "msg.rswirelesscrafting.energy_monitor.anchor_set": "已记为接入点：去远处右键能量监控仪完成绑定",
    "msg.rswirelesscrafting.energy_monitor.bound": "已绑定接入点 %s",
    "msg.rswirelesscrafting.energy_monitor.unlinked": "还没读到网络：贴着网络方块放，或潜行绑定接入点",
    "msg.rswirelesscrafting.energy_monitor.not_draining": "不会耗尽（正在进电 / 收支平衡）",
    "msg.rswirelesscrafting.energy_monitor.status": "网络电量 %s / %s FE · 净收支 %s FE/s · 预计耗尽 %s",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor": "能量监控仪",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.tooltip": "让网络能量可观测的配置项。",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.enabled": "启用能量监控仪",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.enabled.tooltip": "关掉后所有监控仪停止工作（已放置的全部视为未接入）。",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.energyUsage": "常驻耗电（FE/s）",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.energyUsage.tooltip": "监控仪只是个读取设备，收一点「看仪表」的钱。",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.lowThresholdPercent": "低电红线（%）",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.lowThresholdPercent.tooltip": "网络电量低于这个百分比就亮红灯。",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.sampleInterval": "采样间隔（tick）",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.sampleInterval.tooltip": "净收支的采样间隔，20 = 每秒一次。",

    # ----------------------------------------------------------- 智能分拣站
    "block.rswirelesscrafting.smart_sorter": "智能分拣站",
    "block.rswirelesscrafting.smart_sorter.desc": "按物品或标签分流，把网络里的东西搬进面前的容器",
    "block.rswirelesscrafting.smart_sorter.hint.place": "朝着目标容器摆放（和 RS 输出总线一个方向逻辑）",
    "block.rswirelesscrafting.smart_sorter.hint.filter": "界面里 3×3 过滤槽：每格放一个代表物品",
    "block.rswirelesscrafting.smart_sorter.rate": "搬运速率：%s 组/秒",
    "block.rswirelesscrafting.smart_sorter.energy": "搬运耗能：%s FE/组",
    "gui.rswirelesscrafting.smart_sorter.mode.exact": "按物品",
    "gui.rswirelesscrafting.smart_sorter.mode.tag": "按标签",
    "gui.rswirelesscrafting.smart_sorter.mode_tooltip": "点击切换匹配方式：\n按物品 = 只搬完全相同的物品\n按标签 = 搬同标签的全部物品",
    "gui.rswirelesscrafting.smart_sorter.matches": "命中 %s 条规则",
    "text.autoconfig.rswirelesscrafting.option.smartSorter": "智能分拣站",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.tooltip": "按物品 / 标签把网络里的东西搬进面前的容器。",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.enabled": "启用智能分拣站",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.enabled.tooltip": "关掉后所有分拣站停止搬运（已放置的全部视为未激活）。",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.energyPerStack": "每组搬运耗能（FE）",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.energyPerStack.tooltip": "每搬动一组物品从网络扣除的能量。",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.stacksPerSecond": "搬运速率（组/秒）",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.stacksPerSecond.tooltip": "搬运速率上限，防止一次抽干网络。",

    # ----------------------------------------------------------- 网络桥接器
    "block.rswirelesscrafting.network_bridge": "网络桥接器",
    "block.rswirelesscrafting.network_bridge.desc": "让两张各自独立的网络互通：物品和能量都能过，可限速",
    "block.rswirelesscrafting.network_bridge.hint.bind": "潜行 + 右键另一张网络的接入点记下它，再右键本方块完成绑定",
    "block.rswirelesscrafting.network_bridge.item_rate": "物品过桥：%s 件/秒",
    "block.rswirelesscrafting.network_bridge.energy_rate": "能量过桥：%s FE/s",
    "block.rswirelesscrafting.network_bridge.fee": "链路费：%s FE/s（按距离倍率缩放）",
    "msg.rswirelesscrafting.network_bridge.unbound": "已解绑",
    "msg.rswirelesscrafting.network_bridge.anchor_set": "已记为接入点：去远处右键网络桥接器完成绑定",
    "msg.rswirelesscrafting.network_bridge.bound": "已绑定接入点 %s",
    "msg.rswirelesscrafting.network_bridge.status": "桥接器 → %s：%s · 物品 %s 件/秒 · 能量 %s FE/s · 链路费 %s FE/s",
    "msg.rswirelesscrafting.network_bridge.online": "已连通",
    "msg.rswirelesscrafting.network_bridge.offline": "未连通（检查对端网络 / 区块是否加载）",
    "msg.rswirelesscrafting.network_bridge.cycle": "检测到桥接环路，已停用（请重新绑定）",
    "text.autoconfig.rswirelesscrafting.option.networkBridge": "网络桥接器",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.tooltip": "让两张各自独立的网络互通（可限速）。",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.enabled": "启用网络桥接器",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.enabled.tooltip": "关掉后所有桥接器停止转发（已放置的全部视为未连通）。",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.energyUsage": "链路费（FE/s）",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.energyUsage.tooltip": "链路费的基础值，按距离倍率缩放。",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.itemTransferRate": "物品过桥速率（件/秒）",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.itemTransferRate.tooltip": "经过桥搬运物品的速率上限。",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.energyTransferRate": "能量过桥速率（FE/s）",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.energyTransferRate.tooltip": "经过桥搬运能量的速率上限。",
}

EN = {
    # ------------------------------------------------------- 无线能量接收器
    "block.rswirelesscrafting.wireless_energy_receiver": "Wireless Energy Receiver",
    "block.rswirelesscrafting.wireless_energy_receiver.desc":
        "Feeds far-away power generation into the network: it joins as an energy provider, "
        "so remote power counts straight into the network",
    "block.rswirelesscrafting.wireless_energy_receiver.hint.bind":
        "Sneak + right-click the access point (wireless transmitter / network receiver) to record it, "
        "then right-click this block to bind",
    "block.rswirelesscrafting.wireless_energy_receiver.capacity":
        "FE buffer: %s (this is how much it contributes to the network)",
    "block.rswirelesscrafting.wireless_energy_receiver.energy":
        "Link fee: %s FE/s (scaled by the distance multiplier)",
    "block.rswirelesscrafting.wireless_energy_receiver.buffer": "Buffer: %s / %s FE",
    "block.rswirelesscrafting.wireless_energy_receiver.hint.redstone":
        "A redstone signal stops receiving and pauses the link fee",
    "msg.rswirelesscrafting.energy_receiver.unbound": "unbound",
    "msg.rswirelesscrafting.energy_receiver.bound": "bound to the access point",
    "msg.rswirelesscrafting.energy_receiver.anchor_set":
        "Access point recorded: right-click a wireless energy receiver far away to bind it",
    "msg.rswirelesscrafting.energy_receiver.status": "Energy receiver -> %s: %s - link fee %s FE/s",
    "msg.rswirelesscrafting.energy_receiver.online": "working",
    "msg.rswirelesscrafting.energy_receiver.offline":
        "not working (check the access point and chunk loading)",
    "msg.rswirelesscrafting.link.anchor_set":
        "Access point recorded: sneak-right-click the target device to bind",
    "msg.rswirelesscrafting.link.anchor_required":
        "Record an access point on an RS network block or wireless bridge first",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver": "Wireless Energy Receiver",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.tooltip":
        "Feeds far-away FE generation into the network.",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.enabled": "Enable the energy receiver",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.enabled.tooltip":
        "When off, all receivers stop working.",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.capacity": "FE buffer capacity",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.capacity.tooltip":
        "Internal buffer cap, which is also how much it contributes to the network.",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.energyUsage": "Link fee (FE/s)",
    "text.autoconfig.rswirelesscrafting.option.energyReceiver.energyUsage.tooltip":
        "Base link fee, scaled by the distance multiplier.",

    # ----------------------------------------------------------- 能量监控仪
    "block.rswirelesscrafting.energy_monitor": "Energy Monitor",
    "block.rswirelesscrafting.energy_monitor.desc":
        "Makes network energy observable for the first time: stored / capacity / net throughput / time to empty",
    "block.rswirelesscrafting.energy_monitor.hint.place":
        "Place it against an RS network block and it watches that network",
    "block.rswirelesscrafting.energy_monitor.hint.bind":
        "Or sneak + right-click an access point to record it, then right-click this block to watch it remotely",
    "block.rswirelesscrafting.energy_monitor.threshold": "Low warning line: %s%%",
    "block.rswirelesscrafting.energy_monitor.energy": "Drain: %s FE/s",
    "msg.rswirelesscrafting.energy_monitor.unbound": "unbound",
    "msg.rswirelesscrafting.energy_monitor.anchor_set":
        "Access point recorded: right-click an energy monitor far away to bind it",
    "msg.rswirelesscrafting.energy_monitor.bound": "bound to access point %s",
    "msg.rswirelesscrafting.energy_monitor.unlinked":
        "no network read yet: place it against a network block, or sneak-bind an access point",
    "msg.rswirelesscrafting.energy_monitor.not_draining": "never (charging or balanced)",
    "msg.rswirelesscrafting.energy_monitor.status":
        "Network energy %s / %s FE - net %s FE/s - empty in %s",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor": "Energy Monitor",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.tooltip":
        "Makes network energy observable.",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.enabled": "Enable the energy monitor",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.enabled.tooltip":
        "When off, all monitors stop working.",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.energyUsage": "Drain (FE/s)",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.energyUsage.tooltip":
        "The monitor only reads the network; it charges a little for the dashboard.",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.lowThresholdPercent": "Low warning line (%)",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.lowThresholdPercent.tooltip":
        "Light up the warning state below this percentage of stored energy.",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.sampleInterval": "Sample interval (ticks)",
    "text.autoconfig.rswirelesscrafting.option.energyMonitor.sampleInterval.tooltip":
        "Net throughput sampling interval; 20 = once per second.",

    # ----------------------------------------------------------- 智能分拣站
    "block.rswirelesscrafting.smart_sorter": "Smart Sorter",
    "block.rswirelesscrafting.smart_sorter.desc":
        "Routes items by item or tag from the network into the container it faces",
    "block.rswirelesscrafting.smart_sorter.hint.place":
        "Place it facing the target container (same direction logic as an RS exporter)",
    "block.rswirelesscrafting.smart_sorter.hint.filter":
        "The 3x3 filter grid in its screen: one representative item per slot",
    "block.rswirelesscrafting.smart_sorter.rate": "Transfer rate: %s stacks/s",
    "block.rswirelesscrafting.smart_sorter.energy": "Transfer cost: %s FE per stack",
    "gui.rswirelesscrafting.smart_sorter.mode.exact": "By item",
    "gui.rswirelesscrafting.smart_sorter.mode.tag": "By tag",
    "gui.rswirelesscrafting.smart_sorter.mode_tooltip":
        "Click to switch the match mode:\nBy item = only identical items\nBy tag = everything sharing a tag",
    "gui.rswirelesscrafting.smart_sorter.matches": "%s matching rules",
    "text.autoconfig.rswirelesscrafting.option.smartSorter": "Smart Sorter",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.tooltip":
        "Moves items from the network into the container it faces, filtered by item or tag.",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.enabled": "Enable the smart sorter",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.enabled.tooltip":
        "When off, all sorters stop hauling.",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.energyPerStack": "Energy per stack (FE)",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.energyPerStack.tooltip":
        "Network energy spent per stack moved.",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.stacksPerSecond": "Transfer rate (stacks/s)",
    "text.autoconfig.rswirelesscrafting.option.smartSorter.stacksPerSecond.tooltip":
        "Transfer rate cap, so it cannot drain the network in one go.",

    # ----------------------------------------------------------- 网络桥接器
    "block.rswirelesscrafting.network_bridge": "Network Bridge",
    "block.rswirelesscrafting.network_bridge.desc":
        "Links two otherwise separate networks: items and energy both cross, with rate limits",
    "block.rswirelesscrafting.network_bridge.hint.bind":
        "Sneak + right-click an access point on the other network to record it, "
        "then right-click this block to bind",
    "block.rswirelesscrafting.network_bridge.item_rate": "Items across: %s/s",
    "block.rswirelesscrafting.network_bridge.energy_rate": "Energy across: %s FE/s",
    "block.rswirelesscrafting.network_bridge.fee": "Link fee: %s FE/s (scaled by the distance multiplier)",
    "msg.rswirelesscrafting.network_bridge.unbound": "unbound",
    "msg.rswirelesscrafting.network_bridge.anchor_set":
        "Access point recorded: right-click a network bridge far away to bind it",
    "msg.rswirelesscrafting.network_bridge.bound": "bound to access point %s",
    "msg.rswirelesscrafting.network_bridge.status":
        "Bridge -> %s: %s - items %s/s - energy %s FE/s - link fee %s FE/s",
    "msg.rswirelesscrafting.network_bridge.online": "connected",
    "msg.rswirelesscrafting.network_bridge.offline":
        "not connected (check the far network and chunk loading)",
    "msg.rswirelesscrafting.network_bridge.cycle":
        "bridge loop detected, disabled (rebind to retry)",
    "text.autoconfig.rswirelesscrafting.option.networkBridge": "Network Bridge",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.tooltip":
        "Links two otherwise separate networks, with rate limits.",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.enabled": "Enable the network bridge",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.enabled.tooltip":
        "When off, all bridges stop forwarding.",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.energyUsage": "Link fee (FE/s)",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.energyUsage.tooltip":
        "Base link fee, scaled by the distance multiplier.",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.itemTransferRate": "Item rate (items/s)",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.itemTransferRate.tooltip":
        "Cap on items crossing the bridge per second.",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.energyTransferRate": "Energy rate (FE/s)",
    "text.autoconfig.rswirelesscrafting.option.networkBridge.energyTransferRate.tooltip":
        "Cap on FE crossing the bridge per second.",
}


def patch(filename, additions):
    path = os.path.join(LANG, filename)
    with open(path, encoding="utf-8") as f:
        data = json.load(f, object_pairs_hook=OrderedDict)
    added = 0
    for key, value in additions.items():
        if key not in data or data[key] != value:
            data[key] = value
            added += 1
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(filename, "added", added, "keys ->", len(data), "total")


patch("zh_cn.json", ZH)
patch("en_us.json", EN)
