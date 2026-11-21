package org.skriptlang.skript.bukkit.pdc;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.slot.Slot;
import io.papermc.paper.persistence.PersistentDataContainerView;
import java.lang.runtime.SwitchBootstraps;
import java.util.function.Consumer;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;

public final class PDCUtils {
   public static PersistentDataContainerView getPersistentDataContainer(Object holder) {
      return getPersistentDataContainer(holder, container -> {});
   }

   public static PersistentDataContainerView getPersistentDataContainer(Object holder, Consumer<PersistentDataContainerView> consumer) {
      PersistentDataContainerView container = (PersistentDataContainerView)(switch (holder) {
         case PersistentDataHolder dataHolder -> dataHolder.getPersistentDataContainer();
         case ItemType itemType -> itemType.getItemMeta().getPersistentDataContainer();
         case ItemStack itemStack -> !itemStack.hasItemMeta() ? null : itemStack.getPersistentDataContainer();
         case Slot slot -> {
            ItemStack item = slot.getItem();
            yield item != null && item.hasItemMeta() ? item.getPersistentDataContainer() : null;
         }
         case Block block when block.getState() instanceof TileState tileState -> tileState.getPersistentDataContainer();
         case null, default -> null;
      });
      if (container == null) {
         return null;
      } else {
         consumer.accept(container);
         return container;
      }
   }

   public static void editPersistentDataContainer(Object holder, Consumer<PersistentDataContainer> consumer) {
      Object var2 = holder;
      byte var3 = 0;

      while (true) {
         switch (SwitchBootstraps.typeSwitch<"typeSwitch",PersistentDataHolder,ItemType,ItemStack,Slot,Block>(var2, var3)) {
            case -1:
            default:
               break;
            case 0:
               PersistentDataHolder dataHolder = (PersistentDataHolder)var2;
               consumer.accept(dataHolder.getPersistentDataContainer());
               break;
            case 1:
               ItemType itemType = (ItemType)var2;
               ItemMeta meta = itemType.getItemMeta();
               consumer.accept(meta.getPersistentDataContainer());
               itemType.setItemMeta(meta);
               break;
            case 2:
               ItemStack itemStack = (ItemStack)var2;
               if (!itemStack.hasItemMeta()) {
                  return;
               }

               itemStack.editPersistentDataContainer(consumer);
               break;
            case 3:
               Slot slot = (Slot)var2;
               ItemStack item = slot.getItem();
               if (item == null || !item.hasItemMeta()) {
                  return;
               }

               item.editPersistentDataContainer(consumer);
               slot.setItem(item);
               break;
            case 4:
               Block block = (Block)var2;
               if (!(block.getState() instanceof TileState tileState)) {
                  var3 = 5;
                  continue;
               }

               consumer.accept(tileState.getPersistentDataContainer());
               tileState.update();
         }

         return;
      }
   }
}
