package dev.itsmeow.snailmail.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class Location {

    public static final Location ZERO = new Location(Level.OVERWORLD, 0, 0, 0);
    private final ResourceKey<Level> dimension;
    private final int x;
    private final int y;
    private final int z;

    public Location(Level world, BlockPos pos) {
        this(world.dimension(), pos.getX(), pos.getY(), pos.getZ());
    }

    public Location(Level world, int x, int y, int z) {
        this(world.dimension(), x, y, z);
    }

    public Location(ResourceKey<Level> dimension, BlockPos pos) {
        this(dimension, pos.getX(), pos.getY(), pos.getZ());
    }

    public Location(ResourceKey<Level> dimension, int x, int y, int z) {
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

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(dimension.location().toString(), 60);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static Location read(FriendlyByteBuf buf) {
        return new Location(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(60))), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public CompoundTag write(CompoundTag tag) {
        tag.putString("dim", dimension.location().toString());
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("z", z);
        return tag;
    }

    public static Location read(CompoundTag tag) {
        return new Location(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("dim"))), tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public ServerLevel getWorld(MinecraftServer server) {
        return server.getLevel(dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension.location().toString(), x, y, z);
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

    public Vec3 asVec() {
        return new Vec3(this.x + 0.5, this.y, this.z + 0.5);
    }

}
