package com.rinkynooble.infectiouspatch.verify;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dev-only spawn checks for Infectious Patch, run as server commands on a headless server.
 * Never shipped. It doesn't need the patch, so the same jar runs with and without it and the two
 * results are compared (research/server/compare.py).
 *
 * <p>{@code /infectiouspatchverify setup} builds two closed stone cells far from any player, one with
 * a glowstone block in its wall. After the light engine catches up, {@code /infectiouspatchverify run}
 * spawns every Infectious mob type whose spawn setup runs one of the 16 light-check procedures, with
 * every spawn type, in each cell, and writes whether each one stayed to infectiouspatch-verify.txt.
 */
@Mod("infectiouspatch_verify")
public class SpawnCheck {

    private static final String[] PROCEDURES = {
            "AncientZombie", "BalloonZombie", "BeehiveZombie", "BouncerZombie", "CrawlingZombie", "DoorZombie",
            "FatZombie", "FranklinZombie", "FungalZombie", "HazmatZombie", "PumpkinZombie", "ZombieClimber",
            "ZombieGirl", "ZombieHitler", "ZombieHorde", "Zombie",
    };
    private static final BlockPos LIT = new BlockPos(8, 100, 8);
    private static final BlockPos DARK = new BlockPos(24, 100, 8);

    public SpawnCheck() {
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("infectiouspatchverify")
                .then(Commands.literal("setup").executes(SpawnCheck::setup))
                .then(Commands.literal("run").executes(SpawnCheck::run)));
    }

    private static int setup(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getServer().overworld();
        for (BlockPos cell : new BlockPos[] {LIT, DARK}) {
            level.setChunkForced(cell.getX() >> 4, cell.getZ() >> 4, true);
            for (BlockPos pos : BlockPos.betweenClosed(cell.offset(-2, -1, -2), cell.offset(2, 3, 2))) {
                boolean inside = Math.abs(pos.getX() - cell.getX()) <= 1 && Math.abs(pos.getZ() - cell.getZ()) <= 1
                        && pos.getY() >= cell.getY() && pos.getY() <= cell.getY() + 2;
                level.setBlockAndUpdate(pos, inside ? Blocks.AIR.defaultBlockState() : Blocks.STONE.defaultBlockState());
            }
        }
        level.setBlockAndUpdate(LIT.offset(2, 1, 0), Blocks.GLOWSTONE.defaultBlockState());
        say(ctx, "SETUP done: lit cell at " + LIT.toShortString() + ", dark cell at " + DARK.toShortString());
        return Command.SINGLE_SUCCESS;
    }

    private static int run(CommandContext<CommandSourceStack> ctx) {
        try {
            return runChecks(ctx);
        } catch (Throwable t) {
            say(ctx, "FAIL: " + t);
            t.printStackTrace();
            return 0;
        }
    }

    private static int runChecks(CommandContext<CommandSourceStack> ctx) throws Exception {
        ServerLevel level = ctx.getSource().getServer().overworld();
        int litLight = level.getMaxLocalRawBrightness(LIT);
        int darkLight = level.getMaxLocalRawBrightness(DARK);
        say(ctx, "LIGHT lit=" + litLight + " dark=" + darkLight);
        if (litLight <= 4 || darkLight > 4) {
            say(ctx, "FAIL: the cells don't have the light the check needs (lit above 4, dark 4 or less).");
            return 0;
        }

        // Every Infectious mob type, with the light-check procedure its spawn setup runs.
        Map<ResourceLocation, String> types = new LinkedHashMap<>();
        for (ResourceLocation id : ForgeRegistries.ENTITY_TYPES.getKeys().stream().sorted().toList()) {
            if (!id.getNamespace().equals("infectious")) {
                continue;
            }
            Entity probe = ForgeRegistries.ENTITY_TYPES.getValue(id).create(level);
            if (probe instanceof Mob) {
                String procedure = procedureOf(probe.getClass());
                if (procedure != null) {
                    types.put(id, procedure);
                }
            }
        }
        say(ctx, "TYPES " + types.size() + " mob types run one of the 16 procedures");

        List<String> lines = new ArrayList<>();
        int[][] stayed = new int[MobSpawnType.values().length][2];
        for (Map.Entry<ResourceLocation, String> entry : types.entrySet()) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entry.getKey());
            for (MobSpawnType spawnType : MobSpawnType.values()) {
                for (int cell = 0; cell < 2; cell++) {
                    BlockPos pos = cell == 0 ? LIT : DARK;
                    Entity entity = type.spawn(level, pos, spawnType);
                    boolean kept = entity != null && !entity.isRemoved() && level.getEntity(entity.getUUID()) == entity;
                    int others = clearCell(level, pos, entity);
                    if (kept) {
                        stayed[spawnType.ordinal()][cell]++;
                    }
                    lines.add(entry.getKey() + " " + spawnType + " " + (cell == 0 ? "lit" : "dark")
                            + " stayed=" + (kept ? "yes" : "no") + " others=" + others);
                }
            }
        }

        // A mob whose spawn type nobody recorded: run its procedure directly on a mob that never had finalizeSpawn.
        for (String procedure : PROCEDURES) {
            ResourceLocation id = types.entrySet().stream().filter(e -> e.getValue().equals(procedure))
                    .map(Map.Entry::getKey).findFirst().orElse(null);
            if (id == null) {
                lines.add("unrecorded " + procedure + " missing");
                continue;
            }
            Mob mob = (Mob) ForgeRegistries.ENTITY_TYPES.getValue(id).create(level);
            mob.moveTo(LIT.getX() + 0.5, LIT.getY(), LIT.getZ() + 0.5, 0, 0);
            Method execute = Class.forName("net.mcreator.infectious.procedures." + procedure + "OnInitialEntitySpawnProcedure")
                    .getMethod("execute", LevelAccessor.class, double.class, double.class, double.class, Entity.class);
            execute.invoke(null, level, mob.getX(), mob.getY(), mob.getZ(), mob);
            int others = clearCell(level, LIT, null);
            lines.add("unrecorded " + procedure + " " + id + " spawnType=" + mob.getSpawnType()
                    + " removed=" + (mob.isRemoved() ? "yes" : "no") + " others=" + others);
        }

        Files.write(Path.of("infectiouspatch-verify.txt"), lines, StandardCharsets.UTF_8);
        for (MobSpawnType spawnType : MobSpawnType.values()) {
            say(ctx, String.format("STAYED %-16s lit %3d of %d, dark %3d of %d", spawnType,
                    stayed[spawnType.ordinal()][0], types.size(), stayed[spawnType.ordinal()][1], types.size()));
        }
        say(ctx, "DONE " + lines.size() + " lines written to infectiouspatch-verify.txt");
        return Command.SINGLE_SUCCESS;
    }

    /** Removes everything a spawn left in the cell; returns how many entities besides the spawned one there were. */
    private static int clearCell(ServerLevel level, BlockPos cell, Entity spawned) {
        int others = 0;
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(cell).inflate(8), e -> !(e instanceof Player))) {
            if (entity != spawned) {
                others++;
            }
            entity.discard();
        }
        if (spawned != null && !spawned.isRemoved()) {
            spawned.discard();
        }
        return others;
    }

    /** Which of the 16 procedures a mob class calls, read from its class file. */
    private static String procedureOf(Class<?> type) throws IOException {
        try (InputStream in = type.getResourceAsStream(type.getSimpleName() + ".class")) {
            if (in == null) {
                return null;
            }
            String classFile = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
            for (String procedure : PROCEDURES) {
                if (classFile.contains("net/mcreator/infectious/procedures/" + procedure + "OnInitialEntitySpawnProcedure")) {
                    return procedure;
                }
            }
            return null;
        }
    }

    private static void say(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSuccess(() -> Component.literal("[infectiouspatchverify] " + message), false);
    }
}
