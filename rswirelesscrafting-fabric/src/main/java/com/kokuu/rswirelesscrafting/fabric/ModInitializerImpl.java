package com.kokuu.rswirelesscrafting.fabric;

import com.kokuu.rswirelesscrafting.common.AbstractModInitializer;
import com.kokuu.rswirelesscrafting.common.BlockEntities;
import com.kokuu.rswirelesscrafting.common.Blocks;
import com.kokuu.rswirelesscrafting.common.ContentIds;
import com.kokuu.rswirelesscrafting.common.CreativeModeTabItems;
import com.kokuu.rswirelesscrafting.common.Items;
import com.kokuu.rswirelesscrafting.common.Platform;
import com.kokuu.rswirelesscrafting.common.amplifier.AmplifierBlock;
import com.kokuu.rswirelesscrafting.common.amplifier.AmplifierBlockEntity;
import com.kokuu.rswirelesscrafting.common.amplifier.AmplifierBlockItem;
import com.kokuu.rswirelesscrafting.common.autocraftermanager.WirelessAutocrafterManagerItem;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlock;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlockEntity;
import com.kokuu.rswirelesscrafting.common.chargingstation.ChargingStationBlockItem;
import com.kokuu.rswirelesscrafting.common.patterngrid.WirelessPatternGridItem;
import com.kokuu.rswirelesscrafting.common.upgrade.AdvancedEnergyUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.BatteryUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.EnergyUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.InterdimensionalUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.RangeUpgradeItem;
import com.kokuu.rswirelesscrafting.common.upgrade.RemoteEnergyUpgradeItem;
import com.kokuu.rswirelesscrafting.common.wirelesscraftinggrid.WirelessCraftingGridItem;

import java.util.Set;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;
import com.refinedmods.refinedstorage.fabric.api.RefinedStoragePlugin;
import com.refinedmods.refinedstorage.fabric.support.energy.EnergyStorageAdapter;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
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

        Blocks.INSTANCE.setWirelessAmplifier(blockCallback.register(
            ContentIds.WIRELESS_AMPLIFIER,
            () -> new AmplifierBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 9.0F)
                .sound(SoundType.METAL))
        ));

        final DirectRegistryCallback<BlockEntityType<?>> blockEntityCallback =
            new DirectRegistryCallback<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);
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
    }

    private void registerCapabilities() {
        EnergyStorage.ITEM.registerForItems(
            (stack, context) -> new EnergyStorageAdapter(
                Items.INSTANCE.getWirelessCraftingGrid().createEnergyStorage(stack),
                context
            ),
            Items.INSTANCE.getWirelessCraftingGrid()
        );
        EnergyStorage.SIDED.registerForBlockEntity(
            (blockEntity, context) -> new ChargingStationEnergyStorage(blockEntity),
            BlockEntities.INSTANCE.getChargingStation()
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

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);
    }
}
