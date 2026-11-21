package com.earth2me.essentials;

import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.EnumUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public enum Mob {
   CHICKEN("Chicken", Mob.Enemies.FRIENDLY, EntityType.CHICKEN),
   COW("Cow", Mob.Enemies.FRIENDLY, EntityType.COW),
   CREEPER("Creeper", Mob.Enemies.ENEMY, EntityType.CREEPER),
   GHAST("Ghast", Mob.Enemies.ENEMY, EntityType.GHAST),
   GIANT("Giant", Mob.Enemies.ENEMY, EntityType.GIANT),
   HORSE("Horse", Mob.Enemies.FRIENDLY, EntityType.HORSE),
   PIG("Pig", Mob.Enemies.FRIENDLY, EntityType.PIG),
   PIGZOMB("PigZombie", Mob.Enemies.NEUTRAL, MobCompat.ZOMBIFIED_PIGLIN),
   ZOMBIFIED_PIGLIN("ZombifiedPiglin", Mob.Enemies.NEUTRAL, MobCompat.ZOMBIFIED_PIGLIN),
   SHEEP("Sheep", Mob.Enemies.FRIENDLY, "", EntityType.SHEEP),
   SKELETON("Skeleton", Mob.Enemies.ENEMY, EntityType.SKELETON),
   SLIME("Slime", Mob.Enemies.ENEMY, EntityType.SLIME),
   SPIDER("Spider", Mob.Enemies.ENEMY, EntityType.SPIDER),
   SQUID("Squid", Mob.Enemies.FRIENDLY, EntityType.SQUID),
   ZOMBIE("Zombie", Mob.Enemies.ENEMY, EntityType.ZOMBIE),
   WOLF("Wolf", Mob.Enemies.NEUTRAL, "", EntityType.WOLF),
   CAVESPIDER("CaveSpider", Mob.Enemies.ENEMY, EntityType.CAVE_SPIDER),
   ENDERMAN("Enderman", Mob.Enemies.ENEMY, "", EntityType.ENDERMAN),
   SILVERFISH("Silverfish", Mob.Enemies.ENEMY, "", EntityType.SILVERFISH),
   ENDERDRAGON("EnderDragon", Mob.Enemies.ENEMY, EntityType.ENDER_DRAGON),
   VILLAGER("Villager", Mob.Enemies.FRIENDLY, EntityType.VILLAGER),
   BLAZE("Blaze", Mob.Enemies.ENEMY, EntityType.BLAZE),
   MUSHROOMCOW("MushroomCow", Mob.Enemies.FRIENDLY, MobCompat.MOOSHROOM),
   MAGMACUBE("MagmaCube", Mob.Enemies.ENEMY, EntityType.MAGMA_CUBE),
   SNOWMAN("Snowman", Mob.Enemies.FRIENDLY, "", MobCompat.SNOW_GOLEM),
   OCELOT("Ocelot", Mob.Enemies.NEUTRAL, EntityType.OCELOT),
   IRONGOLEM("IronGolem", Mob.Enemies.NEUTRAL, EntityType.IRON_GOLEM),
   WITHER("Wither", Mob.Enemies.ENEMY, EntityType.WITHER),
   BAT("Bat", Mob.Enemies.FRIENDLY, EntityType.BAT),
   WITCH("Witch", Mob.Enemies.ENEMY, EntityType.WITCH),
   BOAT("Boat", Mob.Enemies.NEUTRAL, MobCompat.OAK_BOAT),
   ACACIA_BOAT("AcaciaBoat", Mob.Enemies.NEUTRAL, "ACACIA_BOAT"),
   DARK_OAK_BOAT("DarkOakBoat", Mob.Enemies.NEUTRAL, "DARK_OAK_BOAT"),
   BIRCH_BOAT("BirchBoat", Mob.Enemies.NEUTRAL, "BIRCH_BOAT"),
   JUNGLE_BOAT("JungleBoat", Mob.Enemies.NEUTRAL, "JUNGLE_BOAT"),
   SPRUCE_BOAT("SpruceBoat", Mob.Enemies.NEUTRAL, "SPRUCE_BOAT"),
   MANGROVE_BOAT("MangroveBoat", Mob.Enemies.NEUTRAL, "MANGROVE_BOAT"),
   CHERRY_BOAT("CherryBoat", Mob.Enemies.NEUTRAL, "CHERRY_BOAT"),
   BAMBOO_RAFT("BambooRaft", Mob.Enemies.NEUTRAL, "BAMBOO_RAFT"),
   PALE_OAK_BOAT("PaleOakBoat", Mob.Enemies.NEUTRAL, "PALE_OAK_BOAT"),
   MINECART("Minecart", Mob.Enemies.NEUTRAL, EntityType.MINECART),
   MINECART_CHEST("ChestMinecart", Mob.Enemies.NEUTRAL, MobCompat.CHEST_MINECART),
   MINECART_FURNACE("FurnaceMinecart", Mob.Enemies.NEUTRAL, MobCompat.FURNACE_MINECART),
   MINECART_TNT("TNTMinecart", Mob.Enemies.NEUTRAL, MobCompat.TNT_MINECART),
   MINECART_HOPPER("HopperMinecart", Mob.Enemies.NEUTRAL, MobCompat.HOPPER_MINECART),
   MINECART_MOB_SPAWNER("SpawnerMinecart", Mob.Enemies.NEUTRAL, MobCompat.SPAWNER_MINECART),
   ENDERCRYSTAL("EnderCrystal", Mob.Enemies.NEUTRAL, MobCompat.END_CRYSTAL),
   EXPERIENCEORB("ExperienceOrb", Mob.Enemies.NEUTRAL, "EXPERIENCE_ORB"),
   ARMOR_STAND("ArmorStand", Mob.Enemies.NEUTRAL, "ARMOR_STAND"),
   ENDERMITE("Endermite", Mob.Enemies.ENEMY, "ENDERMITE"),
   GUARDIAN("Guardian", Mob.Enemies.ENEMY, "GUARDIAN"),
   ELDER_GUARDIAN("ElderGuardian", Mob.Enemies.ENEMY, "ELDER_GUARDIAN"),
   RABBIT("Rabbit", Mob.Enemies.FRIENDLY, "RABBIT"),
   SHULKER("Shulker", Mob.Enemies.ENEMY, "SHULKER"),
   POLAR_BEAR("PolarBear", Mob.Enemies.NEUTRAL, "POLAR_BEAR"),
   WITHER_SKELETON("WitherSkeleton", Mob.Enemies.ENEMY, "WITHER_SKELETON"),
   STRAY_SKELETON("StraySkeleton", Mob.Enemies.ENEMY, "STRAY"),
   ZOMBIE_VILLAGER("ZombieVillager", Mob.Enemies.FRIENDLY, "ZOMBIE_VILLAGER"),
   SKELETON_HORSE("SkeletonHorse", Mob.Enemies.FRIENDLY, "SKELETON_HORSE"),
   ZOMBIE_HORSE("ZombieHorse", Mob.Enemies.FRIENDLY, "ZOMBIE_HORSE"),
   DONKEY("Donkey", Mob.Enemies.FRIENDLY, "DONKEY"),
   MULE("Mule", Mob.Enemies.FRIENDLY, "MULE"),
   EVOKER("Evoker", Mob.Enemies.ENEMY, "EVOKER"),
   VEX("Vex", Mob.Enemies.ENEMY, "VEX"),
   VINDICATOR("Vindicator", Mob.Enemies.ENEMY, "VINDICATOR"),
   LLAMA("Llama", Mob.Enemies.NEUTRAL, "LLAMA"),
   HUSK("Husk", Mob.Enemies.ENEMY, "HUSK"),
   ILLUSIONER("Illusioner", Mob.Enemies.ENEMY, "ILLUSIONER"),
   PARROT("Parrot", Mob.Enemies.NEUTRAL, "PARROT"),
   TURTLE("Turtle", Mob.Enemies.NEUTRAL, "TURTLE"),
   PHANTOM("Phantom", Mob.Enemies.ENEMY, "PHANTOM"),
   COD("Cod", Mob.Enemies.NEUTRAL, "", "COD"),
   SALMON("Salmon", Mob.Enemies.NEUTRAL, "", "SALMON"),
   PUFFERFISH("Pufferfish", Mob.Enemies.NEUTRAL, "", "PUFFERFISH"),
   TROPICAL_FISH("TropicalFish", Mob.Enemies.NEUTRAL, "", "TROPICAL_FISH"),
   DROWNED("Drowned", Mob.Enemies.ENEMY, "DROWNED"),
   DOLPHIN("Dolphin", Mob.Enemies.NEUTRAL, "DOLPHIN"),
   CAT("Cat", Mob.Enemies.FRIENDLY, "CAT"),
   FOX("Fox", Mob.Enemies.FRIENDLY, "es", "FOX"),
   PANDA("Panda", Mob.Enemies.NEUTRAL, "PANDA"),
   PILLAGER("Pillager", Mob.Enemies.ENEMY, "PILLAGER"),
   RAVAGER("Ravager", Mob.Enemies.ENEMY, "RAVAGER"),
   TRADER_LLAMA("TraderLlama", Mob.Enemies.FRIENDLY, "TRADER_LLAMA"),
   WANDERING_TRADER("WanderingTrader", Mob.Enemies.FRIENDLY, "WANDERING_TRADER"),
   BEE("Bee", Mob.Enemies.NEUTRAL, "BEE"),
   STRAY("Stray", Mob.Enemies.ENEMY, "STRAY"),
   HOGLIN("Hoglin", Mob.Enemies.ADULT_ENEMY, "HOGLIN"),
   PIGLIN("Piglin", Mob.Enemies.ADULT_ENEMY, "PIGLIN"),
   STRIDER("Strider", Mob.Enemies.FRIENDLY, "STRIDER"),
   ZOGLIN("Zoglin", Mob.Enemies.ENEMY, "ZOGLIN"),
   PIGLIN_BRUTE("PiglinBrute", Mob.Enemies.ADULT_ENEMY, "PIGLIN_BRUTE"),
   AXOLOTL("Axolotl", Mob.Enemies.FRIENDLY, "AXOLOTL"),
   GOAT("Goat", Mob.Enemies.NEUTRAL, "GOAT"),
   GLOW_SQUID("GlowSquid", Mob.Enemies.FRIENDLY, "GLOW_SQUID"),
   ALLAY("Allay", Mob.Enemies.FRIENDLY, "ALLAY"),
   FROG("Frog", Mob.Enemies.FRIENDLY, "FROG"),
   TADPOLE("Tadpole", Mob.Enemies.FRIENDLY, "TADPOLE"),
   WARDEN("Warden", Mob.Enemies.ENEMY, "WARDEN"),
   CHEST_BOAT("ChestBoat", Mob.Enemies.NEUTRAL, "CHEST_BOAT"),
   CAMEL("Camel", Mob.Enemies.FRIENDLY, "CAMEL"),
   SNIFFER("Sniffer", Mob.Enemies.FRIENDLY, "SNIFFER"),
   ARMADILLO("Armadillo", Mob.Enemies.FRIENDLY, "ARMADILLO"),
   BREEZE("Breeze", Mob.Enemies.ENEMY, "BREEZE"),
   BOGGED("Bogged", Mob.Enemies.ENEMY, "BOGGED"),
   CREAKING("Creaking", Mob.Enemies.ENEMY, "CREAKING"),
   HAPPY_GHAST("HappyGhast", Mob.Enemies.FRIENDLY, "HAPPY_GHAST"),
   COPPER_GOLEM("CopperGolem", Mob.Enemies.FRIENDLY, "COPPER_GOLEM"),
   CAMEL_HUSK("CamelHusk", Mob.Enemies.NEUTRAL, "CAMEL_HUSK"),
   NAUTILUS("Nautilus", Mob.Enemies.NEUTRAL, "NAUTILUS"),
   ZOMBIE_NAUTILUS("ZombieNautilus", Mob.Enemies.NEUTRAL, "ZOMBIE_NAUTILUS"),
   PARCHED("Parched", Mob.Enemies.ENEMY, "PARCHED");

   private static final Map<String, Mob> hashMap = new HashMap<>();
   private static final Map<EntityType, Mob> bukkitMap = new HashMap<>();
   public final String name;
   public final Mob.Enemies type;
   private final EntityType bukkitType;
   public String suffix = "s";

   private Mob(final String n, final Mob.Enemies en, final String s, final EntityType type) {
      this.suffix = s;
      this.name = n;
      this.type = en;
      this.bukkitType = type;
   }

   private Mob(final String n, final Mob.Enemies en, final EntityType type) {
      this.name = n;
      this.type = en;
      this.bukkitType = type;
   }

   private Mob(final String n, final Mob.Enemies en, final String s, final String typeName) {
      this.suffix = s;
      this.name = n;
      this.type = en;
      this.bukkitType = EnumUtil.getEntityType(typeName);
   }

   private Mob(final String n, final Mob.Enemies en, final String typeName) {
      this.name = n;
      this.type = en;
      this.bukkitType = EnumUtil.getEntityType(typeName);
   }

   public static Set<String> getMobList() {
      return Collections.unmodifiableSet(hashMap.keySet());
   }

   public static Mob fromName(final String name) {
      return hashMap.get(name.toLowerCase(Locale.ENGLISH));
   }

   public static Mob fromBukkitType(final EntityType type) {
      return bukkitMap.get(type);
   }

   public Entity spawn(final World world, final Server server, final Location loc) throws Mob.MobException {
      Entity entity = world.spawn(loc, this.bukkitType.getEntityClass());
      if (entity == null) {
         Essentials.getWrappedLogger().log(Level.WARNING, AdventureUtil.getAdventureFacet().miniToLegacy(I18n.tlLiteral("unableToSpawnMob")));
         throw new Mob.MobException();
      } else {
         return entity;
      }
   }

   public EntityType getType() {
      return this.bukkitType;
   }

   static {
      for (Mob mob : values()) {
         hashMap.put(mob.name.toLowerCase(Locale.ENGLISH), mob);
         if (mob.bukkitType != null) {
            bukkitMap.put(mob.bukkitType, mob);
         }
      }
   }

   public static enum Enemies {
      FRIENDLY("friendly"),
      NEUTRAL("neutral"),
      ENEMY("enemy"),
      ADULT_ENEMY("adult_enemy");

      protected final String type;

      private Enemies(final String type) {
         this.type = type;
      }
   }

   public static class MobException extends Exception {
      private static final long serialVersionUID = 1L;
   }
}
