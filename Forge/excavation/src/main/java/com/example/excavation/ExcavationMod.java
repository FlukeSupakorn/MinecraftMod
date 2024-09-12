package com.example.excavation;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;

import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExcavationMod.MODID)
public class ExcavationMod {
    public static final String MODID = "excavation";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ExcavationMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        KeyBindings.register();
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!KeyBindings.oreMiningEnabled) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Set<BlockPos> visited = new HashSet<>();

        ItemStack tool = event.getPlayer().getMainHandItem();

        if (OreUtils.isOre(state)) {
            mineAdjacentBlocks(level, pos, state, visited, 0, Config.oreMiningRange, tool, event);
        }
    }

    private void mineAdjacentBlocks(ServerLevel level, BlockPos pos, BlockState state, Set<BlockPos> visited, int depth, int range, ItemStack tool, BlockEvent.BreakEvent event) {
        if (depth > range) return;

        visited.add(pos);

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (!visited.contains(adjacentPos) && adjacentState.getBlock() == state.getBlock()) {
                level.destroyBlock(adjacentPos, true);

                tool.hurtAndBreak(1, event.getPlayer(), (player) -> {
                    player.broadcastBreakEvent(InteractionHand.MAIN_HAND);
                });

                mineAdjacentBlocks(level, adjacentPos, state, visited, depth + 1, range, tool, event);
            }
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("setOreMiningRange")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("range", IntegerArgumentType.integer(1, 256))
                    .executes(context -> {
                        int range = IntegerArgumentType.getInteger(context, "range");
                        Config.oreMiningRange = range;
                        context.getSource().sendSuccess((Supplier<Component>) () -> Component.literal("Ore mining range set to " + range), true);
                        return 1;
                    }))
        );
    }
}
