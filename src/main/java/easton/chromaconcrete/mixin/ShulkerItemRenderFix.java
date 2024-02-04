package easton.chromaconcrete.mixin;

import easton.chromaconcrete.*;
import easton.chromaconcrete.shulker.ChromaShulkerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BuiltinModelItemRenderer.class)
public class ShulkerItemRenderFix {
    /*
    @Redirect(method = "render", at = @At(value = "CONSTANT", args = "classValue=net/minecraft/block/ShulkerBoxBlock", ordinal = 4))
    public boolean mixin(Object block, Class<?> classValue) {
        return (block instanceof ShulkerBoxBlock || block instanceof ChromaShulkerBlock);
    }
    */

    @Inject(method = "render", at = @At("RETURN"))
    public void chromaShulkerInject(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        //BlockEntityRenderDispatcher.INSTANCE.renderEntity(new ChromaShulkerEntity(), matrices, vertexConsumers, light, stack.getOrCreateSubTag("display").contains("color") ? stack.getOrCreateSubTag("display").getInt("color") : 16777215);
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            if (block.equals(ChromaConcrete.CHROMA_SHULKER_BLOCK)) {
                MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(
                        new ChromaShulkerEntity(stack.getOrCreateSubNbt("display").contains("color")
                                ? stack.getOrCreateSubNbt("display").getInt("color")
                                : 16777215,
                                new BlockPos(0,0,0), null),
                        matrices, vertexConsumers, light, overlay);
            }
        }
    }

}