package dev.itsmeow.snailmail.entity;

import dev.itsmeow.imdlib.entity.EntityTypeContainer;
import dev.itsmeow.imdlib.entity.interfaces.IContainerEntity;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.SnailMail.SnailBoxSavedData;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModBlocks;
import dev.itsmeow.snailmail.init.ModEntities;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.util.Location;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

public class SnailManEntity extends PathfinderMob implements IContainerEntity<SnailManEntity> {

    private static final EntityDataAccessor<Float> OPACITY = SynchedEntityData.defineId(SnailManEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(SnailManEntity.class, EntityDataSerializers.FLOAT);
    private Location fromMailbox;
    private Location mailbox;
    private ItemStack transport;
    private boolean leavingDeliveryPoint = true;
    private boolean deliveryFailed = false;

    public SnailManEntity(EntityType<? extends SnailManEntity> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    public SnailManEntity(EntityType<? extends SnailManEntity> type, Level level, Location destination, ItemStack transport, Location fromMailbox) {
        super(type, level);
        this.setInvulnerable(true);
        this.setNoGravity(true);
        this.noPhysics = true;
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
        this.entityData.set(OPACITY, opacity);
    }

    public float getOpacity() {
        return this.entityData.get(OPACITY);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OPACITY, 1F);
        this.entityData.define(YAW, 0F);
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
        this.yRot = this.entityData.get(YAW);
        this.yHeadRot = this.entityData.get(YAW);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source != DamageSource.OUT_OF_WORLD;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if(compound.contains("fromLocation") && compound.contains("targetLocation") && compound.contains("item") && compound.contains("leaving") && compound.contains("failed")) {
            this.fromMailbox = Location.read(compound.getCompound("fromLocation"));
            this.mailbox = Location.read(compound.getCompound("targetLocation"));
            this.transport = ItemStack.of(compound.getCompound("item"));
            this.leavingDeliveryPoint = compound.getBoolean("leaving");
            this.deliveryFailed = compound.getBoolean("failed");
        } else {
            this.remove();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        CompoundTag tag1 = new CompoundTag();
        mailbox.write(tag1);
        compound.put("targetLocation", tag1);

        CompoundTag tag2 = new CompoundTag();
        fromMailbox.write(tag2);
        compound.put("fromLocation", tag2);

        CompoundTag tag = new CompoundTag();
        transport.save(tag);
        compound.put("item", tag);

        compound.putBoolean("leaving", this.leavingDeliveryPoint);
        compound.putBoolean("failed", this.deliveryFailed);
    }

    @Override
    public SnailManEntity getImplementation() {
        return this;
    }

    @Override
    public EntityTypeContainer<? extends SnailManEntity> getContainer() {
        return ModEntities.SNAIL_MAN;
    }

    public static class MoveAwayFromSpawnGoal extends Goal {

        protected SnailManEntity snail;
        private Vec3 from;
        private int totalTicks = 0;
        private Vec3 dest;

        public MoveAwayFromSpawnGoal(SnailManEntity snail) {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.snail = snail;
        }

        @Override
        public boolean canUse() {
            return snail.fromMailbox != null && snail.leavingDeliveryPoint;
        }

        @Override
        public boolean canContinueToUse() {
            return snail.position().distanceTo(from) < 7 && snail.leavingDeliveryPoint && totalTicks < 160;
        }

        @Override
        public void stop() {
            totalTicks = 0;
            dest = null;
            snail.leavingDeliveryPoint = false;
            snail.setDeltaMovement(0, 0, 0);
        }

        @Override
        public void start() {
            this.from = snail.fromMailbox.asVec();
        }

        @Override
        public void tick() {
            Direction direction = this.getDirection();
            if(dest == null) {
                BlockPos away = this.getAwayPos(direction);
                dest = new Vec3(away.getX() + 0.5, away.getY(), away.getZ() + 0.5);
            }
            if(dest != null) {
                snail.entityData.set(YAW, direction.toYRot());
                snail.setDeltaMovement(dest.subtract(snail.position()).normalize().scale(0.05));
                float opacity = Math.abs(6F - Math.min((float) snail.position().distanceTo(from) - 1F, 6F)) / 6F;
                snail.setOpacity(opacity < 0.2F ? 0F : opacity);
            }
            totalTicks++;
        }

        private Direction getDirection() {
            Direction dir = Direction.NORTH;
            if(snail.level.isLoaded(snail.fromMailbox.toBP())) {
                BlockState state = snail.level.getBlockState(snail.fromMailbox.toBP());
                if(state != null && state.getBlock() == ModBlocks.SNAIL_BOX.get()) {
                    dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                }
            }
            return dir;
        }

        private BlockPos getAwayPos(Direction dir) {
            return snail.fromMailbox.toBP().relative(dir, 7);
        }

    }

    public static class MoveToBoxGoal extends Goal {

        protected SnailManEntity snail;
        private Vec3 to;
        private int totalTicks = 0;
        private float angle = 0F;
        private boolean taskReset = false;

        public MoveToBoxGoal(SnailManEntity snail) {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.snail = snail;
        }

        @Override
        public boolean canUse() {
            return snail.mailbox != null && !snail.leavingDeliveryPoint && !snail.deliveryFailed;
        }

        @Override
        public boolean canContinueToUse() {
            return !taskReset && !snail.deliveryFailed && snail.position().distanceTo(to) > 1 || !snail.level.dimension().equals(snail.mailbox.getDimension());
        }

        @Override
        public void stop() {
            totalTicks = 0;
            snail.setDeltaMovement(0, 0, 0);
            BlockPos pos = snail.mailbox.toBP();
            ServerLevel world = snail.mailbox.getWorld(snail.getServer());
            BlockEntity teB = world.getBlockEntity(pos);
            boolean isInvalid = world.getBlockState(pos).getBlock() != ModBlocks.SNAIL_BOX.get() || teB == null || !(teB instanceof SnailBoxBlockEntity);
            if(isInvalid || !SnailMail.deliverTo((SnailBoxBlockEntity) teB, snail.transport, false)) {
                snail.deliveryFailed = true;
                SnailMail.forceArea(snail.fromMailbox.getWorld(snail.getServer()), snail.fromMailbox.toBP(), true);
                if(isInvalid) {
                    SnailBoxSavedData.getData(snail.getServer()).removeBoxRaw(snail.mailbox);
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
        public void start() {
            this.to = snail.mailbox.asVec();
            angle = getDirection().getOpposite().toYRot();
            BlockPos away = this.getAwayPos(getDirection());
            ServerLevel destWorld = snail.mailbox.getWorld(snail.getServer());
            BlockPos newPos = destWorld != null && destWorld.isLoaded(away) ? away : snail.mailbox.toBP();
            if(destWorld != null && destWorld.hasChunk(newPos.getX() >> 4, newPos.getZ() >> 4) && destWorld.getChunkSource().isEntityTickingChunk(new ChunkPos(newPos.getX() >> 4, newPos.getZ() >> 4))) {
                transportTo(snail, newPos, angle);
            } else {
                // entity won't tick there, just do delivery without animation
                taskReset = true;
            }
        }

        @Override
        public void tick() {
            if(totalTicks > 160) {
                snail.moveTo(snail.mailbox.toBP().above(), angle, snail.xRot);
                snail.setDeltaMovement(0, 0, 0);
            } else {
                snail.entityData.set(YAW, angle);
                snail.setDeltaMovement(to.subtract(snail.position()).normalize().scale(0.05));
                float opacity = Math.abs(6F - Math.min((float) snail.position().distanceTo(to) - 3F, 6F)) / 6F;
                snail.setOpacity(opacity < 0.2F ? 0F : opacity);
            }
            totalTicks++;
        }

        private Direction getDirection() {
            Direction dir = Direction.NORTH;
            if(snail.level.isLoaded(snail.mailbox.toBP())) {
                BlockState state = snail.level.getBlockState(snail.mailbox.toBP());
                if(state != null && state.getBlock() == ModBlocks.SNAIL_BOX.get()) {
                    dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                }
            }
            return dir;
        }

        private BlockPos getAwayPos(Direction dir) {
            return snail.mailbox.toBP().relative(dir, 7);
        }


    }

    public static class ReturnFailedDelivery extends Goal {

        protected SnailManEntity snail;
        private Vec3 to;
        private int totalTicks = 0;
        private float angle = 0F;
        private boolean taskReset = false;

        public ReturnFailedDelivery(SnailManEntity snail) {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.snail = snail;
        }

        @Override
        public boolean canUse() {
            return snail.fromMailbox != null && snail.deliveryFailed;
        }

        @Override
        public boolean canContinueToUse() {
            return !taskReset && snail.position().distanceTo(to) > 1 || !snail.level.dimension().equals(snail.fromMailbox.getDimension());
        }

        @Override
        public void stop() {
            totalTicks = 0;
            snail.setDeltaMovement(0, 0, 0);
            BlockPos pos = snail.fromMailbox.toBP();
            BlockEntity teB = snail.level.getBlockEntity(pos);
            boolean isInvalid = snail.level.getBlockState(pos).getBlock() != ModBlocks.SNAIL_BOX.get() || teB == null || !(teB instanceof SnailBoxBlockEntity);
            if(isInvalid || !SnailMail.deliverTo((SnailBoxBlockEntity) teB, snail.transport, true)) {
                if(isInvalid) {
                    SnailBoxSavedData.getData(snail.getServer()).removeBoxRaw(snail.fromMailbox);
                }
                ItemStack stack = snail.transport;
                Optional<ItemStack> iOpt = EnvelopeItem.convert(snail.transport);
                if(iOpt.isPresent()) {
                    stack = iOpt.get();
                    if(!stack.hasTag()) {
                        stack.setTag(new CompoundTag());
                    }
                    stack.getTag().putBoolean("delivery_failed", true);
                }
                snail.spawnAtLocation(stack);
            }
            // remove chunkload
            SnailMail.forceArea(snail.fromMailbox.getWorld(snail.getServer()), pos, false);
            snail.remove();
        }

        @Override
        public void start() {
            this.to = snail.fromMailbox.asVec();
            angle = getDirection().getOpposite().toYRot();
            BlockPos away = this.getAwayPos(getDirection());
            ServerLevel destWorld = snail.fromMailbox.getWorld(snail.getServer());
            BlockPos newPos = destWorld.isLoaded(away) ? away : snail.fromMailbox.toBP();
            if(destWorld.hasChunk(newPos.getX() >> 4, newPos.getZ() >> 4) && destWorld.getChunkSource().isEntityTickingChunk(new ChunkPos(newPos.getX() >> 4, newPos.getZ() >> 4))) {
                transportTo(snail, newPos, angle);
            } else {
                // return instantly, somehow the original delivery chunk isn't ticking anymore
                this.taskReset = true;
            }
            
        }

        @Override
        public void tick() {
            if(totalTicks > 160) {
                snail.moveTo(snail.fromMailbox.toBP().above(), angle, snail.xRot);
                snail.setDeltaMovement(0, 0, 0);
            } else {
                snail.entityData.set(YAW, angle);
                snail.setDeltaMovement(to.subtract(snail.position()).normalize().scale(0.05));
                float opacity = Math.abs(6F - Math.min((float) snail.position().distanceTo(to) - 3F, 6F)) / 6F;
                snail.setOpacity(opacity < 0.2F ? 0F : opacity);
            }
            totalTicks++;
        }

        private Direction getDirection() {
            Direction dir = Direction.NORTH;
            if(snail.level.isLoaded(snail.fromMailbox.toBP())) {
                BlockState state = snail.level.getBlockState(snail.fromMailbox.toBP());
                if(state != null && state.getBlock() == ModBlocks.SNAIL_BOX.get()) {
                    dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                }
            }
            return dir;
        }

        private BlockPos getAwayPos(Direction dir) {
            return snail.fromMailbox.toBP().relative(dir, 7);
        }

    }

    private static void transportTo(SnailManEntity snail, BlockPos newPos, float yaw) {
        if(!snail.level.dimension().equals(snail.mailbox.getDimension())) {
            ServerLevel serverLevel = snail.getServer().getLevel(snail.mailbox.getDimension());
            Entity entity = snail.getType().create(serverLevel);
            if (entity != null) {
                entity.restoreFrom(snail);
                entity.moveTo(newPos, yaw, entity.xRot);
                entity.setDeltaMovement(0, 0, 0);
                serverLevel.addFromAnotherDimension(entity);
            }
            snail.removeAfterChangingDimensions();
            ((ServerLevel)snail.level).resetEmptyTime();
            serverLevel.resetEmptyTime();
        } else {
            snail.setDeltaMovement(0, 0, 0);
            snail.moveTo(newPos, yaw, snail.xRot);
        }
    }

}
