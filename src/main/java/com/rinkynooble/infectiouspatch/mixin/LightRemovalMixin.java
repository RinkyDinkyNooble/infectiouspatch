package com.rinkynooble.infectiouspatch.mixin;

import com.rinkynooble.infectiouspatch.SpawnLightRule;
import net.mcreator.infectious.procedures.AncientZombieOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.BalloonZombieOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.BeehiveZombieOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.BouncerZombieOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.CrawlingZombieOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.DoorZombieOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.FatZombieOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.FranklinZombieOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.FungalZombieOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.HazmatZombieOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.PumpkinZombieOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.ZombieClimberOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.ZombieGirlOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.ZombieHitlerOnInitialEntitySpawnProcedure;
import net.mcreator.infectious.procedures.ZombieOnInitialEntitySpawnProcedure;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * The spawn procedures with Infectious' light removal (the Zombie Horde's is in {@link ZombieHordeMixin}).
 * Between them, about 95 mob types run one of these; CrawlingZombie's alone runs for about 80.
 */
@Mixin(value = {
        AncientZombieOnInitialEntitySpawnProcedure.class,
        BalloonZombieOnInitialEntitySpawnProcedure.class,
        BeehiveZombieOnInitialEntitySpawnProcedure.class,
        BouncerZombieOnInitialEntitySpawnProcedure.class,
        CrawlingZombieOnInitialEntitySpawnProcedure.class,
        DoorZombieOnInitialEntitySpawnProcedure.class,
        FatZombieOnInitialEntitySpawnProcedure.class,
        FranklinZombieOnInitialEntitySpawnProcedure.class,
        FungalZombieOnInitialEntitySpawnProcedure.class,
        HazmatZombieOnInitialEntitySpawnProcedure.class,
        PumpkinZombieOnInitialEntitySpawnProcedure.class,
        ZombieClimberOnInitialEntitySpawnProcedure.class,
        ZombieGirlOnInitialEntitySpawnProcedure.class,
        ZombieHitlerOnInitialEntitySpawnProcedure.class,
        ZombieOnInitialEntitySpawnProcedure.class,
}, remap = false)
public abstract class LightRemovalMixin {

    /**
     * Only the removal right after the light check. The others (Ancient Zombie's gamerule, and one
     * Ancient, Franklin or Hitler zombie per world) come before it or after it and stay as they are.
     */
    @Redirect(
            method = "execute",
            slice = @Slice(from = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelAccessor;getMaxLocalRawBrightness(Lnet/minecraft/core/BlockPos;)I",
                    remap = true)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;discard()V", ordinal = 0, remap = true))
    private static void infectiouspatch$lightRemoval(Entity entity) {
        if (SpawnLightRule.keepsLightRemoval(entity)) {
            entity.discard();
        }
    }
}
