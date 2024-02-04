package easton.chromaconcrete;

import com.mojang.serialization.MapCodec;
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

    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChromaEntity(pos, state);
    }

    /*
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world != null && !world.isClient())
            ((ServerWorld)world).save(null, false, false);
    }
    */
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
    /*
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && player.isCreative() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ChromaEntity) {
                ChromaEntity chromaEntity = (ChromaEntity)blockEntity;
                ItemStack itemStack = new ItemStack(this);

                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.putInt("color", chromaEntity.getHue());
                itemStack.putSubTag("display", compoundTag2);

                ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }
        super.onBreak(world, pos, state, player);
    } */
}
