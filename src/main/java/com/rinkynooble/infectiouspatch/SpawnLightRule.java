package com.rinkynooble.infectiouspatch;

import java.util.function.Supplier;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;

/**
 * Decides whether Infectious' light removal still applies to a mob.
 *
 * <p>In its spawn setup, Infectious removes many of its mobs when no player is within 5 blocks and
 * the spot is lit. For natural spawns that keeps them off lit streets; for spawners, commands and
 * spawn eggs it removes the mob it was asked to place. The removal now only applies to natural spawns.
 */
public final class SpawnLightRule {

    /** How many zombies a natural horde is summoning on this thread right now (0 or 1). */
    private static final ThreadLocal<int[]> NATURAL_HORDE_SUMMONS = ThreadLocal.withInitial(() -> new int[1]);

    private SpawnLightRule() {
    }

    public static boolean keepsLightRemoval(Entity entity) {
        return NATURAL_HORDE_SUMMONS.get()[0] > 0 || isNaturalSpawn(entity);
    }

    /**
     * Natural and chunk generation spawns. A mob without a recorded spawn type counts as natural,
     * so it keeps today's behaviour.
     */
    public static boolean isNaturalSpawn(Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return true;
        }
        MobSpawnType type = mob.getSpawnType();
        return type == null || type == MobSpawnType.NATURAL || type == MobSpawnType.CHUNK_GENERATION;
    }

    /** Runs a natural horde's summon, during which the summoned zombie counts as a natural spawn. */
    public static <T> T summonForNaturalHorde(Supplier<T> summon) {
        int[] summons = NATURAL_HORDE_SUMMONS.get();
        summons[0]++;
        try {
            return summon.get();
        } finally {
            summons[0]--;
        }
    }
}
