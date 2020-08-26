package dev.itsmeow.snailmail.entity;

import java.util.EnumSet;
import java.util.function.Function;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.SnailMail.SnailBoxData;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModBlocks;
import dev.itsmeow.snailmail.init.ModEntities;
import dev.itsmeow.snailmail.util.Location;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

public class SnailManEntity extends CreatureEntity {

    private static final DataParameter<Float> OPACITY = EntityDataManager.createKey(SnailManEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> YAW = EntityDataManager.createKey(SnailManEntity.class, DataSerializers.FLOAT);
    private Location fromMailbox;
    private Location mailbox;
    private ItemStack transport;
    private boolean leavingDeliveryPoint = true;
    private boolean deliveryFailed = false;

    public SnailManEntity(World worldIn) {
        super(ModEntities.SNAIL_MAN.entityType, worldIn);
        this.setInvulnerable(true);
        this.noClip = true;
    }

    public SnailManEntity(World worldIn, Location destination, ItemStack transport, Location fromMailbox) {
        super(ModEntities.SNAIL_MAN.entityType, worldIn);
        this.setInvulnerable(true);
        this.setNoGravity(true);
        this.noClip = true;
        this.fromMailbox = fromMailbox;
        this.mailbox = destination;
        this.transport = transport;
    }

    public void setOpacity(float opacity) {
        if(opacity > 1F) {
            opacity = 1F;
        } else if(opacity < 0F) {
            opacity = 0F;
        }
        this.dataManager.set(OPACITY, opacity);
    }

    public float getOpacity() {
        return this.dataManager.get(OPACITY);
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(OPACITY, 1F);
        this.dataManager.register(YAW, 0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new MoveAwayFromSpawnGoal(this));
        this.goalSelector.addGoal(1, new MoveToBoxGoal(this));
        this.goalSelector.addGoal(2, new ReturnFailedDelivery(this));
    }

    @Override
    public void tick() {
        super.tick();
        this.rotationYaw = this.dataManager.get(YAW);
        this.rotationYawHead = this.dataManager.get(YAW);
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isNoDespawnRequired() {
        return true;
    }

    @Override
    public boolean preventDespawn() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source != DamageSource.OUT_OF_WORLD;
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.fromMailbox = Location.read(compound.getCompound("fromLocation"));
        this.mailbox = Location.read(compound.getCompound("targetLocation"));
        this.transport = ItemStack.read(compound.getCompound("item"));
        this.leavingDeliveryPoint = compound.getBoolean("leaving");
        this.deliveryFailed = compound.getBoolean("failed");
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        CompoundNBT tag1 = new CompoundNBT();
        mailbox.write(tag1);
        compound.put("targetLocation", tag1);

        CompoundNBT tag2 = new CompoundNBT();
        fromMailbox.write(tag2);
        compound.put("fromLocation", tag2);

        CompoundNBT tag = new CompoundNBT();
        transport.write(tag);
        compound.put("item", tag);

        compound.putBoolean("leaving", this.leavingDeliveryPoint);
        compound.putBoolean("failed", this.deliveryFailed);
    }

    public static class MoveAwayFromSpawnGoal extends Goal {

        protected SnailManEntity snail;
        private Vector3d from;
        private int totalTicks = 0;
        private Vector3d dest;

        public MoveAwayFromSpawnGoal(SnailManEntity snail) {
            this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.snail = snail;
        }

        @Override
        public boolean shouldExecute() {
            return snail.fromMailbox != null && snail.leavingDeliveryPoint;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return snail.getPositionVec().distanceTo(from) < 7 && snail.leavingDeliveryPoint && totalTicks < 160;
        }

        @Override
        public void resetTask() {
            totalTicks = 0;
            dest = null;
            snail.leavingDeliveryPoint = false;
            snail.setMotion(0, 0, 0);
        }

        @Override
        public void startExecuting() {
            this.from = snail.fromMailbox.asVec();
        }

        @Override
        public void tick() {
            Direction direction = this.getDirection();
            if(dest == null) {
                BlockPos away = this.getAwayPos(direction);
                dest = new Vector3d(away.getX() + 0.5, away.getY(), away.getZ() + 0.5);
            }
            if(dest != null) {
                snail.dataManager.set(YAW, direction.getHorizontalAngle());
                snail.setMotion(dest.subtract(snail.getPositionVec()).normalize().scale(0.05));
                float opacity = Math.abs(6F - Math.min((float) snail.getPositionVec().distanceTo(from) - 1F, 6F)) / 6F;
                snail.setOpacity(opacity < 0.2F ? 0F : opacity);
            }
            totalTicks++;
        }

        private Direction getDirection() {
            Direction dir = Direction.NORTH;
            if(snail.world.isBlockPresent(snail.fromMailbox.toBP())) {
                BlockState state = snail.world.getBlockState(snail.fromMailbox.toBP());
                if(state != null && state.getBlock() == ModBlocks.SNAIL_BOX) {
                    dir = state.get(BlockStateProperties.HORIZONTAL_FACING);
                }
            }
            return dir;
        }

        private BlockPos getAwayPos(Direction dir) {
            return snail.fromMailbox.toBP().offset(dir, 7);
        }

    }

    public static class MoveToBoxGoal extends Goal {

        protected SnailManEntity snail;
        private Vector3d to;
        private int totalTicks = 0;
        private float angle = 0F;
        private boolean taskReset = false;

        public MoveToBoxGoal(SnailManEntity snail) {
            this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.snail = snail;
        }

        @Override
        public boolean shouldExecute() {
            return snail.mailbox != null && !snail.leavingDeliveryPoint && !snail.deliveryFailed;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !taskReset && !snail.deliveryFailed && snail.getPositionVec().distanceTo(to) > 1 || !snail.world.getRegistryKey().equals(snail.mailbox.getDimension());
        }

        @Override
        public void resetTask() {
            totalTicks = 0;
            snail.setMotion(0, 0, 0);
            BlockPos pos = snail.mailbox.toBP();
            ServerWorld world = snail.mailbox.getWorld(snail.getServer());
            TileEntity teB = world.getTileEntity(pos);
            boolean isInvalid = world.getBlockState(pos).getBlock() != ModBlocks.SNAIL_BOX || teB == null || !(teB instanceof SnailBoxBlockEntity);
            if(isInvalid || !SnailMail.deliverTo((SnailBoxBlockEntity) teB, snail.transport, false)) {
                snail.deliveryFailed = true;
                SnailMail.forceArea(snail.fromMailbox.getWorld(snail.getServer()), snail.fromMailbox.toBP(), true);
                if(isInvalid) {
                    SnailBoxData.getData(snail.getServer()).removeBoxRaw(snail.mailbox);
                }
                // return to deliverer
            } else {
                // delivery was successful, remove!
                snail.remove();
            }
            // remove chunkload so long as return position is not the same as delivery and we will be returning
            if(snail.deliveryFailed || !snail.fromMailbox.equals(snail.mailbox)) {
                SnailMail.forceArea(world, pos, false);
            }
        }

        @Override
        public void startExecuting() {
            this.to = snail.mailbox.asVec();
            angle = getDirection().getOpposite().getHorizontalAngle();
            BlockPos away = this.getAwayPos(getDirection());
            ServerWorld destWorld = snail.mailbox.getWorld(snail.getServer());
            BlockPos newPos = destWorld.isBlockPresent(away) ? away : snail.mailbox.toBP();
            if(destWorld.chunkExists(newPos.getX() >> 4, newPos.getZ() >> 4) && destWorld.getChunkProvider().isChunkLoaded(new ChunkPos(newPos.getX() >> 4, newPos.getZ() >> 4))) {
                transport(newPos);
            } else {
                // entity won't tick there, just do delivery without animation
                taskReset = true;
            }
        }

        @Override
        public void tick() {
            if(totalTicks > 160) {
                snail.moveToBlockPosAndAngles(snail.mailbox.toBP().up(), angle, snail.rotationPitch);
                snail.setMotion(0, 0, 0);
            } else {
                snail.dataManager.set(YAW, angle);
                snail.setMotion(to.subtract(snail.getPositionVec()).normalize().scale(0.05));
                float opacity = Math.abs(6F - Math.min((float) snail.getPositionVec().distanceTo(to) - 3F, 6F)) / 6F;
                snail.setOpacity(opacity < 0.2F ? 0F : opacity);
            }
            totalTicks++;
        }

        private Direction getDirection() {
            Direction dir = Direction.NORTH;
            if(snail.world.isBlockPresent(snail.mailbox.toBP())) {
                BlockState state = snail.world.getBlockState(snail.mailbox.toBP());
                if(state != null && state.getBlock() == ModBlocks.SNAIL_BOX) {
                    dir = state.get(BlockStateProperties.HORIZONTAL_FACING);
                }
            }
            return dir;
        }

        private BlockPos getAwayPos(Direction dir) {
            return snail.mailbox.toBP().offset(dir, 7);
        }

        private void transport(BlockPos newPos) {
            if(!snail.world.getRegistryKey().equals(snail.mailbox.getDimension())) {
                snail.changeDimension(snail.getServer().getWorld(snail.mailbox.getDimension()), new ITeleporter() {
                    @Override
                    public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                        Entity newEntity = entity.getType().create(destWorld);
                        if(newEntity != null) {
                            newEntity.copyDataFromOld(entity);
                            newEntity.moveToBlockPosAndAngles(newPos, angle, newEntity.rotationPitch);
                            newEntity.setMotion(0, 0, 0);
                            destWorld.addEntity(newEntity);
                        }
                        return entity;
                    }
                });
            } else {
                snail.setMotion(0, 0, 0);
                snail.moveToBlockPosAndAngles(newPos, angle, snail.rotationPitch);
            }
        }
    }

    public static class ReturnFailedDelivery extends Goal {

        protected SnailManEntity snail;
        private Vector3d to;
        private int totalTicks = 0;
        private float angle = 0F;
        private boolean taskReset = false;

        public ReturnFailedDelivery(SnailManEntity snail) {
            this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.snail = snail;
        }

        @Override
        public boolean shouldExecute() {
            return snail.fromMailbox != null && snail.deliveryFailed;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return !taskReset && snail.getPositionVec().distanceTo(to) > 1 || !snail.world.getRegistryKey().equals(snail.fromMailbox.getDimension());
        }

        @Override
        public void resetTask() {
            totalTicks = 0;
            snail.setMotion(0, 0, 0);
            BlockPos pos = snail.fromMailbox.toBP();
            TileEntity teB = snail.world.getTileEntity(pos);
            boolean isInvalid = snail.world.getBlockState(pos).getBlock() != ModBlocks.SNAIL_BOX || teB == null || !(teB instanceof SnailBoxBlockEntity);
            if(isInvalid || !SnailMail.deliverTo((SnailBoxBlockEntity) teB, snail.transport, true)) {
                if(isInvalid) {
                    SnailBoxData.getData(snail.getServer()).removeBoxRaw(snail.fromMailbox);
                }
                snail.entityDropItem(snail.transport);
            }
            // remove chunkload
            SnailMail.forceArea(snail.fromMailbox.getWorld(snail.getServer()), pos, false);
            snail.remove();
        }

        @Override
        public void startExecuting() {
            this.to = snail.fromMailbox.asVec();
            angle = getDirection().getOpposite().getHorizontalAngle();
            BlockPos away = this.getAwayPos(getDirection());
            ServerWorld destWorld = snail.fromMailbox.getWorld(snail.getServer());
            BlockPos newPos = destWorld.isBlockPresent(away) ? away : snail.fromMailbox.toBP();
            if(destWorld.chunkExists(newPos.getX() >> 4, newPos.getZ() >> 4) && destWorld.getChunkProvider().isChunkLoaded(new ChunkPos(newPos.getX() >> 4, newPos.getZ() >> 4))) {
                if(!snail.world.getRegistryKey().equals(snail.fromMailbox.getDimension())) {
                    snail.changeDimension(snail.getServer().getWorld(snail.fromMailbox.getDimension()), new ITeleporter() {
                        @Override
                        public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                            Entity newEntity = entity.getType().create(destWorld);
                            if(newEntity != null) {
                                newEntity.copyDataFromOld(entity);
                                newEntity.moveToBlockPosAndAngles(newPos, angle, newEntity.rotationPitch);
                                newEntity.setMotion(0, 0, 0);
                                destWorld.addEntity(newEntity);
                            }
                            return entity;
                        }
                    });
                } else {
                    snail.setMotion(0, 0, 0);
                    snail.moveToBlockPosAndAngles(newPos, angle, snail.rotationPitch);
                }
            } else {
                // return instantly, somehow the original delivery chunk isn't ticking anymore
                this.taskReset = true;
            }
            
        }

        @Override
        public void tick() {
            if(totalTicks > 160) {
                snail.moveToBlockPosAndAngles(snail.fromMailbox.toBP().up(), angle, snail.rotationPitch);
                snail.setMotion(0, 0, 0);
            } else {
                snail.dataManager.set(YAW, angle);
                snail.setMotion(to.subtract(snail.getPositionVec()).normalize().scale(0.05));
                float opacity = Math.abs(6F - Math.min((float) snail.getPositionVec().distanceTo(to) - 3F, 6F)) / 6F;
                snail.setOpacity(opacity < 0.2F ? 0F : opacity);
            }
            totalTicks++;
        }

        private Direction getDirection() {
            Direction dir = Direction.NORTH;
            if(snail.world.isBlockPresent(snail.fromMailbox.toBP())) {
                BlockState state = snail.world.getBlockState(snail.fromMailbox.toBP());
                if(state != null && state.getBlock() == ModBlocks.SNAIL_BOX) {
                    dir = state.get(BlockStateProperties.HORIZONTAL_FACING);
                }
            }
            return dir;
        }

        private BlockPos getAwayPos(Direction dir) {
            return snail.fromMailbox.toBP().offset(dir, 7);
        }

    }

}
