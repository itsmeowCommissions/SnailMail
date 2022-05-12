package dev.itsmeow.snailmail.util;

import net.minecraft.SharedConstants;

public class RandomUtil {

    public static String filterAllowedCharacters(String input, boolean mode) {
        StringBuilder sb = new StringBuilder();
        for(char c : input.toCharArray()) {
            if(RandomUtil.isAllowedCharacter(c, mode)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isAllowedCharacter(char c, boolean mode) {
        boolean off = Character.isAlphabetic(c) || Character.isDigit(c) || c == '_';
        if(!mode) {
            return off;
        } else {
            return SharedConstants.isAllowedChatCharacter(c);
        }
    }

}
