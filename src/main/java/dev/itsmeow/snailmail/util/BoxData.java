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

    public BoxData(String name, int posHash) {
        this.name = name;
        this.posHash = posHash;
        this.pos = null;
        this.showPos = false;
    }

    public BoxData(String name, Location pos, boolean showPos) {
        this.name = name;
        this.posHash = pos.hashCode();
        this.pos = pos;
        this.showPos = showPos;
    }

    public void write(PacketBuffer buf) {
        buf.writeString(name, 35);
        buf.writeInt(posHash);
        buf.writeBoolean(showPos);
        (showPos ? pos : Location.ZERO).write(buf);
    }

    public static BoxData read(PacketBuffer buf) {
        String name = buf.readString(35);
        int hash = buf.readInt();
        if(buf.readBoolean()) {
            return new BoxData(name, Location.read(buf), true);
        } else {
            Location.read(buf);
        }
        return new BoxData(name, hash);
    }

    @Override
    public boolean equals(Object arg0) {
        if(arg0 instanceof BoxData) {
            BoxData data = (BoxData) arg0;
            return arg0 == this || ((name == null && data.name == null || this.name.equals(data.name)) && (this.posHash == data.posHash));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, posHash);
    }

    
}
