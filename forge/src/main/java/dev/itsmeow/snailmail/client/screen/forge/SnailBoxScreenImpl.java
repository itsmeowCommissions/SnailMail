package dev.itsmeow.snailmail.client.screen.forge;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class SnailBoxScreenImpl {

    public static <T extends GuiEventListener & Renderable & NarratableEntry> boolean checkButton(T guiEventListener) {
        return false; //guiEventListener instanceof MiniInventoryButton;
    }

}
