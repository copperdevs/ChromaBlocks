package easton.chromaconcrete;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class ChromaEntity extends BlockEntity implements RenderAttachmentBlockEntity {

    private int hue = 16777215;

    public ChromaEntity(BlockPos pos, BlockState state) {
        super(ChromaConcrete.CHROMA_ENTITY, pos, state);
    }

    public int getHue() {
        return this.hue;
    }

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

    public void setHue(int hue) {
        this.hue = hue;
        this.markDirty();
    }
}