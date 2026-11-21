package com.oheers.fish.baits.manager;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.model.ApplicationResult;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import com.oheers.fish.items.nbt.NbtKeys;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.utils.WeightedRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BaitNBTManager {
   public static final int UNLIMITED_BAIT = -1;
   public static final String BAIT_SEPARATOR = ":";
   public static final String BAIT_ENTRY_DELIMITER = ",";
   public static final String LINE_IDENTIFIER = "\u200c";

   private BaitNBTManager() {
      throw new UnsupportedOperationException();
   }

   public static boolean isBaitObject(ItemStack itemStack) {
      return itemStack != null && !itemStack.isEmpty() ? NBTHolder.itemStack(itemStack).hasKey(NbtKeys.EMF_BAIT.get()) : false;
   }

   @Nullable
   public static String getBaitName(@NonNull ItemStack itemStack) {
      return itemStack.isEmpty() ? null : NBTHolder.itemStack(itemStack).getString(NbtKeys.EMF_BAIT.get());
   }

   public static ItemStack applyBaitNBT(ItemStack item, String bait) {
      if (item != null && !item.isEmpty()) {
         NBTHolder.itemStack(item).setString(NbtKeys.EMF_BAIT.get(), bait);
         return item;
      } else {
         return null;
      }
   }

   public static boolean isBaitedRod(ItemStack itemStack) {
      return itemStack != null && !itemStack.isEmpty() && itemStack.getType() == Material.FISHING_ROD
         ? NBTHolder.itemStack(itemStack).hasKey(NbtKeys.EMF_APPLIED_BAIT.get())
         : false;
   }

   public static ApplicationResult applyBaitedRodNBT(ItemStack item, BaitHandler bait, int quantity) throws MaxBaitsReachedException, MaxBaitReachedException {
      boolean doingLoreStuff = MainConfig.getInstance().getBaitAddToLore();
      AtomicBoolean maxBait = new AtomicBoolean(false);
      AtomicInteger cursorModifier = new AtomicInteger();
      StringBuilder combined = new StringBuilder();
      if (isBaitedRod(item)) {
         removeOldLoreIfNecessary(item, bait);
         boolean foundBait = applyToExistingBaits(item, bait, quantity, combined, cursorModifier, maxBait);
         if (!foundBait) {
            addNewBait(item, bait, quantity, combined, cursorModifier, maxBait, doingLoreStuff);
         } else if (!combined.isEmpty()) {
            combined.deleteCharAt(combined.length() - 1);
         }

         updateNBT(item, combined);
      } else {
         applyInitialBait(item, bait, quantity, combined, cursorModifier, maxBait);
      }

      if (doingLoreStuff && !combined.isEmpty()) {
         item.editMeta(meta -> meta.lore(newApplyLore(item)));
      }

      if (maxBait.get()) {
         throw new MaxBaitReachedException(bait, new ApplicationResult(item, cursorModifier.get()));
      } else {
         return new ApplicationResult(item, cursorModifier.get());
      }
   }

   private static void addNewBait(
      ItemStack item, BaitHandler bait, int quantity, StringBuilder combined, AtomicInteger cursorModifier, AtomicBoolean maxBait, boolean doingLoreStuff
   ) throws MaxBaitsReachedException {
      if (getNumBaitsApplied(item) >= MainConfig.getInstance().getBaitsPerRod()) {
         if (doingLoreStuff) {
            item.editMeta(meta -> meta.lore(newApplyLore(item)));
         }

         throw new MaxBaitsReachedException("Max baits reached.", new ApplicationResult(item, cursorModifier.get()));
      } else {
         int maxBaits = bait.getBaitData().maxBaits();
         if (quantity > maxBaits && maxBaits != -1) {
            cursorModifier.set(-maxBaits);
            combined.append(bait.getId()).append(":").append(maxBaits);
            maxBait.set(true);
         } else {
            combined.append(bait.getId()).append(":").append(quantity);
            cursorModifier.set(-quantity);
         }
      }
   }

   private static void removeOldLoreIfNecessary(@NonNull ItemStack item, BaitHandler bait) {
      try {
         item.editMeta(meta -> meta.lore(deleteOldLore(item)));
      } catch (IndexOutOfBoundsException var3) {
         EvenMoreFish.getInstance().getLogger().severe("Failed to apply bait: " + bait.getId() + " to a user's fishing rod. Check baits.yml format.");
         throw new RuntimeException("Lore removal failed.", var3);
      }
   }

   private static boolean applyToExistingBaits(
      ItemStack item, BaitHandler bait, int quantity, StringBuilder combined, AtomicInteger cursorModifier, AtomicBoolean maxBait
   ) {
      boolean foundBait = false;

      for (String baitName : getBaitArray(item)) {
         String[] split = baitName.split(":");
         String baitId = split[0];
         int baitQuantity = "∞".equals(split[1]) ? -1 : Integer.parseInt(split[1]);
         if (baitId.equals(bait.getId())) {
            if (!bait.getBaitData().infinite() && baitQuantity != -1) {
               int newQuantity = baitQuantity + quantity;
               int maxBaits = bait.getBaitData().maxBaits();
               if (newQuantity > maxBaits && maxBaits != -1) {
                  combined.append(baitId).append(":").append(maxBaits).append(",");
                  cursorModifier.set(-maxBaits + (newQuantity - quantity));
                  maxBait.set(true);
               } else if (newQuantity != 0) {
                  combined.append(baitId).append(":").append(newQuantity).append(",");
                  cursorModifier.set(-quantity);
               }
            } else {
               combined.append(baitId).append(":∞,");
            }

            foundBait = true;
         } else {
            combined.append(baitName).append(",");
         }
      }

      return foundBait;
   }

   private static void updateNBT(ItemStack item, StringBuilder combined) {
      if (item != null && !item.isEmpty()) {
         NBTHolder.itemStack(item).setString(NbtKeys.EMF_APPLIED_BAIT.get(), combined.isEmpty() ? null : combined.toString());
      }
   }

   private static void applyInitialBait(
      ItemStack item, BaitHandler bait, int quantity, StringBuilder combined, AtomicInteger cursorModifier, AtomicBoolean maxBait
   ) {
      if (item != null && !item.isEmpty()) {
         NBTHolder<ItemStack> holder = NBTHolder.itemStack(item);
         int maxBaits = bait.getBaitData().maxBaits();
         if (quantity > maxBaits && maxBaits != -1) {
            combined.append(bait.getId()).append(":").append(maxBaits);
            cursorModifier.set(-maxBaits);
            maxBait.set(true);
         } else {
            combined.append(bait.getId()).append(":").append(quantity);
            cursorModifier.set(-quantity);
         }

         holder.setString(NbtKeys.EMF_APPLIED_BAIT.get(), combined.toString());
      }
   }

   @Nullable
   public static BaitHandler randomBaitApplication(ItemStack fishingRod) {
      if (fishingRod != null && fishingRod.getItemMeta() != null) {
         String[] baitNameList = getBaitArray(fishingRod);
         List<BaitHandler> baitList = new ArrayList<>();

         for (String baitName : baitNameList) {
            BaitHandler bait = BaitManager.getInstance().getBait(baitName.split(":")[0]);
            if (bait != null) {
               baitList.add(bait);
            }
         }

         return baitList.isEmpty() ? null : WeightedRandom.pick(baitList, baitx -> baitx.getBaitData().applicationWeight(), EvenMoreFish.RANDOM);
      } else {
         return null;
      }
   }

   public static Optional<BaitHandler> randomBaitCatch() {
      Map<String, BaitHandler> baitMap = BaitManager.getInstance().getItemMap();
      List<BaitHandler> baitList = baitMap.values().stream().filter(bait -> bait.getBaitData().canBeCaught()).toList();
      return baitList.isEmpty() ? Optional.empty() : Optional.of(WeightedRandom.pick(baitList, bait -> bait.getBaitData().catchWeight(), EvenMoreFish.RANDOM));
   }

   public static boolean hasBaitApplied(ItemStack itemStack, String bait) {
      if (itemStack == null) {
         return false;
      } else {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta == null) {
            return false;
         } else {
            String[] baitList = getBaitArray(itemStack);

            for (String appliedBait : baitList) {
               if (appliedBait.split(":")[0].equals(bait)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public static int deleteAllBaits(ItemStack itemStack) {
      if (itemStack != null && !itemStack.isEmpty()) {
         NBTHolder<ItemStack> holder = NBTHolder.itemStack(itemStack);
         if (!holder.hasKey(NbtKeys.EMF_APPLIED_BAIT.get())) {
            return 0;
         } else {
            String[] baitList = getBaitArray(itemStack);
            int totalDeleted = Arrays.stream(baitList).filter(Objects::nonNull).mapToInt(bait -> {
               String[] parts = bait.split(":");
               return getDeletedFromQuantityString(parts[1]);
            }).sum();
            holder.setString(NbtKeys.EMF_APPLIED_BAIT.get(), null);
            return totalDeleted;
         }
      } else {
         return 0;
      }
   }

   private static int getDeletedFromQuantityString(final String quantityStr) {
      return quantityStr.equals("∞") ? 1 : Integer.parseInt(quantityStr);
   }

   public static List<Component> newApplyLore(ItemStack itemStack) {
      if (itemStack != null && !itemStack.isEmpty()) {
         NBTHolder<ItemStack> holder = NBTHolder.itemStack(itemStack);
         holder.setBoolean(NbtKeys.EMF_BAIT_REFORMATTED.get(), true);
         List<Component> lore = itemStack.lore();
         if (lore == null) {
            lore = new ArrayList<>();
         }

         EMFListMessage format = ConfigMessage.BAIT_ROD_LORE.getMessage().toListMessage();
         Supplier<EMFListMessage> baitVariable = () -> {
            List<Component> list = new ArrayList<>();
            String rodNBT = holder.getString(NbtKeys.EMF_APPLIED_BAIT.get());
            if (rodNBT != null && !rodNBT.isEmpty()) {
               String[] baitRodNbt = rodNBT.split(",");

               for (String bait : baitRodNbt) {
                  EMFSingleMessage baitFormat = ConfigMessage.BAIT_BAITS.getMessage().toSingleMessage();
                  String[] parts = bait.split(":");
                  if (parts.length == 2) {
                     baitFormat.setAmount(parts[1]);
                  } else {
                     baitFormat.setAmount("N/A");
                  }

                  baitFormat.setBait(getBaitFormatted(parts[0]));
                  list.add(baitFormat.getComponentMessage());
               }

               if (MainConfig.getInstance().getBaitShowUnusedSlots()) {
                  for (int i = baitRodNbt.length; i < MainConfig.getInstance().getBaitsPerRod(); i++) {
                     list.add(ConfigMessage.BAIT_UNUSED_SLOT.getMessage().getComponentMessage());
                  }
               }

               return EMFListMessage.ofList(list);
            } else {
               return EMFListMessage.empty();
            }
         };
         format.setVariableWithListInsertion("{baits}", baitVariable.get());
         format.setCurrentBaits(Integer.toString(getNumBaitsApplied(itemStack)));
         format.setMaxBaits(Integer.toString(MainConfig.getInstance().getBaitsPerRod()));
         lore.addAll(format.getComponentListMessage().stream().map(component -> (TextComponent)Component.text("\u200c").append(component)).toList());
         return lore;
      } else {
         return List.of();
      }
   }

   public static List<Component> deleteOldLore(ItemStack itemStack) {
      if (removeOldLoreFormat(itemStack)) {
         return itemStack.lore();
      } else {
         List<Component> lore = itemStack.lore();
         return lore != null && !lore.isEmpty()
            ? lore.stream().filter(component -> !PlainTextComponentSerializer.plainText().serialize(component).startsWith("\u200c")).toList()
            : Collections.emptyList();
      }
   }

   private static int getNumBaitsApplied(ItemStack itemStack) {
      if (itemStack != null && !itemStack.isEmpty()) {
         String rodNBT = NBTHolder.itemStack(itemStack).getString(NbtKeys.EMF_APPLIED_BAIT.get());
         return rodNBT == null ? 1 : rodNBT.split(",").length;
      } else {
         return 1;
      }
   }

   private static EMFSingleMessage getBaitFormatted(String baitID) {
      BaitHandler bait = BaitManager.getInstance().getBait(baitID);
      if (bait == null) {
         EvenMoreFish.getInstance().getLogger().warning(() -> "Bait %s is not a valid bait!".formatted(baitID));
         return EMFSingleMessage.fromString("Invalid Bait");
      } else {
         return EMFSingleMessage.fromString(bait.getDisplayName());
      }
   }

   private static boolean removeOldLoreFormat(@NonNull ItemStack item) {
      if (item.isEmpty()) {
         return false;
      } else {
         NBTHolder<ItemStack> holder = NBTHolder.itemStack(item);
         if (holder.hasKey(NbtKeys.EMF_BAIT_REFORMATTED.get())) {
            return false;
         } else {
            List<Component> lore = item.lore();
            if (lore != null && !lore.isEmpty()) {
               if (MainConfig.getInstance().getBaitShowUnusedSlots()) {
                  int maxBaits = MainConfig.getInstance().getBaitsPerRod() + ConfigMessage.BAIT_ROD_LORE.getMessage().getPlainTextListMessage(null).size();

                  for (int i = 1; i < maxBaits; i++) {
                     try {
                        lore.removeLast();
                     } catch (IndexOutOfBoundsException | NoSuchElementException var7) {
                        break;
                     }
                  }
               } else {
                  int numBaitsApplied = getNumBaitsApplied(item) + ConfigMessage.BAIT_ROD_LORE.getMessage().getPlainTextListMessage(null).size();

                  for (int i = 1; i < numBaitsApplied; i++) {
                     try {
                        lore.removeLast();
                     } catch (IndexOutOfBoundsException | NoSuchElementException var6) {
                        break;
                     }
                  }
               }

               holder.setBoolean(NbtKeys.EMF_BAIT_REFORMATTED.get(), true);
               item.lore(lore);
               return true;
            } else {
               return true;
            }
         }
      }
   }

   public static String[] getBaitArray(final ItemStack item) {
      if (item != null && !item.isEmpty()) {
         String appliedBait = NBTHolder.itemStack(item).getString(NbtKeys.EMF_APPLIED_BAIT.get());
         return appliedBait == null ? new String[0] : appliedBait.split(",");
      } else {
         return new String[0];
      }
   }
}
