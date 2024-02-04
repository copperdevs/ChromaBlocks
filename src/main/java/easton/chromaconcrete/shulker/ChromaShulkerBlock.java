package easton.chromaconcrete.shulker;

import easton.chromaconcrete.ChromaConcrete;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class ChromaShulkerBlock extends BlockWithEntity {
    public static final EnumProperty<Direction> FACING;
    public static final Identifier CONTENTS;

    private final int color;

    public ChromaShulkerBlock(int color, Settings settings) {
        super(settings);
        this.color = color;
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChromaShulkerEntity(pos, state);
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ChromaConcrete.SHULKER_BOX_ENTITY, ChromaShulkerEntity::tick);
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return ActionResult.CONSUME;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ChromaShulkerEntity) {
                ChromaShulkerEntity shulkerBoxBlockEntity = (ChromaShulkerEntity)blockEntity;
                if (canOpen(state, world, pos, shulkerBoxBlockEntity)) {
                    player.openHandledScreen(shulkerBoxBlockEntity);
                    player.incrementStat(Stats.OPEN_SHULKER_BOX);
                    PiglinBrain.onGuardedBlockInteracted(player, true);
                }

                return ActionResult.CONSUME;
            } else {
                return ActionResult.PASS;
            }
        }
    }

    private static boolean canOpen(BlockState state, World world, BlockPos pos, ChromaShulkerEntity entity) {
        if (entity.getAnimationStage() != ChromaShulkerEntity.AnimationStage.CLOSED) {
            return true;
        } else {
            Box box = ShulkerEntity.calculateBoundingBox(state.get(FACING), 0.0F, 0.5F).offset(pos).contract(1.0E-6D);
            return world.isSpaceEmpty(box);
        }
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getSide());
    }

    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ChromaShulkerEntity) {
            ChromaShulkerEntity shulkerBoxBlockEntity = (ChromaShulkerEntity)blockEntity;
            if (!world.isClient && player.isCreative() && !shulkerBoxBlockEntity.isEmpty()) {
                //ItemStack itemStack = getItemStack(this.getColor());
                ItemStack itemStack = new ItemStack(ChromaConcrete.CHROMA_ITEM);
                NbtCompound compoundTag = shulkerBoxBlockEntity.serializeInventory(new NbtCompound());
                shulkerBoxBlockEntity.setStackNbt(itemStack);
                //if (!compoundTag.isEmpty()) {
                //    itemStack.putSubTag("BlockEntityTag", compoundTag);
                //}

                if (shulkerBoxBlockEntity.hasCustomName()) {
                    itemStack.setCustomName(shulkerBoxBlockEntity.getCustomName());
                }

                ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            } else {
                shulkerBoxBlockEntity.checkLootInteraction(player);
            }
        }

        super.onBreak(world, pos, state, player);
    }

    public List<ItemStack> getDroppedStacks(BlockState state, net.minecraft.loot.context.LootContextParameterSet.Builder builder) {
        BlockEntity blockEntity = (BlockEntity)builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof ChromaShulkerEntity) {
            ChromaShulkerEntity shulkerBoxBlockEntity = (ChromaShulkerEntity) blockEntity;
            builder = builder.addDynamicDrop(CONTENTS, (consumer) -> {
                for(int i = 0; i < shulkerBoxBlockEntity.size(); ++i) {
                    consumer.accept(shulkerBoxBlockEntity.getStack(i));
                }

            });
        }

        return super.getDroppedStacks(state, builder);
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomName()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ChromaShulkerEntity) {
                ((ChromaShulkerEntity)blockEntity).setCustomName(itemStack.getName());
            }
        }

    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ChromaShulkerEntity) {
                world.updateComparators(pos, state.getBlock());
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        NbtCompound compoundTag = BlockItem.getBlockEntityNbt(stack);
        if (compoundTag != null) {
            if (compoundTag.contains("LootTable", 8)) {
                tooltip.add(Text.literal("???????"));
            }

            if (compoundTag.contains("Items", 9)) {
                DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
                Inventories.readNbt(compoundTag, defaultedList);
                int i = 0;
                int j = 0;
                Iterator var9 = defaultedList.iterator();

                while(var9.hasNext()) {
                    ItemStack itemStack = (ItemStack)var9.next();
                    if (!itemStack.isEmpty()) {
                        ++j;
                        if (i <= 4) {
                            ++i;
                            MutableText mutableText = itemStack.getName().copy();
                            mutableText.append(" x").append(String.valueOf(itemStack.getCount()));
                            tooltip.add(mutableText);
                        }
                    }
                }

                if (j - i > 0) {
                    tooltip.add(Text.translatable("container.shulkerBox.more", new Object[]{j - i}).formatted(Formatting.ITALIC));
                }
            }
        }

    }

    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof ChromaShulkerEntity ? VoxelShapes.cuboid(((ChromaShulkerEntity)blockEntity).getBoundingBox(state)) : VoxelShapes.fullCube();
        //return VoxelShapes.fullCube();
    }

    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput((Inventory)world.getBlockEntity(pos));
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack itemStack = super.getPickStack(world, pos, state);
        ChromaShulkerEntity shulkerBoxBlockEntity = (ChromaShulkerEntity) world.getBlockEntity(pos);
        NbtCompound compoundTag = shulkerBoxBlockEntity.serializeInventory(new NbtCompound());
        shulkerBoxBlockEntity.setStackNbt(itemStack);
        //if (!compoundTag.isEmpty()) {
        //    itemStack.putSubTag("BlockEntityTag", compoundTag);
        //}

        return itemStack;
    }

    @Nullable
    @Environment(EnvType.CLIENT)
    public static int getColor(Item item) {
        return getColor(Block.getBlockFromItem(item));
    }

    @Nullable
    @Environment(EnvType.CLIENT)
    public static int getColor(Block block) {
        return block instanceof ChromaShulkerBlock ? ((ChromaShulkerBlock)block).getColor() : 16777215;
    }

    public static Block get(@Nullable DyeColor dyeColor) {
        return ChromaConcrete.CHROMA_SHULKER_BLOCK;
    }

    @Nullable
    public int getColor() {
        return this.color;
    }

    public static ItemStack getItemStack(@Nullable DyeColor color) {
        return new ItemStack(get(color));
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
    }

    static {
        FACING = FacingBlock.FACING;
        CONTENTS = new Identifier("contents");
    }
}
