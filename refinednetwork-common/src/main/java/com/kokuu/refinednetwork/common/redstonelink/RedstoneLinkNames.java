package com.kokuu.refinednetwork.common.redstonelink;

import com.kokuu.refinednetwork.common.ModIdentifierUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

/**
 * 无线红石端点的全部文案。
 *
 * <p>单独放一个类而不是塞进 {@code ContentNames}：后者已经贴到 700 行的 checkstyle 上限了。
 */
public final class RedstoneLinkNames {
    public static final MutableComponent NAME = create("block", "wireless_redstone_link");
    public static final MutableComponent DESC = create("block", "wireless_redstone_link.desc")
        .withStyle(ChatFormatting.GRAY);
    public static final MutableComponent HINT = create("block", "wireless_redstone_link.hint")
        .withStyle(ChatFormatting.DARK_GRAY);

    public static final MutableComponent ANCHOR_SET = create("msg", "redstone_link.anchor_set")
        .withStyle(ChatFormatting.GREEN);
    public static final MutableComponent BOUND = create("msg", "redstone_link.bound")
        .withStyle(ChatFormatting.GREEN);
    public static final MutableComponent UNBOUND = create("msg", "redstone_link.unbound")
        .withStyle(ChatFormatting.YELLOW);
    public static final MutableComponent NOT_A_LINK = create("msg", "redstone_link.not_a_link")
        .withStyle(ChatFormatting.RED);
    public static final MutableComponent NO_PERMISSION = create("msg", "redstone_link.no_permission")
        .withStyle(ChatFormatting.RED);
    public static final MutableComponent SOURCE_UNLOADED = create("msg", "redstone_link.source_unloaded")
        .withStyle(ChatFormatting.GRAY);
    public static final MutableComponent SOURCE_INVALID = create("msg", "redstone_link.source_invalid")
        .withStyle(ChatFormatting.GRAY);
    public static final MutableComponent STATUS_SOURCE = create("msg", "redstone_link.status_source");
    public static final String STATUS_SINK_KEY =
        ModIdentifierUtil.createTranslationKey("msg", "redstone_link.status_sink");

    private RedstoneLinkNames() {
    }

    private static MutableComponent create(final String category, final String value) {
        return ModIdentifierUtil.createTranslation(category, value);
    }
}
