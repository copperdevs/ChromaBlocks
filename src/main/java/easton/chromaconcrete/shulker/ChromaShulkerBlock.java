package easton.chromaconcrete.shulker;

import easton.chromaconcrete.ChromaConcrete;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ChromaShulkerBlock extends ShulkerBoxBlock {

    public ChromaShulkerBlock(Settings settings) {
        super(DyeColor.WHITE, settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChromaShulkerEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ChromaConcrete.SHULKER_BOX_ENTITY, ChromaShulkerEntity::tick);
    }

    // only works if you *don't* hold control!?
    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack itemStack = new ItemStack(this);
        world.getBlockEntity(pos, ChromaConcrete.SHULKER_BOX_ENTITY).ifPresent(blockEntity -> {
            blockEntity.setStackNbt(itemStack);
            NbtCompound colorNbt = new NbtCompound();
            blockEntity.writeNbt(colorNbt);
            if (colorNbt.contains("color")) {
                itemStack.getOrCreateSubNbt("display").putInt("color", colorNbt.getInt("color"));
            }
        });
        return itemStack;
    }

}
