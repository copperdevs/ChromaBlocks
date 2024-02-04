package easton.chromaconcrete.shulker;

import easton.chromaconcrete.ChromaConcrete;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public class ChromaShulkerEntity extends LootableContainerBlockEntity implements RenderAttachmentBlockEntity, SidedInventory {
    private static final int[] AVAILABLE_SLOTS = IntStream.range(0, 27).toArray();
    private DefaultedList<ItemStack> inventory;
    private int viewerCount;
    private ChromaShulkerEntity.AnimationStage animationStage;
    private float animationProgress;
    private float prevAnimationProgress;
    @Nullable
    private int cachedColor;
    private boolean cachedColorUpdateNeeded;

    public ChromaShulkerEntity(int hue, BlockPos blockPos, BlockState blockState) {
        super(ChromaConcrete.SHULKER_BOX_ENTITY, blockPos, blockState);
        this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
        this.animationStage = ChromaShulkerEntity.AnimationStage.CLOSED;
        this.cachedColor = hue;
    }

    public ChromaShulkerEntity(BlockPos blockPos, BlockState blockState) {
        this(16777215, blockPos, blockState);
        this.cachedColorUpdateNeeded = true;
    }

    public static void tick(World world, BlockPos pos, BlockState state, ChromaShulkerEntity blockEntity) {
        blockEntity.updateAnimation();
        if (blockEntity.animationStage == ChromaShulkerEntity.AnimationStage.OPENING || blockEntity.animationStage == ChromaShulkerEntity.AnimationStage.CLOSING) {
            blockEntity.pushEntities();
        }
    }

    public void tick() {
        this.updateAnimation();
        if (this.animationStage == ChromaShulkerEntity.AnimationStage.OPENING || this.animationStage == ChromaShulkerEntity.AnimationStage.CLOSING) {
            this.pushEntities();
        }

    }



    protected void updateAnimation() {
        this.prevAnimationProgress = this.animationProgress;
        switch(this.animationStage) {
            case CLOSED:
                this.animationProgress = 0.0F;
                break;
            case OPENING:
                this.animationProgress += 0.1F;
                if (this.animationProgress >= 1.0F) {
                    this.pushEntities();
                    this.animationStage = ChromaShulkerEntity.AnimationStage.OPENED;
                    this.animationProgress = 1.0F;
                    this.updateNeighborStates();
                }
                break;
            case CLOSING:
                this.animationProgress -= 0.1F;
                if (this.animationProgress <= 0.0F) {
                    this.animationStage = ChromaShulkerEntity.AnimationStage.CLOSED;
                    this.animationProgress = 0.0F;
                    this.updateNeighborStates();
                }
                break;
            case OPENED:
                this.animationProgress = 1.0F;
        }

    }

    public ChromaShulkerEntity.AnimationStage getAnimationStage() {
        return this.animationStage;
    }

    public Box getBoundingBox(BlockState state) {
        return this.getBoundingBox((Direction)state.get(ChromaShulkerBlock.FACING));
    }

    public Box getBoundingBox(Direction openDirection) {
        float f = this.getAnimationProgress(1.0F);
        return VoxelShapes.fullCube().getBoundingBox().stretch((double)(0.5F * f * (float)openDirection.getOffsetX()), (double)(0.5F * f * (float)openDirection.getOffsetY()), (double)(0.5F * f * (float)openDirection.getOffsetZ()));
    }

    private Box getCollisionBox(Direction facing) {
        Direction direction = facing.getOpposite();
        return this.getBoundingBox(facing).shrink((double)direction.getOffsetX(), (double)direction.getOffsetY(), (double)direction.getOffsetZ());
    }

    private void pushEntities() {
        BlockState blockState = this.world.getBlockState(this.getPos());
        if (blockState.getBlock() instanceof ChromaShulkerBlock) {
            Direction direction = (Direction)blockState.get(ChromaShulkerBlock.FACING);
            Box box = this.getCollisionBox(direction).offset(this.pos);
            List<Entity> list = this.world.getOtherEntities((Entity)null, box);
            if (!list.isEmpty()) {
                for(int i = 0; i < list.size(); ++i) {
                    Entity entity = (Entity)list.get(i);
                    if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                        double d = 0.0D;
                        double e = 0.0D;
                        double f = 0.0D;
                        Box box2 = entity.getBoundingBox();
                        switch(direction.getAxis()) {
                            case X:
                                if (direction.getDirection() == AxisDirection.POSITIVE) {
                                    d = box.maxX - box2.minX;
                                } else {
                                    d = box2.maxX - box.minX;
                                }

                                d += 0.01D;
                                break;
                            case Y:
                                if (direction.getDirection() == AxisDirection.POSITIVE) {
                                    e = box.maxY - box2.minY;
                                } else {
                                    e = box2.maxY - box.minY;
                                }

                                e += 0.01D;
                                break;
                            case Z:
                                if (direction.getDirection() == AxisDirection.POSITIVE) {
                                    f = box.maxZ - box2.minZ;
                                } else {
                                    f = box2.maxZ - box.minZ;
                                }

                                f += 0.01D;
                        }

                        entity.move(MovementType.SHULKER_BOX, new Vec3d(d * (double)direction.getOffsetX(), e * (double)direction.getOffsetY(), f * (double)direction.getOffsetZ()));
                    }
                }

            }
        }
    }

    public int size() {
        return this.inventory.size();
    }

    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.viewerCount = data;
            if (data == 0) {
                this.animationStage = ChromaShulkerEntity.AnimationStage.CLOSING;
                this.updateNeighborStates();
            }

            if (data == 1) {
                this.animationStage = ChromaShulkerEntity.AnimationStage.OPENING;
                this.updateNeighborStates();
            }

            return true;
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    private void updateNeighborStates() {
        this.getCachedState().updateNeighbors(this.getWorld(), this.getPos(), 3);
    }

    public void onOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.viewerCount < 0) {
                this.viewerCount = 0;
            }

            ++this.viewerCount;
            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.viewerCount);
            if (this.viewerCount == 1) {
                this.world.playSound((PlayerEntity)null, this.pos, SoundEvents.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
            }
        }

    }

    public void onClose(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.viewerCount;
            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.viewerCount);
            if (this.viewerCount <= 0) {
                this.world.playSound((PlayerEntity)null, this.pos, SoundEvents.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
            }
        }

    }

    protected Text getContainerName() {
        return Text.translatable("container.shulkerBox");
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.deserializeInventory(nbt);

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
        nbt = this.serializeInventory(nbt);

        nbt.putInt("color", cachedColor);
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
    @Override
    public @Nullable Object getRenderAttachmentData() {
        //this.markDirty();
        return this.getColor();
    }

    public void deserializeInventory(NbtCompound tag) {
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(tag) && tag.contains("Items", 9)) {
            Inventories.readNbt(tag, this.inventory);
        }

    }

    public NbtCompound serializeInventory(NbtCompound tag) {
        if (!this.serializeLootTable(tag)) {
            Inventories.writeNbt(tag, this.inventory, false);
        }

        return tag;
    }

    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    public int[] getAvailableSlots(Direction side) {
        return AVAILABLE_SLOTS;
    }

    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        System.out.println(stack.getItem().getRegistryEntry().registryKey().getValue());
        return !(Block.getBlockFromItem(stack.getItem()) instanceof ChromaShulkerBlock || Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock);
    }

    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    public float getAnimationProgress(float f) {
        return MathHelper.lerp(f, this.prevAnimationProgress, this.animationProgress);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return super.isValid(slot, stack) && !(Block.getBlockFromItem(stack.getItem()) instanceof ChromaShulkerBlock) && !(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock);
    }

    public int getColor() {
//        if (this.cachedColorUpdateNeeded) {
//            this.cachedColor = ChromaShulkerBlock.getColor(this.getCachedState().getBlock());
//            return -1;
//        }

        return this.cachedColor;
    }

    public void setHue(int hue) {
        this.cachedColor = hue;
        this.cachedColorUpdateNeeded = false;
        this.markDirty();
    }

    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new ShulkerBoxScreenHandler(syncId, playerInventory, this);
    }

    public boolean suffocates() {
        return this.animationStage == ChromaShulkerEntity.AnimationStage.CLOSED;
    }

    public static enum AnimationStage {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING;

        private AnimationStage() {
        }
    }
}
