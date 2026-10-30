package com.kokuu.refinednetwork.neoforge;

import com.kokuu.refinednetwork.common.AbstractModInitializer;
import com.kokuu.refinednetwork.common.BlockEntities;
import com.kokuu.refinednetwork.common.Blocks;
import com.kokuu.refinednetwork.common.ContentIds;
import com.kokuu.refinednetwork.common.ContentNames;
import com.kokuu.refinednetwork.common.CreativeModeTabItems;
import com.kokuu.refinednetwork.common.Items;
import com.kokuu.refinednetwork.common.Platform;
import com.kokuu.refinednetwork.common.amplifier.AmplifierBlock;
import com.kokuu.refinednetwork.common.amplifier.AmplifierBlockEntity;
import com.kokuu.refinednetwork.common.amplifier.AmplifierBlockItem;
import com.kokuu.refinednetwork.common.autocraftermanager.WirelessAutocrafterManagerItem;
import com.kokuu.refinednetwork.common.bridge.WirelessBridgeBlock;
import com.kokuu.refinednetwork.common.bridge.WirelessBridgeBlockEntity;
import com.kokuu.refinednetwork.common.bridge.WirelessBridgeBlockItem;
import com.kokuu.refinednetwork.common.chargingstation.ChargingStationBlock;
import com.kokuu.refinednetwork.common.chargingstation.ChargingStationBlockEntity;
import com.kokuu.refinednetwork.common.chargingstation.ChargingStationBlockItem;
import com.kokuu.refinednetwork.common.configcard.WirelessConfigurationCardItem;
import com.kokuu.refinednetwork.common.connector.WirelessConnectorItem;
import com.kokuu.refinednetwork.common.energymonitor.EnergyMonitorBlock;
import com.kokuu.refinednetwork.common.energymonitor.EnergyMonitorBlockEntity;
import com.kokuu.refinednetwork.common.energymonitor.EnergyMonitorBlockItem;
import com.kokuu.refinednetwork.common.energyreceiver.WirelessEnergyReceiverBlock;
import com.kokuu.refinednetwork.common.energyreceiver.WirelessEnergyReceiverBlockEntity;
import com.kokuu.refinednetwork.common.energyreceiver.WirelessEnergyReceiverBlockItem;
import com.kokuu.refinednetwork.common.link.BoundTargetResolver;
import com.kokuu.refinednetwork.common.link.LinkRegistry;
import com.kokuu.refinednetwork.common.link.PairingClipboard;
import com.kokuu.refinednetwork.common.monitor.WirelessMonitorItem;
import com.kokuu.refinednetwork.common.networkbridge.NetworkBridgeBlock;
import com.kokuu.refinednetwork.common.networkbridge.NetworkBridgeBlockEntity;
import com.kokuu.refinednetwork.common.networkbridge.NetworkBridgeBlockItem;
import com.kokuu.refinednetwork.common.patterngrid.WirelessPatternGridItem;
import com.kokuu.refinednetwork.common.redstonelink.RedstoneLinkBlock;
import com.kokuu.refinednetwork.common.redstonelink.RedstoneLinkBlockEntity;
import com.kokuu.refinednetwork.common.redstonelink.RedstoneLinkBlockItem;
import com.kokuu.refinednetwork.common.smartsorter.SmartSorterBlock;
import com.kokuu.refinednetwork.common.smartsorter.SmartSorterBlockEntity;
import com.kokuu.refinednetwork.common.smartsorter.SmartSorterBlockItem;
import com.kokuu.refinednetwork.common.terminal.AbstractWirelessTerminalItem;
import com.kokuu.refinednetwork.common.upgrade.AdvancedEnergyUpgradeItem;
import com.kokuu.refinednetwork.common.upgrade.BatteryUpgradeItem;
import com.kokuu.refinednetwork.common.upgrade.EnergyUpgradeItem;
import com.kokuu.refinednetwork.common.upgrade.InterdimensionalUpgradeItem;
import com.kokuu.refinednetwork.common.upgrade.OverclockUpgradeItem;
import com.kokuu.refinednetwork.common.upgrade.OverclockUpgrades;
import com.kokuu.refinednetwork.common.upgrade.RangeUpgradeItem;
import com.kokuu.refinednetwork.common.upgrade.RemoteEnergyUpgradeItem;
import com.kokuu.refinednetwork.common.wirelesscraftinggrid.WirelessCraftingGridItem;

import java.util.function.Supplier;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;
import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi;
import com.refinedmods.refinedstorage.neoforge.support.energy.EnergyStorageAdapter;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.kokuu.refinednetwork.common.ModIdentifierUtil.MOD_ID;

@Mod(MOD_ID)
public class ModInitializer extends AbstractModInitializer {
    private final DeferredRegister<Block> blockRegistry = DeferredRegister.create(BuiltInRegistries.BLOCK, MOD_ID);
    private final DeferredRegister<BlockEntityType<?>> blockEntityTypeRegistry =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);
    private final DeferredRegister<Item> itemRegistry = DeferredRegister.create(BuiltInRegistries.ITEM, MOD_ID);
    private final DeferredRegister<MenuType<?>> menuTypeRegistry =
        DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);
    private final DeferredRegister<DataComponentType<?>> dataComponentTypeRegistry = DeferredRegister.create(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        MOD_ID
    );

    public ModInitializer(final IEventBus eventBus, final ModContainer modContainer) {
        final ConfigImpl config = new ConfigImpl();
        modContainer.registerConfig(ModConfig.Type.COMMON, config.getSpec());
        Platform.setConfigProvider(() -> config);
        Platform.setItemEnergyProvider(new PlatformImpl());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            eventBus.addListener(ClientModInitializer::onClientSetup);
            eventBus.addListener(ClientModInitializer::onRegisterRenderers);
            eventBus.addListener(ClientModInitializer::onRegisterMenuScreens);
            eventBus.addListener(ClientModInitializer::onRegisterKeyMappings);
            eventBus.addListener(ClientModInitializer::onRegisterGuiLayers);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
        registerContent(eventBus);
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        // 升级注册必须等物品真正登记完（DeferredRegister 要等注册事件触发），所以放到 setup 阶段。
        eventBus.addListener((final FMLCommonSetupEvent event) ->
            event.enqueueWork(OverclockUpgrades::register));
        eventBus.addListener(this::registerCapabilities);
        eventBus.addListener(this::registerCreativeModeTabListener);
    }

    private void onServerStopping(final ServerStoppingEvent event) {
        LinkRegistry.clearAll();
        PairingClipboard.clearAll();
    }

    private void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
            || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // 连接器：任何姿态都接管——敲哪台连哪台，绝不放方块。
        if (player.getMainHandItem().getItem() instanceof WirelessConnectorItem connector) {
            final InteractionResult result =
                connector.apply(player, event.getLevel(), event.getPos(), player.isCrouching());
            event.setCanceled(true);
            event.setCancellationResult(result);
            return;
        }
        if (!player.isCrouching()) {
            return;
        }
        // 潜行 + 手上有物品时原版会跳过方块交互直接放置，这里手动把交互交还给本模组方块。
        final InteractionResult result = ownBlockOrAnchor(event, player);
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    private static InteractionResult ownBlockOrAnchor(final PlayerInteractEvent.RightClickBlock event,
                                                       final ServerPlayer player) {
        final var pos = event.getPos();
        final var state = event.getLevel().getBlockState(pos);
        if (state.getBlock().getDescriptionId().startsWith("block.refinednetwork.")) {
            return state.useWithoutItem(event.getLevel(), player, event.getHitVec());
        }
        final GlobalPos target = GlobalPos.of(event.getLevel().dimension(), pos);
        if (BoundTargetResolver.resolveNetwork(event.getLevel(), target).isEmpty()) {
            return InteractionResult.PASS;
        }
        PairingClipboard.setAnchor(player, target);
        player.displayClientMessage(ContentNames.LINK_ANCHOR_SET, false);
        // 记完接入点就到此为止，别顺手把手里的方块放下去。
        return InteractionResult.SUCCESS;
    }

    private void registerContent(final IEventBus eventBus) {
        registerBlocks(eventBus);
        registerItems(eventBus);
        registerMenus(eventBus);
        registerDataComponents(eventBus);
    }

    private void registerBlocks(final IEventBus eventBus) {
        final RegistryCallback<Block> callback = new ForgeRegistryCallback<>(blockRegistry);
        Blocks.INSTANCE.setTerminalChargingStation(callback.register(
            ContentIds.TERMINAL_CHARGING_STATION,
            () -> new ChargingStationBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.5F, 7.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setWirelessBridge(callback.register(
            ContentIds.WIRELESS_BRIDGE,
            () -> new WirelessBridgeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 9.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setWirelessAmplifier(callback.register(
            ContentIds.WIRELESS_AMPLIFIER,
            () -> new AmplifierBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 9.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setWirelessEnergyReceiver(callback.register(
            ContentIds.WIRELESS_ENERGY_RECEIVER,
            () -> new WirelessEnergyReceiverBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 9.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setEnergyMonitor(callback.register(
            ContentIds.ENERGY_MONITOR,
            () -> new EnergyMonitorBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.5F, 7.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setSmartSorter(callback.register(
            ContentIds.SMART_SORTER,
            () -> new SmartSorterBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 9.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setNetworkBridge(callback.register(
            ContentIds.NETWORK_BRIDGE,
            () -> new NetworkBridgeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 9.0F)
                .sound(SoundType.METAL))
        ));
        Blocks.INSTANCE.setRedstoneLink(callback.register(
            ContentIds.REDSTONE_LINK,
            () -> new RedstoneLinkBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE))
        ));
        blockRegistry.register(eventBus);

        final RegistryCallback<BlockEntityType<?>> blockEntityCallback =
            new ForgeRegistryCallback<>(blockEntityTypeRegistry);
        BlockEntities.INSTANCE.setChargingStation(blockEntityCallback.register(
            ContentIds.CHARGING_STATION,
            () -> BlockEntityType.Builder.of(
                ChargingStationBlockEntity::new,
                Blocks.INSTANCE.getTerminalChargingStation()
            ).build(null)
        ));
        BlockEntities.INSTANCE.setWirelessBridge(blockEntityCallback.register(
            ContentIds.WIRELESS_BRIDGE,
            () -> BlockEntityType.Builder.of(
                WirelessBridgeBlockEntity::new,
                Blocks.INSTANCE.getWirelessBridge()
            ).build(null)
        ));
        BlockEntities.INSTANCE.setWirelessAmplifier(blockEntityCallback.register(
            ContentIds.WIRELESS_AMPLIFIER,
            () -> BlockEntityType.Builder.of(
                AmplifierBlockEntity::new,
                Blocks.INSTANCE.getWirelessAmplifier()
            ).build(null)
        ));
        BlockEntities.INSTANCE.setWirelessEnergyReceiver(blockEntityCallback.register(
            ContentIds.WIRELESS_ENERGY_RECEIVER,
            () -> BlockEntityType.Builder.of(
                WirelessEnergyReceiverBlockEntity::new,
                Blocks.INSTANCE.getWirelessEnergyReceiver()
            ).build(null)
        ));
        BlockEntities.INSTANCE.setEnergyMonitor(blockEntityCallback.register(
            ContentIds.ENERGY_MONITOR,
            () -> BlockEntityType.Builder.of(
                EnergyMonitorBlockEntity::new,
                Blocks.INSTANCE.getEnergyMonitor()
            ).build(null)
        ));
        BlockEntities.INSTANCE.setSmartSorter(blockEntityCallback.register(
            ContentIds.SMART_SORTER,
            () -> BlockEntityType.Builder.of(
                SmartSorterBlockEntity::new,
                Blocks.INSTANCE.getSmartSorter()
            ).build(null)
        ));
        BlockEntities.INSTANCE.setNetworkBridge(blockEntityCallback.register(
            ContentIds.NETWORK_BRIDGE,
            () -> BlockEntityType.Builder.of(
                NetworkBridgeBlockEntity::new,
                Blocks.INSTANCE.getNetworkBridge()
            ).build(null)
        ));
        BlockEntities.INSTANCE.setRedstoneLink(blockEntityCallback.register(
            ContentIds.REDSTONE_LINK,
            () -> BlockEntityType.Builder.of(
                RedstoneLinkBlockEntity::new,
                Blocks.INSTANCE.getRedstoneLink()
            ).build(null)
        ));
        blockEntityTypeRegistry.register(eventBus);
    }

    private void registerItems(final IEventBus eventBus) {
        final RegistryCallback<Item> callback = new ForgeRegistryCallback<>(itemRegistry);
        registerCustomItems(callback);
        itemRegistry.register(eventBus);
    }

    private void registerCustomItems(final RegistryCallback<Item> callback) {
        Items.INSTANCE.setWirelessCraftingGrid(
            callback.register(ContentIds.WIRELESS_CRAFTING_GRID, () -> new WirelessCraftingGridItem(
                RefinedStorageApi.INSTANCE.getEnergyItemHelper(),
                RefinedStorageApi.INSTANCE.getNetworkItemHelper()
            ) {
                @Override
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
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
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
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
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
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
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
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
        registerRedstoneLinkItem(callback);
    }

    private void registerRedstoneLinkItem(final RegistryCallback<Item> callback) {
        Items.INSTANCE.setRedstoneLinkBlockItem(
            callback.register(
                ContentIds.REDSTONE_LINK,
                () -> new RedstoneLinkBlockItem(
                    Blocks.INSTANCE.getRedstoneLink(),
                    new Item.Properties()
                )
            )
        );
    }

    private void registerMenus(final IEventBus eventBus) {
        registerMenus(new ForgeRegistryCallback<>(menuTypeRegistry), new ExtendedMenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu, D> MenuType<T> create(final MenuSupplier<T, D> supplier,
                                                                           final StreamCodec<RegistryFriendlyByteBuf, D>
                                                                               streamCodec) {
                return IMenuTypeExtension.create((syncId, inventory, buf) -> {
                    final D data = streamCodec.decode(buf);
                    return supplier.create(syncId, inventory, data);
                });
            }
        });
        menuTypeRegistry.register(eventBus);
    }

    private void registerDataComponents(final IEventBus eventBus) {
        final RegistryCallback<DataComponentType<?>> callback = new ForgeRegistryCallback<>(dataComponentTypeRegistry);
        registerDataComponents(callback);
        dataComponentTypeRegistry.register(eventBus);
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        // 四件无线终端共用同一套能量能力（容量来自 AbstractWirelessTerminalItem）。
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, ctx) -> new EnergyStorageAdapter(
                ((AbstractWirelessTerminalItem) stack.getItem()).createEnergyStorage(stack)
            ),
            Items.INSTANCE.getWirelessCraftingGrid(),
            Items.INSTANCE.getWirelessPatternGrid(),
            Items.INSTANCE.getWirelessAutocrafterManager(),
            Items.INSTANCE.getWirelessMonitor()
        );
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            BlockEntities.INSTANCE.getChargingStation(),
            (blockEntity, side) -> new ChargingStationEnergyStorage(blockEntity)
        );
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            BlockEntities.INSTANCE.getWirelessEnergyReceiver(),
            (blockEntity, side) -> new WirelessEnergyReceiverEnergyStorage(blockEntity)
        );
        // 线缆和网络只认「方块类型上注册的容器能力」，只把容器塞在 BE 里是不够的。
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getChargingStation());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getWirelessAmplifier());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getWirelessBridge());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getNetworkBridge());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getSmartSorter());
    }

    private void registerNetworkNodeContainerProvider(
        final RegisterCapabilitiesEvent event,
        final BlockEntityType<? extends AbstractNetworkNodeContainerBlockEntity<?>> type
    ) {
        event.registerBlockEntity(
            RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),
            type,
            (blockEntity, side) -> blockEntity.getContainerProvider()
        );
    }

    private void registerCreativeModeTabListener(final BuildCreativeModeTabContentsEvent e) {
        final ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            RefinedStorageApi.INSTANCE.getCreativeModeTabId()
        );
        if (!e.getTabKey().equals(creativeModeTab)) {
            return;
        }
        CreativeModeTabItems.addItems(e::accept);
    }

    private record ForgeRegistryCallback<T>(DeferredRegister<T> registry) implements RegistryCallback<T> {
        @Override
        public <R extends T> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
            return registry.register(id.getPath(), value);
        }
    }
}
