package me.kurisu.passableleaves.mixin;

import me.kurisu.passableleaves.PassableLeaves;
import net.minecraft.block.BlockState;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VineBlock.class)
public class VineBlockMixin {

    @Inject(at = @At("HEAD"), method = "shouldConnectTo", cancellable = true)
    private static void passableLeaves_shouldConnectTo(BlockView world, BlockPos neighborPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockstate = world.getBlockState(neighborPos);
        if (PassableLeaves.isLeaves(blockstate)) {
            cir.setReturnValue(true);
        }
    }
}
