package easton.chromaconcrete;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ChromaBlock extends BlockWithEntity {

    public ChromaBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChromaEntity(pos, state);
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ChromaEntity) {
            int hue = itemStack.getOrCreateSubNbt("display").getInt("color");
            if (hue != 0) {
                ((ChromaEntity) blockEntity).setHue(itemStack.getOrCreateSubNbt("display").getInt("color"));
            } else {
                ((ChromaEntity) blockEntity).setHue(16777215);
            }
        }
    }
}
