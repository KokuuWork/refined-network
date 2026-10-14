package com.kokuu.rswirelesscrafting.common.upgrade;

import com.kokuu.rswirelesscrafting.common.DataComponents;
import com.kokuu.rswirelesscrafting.common.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

/**
 * 无序合成配方：无线合成终端 + N 张升级卡 → 装有这些升级卡的终端。
 * 终端的其余数据组件（合成矩阵、能量、网卡绑定）全部保留。
 *
 * <p>参照 RS 2 的 {@code StorageContainerUpgradeRecipe}：继承 {@link ShapelessRecipe}
 * 并覆写 {@link #matches} / {@link #assemble}，因为「任意终端 + 任意升级卡」是动态匹配，
 * 无法用固定的 ingredient 列表表达。
 */
public class InstallUpgradeRecipe extends ShapelessRecipe {
    public static final MapCodec<InstallUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.optionalFieldOf("group", "").forGetter(InstallUpgradeRecipe::getGroup),
        Codec.STRING.optionalFieldOf("category", CraftingBookCategory.MISC.name())
            .xmap(CraftingBookCategory::valueOf, Enum::name)
            .forGetter(recipe -> recipe.category())
    ).apply(instance, InstallUpgradeRecipe::new));

    public InstallUpgradeRecipe(final String group, final CraftingBookCategory category) {
        // The result must not be empty: every recipe is encoded through the recipe registry when
        // the server syncs 'update_recipes', and ItemStack.STREAM_CODEC rejects empty stacks.
        super(
            group,
            category,
            Items.INSTANCE.getWirelessCraftingGrid().getDefaultInstance(),
            NonNullList.create()
        );
    }

    /**
     * Without this override the inherited implementation returns vanilla's {@code shapeless}
     * serializer, so the recipe would be encoded by {@code ShapelessRecipe.Serializer#toNetwork}
     * — which serialises the raw result field and aborts the connection with
     * "Empty ItemStack not allowed". It would also be decoded back as a plain
     * {@link ShapelessRecipe} on the client, losing {@code getSerializer}/{@code matches}.
     */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return InstallUpgradeRecipeSerializer.INSTANCE;
    }

    /**
     * The ingredient list is intentionally empty because matching is fully dynamic, so this
     * recipe cannot be represented in the recipe book. This mirrors vanilla's
     * {@link net.minecraft.world.item.crafting.CustomRecipe}, which is special by default.
     */
    @Override
    public boolean isSpecial() {
        return true;
    }

    private static Optional<ItemStack> findGrid(final CraftingInput input) {
        ItemStack found = null;
        for (int i = 0; i < input.size(); ++i) {
            final ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.INSTANCE.getWirelessCraftingGrid())) {
                if (found != null) {
                    return Optional.empty();
                }
                found = stack;
            }
        }
        return Optional.ofNullable(found);
    }

    private static List<ItemStack> findUpgrades(final CraftingInput input) {
        final List<ItemStack> upgrades = new ArrayList<>();
        for (int i = 0; i < input.size(); ++i) {
            final ItemStack stack = input.getItem(i);
            if (stack.isEmpty() || stack.is(Items.INSTANCE.getWirelessCraftingGrid())) {
                continue;
            }
            if (isSupportedUpgrade(stack)) {
                upgrades.add(stack);
            }
        }
        return upgrades;
    }

    private static boolean isSupportedUpgrade(final ItemStack stack) {
        return stack.is(Upgrades.RANGE_UPGRADES) || stack.is(Upgrades.INTERDIMENSIONAL_UPGRADES);
    }

    @Override
    public boolean matches(final CraftingInput input, final Level level) {
        final Optional<ItemStack> grid = findGrid(input);
        if (grid.isEmpty()) {
            return false;
        }
        final List<ItemStack> upgrades = findUpgrades(input);
        return !upgrades.isEmpty() && canInstall(grid.get(), upgrades);
    }

    private boolean canInstall(final ItemStack grid, final List<ItemStack> newUpgrades) {
        final List<ItemStack> existing = Upgrades.getUpgrades(grid);
        final List<ItemStack> merged = new ArrayList<>(existing);
        for (final ItemStack upgrade : newUpgrades) {
            if (merged.size() + 1 > Upgrades.MAX_UPGRADES) {
                return false;
            }
            merged.add(upgrade.copy());
        }
        return true;
    }

    @Override
    public ItemStack assemble(final CraftingInput input, final HolderLookup.Provider registries) {
        final Optional<ItemStack> grid = findGrid(input);
        if (grid.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final ItemStack result = grid.get().copy();
        final List<ItemStack> upgrades = new ArrayList<>(Upgrades.getUpgrades(result));
        upgrades.addAll(findUpgrades(input).stream().map(ItemStack::copy).toList());
        result.set(DataComponents.INSTANCE.getUpgrades(), List.copyOf(upgrades));
        return result;
    }
}
