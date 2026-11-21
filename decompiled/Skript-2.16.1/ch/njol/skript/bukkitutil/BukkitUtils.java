package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.util.PaperUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class BukkitUtils {
   private static final BiMap<EquipmentSlot, Integer> BUKKIT_EQUIPMENT_SLOT_INDICES = HashBiMap.create();

   public static boolean registryExists(String registry) {
      return Skript.classExists("org.bukkit.Registry") && Skript.fieldExists(Registry.class, registry);
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   @Nullable
   public static Registry<PotionEffectType> getPotionEffectTypeRegistry() {
      if (registryExists("MOB_EFFECT")) {
         return Registry.MOB_EFFECT;
      } else {
         return registryExists("EFFECT") ? Registry.EFFECT : null;
      }
   }

   public static Integer getEquipmentSlotIndex(EquipmentSlot equipmentSlot) {
      return (Integer)BUKKIT_EQUIPMENT_SLOT_INDICES.get(equipmentSlot);
   }

   public static EquipmentSlot getEquipmentSlotFromIndex(int slotIndex) {
      return (EquipmentSlot)BUKKIT_EQUIPMENT_SLOT_INDICES.inverse().get(slotIndex);
   }

   @Nullable
   public static <R extends Keyed> RegistryClassInfo<?> getRegistryClassInfo(String classPath, String registryName, String codeName, String languageNode) {
      if (!Skript.classExists(classPath)) {
         return null;
      } else {
         Registry<R> registry = null;
         if (registryExists(registryName)) {
            try {
               registry = (Registry<R>)Registry.class.getField(registryName).get(null);
            } catch (IllegalAccessException | NoSuchFieldException var8) {
               throw new RuntimeException(var8);
            }
         } else if (PaperUtils.registryExists(registryName)) {
            registry = PaperUtils.getBukkitRegistry(registryName);
         }

         if (registry != null) {
            Class<R> registryClass;
            try {
               registryClass = (Class<R>)Class.forName(classPath);
            } catch (ClassNotFoundException var7) {
               Skript.debug("Could not retrieve the class with the path: '" + classPath + "'.");
               throw new RuntimeException(var7);
            }

            return new RegistryClassInfo<>(registryClass, registry, codeName, languageNode);
         } else {
            Skript.debug("There were no registries found for '" + registryName + "'.");
            return null;
         }
      }
   }

   static {
      BUKKIT_EQUIPMENT_SLOT_INDICES.put(EquipmentSlot.FEET, 36);
      BUKKIT_EQUIPMENT_SLOT_INDICES.put(EquipmentSlot.LEGS, 37);
      BUKKIT_EQUIPMENT_SLOT_INDICES.put(EquipmentSlot.CHEST, 38);
      BUKKIT_EQUIPMENT_SLOT_INDICES.put(EquipmentSlot.HEAD, 39);
      BUKKIT_EQUIPMENT_SLOT_INDICES.put(EquipmentSlot.OFF_HAND, 40);
   }
}
