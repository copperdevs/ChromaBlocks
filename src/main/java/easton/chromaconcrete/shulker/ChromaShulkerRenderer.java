package easton.chromaconcrete.shulker;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

import static net.minecraft.client.render.TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE;
/*
@Environment(EnvType.CLIENT)
public class ChromaShulkerRenderer extends BlockEntityRenderer<ChromaShulkerEntity> {
    private final ShulkerEntityModel<?> model = new ShulkerEntityModel<>();

    public ChromaShulkerRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
        //this.model = shulkerEntityModel;
    }

    @Override
    public void render(ChromaShulkerEntity shulkerBoxBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        Direction direction = Direction.UP;
        if (shulkerBoxBlockEntity.hasWorld()) {
            BlockState blockState = shulkerBoxBlockEntity.getWorld().getBlockState(shulkerBoxBlockEntity.getPos());
            if (blockState.getBlock() instanceof ChromaShulkerBlock) {
                direction = (Direction)blockState.get(ChromaShulkerBlock.FACING);
            }
        }

        int hue = shulkerBoxBlockEntity.getColor();

        if (hue >= 0) {
            SpriteIdentifier spriteIdentifier2;
            spriteIdentifier2 = new SpriteIdentifier(SHULKER_BOXES_ATLAS_TEXTURE, new Identifier("entity/shulker/shulker_white"));

            matrixStack.push();
            matrixStack.translate(0.5D, 0.5D, 0.5D);
            float g = 0.9995F;
            matrixStack.scale(0.9995F, 0.9995F, 0.9995F);
            matrixStack.multiply(direction.getRotationQuaternion());
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            matrixStack.translate(0.0D, -1.0D, 0.0D);
            VertexConsumer vertexConsumer = spriteIdentifier2.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityCutoutNoCull);

            Color color = Color.decode(hue + "");

            //this.model.getBottomShell().render(matrixStack, vertexConsumer, i, j);
            //this.model.getBottomShell().render(matrixStack, vertexConsumer, i, j, 0.5f, 0.1f, 0.1f, 0.5f);
            this.model.getBottomShell().render(matrixStack, vertexConsumer, i, j, color.getRed()/255.0f, color.getGreen()/255.0f, color.getBlue()/255.0f, color.getAlpha()/255.0f);
            matrixStack.translate(0.0D, (double)(-shulkerBoxBlockEntity.getAnimationProgress(f) * 0.5F), 0.0D);
            matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270.0F * shulkerBoxBlockEntity.getAnimationProgress(f)));
            //this.model.getTopShell().render(matrixStack, vertexConsumer, i, j);
            //this.model.getTopShell().render(matrixStack, vertexConsumer, i, j, 0.5f, 0.1f, 0.1f, 0.5f);
            this.model.getTopShell().render(matrixStack, vertexConsumer, i, j, color.getRed()/255.0f, color.getGreen()/255.0f, color.getBlue()/255.0f, color.getAlpha()/255.0f);
            matrixStack.pop();
        }
    }
}
*/

@Environment(EnvType.CLIENT)
public class ChromaShulkerRenderer implements BlockEntityRenderer<ChromaShulkerEntity> {
    private final ShulkerEntityModel<?> model;

    public ChromaShulkerRenderer(BlockEntityRendererFactory.Context ctx) {
        this.model = new ShulkerEntityModel(ctx.getLayerModelPart(EntityModelLayers.SHULKER));
    }

    public void render(ChromaShulkerEntity shulkerBoxBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
        Direction direction = Direction.UP;
        if (shulkerBoxBlockEntity.hasWorld()) {
            BlockState blockState = shulkerBoxBlockEntity.getWorld().getBlockState(shulkerBoxBlockEntity.getPos());
            if (blockState.getBlock() instanceof ShulkerBoxBlock) {
                direction = blockState.get(ShulkerBoxBlock.FACING);
            }
        }

        int hue = (int) shulkerBoxBlockEntity.getRenderAttachmentData();

        if (hue >= 0) {
            SpriteIdentifier spriteIdentifier2 = new SpriteIdentifier(SHULKER_BOXES_ATLAS_TEXTURE, new Identifier("entity/shulker/shulker_white"));
            Color color = Color.decode(hue + "");

            matrixStack.push();
            matrixStack.translate(0.5D, 0.5D, 0.5D);
            float g = 0.9995F;
            matrixStack.scale(0.9995F, 0.9995F, 0.9995F);
            matrixStack.multiply(direction.getRotationQuaternion());
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            matrixStack.translate(0.0D, -1.0D, 0.0D);

            ModelPart modelPart = this.model.getLid();
            modelPart.setPivot(0.0F, 24.0F - shulkerBoxBlockEntity.getAnimationProgress(f) * 0.5F * 16.0F, 0.0F);
            modelPart.yaw = 270.0F * shulkerBoxBlockEntity.getAnimationProgress(f) * 0.017453292F;
            VertexConsumer vertexConsumer = spriteIdentifier2.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityCutoutNoCull);
            this.model.render(matrixStack, vertexConsumer, i, j, color.getRed()/255.0f, color.getGreen()/255.0f, color.getBlue()/255.0f, color.getAlpha()/255.0f);
            matrixStack.pop();
        }
    }
}