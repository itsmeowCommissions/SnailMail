package dev.itsmeow.snailmail.util;

import java.util.Objects;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class Location {

    public static final Location ZERO = new Location(World.OVERWORLD, 0, 0, 0);
    private final RegistryKey<World> dimension;
    private final int x;
    private final int y;
    private final int z;

    public Location(World world, BlockPos pos) {
        this(world.getRegistryKey(), pos.getX(), pos.getY(), pos.getZ());
    }

    public Location(World world, int x, int y, int z) {
        this(world.getRegistryKey(), x, y, z);
    }

    public Location(RegistryKey<World> dimension, BlockPos pos) {
        this(dimension, pos.getX(), pos.getY(), pos.getZ());
    }

    public Location(RegistryKey<World> dimension, int x, int y, int z) {
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
        return new Location(RegistryKey.of(Registry.DIMENSION, new ResourceLocation(buf.readString(60))), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public CompoundNBT write(CompoundNBT tag) {
        tag.putString("dim", dimension.getRegistryName().toString());
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        return tag;
    }

    public static Location read(CompoundNBT tag) {
        return new Location(RegistryKey.of(Registry.DIMENSION, new ResourceLocation(tag.getString("dim"))), tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    public RegistryKey<World> getDimension() {
        return dimension;
    }

    public ServerWorld getWorld(MinecraftServer server) {
        return server.getWorld(dimension);
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

    public Vector3d asVec() {
        return new Vector3d(this.x + 0.5, this.y, this.z + 0.5);
    }

}
