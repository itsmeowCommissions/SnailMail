package dev.itsmeow.snailmail.menu.forge;

import dev.itsmeow.snailmail.menu.EnvelopeMenu;
import dev.itsmeow.snailmail.menu.slots.forge.NoItemCapSlot;
import dev.itsmeow.snailmail.menu.slots.forge.StampSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraftforge.items.IItemHandler;

public class EnvelopeMenuForge extends EnvelopeMenu {

    private final IItemHandler items;

    public EnvelopeMenuForge(int id, Container playerInventory, IItemHandler items) {
        this(id, playerInventory, items, BlockPos.ZERO, "", "");
    }

    public EnvelopeMenuForge(int id, Container playerInventory, IItemHandler items, BlockPos returnPos, String toName, String fromName) {
        super(id, playerInventory, items.getSlots(), returnPos, toName, fromName);
        this.items = items;
        addOwnSlots();
        addPlayerSlots(playerInventory);
    }

    protected void addOwnSlots() {
        for (int i = 0; i < 27; i++) {
            int yCoord = i / 9 * 18; // 9 slots fit per row, 18 is size of the slot texture
            int xCoord = i % 9 * 18; // 0, 1*18, 2*18, 3*18, loop per row
            this.addSlot(new NoItemCapSlot(items, i, 8 + xCoord, 26 + yCoord));
        }
        this.addSlot(new StampSlot(items, 27, 8 + 8 * 18, 6));
    }

}
