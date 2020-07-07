package dev.itsmeow.snailmail.entity;

import dev.itsmeow.snailmail.init.ModEntities;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SnailManEntity extends MobEntity {

    private BlockPos mailbox;

    public SnailManEntity(World worldIn) {
        super(ModEntities.SNAIL_MAN.entityType, worldIn);
        this.setInvulnerable(true);
    }

    @Override
    protected void registerGoals() {

    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source != DamageSource.OUT_OF_WORLD;
    }

    public void setMailboxLocation(BlockPos pos) {
        this.mailbox = pos;
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if(compound.contains("mailX") && compound.contains("mailY") && compound.contains("mailZ")) {
            this.mailbox = new BlockPos(compound.getInt("mailX"), compound.getInt("mailY"), compound.getInt("mailZ"));
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        if(mailbox != null) {
            compound.putInt("mailX", mailbox.getX());
            compound.putInt("mailY", mailbox.getY());
            compound.putInt("mailZ", mailbox.getZ());
        }
    }

}
