package com.kokuu.rswirelesscrafting.common;

public interface Config {
    WirelessCraftingGridEntry getWirelessCraftingGrid();

    DistanceEnergyEntry getDistanceEnergy();

    ChargingStationEntry getChargingStation();

    HudEntry getHud();

    BridgeEntry getBridge();


    AmplifierEntry getAmplifier();

    EnergyReceiverEntry getEnergyReceiver();

    EnergyMonitorEntry getEnergyMonitor();

    SmartSorterEntry getSmartSorter();

    NetworkBridgeEntry getNetworkBridge();

    /**
     * 无线能量接收器：把远处 FE 发电接进网络。
     *
     * <p>它以「网络能量提供者」的身份挂进绑定的网络，于是远处发电机的电直接算进网络电量池。
     * 链路费按距离线性收（与手持终端同一套口径），所以远端发电不是白嫖的。
     */
    interface EnergyReceiverEntry {
        boolean isEnabled();

        long getCapacity();

        long getEnergyUsage();
    }

    /**
     * 能量监控仪：让网络能量第一次可观测。
     *
     * <p>纯读取设备，不向网络提供任何东西；常驻收一点「看仪表」的钱。
     */
    interface EnergyMonitorEntry {
        boolean isEnabled();

        long getEnergyUsage();

        int getLowThresholdPercent();

        int getSampleInterval();
    }

    /**
     * 智能分拣站：按物品 / 标签把网络里的东西搬进面前的容器。
     */
    interface SmartSorterEntry {
        boolean isEnabled();

        long getEnergyPerStack();

        int getStacksPerSecond();
    }

    /**
     * 网络桥接器：让两张各自独立的网络互通（可限速）。
     */
    interface NetworkBridgeEntry {
        boolean isEnabled();

        long getEnergyUsage();

        long getItemTransferRate();

        long getEnergyTransferRate();
    }

    /**
     * 无线增幅塔：覆盖半径内的无线终端距离倍率打折，塔本身接网络常驻耗电。
     */
    interface AmplifierEntry {
        boolean isEnabled();

        long getEnergyUsage();

        int getRadius();

        int getMultiplierPercent();
    }

    /**
     * 手持无线终端时的常驻小字提示（电量 · 距离 · 收费倍率）。
     */
    interface HudEntry {
        boolean isEnabled();
    }

    interface WirelessCraftingGridEntry {
        long getEnergyCapacity();

        long getBatteryUpgradeCapacity();

        long getOpenEnergyUsage();

        long getCraftingEnergyUsage();

        long getAutocraftingEnergyUsage();

        long getClearMatrixEnergyUsage();

        long getRecipeTransferEnergyUsage();

        /**
         * 未接入网络（没绑定，或超出范围 / 缺维度插件）时，是否仍然允许打开终端。
         *
         * <p>打开后就是一台普通的 3×3 合成台：不读网络、不自动合成、不扣电。官方附属 Quartz Arsenal
         * 也这么做，新玩家在绑定前不会觉得「买了不能用」。关掉则保持旧行为（直接拒绝并提示）。
         */
        boolean isOfflineModeEnabled();
    }

    /**
     * 无线设备「按距离扣电」的价目表。
     *
     * <p>同一维度内，设备与绑定网络的水平距离小于等于 {@link #getBaseRange()} 时耗电不变（×1），
     * 超出后按距离<b>线性</b>增长——倍率 = 距离 / 基础范围（百分比取整），距离越远收得越狠；
     * 跨维度时距离失去意义，改为套用 {@link #getInterdimensionalMultiplier()}。
     * 「初级能耗插件」把距离倍率减半、「高级能耗插件」把距离倍率归 1（均不影响跨维度倍率）。
     */
    interface DistanceEnergyEntry {
        int getBaseRange();

        int getInterdimensionalMultiplier();

        int getBasicEnergyDivisor();
    }

    /**
     * 一档充电站的配置。
     *
     * <p>{@code energyUsage} 是<b>基础待机损耗</b>（常驻，充电另算），{@code wiredChargeRate}、
     * {@code wirelessChargeRate} 是充电速率，单位都是 <b>每秒（FE/s）</b>，方块内部按每秒
     * 20 tick 摊到每 tick；{@code wirelessChargeRate} 指的是<b>每个物品</b>的额度。
     *
     * <p>{@link #isIdleEnergyUsageEnabled()} 是待机损耗的<b>配置总闸</b>（关掉后永不扣）；
     * 游戏内还可用红石信号临时关掉无线充电并停扣待机损耗，作为省电开关。
     *
     * <p>{@link #getWirelessChargeEfficiencyPercent()} 是<b>无线充电效率</b>：消耗的能量里只有
     * 这个百分比真正充进设备，其余作为传输损耗蒸发；有线充电恒为 100%（无损耗）。
     */
    interface ChargingStationEntry {
        long getEnergyUsage();

        boolean isIdleEnergyUsageEnabled();

        long getWiredChargeRate();

        long getWirelessChargeRate();

        int getWirelessChargeEfficiencyPercent();

        int getWirelessRadius();

        long getExternalCapacity();
    }

    /**
     * 无线桥：把 RS 网络延伸到远处（可跨维度），据点自带区块保活。
     */
    interface BridgeEntry {
        boolean isEnabled();

        long getEnergyUsage();

        boolean isChunkLoadingEnabled();

        long getChunkLoadingEnergyUsage();
    }
}
