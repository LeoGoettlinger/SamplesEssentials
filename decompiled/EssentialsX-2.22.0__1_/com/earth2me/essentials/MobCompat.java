package com.earth2me.essentials;

import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.RegistryUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import net.ess3.nms.refl.ReflUtil;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.AbstractNautilus;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Camel;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Llama;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.ZombieNautilus;
import org.bukkit.entity.Llama.Color;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Panda.Gene;
import org.bukkit.entity.Parrot.Variant;
import org.bukkit.entity.TropicalFish.Pattern;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;

public final class MobCompat {
   public static final Class<?> RAIDER = ReflUtil.getClassCached("org.bukkit.entity.Raider");
   private static final Class<?> COW = ReflUtil.getClassCached("org.bukkit.entity.Cow");
   private static final Class<?> COW_VARIANT = ReflUtil.getClassCached("org.bukkit.entity.Cow$Variant");
   private static final MethodHandle COW_VARIANT_HANDLE;
   public static final EntityType LLAMA;
   public static final EntityType PARROT;
   public static final EntityType TROPICAL_FISH;
   public static final EntityType PANDA;
   public static final EntityType TRADER_LLAMA;
   public static final EntityType SHULKER;
   public static final EntityType STRAY;
   public static final EntityType FOX;
   public static final EntityType PHANTOM;
   public static final EntityType AXOLOTL;
   public static final EntityType GOAT;
   public static final EntityType FROG;
   public static final EntityType CAMEL;
   public static final EntityType SALMON;
   public static final EntityType CAMEL_HUSK;
   public static final EntityType NAUTILUS;
   public static final EntityType ZOMBIE_NAUTILUS;
   public static final EntityType CAT;
   public static final EntityType ZOMBIFIED_PIGLIN;
   public static final EntityType MOOSHROOM;
   public static final EntityType SNOW_GOLEM;
   public static final EntityType CHEST_MINECART;
   public static final EntityType FURNACE_MINECART;
   public static final EntityType TNT_MINECART;
   public static final EntityType HOPPER_MINECART;
   public static final EntityType SPAWNER_MINECART;
   public static final EntityType END_CRYSTAL;
   public static final EntityType FIREWORK_ROCKET;
   public static final EntityType OAK_BOAT;

   private MobCompat() {
   }

   public static void setCatType(final Entity entity, final MobCompat.CatType type) {
      if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_14_R01)) {
         ((Ocelot)entity).setCatType(Type.valueOf(type.ocelotTypeName));
      } else {
         Class cat = ReflUtil.getClassCached("org.bukkit.entity.Cat");
         Class catType = ReflUtil.getClassCached("org.bukkit.entity.Cat$Type");
         Method setCatType = ReflUtil.getMethodCached(cat, "setCatType", catType);

         try {
            setCatType.invoke(entity, EnumUtil.valueOf(catType, type.catTypeName));
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }
   }

   public static void setVillagerProfession(final Entity entity, final MobCompat.VillagerProfession profession) {
      if (entity instanceof Villager) {
         Villager villager = (Villager)entity;
         villager.setProfession(profession.asEnum());
         if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_14_R01)) {
            Class villagerCareer = ReflUtil.getClassCached("org.bukkit.entity.Villager$Career");
            Method setCareer = ReflUtil.getMethodCached(Villager.class, "setCareer", villagerCareer);

            try {
               setCareer.invoke(entity, EnumUtil.valueOf(villagerCareer, profession.oldCareer));
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         }
      }
   }

   public static void setVillagerType(final Entity entity, final String type) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_14_R01)) {
         if (entity instanceof Villager) {
            ((Villager)entity).setVillagerType(org.bukkit.entity.Villager.Type.valueOf(type));
         }
      }
   }

   public static void setLlamaColor(final Entity entity, final String color) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_11_R01)) {
         if (entity instanceof Llama) {
            ((Llama)entity).setColor(Color.valueOf(color));
         }
      }
   }

   public static void setParrotVariant(final Entity entity, final String variant) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_12_0_R01)) {
         if (entity instanceof Parrot) {
            ((Parrot)entity).setVariant(Variant.valueOf(variant));
         }
      }
   }

   public static void setTropicalFishPattern(final Entity entity, final String pattern) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_12_0_R01)) {
         if (entity instanceof TropicalFish) {
            ((TropicalFish)entity).setPattern(Pattern.valueOf(pattern));
         }
      }
   }

   public static void setMooshroomVariant(final Entity entity, final String variant) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_14_R01)) {
         if (entity instanceof MushroomCow) {
            ((MushroomCow)entity).setVariant(org.bukkit.entity.MushroomCow.Variant.valueOf(variant));
         }
      }
   }

   public static void setPandaGene(final Entity entity, final String gene, final boolean mainGene) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_14_R01)) {
         if (entity instanceof Panda) {
            Panda panda = (Panda)entity;
            Gene pandaGene = Gene.valueOf(gene);
            if (mainGene) {
               panda.setMainGene(pandaGene);
            } else {
               panda.setHiddenGene(pandaGene);
            }
         }
      }
   }

   public static void setFoxType(final Entity entity, final String type) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_14_R01)) {
         if (entity instanceof Fox) {
            ((Fox)entity).setFoxType(org.bukkit.entity.Fox.Type.valueOf(type));
         }
      }
   }

   public static void setAxolotlVariant(final Entity entity, final String variant) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_17_R01)) {
         if (entity instanceof Axolotl) {
            ((Axolotl)entity).setVariant(org.bukkit.entity.Axolotl.Variant.valueOf(variant));
         }
      }
   }

   public static void setFrogVariant(final Entity entity, final String variant) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_19_R01)) {
         if (entity instanceof Frog) {
            ((Frog)entity).setVariant(org.bukkit.entity.Frog.Variant.valueOf(variant));
         }
      }
   }

   public static void setBoatVariant(final Entity entity, final MobCompat.BoatVariant variant) {
      if (!VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_21_3_R01)
         && !VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_9_R01)) {
         if (entity instanceof Boat) {
            Boat boat = (Boat)entity;
            if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_19_R01)) {
               boat.setWoodType(TreeSpecies.valueOf(variant.getTreeSpecies()));
            } else {
               boat.setBoatType(org.bukkit.entity.Boat.Type.valueOf(variant.getBoatType()));
            }
         }
      }
   }

   public static void setCamelSaddle(final Entity entity, final Player target) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_20_1_R01)) {
         if (entity instanceof Camel) {
            Camel camel = (Camel)entity;
            camel.setTamed(true);
            camel.setOwner(target);
            camel.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
         }
      }
   }

   public static void setWolfVariant(final Entity entity, final String variant) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_20_6_R01)) {
         if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            wolf.setVariant(RegistryUtil.valueOf(org.bukkit.entity.Wolf.Variant.class, variant));
         }
      }
   }

   public static void setSalmonSize(Entity spawned, String s) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_21_3_R01)) {
         if (spawned instanceof Salmon) {
            ((Salmon)spawned).setVariant(org.bukkit.entity.Salmon.Variant.valueOf(s));
         }
      }
   }

   public static void setCowVariant(final Entity spawned, final String variant) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_21_5_R01) && COW_VARIANT_HANDLE != null) {
         if (spawned instanceof Cow) {
            try {
               COW_VARIANT_HANDLE.invoke((Entity)spawned, (Object)RegistryUtil.valueOf(COW_VARIANT, variant));
            } catch (Throwable var3) {
            }
         }
      }
   }

   public static void setChickenVariant(final Entity spawned, final String variant) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_21_5_R01)) {
         if (spawned instanceof Chicken) {
            ((Chicken)spawned).setVariant(RegistryUtil.valueOf(org.bukkit.entity.Chicken.Variant.class, variant));
         }
      }
   }

   public static void setPigVariant(final Entity spawned, final String variant) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_21_5_R01)) {
         if (spawned instanceof Pig) {
            ((Pig)spawned).setVariant(RegistryUtil.valueOf(org.bukkit.entity.Pig.Variant.class, variant));
         }
      }
   }

   public static void setZombieNautilusVariant(final Entity spawned, final String variant) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_21_11_R01)) {
         if (spawned instanceof ZombieNautilus) {
            ((ZombieNautilus)spawned).setVariant(RegistryUtil.valueOf(org.bukkit.entity.ZombieNautilus.Variant.class, variant));
         }
      }
   }

   public static void setNautilusSaddle(final Entity entity, final Player target) {
      if (!VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_21_11_R01)) {
         if (entity instanceof AbstractNautilus) {
            AbstractNautilus nautilus = (AbstractNautilus)entity;
            nautilus.setTamed(true);
            nautilus.setOwner(target);
            nautilus.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
         }
      }
   }

   static {
      MethodHandle handle = null;

      try {
         handle = MethodHandles.lookup().findVirtual(COW, "setVariant", MethodType.methodType(void.class, COW_VARIANT));
      } catch (Throwable var2) {
      }

      COW_VARIANT_HANDLE = handle;
      LLAMA = EnumUtil.getEntityType("LLAMA");
      PARROT = EnumUtil.getEntityType("PARROT");
      TROPICAL_FISH = EnumUtil.getEntityType("TROPICAL_FISH");
      PANDA = EnumUtil.getEntityType("PANDA");
      TRADER_LLAMA = EnumUtil.getEntityType("TRADER_LLAMA");
      SHULKER = EnumUtil.getEntityType("SHULKER");
      STRAY = EnumUtil.getEntityType("STRAY");
      FOX = EnumUtil.getEntityType("FOX");
      PHANTOM = EnumUtil.getEntityType("PHANTOM");
      AXOLOTL = EnumUtil.getEntityType("AXOLOTL");
      GOAT = EnumUtil.getEntityType("GOAT");
      FROG = EnumUtil.getEntityType("FROG");
      CAMEL = EnumUtil.getEntityType("CAMEL");
      SALMON = EnumUtil.getEntityType("SALMON");
      CAMEL_HUSK = EnumUtil.getEntityType("CAMEL_HUSK");
      NAUTILUS = EnumUtil.getEntityType("NAUTILUS");
      ZOMBIE_NAUTILUS = EnumUtil.getEntityType("ZOMBIE_NAUTILUS");
      CAT = EnumUtil.getEntityType("CAT", "OCELOT");
      ZOMBIFIED_PIGLIN = EnumUtil.getEntityType("ZOMBIFIED_PIGLIN", "PIG_ZOMBIE");
      MOOSHROOM = EnumUtil.getEntityType("MOOSHROOM", "MUSHROOM_COW");
      SNOW_GOLEM = EnumUtil.getEntityType("SNOW_GOLEM", "SNOWMAN");
      CHEST_MINECART = EnumUtil.getEntityType("CHEST_MINECART", "MINECART_CHEST");
      FURNACE_MINECART = EnumUtil.getEntityType("FURNACE_MINECART", "MINECART_FURNACE");
      TNT_MINECART = EnumUtil.getEntityType("TNT_MINECART", "MINECART_TNT");
      HOPPER_MINECART = EnumUtil.getEntityType("HOPPER_MINECART", "MINECART_HOPPER");
      SPAWNER_MINECART = EnumUtil.getEntityType("SPAWNER_MINECART", "MINECART_MOB_SPAWNER");
      END_CRYSTAL = EnumUtil.getEntityType("END_CRYSTAL", "ENDER_CRYSTAL");
      FIREWORK_ROCKET = EnumUtil.getEntityType("FIREWORK_ROCKET", "FIREWORK");
      OAK_BOAT = EnumUtil.getEntityType("BOAT", "OAK_BOAT");
   }

   public static enum BoatVariant {
      ACACIA("ACACIA", "ACACIA"),
      BIRCH("BIRCH", "BIRCH"),
      DARKOAK("DARK_OAK", "DARK_OAK"),
      GENERIC("GENERIC", "OAK"),
      JUNGLE("JUNGLE", "JUNGLE"),
      REDWOOD("REDWOOD", "SPRUCE"),
      OAK("GENERIC", "OAK"),
      SPRUCE("REDWOOD", "SPRUCE"),
      MANGROVE("GENERIC", "MANGROVE");

      private final String treeSpecies;
      private final String boatType;

      private BoatVariant(final String treeSpecies, final String boatType) {
         this.treeSpecies = treeSpecies;
         this.boatType = boatType;
      }

      public String getTreeSpecies() {
         return this.treeSpecies;
      }

      public String getBoatType() {
         return this.boatType;
      }
   }

   public static enum CatType {
      SIAMESE("SIAMESE", "SIAMESE_CAT"),
      WHITE("WHITE", "SIAMESE_CAT"),
      RED("RED", "RED_CAT"),
      TABBY("TABBY", "RED_CAT"),
      TUXEDO("BLACK", "BLACK_CAT"),
      BRITISH_SHORTHAIR("BRITISH_SHORTHAIR", "SIAMESE_CAT"),
      CALICO("CALICO", "RED_CAT"),
      PERSIAN("PERSIAN", "RED_CAT"),
      RAGDOLL("RAGDOLL", "SIAMESE_CAT"),
      JELLIE("JELLIE", "SIAMESE_CAT"),
      BLACK("ALL_BLACK", "BLACK_CAT");

      private final String catTypeName;
      private final String ocelotTypeName;

      private CatType(final String catTypeName, final String ocelotTypeName) {
         this.catTypeName = catTypeName;
         this.ocelotTypeName = ocelotTypeName;
      }
   }

   public static enum VillagerProfession {
      NONE("FARMER", "FARMER", "NONE"),
      ARMORER("BLACKSMITH", "ARMORER"),
      BUTCHER("FARMER", "BUTCHER"),
      CARTOGRAPHER("LIBRARIAN", "CARTOGRAPHER"),
      CLERIC("PRIEST", "CLERIC"),
      FARMER("FARMER", "FARMER"),
      FISHERMAN("FARMER", "FISHERMAN"),
      FLETCHER("FARMER", "FLETCHER"),
      LEATHERWORKER("BUTCHER", "LEATHERWORKER"),
      LIBRARIAN("LIBRARIAN", "LIBRARIAN"),
      MASON(null, null, "MASON"),
      NITWIT("NITWIT", "NITWIT"),
      SHEPHERD("FARMER", "SHEPHERD"),
      TOOLSMITH("BLACKSMITH", "TOOL_SMITH", "TOOLSMITH"),
      WEAPONSMITH("BLACKSMITH", "WEAPON_SMITH", "WEAPONSMITH");

      private final String oldProfession;
      private final String oldCareer;
      private final String newProfession;

      private VillagerProfession(final String oldProfession, final String career) {
         this.oldProfession = oldProfession;
         this.oldCareer = career;
         this.newProfession = career;
      }

      private VillagerProfession(final String oldProfession, final String oldCareer, final String newProfession) {
         this.oldProfession = oldProfession;
         this.oldCareer = oldCareer;
         this.newProfession = newProfession;
      }

      private Profession asEnum() {
         return RegistryUtil.valueOf(Profession.class, this.newProfession, this.oldProfession);
      }
   }
}
