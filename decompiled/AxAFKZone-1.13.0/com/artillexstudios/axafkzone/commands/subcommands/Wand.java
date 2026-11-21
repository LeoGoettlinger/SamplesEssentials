package com.artillexstudios.axafkzone.commands.subcommands;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.libs.axapi.items.NBTWrapper;
import com.artillexstudios.axafkzone.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axafkzone.libs.axapi.utils.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum Wand {
   INSTANCE;

   public void execute(Player sender) {
      WrappedItemStack wrapped = ItemBuilder.create(AxAFKZone.LANG.getSection("selection-wand")).glow(true).wrapped();
      NBTWrapper wrapper = new NBTWrapper(wrapped);
      wrapper.set("axafkzone-wand", true);
      wrapper.build();
      sender.getInventory().addItem(new ItemStack[]{wrapped.toBukkit()});
   }
}
