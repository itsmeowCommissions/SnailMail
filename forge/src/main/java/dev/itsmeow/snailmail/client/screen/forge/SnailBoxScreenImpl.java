package dev.itsmeow.snailmail.client.screen.forge;

import net.minecraft.client.gui.components.AbstractWidget;
import vazkii.quark.content.management.client.gui.MiniInventoryButton;

public class SnailBoxScreenImpl {

    public static <T extends AbstractWidget> boolean checkButton(T button) {
        return button instanceof MiniInventoryButton;
    }

}
