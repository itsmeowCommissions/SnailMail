package dev.itsmeow.snailmail.util;

import java.util.Objects;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class Location {

    public static final Location ZERO = new Location(DimensionType.OVERWORLD, 0, 0, 0);
    private final DimensionType dimension;
    private final int x;
    private final int y;
    private final int z;

    public Location(IWorld world, BlockPos pos) {
        this(world.getDimension().getType(), pos.getX(), pos.getY(), pos.getZ());
    }

    public Location(IWorld world, int x, int y, int z) {
        this(world.getDimension().getType(), x, y, z);
    }

    public Location(DimensionType dimension, BlockPos pos) {
        this(dimension, pos.getX(), pos.getY(), pos.getZ());
    }

    public Location(DimensionType dimension, int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public BlockPos toBP() {
        return new BlockPos(x, y, z);
    }

    public void write(PacketBuffer buf) {
        buf.writeString(dimension.getRegistryName().toString(), 60);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static Location read(PacketBuffer buf) {
        return new Location(DimensionType.byName(new ResourceLocation(buf.readString(60))), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public CompoundNBT write(CompoundNBT tag) {
        tag.putString("dim", dimension.getRegistryName().toString());
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        return tag;
    }

    public static Location read(CompoundNBT tag) {
        return new Location(DimensionType.byName(new ResourceLocation(tag.getString("dim"))), tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    public DimensionType getDimension() {
        return dimension;
    }

    public World getWorld(MinecraftServer server) {
        return DimensionManager.getWorld(server, dimension, true, true);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension.getRegistryName().toString(), x, y, z);
    }

    @Override
    public boolean equals(Object other) {
        if(this == other) {
            return true;
        } else if(!(other instanceof Location)) {
            return false;
        } else {
            Location otherL = (Location) other;
            if(this.getX() != otherL.getX()) {
                return false;
            } else if(this.getY() != otherL.getY()) {
                return false;
            } else if(this.getZ() != otherL.getZ()) {
                return false;
            } else {
                return this.getDimension() == otherL.getDimension();
            }
        }
    }

}
