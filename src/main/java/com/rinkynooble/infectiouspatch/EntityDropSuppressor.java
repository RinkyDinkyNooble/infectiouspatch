package com.rinkynooble.infectiouspatch;

import java.lang.StackWalker.StackFrame;
import java.util.stream.Stream;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EntityDropSuppressor {

    private static final String PROCEDURES_PACKAGE = "net.mcreator.infectious.procedures.";

    public EntityDropSuppressor() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ItemEntity item)) {
            return;
        }
        if (item.getOwner() != null) {
            return;
        }
        if (isSpawnedByInfectiousProcedure()) {
            event.setCanceled(true);
        }
    }

    private static boolean isSpawnedByInfectiousProcedure() {
        return StackWalker.getInstance()
                .walk((Stream<StackFrame> frames) -> frames
                        .anyMatch(frame -> frame.getClassName().startsWith(PROCEDURES_PACKAGE)));
    }
}