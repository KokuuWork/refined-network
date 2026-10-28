package com.kokuu.rswirelesscrafting.common.autocraftermanager;

import javax.annotation.Nullable;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 「无线自动合成管理器」背后那个**不存在于世界里**的自动合成管理器。
 *
 * <p>管理器本身**不持有任何状态**——它展示的全是「网络上那些合成器各自的样板容器」的实时内容
 * （{@code Autocrafter} 是 RS 的公开 API：名字、样板容器、坐标）。所以虚拟版本比样板网格还干净：
 *
 * <ul>
 *   <li>没有状态要回写物品（不需要数据组件、不需要存档钩子）；</li>
 *   <li>不需要反射：{@link AutocrafterManagerBlockEntity#createMenu} 是 <b>public</b> 的，
 *       它内部自己去网络上收集 {@code Autocrafter} 并按名字分组；</li>
 *   <li>菜单里的槽位直接对着**真实合成器**的样板容器，所以移动样板就是在真实世界里移动，
 *       关掉界面不会丢东西。</li>
 * </ul>
 *
 * <p>三条虚拟方块实体的铁律见 {@code VirtualBlockEntities}：用 {@code AIR} 状态构造（不注册幽灵节点）、
 * 覆写 {@link #setChanged()}（不碰区块）、入网只 {@code setNetwork} 一句。
 */
public class VirtualAutocrafterManager extends AutocrafterManagerBlockEntity {
    public VirtualAutocrafterManager(final Level level, @Nullable final Network network) {
        super(BlockPos.ZERO, virtualState());
        setLevel(level);
        mainNetworkNode.setNetwork(network);
    }

    /**
     * RS 2.0.9 起方块实体会校验「BE 类型 ↔ 方块状态」是否匹配，AIR 会抛
     * {@code Invalid block entity ... got Block{minecraft:air}}。改用真实方块的默认状态；
     * 幽灵节点由下面的 {@link #initialize(ServerLevel, Direction)} 空实现挡住。
     */
    private static BlockState virtualState() {
        return BuiltInRegistries.BLOCK
            .get(ResourceLocation.fromNamespaceAndPath("refinedstorage", "autocrafter_manager"))
            .defaultBlockState();
    }

    @Override
    protected void initialize(final ServerLevel level, final Direction direction) {
        // 虚拟方块实体不参与世界里的网络图，不注册任何节点。
    }

    /**
     * 管理器不持有状态，所以这里什么都不用做。
     *
     * <p>但**必须覆写**：原版实现会去 {@code level.blockEntityChanged(pos)}，
     * 那会去加载 {@code BlockPos.ZERO} 所在的区块。
     */
    @Override
    public void setChanged() {
    }
}
