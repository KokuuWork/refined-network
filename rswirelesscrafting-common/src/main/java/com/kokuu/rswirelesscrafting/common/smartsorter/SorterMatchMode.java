package com.kokuu.rswirelesscrafting.common.smartsorter;

import java.util.Optional;

import net.minecraft.util.StringRepresentable;

/**
 * 智能分拣站的过滤方式。
 *
 * <p>RS 原版的输出总线只能按**具体物品**白名单过滤——想把「所有木头」或者
 * 「所有带着 {@code #minecraft:logs} 标签的东西」一次性分走，得往过滤槽里逐个填。
 * 本模组的「标签」模式就是补这个缺口的：填一个代表物品，它所在的所有物品标签都会被匹配。
 */
public enum SorterMatchMode implements StringRepresentable {
    /** 按具体物品匹配（与原版输出总线一致，作为默认行为）。 */
    EXACT("exact"),
    /** 按物品标签匹配：填一个代表物品，凡是和它共享任一物品标签的都算。 */
    TAG("tag");

    private final String name;

    SorterMatchMode(final String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public SorterMatchMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static Optional<SorterMatchMode> byName(final String name) {
        for (final SorterMatchMode mode : values()) {
            if (mode.name.equals(name)) {
                return Optional.of(mode);
            }
        }
        return Optional.empty();
    }
}
