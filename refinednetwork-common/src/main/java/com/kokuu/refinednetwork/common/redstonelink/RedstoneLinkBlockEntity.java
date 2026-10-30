package com.kokuu.refinednetwork.common.redstonelink;

import com.kokuu.refinednetwork.common.BlockEntities;

import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 无线红石端点。
 *
 * <p>两个端点组成一对：**先被记录的那个当输入端**（只读取，不输出），**后配对的那个当输出端**，
 * 每 2 tick 去读输入端所在位置的红石强度，有信号就把自己点亮并对外输出 15 级。
 *
 * <p>整件事都在服务端完成：两端都在同一台服务端上，输出端直接 {@code getBestNeighborSignal}
 * 读输入端即可，**不需要任何网络包**，跨维度也一样。代价是两端区块都得加载——本版不做区块保活，
 * 输入端未加载时按「无信号」处理。
 *
 * <p>只传「有 / 无」：不还原 1–14 级的强度。跨维度还原强度既不符合直觉，也会让方块状态翻 16 倍，
 * 收益不值。
 */
public class RedstoneLinkBlockEntity extends BlockEntity {
    private static final String TAG_TARGET_DIMENSION = "targetDimension";
    private static final String TAG_TARGET_POS = "targetPos";
    private static final String TAG_OWNER = "owner";
    private static final int SAMPLE_INTERVAL = 2;

    @Nullable
    private GlobalPos target;
    @Nullable
    private UUID owner;
    private int sampleCooldown;

    public RedstoneLinkBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getRedstoneLink(), pos, state);
    }

    /**
     * 是否已经配对（即自己是输出端）。
     */
    public boolean isEndpoint() {
        return target != null;
    }

    @Nullable
    public GlobalPos getTarget() {
        return target;
    }

    public boolean isOwnedBy(final Player player) {
        return owner == null || owner.equals(player.getUUID());
    }

    /**
     * 把自己配成输出端，指向 {@code anchor}（输入端）。
     */
    public void bindTo(final GlobalPos anchor, final Player player) {
        this.target = anchor;
        this.owner = player.getUUID();
        setChanged();
    }

    public void unbind() {
        this.target = null;
        this.owner = null;
        setPowered(false);
        setChanged();
    }

    /**
     * 服务端每 tick 调用；内部按 {@link #SAMPLE_INTERVAL} 节流，避免红石抖动时反复刷方块状态。
     */
    public void tick() {
        if (!(level instanceof ServerLevel serverLevel) || target == null) {
            return;
        }
        if (++sampleCooldown < SAMPLE_INTERVAL) {
            return;
        }
        sampleCooldown = 0;
        setPowered(readSource(serverLevel, target));
    }

    private static boolean readSource(final ServerLevel level, final GlobalPos source) {
        final ServerLevel sourceLevel = level.getServer().getLevel(source.dimension());
        if (sourceLevel == null || !sourceLevel.isLoaded(source.pos())) {
            return false;
        }
        return sourceLevel.getBestNeighborSignal(source.pos()) > 0;
    }

    private void setPowered(final boolean powered) {
        if (level == null || level.isClientSide()) {
            return;
        }
        final BlockState state = getBlockState();
        if (!(state.getBlock() instanceof RedstoneLinkBlock)
            || state.getValue(RedstoneLinkBlock.POWERED) == powered) {
            return;
        }
        level.setBlockAndUpdate(worldPosition, state.setValue(RedstoneLinkBlock.POWERED, powered));
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        final GlobalPos current = target;
        if (current != null) {
            tag.putString(TAG_TARGET_DIMENSION, current.dimension().location().toString());
            tag.putLong(TAG_TARGET_POS, current.pos().asLong());
        }
        if (owner != null) {
            tag.putUUID(TAG_OWNER, owner);
        }
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
        final String dimension = tag.getString(TAG_TARGET_DIMENSION);
        if (dimension.isEmpty() || !tag.contains(TAG_TARGET_POS)) {
            target = null;
            return;
        }
        final ResourceLocation location = ResourceLocation.tryParse(dimension);
        if (location == null) {
            target = null;
            return;
        }
        target = GlobalPos.of(
            ResourceKey.create(Registries.DIMENSION, location),
            BlockPos.of(tag.getLong(TAG_TARGET_POS))
        );
    }
}
