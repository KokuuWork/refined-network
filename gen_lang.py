"""更新 zh_cn.json / en_us.json 中四种方块及共用绑定提示的文案。

按现有键顺序读取；新增键追加至末尾，已有键的值更新为下方文案源。
输出保持 2 空格缩进。
"""
import json
import os
from collections import OrderedDict

HERE = os.path.dirname(os.path.abspath(__file__))
LANG = os.path.join(
    HERE, "refinednetwork-common", "src", "main", "resources",
    "assets", "refinednetwork", "lang"
)

ZH = {
    # ------------------------------------------------------- 无线能量接收器
    "block.refinednetwork.wireless_energy_receiver": "无线能量接收器",
    "block.refinednetwork.wireless_energy_receiver.desc": "从绑定的供电网络取电，存入缓冲并供给旁边的本地 RS 网络。",
    "block.refinednetwork.wireless_energy_receiver.hint.bind": "贴着用电网络放置。用无线连接器右键供电网络方块记录接入点，再右键本方块绑定。",
    "block.refinednetwork.wireless_energy_receiver.capacity": "缓冲上限：%s FE（不是当前电量）。",
    "block.refinednetwork.wireless_energy_receiver.energy": "基础链路费：%s FE/s，按距离或跨维度倍率计算，从供电网络扣取。",
    "block.refinednetwork.wireless_energy_receiver.buffer": "缓冲：%s / %s FE",
    "block.refinednetwork.wireless_energy_receiver.hint.redstone": "红石信号停止接收并暂停链路费，保留已有缓冲电量。",
    "msg.refinednetwork.energy_receiver.unbound": "已解绑。",
    "msg.refinednetwork.energy_receiver.bound": "已绑定供电接入点。",
    "msg.refinednetwork.energy_receiver.anchor_set": "已记录供电接入点。用无线连接器右键无线能量接收器完成绑定。",
    "msg.refinednetwork.energy_receiver.status": "能量接收器 → %s：%s · 链路费 %s FE/s",
    "msg.refinednetwork.energy_receiver.online": "链路已接通",
    "msg.refinednetwork.energy_receiver.offline": "链路未接通。检查两端网络、供电、权限和区块加载。",
    "msg.refinednetwork.link.anchor_set": "已记录接入点。用无线连接器右键目标设备完成绑定。",
    "msg.refinednetwork.link.anchor_required": "尚未记录接入点。用无线连接器右键 RS 网络方块，再右键目标设备绑定。",
    "text.autoconfig.refinednetwork.option.energyReceiver": "无线能量接收器",
    "text.autoconfig.refinednetwork.option.energyReceiver.tooltip": "从绑定的供电网络取电，通过缓冲供给旁边的本地 RS 网络。",
    "text.autoconfig.refinednetwork.option.energyReceiver.enabled": "启用能量接收器",
    "text.autoconfig.refinednetwork.option.energyReceiver.enabled.tooltip": "关闭后接收器停止接收和供电、暂停链路费，保留已有缓冲电量。",
    "text.autoconfig.refinednetwork.option.energyReceiver.capacity": "缓冲容量（FE）",
    "text.autoconfig.refinednetwork.option.energyReceiver.capacity.tooltip": "内部缓冲的电量上限，不是当前电量。只有实际存入缓冲的电量可供本地网络使用。",
    "text.autoconfig.refinednetwork.option.energyReceiver.energyUsage": "基础链路费（FE/s）",
    "text.autoconfig.refinednetwork.option.energyReceiver.energyUsage.tooltip": "从绑定的供电网络扣取的基础链路费。同维度按距离倍率计算，跨维度按跨维度倍率计算。",

    # ----------------------------------------------------------- 能量监控仪
    "block.refinednetwork.energy_monitor": "能量监控仪",
    "block.refinednetwork.energy_monitor.desc": "显示网络电量、容量、净收支与预计耗尽时间。",
    "block.refinednetwork.energy_monitor.hint.place": "贴着 RS 网络方块放置，自动读取该网络。",
    "block.refinednetwork.energy_monitor.hint.bind": "远程监控：用无线连接器右键 RS 网络方块记录接入点，再右键本方块绑定。",
    "block.refinednetwork.energy_monitor.threshold": "低电阈值：%s%%。",
    "block.refinednetwork.energy_monitor.energy": "持续耗电：%s FE/s。",
    "msg.refinednetwork.energy_monitor.unbound": "已解绑。",
    "msg.refinednetwork.energy_monitor.anchor_set": "已记录接入点。用无线连接器右键能量监控仪完成绑定。",
    "msg.refinednetwork.energy_monitor.bound": "已绑定接入点：%s。",
    "msg.refinednetwork.energy_monitor.unlinked": "尚未读到网络。贴着 RS 网络方块放置，或用无线连接器先记录接入点再右键本方块绑定；检查权限和区块加载。",
    "msg.refinednetwork.energy_monitor.not_draining": "暂无法估算（收支非负或电量为零）",
    "msg.refinednetwork.energy_monitor.status": "网络电量 %s / %s FE · 净收支 %s FE/s · 预计耗尽（秒）：%s",
    "text.autoconfig.refinednetwork.option.energyMonitor": "能量监控仪",
    "text.autoconfig.refinednetwork.option.energyMonitor.tooltip": "读取网络电量、容量和净收支，并估算耗尽时间。",
    "text.autoconfig.refinednetwork.option.energyMonitor.enabled": "启用能量监控仪",
    "text.autoconfig.refinednetwork.option.energyMonitor.enabled.tooltip": "关闭后监控仪停止读取网络和扣取运行耗电，显示未连接状态。",
    "text.autoconfig.refinednetwork.option.energyMonitor.energyUsage": "持续耗电（FE/s）",
    "text.autoconfig.refinednetwork.option.energyMonitor.energyUsage.tooltip": "监控仪读取网络时，从所监控网络扣取的持续耗电。",
    "text.autoconfig.refinednetwork.option.energyMonitor.lowThresholdPercent": "低电阈值（%）",
    "text.autoconfig.refinednetwork.option.energyMonitor.lowThresholdPercent.tooltip": "网络电量占容量的百分比低于或等于该阈值时亮红灯；容量为零时也触发。",
    "text.autoconfig.refinednetwork.option.energyMonitor.sampleInterval": "采样间隔（tick）",
    "text.autoconfig.refinednetwork.option.energyMonitor.sampleInterval.tooltip": "净收支采样间隔。按 20 tick/s 计算，20 tick 为每秒采样一次。",

    # ----------------------------------------------------------- 智能分拣站
    "block.refinednetwork.smart_sorter": "智能分拣站",
    "block.refinednetwork.smart_sorter.desc": "按物品或标签筛选，将网络物品送入面前的容器。",
    "block.refinednetwork.smart_sorter.hint.place": "面向目标容器放置，方向逻辑与 RS 输出总线相同。",
    "block.refinednetwork.smart_sorter.hint.filter": "界面内有 3×3 过滤槽，每格放一个代表物品。",
    "block.refinednetwork.smart_sorter.rate": "搬运批次预算：%s 次/秒（单次数量另受容器限制）。",
    "block.refinednetwork.smart_sorter.energy": "每次成功搬运耗能：%s FE（当前按批次结算）。",
    "gui.refinednetwork.smart_sorter.mode.exact": "按物品",
    "gui.refinednetwork.smart_sorter.mode.tag": "按标签",
    "gui.refinednetwork.smart_sorter.mode_tooltip": "点击切换匹配方式。\n按物品：仅搬运与过滤物品相同的物品。\n按标签：搬运与过滤物品共享标签的物品。",
    "gui.refinednetwork.smart_sorter.matches": "匹配 %s 条规则",
    "text.autoconfig.refinednetwork.option.smartSorter": "智能分拣站",
    "text.autoconfig.refinednetwork.option.smartSorter.tooltip": "按物品或标签筛选，将网络物品送入面前的容器。",
    "text.autoconfig.refinednetwork.option.smartSorter.enabled": "启用智能分拣站",
    "text.autoconfig.refinednetwork.option.smartSorter.enabled.tooltip": "关闭后分拣站停止搬运，显示未激活状态。",
    "text.autoconfig.refinednetwork.option.smartSorter.energyPerStack": "每成功搬运批次耗能（FE）",
    "text.autoconfig.refinednetwork.option.smartSorter.energyPerStack.tooltip": "当前实现每个成功搬运批次扣取一次费用，并非按实际堆叠组数；单次数量上限待核实。",
    "text.autoconfig.refinednetwork.option.smartSorter.stacksPerSecond": "搬运批次预算（次/秒）",
    "text.autoconfig.refinednetwork.option.smartSorter.stacksPerSecond.tooltip": "当前用于分配成功搬运批次预算，不是物品堆叠数上限；单次可跨多个槽位，数量限制另行核实。",

    # ----------------------------------------------------------- 网络桥接器
    "block.refinednetwork.network_bridge": "网络桥接器",
    "block.refinednetwork.network_bridge.desc": "让本地网络访问另一张独立网络的物品与电量，传输受速率上限限制。",
    "block.refinednetwork.network_bridge.hint.bind": "用无线连接器右键另一张 RS 网络的方块记录接入点，再右键本方块绑定。",
    "block.refinednetwork.network_bridge.item_rate": "物品传输上限：%s 件/秒。",
    "block.refinednetwork.network_bridge.energy_rate": "能量传输上限：%s FE/s。",
    "block.refinednetwork.network_bridge.fee": "基础链路费：%s FE/s，按距离或跨维度倍率计算，从本地网络扣取。",
    "msg.refinednetwork.network_bridge.unbound": "已解绑。",
    "msg.refinednetwork.network_bridge.anchor_set": "已记录接入点。用无线连接器右键网络桥接器完成绑定。",
    "msg.refinednetwork.network_bridge.bound": "已绑定接入点：%s。",
    "msg.refinednetwork.network_bridge.status": "网络桥接器 → %s：%s · 物品上限 %s 件/秒 · 能量上限 %s FE/s · 链路费 %s FE/s",
    "msg.refinednetwork.network_bridge.online": "链路已接通",
    "msg.refinednetwork.network_bridge.offline": "链路未接通。检查两端网络、供电、权限、红石和区块加载。",
    "msg.refinednetwork.network_bridge.cycle": "已触发桥接环路保护。检查绑定关系，重新绑定后重试。",
    "text.autoconfig.refinednetwork.option.networkBridge": "网络桥接器",
    "text.autoconfig.refinednetwork.option.networkBridge.tooltip": "让本地网络访问另一张独立网络的物品与电量，可限制传输速率。",
    "text.autoconfig.refinednetwork.option.networkBridge.enabled": "启用网络桥接器",
    "text.autoconfig.refinednetwork.option.networkBridge.enabled.tooltip": "关闭后桥接器停止转发和扣取链路费，显示未连通状态。",
    "text.autoconfig.refinednetwork.option.networkBridge.energyUsage": "基础链路费（FE/s）",
    "text.autoconfig.refinednetwork.option.networkBridge.energyUsage.tooltip": "从本地网络扣取的基础链路费。同维度按距离倍率计算，跨维度按跨维度倍率计算。",
    "text.autoconfig.refinednetwork.option.networkBridge.itemTransferRate": "物品传输上限（件/秒）",
    "text.autoconfig.refinednetwork.option.networkBridge.itemTransferRate.tooltip": "每秒经桥接器传输的物品数量上限。",
    "text.autoconfig.refinednetwork.option.networkBridge.energyTransferRate": "能量传输上限（FE/s）",
    "text.autoconfig.refinednetwork.option.networkBridge.energyTransferRate.tooltip": "每秒经桥接器传输的电量上限。",
}

EN = {
    # ------------------------------------------------------- 无线能量接收器
    "block.refinednetwork.wireless_energy_receiver": "Wireless Energy Receiver",
    "block.refinednetwork.wireless_energy_receiver.desc":
        "Draws energy from the bound supply network into its buffer, "
        "then supplies the adjacent local RS network.",
    "block.refinednetwork.wireless_energy_receiver.hint.bind":
        "Place against the network needing power. Use the Wireless Connector to right-click a supply network block, "
        "then right-click this block to bind.",
    "block.refinednetwork.wireless_energy_receiver.capacity":
        "Buffer capacity: %s FE (not the current stored energy).",
    "block.refinednetwork.wireless_energy_receiver.energy":
        "Base link fee: %s FE/s, scaled by distance or the interdimensional multiplier and drawn from the supply network.",
    "block.refinednetwork.wireless_energy_receiver.buffer": "Buffer: %s / %s FE",
    "block.refinednetwork.wireless_energy_receiver.hint.redstone":
        "Redstone stops receiving and pauses the link fee; stored buffer energy is retained.",
    "msg.refinednetwork.energy_receiver.unbound": "Unbound.",
    "msg.refinednetwork.energy_receiver.bound": "Bound to the supply access point.",
    "msg.refinednetwork.energy_receiver.anchor_set":
        "Supply access point recorded. Right-click the Wireless Energy Receiver with the Wireless Connector to bind.",
    "msg.refinednetwork.energy_receiver.status": "Energy receiver -> %s: %s - Link fee %s FE/s",
    "msg.refinednetwork.energy_receiver.online": "Link connected",
    "msg.refinednetwork.energy_receiver.offline":
        "Link disconnected. Check both networks, power, permissions and chunk loading.",
    "msg.refinednetwork.link.anchor_set":
        "Access point recorded. Right-click the target device with the Wireless Connector to bind.",
    "msg.refinednetwork.link.anchor_required":
        "No access point recorded. Use the Wireless Connector to right-click an RS network block, then the target device to bind.",
    "text.autoconfig.refinednetwork.option.energyReceiver": "Wireless Energy Receiver",
    "text.autoconfig.refinednetwork.option.energyReceiver.tooltip":
        "Draws energy from the bound supply network through a buffer to supply the adjacent local RS network.",
    "text.autoconfig.refinednetwork.option.energyReceiver.enabled": "Enable the energy receiver",
    "text.autoconfig.refinednetwork.option.energyReceiver.enabled.tooltip":
        "When off, receivers stop receiving and supplying energy and pause the link fee; stored buffer energy is retained.",
    "text.autoconfig.refinednetwork.option.energyReceiver.capacity": "Buffer capacity (FE)",
    "text.autoconfig.refinednetwork.option.energyReceiver.capacity.tooltip":
        "Internal buffer capacity, not the current stored energy. Only energy actually stored in the buffer can supply the local network.",
    "text.autoconfig.refinednetwork.option.energyReceiver.energyUsage": "Base link fee (FE/s)",
    "text.autoconfig.refinednetwork.option.energyReceiver.energyUsage.tooltip":
        "Base link fee drawn from the bound supply network. Scaled by distance in the same dimension, or by the interdimensional multiplier across dimensions.",

    # ----------------------------------------------------------- 能量监控仪
    "block.refinednetwork.energy_monitor": "Energy Monitor",
    "block.refinednetwork.energy_monitor.desc":
        "Shows network energy, capacity, net rate and estimated time to empty.",
    "block.refinednetwork.energy_monitor.hint.place":
        "Place against an RS network block to read that network automatically.",
    "block.refinednetwork.energy_monitor.hint.bind":
        "Remote monitoring: use the Wireless Connector to right-click an RS network block, then right-click this block to bind.",
    "block.refinednetwork.energy_monitor.threshold": "Low-energy threshold: %s%%.",
    "block.refinednetwork.energy_monitor.energy": "Continuous drain: %s FE/s.",
    "msg.refinednetwork.energy_monitor.unbound": "Unbound.",
    "msg.refinednetwork.energy_monitor.anchor_set":
        "Access point recorded. Right-click the Energy Monitor with the Wireless Connector to bind.",
    "msg.refinednetwork.energy_monitor.bound": "Bound to access point: %s.",
    "msg.refinednetwork.energy_monitor.unlinked":
        "No network reading. Place against an RS network block, or use the Wireless Connector to record an access point and right-click this block to bind; check permissions and chunk loading.",
    "msg.refinednetwork.energy_monitor.not_draining": "Not estimated (nonnegative net rate or zero stored energy)",
    "msg.refinednetwork.energy_monitor.status":
        "Network energy %s / %s FE - Net rate %s FE/s - Estimated time to empty (seconds): %s",
    "text.autoconfig.refinednetwork.option.energyMonitor": "Energy Monitor",
    "text.autoconfig.refinednetwork.option.energyMonitor.tooltip":
        "Reads network energy, capacity and net rate, and estimates time to empty.",
    "text.autoconfig.refinednetwork.option.energyMonitor.enabled": "Enable the energy monitor",
    "text.autoconfig.refinednetwork.option.energyMonitor.enabled.tooltip":
        "When off, monitors stop reading networks and drawing operating energy, and show a disconnected state.",
    "text.autoconfig.refinednetwork.option.energyMonitor.energyUsage": "Continuous drain (FE/s)",
    "text.autoconfig.refinednetwork.option.energyMonitor.energyUsage.tooltip":
        "Continuous energy drawn from the monitored network while taking readings.",
    "text.autoconfig.refinednetwork.option.energyMonitor.lowThresholdPercent": "Low-energy threshold (%)",
    "text.autoconfig.refinednetwork.option.energyMonitor.lowThresholdPercent.tooltip":
        "Lights the warning when stored energy as a percentage of capacity is at or below this threshold, or capacity is zero.",
    "text.autoconfig.refinednetwork.option.energyMonitor.sampleInterval": "Sample interval (ticks)",
    "text.autoconfig.refinednetwork.option.energyMonitor.sampleInterval.tooltip":
        "Net rate sampling interval. At 20 ticks/s, 20 ticks means one sample per second.",

    # ----------------------------------------------------------- 智能分拣站
    "block.refinednetwork.smart_sorter": "Smart Sorter",
    "block.refinednetwork.smart_sorter.desc":
        "Filters by item or tag and sends network items into the container it faces.",
    "block.refinednetwork.smart_sorter.hint.place":
        "Place facing the target container, with the same direction logic as an RS Exporter.",
    "block.refinednetwork.smart_sorter.hint.filter":
        "The screen has a 3x3 filter grid; place one representative item per slot.",
    "block.refinednetwork.smart_sorter.rate": "Transfer budget: %s batches/s (amount also depends on the container).",
    "block.refinednetwork.smart_sorter.energy": "Cost per successful transfer: %s FE (currently billed per batch).",
    "gui.refinednetwork.smart_sorter.mode.exact": "By item",
    "gui.refinednetwork.smart_sorter.mode.tag": "By tag",
    "gui.refinednetwork.smart_sorter.mode_tooltip":
        "Click to switch the match mode.\nBy item: only items identical to the filter item.\nBy tag: items sharing a tag with the filter item.",
    "gui.refinednetwork.smart_sorter.matches": "Matching rules: %s",
    "text.autoconfig.refinednetwork.option.smartSorter": "Smart Sorter",
    "text.autoconfig.refinednetwork.option.smartSorter.tooltip":
        "Filters by item or tag and sends network items into the container it faces.",
    "text.autoconfig.refinednetwork.option.smartSorter.enabled": "Enable the smart sorter",
    "text.autoconfig.refinednetwork.option.smartSorter.enabled.tooltip":
        "When off, sorters stop transferring items and show an inactive state.",
    "text.autoconfig.refinednetwork.option.smartSorter.energyPerStack": "Energy per successful batch (FE)",
    "text.autoconfig.refinednetwork.option.smartSorter.energyPerStack.tooltip":
        "Currently billed once per successful batch, not per item stack. The per-transfer amount limit needs verification.",
    "text.autoconfig.refinednetwork.option.smartSorter.stacksPerSecond": "Transfer budget (batches/s)",
    "text.autoconfig.refinednetwork.option.smartSorter.stacksPerSecond.tooltip":
        "Currently allocates a transfer-batch budget, not a limit on item stacks. A batch may span slots; amount limits need verification.",

    # ----------------------------------------------------------- 网络桥接器
    "block.refinednetwork.network_bridge": "Network Bridge",
    "block.refinednetwork.network_bridge.desc":
        "Lets the local network access items and energy from another independent network, subject to transfer limits.",
    "block.refinednetwork.network_bridge.hint.bind":
        "Use the Wireless Connector to right-click a block on the other RS network to record its access point, "
        "then right-click this block to bind.",
    "block.refinednetwork.network_bridge.item_rate": "Item transfer limit: %s items/s.",
    "block.refinednetwork.network_bridge.energy_rate": "Energy transfer limit: %s FE/s.",
    "block.refinednetwork.network_bridge.fee": "Base link fee: %s FE/s, scaled by distance or the interdimensional multiplier and drawn from the local network.",
    "msg.refinednetwork.network_bridge.unbound": "Unbound.",
    "msg.refinednetwork.network_bridge.anchor_set":
        "Access point recorded. Right-click the Network Bridge with the Wireless Connector to bind.",
    "msg.refinednetwork.network_bridge.bound": "Bound to access point: %s.",
    "msg.refinednetwork.network_bridge.status":
        "Network bridge -> %s: %s - Item limit %s items/s - Energy limit %s FE/s - Link fee %s FE/s",
    "msg.refinednetwork.network_bridge.online": "Link connected",
    "msg.refinednetwork.network_bridge.offline":
        "Link disconnected. Check both networks, power, permissions, redstone and chunk loading.",
    "msg.refinednetwork.network_bridge.cycle":
        "Bridge loop protection triggered. Check bindings, then rebind to retry.",
    "text.autoconfig.refinednetwork.option.networkBridge": "Network Bridge",
    "text.autoconfig.refinednetwork.option.networkBridge.tooltip":
        "Lets the local network access items and energy from another independent network, with configurable transfer limits.",
    "text.autoconfig.refinednetwork.option.networkBridge.enabled": "Enable the network bridge",
    "text.autoconfig.refinednetwork.option.networkBridge.enabled.tooltip":
        "When off, Network Bridges stop forwarding and drawing link fees, and show a disconnected state.",
    "text.autoconfig.refinednetwork.option.networkBridge.energyUsage": "Base link fee (FE/s)",
    "text.autoconfig.refinednetwork.option.networkBridge.energyUsage.tooltip":
        "Base link fee drawn from the local network. Scaled by distance in the same dimension, or by the interdimensional multiplier across dimensions.",
    "text.autoconfig.refinednetwork.option.networkBridge.itemTransferRate": "Item transfer limit (items/s)",
    "text.autoconfig.refinednetwork.option.networkBridge.itemTransferRate.tooltip":
        "Maximum number of items transferred through the bridge per second.",
    "text.autoconfig.refinednetwork.option.networkBridge.energyTransferRate": "Energy transfer limit (FE/s)",
    "text.autoconfig.refinednetwork.option.networkBridge.energyTransferRate.tooltip":
        "Maximum energy transferred through the bridge per second.",
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
