package github.flukesupakorn;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;

public class BetterlifeModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register custom item entity renderer
		EntityRendererRegistry.register(EntityType.ITEM, (context) -> 
			new CustomItemEntityRenderer(context));

		// Add HUD to verify mod is running
		HudRenderCallback.EVENT.register((drawContext, delta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player != null) {
				int width = client.getWindow().getScaledWidth();
				int height = client.getWindow().getScaledHeight();
				
				// Draw text in bottom right corner
				String text = "BetterLife Mod Active";
				drawContext.drawText(
					client.textRenderer,
					Text.literal(text),
					width - client.textRenderer.getWidth(text) - 5,
					height - 15,
					0xFFFFFF,
					true
				);
			}
		});
	}
}