package github.flukesupakorn;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Style;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.RotationAxis;

public class CustomItemEntityRenderer extends ItemEntityRenderer {
    public CustomItemEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(ItemEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        ItemStack stack = entity.getStack();
        if (!stack.isEmpty()) {
            // Create colored text components
            MutableText itemName = Text.literal(stack.getName().getString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY));
            MutableText separator = Text.literal(" x ").setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
            MutableText count = Text.literal(String.valueOf(stack.getCount())).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
            
            // Combine the text components
            MutableText displayText = itemName.append(separator).append(count);

            double yOffset = 1.0; // Higher above the entity
            Vec3d pos = entity.getPos();

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            float scale = 0.025f;

            matrices.push();
            matrices.translate(0, yOffset, 0);
            
            // Make text face the player
            matrices.multiply(this.dispatcher.getRotation());
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            
            matrices.scale(-scale, -scale, scale);

            int textWidth = textRenderer.getWidth(displayText);
            textRenderer.draw(displayText, -textWidth / 2f, 0, 0xFFFFFF, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            matrices.pop();
        }
    }
}