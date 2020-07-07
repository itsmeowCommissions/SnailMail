package dev.itsmeow.snailmail.util;

import java.nio.charset.StandardCharsets;

import net.minecraft.network.PacketBuffer;

public class RandomUtil {

    public static String filterAllowedCharacters(String input) {
        StringBuilder sb = new StringBuilder();
        for(char c : input.toCharArray()) {
            if(RandomUtil.isAllowedCharacter(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isAllowedCharacter(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_';
    }

    public static void writeString(PacketBuffer buf, String value) {
        buf.writeInt(value.length());
        buf.writeCharSequence(value, StandardCharsets.UTF_8);
    }

    public static String readString(PacketBuffer buf) {
        return String.valueOf(buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8));
    }
}
