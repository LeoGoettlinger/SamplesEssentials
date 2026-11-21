package com.earth2me.essentials;

import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.LocationUtil;
import com.earth2me.essentials.utils.StringUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.ess3.api.TranslatableException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public final class SpawnMob {
   private static final Material GOLDEN_HELMET = EnumUtil.getMaterial("GOLDEN_HELMET", "GOLD_HELMET");
   private static final Material GOLDEN_CHESTPLATE = EnumUtil.getMaterial("GOLDEN_CHESTPLATE", "GOLD_CHESTPLATE");
   private static final Material GOLDEN_LEGGINGS = EnumUtil.getMaterial("GOLDEN_LEGGINGS", "GOLD_LEGGINGS");
   private static final Material GOLDEN_BOOTS = EnumUtil.getMaterial("GOLDEN_BOOTS", "GOLD_BOOTS");
   private static final Material GOLDEN_SWORD = EnumUtil.getMaterial("GOLDEN_SWORD", "GOLD_SWORD");

   private SpawnMob() {
   }

   public static String mobList(final User user) {
      Set<String> mobList = Mob.getMobList();
      Set<String> availableList = new HashSet<>();

      for (String mob : mobList) {
         if (user.isAuthorized("essentials.spawnmob." + mob.toLowerCase(Locale.ENGLISH))) {
            availableList.add(mob);
         }
      }

      if (availableList.isEmpty()) {
         availableList.add(user.playerTl("none"));
      }

      return StringUtil.joinList(availableList);
   }

   public static List<String> mobParts(final String mobString) {
      String[] mobParts = mobString.split(",");
      List<String> mobs = new ArrayList<>();

      for (String mobPart : mobParts) {
         String[] mobDatas = mobPart.split(":");
         mobs.add(mobDatas[0]);
      }

      return mobs;
   }

   public static List<String> mobData(final String mobString) {
      String[] mobParts = mobString.split(",");
      List<String> mobData = new ArrayList<>();

      for (String mobPart : mobParts) {
         String[] mobDatas = mobPart.split(":");
         if (mobDatas.length == 1) {
            if (mobPart.contains(":")) {
               mobData.add("");
            } else {
               mobData.add(null);
            }
         } else {
            mobData.add(mobDatas[1]);
         }
      }

      return mobData;
   }

   public static void spawnmob(
      final net.ess3.api.IEssentials ess, final Server server, final User user, final List<String> parts, final List<String> data, final int mobCount
   ) throws Exception {
      Block block = LocationUtil.getTarget(user.getBase()).getBlock();
      if (block == null) {
         throw new TranslatableException("unableToSpawnMob");
      } else {
         spawnmob(ess, server, user.getSource(), user, block.getLocation(), parts, data, mobCount);
      }
   }

   public static void spawnmob(
      final net.ess3.api.IEssentials ess,
      final Server server,
      final CommandSource sender,
      final User target,
      final List<String> parts,
      final List<String> data,
      final int mobCount
   ) throws Exception {
      spawnmob(ess, server, sender, target, target.getLocation(), parts, data, mobCount);
   }

   public static void spawnmob(
      final net.ess3.api.IEssentials ess,
      final Server server,
      final CommandSource sender,
      final User target,
      final Location loc,
      final List<String> parts,
      final List<String> data,
      int mobCount
   ) throws Exception {
      Location sloc = LocationUtil.getSafeDestination(ess, loc);

      for (String part : parts) {
         Mob mob = Mob.fromName(part);
         checkSpawnable(ess, sender, mob);
      }

      int serverLimit = ess.getSettings().getSpawnMobLimit();
      int effectiveLimit = serverLimit / parts.size();
      if (effectiveLimit < 1) {
         effectiveLimit = 1;

         while (parts.size() > serverLimit) {
            parts.remove(serverLimit);
         }
      }

      if (mobCount > effectiveLimit) {
         mobCount = effectiveLimit;
         sender.sendTl("mobSpawnLimit");
      }

      Mob mob = Mob.fromName(parts.get(0));

      try {
         for (int i = 0; i < mobCount; i++) {
            spawnMob(ess, server, sender, target, sloc, parts, data);
         }

         sender.sendMessage(mobCount * parts.size() + " " + mob.name.toLowerCase(Locale.ENGLISH) + mob.suffix + " " + sender.tl("spawned"));
      } catch (Mob.MobException var13) {
         throw new TranslatableException(var13, "unableToSpawnMob");
      } catch (NumberFormatException var14) {
         throw new TranslatableException(var14, "numberRequired");
      } catch (NullPointerException var15) {
         throw new TranslatableException(var15, "soloMob");
      }
   }

   private static void spawnMob(
      final net.ess3.api.IEssentials ess,
      final Server server,
      final CommandSource sender,
      final User target,
      final Location sloc,
      final List<String> parts,
      final List<String> data
   ) throws Exception {
      Entity spawnedMob = null;

      for (int i = 0; i < parts.size(); i++) {
         if (i == 0) {
            Mob mob = Mob.fromName(parts.get(i));
            spawnedMob = mob.spawn(sloc.getWorld(), server, sloc);
            defaultMobData(mob.getType(), spawnedMob);
            if (data.get(i) != null) {
               changeMobData(sender, mob.getType(), spawnedMob, data.get(i).toLowerCase(Locale.ENGLISH), target);
            }
         }

         int next = i + 1;
         if (next < parts.size()) {
            Mob mMob = Mob.fromName(parts.get(next));
            Entity spawnedMount = mMob.spawn(sloc.getWorld(), server, sloc);
            defaultMobData(mMob.getType(), spawnedMount);
            if (data.get(next) != null) {
               changeMobData(sender, mMob.getType(), spawnedMount, data.get(next).toLowerCase(Locale.ENGLISH), target);
            }

            spawnedMob.setPassenger(spawnedMount);
            spawnedMob = spawnedMount;
         }
      }
   }

   private static void checkSpawnable(final net.ess3.api.IEssentials ess, final CommandSource sender, final Mob mob) throws Exception {
      if (mob != null && mob.getType() != null) {
         if (ess.getSettings().getProtectPreventSpawn(mob.getType().toString().toLowerCase(Locale.ENGLISH))) {
            throw new TranslatableException("disabledToSpawnMob");
         } else if (sender.isPlayer() && !ess.getUser(sender.getPlayer()).isAuthorized("essentials.spawnmob." + mob.name.toLowerCase(Locale.ENGLISH))) {
            throw new TranslatableException("noPermToSpawnMob");
         }
      } else {
         throw new TranslatableException("invalidMob");
      }
   }

   private static void changeMobData(final CommandSource sender, final EntityType type, final Entity spawned, final String inputData, final User target) throws Exception {
      String data = inputData;
      if (inputData.isEmpty()) {
         sender.sendTl("mobDataList", StringUtil.joinList(MobData.getValidHelp(spawned)));
      }

      if (spawned instanceof Zombie) {
         ((Zombie)spawned).setBaby(false);
      } else if (spawned instanceof Ageable) {
         ((Ageable)spawned).setAdult();
      }

      if ((spawned instanceof Zombie || type == EntityType.SKELETON) && (inputData.contains("armor") || inputData.contains("armour"))) {
         EntityEquipment invent = ((LivingEntity)spawned).getEquipment();
         if (!inputData.contains("noarmor") && !inputData.contains("noarmour")) {
            if (inputData.contains("netherite") && VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_16_1_R01)) {
               invent.setBoots(new ItemStack(Material.NETHERITE_BOOTS, 1));
               invent.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS, 1));
               invent.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE, 1));
               invent.setHelmet(new ItemStack(Material.NETHERITE_HELMET, 1));
            } else if (inputData.contains("diamond")) {
               invent.setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1));
               invent.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
               invent.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
               invent.setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1));
            } else if (inputData.contains("gold")) {
               invent.setBoots(new ItemStack(GOLDEN_BOOTS, 1));
               invent.setLeggings(new ItemStack(GOLDEN_LEGGINGS, 1));
               invent.setChestplate(new ItemStack(GOLDEN_CHESTPLATE, 1));
               invent.setHelmet(new ItemStack(GOLDEN_HELMET, 1));
            } else if (inputData.contains("leather")) {
               invent.setBoots(new ItemStack(Material.LEATHER_BOOTS, 1));
               invent.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS, 1));
               invent.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
               invent.setHelmet(new ItemStack(Material.LEATHER_HELMET, 1));
            } else if (inputData.contains("iron")) {
               invent.setBoots(new ItemStack(Material.IRON_BOOTS, 1));
               invent.setLeggings(new ItemStack(Material.IRON_LEGGINGS, 1));
               invent.setChestplate(new ItemStack(Material.IRON_CHESTPLATE, 1));
               invent.setHelmet(new ItemStack(Material.IRON_HELMET, 1));
            }
         } else {
            invent.clear();
         }

         invent.setBootsDropChance(0.0F);
         invent.setLeggingsDropChance(0.0F);
         invent.setChestplateDropChance(0.0F);
         invent.setHelmetDropChance(0.0F);
      }

      for (MobData newData = MobData.fromData(spawned, inputData); newData != null; newData = MobData.fromData(spawned, data)) {
         newData.setData(spawned, target.getBase(), data);
         data = data.replace(newData.getMatched(), "");
      }
   }

   private static void defaultMobData(final EntityType type, final Entity spawned) {
      if (type == EntityType.SKELETON) {
         EntityEquipment invent = ((LivingEntity)spawned).getEquipment();
         Inventories.setItemInMainHand(invent, new ItemStack(Material.BOW, 1));
         Inventories.setItemInMainHandDropChance(invent, 0.1F);
      }

      if (type == MobCompat.ZOMBIFIED_PIGLIN) {
         PigZombie zombie = (PigZombie)spawned;
         setVillager(zombie, false);
         EntityEquipment invent = zombie.getEquipment();
         Inventories.setItemInMainHand(invent, new ItemStack(GOLDEN_SWORD, 1));
         Inventories.setItemInMainHandDropChance(invent, 0.1F);
      }

      if (type == EntityType.ZOMBIE) {
         Zombie zombie = (Zombie)spawned;
         setVillager(zombie, false);
      }

      if (type == EntityType.HORSE) {
         ((Horse)spawned).setJumpStrength(1.2);
      }
   }

   private static void setVillager(final Zombie zombie, final boolean villager) {
      try {
         zombie.setVillager(villager);
      } catch (Exception var3) {
      }
   }
}
