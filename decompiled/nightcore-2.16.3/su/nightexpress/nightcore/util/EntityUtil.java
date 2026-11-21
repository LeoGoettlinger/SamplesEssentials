package su.nightexpress.nightcore.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Nameable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.bridge.Software;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.night.NightMessage;

public class EntityUtil {
   public static final EquipmentSlot[] EQUIPMENT_SLOTS = new EquipmentSlot[]{
      EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };

   public static int nextEntityId() {
      return Software.get().nextEntityId();
   }

   public static void setCustomName(@NonNull Entity entity, @Nullable String name) {
      setCustomName((Nameable)entity, name);
   }

   public static void setCustomName(@NonNull Entity entity, @Nullable NightComponent name) {
      setCustomName((Nameable)entity, name);
   }

   @NonNull
   public static String getNameSerialized(@NonNull Entity entity) {
      String customName = getNameSerialized((Nameable)entity);
      return customName != null ? customName : LangUtil.getSerializedName(entity.getType());
   }

   public static void setCustomName(@NonNull Nameable nameable, @Nullable String name) {
      setCustomName(nameable, name == null ? null : NightMessage.parse(name));
   }

   public static void setCustomName(@NonNull Nameable nameable, @Nullable NightComponent name) {
      Software.get().setCustomName(nameable, name);
   }

   @Nullable
   public static String getNameSerialized(@NonNull Nameable nameable) {
      return Software.get().getCustomName(nameable);
   }

   @Deprecated
   public static double getAttribute(@NonNull LivingEntity entity, @NonNull Attribute attribute) {
      return getAttributeValue(entity, attribute);
   }

   @Deprecated
   public static double getAttributeBase(@NonNull LivingEntity entity, @NonNull Attribute attribute) {
      return getAttributeBaseValue(entity, attribute);
   }

   public static double getAttributeValue(@NonNull LivingEntity entity, @NonNull Attribute attribute) {
      AttributeInstance instance = entity.getAttribute(attribute);
      return instance == null ? 0.0 : instance.getValue();
   }

   public static double getAttributeBaseValue(@NonNull LivingEntity entity, @NonNull Attribute attribute) {
      AttributeInstance instance = entity.getAttribute(attribute);
      return instance == null ? 0.0 : instance.getBaseValue();
   }

   public static void modifyAttribute(@NonNull LivingEntity entity, @NonNull Attribute attribute, @NonNull Consumer<AttributeInstance> consumer) {
      AttributeInstance instance = entity.getAttribute(attribute);
      if (instance != null) {
         consumer.accept(instance);
      }
   }

   public static double getMaxHealth(@NonNull LivingEntity entity) {
      return getAttributeValue(entity, Attribute.MAX_HEALTH);
   }

   public static void addHealth(@NonNull LivingEntity entity, double amount) {
      setHealth(entity, entity.getHealth() + Math.abs(amount));
   }

   public static void removeHealth(@NonNull LivingEntity entity, double amount) {
      setHealth(entity, entity.getHealth() - Math.abs(amount));
   }

   public static void setHealth(@NonNull LivingEntity entity, double value) {
      double maxHealth = getMaxHealth(entity);
      double health = Math.clamp(value, 0.0, maxHealth);
      entity.setHealth(health);
   }

   @Nullable
   public static ItemStack getItemInSlot(@NonNull LivingEntity entity, @NonNull EquipmentSlot slot) {
      if (entity instanceof Player player) {
         return player.getInventory().getItem(slot);
      } else {
         EntityEquipment equipment = entity.getEquipment();
         return equipment == null ? null : equipment.getItem(slot);
      }
   }

   @NonNull
   public static Map<EquipmentSlot, ItemStack> getEquippedItems(@NonNull LivingEntity entity) {
      return getEquippedItems(entity, EQUIPMENT_SLOTS);
   }

   @NonNull
   public static Map<EquipmentSlot, ItemStack> getEquippedItems(@NonNull LivingEntity entity, EquipmentSlot... slots) {
      EntityEquipment equipment = entity.getEquipment();
      if (equipment == null) {
         return Collections.emptyMap();
      } else {
         Map<EquipmentSlot, ItemStack> map = new HashMap<>();

         for (EquipmentSlot slot : slots) {
            if (!slot.name().equalsIgnoreCase("BODY")) {
               map.put(slot, equipment.getItem(slot));
            }
         }

         return map;
      }
   }

   @NonNull
   public static Map<EquipmentSlot, ItemStack> getEquippedHands(@NonNull LivingEntity entity) {
      return getEquippedItems(entity, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
   }

   @NonNull
   public static Map<EquipmentSlot, ItemStack> getEquippedArmor(@NonNull LivingEntity entity) {
      return getEquippedItems(entity, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
   }

   @Nullable
   public static BlockFace getDirection(@NonNull Entity entity) {
      float yaw = Math.round(entity.getLocation().getYaw() / 90.0F);
      if (yaw == -4.0F || yaw == 0.0F || yaw == 4.0F) {
         return BlockFace.SOUTH;
      } else if (yaw == -1.0F || yaw == 3.0F) {
         return BlockFace.EAST;
      } else if (yaw == -2.0F || yaw == 2.0F) {
         return BlockFace.NORTH;
      } else {
         return yaw != -3.0F && yaw != 1.0F ? null : BlockFace.WEST;
      }
   }
}
