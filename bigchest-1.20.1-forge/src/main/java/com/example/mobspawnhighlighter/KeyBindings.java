package com.example.mobspawnhighlighter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = MobSpawnHighlighterMod.MODID, value = Dist.CLIENT)
public class KeyBindings {
    public static boolean highlightEnabled = false;
    private static int shiftPressCount = 0;
    private static long lastShiftPressTime = 0;
    public static final long SHIFT_PRESS_INTERVAL = 500; // milliseconds
    private static boolean wasShiftDown = false;
    private static boolean showStatusText = false;
    private static long statusTextDisplayTime = 0;
    public static final long STATUS_TEXT_DURATION = 2000; // milliseconds
    public static final long TOGGLE_COOLDOWN = 3000; // milliseconds
    private static long lastToggleTime = 0;

    public static void register() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(KeyBindings::onClientSetup);
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        // No key mappings to add
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player != null && (player.getMainHandItem().getItem() == Items.TORCH || player.getOffhandItem().getItem() == Items.TORCH)) {
                boolean isShiftDown = mc.options.keyShift.isDown();
                long currentTime = System.currentTimeMillis();

                if (isShiftDown && !wasShiftDown && currentTime - lastToggleTime > TOGGLE_COOLDOWN) {
                    if (currentTime - lastShiftPressTime > SHIFT_PRESS_INTERVAL) {
                        shiftPressCount = 0;
                    }
                    lastShiftPressTime = currentTime;
                    shiftPressCount++;
                    if (shiftPressCount >= 5) {
                        highlightEnabled = !highlightEnabled;
                        shiftPressCount = 0;
                        lastToggleTime = currentTime;
                        statusTextDisplayTime = currentTime;
                        showStatusText = true;
                        MobSpawnHighlighterHudRenderer.updateLastToggleTime();
                        playToggleSound(player, highlightEnabled);
                    }
                }
                wasShiftDown = isShiftDown;
            } else {
                shiftPressCount = 0;
                wasShiftDown = false;
            }
        }
    }

    private static void playToggleSound(LocalPlayer player, boolean enabled) {
        SoundEvent sound = enabled ? SoundEvents.NOTE_BLOCK_BELL.value() : SoundEvents.NOTE_BLOCK_BASS.value();
        player.level().playSound(player, player.blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static long getLastShiftPressTime() {
        return lastShiftPressTime;
    }

    public static int getShiftPressCount() {
        return shiftPressCount;
    }

    public static boolean shouldShowStatusText() {
        return showStatusText && System.currentTimeMillis() - statusTextDisplayTime < STATUS_TEXT_DURATION;
    }

    public static void resetShowStatusText() {
        showStatusText = false;
    }

    public static String getHighlightStatus() {
        return highlightEnabled ? "enabled" : "disabled";
    }

    public static int getHighlightColor() {
        return highlightEnabled ? 0x55FF55 : 0xFF5555;
    }

    public static String getModeStatusText() {
        return "MobSpawnHighlighter " + (highlightEnabled ? "enabled" : "disabled");
    }

    public static long getStatusTextDisplayTime() {
        return statusTextDisplayTime;
    }

    public static int getProgressColor() {
        return highlightEnabled ? 0xFF5555 : 0x55FF55; // Green for enabling, red for disabling
    }
}
