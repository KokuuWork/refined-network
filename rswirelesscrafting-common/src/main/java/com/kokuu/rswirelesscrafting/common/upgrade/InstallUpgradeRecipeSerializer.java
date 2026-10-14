package com.kokuu.rswirelesscrafting.common.upgrade;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class InstallUpgradeRecipeSerializer implements RecipeSerializer<InstallUpgradeRecipe> {
    public static final InstallUpgradeRecipeSerializer INSTANCE = new InstallUpgradeRecipeSerializer();

    private static final StreamCodec<RegistryFriendlyByteBuf, InstallUpgradeRecipe> STREAM_CODEC = StreamCodec
        .composite(
        ByteBufCodecs.STRING_UTF8,
        recipe -> recipe.getGroup(),
        ByteBufCodecs.VAR_INT,
        recipe -> recipe.category().ordinal(),
        (group, ordinal) -> new InstallUpgradeRecipe(group, CraftingBookCategory.values()[ordinal])
    );

    @Override
    public MapCodec<InstallUpgradeRecipe> codec() {
        return InstallUpgradeRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, InstallUpgradeRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
