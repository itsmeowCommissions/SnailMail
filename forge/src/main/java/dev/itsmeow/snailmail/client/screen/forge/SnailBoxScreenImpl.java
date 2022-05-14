package dev.itsmeow.snailmail.client.screen.forge;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import vazkii.quark.content.management.client.screen.widgets.MiniInventoryButton;

public class SnailBoxScreenImpl {

    public static <T extends GuiEventListener & Widget & NarratableEntry> boolean checkButton(T guiEventListener) {
        return guiEventListener instanceof MiniInventoryButton;
    }

}
