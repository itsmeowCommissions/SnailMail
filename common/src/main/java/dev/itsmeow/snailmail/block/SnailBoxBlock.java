package dev.itsmeow.snailmail.block;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.util.SnailMailCommonConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class SnailBoxBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {

    private static VoxelShape SHAPE_X;
    private static VoxelShape SHAPE_Z;
    public static HashMap<UUID, BlockPos> lastClickedBox = new HashMap<>();

    static {
        double d = 0.0625D * 4;
        SHAPE_X = Shapes.box(d, 0, 0, 1 - d, 1, 1);
        SHAPE_Z = Shapes.box(0, 0, d, 1, 1, 1 - d);
    }

    public SnailBoxBlock() {
        super(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 1200.0F).sound(SoundType.WOOD));
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH).setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis() == Direction.Axis.Z ? SHAPE_X : SHAPE_Z;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SnailBoxBlockEntity(blockPos, blockState);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(state.getBlock() != newState.getBlock()) {
            BlockEntity teB = worldIn.getBlockEntity(pos);
            if(teB instanceof SnailBoxBlockEntity) {
                SnailBoxBlockEntity.dropItems((SnailBoxBlockEntity) teB);
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if(level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            if (level.getBlockEntity(pos) != null) {
                if (canOpen(level, pos, player)) {
                    lastClickedBox.put(player.getUUID(), new BlockPos(pos));
                    ((SnailBoxBlockEntity) level.getBlockEntity(pos)).openGUI((ServerPlayer) player);
                } else if (hand == InteractionHand.MAIN_HAND) {
                    player.sendSystemMessage(Component.translatable("message.snailmail.noperm").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    public static boolean canOpen(Level worldIn, BlockPos pos, Player player) {
        BlockEntity teB = worldIn.getBlockEntity(pos);
        return teB != null && teB instanceof SnailBoxBlockEntity && isAccessibleFor((SnailBoxBlockEntity) teB, player);
    }

    public static boolean isAccessibleFor(SnailBoxBlockEntity te, Player player) {

        if(!SnailMailCommonConfig.lockBoxes()) {
            return true;
        }
        UUID uuid = UUIDUtil.getOrCreatePlayerUUID(player.getGameProfile());
        if(uuid.equals(te.getOwner()) || te.isMember(uuid)) {
            return true;
        }
        if(SnailMailCommonConfig.opBypassLock()) {
            return player.hasPermissions(SnailMailCommonConfig.bypassLockOpLevel());
        }
        return false;
    }

}
