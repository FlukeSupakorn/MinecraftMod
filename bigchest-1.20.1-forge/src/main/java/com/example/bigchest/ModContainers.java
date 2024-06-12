package com.example.bigchest;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModContainers {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BigChestMod.MODID);

    public static final RegistryObject<MenuType<BigChestContainer>> BIG_CHEST = CONTAINERS.register("big_chest", () -> IForgeMenuType.create(BigChestContainer::new));
}
