package dev.itsmeow.snailmail.block;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.SnailMail.Configuration;
import dev.itsmeow.snailmail.SnailMail.SnailBoxData;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModBlockEntities;
import dev.itsmeow.snailmail.init.ModBlocks;
import dev.itsmeow.snailmail.util.Location;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SnailMail.MODID)
public class SnailBoxBlock extends Block implements IWaterLoggable {

    private static VoxelShape SHAPE_X;
    private static VoxelShape SHAPE_Z;

    static {
        double d = 0.0625D * 4;
        SHAPE_X = VoxelShapes.create(d, 0, 0, 1 - d, 1, 1);
        SHAPE_Z = VoxelShapes.create(0, 0, d, 1, 1, 1 - d);
    }

    public SnailBoxBlock(String name) {
        super(Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(2.0F, 1200.0F).sound(SoundType.WOOD));
        this.setRegistryName(SnailMail.MODID, name);
        this.setDefaultState(this.getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH).with(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(BlockStateProperties.HORIZONTAL_FACING, rot.rotate(state.get(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return state.get(BlockStateProperties.HORIZONTAL_FACING).getAxis() == Axis.Z ? SHAPE_X : SHAPE_Z;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.WATERLOGGED);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IFluidState getFluidState(BlockState state) {
        return state.get(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public boolean isNormalCube(BlockState p_220081_1_, IBlockReader p_220081_2_, BlockPos p_220081_3_) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModBlockEntities.SNAIL_BOX.create();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(state.getBlock() != newState.getBlock()) {
            TileEntity teB = worldIn.getTileEntity(pos);
            if(teB instanceof SnailBoxBlockEntity) {
                SnailBoxBlockEntity te = (SnailBoxBlockEntity) teB;
                for(int i = 0; i < te.getItemHandler().getSlots(); ++i) {
                    InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), te.getItemHandler().getStackInSlot(i));
                }
            }
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(handIn == Hand.MAIN_HAND && !worldIn.isRemote() && worldIn.getTileEntity(pos) != null) {
            if(canOpen(worldIn, pos, player)) {
                ((SnailBoxBlockEntity) worldIn.getTileEntity(pos)).openGUI((ServerPlayerEntity) player);
                return ActionResultType.SUCCESS;
            } else {
                player.sendMessage(new TranslationTextComponent("message.snailmail.noperm").applyTextStyle(TextFormatting.RED));
                return ActionResultType.FAIL;
            }
        }
        return ActionResultType.PASS;
    }

    public static boolean canOpen(World worldIn, BlockPos pos, PlayerEntity player) {
        TileEntity teB = worldIn.getTileEntity(pos);
        return teB != null && teB instanceof SnailBoxBlockEntity && isAccessibleFor((SnailBoxBlockEntity) teB, PlayerEntity.getUUID(player.getGameProfile()));
    }

    public static boolean isAccessibleFor(SnailBoxBlockEntity te, UUID uuid) {
        return (!SnailMail.Configuration.get().LOCK_BOXES.get() || uuid.equals(te.getOwner()) || te.isMember(uuid));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if(placer instanceof PlayerEntity && !worldIn.isRemote()) {
            UUID uuid = PlayerEntity.getUUID(((PlayerEntity) placer).getGameProfile());
            TileEntity teB = worldIn.getTileEntity(pos);
            if(teB != null && teB instanceof SnailBoxBlockEntity) {
                Set<Location> box = SnailBoxData.getData(worldIn.getServer()).getBoxes(uuid);
                int size = box == null ? 0 : box.size();
                ((SnailBoxBlockEntity) teB).initializeOwner(uuid, ((PlayerEntity) placer).getGameProfile().getName() + " Snailbox #" + (size + 1), false);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if(event.getState().getBlock() == ModBlocks.SNAIL_BOX) {
            IWorld world = event.getWorld();
            BlockPos pos = event.getPos();
            UUID uuid = PlayerEntity.getUUID(event.getPlayer().getGameProfile());
            TileEntity teB = world.getTileEntity(pos);
            if(teB != null && teB instanceof SnailBoxBlockEntity) {
                UUID owner = ((SnailBoxBlockEntity) teB).getOwner();
                if(owner != null && !uuid.equals(owner) && Configuration.get().PROTECT_BOX_DESTROY.get()) {
                    event.setCanceled(true);
                } else {
                    SnailBoxData.getData(((World) event.getWorld()).getServer()).removeBoxRaw(new Location(event.getWorld(), pos));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        ArrayList<BlockPos> list = new ArrayList<>();
        event.getExplosion().getAffectedBlockPositions().forEach(pos -> {
            if(event.getWorld().getBlockState(pos).getBlock() == ModBlocks.SNAIL_BOX) {
                list.add(pos);
            }
        });
        for(BlockPos pos : list) {
            event.getExplosion().getAffectedBlockPositions().remove(pos);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if(event.getWorld() instanceof World && !event.getWorld().isRemote()) {
            ChunkPos cPos = event.getChunk().getPos();
            Location[] posL = SnailBoxData.getData(((World) event.getWorld()).getServer()).getAllBoxes().toArray(new Location[0]);
            for(int i = 0; i < posL.length; i++) {
                Location loc = posL[i];
                if(loc.getDimension() == event.getWorld().getDimension().getType()) {
                    if(cPos.getXStart() <= loc.getX() && cPos.getXEnd() >= loc.getX()) {
                        if(cPos.getZStart() <= loc.getZ() && cPos.getZEnd() >= loc.getZ()) {
                            BlockState state = event.getChunk().getBlockState(loc.toBP());
                            if(state.getBlock() != ModBlocks.SNAIL_BOX) {
                                SnailBoxData.getData(((World) event.getWorld()).getServer()).removeBoxRaw(loc);
                            }
                        }
                    }
                }
            }
        }
    }
}
