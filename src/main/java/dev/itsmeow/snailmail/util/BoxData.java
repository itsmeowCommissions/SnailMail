package dev.itsmeow.snailmail.util;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.network.PacketBuffer;

public class BoxData {

    public final String name;
    @Nullable
    public final Location pos;
    @Nonnull
    public final int posHash;
    public final boolean showPos;
    public final boolean member;

    public BoxData(String name, int posHash, boolean member) {
        this.name = name;
        this.posHash = posHash;
        this.pos = null;
        this.showPos = false;
        this.member = member;
    }

    public BoxData(String name, Location pos, boolean showPos, boolean member) {
        this.name = name;
        this.posHash = pos.hashCode();
        this.pos = pos;
        this.showPos = showPos;
        this.member = member;
    }

    public void write(PacketBuffer buf) {
        buf.writeString(name, 35);
        buf.writeInt(posHash);
        buf.writeBoolean(showPos);
        (showPos ? pos : Location.ZERO).write(buf);
        buf.writeBoolean(member);
    }

    public static BoxData read(PacketBuffer buf) {
        String name = buf.readString(35);
        int hash = buf.readInt();
        if(buf.readBoolean()) {
            return new BoxData(name, Location.read(buf), true, buf.readBoolean());
        } else {
            Location.read(buf);
        }
        return new BoxData(name, hash, buf.readBoolean());
    }

    @Override
    public boolean equals(Object arg0) {
        if(arg0 instanceof BoxData) {
            BoxData data = (BoxData) arg0;
            return arg0 == this || ((name == null && data.name == null || this.name.equals(data.name)) && (this.posHash == data.posHash) && (this.member == data.member));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, posHash, member);
    }

    
}
