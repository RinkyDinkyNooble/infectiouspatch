package com.rinkynooble.infectiouspatch;

import net.minecraftforge.fml.common.Mod;

@Mod(InfectiousPatch.MODID)
public class InfectiousPatch {

    public static final String MODID = "infectiouspatch";

    public InfectiousPatch() {
        new LootdropFix();
        new EntityDropSuppressor();
    }
}
