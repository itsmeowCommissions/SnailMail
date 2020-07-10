package dev.itsmeow.snailmail.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;

public class ServerBoxData {

    public final Location pos;
    public final UUID owner;
    public String name;
    public Set<UUID> members;
    public boolean publicBox = false;

    public ServerBoxData(String name, Location pos, UUID owner, Set<UUID> members, boolean publicBox) {
        this.name = name;
        this.pos = pos;
        this.owner = owner;
        this.members = members == null ? new HashSet<UUID>() : members;
    }

    public void write(CompoundNBT tag) {
        tag.putString("name", name);
        tag.put("location", pos.write(new CompoundNBT()));
        tag.putUniqueId("owner", owner);
        UUID[] uuids = members.toArray(new UUID[0]);
        ListNBT tagList = new ListNBT();
        for(int i = 0; i < uuids.length; i++) {
            tagList.add(StringNBT.valueOf(uuids[i].toString()));
        }
        tag.put("members", tagList);
        tag.putBoolean("public", publicBox);
    }

    public static ServerBoxData read(CompoundNBT tag) {
        String name = tag.getString("name");
        Location pos = Location.read(tag.getCompound("location"));
        UUID owner = tag.getUniqueId("owner");
        Set<UUID> uuids = new HashSet<UUID>();
        ListNBT tagList = tag.getList("members", Constants.NBT.TAG_STRING);
        for(int i = 0; i < tagList.size(); i++) {
            uuids.add(UUID.fromString(tagList.getString(i)));
        }
        boolean publicB =  tag.getBoolean("public");
        return new ServerBoxData(name, pos, owner, uuids, publicB);
    }

    @Override
    public boolean equals(Object other) {
        if(this == other) {
            return true;
        } else if(!(other instanceof ServerBoxData)) {
            return false;
        } else {
            ServerBoxData otherL = (ServerBoxData) other;
            if(!pos.equals(otherL.pos)) {
                return false;
            } else if(!name.equals(otherL.name)) {
                return false;
            } else if(!owner.equals(otherL.owner)) {
                return false;
            } else if(publicBox != otherL.publicBox) {
                return false;
            } else {
                return members.equals(otherL.members);
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, pos, owner, members, publicBox);
    }

}
