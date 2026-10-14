package com.kokuu.rswirelesscrafting.common.chargingstation;

import com.kokuu.rswirelesscrafting.common.Config;

/**
 * 充电站的两种规格。两者共用同一套方块实体、菜单与界面，只在数值上分档，
 * 因此差异全部收敛到「这一档读哪一节配置」这一个方法里。
 *
 * <ul>
 *   <li>{@link #AIR_COOLED} —— 400V 风冷：120 FE/t 有线、20 FE/t 无线、半径 4 格；</li>
 *   <li>{@link #LIQUID_COOLED} —— 800V 液冷：240 FE/t 有线、80 FE/t 无线、半径 10 格。</li>
 * </ul>
 *
 * <p>{@code networkId} 用于随界面打开数据在网络上区分客户端该画哪一张背景图。
 */
public enum ChargingStationTier {
    AIR_COOLED(0),
    LIQUID_COOLED(1);

    private final int networkId;

    ChargingStationTier(final int networkId) {
        this.networkId = networkId;
    }

    public int getNetworkId() {
        return networkId;
    }

    public static ChargingStationTier byNetworkId(final int networkId) {
        return networkId == LIQUID_COOLED.networkId ? LIQUID_COOLED : AIR_COOLED;
    }

    public Config.ChargingStationEntry getEntry(final Config config) {
        return this == LIQUID_COOLED
            ? config.getLiquidCooledChargingStation()
            : config.getAirCooledChargingStation();
    }
}
