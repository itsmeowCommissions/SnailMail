package net.examplemod.client.screen.forge;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class SnailBoxScreenImpl {
    public static <T extends GuiEventListener & Widget & NarratableEntry> boolean checkButton(T guiEventListener) {
        return false; //guiEventListener instanceof MiniInventoryButton;
    }
}
