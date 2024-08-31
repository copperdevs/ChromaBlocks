package easton.chromaconcrete;

import easton.chromaconcrete.shulker.ChromaShulkerBlock;
import easton.chromaconcrete.shulker.ChromaShulkerEntity;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.BlockPlacementDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ChromaConcrete implements ModInitializer {

    public static final String MOD_ID = "chromaconcrete";

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }

    public static BlockEntityType<ChromaEntity> CHROMA_ENTITY;
    public static BlockEntityType<ChromaShulkerEntity> SHULKER_BOX_ENTITY;

    public static final ChromaBlock CHROMA_BLOCK = new ChromaBlock(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.8F).requiresTool());
    public static final ChromaItem CHROMA_ITEM = new ChromaItem(CHROMA_BLOCK, new Item.Settings());

    public static final ChromaBlock CHROMA_WOOL = new ChromaBlock(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.GUITAR).strength(0.8F).sounds(BlockSoundGroup.WOOL).burnable());
    public static final ChromaItem CHROMA_WOOL_ITEM = new ChromaItem(CHROMA_WOOL, new Item.Settings());

    public static final Block CHROMA_SHULKER_BLOCK = createChromaShulker(AbstractBlock.Settings.create().hardness(2.0f));
    public static final ChromaItem CHROMA_SHULKER_ITEM = new ChromaItem(CHROMA_SHULKER_BLOCK, new Item.Settings().maxCount(1));

    @Override
    public void onInitialize() {

        Registry.register(Registries.BLOCK, id("chroma"), CHROMA_BLOCK);
        Registry.register(Registries.ITEM, id("chroma"), CHROMA_ITEM);
        CHROMA_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, "chromaconcrete:chroma", BlockEntityType.Builder.create(ChromaEntity::new, CHROMA_BLOCK, CHROMA_WOOL).build(null));

        Registry.register(Registries.BLOCK, id("chroma_wool"), CHROMA_WOOL);
        Registry.register(Registries.ITEM, id("chroma_wool"), CHROMA_WOOL_ITEM);

        Registry.register(Registries.BLOCK, id("chroma_shulker_box"), CHROMA_SHULKER_BLOCK);
        Registry.register(Registries.ITEM, id("chroma_shulker_box"), CHROMA_SHULKER_ITEM);

        SHULKER_BOX_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("chroma_shulker_box"), BlockEntityType.Builder.create(ChromaShulkerEntity::new, CHROMA_SHULKER_BLOCK).build(null));

        DispenserBlock.registerBehavior(CHROMA_SHULKER_ITEM, new BlockPlacementDispenserBehavior());
    }

    private static ChromaShulkerBlock createChromaShulker(AbstractBlock.Settings settings) {
        AbstractBlock.ContextPredicate contextPredicate = (blockState, blockView, blockPos) -> {
            BlockEntity blockEntity = blockView.getBlockEntity(blockPos);
            if (!(blockEntity instanceof ChromaShulkerEntity shulkerBoxBlockEntity)) {
                return true;
            } else {
                return shulkerBoxBlockEntity.suffocates();
            }
        };
        return new ChromaShulkerBlock(settings.pistonBehavior(PistonBehavior.DESTROY).strength(2.0F).dynamicBounds().nonOpaque().suffocates(contextPredicate).blockVision(contextPredicate));
    }
}
