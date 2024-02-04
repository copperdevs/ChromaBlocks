package easton.chromaconcrete.shulker;

import easton.chromaconcrete.ChromaConcrete;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class ChromaShulkerEntity extends ShulkerBoxBlockEntity implements RenderAttachmentBlockEntity {
    @Nullable
    private int cachedColor;

    public ChromaShulkerEntity(int hue, BlockPos blockPos, BlockState blockState) {
        super(DyeColor.WHITE, blockPos, blockState);
        this.cachedColor = hue;
        this.type = ChromaConcrete.SHULKER_BOX_ENTITY;
    }

    public ChromaShulkerEntity(BlockPos blockPos, BlockState blockState) {
        this(16777215, blockPos, blockState);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        setHue(nbt.getInt("color"));

        if (this.getWorld() != null && this.getWorld().isClient()) {
            MinecraftClient.getInstance().worldRenderer.updateBlock(this.getWorld(), pos, null, null, 0);
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putInt("color", cachedColor);
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.hasWorld() && !this.getWorld().isClient()) {
            ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
        }
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return this.cachedColor;
    }

    public void setHue(int hue) {
        this.cachedColor = hue;
        this.markDirty();
    }

}
