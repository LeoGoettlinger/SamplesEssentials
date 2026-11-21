package com.artillexstudios.axtrade.libs.axapi.gui.inventory;

import java.util.function.Consumer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public record BakedGuiItem(ItemStack stack, Consumer<InventoryClickEvent> eventConsumer) {
}
