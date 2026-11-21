package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.Fields;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Animals;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Armadillo;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Bogged;
import org.bukkit.entity.Breeze;
import org.bukkit.entity.Camel;
import org.bukkit.entity.CamelHusk;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.ChestBoat;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Cod;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Creaking;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Egg;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Fish;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.GlowSquid;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Illager;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Parched;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Slime;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Sniffer;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Stray;
import org.bukkit.entity.SulfurCube;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tadpole;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.Trident;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Warden;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.boat.AcaciaBoat;
import org.bukkit.entity.boat.AcaciaChestBoat;
import org.bukkit.entity.boat.BambooChestRaft;
import org.bukkit.entity.boat.BambooRaft;
import org.bukkit.entity.boat.BirchBoat;
import org.bukkit.entity.boat.BirchChestBoat;
import org.bukkit.entity.boat.CherryBoat;
import org.bukkit.entity.boat.CherryChestBoat;
import org.bukkit.entity.boat.DarkOakBoat;
import org.bukkit.entity.boat.DarkOakChestBoat;
import org.bukkit.entity.boat.JungleBoat;
import org.bukkit.entity.boat.JungleChestBoat;
import org.bukkit.entity.boat.MangroveBoat;
import org.bukkit.entity.boat.MangroveChestBoat;
import org.bukkit.entity.boat.OakBoat;
import org.bukkit.entity.boat.OakChestBoat;
import org.bukkit.entity.boat.PaleOakBoat;
import org.bukkit.entity.boat.PaleOakChestBoat;
import org.bukkit.entity.boat.SpruceBoat;
import org.bukkit.entity.boat.SpruceChestBoat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class SimpleEntityData extends EntityData<Entity> {
   private static final List<SimpleEntityData.SimpleEntityDataInfo> types = new ArrayList<>();
   private transient SimpleEntityData.SimpleEntityDataInfo simpleInfo;

   @Internal
   public static void addSimpleEntity(String codeName, Class<? extends Entity> entityClass) {
      addSimpleEntity(codeName, entityClass, Kleenean.UNKNOWN);
   }

   @Internal
   public static void addSimpleEntity(String codeName, Class<? extends Entity> entityClass, Kleenean allowSpawning) {
      types.add(new SimpleEntityData.SimpleEntityDataInfo(codeName, entityClass, false, allowSpawning));
   }

   @Internal
   public static void addSuperEntity(String codeName, Class<? extends Entity> entityClass) {
      addSuperEntity(codeName, entityClass, Kleenean.UNKNOWN);
   }

   @Internal
   public static void addSuperEntity(String codeName, Class<? extends Entity> entityClass, Kleenean allowSpawning) {
      types.add(new SimpleEntityData.SimpleEntityDataInfo(codeName, entityClass, true, allowSpawning));
   }

   public SimpleEntityData() {
      this(Entity.class);
   }

   private SimpleEntityData(SimpleEntityData.SimpleEntityDataInfo simpleInfo) {
      assert simpleInfo != null;

      this.simpleInfo = simpleInfo;
      this.codeNameIndex = types.indexOf(simpleInfo);
   }

   public SimpleEntityData(Class<? extends Entity> entityClass) {
      assert entityClass != null && entityClass.isInterface() : entityClass;

      int i = 0;
      SimpleEntityData.SimpleEntityDataInfo closestInfo = null;
      int closestPattern = 0;

      for (SimpleEntityData.SimpleEntityDataInfo info : types) {
         if (info.c.isAssignableFrom(entityClass) && (closestInfo == null || closestInfo.c.isAssignableFrom(info.c))) {
            closestInfo = info;
            closestPattern = i;
         }

         i++;
      }

      if (closestInfo != null) {
         this.simpleInfo = closestInfo;
         this.codeNameIndex = closestPattern;
      } else {
         throw new IllegalStateException();
      }
   }

   public SimpleEntityData(Entity entity) {
      int i = 0;
      SimpleEntityData.SimpleEntityDataInfo closestInfo = null;
      int closestPattern = 0;

      for (SimpleEntityData.SimpleEntityDataInfo info : types) {
         if (info.c.isInstance(entity) && (closestInfo == null || closestInfo.c.isAssignableFrom(info.c))) {
            closestInfo = info;
            closestPattern = i;
         }

         i++;
      }

      if (closestInfo != null) {
         this.simpleInfo = closestInfo;
         this.codeNameIndex = closestPattern;
      } else {
         throw new IllegalStateException();
      }
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.simpleInfo = types.get(matchedCodeName);

      assert this.simpleInfo != null : matchedCodeName;

      return true;
   }

   @Override
   protected boolean init(@Nullable Class<? extends Entity> entityClass, @Nullable Entity entity) {
      assert false;

      return false;
   }

   @Override
   public void set(Entity entity) {
   }

   @Override
   public boolean match(Entity entity) {
      if (this.simpleInfo.isSupertype) {
         return this.simpleInfo.c.isInstance(entity);
      } else {
         SimpleEntityData.SimpleEntityDataInfo closest = null;

         for (SimpleEntityData.SimpleEntityDataInfo info : types) {
            if (info.c.isInstance(entity) && (closest == null || closest.c.isAssignableFrom(info.c))) {
               closest = info;
            }
         }

         if (closest != null) {
            return this.simpleInfo.c == closest.c;
         } else {
            assert false;

            return false;
         }
      }
   }

   @Override
   public Class<? extends Entity> getType() {
      return this.simpleInfo.c;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new SimpleEntityData(this.simpleInfo);
   }

   @Override
   protected int hashCode_i() {
      return this.simpleInfo.hashCode();
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof SimpleEntityData other ? this.simpleInfo.equals(other.simpleInfo) : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return this.simpleInfo.c == entityData.getType() || this.simpleInfo.isSupertype && this.simpleInfo.c.isAssignableFrom(entityData.getType());
   }

   @Override
   public boolean canSpawn(@Nullable World world) {
      if (this.simpleInfo.allowSpawning.isUnknown()) {
         return super.canSpawn(world);
      } else {
         return world == null ? false : this.simpleInfo.allowSpawning.isTrue();
      }
   }

   @Override
   public Fields serialize() throws NotSerializableException {
      Fields fields = super.serialize();
      fields.putObject("info.codeName", this.simpleInfo.codeName);
      return fields;
   }

   @Override
   public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
      String codeName = fields.getAndRemoveObject("info.codeName", String.class);

      for (SimpleEntityData.SimpleEntityDataInfo info : types) {
         if (info.codeName.equals(codeName)) {
            this.simpleInfo = info;
            super.deserialize(fields);
            return;
         }
      }

      throw new StreamCorruptedException("Invalid SimpleEntityDataInfo code name " + codeName);
   }

   static {
      addSimpleEntity("zombie", Zombie.class);
      addSuperEntity("skeleton", Skeleton.class);
      addSimpleEntity("tnt", TNTPrimed.class);
      addSimpleEntity("spider", Spider.class);
      addSimpleEntity("player", Player.class);
      addSimpleEntity("lightning bolt", LightningStrike.class);
      addSimpleEntity("giant", Giant.class);
      addSimpleEntity("ghast", Ghast.class);
      addSimpleEntity("fish hook", FishHook.class);
      addSuperEntity("fireball", Fireball.class, Kleenean.TRUE);
      addSimpleEntity("small fireball", SmallFireball.class);
      addSimpleEntity("large fireball", LargeFireball.class);
      addSimpleEntity("ender pearl", EnderPearl.class);
      addSimpleEntity("ender dragon", EnderDragon.class);
      addSimpleEntity("ender crystal", EnderCrystal.class);
      addSimpleEntity("dragon fireball", DragonFireball.class);
      addSimpleEntity("egg", Egg.class);
      addSimpleEntity("cave spider", CaveSpider.class);
      addSimpleEntity("arrow", Arrow.class);
      addSimpleEntity("squid", Squid.class);
      addSimpleEntity("snow golem", Snowman.class);
      addSimpleEntity("snowball", Snowball.class);
      addSimpleEntity("slime", Slime.class);
      addSimpleEntity("magma cube", MagmaCube.class);
      addSimpleEntity("ender eye", EnderSignal.class);
      addSimpleEntity("blaze", Blaze.class);
      addSimpleEntity("zombie pigman", PigZombie.class);
      addSimpleEntity("bottle of enchanting", ThrownExpBottle.class);
      addSimpleEntity("iron golem", IronGolem.class);
      addSimpleEntity("ocelot", Ocelot.class);
      addSimpleEntity("wither skeleton", WitherSkeleton.class);
      addSimpleEntity("firework", Firework.class, Kleenean.TRUE);
      addSimpleEntity("wither", Wither.class);
      addSimpleEntity("wither skull", WitherSkull.class);
      addSimpleEntity("witch", Witch.class);
      addSimpleEntity("bat", Bat.class);
      addSimpleEntity("item frame", ItemFrame.class);
      addSimpleEntity("painting", Painting.class);
      addSimpleEntity("horse", Horse.class);
      addSimpleEntity("skeleton horse", SkeletonHorse.class);
      addSimpleEntity("undead horse", ZombieHorse.class);
      addSimpleEntity("mule", Mule.class);
      addSimpleEntity("donkey", Donkey.class);
      addSimpleEntity("leash hitch", LeashHitch.class);
      addSimpleEntity("elder guardian", ElderGuardian.class);
      addSimpleEntity("normal guardian", Guardian.class);
      addSimpleEntity("armor stand", ArmorStand.class);
      addSimpleEntity("endermite", Endermite.class);
      addSimpleEntity("silverfish", Silverfish.class);
      addSimpleEntity("tipped arrow", TippedArrow.class);
      addSimpleEntity("area effect cloud", AreaEffectCloud.class);
      addSimpleEntity("shulker", Shulker.class);
      addSimpleEntity("shulker bullet", ShulkerBullet.class);
      addSimpleEntity("spectral arrow", SpectralArrow.class);
      addSimpleEntity("husk", Husk.class);
      addSimpleEntity("stray", Stray.class);
      addSimpleEntity("polar bear", PolarBear.class);
      addSimpleEntity("llama spit", LlamaSpit.class);
      addSimpleEntity("vindicator", Vindicator.class);
      addSimpleEntity("vex", Vex.class);
      addSimpleEntity("evoker", Evoker.class);
      addSimpleEntity("evoker fangs", EvokerFangs.class);
      addSimpleEntity("illusioner", Illusioner.class);
      addSimpleEntity("trident", Trident.class);
      addSimpleEntity("puffer fish", PufferFish.class);
      addSimpleEntity("cod", Cod.class);
      addSimpleEntity("turtle", Turtle.class);
      addSimpleEntity("drowned", Drowned.class);
      addSimpleEntity("phantom", Phantom.class);
      addSimpleEntity("dolphin", Dolphin.class);
      addSimpleEntity("pillager", Pillager.class);
      addSimpleEntity("ravager", Ravager.class);
      addSimpleEntity("wandering trader", WanderingTrader.class);
      addSimpleEntity("piglin", Piglin.class);
      addSimpleEntity("hoglin", Hoglin.class);
      addSimpleEntity("zoglin", Zoglin.class);
      addSimpleEntity("piglin brute", PiglinBrute.class);
      addSimpleEntity("glow squid", GlowSquid.class);
      addSimpleEntity("marker", Marker.class);
      addSimpleEntity("glow item frame", GlowItemFrame.class);
      addSimpleEntity("allay", Allay.class);
      addSimpleEntity("tadpole", Tadpole.class);
      addSimpleEntity("warden", Warden.class);
      addSimpleEntity("camel", Camel.class);
      addSimpleEntity("sniffer", Sniffer.class);
      addSimpleEntity("interaction", Interaction.class);
      if (Skript.isRunningMinecraft(1, 20, 3)) {
         addSimpleEntity("breeze", Breeze.class);
         addSimpleEntity("wind charge", WindCharge.class);
      }

      if (Skript.isRunningMinecraft(1, 20, 5)) {
         addSimpleEntity("armadillo", Armadillo.class);
         addSimpleEntity("bogged", Bogged.class);
      }

      if (Skript.isRunningMinecraft(1, 21, 2)) {
         addSimpleEntity("creaking", Creaking.class);
         addSimpleEntity("oak boat", OakBoat.class);
         addSimpleEntity("dark oak boat", DarkOakBoat.class);
         addSimpleEntity("pale oak boat", PaleOakBoat.class);
         addSimpleEntity("acacia boat", AcaciaBoat.class);
         addSimpleEntity("birch boat", BirchBoat.class);
         addSimpleEntity("spruce boat", SpruceBoat.class);
         addSimpleEntity("jungle boat", JungleBoat.class);
         addSimpleEntity("bamboo raft", BambooRaft.class);
         addSimpleEntity("mangrove boat", MangroveBoat.class);
         addSimpleEntity("cherry boat", CherryBoat.class);
         addSimpleEntity("oak chest boat", OakChestBoat.class);
         addSimpleEntity("dark oak chest boat", DarkOakChestBoat.class);
         addSimpleEntity("pale oak chest boat", PaleOakChestBoat.class);
         addSimpleEntity("acacia chest boat", AcaciaChestBoat.class);
         addSimpleEntity("birch chest boat", BirchChestBoat.class);
         addSimpleEntity("spruce chest boat", SpruceChestBoat.class);
         addSimpleEntity("jungle chest boat", JungleChestBoat.class);
         addSimpleEntity("bamboo chest raft", BambooChestRaft.class);
         addSimpleEntity("mangrove chest boat", MangroveChestBoat.class);
         addSimpleEntity("cherry chest boat", CherryChestBoat.class);
         addSuperEntity("boat", Boat.class);
         addSuperEntity("any boat", Boat.class);
         addSuperEntity("chest boat", ChestBoat.class);
         addSuperEntity("any chest boat", ChestBoat.class);
      }

      if (Skript.isRunningMinecraft(1, 21, 6)) {
         addSimpleEntity("happy ghast", HappyGhast.class);
      }

      if (Skript.isRunningMinecraft(1, 21, 9)) {
         addSimpleEntity("copper golem", CopperGolem.class);
         addSimpleEntity("mannequin", Mannequin.class);
      }

      if (Skript.isRunningMinecraft(1, 21, 11)) {
         addSimpleEntity("camel husk", CamelHusk.class);
         addSimpleEntity("parched", Parched.class);
      }

      if (Skript.isRunningMinecraft(26, 2)) {
         addSimpleEntity("sulfur cube", SulfurCube.class);
      }

      addSuperEntity("human", HumanEntity.class);
      addSuperEntity("damageable", Damageable.class);
      addSuperEntity("monster", Monster.class);
      addSuperEntity("mob", Mob.class);
      addSuperEntity("creature", Creature.class);
      addSuperEntity("animal", Animals.class);
      addSuperEntity("tameable", Tameable.class);
      addSuperEntity("fish", Fish.class);
      addSuperEntity("golem", Golem.class);
      addSuperEntity("projectile", Projectile.class);
      addSuperEntity("living entity", LivingEntity.class);
      addSuperEntity("entity", Entity.class);
      addSuperEntity("chested horse", ChestedHorse.class);
      addSuperEntity("any horse", AbstractHorse.class);
      addSuperEntity("guardian", Guardian.class);
      addSuperEntity("water mob", WaterMob.class);
      addSuperEntity("any fireball", Fireball.class);
      addSuperEntity("illager", Illager.class);
      addSuperEntity("spellcaster", Spellcaster.class);
      addSuperEntity("raider", Raider.class);
      if (Skript.classExists("org.bukkit.entity.Enemy")) {
         addSuperEntity("enemy", Enemy.class);
      }

      String[] codeNames = new String[types.size()];
      int i = 0;

      for (SimpleEntityData.SimpleEntityDataInfo info : types) {
         codeNames[i++] = info.codeName;
      }

      EntityData.register(SimpleEntityData.class, "simple", Entity.class, 0, codeNames);
   }

   public static final class SimpleEntityDataInfo {
      final String codeName;
      final Class<? extends Entity> c;
      final boolean isSupertype;
      final Kleenean allowSpawning;

      SimpleEntityDataInfo(String codeName, Class<? extends Entity> c, boolean isSupertype, Kleenean allowSpawning) {
         this.codeName = codeName;
         this.c = c;
         this.isSupertype = isSupertype;
         this.allowSpawning = allowSpawning;
      }

      @Override
      public int hashCode() {
         return this.c.hashCode();
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (obj instanceof SimpleEntityData.SimpleEntityDataInfo other) {
            if (this.c != other.c) {
               return false;
            } else {
               assert this.codeName.equals(other.codeName);

               assert this.isSupertype == other.isSupertype;

               return true;
            }
         } else {
            return false;
         }
      }
   }
}
