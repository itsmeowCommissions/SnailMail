package dev.itsmeow.snailmail.block;

import java.util.ArrayList;
import java.util.UUID;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.SnailMail.SnailBoxData;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModBlockEntities;
import dev.itsmeow.snailmail.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SnailMail.MODID)
public class SnailBoxBlock extends Block {

    public SnailBoxBlock(String name) {
        super(Block.Properties.create(Material.WOOD, MaterialColor.WOOD).hardnessAndResistance(2.0F, 1200.0F).sound(SoundType.WOOD));
        this.setRegistryName(SnailMail.MODID, name);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModBlockEntities.SNAIL_BOX.create();
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity teB = worldIn.getTileEntity(pos);
        if(teB != null && teB instanceof SnailBoxBlockEntity && player instanceof ServerPlayerEntity && (!SnailMail.Configuration.get().LOCK_BOXES.get() || PlayerEntity.getUUID(player.getGameProfile()).equals(((SnailBoxBlockEntity) teB).getOwner()))) {
            ((SnailBoxBlockEntity) teB).openGUI((ServerPlayerEntity) player);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if(placer instanceof PlayerEntity) {
            UUID uuid = PlayerEntity.getUUID(((PlayerEntity) placer).getGameProfile());
            TileEntity teB = worldIn.getTileEntity(pos);
            if(teB != null && teB instanceof SnailBoxBlockEntity) {
                ((SnailBoxBlockEntity) teB).setOwner(uuid);
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
                if(!uuid.equals(owner)) {
                    event.setCanceled(true);
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
        if(event.getWorld() instanceof World) {
            ChunkPos cPos = event.getChunk().getPos();
            BlockPos[] posL = SnailBoxData.getData(((World) event.getWorld()).getServer()).getAllBoxes().toArray(new BlockPos[0]);
            for(int i = 0; i < posL.length; i++) {
                BlockPos pos = posL[i];
                if(cPos.getXStart() <= pos.getX() && cPos.getXEnd() >= pos.getX()) {
                    if(cPos.getZStart() <= pos.getZ() && cPos.getZEnd() >= pos.getZ()) {
                        BlockState state = event.getChunk().getBlockState(pos);
                        if(state.getBlock() != ModBlocks.SNAIL_BOX) {
                            SnailBoxData.getData(((World) event.getWorld()).getServer()).removeBoxRaw(pos);
                        }
                    }
                }
            }
        }
    }
}
