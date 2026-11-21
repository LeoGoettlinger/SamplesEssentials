package net.coreprotect.database.rollback;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Lookup;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.utility.ItemUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;

public class RollbackUtil extends Lookup {
   protected static int modifyContainerItems(Material type, Object container, int slot, ItemStack itemstack, int action) {
      int modifiedArmor = -1;

      try {
         ItemStack[] contents = null;
         if (type != null && type.equals(Material.ARMOR_STAND)) {
            EntityEquipment equipment = (EntityEquipment)container;
            if (equipment != null) {
               if (action == 1) {
                  itemstack.setAmount(1);
               } else {
                  itemstack.setType(Material.AIR);
                  itemstack.setAmount(0);
               }

               if (slot < 4) {
                  contents = equipment.getArmorContents();
                  if (slot >= 0) {
                     contents[slot] = itemstack;
                  }

                  equipment.setArmorContents(contents);
               } else {
                  ArmorStand armorStand = (ArmorStand)equipment.getHolder();
                  armorStand.setArms(true);
                  switch (slot) {
                     case 4:
                        equipment.setItemInMainHand(itemstack);
                        break;
                     case 5:
                        equipment.setItemInOffHand(itemstack);
                  }
               }
            }
         } else if (type != null && type.equals(Material.ITEM_FRAME)) {
            ItemFrame frame = (ItemFrame)container;
            if (frame != null) {
               if (action == 1) {
                  itemstack.setAmount(1);
               } else {
                  itemstack.setType(Material.AIR);
                  itemstack.setAmount(0);
               }

               frame.setItem(itemstack);
            }
         } else if (type != null && type.equals(Material.JUKEBOX)) {
            Jukebox jukebox = (Jukebox)container;
            if (jukebox != null) {
               if (action == 1 && itemstack.getType().name().startsWith("MUSIC_DISC")) {
                  itemstack.setAmount(1);
               } else {
                  itemstack.setType(Material.AIR);
                  itemstack.setAmount(0);
               }

               jukebox.setRecord(itemstack);
               jukebox.update();
            }
         } else {
            Inventory inventory = (Inventory)container;
            if (inventory != null) {
               boolean isPlayerInventory = inventory instanceof PlayerInventory;
               if (action == 1) {
                  int count = 0;
                  int amount = itemstack.getAmount();
                  itemstack.setAmount(1);

                  for (; count < amount; count++) {
                     boolean addedItem = false;
                     if (isPlayerInventory) {
                        int setArmor = ItemUtils.setPlayerArmor((PlayerInventory)inventory, itemstack);
                        addedItem = setArmor > -1;
                        modifiedArmor = addedItem ? setArmor : modifiedArmor;
                     }

                     if (!addedItem) {
                        if (!BukkitAdapter.ADAPTER.isChiseledBookshelf(type)) {
                           addedItem = inventory.addItem(new ItemStack[]{itemstack}).size() == 0;
                        } else {
                           ItemStack[] inventoryContents = inventory.getStorageContents();
                           int i = 0;

                           for (ItemStack stack : inventoryContents) {
                              if (stack == null) {
                                 inventoryContents[i] = itemstack;
                                 addedItem = true;
                                 break;
                              }

                              i++;
                           }

                           if (addedItem) {
                              inventory.setStorageContents(inventoryContents);
                           } else {
                              addedItem = inventory.addItem(new ItemStack[]{itemstack}).size() == 0;
                           }
                        }
                     }

                     if (!addedItem && isPlayerInventory) {
                        PlayerInventory playerInventory = (PlayerInventory)inventory;
                        ItemStack offhand = playerInventory.getItemInOffHand();
                        if (offhand == null
                           || offhand.getType() == Material.AIR
                           || itemstack.isSimilar(offhand) && offhand.getAmount() < offhand.getMaxStackSize()) {
                           ItemStack setOffhand = itemstack.clone();
                           if (itemstack.isSimilar(offhand)) {
                              setOffhand.setAmount(offhand.getAmount() + 1);
                           }

                           playerInventory.setItemInOffHand(setOffhand);
                        }
                     }
                  }
               } else {
                  int removeAmount = itemstack.getAmount();
                  ItemStack removeMatch = itemstack.clone();
                  removeMatch.setAmount(1);
                  ItemStack[] inventoryContents = (ItemStack[])(isPlayerInventory ? inventory.getContents() : inventory.getStorageContents()).clone();

                  for (int i = inventoryContents.length - 1; i >= 0; i--) {
                     if (inventoryContents[i] != null) {
                        ItemStack itemStack = inventoryContents[i].clone();
                        int maxAmount = itemStack.getAmount();
                        int currentAmount = maxAmount;
                        itemStack.setAmount(1);
                        if (itemStack.toString().equals(removeMatch.toString())) {
                           for (int scan = 0; scan < maxAmount && removeAmount > 0; scan++) {
                              itemStack.setAmount(--currentAmount);
                              removeAmount--;
                           }
                        } else {
                           itemStack.setAmount(maxAmount);
                        }

                        if (itemStack.getAmount() == 0) {
                           inventoryContents[i] = null;
                        } else {
                           inventoryContents[i] = itemStack;
                        }
                     }

                     if (removeAmount == 0) {
                        break;
                     }
                  }

                  if (isPlayerInventory) {
                     inventory.setContents(inventoryContents);
                  } else {
                     inventory.setStorageContents(inventoryContents);
                  }

                  for (int count = 0; count < removeAmount; count++) {
                     inventory.removeItem(new ItemStack[]{removeMatch});
                  }
               }
            }
         }
      } catch (Exception var18) {
         var18.printStackTrace();
      }

      return modifiedArmor;
   }

   public static void sortContainerItems(PlayerInventory inventory, List<Integer> modifiedArmorSlots) {
      try {
         ItemStack[] armorContents = inventory.getArmorContents();
         ItemStack[] storageContents = inventory.getStorageContents();

         for (int armor = 0; armor < armorContents.length; armor++) {
            ItemStack armorItem = armorContents[armor];
            if (armorItem != null && modifiedArmorSlots.contains(armor)) {
               for (int storage = 0; storage < storageContents.length; storage++) {
                  ItemStack storageItem = storageContents[storage];
                  if (storageItem == null) {
                     storageContents[storage] = armorItem;
                     armorContents[armor] = null;
                     break;
                  }
               }
            }
         }

         inventory.setArmorContents(armorContents);
         inventory.setStorageContents(storageContents);
      } catch (Exception var8) {
         var8.printStackTrace();
      }
   }

   private static void buildFireworkEffect(Builder effectBuilder, Material rowType, ItemStack itemstack) {
      try {
         FireworkEffect effect = effectBuilder.build();
         if (rowType == Material.FIREWORK_ROCKET) {
            FireworkMeta meta = (FireworkMeta)itemstack.getItemMeta();
            meta.addEffect(effect);
            itemstack.setItemMeta(meta);
         } else if (rowType == Material.FIREWORK_STAR) {
            FireworkEffectMeta meta = (FireworkEffectMeta)itemstack.getItemMeta();
            meta.setEffect(effect);
            itemstack.setItemMeta(meta);
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public static Object[] populateItemStack(ItemStack itemstack, Object list) {
      int slot = 0;
      String faceData = "";

      try {
         Material rowType = itemstack.getType();
         List<Object> metaList = (List<Object>)list;
         if (metaList.size() > 0 && !(metaList.get(0) instanceof List)) {
            if (rowType.name().endsWith("_BANNER")) {
               BannerMeta meta = (BannerMeta)itemstack.getItemMeta();

               for (Object value : metaList) {
                  if (value instanceof Map) {
                     Pattern pattern = new Pattern((Map)value);
                     meta.addPattern(pattern);
                  }
               }

               itemstack.setItemMeta(meta);
            } else if (BlockGroup.SHULKER_BOXES.contains(rowType)) {
               BlockStateMeta meta = (BlockStateMeta)itemstack.getItemMeta();
               ShulkerBox shulkerBox = (ShulkerBox)meta.getBlockState();

               for (Object valuex : metaList) {
                  ItemStack item = ItemUtils.unserializeItemStackLegacy(valuex);
                  if (item != null) {
                     shulkerBox.getInventory().addItem(new ItemStack[]{item});
                  }
               }

               meta.setBlockState(shulkerBox);
               itemstack.setItemMeta(meta);
            }

            return new Object[]{slot, faceData, itemstack};
         }

         int itemCount = 0;
         Builder effectBuilder = FireworkEffect.builder();

         for (List<Map<String, Object>> map : (List)list) {
            if (map.size() == 0) {
               if (itemCount == 3 && (rowType == Material.FIREWORK_ROCKET || rowType == Material.FIREWORK_STAR)) {
                  buildFireworkEffect(effectBuilder, rowType, itemstack);
                  itemCount = 0;
               }

               itemCount++;
            } else {
               Map<String, Object> mapData = map.get(0);
               if (mapData.get("slot") != null) {
                  slot = (Integer)mapData.get("slot");
               } else if (mapData.get("facing") != null) {
                  faceData = (String)mapData.get("facing");
               } else if (mapData.get("modifiers") != null) {
                  ItemMeta itemMeta = itemstack.getItemMeta();
                  if (itemMeta.hasAttributeModifiers()) {
                     for (Entry<Attribute, AttributeModifier> entry : itemMeta.getAttributeModifiers().entries()) {
                        itemMeta.removeAttributeModifier(entry.getKey(), entry.getValue());
                     }
                  }

                  for (Object item : (List)mapData.get("modifiers")) {
                     Map<Object, Map<String, Object>> modifiersMap = (Map<Object, Map<String, Object>>)item;

                     for (Entry<Object, Map<String, Object>> entry : modifiersMap.entrySet()) {
                        try {
                           Attribute attribute = null;
                           if (entry.getKey() instanceof Attribute) {
                              attribute = (Attribute)entry.getKey();
                           } else {
                              attribute = (Attribute)BukkitAdapter.ADAPTER.getRegistryValue((String)entry.getKey(), Attribute.class);
                           }

                           AttributeModifier modifier = AttributeModifier.deserialize(entry.getValue());
                           itemMeta.addAttributeModifier(attribute, modifier);
                        } catch (IllegalArgumentException var20) {
                        }
                     }
                  }

                  itemstack.setItemMeta(itemMeta);
               } else if (itemCount == 0) {
                  ItemMeta meta = ItemUtils.deserializeItemMeta(itemstack.getItemMeta().getClass(), map.get(0));
                  itemstack.setItemMeta(meta);
                  if (map.size() > 1 && rowType == Material.POTION) {
                     PotionMeta subMeta = (PotionMeta)itemstack.getItemMeta();
                     Color color = Color.deserialize(map.get(1));
                     subMeta.setColor(color);
                     itemstack.setItemMeta(subMeta);
                  }
               } else if (rowType != Material.LEATHER_HORSE_ARMOR
                  && rowType != Material.LEATHER_HELMET
                  && rowType != Material.LEATHER_CHESTPLATE
                  && rowType != Material.LEATHER_LEGGINGS
                  && rowType != Material.LEATHER_BOOTS) {
                  if (rowType == Material.POTION) {
                     for (Map<String, Object> potionData : map) {
                        PotionMeta meta = (PotionMeta)itemstack.getItemMeta();
                        PotionEffect effect = new PotionEffect(potionData);
                        meta.addCustomEffect(effect, true);
                        itemstack.setItemMeta(meta);
                     }
                  } else if (rowType.name().endsWith("_BANNER")) {
                     for (Map<String, Object> patternData : map) {
                        BannerMeta meta = (BannerMeta)itemstack.getItemMeta();
                        Pattern pattern = new Pattern(patternData);
                        meta.addPattern(pattern);
                        itemstack.setItemMeta(meta);
                     }
                  } else if (rowType == Material.CROSSBOW) {
                     CrossbowMeta meta = (CrossbowMeta)itemstack.getItemMeta();

                     for (Map<String, Object> itemData : map) {
                        ItemStack crossbowItem = ItemUtils.unserializeItemStack(itemData);
                        if (crossbowItem != null) {
                           meta.addChargedProjectile(crossbowItem);
                        }
                     }

                     itemstack.setItemMeta(meta);
                  } else if (rowType == Material.MAP || rowType == Material.FILLED_MAP) {
                     for (Map<String, Object> colorData : map) {
                        MapMeta meta = (MapMeta)itemstack.getItemMeta();
                        Color color = Color.deserialize(colorData);
                        meta.setColor(color);
                        itemstack.setItemMeta(meta);
                     }
                  } else if (rowType != Material.FIREWORK_ROCKET && rowType != Material.FIREWORK_STAR) {
                     if (rowType == Material.SUSPICIOUS_STEW) {
                        for (Map<String, Object> suspiciousStewData : map) {
                           SuspiciousStewMeta meta = (SuspiciousStewMeta)itemstack.getItemMeta();
                           PotionEffect effect = new PotionEffect(suspiciousStewData);
                           meta.addCustomEffect(effect, true);
                           itemstack.setItemMeta(meta);
                        }
                     } else {
                        BukkitAdapter.ADAPTER.setItemMeta(rowType, itemstack, map);
                     }
                  } else if (itemCount == 1) {
                     effectBuilder = FireworkEffect.builder();

                     for (Map<String, Object> fireworkData : map) {
                        Type type = (Type)fireworkData.getOrDefault("type", Type.BALL);
                        boolean hasFlicker = (Boolean)fireworkData.get("flicker");
                        boolean hasTrail = (Boolean)fireworkData.get("trail");
                        effectBuilder.with(type);
                        effectBuilder.flicker(hasFlicker);
                        effectBuilder.trail(hasTrail);
                     }
                  } else if (itemCount == 2) {
                     for (Map<String, Object> colorData : map) {
                        Color color = Color.deserialize(colorData);
                        effectBuilder.withColor(color);
                     }
                  } else if (itemCount == 3) {
                     for (Map<String, Object> colorData : map) {
                        Color color = Color.deserialize(colorData);
                        effectBuilder.withFade(color);
                     }

                     buildFireworkEffect(effectBuilder, rowType, itemstack);
                     itemCount = 0;
                  }
               } else {
                  for (Map<String, Object> colorData : map) {
                     LeatherArmorMeta meta = (LeatherArmorMeta)itemstack.getItemMeta();
                     Color color = Color.deserialize(colorData);
                     meta.setColor(color);
                     itemstack.setItemMeta(meta);
                  }
               }

               itemCount++;
            }
         }
      } catch (Exception var21) {
         var21.printStackTrace();
      }

      return new Object[]{slot, faceData, itemstack};
   }

   public static Object[] populateItemStack(ItemStack itemstack, byte[] metadata) {
      if (metadata != null) {
         try {
            ByteArrayInputStream metaByteStream = new ByteArrayInputStream(metadata);
            BukkitObjectInputStream metaObjectStream = new BukkitObjectInputStream(metaByteStream);
            Object metaList = metaObjectStream.readObject();
            metaObjectStream.close();
            metaByteStream.close();
            return populateItemStack(itemstack, metaList);
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

      return new Object[]{0, "", itemstack};
   }

   public static List<Object> deserializeMetadata(byte[] metadata) {
      if (metadata == null) {
         return null;
      } else {
         try {
            ByteArrayInputStream metaByteStream = new ByteArrayInputStream(metadata);
            BukkitObjectInputStream metaObjectStream = new BukkitObjectInputStream(metaByteStream);
            List<Object> metaList = (List<Object>)metaObjectStream.readObject();
            metaObjectStream.close();
            metaByteStream.close();
            return metaList;
         } catch (Exception var4) {
            var4.printStackTrace();
            return null;
         }
      }
   }

   public static void queueEntitySpawn(String user, BlockState block, EntityType type, int data) {
      Queue.queueEntitySpawn(user, block, type, data);
   }

   public static void queueSkullUpdate(String user, BlockState block, int rowId) {
      Queue.queueSkullUpdate(user, block, rowId);
   }

   public static void queueSignUpdate(String user, BlockState block, int action, int time) {
      Queue.queueSignUpdate(user, block, action, time);
   }
}
