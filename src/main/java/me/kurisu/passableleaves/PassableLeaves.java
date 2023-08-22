package me.kurisu.passableleaves;

import me.kurisu.passableleaves.enchantment.PassableLeavesEnchantments;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PassableLeaves implements ModInitializer {
    public static final String MOD_ID = "passableleaves";
    public static final me.kurisu.passableleaves.PassableLeavesConfig CONFIG = me.kurisu.passableleaves.PassableLeavesConfig.createAndLoad();
    public static final TagKey<Block> EXCLUDED_LEAVES = TagKey.of(Registry.BLOCK_KEY, new Identifier(MOD_ID, "excluded_leaves"));

    @Override
    public void onInitialize() {
        PassableLeavesEnchantments.initialize();
    }

    public static boolean isFlyingInCreative(PlayerEntity playerEntity) {
        return playerEntity.getAbilities().creativeMode && playerEntity.getAbilities().flying;
    }

    public static boolean isLeaves(AbstractBlock.AbstractBlockState block) {
        return block.isIn(BlockTags.LEAVES) && !block.isIn(EXCLUDED_LEAVES);
    }
}
