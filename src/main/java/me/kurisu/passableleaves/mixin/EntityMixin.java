package me.kurisu.passableleaves.mixin;

import me.kurisu.passableleaves.PassableLeaves;
import me.kurisu.passableleaves.access.EntityAccess;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityAccess {
    @Shadow
    public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Shadow
    public World world;

    @Shadow
    public abstract Box getBoundingBox();

    private boolean isInsideLeaves;
    private BlockState lastLeafFalledOn;

    public boolean getIsInsideLeaves() {
        return this.isInsideLeaves;
    }

    @Inject(method = "baseTick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;inPowderSnow:Z"))
    private void passableLeaves_baseTick(CallbackInfo ci) {

        BlockPos leafBlockPos = this.getLeafPositionEntityIsInside();
        this.isInsideLeaves = leafBlockPos != null;

        if (this.isInsideLeaves) {
            this.handleInsideLeaves(leafBlockPos);
        }
    }

    @Inject(method = "playStepSound", at = @At("TAIL"))
    private void passableLeaves_playLeafStepSound(CallbackInfo ci) {
        if (this.isInsideLeaves && PassableLeaves.CONFIG.soundEnabled()) {
            BlockSoundGroup soundGroup = BlockSoundGroup.AZALEA_LEAVES;
            this.playSound(soundGroup.getBreakSound(), soundGroup.getVolume() * 0.6F,
                    soundGroup.getPitch());
        }
    }

    private BlockPos getLeafPositionEntityIsInside() {
        Box contractedBoundingBox = this.getBoundingBox().contract(0.1F);
        return BlockPos.stream(contractedBoundingBox).filter((pos) -> {
            BlockState blockState = this.world.getBlockState(pos);
            return PassableLeaves.isLeaves(blockState);
        }).findFirst().orElse(null);
    }

    private void handleInsideLeaves(BlockPos blockPos) {
        // don't affect creative mode and flying
        if (((Entity) (Object) this) instanceof PlayerEntity) {
            if (PassableLeaves.isFlyingInCreative(((PlayerEntity) (Object) this))) {
                return;
            }
        }

        if (PassableLeaves.CONFIG.fallingEnabled()) {
            BlockState leafBlockState = this.world.getBlockState(blockPos);
            this.fallingOnLeaves(leafBlockState, this.world, ((Entity) (Object) this));
        }
    }

    private void fallingOnLeaves(BlockState blockState, World world, Entity entity) {
        if (entity.isOnGround()) {
            this.lastLeafFalledOn = null;
            return;
        }

        // dont apply same effect on same leaf
        if(blockState.equals(this.lastLeafFalledOn)){
            return;
        }

        if (entity.fallDistance > entity.getSafeFallDistance()) {
            lastLeafFalledOn = blockState;
            if (PassableLeaves.CONFIG.soundEnabled()) {
                BlockSoundGroup soundGroup = BlockSoundGroup.AZALEA_LEAVES;
                entity.playSound(soundGroup.getBreakSound(),
                        Math.min(soundGroup.getVolume() + 0.5F, soundGroup.getVolume() + entity.fallDistance - entity.getSafeFallDistance()),
                        soundGroup.getPitch() * 0.7F);
            }

            Vec3d slowDownedVelocity = entity.getVelocity().multiply(PassableLeaves.CONFIG.fallingSpeedReductionMultiplier());
            entity.setVelocity(slowDownedVelocity);

            // spawn fancy particle
            if (!world.isClient && PassableLeaves.CONFIG.particlesEnabled()) {
                int particleCount = MathHelper.ceil(entity.fallDistance - entity.getSafeFallDistance()) * 100;

                ((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState),
                        entity.getX(), entity.getY(), entity.getZ(), particleCount,
                        0.0, 0.0, 0.0, 0.15000000596046448);
            }

            entity.fallDistance = entity.fallDistance * PassableLeaves.CONFIG.fallingDistanceReductionMultiplier();
            entity.handleFallDamage(entity.fallDistance, 1.0F, DamageSource.FALL);
        }
    }
}
