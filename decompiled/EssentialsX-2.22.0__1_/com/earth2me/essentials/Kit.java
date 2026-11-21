package com.earth2me.essentials;

import com.earth2me.essentials.commands.NoChargeException;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.libs.snakeyaml.external.biz.base64Coder.Base64Coder;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.SimpleTextInput;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.NumberUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.KitClaimEvent;
import net.ess3.provider.SerializationProvider;
import net.essentialsx.api.v2.events.KitPreExpandItemsEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Kit {
   final net.ess3.api.IEssentials ess;
   final String kitName;
   final Map<String, Object> kit;
   final Trade charge;

   public Kit(final String kitName, final net.ess3.api.IEssentials ess) throws Exception {
      this.kitName = kitName;
      this.ess = ess;
      this.kit = ess.getKits().getKit(kitName);
      this.charge = new Trade("kit-" + kitName, new Trade("kit-kit", ess), ess);
      if (this.kit == null) {
         throw new TranslatableException("kitNotFound");
      }
   }

   public String getName() {
      return this.kitName;
   }

   public void checkPerms(final User user) throws Exception {
      if (!user.isAuthorized("essentials.kits." + this.kitName)) {
         throw new TranslatableException("noKitPermission", "essentials.kits." + this.kitName);
      }
   }

   public void checkDelay(final User user) throws Exception {
      long nextUse = this.getNextUse(user);
      if (nextUse != 0L) {
         if (nextUse < 0L) {
            user.sendTl("kitOnce");
            throw new NoChargeException();
         } else {
            user.sendTl("kitTimed", DateUtil.formatDateDiff(nextUse));
            throw new NoChargeException();
         }
      }
   }

   public void checkAffordable(final User user) throws Exception {
      this.charge.isAffordableFor(user);
   }

   public void setTime(final User user) throws Exception {
      Calendar time = new GregorianCalendar();
      user.setKitTimestamp(this.kitName, time.getTimeInMillis());
   }

   public void resetTime(final User user) {
      user.setKitTimestamp(this.kitName, 0L);
   }

   public void chargeUser(final User user) throws Exception {
      this.charge.charge(user);
   }

   public long getNextUse(final User user) throws Exception {
      if (user.isAuthorized("essentials.kit.exemptdelay")) {
         return 0L;
      } else {
         Calendar time = new GregorianCalendar();
         double delay = 0.0;

         try {
            delay = this.kit.containsKey("delay") ? ((Number)this.kit.get("delay")).doubleValue() : 0.0;
         } catch (Exception var8) {
            throw new TranslatableException("kitError2");
         }

         long lastTime = user.getKitTimestamp(this.kitName);
         Calendar delayTime = new GregorianCalendar();
         delayTime.setTimeInMillis(lastTime);
         delayTime.add(13, (int)delay);
         delayTime.add(14, (int)(delay * 1000.0 % 1000.0));
         if (lastTime == 0L || lastTime > time.getTimeInMillis()) {
            return 0L;
         } else if (delay < 0.0) {
            return -1L;
         } else {
            return delayTime.before(time) ? 0L : delayTime.getTimeInMillis();
         }
      }
   }

   @Deprecated
   public List<String> getItems(final User user) throws Exception {
      return this.getItems();
   }

   public List<String> getItems() throws Exception {
      if (this.kit == null) {
         throw new TranslatableException("kitNotFound");
      } else {
         try {
            List<String> itemList = new ArrayList<>();
            Object kitItems = this.kit.get("items");
            if (kitItems instanceof List) {
               for (Object item : (List)kitItems) {
                  if (!(item instanceof String)) {
                     throw new Exception("Invalid kit item: " + item.toString());
                  }

                  itemList.add(item.toString());
               }

               return itemList;
            } else {
               throw new Exception("Invalid item list");
            }
         } catch (Exception var5) {
            this.ess.getLogger().log(Level.WARNING, "Error parsing kit " + this.kitName + ": " + var5.getMessage());
            throw new TranslatableException(var5, "kitError2");
         }
      }
   }

   public boolean expandItems(final User user) throws Exception {
      return this.expandItems(user, this.getItems(user));
   }

   public boolean expandItems(final User user, final List<String> items) throws Exception {
      try {
         IText input = new SimpleTextInput(items);
         IText output = new KeywordReplacer(input, user.getSource(), this.ess, true, true);
         KitClaimEvent event = new KitClaimEvent(user, this);
         Bukkit.getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            return false;
         } else {
            boolean spew = false;
            boolean allowUnsafe = this.ess.getSettings().allowUnsafeEnchantments();
            boolean autoEquip = this.ess.getSettings().isKitAutoEquip();
            List<ItemStack> itemList = new ArrayList<>();
            Map<Integer, ItemStack> itemsWithSlot = new HashMap<>();
            List<String> commandQueue = new ArrayList<>();
            List<String> moneyQueue = new ArrayList<>();
            String currencySymbol = this.ess.getSettings().getCurrencySymbol().isEmpty() ? "$" : this.ess.getSettings().getCurrencySymbol();

            for (String kitItem : output.getLines()) {
               if (kitItem.startsWith("$") || kitItem.startsWith(currencySymbol)) {
                  moneyQueue.add(NumberUtil.sanitizeCurrencyString(kitItem, this.ess));
               } else if (kitItem.startsWith("/")) {
                  String command = kitItem.substring(1);
                  String name = user.getName();
                  command = command.replace("{player}", name);
                  commandQueue.add(command);
               } else {
                  int itemSlot = -1;
                  if (kitItem.startsWith("slot:")) {
                     int spaceIndex = kitItem.indexOf(" ");
                     if (spaceIndex != -1) {
                        String slotStr = kitItem.substring("slot:".length(), spaceIndex);
                        itemSlot = NumberUtil.isInt(slotStr) ? Integer.parseInt(slotStr) : -1;
                        kitItem = kitItem.substring(spaceIndex + 1);
                     }
                  }

                  SerializationProvider serializationProvider = this.ess.provider(SerializationProvider.class);
                  ItemStack stack;
                  if (kitItem.startsWith("@")) {
                     if (serializationProvider == null) {
                        this.ess
                           .getLogger()
                           .log(Level.WARNING, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("kitError3", this.kitName, user.getName())));
                        continue;
                     }

                     stack = serializationProvider.deserializeItem(Base64Coder.decodeLines(kitItem.substring(1)));
                  } else {
                     String[] parts = kitItem.split(" +");
                     ItemStack parseStack = this.ess.getItemDb().get(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 1);
                     if (parseStack.getType() == Material.AIR) {
                        continue;
                     }

                     MetaItemStack metaStack = new MetaItemStack(parseStack);
                     if (parts.length > 2) {
                        metaStack.parseStringMeta(null, allowUnsafe, parts, 2, this.ess);
                     }

                     stack = metaStack.getItemStack();
                  }

                  if (itemSlot != -1 && !itemsWithSlot.containsKey(itemSlot)) {
                     itemsWithSlot.put(itemSlot, stack);
                  } else {
                     itemList.add(stack);
                  }
               }
            }

            int maxStackSize = user.isAuthorized("essentials.oversizedstacks") ? this.ess.getSettings().getOversizedStackSize() : 0;
            boolean isDropItemsIfFull = this.ess.getSettings().isDropItemsIfFull();
            List<ItemStack> totalItems = new ArrayList<>(itemList);
            totalItems.addAll(itemsWithSlot.values());
            KitPreExpandItemsEvent itemsEvent = new KitPreExpandItemsEvent(user, this.kitName, totalItems);
            Bukkit.getPluginManager().callEvent(itemsEvent);
            ItemStack[] totalItemsArray = totalItems.toArray(new ItemStack[0]);
            if (!isDropItemsIfFull && !Inventories.hasSpace(user.getBase(), maxStackSize, autoEquip, totalItemsArray)) {
               user.sendTl("kitInvFullNoDrop");
               return false;
            } else {
               for (Entry<Integer, ItemStack> itemWithSlot : itemsWithSlot.entrySet()) {
                  ItemStack leftover = Inventories.addItem(user.getBase(), maxStackSize, itemWithSlot.getValue(), itemWithSlot.getKey());
                  if (leftover != null) {
                     itemList.add(leftover);
                  }
               }

               ItemStack[] itemArray = itemList.toArray(new ItemStack[0]);
               Map<Integer, ItemStack> leftover = Inventories.addItem(user.getBase(), maxStackSize, autoEquip, itemArray);
               if (!isDropItemsIfFull && !leftover.isEmpty()) {
                  throw new IllegalStateException(
                     "Something has gone terribly wrong while adding items to the user's inventory. Please report this to the EssentialsX developers. Items left over: "
                        + leftover
                        + ". Original items: "
                        + Arrays.toString((Object[])itemArray)
                  );
               } else {
                  for (ItemStack itemStack : leftover.values()) {
                     for (int spillAmount = itemStack.getAmount(); spillAmount > 0; spillAmount -= itemStack.getAmount()) {
                        if (maxStackSize != 0) {
                           itemStack.setAmount(Math.min(spillAmount, itemStack.getMaxStackSize()));
                        }

                        user.getWorld().dropItemNaturally(user.getLocation(), itemStack);
                     }

                     spew = true;
                  }

                  user.getBase().updateInventory();

                  for (String valueString : moneyQueue) {
                     BigDecimal value = new BigDecimal(valueString.trim());
                     Trade t = new Trade(value, this.ess);
                     t.pay(user, Trade.OverflowType.DROP);
                  }

                  for (String cmd : commandQueue) {
                     Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                  }

                  if (spew) {
                     user.sendTl("kitInvFull");
                  }

                  return true;
               }
            }
         }
      } catch (Exception var25) {
         user.getBase().updateInventory();
         this.ess.getLogger().log(Level.WARNING, var25.getMessage());
         throw new TranslatableException(var25, "kitError2");
      }
   }
}
