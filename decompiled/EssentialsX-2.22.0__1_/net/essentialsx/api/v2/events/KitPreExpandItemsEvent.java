package net.essentialsx.api.v2.events;

import java.util.List;
import net.ess3.api.IUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class KitPreExpandItemsEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final IUser user;
   private final String kitName;
   private final List<ItemStack> itemStacks;

   public KitPreExpandItemsEvent(IUser user, String kitName, List<ItemStack> itemStacks) {
      this.user = user;
      this.kitName = kitName;
      this.itemStacks = itemStacks;
   }

   public IUser getUser() {
      return this.user;
   }

   public String getKitName() {
      return this.kitName;
   }

   public List<ItemStack> getItemStacks() {
      return this.itemStacks;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
