package dev.itsmeow.snailmail.item;

import dev.itsmeow.snailmail.SnailMail;
import net.minecraft.item.Item;

public class NamedItem extends Item {

    public NamedItem(String name) {
        this(name, new Item.Properties().group(SnailMail.ITEM_GROUP));
    }

    public NamedItem(String name, Item.Properties props) {
        super(props);
        this.setRegistryName(SnailMail.MODID, name);
    }

}
