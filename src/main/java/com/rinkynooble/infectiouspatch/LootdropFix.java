package com.rinkynooble.infectiouspatch;

import java.util.Locale;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(LootdropFix.MODID)
public class LootdropFix {

    public static final String MODID = "infectiouspatch";

    private static final String[] MATCH_FRAGMENTS = {
        "will land!",
        "entity.bat.takeoff neutral @p ~ ~ ~ 1 0.7",
    };

    public LootdropFix() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static boolean isLootdropAnnouncementCommand(String lowerCaseInput) {
        for (String fragment : MATCH_FRAGMENTS) {
            if (lowerCaseInput.contains(fragment.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("null")
    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        String input;
        try {
            input = event.getParseResults().getReader().getString();
        } catch (Throwable t) {
            return;
        }
        if (input == null) {
            return;
        }

        String lower = input.toLowerCase(Locale.ROOT);
        if (!isLootdropAnnouncementCommand(lower)) {
            return;
        }

        CommandSourceStack source = event.getParseResults().getContext().getSource();
        ServerLevel level;
        try {
            level = source.getLevel();
        } catch (Throwable t) {
            return;
        }
        if (level == null) {
            return;
        }

        boolean enabled;
        
        try {
            enabled = level.getGameRules().getBoolean(net.mcreator.infectious.init.InfectiousModGameRules.ENABLE_LOOTDROPS);
        } catch (Throwable t) {
            return;
        }

        if (!enabled) {
            event.setCanceled(true);
        }
    }
}