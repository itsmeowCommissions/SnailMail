package dev.itsmeow.snailmail.init;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity.SnailBoxContainer;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.item.EnvelopeItem.EnvelopeContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.network.IContainerFactory;

public class ModContainers {

    public static final ContainerType<SnailBoxContainer> SNAIL_BOX = create("snail_box", SnailBoxContainer::getClientContainer);
    public static final ContainerType<EnvelopeContainer> ENVELOPE = create("envelope", EnvelopeItem::getClientContainer);

    private static <T extends Container> ContainerType<T> create(String name, IContainerFactory<T> factory) {
        ContainerType<T> type = new ContainerType<T>(factory);
        type.setRegistryName(SnailMail.MODID, name);
        return type;
    }

}
