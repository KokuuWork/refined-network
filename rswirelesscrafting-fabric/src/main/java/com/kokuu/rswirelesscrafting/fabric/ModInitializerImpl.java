package com.kokuu.rswirelesscrafting.fabric;

import com.kokuu.rswirelesscrafting.common.AbstractModInitializer;
import com.kokuu.rswirelesscrafting.common.BlockEntities;
import com.kokuu.rswirelesscrafting.common.Blocks;
import com.kokuu.rswirelesscrafting.common.ContentIds;
import com.kokuu.rswirelesscrafting.common.ContentNames;
import com.kokuu.rswirelesscrafting.common.CreativeModeTabItems;
import com.kokuu.rswirelesscrafting.common.Items;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.amplifier.AmplifierBlock;
import com.kokuu.rswirelesscrafting.common.amplifier.AmplifierBlockEntity;
import com.kokuu.rswirelesscrafting.common.amplifier.AmplifierBlockItem;
import com.kokuu.rswirelesscrafting.common.autocraftermanager.WirelessAutocrafterManagerItem;
import com.kokuu.rswirelesscrafting.common.bridge.WirelessBridgeBlock;
import com.kokuu.rswirelesscrafting.common.bridge.WirelessBridgeBlockEntity;
import com.kokuu.rswirelesscrafting.common.bridge.WirelessBridgeBlockItem;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlock;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlockEntity;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlockItem;
import com.kokuu.rswirelesscrafting.common.configcard.WirelessConfigurationCardItem;
import com.kokuu.rswirelesscrafting.common.connector.WirelessConnectorItem;
import com.kokuu.rswirelesscrafting.common.energymonitor.EnergyMonitorBlock;
import com.kokuu.rswirelesscrafting.common.energymonitor.EnergyMonitorBlockEntity;
import com.kokuu.rswirelesscrafting.common.energymonitor.EnergyMonitorBlockItem;
import com.kokuu.rswirelesscrafting.common.energyreceiver.WirelessEnergyReceiverBlock;
import com.kokuu.rswirelesscrafting.common.energyreceiver.WirelessEnergyReceiverBlockEntity;
import com.kokuu.rswirelesscrafting.common.energyreceiver.WirelessEnergyReceiverBlockItem;
import com.kokuu.rswirelesscrafting.common.link.BoundTargetResolver;
import com.kokuu.rswirelesscrafting.common.link.LinkRegistry;
import com.kokuu.rswirelesscrafting.common.link.PairingClipboard;
import com.kokuu.rswirelesscrafting.common.monitor.WirelessMonitorItem;
import com.kokuu.rswirelesscrafting.common.networkbridge.NetworkBridgeBlock;
import com.kokuu.rswirelesscrafting.common.networkbridge.NetworkBridgeBlockEntity;
import com.kokuu.rswirelesscrafting.common.networkbridge.NetworkBridgeBlockItem;
import com.kokuu.rswirelesscrafting.common.patterngrid.WirelessPatternGridItem;
import com.kokuu.rswirelesscrafting.common.smartsorter.SmartSorterBlock;
import com.kokuu.rswirelesscrafting.common.smartsorter.SmartSorterBlockEntity;
import com.kokuu.rswirelesscrafting.common.smartsorter.SmartSorterBlockItem;
import com.kokuu.rswirelesscrafting.common.terminal.AbstractWirelessTerminalItem;
import com.kokuu.rswirelesscrafting.common.upgrade.AdvancedEnergyUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.BatteryUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.EnergyUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.InterdimensionalUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.OverclockUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.OverclockUpgrades;
import com.kokuu.rswirelesscrafting.common.upgrade.RangeUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.RemoteEnergyUpgradeItem;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridItem;

import java.util.Set;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;
import com.refinedmods.refinedstorage.fabric.api.RefinedStorageFabricApi;
import com.refinedmods.refinedstorage.fabric.api.RefinedStoragePlugin;
import com.refinedmods.refinedstorage.fabric.support.energy.EnergyStorageAdapter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import team.reborn.energy.api.EnergyStorage;

public class ModInitializerImpl extends AbstractModInitializer implements RefinedStoragePlugin, ModInitializer {
    @Override
    public void onApiAvailable(final RefinedStorageApi refinedStorageApi) {
        Platform.setConfigProvider(ConfigImpl::get);
        Platform.setItemEnergyProvider(new PlatformImpl());
        registerContent();
        registerCapabilities();
        registerCreativeModeTabListener(refinedStorageApi);
    }

    private void registerContent() {
        registerBlocks();
        registerItems();
        registerMenus(new DirectRegistryCallback<>(BuiltInRegistries.MENU), new ExtendedMenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu, D> MenuType<T> create(final MenuSupplier<T, D> supplier,
                                                                           final StreamCodec<RegistryFriendlyByteBuf, D>
                                                                               streamCodec) {
                return new ExtendedScreenHandlerType<>(supplier::create, streamCodec);
            }
        });
        registerDataComponents(new DirectRegistryCallback<>(BuiltInRegistries.DATA_COMPONENT_TYPE));
        // 把超频卡登记为「原版自动合成器可用的升级」（公开 API）。Fabric 的注册是即时的。
        OverclockUpgrades.register();
    }

    private void registerBlocks() {
        final DirectRegistryCallback<Block> blockCallback = new DirectRegistryCallback<>(BuiltInRegistries.BLOCK);
        Blocks.INSTANCE.setTerminalChargingStation(blockCallback.register(
            ContentIds.TERMINAL_CHARGING_STATION,
            () -> new ChargingStationBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.5F, 7.0F)
                .sound(SoundType.METAL))
        ));

        Blocks.INSTANCE.setWirelessBridge(blockCallback.register(
            ContentIds.WIRELESS_BRIDGE,
            () -> new WirelessBridgeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 9.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setWirelessAmplifier(blockCallback.register(
            ContentIds.WIRELESS_AMPLIFIER,
            () -> new AmplifierBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 9.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setWirelessEnergyReceiver(blockCallback.register(
            ContentIds.WIRELESS_ENERGY_RECEIVER,
            () -> new WirelessEnergyReceiverBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 9.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setEnergyMonitor(blockCallback.register(
            ContentIds.ENERGY_MONITOR,
            () -> new EnergyMonitorBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.5F, 7.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setSmartSorter(blockCallback.register(
            ContentIds.SMART_SORTER,
            () -> new SmartSorterBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 9.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setNetworkBridge(blockCallback.register(
            ContentIds.NETWORK_BRIDGE,
            () -> new NetworkBridgeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 9.0F)
                .sound(SoundType.METAL))
        ));

        final DirectRegistryCallback<BlockEntityType<?>> blockEntityCallback =
            new DirectRegistryCallback<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);
        BlockEntities.INSTANCE.setWirelessBridge(blockEntityCallback.register(
            ContentIds.WIRELESS_BRIDGE,
            () -> new BlockEntityType<>(
                WirelessBridgeBlockEntity::new,
                Set.of(Blocks.INSTANCE.getWirelessBridge()),
                null
            )
        ));
        BlockEntities.INSTANCE.setWirelessAmplifier(blockEntityCallback.register(
            ContentIds.WIRELESS_AMPLIFIER,
            () -> new BlockEntityType<>(
                AmplifierBlockEntity::new,
                Set.of(Blocks.INSTANCE.getWirelessAmplifier()),
                null
            )
        ));
        BlockEntities.INSTANCE.setChargingStation(blockEntityCallback.register(
            ContentIds.CHARGING_STATION,
            () -> new BlockEntityType<>(
                ChargingStationBlockEntity::new,
                Set.of(Blocks.INSTANCE.getTerminalChargingStation()),
                null
            )
        ));
        BlockEntities.INSTANCE.setWirelessEnergyReceiver(blockEntityCallback.register(
            ContentIds.WIRELESS_ENERGY_RECEIVER,
            () -> new BlockEntityType<>(
                WirelessEnergyReceiverBlockEntity::new,
                Set.of(Blocks.INSTANCE.getWirelessEnergyReceiver()),
                null
            )
        ));
        BlockEntities.INSTANCE.setEnergyMonitor(blockEntityCallback.register(
            ContentIds.ENERGY_MONITOR,
            () -> new BlockEntityType<>(
                EnergyMonitorBlockEntity::new,
                Set.of(Blocks.INSTANCE.getEnergyMonitor()),
                null
            )
        ));
        BlockEntities.INSTANCE.setSmartSorter(blockEntityCallback.register(
            ContentIds.SMART_SORTER,
            () -> new BlockEntityType<>(
                SmartSorterBlockEntity::new,
                Set.of(Blocks.INSTANCE.getSmartSorter()),
                null
            )
        ));
        BlockEntities.INSTANCE.setNetworkBridge(blockEntityCallback.register(
            ContentIds.NETWORK_BRIDGE,
            () -> new BlockEntityType<>(
                NetworkBridgeBlockEntity::new,
                Set.of(Blocks.INSTANCE.getNetworkBridge()),
                null
            )
        ));
    }

    private void registerItems() {
        registerCustomItems(new DirectRegistryCallback<>(BuiltInRegistries.ITEM));
    }

    private void registerCustomItems(final RegistryCallback<Item> callback) {
        Items.INSTANCE.setWirelessCraftingGrid(
            callback.register(ContentIds.WIRELESS_CRAFTING_GRID, () -> new WirelessCraftingGridItem(
                RefinedStorageApi.INSTANCE.getEnergyItemHelper(),
                RefinedStorageApi.INSTANCE.getNetworkItemHelper()
            ) {
                @Override
                public boolean allowComponentsUpdateAnimation(final Player player,
                                                               final InteractionHand hand,
                                                               final ItemStack oldStack,
                                                               final ItemStack newStack) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            })
        );
        Items.INSTANCE.setWirelessAutocrafterManager(
            callback.register(ContentIds.WIRELESS_AUTOCRAFTER_MANAGER, () -> new WirelessAutocrafterManagerItem(
                RefinedStorageApi.INSTANCE.getEnergyItemHelper(),
                RefinedStorageApi.INSTANCE.getNetworkItemHelper()
            ) {
                @Override
                public boolean allowComponentsUpdateAnimation(final Player player,
                                                               final InteractionHand hand,
                                                               final ItemStack oldStack,
                                                               final ItemStack newStack) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            })
        );
        Items.INSTANCE.setWirelessPatternGrid(
            callback.register(ContentIds.WIRELESS_PATTERN_GRID, () -> new WirelessPatternGridItem(
                RefinedStorageApi.INSTANCE.getEnergyItemHelper(),
                RefinedStorageApi.INSTANCE.getNetworkItemHelper()
            ) {
                @Override
                public boolean allowComponentsUpdateAnimation(final Player player,
                                                               final InteractionHand hand,
                                                               final ItemStack oldStack,
                                                               final ItemStack newStack) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            })
        );
        Items.INSTANCE.setOverclockUpgrade(
            callback.register(ContentIds.OVERCLOCK_UPGRADE, OverclockUpgradeItem::new)
        );
        Items.INSTANCE.setBatteryUpgrade(
            callback.register(ContentIds.BATTERY_UPGRADE, BatteryUpgradeItem::new)
        );
        Items.INSTANCE.setRangeUpgrade(
            callback.register(ContentIds.RANGE_UPGRADE, RangeUpgradeItem::new)
        );
        Items.INSTANCE.setInterdimensionalUpgrade(
            callback.register(ContentIds.INTERDIMENSIONAL_UPGRADE, InterdimensionalUpgradeItem::new)
        );
        Items.INSTANCE.setRemoteEnergyUpgrade(
            callback.register(ContentIds.REMOTE_ENERGY_UPGRADE, RemoteEnergyUpgradeItem::new)
        );
        Items.INSTANCE.setEnergyUpgrade(
            callback.register(ContentIds.ENERGY_UPGRADE, EnergyUpgradeItem::new)
        );
        Items.INSTANCE.setAdvancedEnergyUpgrade(
            callback.register(ContentIds.ADVANCED_ENERGY_UPGRADE, AdvancedEnergyUpgradeItem::new)
        );
        Items.INSTANCE.setWirelessMonitor(
            callback.register(ContentIds.WIRELESS_MONITOR, () -> new WirelessMonitorItem(
                new Item.Properties().stacksTo(1),
                RefinedStorageApi.INSTANCE.getEnergyItemHelper(),
                RefinedStorageApi.INSTANCE.getNetworkItemHelper()
            ) {
                @Override
                public boolean allowComponentsUpdateAnimation(final Player player,
                                                               final InteractionHand hand,
                                                               final ItemStack oldStack,
                                                               final ItemStack newStack) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            })
        );
        Items.INSTANCE.setWirelessConfigurationCard(
            callback.register(ContentIds.WIRELESS_CONFIGURATION_CARD, () -> new WirelessConfigurationCardItem(
                new Item.Properties().stacksTo(1)
            ))
        );
        Items.INSTANCE.setWirelessConnector(
            callback.register(ContentIds.WIRELESS_CONNECTOR, () -> new WirelessConnectorItem(
                new Item.Properties().stacksTo(1)
            ))
        );
        Items.INSTANCE.setWirelessBridgeBlockItem(
            callback.register(ContentIds.WIRELESS_BRIDGE, () -> new WirelessBridgeBlockItem(
                Blocks.INSTANCE.getWirelessBridge(),
                new Item.Properties()
            ))
        );
        Items.INSTANCE.setWirelessAmplifierBlockItem(
            callback.register(ContentIds.WIRELESS_AMPLIFIER, () -> new AmplifierBlockItem(
                Blocks.INSTANCE.getWirelessAmplifier(),
                new Item.Properties()
            ))
        );
        Items.INSTANCE.setTerminalChargingStationBlockItem(
            callback.register(
                ContentIds.TERMINAL_CHARGING_STATION,
                () -> new ChargingStationBlockItem(
                    Blocks.INSTANCE.getTerminalChargingStation(),
                    new Item.Properties()
                )
            )
        );
        Items.INSTANCE.setWirelessEnergyReceiverBlockItem(
            callback.register(
                ContentIds.WIRELESS_ENERGY_RECEIVER,
                () -> new WirelessEnergyReceiverBlockItem(
                    Blocks.INSTANCE.getWirelessEnergyReceiver(),
                    new Item.Properties()
                )
            )
        );
        Items.INSTANCE.setEnergyMonitorBlockItem(
            callback.register(
                ContentIds.ENERGY_MONITOR,
                () -> new EnergyMonitorBlockItem(
                    Blocks.INSTANCE.getEnergyMonitor(),
                    new Item.Properties()
                )
            )
        );
        Items.INSTANCE.setSmartSorterBlockItem(
            callback.register(
                ContentIds.SMART_SORTER,
                () -> new SmartSorterBlockItem(
                    Blocks.INSTANCE.getSmartSorter(),
                    new Item.Properties()
                )
            )
        );
        Items.INSTANCE.setNetworkBridgeBlockItem(
            callback.register(
                ContentIds.NETWORK_BRIDGE,
                () -> new NetworkBridgeBlockItem(
                    Blocks.INSTANCE.getNetworkBridge(),
                    new Item.Properties()
                )
            )
        );
    }

    private void registerCapabilities() {
        // 四件无线终端共用同一套能量能力（容量来自 AbstractWirelessTerminalItem）。
        EnergyStorage.ITEM.registerForItems(
            (stack, context) -> new EnergyStorageAdapter(
                ((AbstractWirelessTerminalItem) stack.getItem()).createEnergyStorage(stack),
                context
            ),
            Items.INSTANCE.getWirelessCraftingGrid(),
            Items.INSTANCE.getWirelessPatternGrid(),
            Items.INSTANCE.getWirelessAutocrafterManager(),
            Items.INSTANCE.getWirelessMonitor()
        );
        EnergyStorage.SIDED.registerForBlockEntity(
            (blockEntity, context) -> new ChargingStationEnergyStorage(blockEntity),
            BlockEntities.INSTANCE.getChargingStation()
        );
        EnergyStorage.SIDED.registerForBlockEntity(
            (blockEntity, context) -> new WirelessEnergyReceiverEnergyStorage(blockEntity),
            BlockEntities.INSTANCE.getWirelessEnergyReceiver()
        );
        // 线缆和网络只认「方块类型上注册的容器」，只把容器塞在 BE 里是不够的。
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getChargingStation());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getWirelessAmplifier());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getWirelessBridge());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getNetworkBridge());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getSmartSorter());
    }

    private void registerNetworkNodeContainerProvider(
        final BlockEntityType<? extends AbstractNetworkNodeContainerBlockEntity<?>> type
    ) {
        RefinedStorageFabricApi.INSTANCE.getNetworkNodeContainerProviderLookup().registerForBlockEntity(
            (blockEntity, direction) -> blockEntity.getContainerProvider(),
            type
        );
    }

    private void registerCreativeModeTabListener(final RefinedStorageApi refinedStorageApi) {
        final ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            refinedStorageApi.getCreativeModeTabId()
        );
        ItemGroupEvents.modifyEntriesEvent(creativeModeTab).register(
            entries -> CreativeModeTabItems.addItems(entries::accept)
        );
    }

    private InteractionResult onUseBlock(final Player player,
                                         final Level level,
                                         final InteractionHand hand,
                                         final BlockHitResult hitResult) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND
            || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        // 连接器：任何姿态都接管——敲哪台连哪台，绝不放方块。
        if (serverPlayer.getMainHandItem().getItem() instanceof WirelessConnectorItem connector) {
            return connector.apply(
                serverPlayer,
                level,
                hitResult.getBlockPos(),
                serverPlayer.isCrouching()
            );
        }
        if (!serverPlayer.isCrouching()) {
            return InteractionResult.PASS;
        }
        return ownBlockOrAnchor(level, serverPlayer, hitResult);
    }

    private static InteractionResult ownBlockOrAnchor(final Level level,
                                                      final ServerPlayer player,
                                                      final BlockHitResult hitResult) {
        final var pos = hitResult.getBlockPos();
        final var state = level.getBlockState(pos);
        // 潜行 + 手上有物品时，原版会跳过方块交互直接放置物品——这里手动把交互交还给方块。
        if (state.getBlock().getDescriptionId().startsWith("block.rswirelesscrafting.")) {
            return state.useWithoutItem(level, player, hitResult);
        }
        final GlobalPos target = GlobalPos.of(level.dimension(), pos);
        if (BoundTargetResolver.resolveNetwork(level, target).isEmpty()) {
            return InteractionResult.PASS;
        }
        PairingClipboard.setAnchor(player, target);
        player.displayClientMessage(ContentNames.LINK_ANCHOR_SET, false);
        // 记完接入点就到此为止，别顺手把手里的方块放下去。
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);
        UseBlockCallback.EVENT.register(this::onUseBlock);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LinkRegistry.clearAll();
            PairingClipboard.clearAll();
        });
    }
}
