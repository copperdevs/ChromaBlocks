package easton.chromaconcrete;

import easton.chromaconcrete.shulker.ChromaShulkerRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import static easton.chromaconcrete.ChromaConcrete.*;

public class ClientConcrete implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> 0x3495eb, CHROMA_BLOCK);
        //ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> ((ChromaEntity)((RenderAttachmentBlockEntity)view.getBlockEntity(pos)).getRenderAttachmentData()).getHue(), CHROMA_BLOCK);
        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> getBlockEntityColor(view, pos), CHROMA_BLOCK);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> stack.getOrCreateSubNbt("display").contains("color") ? stack.getOrCreateSubNbt("display").getInt("color") : 16777215, CHROMA_ITEM);

        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> getBlockEntityColor(view, pos), CHROMA_WOOL);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> stack.getOrCreateSubNbt("display").contains("color") ? stack.getOrCreateSubNbt("display").getInt("color") : 16777215, CHROMA_WOOL_ITEM);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> 12777215, CHROMA_SHULKER_ITEM);

        BlockEntityRendererRegistry.register(SHULKER_BOX_ENTITY, ChromaShulkerRenderer::new);
        //EntityModelLayerRegistry.registerModelLayer(EntityModelLayers.SHULKER, );
        //BlockEntityRendererRegistry.INSTANCE.register(SHULKER_BOX_ENTITY, ChromaShulkerRenderer::new);
        //BlockEntityRendererRegistry.INSTANCE.register(BED_ENTITY, ChromaBedRenderer::new);
    }

    private int getBlockEntityColor(BlockView view, BlockPos pos) {
        BlockEntity entity = view.getBlockEntity(pos);
        if (entity != null) {
           // return ((ChromaEntity)((ChromaEntity)entity).getRenderAttachmentData()).getHue();
            return (int) ((ChromaEntity)entity).getRenderAttachmentData();
        } else {
            return 16777215;
        }
    }
}