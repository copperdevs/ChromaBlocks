package easton.chromaconcrete.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static easton.chromaconcrete.ChromaConcrete.*;

@Mixin(BlockItem.class)
public class BlockItemInject {

    // a bit hacky, but necessary since the dying recipe stores the color tag in the display tag on the item,
    // and normally everything for block entities is stored under BlockEntityTag
    @Inject(cancellable = true, method = "writeNbtToBlockEntity", at = @At("HEAD"))
    private static void injected(World world, PlayerEntity player, BlockPos pos, ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        if (stack.getItem().equals(CHROMA_ITEM) || stack.getItem().equals(CHROMA_WOOL_ITEM) || stack.getItem().equals(CHROMA_SHULKER_ITEM)) {
            MinecraftServer minecraftServer = world.getServer();
            if (minecraftServer == null) {
                info.setReturnValue(false);
            } else {
                NbtCompound compoundTag = stack.getSubNbt("display");
                NbtCompound invTag = null;
                if (stack.getItem().equals(CHROMA_SHULKER_ITEM)) invTag = stack.getSubNbt("BlockEntityTag");

                if (compoundTag != null || invTag != null) {
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity != null) {
                        if (!world.isClient && blockEntity.copyItemDataRequiresOperator() && (player == null || !player.isCreativeLevelTwoOp())) {
                            info.setReturnValue(false);
                        }

                        NbtCompound compoundTag2 = blockEntity.createNbt();
                        NbtCompound compoundTag3 = compoundTag2.copy();
                        compoundTag2.copyFrom(compoundTag);
                        if (!compoundTag2.contains("color") || compoundTag2.getInt("color") == 0) {
                            compoundTag2.putInt("color", 16777215);
                        }
                        if (invTag != null) {
                            compoundTag2.copyFrom(invTag);
                        }

                        if (!compoundTag2.equals(compoundTag3)) {
                            blockEntity.readNbt(compoundTag2);
                            blockEntity.markDirty();
                            info.setReturnValue(true);
                        }
                    }
                }

                info.setReturnValue(false);
            }
        }
    }

}