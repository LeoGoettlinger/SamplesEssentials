package net.coreprotect.bukkit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.coreprotect.model.BlockGroup;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Wolf.Variant;
import org.bukkit.event.inventory.InventoryType;

public class Bukkit_v1_21 extends Bukkit_v1_20 implements BukkitInterface {
   public static Set<Material> COPPER_CHESTS = new HashSet<>(Arrays.asList());

   public Bukkit_v1_21() {
      this.initializeBlockGroups();
      this.initializeTrapdoorBlocks();
      BlockGroup.INTERACT_BLOCKS.addAll(this.copperChestMaterials());
      BlockGroup.CONTAINERS.addAll(this.copperChestMaterials());
      BlockGroup.UPDATE_STATE.addAll(this.copperChestMaterials());
   }

   private void initializeBlockGroups() {
      this.addToBlockGroupIfMissing(Material.CRAFTER, BlockGroup.CONTAINERS);
      this.addToBlockGroupIfMissing(Material.CRAFTER, BlockGroup.INTERACT_BLOCKS);
      this.addToBlockGroupIfMissing(Material.CRAFTER, BlockGroup.UPDATE_STATE);
   }

   private void initializeTrapdoorBlocks() {
      for (Material value : Tag.TRAPDOORS.getValues()) {
         if (value != Material.IRON_TRAPDOOR) {
            this.addToBlockGroupIfMissing(value, BlockGroup.INTERACT_BLOCKS);
            this.addToBlockGroupIfMissing(value, BlockGroup.SAFE_INTERACT_BLOCKS);
         }
      }
   }

   private void addToBlockGroupIfMissing(Material block, Set<Material> group) {
      if (!group.contains(block)) {
         group.add(block);
      }
   }

   @Override
   public EntityType getEntityType(Material material) {
      switch (material) {
         case END_CRYSTAL:
            return EntityType.valueOf("END_CRYSTAL");
         default:
            return EntityType.UNKNOWN;
      }
   }

   @Override
   public Object getRegistryKey(Object value) {
      return ((Keyed)value).getKey().toString();
   }

   @Override
   public Object getRegistryValue(String key, Object tClass) {
      NamespacedKey namespacedKey = NamespacedKey.fromString(key);
      return Bukkit.getRegistry((Class)tClass).get(namespacedKey);
   }

   @Override
   public void getWolfVariant(Wolf wolf, List<Object> info) {
      info.add(this.getRegistryKey(wolf.getVariant()));
   }

   @Override
   public void setWolfVariant(Wolf wolf, Object value) {
      if (value instanceof String) {
         value = this.getRegistryValue((String)value, Variant.class);
      }

      Variant variant = (Variant)value;
      wolf.setVariant(variant);
   }

   @Override
   public boolean isCrafter(InventoryType type) {
      return type == InventoryType.CRAFTER;
   }

   @Override
   public boolean isCopperChest(Material material) {
      return COPPER_CHESTS.contains(material) && material != Material.CHEST;
   }

   @Override
   public Set<Material> copperChestMaterials() {
      if (COPPER_CHESTS.isEmpty()) {
         Material copperChest = Material.getMaterial("COPPER_CHEST");
         if (copperChest == null) {
            COPPER_CHESTS.add(Material.CHEST);
         } else {
            COPPER_CHESTS.add(Material.getMaterial("COPPER_CHEST"));
            COPPER_CHESTS.add(Material.getMaterial("EXPOSED_COPPER_CHEST"));
            COPPER_CHESTS.add(Material.getMaterial("WEATHERED_COPPER_CHEST"));
            COPPER_CHESTS.add(Material.getMaterial("OXIDIZED_COPPER_CHEST"));
            COPPER_CHESTS.add(Material.getMaterial("WAXED_COPPER_CHEST"));
            COPPER_CHESTS.add(Material.getMaterial("WAXED_EXPOSED_COPPER_CHEST"));
            COPPER_CHESTS.add(Material.getMaterial("WAXED_WEATHERED_COPPER_CHEST"));
            COPPER_CHESTS.add(Material.getMaterial("WAXED_OXIDIZED_COPPER_CHEST"));
         }
      }

      return COPPER_CHESTS;
   }
}
