package com.rinkynooble.infectiouspatch.mixin;

import com.rinkynooble.infectiouspatch.SpawnLightRule;
import net.mcreator.infectious.procedures.ZombieHordeOnInitialEntitySpawnProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = ZombieHordeOnInitialEntitySpawnProcedure.class, remap = false)
public abstract class ZombieHordeMixin {

    /** The same light removal as {@link LightRemovalMixin}. */
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

    /**
     * The horde summons up to 54 zombies as MOB_SUMMONED, in light too (its dark check is wrong), and
     * each zombie's own light check removes it again. For a natural horde they count as natural spawns,
     * so that still happens; for a horde from a spawner or command they stay.
     */
    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;)Lnet/minecraft/world/entity/Entity;",
                    remap = true))
    private static Entity infectiouspatch$summon(EntityType<?> type, ServerLevel level, BlockPos pos, MobSpawnType spawnType,
                                                 LevelAccessor world, double x, double y, double z, Entity horde) {
        if (!SpawnLightRule.isNaturalSpawn(horde)) {
            return type.spawn(level, pos, spawnType);
        }
        return SpawnLightRule.summonForNaturalHorde(() -> type.spawn(level, pos, spawnType));
    }
}
