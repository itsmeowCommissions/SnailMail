package dev.itsmeow.snailmail.menu.forge;

import dev.itsmeow.snailmail.menu.SnailBoxMenu;
import dev.itsmeow.snailmail.menu.slots.forge.EnvelopeSlot;
import dev.itsmeow.snailmail.menu.slots.forge.ReadOnlySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraftforge.items.IItemHandler;

import java.util.Set;

public class SnailBoxMenuForge extends SnailBoxMenu {

    private IItemHandler items;

    public SnailBoxMenuForge(int id, Container playerInventory, BlockPos pos, IItemHandler items) {
        super(id, playerInventory, pos);
        this.items = items;
        addOwnSlots();
        addPlayerSlots(playerInventory);
    }

    public SnailBoxMenuForge(int id, Container playerInventory, IItemHandler items, BlockPos pos, String startingName, boolean isOwner, boolean isPublic, Set<String> members) {
        super(id, playerInventory, pos, startingName, isOwner, isPublic, members);
        this.items = items;
        addOwnSlots();
        addPlayerSlots(playerInventory);
    }

    protected void addOwnSlots() {
        for(int i = 0; i < 27; i++) {
            int yCoord = i / 9 * 18; // 9 slots fit per row, 18 is size of the slot texture
            int xCoord = i % 9 * 18; // 0, 1*18, 2*18, 3*18, loop per row
            this.addSlot(new ReadOnlySlot(items, i, 8 + xCoord, 26 + yCoord));
        }
        this.addSlot(new EnvelopeSlot(items, 27, 8 + 8 * 18, 6));
    }

}
