package me.kurisu.passableleaves.mixin;

import me.kurisu.passableleaves.PassableLeaves;
import me.kurisu.passableleaves.enchantment.PassableLeavesEnchantments;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
    @Inject(method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void passableLeaves_adaptCollisionShape(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (PassableLeaves.isLeaves((AbstractBlock.AbstractBlockState) (Object) this)) {
            if (context instanceof EntityShapeContext) {
                Entity entity = ((EntityShapeContext) context).getEntity();

                if (entity instanceof LivingEntity) {
                    if (!entity.isPlayer() && PassableLeaves.CONFIG.playerOnlyAffected()) {
                        cir.setReturnValue(VoxelShapes.fullCube());
                        return;
                    }
                    // don't affect creative mode and flying
                    if (entity instanceof PlayerEntity) {
                        if (PassableLeaves.isFlyingInCreative(((PlayerEntity) (Object) entity))) {
                            return;
                        }
                    }

                    if (entity.isSneaking() && PassableLeaves.CONFIG.fallWhenSneakingEnabled()) {
                        return;
                    }

                    BlockPos entityPos = entity.getBlockPos();
                    // check if leaf is below player
                    if (pos.getY() < entityPos.getY()) {
                        // don't apply when the player is falling from to high
                        if (entity.fallDistance < entity.getSafeFallDistance() || !PassableLeaves.CONFIG.fallingEnabled()) {
                            if (PassableLeaves.CONFIG.enchantmentEnabled()) {
                                int enchantmentLevel = EnchantmentHelper.getEquipmentLevel(PassableLeavesEnchantments.LEAF_WALKER, (LivingEntity) entity);
                                if (enchantmentLevel > 0) {
                                    cir.setReturnValue(VoxelShapes.fullCube());
                                    return;
                                }
                            }

                            if (PassableLeaves.CONFIG.walkOnTopOfLeavesEnabled()) {
                                if (!PassableLeaves.CONFIG.sprintOnTopOfLeavesEnabled() && entity.isSprinting()) {
                                    return;
                                }
                                cir.setReturnValue(VoxelShapes.fullCube());
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "getAmbientOcclusionLightLevel", at = @At("HEAD"), cancellable = true)
    private void passableLeaves_adaptOcclusionLightLevel(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (PassableLeaves.isLeaves((AbstractBlock.AbstractBlockState) (Object) this)) {
            cir.setReturnValue(0.2F);
        }
    }

    @Inject(method = "canPathfindThrough", at = @At("HEAD"), cancellable = true)
    private void canPathfindThrough(BlockView world, BlockPos pos, NavigationType type, CallbackInfoReturnable<Boolean> cir) {
        if (PassableLeaves.isLeaves((AbstractBlock.AbstractBlockState) (Object) this)) {
            cir.setReturnValue(!PassableLeaves.CONFIG.playerOnlyAffected());
        }
    }
}
