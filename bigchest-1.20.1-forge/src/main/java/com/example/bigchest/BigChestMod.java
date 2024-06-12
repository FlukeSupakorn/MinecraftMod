package com.example.bigchest;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BigChestMod.MODID)
public class BigChestMod {
    public static final String MODID = "bigchest";

    public BigChestMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        ModContainers.CONTAINERS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(new BigChestEventHandler());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(ModContainers.BIG_CHEST.get(), BigChestScreen::new);
    }
}
