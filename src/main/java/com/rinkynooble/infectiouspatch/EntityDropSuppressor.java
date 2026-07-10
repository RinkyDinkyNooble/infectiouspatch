package com.rinkynooble.infectiouspatch;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityDropSuppressor {

    private static volatile boolean suppressWindowActive = false;

    public EntityDropSuppressor() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (isInfectiousEntity(event.getEntity())) {
            suppressWindowActive = true;
        }
    }

    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!suppressWindowActive) return;
        if (event.getEntity() instanceof ItemEntity item && item.getOwner() == null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            suppressWindowActive = false;
        }
    }

    private static boolean isInfectiousEntity(Entity entity) {
        EntityType<?> type = entity.getType();
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return key != null && key.getNamespace().equals("infectious");
    }
}