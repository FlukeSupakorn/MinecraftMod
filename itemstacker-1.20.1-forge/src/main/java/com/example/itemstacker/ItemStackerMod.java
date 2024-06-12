package com.example.itemstacker;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ItemStackerMod.MODID)
public class ItemStackerMod {
    public static final String MODID = "itemstacker";

    public ItemStackerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(new ItemStackHandler());
        KeyBindings.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Register any setup code here
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Register any client-specific setup code here
    }
}
