package dev.itsmeow.snailmail.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class NamedBlockItem extends BlockItem {

    public NamedBlockItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
        this.setRegistryName(blockIn.getRegistryName());
    }

}
