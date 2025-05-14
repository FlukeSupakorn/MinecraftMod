package github.flukesupakorn.itemstacking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

public class ItemNameRenderer {
    public static void renderItemName(ItemEntity itemEntity, MatrixStack matrices, 
            VertexConsumerProvider vertexConsumers, int light) {
        if (!itemEntity.isOnGround()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        
        String displayText = itemEntity.getStack().getName().getString() + 
                           " x " + itemEntity.getStack().getCount();
        
        matrices.push();
        matrices.translate(0.0D, itemEntity.getHeight() + 0.5D, 0.0D);
        matrices.multiply(client.gameRenderer.getCamera().getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float x = -textRenderer.getWidth(displayText) / 2.0F;
        
        textRenderer.draw(Text.literal(displayText), x, 0, 0xFFFFFF, false, 
            matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        
        matrices.pop();
    }
} 