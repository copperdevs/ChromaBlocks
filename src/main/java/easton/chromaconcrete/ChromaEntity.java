package easton.chromaconcrete;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.impl.client.rendering.WorldRenderContextImpl;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class ChromaEntity extends BlockEntity implements RenderAttachmentBlockEntity {

    private int hue = 16777215;

    public ChromaEntity(BlockPos pos, BlockState state) {
        super(ChromaConcrete.CHROMA_ENTITY, pos, state);

        //if (world != null && !world.isClient())
        //    ((ServerWorld)world).save(null, false, false);
        //not here, maybe when first placed ie. onPlaced in ChromaBlock

    }

    public int getHue() {
        return this.hue;
    }

    /*
    @Override
    @Nullable
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return new BlockEntityUpdateS2CPacket(this.pos, 13, this.toInitialChunkDataTag());
    }

    @Override
    public CompoundTag toInitialChunkDataTag() {
        return this.toTag(new CompoundTag());
    }
    */

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putInt("color", hue);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        setHue(nbt.getInt("color"));

        if (this.getWorld() != null && this.getWorld().isClient()) {
            MinecraftClient.getInstance().worldRenderer.updateBlock(this.getWorld(), pos, null, null, 0);
        }
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return this.getHue();
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.hasWorld() && !this.getWorld().isClient()) {
            ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
        }
    }
/*
    @Override
    public void fromClientTag(NbtCompound compoundTag) {
        readNbt(compoundTag);
        //hue = compoundTag.getInt("color");
    }

    @Override
    public NbtCompound toClientTag(NbtCompound compoundTag) {
        return writeNbt(compoundTag);
    }
*/
    public void setHue(int hue) {
        this.hue = hue;
        this.markDirty();
    }
}