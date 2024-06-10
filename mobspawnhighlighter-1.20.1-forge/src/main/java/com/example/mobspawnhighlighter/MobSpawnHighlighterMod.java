package com.example.mobspawnhighlighter;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MobSpawnHighlighterMod.MODID)
public class MobSpawnHighlighterMod {
    public static final String MODID = "mobspawnhighlighter";

    public MobSpawnHighlighterMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(new BlockHighlighter());
        KeyBindings.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Register any setup code here
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Register any client-specific setup code here
    }
}
