package github.flukesupakorn.mixin;

import github.flukesupakorn.itemstacking.ItemNameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        ItemNameRenderer.renderItemName(itemEntity, matrixStack, vertexConsumerProvider, i);
    }
} 