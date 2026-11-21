package com.sk89q.worldedit.bukkit.adapter.ext.fawe.v1_21_4;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.sk89q.worldedit.world.DataFixer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringUtil;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.DyeColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.enginehub.linbus.tree.LinCompoundTag;

class PaperweightDataConverters implements DataFixer {
   private final PaperweightAdapter adapter;
   private static final NbtOps OPS_NBT = NbtOps.INSTANCE;
   private static final int LEGACY_VERSION = 1343;
   private static int DATA_VERSION;
   static PaperweightDataConverters INSTANCE;
   private final Map<PaperweightDataConverters.LegacyType, List<PaperweightDataConverters.DataConverter>> converters = new EnumMap<>(
      PaperweightDataConverters.LegacyType.class
   );
   private final Map<PaperweightDataConverters.LegacyType, List<PaperweightDataConverters.DataInspector>> inspectors = new EnumMap<>(
      PaperweightDataConverters.LegacyType.class
   );
   private com.mojang.datafixers.DataFixer fixer;
   private static final Map<String, PaperweightDataConverters.LegacyType> DFU_TO_LEGACY = new HashMap<>();
   private static final Map<String, ResourceLocation> OLD_ID_TO_KEY_MAP = new HashMap<>();

   @Override
   public <T> T fixUp(DataFixer.FixType<T> type, T original, int srcVer) {
      if (type == DataFixer.FixTypes.CHUNK) {
         return (T)this.fixChunk((LinCompoundTag)original, srcVer);
      } else if (type == DataFixer.FixTypes.BLOCK_ENTITY) {
         return (T)this.fixBlockEntity((LinCompoundTag)original, srcVer);
      } else if (type == DataFixer.FixTypes.ENTITY) {
         return (T)this.fixEntity((LinCompoundTag)original, srcVer);
      } else if (type == DataFixer.FixTypes.BLOCK_STATE) {
         return (T)this.fixBlockState((String)original, srcVer);
      } else if (type == DataFixer.FixTypes.ITEM_TYPE) {
         return (T)this.fixItemType((String)original, srcVer);
      } else {
         return (T)(type == DataFixer.FixTypes.BIOME ? this.fixBiome((String)original, srcVer) : original);
      }
   }

   private LinCompoundTag fixChunk(LinCompoundTag originalChunk, int srcVer) {
      CompoundTag tag = (CompoundTag)this.adapter.fromNativeLin(originalChunk);
      CompoundTag fixed = convert(PaperweightDataConverters.LegacyType.CHUNK, tag, srcVer);
      return (LinCompoundTag)this.adapter.toNativeLin((Tag)fixed);
   }

   private LinCompoundTag fixBlockEntity(LinCompoundTag origTileEnt, int srcVer) {
      CompoundTag tag = (CompoundTag)this.adapter.fromNativeLin(origTileEnt);
      CompoundTag fixed = convert(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, tag, srcVer);
      return (LinCompoundTag)this.adapter.toNativeLin((Tag)fixed);
   }

   private LinCompoundTag fixEntity(LinCompoundTag origEnt, int srcVer) {
      CompoundTag tag = (CompoundTag)this.adapter.fromNativeLin(origEnt);
      CompoundTag fixed = convert(PaperweightDataConverters.LegacyType.ENTITY, tag, srcVer);
      return (LinCompoundTag)this.adapter.toNativeLin((Tag)fixed);
   }

   private String fixBlockState(String blockState, int srcVer) {
      CompoundTag stateNBT = stateToNBT(blockState);
      Dynamic<Tag> dynamic = new Dynamic(OPS_NBT, stateNBT);
      CompoundTag fixed = (CompoundTag)INSTANCE.fixer.update(References.BLOCK_STATE, dynamic, srcVer, DATA_VERSION).getValue();
      return this.nbtToState(fixed);
   }

   private String nbtToState(CompoundTag tagCompound) {
      StringBuilder sb = new StringBuilder();
      sb.append(tagCompound.getString("Name"));
      if (tagCompound.contains("Properties", 10)) {
         sb.append('[');
         CompoundTag props = tagCompound.getCompound("Properties");
         sb.append(props.getAllKeys().stream().map(k -> k + "=" + props.getString(k).replace("\"", "")).collect(Collectors.joining(",")));
         sb.append(']');
      }

      return sb.toString();
   }

   private static CompoundTag stateToNBT(String blockState) {
      int propIdx = blockState.indexOf(91);
      CompoundTag tag = new CompoundTag();
      if (propIdx < 0) {
         tag.putString("Name", blockState);
      } else {
         tag.putString("Name", blockState.substring(0, propIdx));
         CompoundTag propTag = new CompoundTag();
         String props = blockState.substring(propIdx + 1, blockState.length() - 1);
         String[] propArr = props.split(",");

         for (String pair : propArr) {
            String[] split = pair.split("=");
            propTag.putString(split[0], split[1]);
         }

         tag.put("Properties", propTag);
      }

      return tag;
   }

   private String fixBiome(String key, int srcVer) {
      return fixName(key, srcVer, References.BIOME);
   }

   private String fixItemType(String key, int srcVer) {
      return fixName(key, srcVer, References.ITEM_NAME);
   }

   private static String fixName(String key, int srcVer, TypeReference type) {
      return ((Tag)INSTANCE.fixer.update(type, new Dynamic(OPS_NBT, StringTag.valueOf(key)), srcVer, DATA_VERSION).getValue()).getAsString();
   }

   PaperweightDataConverters(int dataVersion, PaperweightAdapter adapter) {
      DATA_VERSION = dataVersion;
      INSTANCE = this;
      this.adapter = adapter;
      this.registerConverters();
      this.registerInspectors();
      this.fixer = new PaperweightDataConverters.WrappedDataFixer(DataFixers.getDataFixer());
   }

   public static CompoundTag convert(PaperweightDataConverters.LegacyType type, CompoundTag cmp) {
      return convert(type.getDFUType(), cmp);
   }

   public static CompoundTag convert(PaperweightDataConverters.LegacyType type, CompoundTag cmp, int sourceVer) {
      return convert(type.getDFUType(), cmp, sourceVer);
   }

   public static CompoundTag convert(PaperweightDataConverters.LegacyType type, CompoundTag cmp, int sourceVer, int targetVer) {
      return convert(type.getDFUType(), cmp, sourceVer, targetVer);
   }

   public static CompoundTag convert(TypeReference type, CompoundTag cmp) {
      int i = cmp.contains("DataVersion", 99) ? cmp.getInt("DataVersion") : -1;
      return convert(type, cmp, i);
   }

   public static CompoundTag convert(TypeReference type, CompoundTag cmp, int sourceVer) {
      return convert(type, cmp, sourceVer, DATA_VERSION);
   }

   public static CompoundTag convert(TypeReference type, CompoundTag cmp, int sourceVer, int targetVer) {
      return sourceVer >= targetVer ? cmp : (CompoundTag)INSTANCE.fixer.update(type, new Dynamic(OPS_NBT, cmp), sourceVer, targetVer).getValue();
   }

   private void registerInspector(PaperweightDataConverters.LegacyType type, PaperweightDataConverters.DataInspector inspector) {
      this.inspectors.computeIfAbsent(type, k -> new ArrayList<>()).add(inspector);
   }

   private void registerConverter(PaperweightDataConverters.LegacyType type, PaperweightDataConverters.DataConverter converter) {
      int version = converter.getDataVersion();
      List<PaperweightDataConverters.DataConverter> list = this.converters.computeIfAbsent(type, k -> new ArrayList<>());
      if (!list.isEmpty() && list.get(list.size() - 1).getDataVersion() > version) {
         for (int j = 0; j < list.size(); j++) {
            if (list.get(j).getDataVersion() > version) {
               list.add(j, converter);
               break;
            }
         }
      } else {
         list.add(converter);
      }
   }

   private void registerInspectors() {
      this.registerEntityItemList("EntityHorseDonkey", "SaddleItem", "Items");
      this.registerEntityItemList("EntityHorseMule", "Items");
      this.registerEntityItemList("EntityMinecartChest", "Items");
      this.registerEntityItemList("EntityMinecartHopper", "Items");
      this.registerEntityItemList("EntityVillager", "Inventory");
      this.registerEntityItemListEquipment("EntityArmorStand");
      this.registerEntityItemListEquipment("EntityBat");
      this.registerEntityItemListEquipment("EntityBlaze");
      this.registerEntityItemListEquipment("EntityCaveSpider");
      this.registerEntityItemListEquipment("EntityChicken");
      this.registerEntityItemListEquipment("EntityCow");
      this.registerEntityItemListEquipment("EntityCreeper");
      this.registerEntityItemListEquipment("EntityEnderDragon");
      this.registerEntityItemListEquipment("EntityEnderman");
      this.registerEntityItemListEquipment("EntityEndermite");
      this.registerEntityItemListEquipment("EntityEvoker");
      this.registerEntityItemListEquipment("EntityGhast");
      this.registerEntityItemListEquipment("EntityGiantZombie");
      this.registerEntityItemListEquipment("EntityGuardian");
      this.registerEntityItemListEquipment("EntityGuardianElder");
      this.registerEntityItemListEquipment("EntityHorse");
      this.registerEntityItemListEquipment("EntityHorseDonkey");
      this.registerEntityItemListEquipment("EntityHorseMule");
      this.registerEntityItemListEquipment("EntityHorseSkeleton");
      this.registerEntityItemListEquipment("EntityHorseZombie");
      this.registerEntityItemListEquipment("EntityIronGolem");
      this.registerEntityItemListEquipment("EntityMagmaCube");
      this.registerEntityItemListEquipment("EntityMushroomCow");
      this.registerEntityItemListEquipment("EntityOcelot");
      this.registerEntityItemListEquipment("EntityPig");
      this.registerEntityItemListEquipment("EntityPigZombie");
      this.registerEntityItemListEquipment("EntityRabbit");
      this.registerEntityItemListEquipment("EntitySheep");
      this.registerEntityItemListEquipment("EntityShulker");
      this.registerEntityItemListEquipment("EntitySilverfish");
      this.registerEntityItemListEquipment("EntitySkeleton");
      this.registerEntityItemListEquipment("EntitySkeletonStray");
      this.registerEntityItemListEquipment("EntitySkeletonWither");
      this.registerEntityItemListEquipment("EntitySlime");
      this.registerEntityItemListEquipment("EntitySnowman");
      this.registerEntityItemListEquipment("EntitySpider");
      this.registerEntityItemListEquipment("EntitySquid");
      this.registerEntityItemListEquipment("EntityVex");
      this.registerEntityItemListEquipment("EntityVillager");
      this.registerEntityItemListEquipment("EntityVindicator");
      this.registerEntityItemListEquipment("EntityWitch");
      this.registerEntityItemListEquipment("EntityWither");
      this.registerEntityItemListEquipment("EntityWolf");
      this.registerEntityItemListEquipment("EntityZombie");
      this.registerEntityItemListEquipment("EntityZombieHusk");
      this.registerEntityItemListEquipment("EntityZombieVillager");
      this.registerEntityItemSingle("EntityFireworks", "FireworksItem");
      this.registerEntityItemSingle("EntityHorse", "ArmorItem");
      this.registerEntityItemSingle("EntityHorse", "SaddleItem");
      this.registerEntityItemSingle("EntityHorseMule", "SaddleItem");
      this.registerEntityItemSingle("EntityHorseSkeleton", "SaddleItem");
      this.registerEntityItemSingle("EntityHorseZombie", "SaddleItem");
      this.registerEntityItemSingle("EntityItem", "Item");
      this.registerEntityItemSingle("EntityItemFrame", "Item");
      this.registerEntityItemSingle("EntityPotion", "Potion");
      this.registerInspector(
         PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataInspectorItem("TileEntityRecordPlayer", "RecordItem")
      );
      this.registerInspector(
         PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataInspectorItemList("TileEntityBrewingStand", "Items")
      );
      this.registerInspector(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataInspectorItemList("TileEntityChest", "Items"));
      this.registerInspector(
         PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataInspectorItemList("TileEntityDispenser", "Items")
      );
      this.registerInspector(
         PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataInspectorItemList("TileEntityDropper", "Items")
      );
      this.registerInspector(
         PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataInspectorItemList("TileEntityFurnace", "Items")
      );
      this.registerInspector(
         PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataInspectorItemList("TileEntityHopper", "Items")
      );
      this.registerInspector(
         PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataInspectorItemList("TileEntityShulkerBox", "Items")
      );
      this.registerInspector(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataInspectorMobSpawnerMobs());
      this.registerInspector(PaperweightDataConverters.LegacyType.CHUNK, new PaperweightDataConverters.DataInspectorChunks());
      this.registerInspector(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataInspectorCommandBlock());
      this.registerInspector(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataInspectorEntityPassengers());
      this.registerInspector(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataInspectorMobSpawnerMinecart());
      this.registerInspector(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataInspectorVillagers());
      this.registerInspector(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataInspectorBlockEntity());
      this.registerInspector(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataInspectorEntity());
      this.registerInspector(PaperweightDataConverters.LegacyType.LEVEL, new PaperweightDataConverters.DataInspectorLevelPlayer());
      this.registerInspector(PaperweightDataConverters.LegacyType.PLAYER, new PaperweightDataConverters.DataInspectorPlayer());
      this.registerInspector(PaperweightDataConverters.LegacyType.PLAYER, new PaperweightDataConverters.DataInspectorPlayerVehicle());
      this.registerInspector(PaperweightDataConverters.LegacyType.STRUCTURE, new PaperweightDataConverters.DataInspectorStructure());
   }

   private void registerConverters() {
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterEquipment());
      this.registerConverter(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataConverterSignText());
      this.registerConverter(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataConverterMaterialId());
      this.registerConverter(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataConverterPotionId());
      this.registerConverter(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataConverterSpawnEgg());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterMinecart());
      this.registerConverter(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataConverterMobSpawner());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterUUID());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterHealth());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterSaddle());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterHanging());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterDropChances());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterRiding());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterArmorStand());
      this.registerConverter(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataConverterBook());
      this.registerConverter(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataConverterCookedFish());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterZombie());
      this.registerConverter(PaperweightDataConverters.LegacyType.OPTIONS, new PaperweightDataConverters.DataConverterVBO());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterGuardian());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterSkeleton());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterZombieType());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterHorse());
      this.registerConverter(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataConverterTileEntity());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterEntity());
      this.registerConverter(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataConverterBanner());
      this.registerConverter(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataConverterPotionWater());
      this.registerConverter(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataConverterShulker());
      this.registerConverter(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataConverterShulkerBoxItem());
      this.registerConverter(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, new PaperweightDataConverters.DataConverterShulkerBoxBlock());
      this.registerConverter(PaperweightDataConverters.LegacyType.OPTIONS, new PaperweightDataConverters.DataConverterLang());
      this.registerConverter(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataConverterTotem());
      this.registerConverter(PaperweightDataConverters.LegacyType.CHUNK, new PaperweightDataConverters.DataConverterBedBlock());
      this.registerConverter(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, new PaperweightDataConverters.DataConverterBedItem());
   }

   private void registerEntityItemList(String type, String... keys) {
      this.registerInspector(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataInspectorItemList(type, keys));
   }

   private void registerEntityItemSingle(String type, String key) {
      this.registerInspector(PaperweightDataConverters.LegacyType.ENTITY, new PaperweightDataConverters.DataInspectorItem(type, key));
   }

   private void registerEntityItemListEquipment(String type) {
      this.registerEntityItemList(type, "ArmorItems", "HandItems");
   }

   private static ResourceLocation getKey(String type) {
      ResourceLocation key = OLD_ID_TO_KEY_MAP.get(type);
      if (key == null) {
         throw new IllegalArgumentException("Unknown mapping for " + type);
      } else {
         return key;
      }
   }

   private static void convertCompound(PaperweightDataConverters.LegacyType type, CompoundTag cmp, String key, int sourceVer, int targetVer) {
      cmp.put(key, convert(type, cmp.getCompound(key), sourceVer, targetVer));
   }

   private static void convertItem(CompoundTag nbttagcompound, String key, int sourceVer, int targetVer) {
      if (nbttagcompound.contains(key, 10)) {
         convertCompound(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, nbttagcompound, key, sourceVer, targetVer);
      }
   }

   private static void convertItems(CompoundTag nbttagcompound, String key, int sourceVer, int targetVer) {
      if (nbttagcompound.contains(key, 9)) {
         ListTag nbttaglist = nbttagcompound.getList(key, 10);

         for (int j = 0; j < nbttaglist.size(); j++) {
            nbttaglist.set(j, convert(PaperweightDataConverters.LegacyType.ITEM_INSTANCE, nbttaglist.getCompound(j), sourceVer, targetVer));
         }
      }
   }

   static {
      Map<String, ResourceLocation> map = OLD_ID_TO_KEY_MAP;
      map.put("EntityItem", ResourceLocation.parse("item"));
      map.put("EntityExperienceOrb", ResourceLocation.parse("xp_orb"));
      map.put("EntityAreaEffectCloud", ResourceLocation.parse("area_effect_cloud"));
      map.put("EntityGuardianElder", ResourceLocation.parse("elder_guardian"));
      map.put("EntitySkeletonWither", ResourceLocation.parse("wither_skeleton"));
      map.put("EntitySkeletonStray", ResourceLocation.parse("stray"));
      map.put("EntityEgg", ResourceLocation.parse("egg"));
      map.put("EntityLeash", ResourceLocation.parse("leash_knot"));
      map.put("EntityPainting", ResourceLocation.parse("painting"));
      map.put("EntityTippedArrow", ResourceLocation.parse("arrow"));
      map.put("EntitySnowball", ResourceLocation.parse("snowball"));
      map.put("EntityLargeFireball", ResourceLocation.parse("fireball"));
      map.put("EntitySmallFireball", ResourceLocation.parse("small_fireball"));
      map.put("EntityEnderPearl", ResourceLocation.parse("ender_pearl"));
      map.put("EntityEnderSignal", ResourceLocation.parse("eye_of_ender_signal"));
      map.put("EntityPotion", ResourceLocation.parse("potion"));
      map.put("EntityThrownExpBottle", ResourceLocation.parse("xp_bottle"));
      map.put("EntityItemFrame", ResourceLocation.parse("item_frame"));
      map.put("EntityWitherSkull", ResourceLocation.parse("wither_skull"));
      map.put("EntityTNTPrimed", ResourceLocation.parse("tnt"));
      map.put("EntityFallingBlock", ResourceLocation.parse("falling_block"));
      map.put("EntityFireworks", ResourceLocation.parse("fireworks_rocket"));
      map.put("EntityZombieHusk", ResourceLocation.parse("husk"));
      map.put("EntitySpectralArrow", ResourceLocation.parse("spectral_arrow"));
      map.put("EntityShulkerBullet", ResourceLocation.parse("shulker_bullet"));
      map.put("EntityDragonFireball", ResourceLocation.parse("dragon_fireball"));
      map.put("EntityZombieVillager", ResourceLocation.parse("zombie_villager"));
      map.put("EntityHorseSkeleton", ResourceLocation.parse("skeleton_horse"));
      map.put("EntityHorseZombie", ResourceLocation.parse("zombie_horse"));
      map.put("EntityArmorStand", ResourceLocation.parse("armor_stand"));
      map.put("EntityHorseDonkey", ResourceLocation.parse("donkey"));
      map.put("EntityHorseMule", ResourceLocation.parse("mule"));
      map.put("EntityEvokerFangs", ResourceLocation.parse("evocation_fangs"));
      map.put("EntityEvoker", ResourceLocation.parse("evocation_illager"));
      map.put("EntityVex", ResourceLocation.parse("vex"));
      map.put("EntityVindicator", ResourceLocation.parse("vindication_illager"));
      map.put("EntityIllagerIllusioner", ResourceLocation.parse("illusion_illager"));
      map.put("EntityMinecartCommandBlock", ResourceLocation.parse("commandblock_minecart"));
      map.put("EntityBoat", ResourceLocation.parse("boat"));
      map.put("EntityMinecartRideable", ResourceLocation.parse("minecart"));
      map.put("EntityMinecartChest", ResourceLocation.parse("chest_minecart"));
      map.put("EntityMinecartFurnace", ResourceLocation.parse("furnace_minecart"));
      map.put("EntityMinecartTNT", ResourceLocation.parse("tnt_minecart"));
      map.put("EntityMinecartHopper", ResourceLocation.parse("hopper_minecart"));
      map.put("EntityMinecartMobSpawner", ResourceLocation.parse("spawner_minecart"));
      map.put("EntityCreeper", ResourceLocation.parse("creeper"));
      map.put("EntitySkeleton", ResourceLocation.parse("skeleton"));
      map.put("EntitySpider", ResourceLocation.parse("spider"));
      map.put("EntityGiantZombie", ResourceLocation.parse("giant"));
      map.put("EntityZombie", ResourceLocation.parse("zombie"));
      map.put("EntitySlime", ResourceLocation.parse("slime"));
      map.put("EntityGhast", ResourceLocation.parse("ghast"));
      map.put("EntityPigZombie", ResourceLocation.parse("zombie_pigman"));
      map.put("EntityEnderman", ResourceLocation.parse("enderman"));
      map.put("EntityCaveSpider", ResourceLocation.parse("cave_spider"));
      map.put("EntitySilverfish", ResourceLocation.parse("silverfish"));
      map.put("EntityBlaze", ResourceLocation.parse("blaze"));
      map.put("EntityMagmaCube", ResourceLocation.parse("magma_cube"));
      map.put("EntityEnderDragon", ResourceLocation.parse("ender_dragon"));
      map.put("EntityWither", ResourceLocation.parse("wither"));
      map.put("EntityBat", ResourceLocation.parse("bat"));
      map.put("EntityWitch", ResourceLocation.parse("witch"));
      map.put("EntityEndermite", ResourceLocation.parse("endermite"));
      map.put("EntityGuardian", ResourceLocation.parse("guardian"));
      map.put("EntityShulker", ResourceLocation.parse("shulker"));
      map.put("EntityPig", ResourceLocation.parse("pig"));
      map.put("EntitySheep", ResourceLocation.parse("sheep"));
      map.put("EntityCow", ResourceLocation.parse("cow"));
      map.put("EntityChicken", ResourceLocation.parse("chicken"));
      map.put("EntitySquid", ResourceLocation.parse("squid"));
      map.put("EntityWolf", ResourceLocation.parse("wolf"));
      map.put("EntityMushroomCow", ResourceLocation.parse("mooshroom"));
      map.put("EntitySnowman", ResourceLocation.parse("snowman"));
      map.put("EntityOcelot", ResourceLocation.parse("ocelot"));
      map.put("EntityIronGolem", ResourceLocation.parse("villager_golem"));
      map.put("EntityHorse", ResourceLocation.parse("horse"));
      map.put("EntityRabbit", ResourceLocation.parse("rabbit"));
      map.put("EntityPolarBear", ResourceLocation.parse("polar_bear"));
      map.put("EntityLlama", ResourceLocation.parse("llama"));
      map.put("EntityLlamaSpit", ResourceLocation.parse("llama_spit"));
      map.put("EntityParrot", ResourceLocation.parse("parrot"));
      map.put("EntityVillager", ResourceLocation.parse("villager"));
      map.put("EntityEnderCrystal", ResourceLocation.parse("ender_crystal"));
      map.put("TileEntityFurnace", ResourceLocation.parse("furnace"));
      map.put("TileEntityChest", ResourceLocation.parse("chest"));
      map.put("TileEntityEnderChest", ResourceLocation.parse("ender_chest"));
      map.put("TileEntityRecordPlayer", ResourceLocation.parse("jukebox"));
      map.put("TileEntityDispenser", ResourceLocation.parse("dispenser"));
      map.put("TileEntityDropper", ResourceLocation.parse("dropper"));
      map.put("TileEntitySign", ResourceLocation.parse("sign"));
      map.put("TileEntityMobSpawner", ResourceLocation.parse("mob_spawner"));
      map.put("TileEntityNote", ResourceLocation.parse("noteblock"));
      map.put("TileEntityPiston", ResourceLocation.parse("piston"));
      map.put("TileEntityBrewingStand", ResourceLocation.parse("brewing_stand"));
      map.put("TileEntityEnchantTable", ResourceLocation.parse("enchanting_table"));
      map.put("TileEntityEnderPortal", ResourceLocation.parse("end_portal"));
      map.put("TileEntityBeacon", ResourceLocation.parse("beacon"));
      map.put("TileEntitySkull", ResourceLocation.parse("skull"));
      map.put("TileEntityLightDetector", ResourceLocation.parse("daylight_detector"));
      map.put("TileEntityHopper", ResourceLocation.parse("hopper"));
      map.put("TileEntityComparator", ResourceLocation.parse("comparator"));
      map.put("TileEntityFlowerPot", ResourceLocation.parse("flower_pot"));
      map.put("TileEntityBanner", ResourceLocation.parse("banner"));
      map.put("TileEntityStructure", ResourceLocation.parse("structure_block"));
      map.put("TileEntityEndGateway", ResourceLocation.parse("end_gateway"));
      map.put("TileEntityCommand", ResourceLocation.parse("command_block"));
      map.put("TileEntityShulkerBox", ResourceLocation.parse("shulker_box"));
      map.put("TileEntityBed", ResourceLocation.parse("bed"));
   }

   public interface DataConverter {
      int getDataVersion();

      CompoundTag convert(CompoundTag cmp);
   }

   private static class DataConverterArmorStand implements PaperweightDataConverters.DataConverter {
      DataConverterArmorStand() {
      }

      @Override
      public int getDataVersion() {
         return 147;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("ArmorStand".equals(cmp.getString("id")) && cmp.getBoolean("Silent") && !cmp.getBoolean("Marker")) {
            cmp.remove("Silent");
         }

         return cmp;
      }
   }

   private static class DataConverterBanner implements PaperweightDataConverters.DataConverter {
      DataConverterBanner() {
      }

      @Override
      public int getDataVersion() {
         return 804;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("minecraft:banner".equals(cmp.getString("id")) && cmp.contains("tag", 10)) {
            CompoundTag nbttagcompound1 = cmp.getCompound("tag");
            if (nbttagcompound1.contains("BlockEntityTag", 10)) {
               CompoundTag nbttagcompound2 = nbttagcompound1.getCompound("BlockEntityTag");
               if (nbttagcompound2.contains("Base", 99)) {
                  cmp.putShort("Damage", (short)(nbttagcompound2.getShort("Base") & 15));
                  if (nbttagcompound1.contains("display", 10)) {
                     CompoundTag nbttagcompound3 = nbttagcompound1.getCompound("display");
                     if (nbttagcompound3.contains("Lore", 9)) {
                        ListTag nbttaglist = nbttagcompound3.getList("Lore", 8);
                        if (nbttaglist.size() == 1 && "(+NBT)".equals(nbttaglist.getString(0))) {
                           return cmp;
                        }
                     }
                  }

                  nbttagcompound2.remove("Base");
                  if (nbttagcompound2.isEmpty()) {
                     nbttagcompound1.remove("BlockEntityTag");
                  }

                  if (nbttagcompound1.isEmpty()) {
                     cmp.remove("tag");
                  }
               }
            }
         }

         return cmp;
      }
   }

   private static class DataConverterBedBlock implements PaperweightDataConverters.DataConverter {
      private static final Logger a = LogManager.getLogger(PaperweightDataConverters.class);

      DataConverterBedBlock() {
      }

      @Override
      public int getDataVersion() {
         return 1125;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         try {
            CompoundTag nbttagcompound1 = cmp.getCompound("Level");
            int i = nbttagcompound1.getInt("xPos");
            int j = nbttagcompound1.getInt("zPos");
            ListTag nbttaglist = nbttagcompound1.getList("TileEntities", 10);
            ListTag nbttaglist1 = nbttagcompound1.getList("Sections", 10);

            for (int k = 0; k < nbttaglist1.size(); k++) {
               CompoundTag nbttagcompound2 = nbttaglist1.getCompound(k);
               byte b0 = nbttagcompound2.getByte("Y");
               byte[] abyte = nbttagcompound2.getByteArray("Blocks");

               for (int l = 0; l < abyte.length; l++) {
                  if (416 == (abyte[l] & 255) << 4) {
                     int i1 = l & 15;
                     int j1 = l >> 8 & 15;
                     int k1 = l >> 4 & 15;
                     CompoundTag nbttagcompound3 = new CompoundTag();
                     nbttagcompound3.putString("id", "bed");
                     nbttagcompound3.putInt("x", i1 + (i << 4));
                     nbttagcompound3.putInt("y", j1 + (b0 << 4));
                     nbttagcompound3.putInt("z", k1 + (j << 4));
                     nbttaglist.add(nbttagcompound3);
                  }
               }
            }
         } catch (Exception var16) {
            a.warn("Unable to datafix Bed blocks, level format may be missing tags.");
         }

         return cmp;
      }
   }

   private static class DataConverterBedItem implements PaperweightDataConverters.DataConverter {
      DataConverterBedItem() {
      }

      @Override
      public int getDataVersion() {
         return 1125;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("minecraft:bed".equals(cmp.getString("id")) && cmp.getShort("Damage") == 0) {
            cmp.putShort("Damage", (short)DyeColor.RED.getId());
         }

         return cmp;
      }
   }

   private static class DataConverterBook implements PaperweightDataConverters.DataConverter {
      DataConverterBook() {
      }

      @Override
      public int getDataVersion() {
         return 165;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("minecraft:written_book".equals(cmp.getString("id"))) {
            CompoundTag nbttagcompound1 = cmp.getCompound("tag");
            if (nbttagcompound1.contains("pages", 9)) {
               ListTag nbttaglist = nbttagcompound1.getList("pages", 8);

               for (int i = 0; i < nbttaglist.size(); i++) {
                  String s = nbttaglist.getString(i);
                  Component object = null;
                  if (!"null".equals(s) && !StringUtil.isNullOrEmpty(s)) {
                     if (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"' || s.charAt(0) == '{' && s.charAt(s.length() - 1) == '}') {
                        try {
                           object = (Component)GsonHelper.fromJson(PaperweightDataConverters.DataConverterSignText.a, s, Component.class, true);
                           if (object == null) {
                              object = Component.literal("");
                           }
                        } catch (JsonParseException var10) {
                        }

                        if (object == null) {
                           try {
                              object = Serializer.fromJson(s, MinecraftServer.getServer().registryAccess());
                           } catch (JsonParseException var9) {
                           }
                        }

                        if (object == null) {
                           try {
                              object = Serializer.fromJsonLenient(s, MinecraftServer.getServer().registryAccess());
                           } catch (JsonParseException var8) {
                           }
                        }

                        if (object == null) {
                           object = Component.literal(s);
                        }
                     } else {
                        object = Component.literal(s);
                     }
                  } else {
                     object = Component.literal("");
                  }

                  nbttaglist.set(i, StringTag.valueOf(Serializer.toJson(object, MinecraftServer.getServer().registryAccess())));
               }

               nbttagcompound1.put("pages", nbttaglist);
            }
         }

         return cmp;
      }
   }

   private static class DataConverterCookedFish implements PaperweightDataConverters.DataConverter {
      private static final ResourceLocation a = ResourceLocation.parse("cooked_fished");

      DataConverterCookedFish() {
      }

      @Override
      public int getDataVersion() {
         return 502;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if (cmp.contains("id", 8) && a.equals(ResourceLocation.parse(cmp.getString("id")))) {
            cmp.putString("id", "minecraft:cooked_fish");
         }

         return cmp;
      }
   }

   private static class DataConverterDropChances implements PaperweightDataConverters.DataConverter {
      DataConverterDropChances() {
      }

      @Override
      public int getDataVersion() {
         return 113;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if (cmp.contains("HandDropChances", 9)) {
            ListTag nbttaglist = cmp.getList("HandDropChances", 5);
            if (nbttaglist.size() == 2 && nbttaglist.getFloat(0) == 0.0F && nbttaglist.getFloat(1) == 0.0F) {
               cmp.remove("HandDropChances");
            }
         }

         if (cmp.contains("ArmorDropChances", 9)) {
            ListTag nbttaglist = cmp.getList("ArmorDropChances", 5);
            if (nbttaglist.size() == 4
               && nbttaglist.getFloat(0) == 0.0F
               && nbttaglist.getFloat(1) == 0.0F
               && nbttaglist.getFloat(2) == 0.0F
               && nbttaglist.getFloat(3) == 0.0F) {
               cmp.remove("ArmorDropChances");
            }
         }

         return cmp;
      }
   }

   private static class DataConverterEntity implements PaperweightDataConverters.DataConverter {
      private static final Map<String, String> a = Maps.newHashMap();

      DataConverterEntity() {
      }

      @Override
      public int getDataVersion() {
         return 704;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         String s = a.get(cmp.getString("id"));
         if (s != null) {
            cmp.putString("id", s);
         }

         return cmp;
      }

      static {
         a.put("AreaEffectCloud", "minecraft:area_effect_cloud");
         a.put("ArmorStand", "minecraft:armor_stand");
         a.put("Arrow", "minecraft:arrow");
         a.put("Bat", "minecraft:bat");
         a.put("Blaze", "minecraft:blaze");
         a.put("Boat", "minecraft:boat");
         a.put("CaveSpider", "minecraft:cave_spider");
         a.put("Chicken", "minecraft:chicken");
         a.put("Cow", "minecraft:cow");
         a.put("Creeper", "minecraft:creeper");
         a.put("Donkey", "minecraft:donkey");
         a.put("DragonFireball", "minecraft:dragon_fireball");
         a.put("ElderGuardian", "minecraft:elder_guardian");
         a.put("EnderCrystal", "minecraft:ender_crystal");
         a.put("EnderDragon", "minecraft:ender_dragon");
         a.put("Enderman", "minecraft:enderman");
         a.put("Endermite", "minecraft:endermite");
         a.put("EyeOfEnderSignal", "minecraft:eye_of_ender_signal");
         a.put("FallingSand", "minecraft:falling_block");
         a.put("Fireball", "minecraft:fireball");
         a.put("FireworksRocketEntity", "minecraft:fireworks_rocket");
         a.put("Ghast", "minecraft:ghast");
         a.put("Giant", "minecraft:giant");
         a.put("Guardian", "minecraft:guardian");
         a.put("Horse", "minecraft:horse");
         a.put("Husk", "minecraft:husk");
         a.put("Item", "minecraft:item");
         a.put("ItemFrame", "minecraft:item_frame");
         a.put("LavaSlime", "minecraft:magma_cube");
         a.put("LeashKnot", "minecraft:leash_knot");
         a.put("MinecartChest", "minecraft:chest_minecart");
         a.put("MinecartCommandBlock", "minecraft:commandblock_minecart");
         a.put("MinecartFurnace", "minecraft:furnace_minecart");
         a.put("MinecartHopper", "minecraft:hopper_minecart");
         a.put("MinecartRideable", "minecraft:minecart");
         a.put("MinecartSpawner", "minecraft:spawner_minecart");
         a.put("MinecartTNT", "minecraft:tnt_minecart");
         a.put("Mule", "minecraft:mule");
         a.put("MushroomCow", "minecraft:mooshroom");
         a.put("Ozelot", "minecraft:ocelot");
         a.put("Painting", "minecraft:painting");
         a.put("Pig", "minecraft:pig");
         a.put("PigZombie", "minecraft:zombie_pigman");
         a.put("PolarBear", "minecraft:polar_bear");
         a.put("PrimedTnt", "minecraft:tnt");
         a.put("Rabbit", "minecraft:rabbit");
         a.put("Sheep", "minecraft:sheep");
         a.put("Shulker", "minecraft:shulker");
         a.put("ShulkerBullet", "minecraft:shulker_bullet");
         a.put("Silverfish", "minecraft:silverfish");
         a.put("Skeleton", "minecraft:skeleton");
         a.put("SkeletonHorse", "minecraft:skeleton_horse");
         a.put("Slime", "minecraft:slime");
         a.put("SmallFireball", "minecraft:small_fireball");
         a.put("SnowMan", "minecraft:snowman");
         a.put("Snowball", "minecraft:snowball");
         a.put("SpectralArrow", "minecraft:spectral_arrow");
         a.put("Spider", "minecraft:spider");
         a.put("Squid", "minecraft:squid");
         a.put("Stray", "minecraft:stray");
         a.put("ThrownEgg", "minecraft:egg");
         a.put("ThrownEnderpearl", "minecraft:ender_pearl");
         a.put("ThrownExpBottle", "minecraft:xp_bottle");
         a.put("ThrownPotion", "minecraft:potion");
         a.put("Villager", "minecraft:villager");
         a.put("VillagerGolem", "minecraft:villager_golem");
         a.put("Witch", "minecraft:witch");
         a.put("WitherBoss", "minecraft:wither");
         a.put("WitherSkeleton", "minecraft:wither_skeleton");
         a.put("WitherSkull", "minecraft:wither_skull");
         a.put("Wolf", "minecraft:wolf");
         a.put("XPOrb", "minecraft:xp_orb");
         a.put("Zombie", "minecraft:zombie");
         a.put("ZombieHorse", "minecraft:zombie_horse");
         a.put("ZombieVillager", "minecraft:zombie_villager");
      }
   }

   private static class DataConverterEquipment implements PaperweightDataConverters.DataConverter {
      DataConverterEquipment() {
      }

      @Override
      public int getDataVersion() {
         return 100;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         ListTag nbttaglist = cmp.getList("Equipment", 10);
         if (!nbttaglist.isEmpty() && !cmp.contains("HandItems", 10)) {
            ListTag nbttaglist1 = new ListTag();
            nbttaglist1.add(nbttaglist.get(0));
            nbttaglist1.add(new CompoundTag());
            cmp.put("HandItems", nbttaglist1);
         }

         if (nbttaglist.size() > 1 && !cmp.contains("ArmorItem", 10)) {
            ListTag nbttaglist1 = new ListTag();
            nbttaglist1.add(nbttaglist.get(1));
            nbttaglist1.add(nbttaglist.get(2));
            nbttaglist1.add(nbttaglist.get(3));
            nbttaglist1.add(nbttaglist.get(4));
            cmp.put("ArmorItems", nbttaglist1);
         }

         cmp.remove("Equipment");
         if (cmp.contains("DropChances", 9)) {
            ListTag nbttaglist1 = cmp.getList("DropChances", 5);
            if (!cmp.contains("HandDropChances", 10)) {
               ListTag nbttaglist2 = new ListTag();
               nbttaglist2.add(FloatTag.valueOf(nbttaglist1.getFloat(0)));
               nbttaglist2.add(FloatTag.valueOf(0.0F));
               cmp.put("HandDropChances", nbttaglist2);
            }

            if (!cmp.contains("ArmorDropChances", 10)) {
               ListTag nbttaglist2 = new ListTag();
               nbttaglist2.add(FloatTag.valueOf(nbttaglist1.getFloat(1)));
               nbttaglist2.add(FloatTag.valueOf(nbttaglist1.getFloat(2)));
               nbttaglist2.add(FloatTag.valueOf(nbttaglist1.getFloat(3)));
               nbttaglist2.add(FloatTag.valueOf(nbttaglist1.getFloat(4)));
               cmp.put("ArmorDropChances", nbttaglist2);
            }

            cmp.remove("DropChances");
         }

         return cmp;
      }
   }

   private static class DataConverterGuardian implements PaperweightDataConverters.DataConverter {
      DataConverterGuardian() {
      }

      @Override
      public int getDataVersion() {
         return 700;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("Guardian".equals(cmp.getString("id"))) {
            if (cmp.getBoolean("Elder")) {
               cmp.putString("id", "ElderGuardian");
            }

            cmp.remove("Elder");
         }

         return cmp;
      }
   }

   private static class DataConverterHanging implements PaperweightDataConverters.DataConverter {
      DataConverterHanging() {
      }

      @Override
      public int getDataVersion() {
         return 111;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         String s = cmp.getString("id");
         boolean flag = "Painting".equals(s);
         boolean flag1 = "ItemFrame".equals(s);
         if ((flag || flag1) && !cmp.contains("Facing", 99)) {
            Direction enumdirection;
            if (cmp.contains("Direction", 99)) {
               enumdirection = Direction.from2DDataValue(cmp.getByte("Direction"));
               cmp.putInt("TileX", cmp.getInt("TileX") + enumdirection.getStepX());
               cmp.putInt("TileY", cmp.getInt("TileY") + enumdirection.getStepY());
               cmp.putInt("TileZ", cmp.getInt("TileZ") + enumdirection.getStepZ());
               cmp.remove("Direction");
               if (flag1 && cmp.contains("ItemRotation", 99)) {
                  cmp.putByte("ItemRotation", (byte)(cmp.getByte("ItemRotation") * 2));
               }
            } else {
               enumdirection = Direction.from2DDataValue(cmp.getByte("Dir"));
               cmp.remove("Dir");
            }

            cmp.putByte("Facing", (byte)enumdirection.get2DDataValue());
         }

         return cmp;
      }
   }

   private static class DataConverterHealth implements PaperweightDataConverters.DataConverter {
      private static final Set<String> a = Sets.newHashSet(
         new String[]{
            "ArmorStand",
            "Bat",
            "Blaze",
            "CaveSpider",
            "Chicken",
            "Cow",
            "Creeper",
            "EnderDragon",
            "Enderman",
            "Endermite",
            "EntityHorse",
            "Ghast",
            "Giant",
            "Guardian",
            "LavaSlime",
            "MushroomCow",
            "Ozelot",
            "Pig",
            "PigZombie",
            "Rabbit",
            "Sheep",
            "Shulker",
            "Silverfish",
            "Skeleton",
            "Slime",
            "SnowMan",
            "Spider",
            "Squid",
            "Villager",
            "VillagerGolem",
            "Witch",
            "WitherBoss",
            "Wolf",
            "Zombie"
         }
      );

      DataConverterHealth() {
      }

      @Override
      public int getDataVersion() {
         return 109;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if (a.contains(cmp.getString("id"))) {
            float f;
            if (cmp.contains("HealF", 99)) {
               f = cmp.getFloat("HealF");
               cmp.remove("HealF");
            } else {
               if (!cmp.contains("Health", 99)) {
                  return cmp;
               }

               f = cmp.getFloat("Health");
            }

            cmp.putFloat("Health", f);
         }

         return cmp;
      }
   }

   private static class DataConverterHorse implements PaperweightDataConverters.DataConverter {
      DataConverterHorse() {
      }

      @Override
      public int getDataVersion() {
         return 703;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("EntityHorse".equals(cmp.getString("id"))) {
            int i = cmp.getInt("Type");
            switch (i) {
               case 0:
               default:
                  cmp.putString("id", "Horse");
                  break;
               case 1:
                  cmp.putString("id", "Donkey");
                  break;
               case 2:
                  cmp.putString("id", "Mule");
                  break;
               case 3:
                  cmp.putString("id", "ZombieHorse");
                  break;
               case 4:
                  cmp.putString("id", "SkeletonHorse");
            }

            cmp.remove("Type");
         }

         return cmp;
      }
   }

   private static class DataConverterLang implements PaperweightDataConverters.DataConverter {
      DataConverterLang() {
      }

      @Override
      public int getDataVersion() {
         return 816;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if (cmp.contains("lang", 8)) {
            cmp.putString("lang", cmp.getString("lang").toLowerCase(Locale.ROOT));
         }

         return cmp;
      }
   }

   private static class DataConverterMaterialId implements PaperweightDataConverters.DataConverter {
      private static final String[] materials = new String[2268];

      DataConverterMaterialId() {
      }

      @Override
      public int getDataVersion() {
         return 102;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if (cmp.contains("id", 99)) {
            short short0 = cmp.getShort("id");
            if (short0 > 0 && short0 < materials.length && materials[short0] != null) {
               cmp.putString("id", materials[short0]);
            }
         }

         return cmp;
      }

      static {
         materials[1] = "minecraft:stone";
         materials[2] = "minecraft:grass";
         materials[3] = "minecraft:dirt";
         materials[4] = "minecraft:cobblestone";
         materials[5] = "minecraft:planks";
         materials[6] = "minecraft:sapling";
         materials[7] = "minecraft:bedrock";
         materials[8] = "minecraft:flowing_water";
         materials[9] = "minecraft:water";
         materials[10] = "minecraft:flowing_lava";
         materials[11] = "minecraft:lava";
         materials[12] = "minecraft:sand";
         materials[13] = "minecraft:gravel";
         materials[14] = "minecraft:gold_ore";
         materials[15] = "minecraft:iron_ore";
         materials[16] = "minecraft:coal_ore";
         materials[17] = "minecraft:log";
         materials[18] = "minecraft:leaves";
         materials[19] = "minecraft:sponge";
         materials[20] = "minecraft:glass";
         materials[21] = "minecraft:lapis_ore";
         materials[22] = "minecraft:lapis_block";
         materials[23] = "minecraft:dispenser";
         materials[24] = "minecraft:sandstone";
         materials[25] = "minecraft:noteblock";
         materials[27] = "minecraft:golden_rail";
         materials[28] = "minecraft:detector_rail";
         materials[29] = "minecraft:sticky_piston";
         materials[30] = "minecraft:web";
         materials[31] = "minecraft:tallgrass";
         materials[32] = "minecraft:deadbush";
         materials[33] = "minecraft:piston";
         materials[35] = "minecraft:wool";
         materials[37] = "minecraft:yellow_flower";
         materials[38] = "minecraft:red_flower";
         materials[39] = "minecraft:brown_mushroom";
         materials[40] = "minecraft:red_mushroom";
         materials[41] = "minecraft:gold_block";
         materials[42] = "minecraft:iron_block";
         materials[43] = "minecraft:double_stone_slab";
         materials[44] = "minecraft:stone_slab";
         materials[45] = "minecraft:brick_block";
         materials[46] = "minecraft:tnt";
         materials[47] = "minecraft:bookshelf";
         materials[48] = "minecraft:mossy_cobblestone";
         materials[49] = "minecraft:obsidian";
         materials[50] = "minecraft:torch";
         materials[51] = "minecraft:fire";
         materials[52] = "minecraft:mob_spawner";
         materials[53] = "minecraft:oak_stairs";
         materials[54] = "minecraft:chest";
         materials[56] = "minecraft:diamond_ore";
         materials[57] = "minecraft:diamond_block";
         materials[58] = "minecraft:crafting_table";
         materials[60] = "minecraft:farmland";
         materials[61] = "minecraft:furnace";
         materials[62] = "minecraft:lit_furnace";
         materials[65] = "minecraft:ladder";
         materials[66] = "minecraft:rail";
         materials[67] = "minecraft:stone_stairs";
         materials[69] = "minecraft:lever";
         materials[70] = "minecraft:stone_pressure_plate";
         materials[72] = "minecraft:wooden_pressure_plate";
         materials[73] = "minecraft:redstone_ore";
         materials[76] = "minecraft:redstone_torch";
         materials[77] = "minecraft:stone_button";
         materials[78] = "minecraft:snow_layer";
         materials[79] = "minecraft:ice";
         materials[80] = "minecraft:snow";
         materials[81] = "minecraft:cactus";
         materials[82] = "minecraft:clay";
         materials[84] = "minecraft:jukebox";
         materials[85] = "minecraft:fence";
         materials[86] = "minecraft:pumpkin";
         materials[87] = "minecraft:netherrack";
         materials[88] = "minecraft:soul_sand";
         materials[89] = "minecraft:glowstone";
         materials[90] = "minecraft:portal";
         materials[91] = "minecraft:lit_pumpkin";
         materials[95] = "minecraft:stained_glass";
         materials[96] = "minecraft:trapdoor";
         materials[97] = "minecraft:monster_egg";
         materials[98] = "minecraft:stonebrick";
         materials[99] = "minecraft:brown_mushroom_block";
         materials[100] = "minecraft:red_mushroom_block";
         materials[101] = "minecraft:iron_bars";
         materials[102] = "minecraft:glass_pane";
         materials[103] = "minecraft:melon_block";
         materials[106] = "minecraft:vine";
         materials[107] = "minecraft:fence_gate";
         materials[108] = "minecraft:brick_stairs";
         materials[109] = "minecraft:stone_brick_stairs";
         materials[110] = "minecraft:mycelium";
         materials[111] = "minecraft:waterlily";
         materials[112] = "minecraft:nether_brick";
         materials[113] = "minecraft:nether_brick_fence";
         materials[114] = "minecraft:nether_brick_stairs";
         materials[116] = "minecraft:enchanting_table";
         materials[119] = "minecraft:end_portal";
         materials[120] = "minecraft:end_portal_frame";
         materials[121] = "minecraft:end_stone";
         materials[122] = "minecraft:dragon_egg";
         materials[123] = "minecraft:redstone_lamp";
         materials[125] = "minecraft:double_wooden_slab";
         materials[126] = "minecraft:wooden_slab";
         materials[127] = "minecraft:cocoa";
         materials[128] = "minecraft:sandstone_stairs";
         materials[129] = "minecraft:emerald_ore";
         materials[130] = "minecraft:ender_chest";
         materials[131] = "minecraft:tripwire_hook";
         materials[133] = "minecraft:emerald_block";
         materials[134] = "minecraft:spruce_stairs";
         materials[135] = "minecraft:birch_stairs";
         materials[136] = "minecraft:jungle_stairs";
         materials[137] = "minecraft:command_block";
         materials[138] = "minecraft:beacon";
         materials[139] = "minecraft:cobblestone_wall";
         materials[141] = "minecraft:carrots";
         materials[142] = "minecraft:potatoes";
         materials[143] = "minecraft:wooden_button";
         materials[145] = "minecraft:anvil";
         materials[146] = "minecraft:trapped_chest";
         materials[147] = "minecraft:light_weighted_pressure_plate";
         materials[148] = "minecraft:heavy_weighted_pressure_plate";
         materials[151] = "minecraft:daylight_detector";
         materials[152] = "minecraft:redstone_block";
         materials[153] = "minecraft:quartz_ore";
         materials[154] = "minecraft:hopper";
         materials[155] = "minecraft:quartz_block";
         materials[156] = "minecraft:quartz_stairs";
         materials[157] = "minecraft:activator_rail";
         materials[158] = "minecraft:dropper";
         materials[159] = "minecraft:stained_hardened_clay";
         materials[160] = "minecraft:stained_glass_pane";
         materials[161] = "minecraft:leaves2";
         materials[162] = "minecraft:log2";
         materials[163] = "minecraft:acacia_stairs";
         materials[164] = "minecraft:dark_oak_stairs";
         materials[170] = "minecraft:hay_block";
         materials[171] = "minecraft:carpet";
         materials[172] = "minecraft:hardened_clay";
         materials[173] = "minecraft:coal_block";
         materials[174] = "minecraft:packed_ice";
         materials[175] = "minecraft:double_plant";
         materials[256] = "minecraft:iron_shovel";
         materials[257] = "minecraft:iron_pickaxe";
         materials[258] = "minecraft:iron_axe";
         materials[259] = "minecraft:flint_and_steel";
         materials[260] = "minecraft:apple";
         materials[261] = "minecraft:bow";
         materials[262] = "minecraft:arrow";
         materials[263] = "minecraft:coal";
         materials[264] = "minecraft:diamond";
         materials[265] = "minecraft:iron_ingot";
         materials[266] = "minecraft:gold_ingot";
         materials[267] = "minecraft:iron_sword";
         materials[268] = "minecraft:wooden_sword";
         materials[269] = "minecraft:wooden_shovel";
         materials[270] = "minecraft:wooden_pickaxe";
         materials[271] = "minecraft:wooden_axe";
         materials[272] = "minecraft:stone_sword";
         materials[273] = "minecraft:stone_shovel";
         materials[274] = "minecraft:stone_pickaxe";
         materials[275] = "minecraft:stone_axe";
         materials[276] = "minecraft:diamond_sword";
         materials[277] = "minecraft:diamond_shovel";
         materials[278] = "minecraft:diamond_pickaxe";
         materials[279] = "minecraft:diamond_axe";
         materials[280] = "minecraft:stick";
         materials[281] = "minecraft:bowl";
         materials[282] = "minecraft:mushroom_stew";
         materials[283] = "minecraft:golden_sword";
         materials[284] = "minecraft:golden_shovel";
         materials[285] = "minecraft:golden_pickaxe";
         materials[286] = "minecraft:golden_axe";
         materials[287] = "minecraft:string";
         materials[288] = "minecraft:feather";
         materials[289] = "minecraft:gunpowder";
         materials[290] = "minecraft:wooden_hoe";
         materials[291] = "minecraft:stone_hoe";
         materials[292] = "minecraft:iron_hoe";
         materials[293] = "minecraft:diamond_hoe";
         materials[294] = "minecraft:golden_hoe";
         materials[295] = "minecraft:wheat_seeds";
         materials[296] = "minecraft:wheat";
         materials[297] = "minecraft:bread";
         materials[298] = "minecraft:leather_helmet";
         materials[299] = "minecraft:leather_chestplate";
         materials[300] = "minecraft:leather_leggings";
         materials[301] = "minecraft:leather_boots";
         materials[302] = "minecraft:chainmail_helmet";
         materials[303] = "minecraft:chainmail_chestplate";
         materials[304] = "minecraft:chainmail_leggings";
         materials[305] = "minecraft:chainmail_boots";
         materials[306] = "minecraft:iron_helmet";
         materials[307] = "minecraft:iron_chestplate";
         materials[308] = "minecraft:iron_leggings";
         materials[309] = "minecraft:iron_boots";
         materials[310] = "minecraft:diamond_helmet";
         materials[311] = "minecraft:diamond_chestplate";
         materials[312] = "minecraft:diamond_leggings";
         materials[313] = "minecraft:diamond_boots";
         materials[314] = "minecraft:golden_helmet";
         materials[315] = "minecraft:golden_chestplate";
         materials[316] = "minecraft:golden_leggings";
         materials[317] = "minecraft:golden_boots";
         materials[318] = "minecraft:flint";
         materials[319] = "minecraft:porkchop";
         materials[320] = "minecraft:cooked_porkchop";
         materials[321] = "minecraft:painting";
         materials[322] = "minecraft:golden_apple";
         materials[323] = "minecraft:sign";
         materials[324] = "minecraft:wooden_door";
         materials[325] = "minecraft:bucket";
         materials[326] = "minecraft:water_bucket";
         materials[327] = "minecraft:lava_bucket";
         materials[328] = "minecraft:minecart";
         materials[329] = "minecraft:saddle";
         materials[330] = "minecraft:iron_door";
         materials[331] = "minecraft:redstone";
         materials[332] = "minecraft:snowball";
         materials[333] = "minecraft:boat";
         materials[334] = "minecraft:leather";
         materials[335] = "minecraft:milk_bucket";
         materials[336] = "minecraft:brick";
         materials[337] = "minecraft:clay_ball";
         materials[338] = "minecraft:reeds";
         materials[339] = "minecraft:paper";
         materials[340] = "minecraft:book";
         materials[341] = "minecraft:slime_ball";
         materials[342] = "minecraft:chest_minecart";
         materials[343] = "minecraft:furnace_minecart";
         materials[344] = "minecraft:egg";
         materials[345] = "minecraft:compass";
         materials[346] = "minecraft:fishing_rod";
         materials[347] = "minecraft:clock";
         materials[348] = "minecraft:glowstone_dust";
         materials[349] = "minecraft:fish";
         materials[350] = "minecraft:cooked_fish";
         materials[351] = "minecraft:dye";
         materials[352] = "minecraft:bone";
         materials[353] = "minecraft:sugar";
         materials[354] = "minecraft:cake";
         materials[355] = "minecraft:bed";
         materials[356] = "minecraft:repeater";
         materials[357] = "minecraft:cookie";
         materials[358] = "minecraft:filled_map";
         materials[359] = "minecraft:shears";
         materials[360] = "minecraft:melon";
         materials[361] = "minecraft:pumpkin_seeds";
         materials[362] = "minecraft:melon_seeds";
         materials[363] = "minecraft:beef";
         materials[364] = "minecraft:cooked_beef";
         materials[365] = "minecraft:chicken";
         materials[366] = "minecraft:cooked_chicken";
         materials[367] = "minecraft:rotten_flesh";
         materials[368] = "minecraft:ender_pearl";
         materials[369] = "minecraft:blaze_rod";
         materials[370] = "minecraft:ghast_tear";
         materials[371] = "minecraft:gold_nugget";
         materials[372] = "minecraft:nether_wart";
         materials[373] = "minecraft:potion";
         materials[374] = "minecraft:glass_bottle";
         materials[375] = "minecraft:spider_eye";
         materials[376] = "minecraft:fermented_spider_eye";
         materials[377] = "minecraft:blaze_powder";
         materials[378] = "minecraft:magma_cream";
         materials[379] = "minecraft:brewing_stand";
         materials[380] = "minecraft:cauldron";
         materials[381] = "minecraft:ender_eye";
         materials[382] = "minecraft:speckled_melon";
         materials[383] = "minecraft:spawn_egg";
         materials[384] = "minecraft:experience_bottle";
         materials[385] = "minecraft:fire_charge";
         materials[386] = "minecraft:writable_book";
         materials[387] = "minecraft:written_book";
         materials[388] = "minecraft:emerald";
         materials[389] = "minecraft:item_frame";
         materials[390] = "minecraft:flower_pot";
         materials[391] = "minecraft:carrot";
         materials[392] = "minecraft:potato";
         materials[393] = "minecraft:baked_potato";
         materials[394] = "minecraft:poisonous_potato";
         materials[395] = "minecraft:map";
         materials[396] = "minecraft:golden_carrot";
         materials[397] = "minecraft:skull";
         materials[398] = "minecraft:carrot_on_a_stick";
         materials[399] = "minecraft:nether_star";
         materials[400] = "minecraft:pumpkin_pie";
         materials[401] = "minecraft:fireworks";
         materials[402] = "minecraft:firework_charge";
         materials[403] = "minecraft:enchanted_book";
         materials[404] = "minecraft:comparator";
         materials[405] = "minecraft:netherbrick";
         materials[406] = "minecraft:quartz";
         materials[407] = "minecraft:tnt_minecart";
         materials[408] = "minecraft:hopper_minecart";
         materials[417] = "minecraft:iron_horse_armor";
         materials[418] = "minecraft:golden_horse_armor";
         materials[419] = "minecraft:diamond_horse_armor";
         materials[420] = "minecraft:lead";
         materials[421] = "minecraft:name_tag";
         materials[422] = "minecraft:command_block_minecart";
         materials[2256] = "minecraft:record_13";
         materials[2257] = "minecraft:record_cat";
         materials[2258] = "minecraft:record_blocks";
         materials[2259] = "minecraft:record_chirp";
         materials[2260] = "minecraft:record_far";
         materials[2261] = "minecraft:record_mall";
         materials[2262] = "minecraft:record_mellohi";
         materials[2263] = "minecraft:record_stal";
         materials[2264] = "minecraft:record_strad";
         materials[2265] = "minecraft:record_ward";
         materials[2266] = "minecraft:record_11";
         materials[2267] = "minecraft:record_wait";
         materials[409] = "minecraft:prismarine_shard";
         materials[410] = "minecraft:prismarine_crystals";
         materials[411] = "minecraft:rabbit";
         materials[412] = "minecraft:cooked_rabbit";
         materials[413] = "minecraft:rabbit_stew";
         materials[414] = "minecraft:rabbit_foot";
         materials[415] = "minecraft:rabbit_hide";
         materials[416] = "minecraft:armor_stand";
         materials[423] = "minecraft:mutton";
         materials[424] = "minecraft:cooked_mutton";
         materials[425] = "minecraft:banner";
         materials[426] = "minecraft:end_crystal";
         materials[427] = "minecraft:spruce_door";
         materials[428] = "minecraft:birch_door";
         materials[429] = "minecraft:jungle_door";
         materials[430] = "minecraft:acacia_door";
         materials[431] = "minecraft:dark_oak_door";
         materials[432] = "minecraft:chorus_fruit";
         materials[433] = "minecraft:chorus_fruit_popped";
         materials[434] = "minecraft:beetroot";
         materials[435] = "minecraft:beetroot_seeds";
         materials[436] = "minecraft:beetroot_soup";
         materials[437] = "minecraft:dragon_breath";
         materials[438] = "minecraft:splash_potion";
         materials[439] = "minecraft:spectral_arrow";
         materials[440] = "minecraft:tipped_arrow";
         materials[441] = "minecraft:lingering_potion";
         materials[442] = "minecraft:shield";
         materials[443] = "minecraft:elytra";
         materials[444] = "minecraft:spruce_boat";
         materials[445] = "minecraft:birch_boat";
         materials[446] = "minecraft:jungle_boat";
         materials[447] = "minecraft:acacia_boat";
         materials[448] = "minecraft:dark_oak_boat";
         materials[449] = "minecraft:totem_of_undying";
         materials[450] = "minecraft:shulker_shell";
         materials[452] = "minecraft:iron_nugget";
         materials[453] = "minecraft:knowledge_book";
      }
   }

   private static class DataConverterMinecart implements PaperweightDataConverters.DataConverter {
      private static final List<String> a = Lists.newArrayList(
         new String[]{"MinecartRideable", "MinecartChest", "MinecartFurnace", "MinecartTNT", "MinecartSpawner", "MinecartHopper", "MinecartCommandBlock"}
      );

      DataConverterMinecart() {
      }

      @Override
      public int getDataVersion() {
         return 106;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("Minecart".equals(cmp.getString("id"))) {
            String s = "MinecartRideable";
            int i = cmp.getInt("Type");
            if (i > 0 && i < a.size()) {
               s = a.get(i);
            }

            cmp.putString("id", s);
            cmp.remove("Type");
         }

         return cmp;
      }
   }

   private static class DataConverterMobSpawner implements PaperweightDataConverters.DataConverter {
      DataConverterMobSpawner() {
      }

      @Override
      public int getDataVersion() {
         return 107;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if (!"MobSpawner".equals(cmp.getString("id"))) {
            return cmp;
         } else {
            if (cmp.contains("EntityId", 8)) {
               String s = cmp.getString("EntityId");
               CompoundTag nbttagcompound1 = cmp.getCompound("SpawnData");
               nbttagcompound1.putString("id", s.isEmpty() ? "Pig" : s);
               cmp.put("SpawnData", nbttagcompound1);
               cmp.remove("EntityId");
            }

            if (cmp.contains("SpawnPotentials", 9)) {
               ListTag nbttaglist = cmp.getList("SpawnPotentials", 10);

               for (int i = 0; i < nbttaglist.size(); i++) {
                  CompoundTag nbttagcompound2 = nbttaglist.getCompound(i);
                  if (nbttagcompound2.contains("Type", 8)) {
                     CompoundTag nbttagcompound3 = nbttagcompound2.getCompound("Properties");
                     nbttagcompound3.putString("id", nbttagcompound2.getString("Type"));
                     nbttagcompound2.put("Entity", nbttagcompound3);
                     nbttagcompound2.remove("Type");
                     nbttagcompound2.remove("Properties");
                  }
               }
            }

            return cmp;
         }
      }
   }

   private static class DataConverterPotionId implements PaperweightDataConverters.DataConverter {
      private static final String[] potions = new String[128];

      DataConverterPotionId() {
      }

      @Override
      public int getDataVersion() {
         return 102;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("minecraft:potion".equals(cmp.getString("id"))) {
            CompoundTag nbttagcompound1 = cmp.getCompound("tag");
            short short0 = cmp.getShort("Damage");
            if (!nbttagcompound1.contains("Potion", 8)) {
               String s = potions[short0 & 127];
               nbttagcompound1.putString("Potion", s == null ? "minecraft:water" : s);
               cmp.put("tag", nbttagcompound1);
               if ((short0 & 16384) == 16384) {
                  cmp.putString("id", "minecraft:splash_potion");
               }
            }

            if (short0 != 0) {
               cmp.putShort("Damage", (short)0);
            }
         }

         return cmp;
      }

      static {
         potions[0] = "minecraft:water";
         potions[1] = "minecraft:regeneration";
         potions[2] = "minecraft:swiftness";
         potions[3] = "minecraft:fire_resistance";
         potions[4] = "minecraft:poison";
         potions[5] = "minecraft:healing";
         potions[6] = "minecraft:night_vision";
         potions[7] = null;
         potions[8] = "minecraft:weakness";
         potions[9] = "minecraft:strength";
         potions[10] = "minecraft:slowness";
         potions[11] = "minecraft:leaping";
         potions[12] = "minecraft:harming";
         potions[13] = "minecraft:water_breathing";
         potions[14] = "minecraft:invisibility";
         potions[15] = null;
         potions[16] = "minecraft:awkward";
         potions[17] = "minecraft:regeneration";
         potions[18] = "minecraft:swiftness";
         potions[19] = "minecraft:fire_resistance";
         potions[20] = "minecraft:poison";
         potions[21] = "minecraft:healing";
         potions[22] = "minecraft:night_vision";
         potions[23] = null;
         potions[24] = "minecraft:weakness";
         potions[25] = "minecraft:strength";
         potions[26] = "minecraft:slowness";
         potions[27] = "minecraft:leaping";
         potions[28] = "minecraft:harming";
         potions[29] = "minecraft:water_breathing";
         potions[30] = "minecraft:invisibility";
         potions[31] = null;
         potions[32] = "minecraft:thick";
         potions[33] = "minecraft:strong_regeneration";
         potions[34] = "minecraft:strong_swiftness";
         potions[35] = "minecraft:fire_resistance";
         potions[36] = "minecraft:strong_poison";
         potions[37] = "minecraft:strong_healing";
         potions[38] = "minecraft:night_vision";
         potions[39] = null;
         potions[40] = "minecraft:weakness";
         potions[41] = "minecraft:strong_strength";
         potions[42] = "minecraft:slowness";
         potions[43] = "minecraft:strong_leaping";
         potions[44] = "minecraft:strong_harming";
         potions[45] = "minecraft:water_breathing";
         potions[46] = "minecraft:invisibility";
         potions[47] = null;
         potions[48] = null;
         potions[49] = "minecraft:strong_regeneration";
         potions[50] = "minecraft:strong_swiftness";
         potions[51] = "minecraft:fire_resistance";
         potions[52] = "minecraft:strong_poison";
         potions[53] = "minecraft:strong_healing";
         potions[54] = "minecraft:night_vision";
         potions[55] = null;
         potions[56] = "minecraft:weakness";
         potions[57] = "minecraft:strong_strength";
         potions[58] = "minecraft:slowness";
         potions[59] = "minecraft:strong_leaping";
         potions[60] = "minecraft:strong_harming";
         potions[61] = "minecraft:water_breathing";
         potions[62] = "minecraft:invisibility";
         potions[63] = null;
         potions[64] = "minecraft:mundane";
         potions[65] = "minecraft:long_regeneration";
         potions[66] = "minecraft:long_swiftness";
         potions[67] = "minecraft:long_fire_resistance";
         potions[68] = "minecraft:long_poison";
         potions[69] = "minecraft:healing";
         potions[70] = "minecraft:long_night_vision";
         potions[71] = null;
         potions[72] = "minecraft:long_weakness";
         potions[73] = "minecraft:long_strength";
         potions[74] = "minecraft:long_slowness";
         potions[75] = "minecraft:long_leaping";
         potions[76] = "minecraft:harming";
         potions[77] = "minecraft:long_water_breathing";
         potions[78] = "minecraft:long_invisibility";
         potions[79] = null;
         potions[80] = "minecraft:awkward";
         potions[81] = "minecraft:long_regeneration";
         potions[82] = "minecraft:long_swiftness";
         potions[83] = "minecraft:long_fire_resistance";
         potions[84] = "minecraft:long_poison";
         potions[85] = "minecraft:healing";
         potions[86] = "minecraft:long_night_vision";
         potions[87] = null;
         potions[88] = "minecraft:long_weakness";
         potions[89] = "minecraft:long_strength";
         potions[90] = "minecraft:long_slowness";
         potions[91] = "minecraft:long_leaping";
         potions[92] = "minecraft:harming";
         potions[93] = "minecraft:long_water_breathing";
         potions[94] = "minecraft:long_invisibility";
         potions[95] = null;
         potions[96] = "minecraft:thick";
         potions[97] = "minecraft:regeneration";
         potions[98] = "minecraft:swiftness";
         potions[99] = "minecraft:long_fire_resistance";
         potions[100] = "minecraft:poison";
         potions[101] = "minecraft:strong_healing";
         potions[102] = "minecraft:long_night_vision";
         potions[103] = null;
         potions[104] = "minecraft:long_weakness";
         potions[105] = "minecraft:strength";
         potions[106] = "minecraft:long_slowness";
         potions[107] = "minecraft:leaping";
         potions[108] = "minecraft:strong_harming";
         potions[109] = "minecraft:long_water_breathing";
         potions[110] = "minecraft:long_invisibility";
         potions[111] = null;
         potions[112] = null;
         potions[113] = "minecraft:regeneration";
         potions[114] = "minecraft:swiftness";
         potions[115] = "minecraft:long_fire_resistance";
         potions[116] = "minecraft:poison";
         potions[117] = "minecraft:strong_healing";
         potions[118] = "minecraft:long_night_vision";
         potions[119] = null;
         potions[120] = "minecraft:long_weakness";
         potions[121] = "minecraft:strength";
         potions[122] = "minecraft:long_slowness";
         potions[123] = "minecraft:leaping";
         potions[124] = "minecraft:strong_harming";
         potions[125] = "minecraft:long_water_breathing";
         potions[126] = "minecraft:long_invisibility";
         potions[127] = null;
      }
   }

   private static class DataConverterPotionWater implements PaperweightDataConverters.DataConverter {
      DataConverterPotionWater() {
      }

      @Override
      public int getDataVersion() {
         return 806;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         String s = cmp.getString("id");
         if ("minecraft:potion".equals(s)
            || "minecraft:splash_potion".equals(s)
            || "minecraft:lingering_potion".equals(s)
            || "minecraft:tipped_arrow".equals(s)) {
            CompoundTag nbttagcompound1 = cmp.getCompound("tag");
            if (!nbttagcompound1.contains("Potion", 8)) {
               nbttagcompound1.putString("Potion", "minecraft:water");
            }

            if (!cmp.contains("tag", 10)) {
               cmp.put("tag", nbttagcompound1);
            }
         }

         return cmp;
      }
   }

   private static class DataConverterRiding implements PaperweightDataConverters.DataConverter {
      DataConverterRiding() {
      }

      @Override
      public int getDataVersion() {
         return 135;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         while (cmp.contains("Riding", 10)) {
            CompoundTag nbttagcompound1 = this.b(cmp);
            this.convert(cmp, nbttagcompound1);
            cmp = nbttagcompound1;
         }

         return cmp;
      }

      protected void convert(CompoundTag nbttagcompound, CompoundTag nbttagcompound1) {
         ListTag nbttaglist = new ListTag();
         nbttaglist.add(nbttagcompound);
         nbttagcompound1.put("Passengers", nbttaglist);
      }

      protected CompoundTag b(CompoundTag nbttagcompound) {
         CompoundTag nbttagcompound1 = nbttagcompound.getCompound("Riding");
         nbttagcompound.remove("Riding");
         return nbttagcompound1;
      }
   }

   private static class DataConverterSaddle implements PaperweightDataConverters.DataConverter {
      DataConverterSaddle() {
      }

      @Override
      public int getDataVersion() {
         return 110;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("EntityHorse".equals(cmp.getString("id")) && !cmp.contains("SaddleItem", 10) && cmp.getBoolean("Saddle")) {
            CompoundTag nbttagcompound1 = new CompoundTag();
            nbttagcompound1.putString("id", "minecraft:saddle");
            nbttagcompound1.putByte("Count", (byte)1);
            nbttagcompound1.putShort("Damage", (short)0);
            cmp.put("SaddleItem", nbttagcompound1);
            cmp.remove("Saddle");
         }

         return cmp;
      }
   }

   private static class DataConverterShulker implements PaperweightDataConverters.DataConverter {
      DataConverterShulker() {
      }

      @Override
      public int getDataVersion() {
         return 808;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("minecraft:shulker".equals(cmp.getString("id")) && !cmp.contains("Color", 99)) {
            cmp.putByte("Color", (byte)10);
         }

         return cmp;
      }
   }

   private static class DataConverterShulkerBoxBlock implements PaperweightDataConverters.DataConverter {
      DataConverterShulkerBoxBlock() {
      }

      @Override
      public int getDataVersion() {
         return 813;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("minecraft:shulker".equals(cmp.getString("id"))) {
            cmp.remove("Color");
         }

         return cmp;
      }
   }

   private static class DataConverterShulkerBoxItem implements PaperweightDataConverters.DataConverter {
      public static final String[] a = new String[]{
         "minecraft:white_shulker_box",
         "minecraft:orange_shulker_box",
         "minecraft:magenta_shulker_box",
         "minecraft:light_blue_shulker_box",
         "minecraft:yellow_shulker_box",
         "minecraft:lime_shulker_box",
         "minecraft:pink_shulker_box",
         "minecraft:gray_shulker_box",
         "minecraft:silver_shulker_box",
         "minecraft:cyan_shulker_box",
         "minecraft:purple_shulker_box",
         "minecraft:blue_shulker_box",
         "minecraft:brown_shulker_box",
         "minecraft:green_shulker_box",
         "minecraft:red_shulker_box",
         "minecraft:black_shulker_box"
      };

      DataConverterShulkerBoxItem() {
      }

      @Override
      public int getDataVersion() {
         return 813;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("minecraft:shulker_box".equals(cmp.getString("id")) && cmp.contains("tag", 10)) {
            CompoundTag nbttagcompound1 = cmp.getCompound("tag");
            if (nbttagcompound1.contains("BlockEntityTag", 10)) {
               CompoundTag nbttagcompound2 = nbttagcompound1.getCompound("BlockEntityTag");
               if (nbttagcompound2.getList("Items", 10).isEmpty()) {
                  nbttagcompound2.remove("Items");
               }

               int i = nbttagcompound2.getInt("Color");
               nbttagcompound2.remove("Color");
               if (nbttagcompound2.isEmpty()) {
                  nbttagcompound1.remove("BlockEntityTag");
               }

               if (nbttagcompound1.isEmpty()) {
                  cmp.remove("tag");
               }

               cmp.putString("id", a[i % 16]);
            }
         }

         return cmp;
      }
   }

   private static class DataConverterSignText implements PaperweightDataConverters.DataConverter {
      public static final Gson a = new GsonBuilder().registerTypeAdapter(Component.class, new JsonDeserializer() {
         MutableComponent a(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            if (jsonelement.isJsonPrimitive()) {
               return Component.literal(jsonelement.getAsString());
            } else if (jsonelement.isJsonArray()) {
               JsonArray jsonarray = jsonelement.getAsJsonArray();
               MutableComponent ichatbasecomponent = null;

               for (JsonElement jsonelement1 : jsonarray) {
                  MutableComponent ichatbasecomponent1 = this.a(jsonelement1, jsonelement1.getClass(), jsondeserializationcontext);
                  if (ichatbasecomponent == null) {
                     ichatbasecomponent = ichatbasecomponent1;
                  } else {
                     ichatbasecomponent.append(ichatbasecomponent1);
                  }
               }

               return ichatbasecomponent;
            } else {
               throw new JsonParseException("Don't know how to turn " + jsonelement + " into a Component");
            }
         }

         public Object deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            return this.a(jsonelement, type, jsondeserializationcontext);
         }
      }).create();

      DataConverterSignText() {
      }

      @Override
      public int getDataVersion() {
         return 101;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("Sign".equals(cmp.getString("id"))) {
            this.convert(cmp, "Text1");
            this.convert(cmp, "Text2");
            this.convert(cmp, "Text3");
            this.convert(cmp, "Text4");
         }

         return cmp;
      }

      private void convert(CompoundTag nbttagcompound, String s) {
         String s1 = nbttagcompound.getString(s);
         Component object = null;
         if (!"null".equals(s1) && !StringUtil.isNullOrEmpty(s1)) {
            if (s1.charAt(0) == '"' && s1.charAt(s1.length() - 1) == '"' || s1.charAt(0) == '{' && s1.charAt(s1.length() - 1) == '}') {
               try {
                  object = (Component)GsonHelper.fromJson(a, s1, Component.class, true);
                  if (object == null) {
                     object = Component.literal("");
                  }
               } catch (JsonParseException var8) {
               }

               if (object == null) {
                  try {
                     object = Serializer.fromJson(s1, MinecraftServer.getServer().registryAccess());
                  } catch (JsonParseException var7) {
                  }
               }

               if (object == null) {
                  try {
                     object = Serializer.fromJsonLenient(s1, MinecraftServer.getServer().registryAccess());
                  } catch (JsonParseException var6) {
                  }
               }

               if (object == null) {
                  object = Component.literal(s1);
               }
            } else {
               object = Component.literal(s1);
            }
         } else {
            object = Component.literal("");
         }

         nbttagcompound.putString(s, Serializer.toJson(object, MinecraftServer.getServer().registryAccess()));
      }
   }

   private static class DataConverterSkeleton implements PaperweightDataConverters.DataConverter {
      DataConverterSkeleton() {
      }

      @Override
      public int getDataVersion() {
         return 701;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         String s = cmp.getString("id");
         if ("Skeleton".equals(s)) {
            int i = cmp.getInt("SkeletonType");
            if (i == 1) {
               cmp.putString("id", "WitherSkeleton");
            } else if (i == 2) {
               cmp.putString("id", "Stray");
            }

            cmp.remove("SkeletonType");
         }

         return cmp;
      }
   }

   private static class DataConverterSpawnEgg implements PaperweightDataConverters.DataConverter {
      private static final String[] eggs = new String[256];

      DataConverterSpawnEgg() {
      }

      @Override
      public int getDataVersion() {
         return 105;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("minecraft:spawn_egg".equals(cmp.getString("id"))) {
            CompoundTag nbttagcompound1 = cmp.getCompound("tag");
            CompoundTag nbttagcompound2 = nbttagcompound1.getCompound("EntityTag");
            short short0 = cmp.getShort("Damage");
            if (!nbttagcompound2.contains("id", 8)) {
               String s = eggs[short0 & 255];
               if (s != null) {
                  nbttagcompound2.putString("id", s);
                  nbttagcompound1.put("EntityTag", nbttagcompound2);
                  cmp.put("tag", nbttagcompound1);
               }
            }

            if (short0 != 0) {
               cmp.putShort("Damage", (short)0);
            }
         }

         return cmp;
      }

      static {
         eggs[1] = "Item";
         eggs[2] = "XPOrb";
         eggs[7] = "ThrownEgg";
         eggs[8] = "LeashKnot";
         eggs[9] = "Painting";
         eggs[10] = "Arrow";
         eggs[11] = "Snowball";
         eggs[12] = "Fireball";
         eggs[13] = "SmallFireball";
         eggs[14] = "ThrownEnderpearl";
         eggs[15] = "EyeOfEnderSignal";
         eggs[16] = "ThrownPotion";
         eggs[17] = "ThrownExpBottle";
         eggs[18] = "ItemFrame";
         eggs[19] = "WitherSkull";
         eggs[20] = "PrimedTnt";
         eggs[21] = "FallingSand";
         eggs[22] = "FireworksRocketEntity";
         eggs[23] = "TippedArrow";
         eggs[24] = "SpectralArrow";
         eggs[25] = "ShulkerBullet";
         eggs[26] = "DragonFireball";
         eggs[30] = "ArmorStand";
         eggs[41] = "Boat";
         eggs[42] = "MinecartRideable";
         eggs[43] = "MinecartChest";
         eggs[44] = "MinecartFurnace";
         eggs[45] = "MinecartTNT";
         eggs[46] = "MinecartHopper";
         eggs[47] = "MinecartSpawner";
         eggs[40] = "MinecartCommandBlock";
         eggs[48] = "Mob";
         eggs[49] = "Monster";
         eggs[50] = "Creeper";
         eggs[51] = "Skeleton";
         eggs[52] = "Spider";
         eggs[53] = "Giant";
         eggs[54] = "Zombie";
         eggs[55] = "Slime";
         eggs[56] = "Ghast";
         eggs[57] = "PigZombie";
         eggs[58] = "Enderman";
         eggs[59] = "CaveSpider";
         eggs[60] = "Silverfish";
         eggs[61] = "Blaze";
         eggs[62] = "LavaSlime";
         eggs[63] = "EnderDragon";
         eggs[64] = "WitherBoss";
         eggs[65] = "Bat";
         eggs[66] = "Witch";
         eggs[67] = "Endermite";
         eggs[68] = "Guardian";
         eggs[69] = "Shulker";
         eggs[90] = "Pig";
         eggs[91] = "Sheep";
         eggs[92] = "Cow";
         eggs[93] = "Chicken";
         eggs[94] = "Squid";
         eggs[95] = "Wolf";
         eggs[96] = "MushroomCow";
         eggs[97] = "SnowMan";
         eggs[98] = "Ozelot";
         eggs[99] = "VillagerGolem";
         eggs[100] = "EntityHorse";
         eggs[101] = "Rabbit";
         eggs[120] = "Villager";
         eggs[200] = "EnderCrystal";
      }
   }

   private static class DataConverterTileEntity implements PaperweightDataConverters.DataConverter {
      private static final Map<String, String> a = Maps.newHashMap();

      DataConverterTileEntity() {
      }

      @Override
      public int getDataVersion() {
         return 704;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         String s = a.get(cmp.getString("id"));
         if (s != null) {
            cmp.putString("id", s);
         }

         return cmp;
      }

      static {
         a.put("Airportal", "minecraft:end_portal");
         a.put("Banner", "minecraft:banner");
         a.put("Beacon", "minecraft:beacon");
         a.put("Cauldron", "minecraft:brewing_stand");
         a.put("Chest", "minecraft:chest");
         a.put("Comparator", "minecraft:comparator");
         a.put("Control", "minecraft:command_block");
         a.put("DLDetector", "minecraft:daylight_detector");
         a.put("Dropper", "minecraft:dropper");
         a.put("EnchantTable", "minecraft:enchanting_table");
         a.put("EndGateway", "minecraft:end_gateway");
         a.put("EnderChest", "minecraft:ender_chest");
         a.put("FlowerPot", "minecraft:flower_pot");
         a.put("Furnace", "minecraft:furnace");
         a.put("Hopper", "minecraft:hopper");
         a.put("MobSpawner", "minecraft:mob_spawner");
         a.put("Music", "minecraft:noteblock");
         a.put("Piston", "minecraft:piston");
         a.put("RecordPlayer", "minecraft:jukebox");
         a.put("Sign", "minecraft:sign");
         a.put("Skull", "minecraft:skull");
         a.put("Structure", "minecraft:structure_block");
         a.put("Trap", "minecraft:dispenser");
      }
   }

   private static class DataConverterTotem implements PaperweightDataConverters.DataConverter {
      DataConverterTotem() {
      }

      @Override
      public int getDataVersion() {
         return 820;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("minecraft:totem".equals(cmp.getString("id"))) {
            cmp.putString("id", "minecraft:totem_of_undying");
         }

         return cmp;
      }
   }

   private static class DataConverterUUID implements PaperweightDataConverters.DataConverter {
      DataConverterUUID() {
      }

      @Override
      public int getDataVersion() {
         return 108;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if (cmp.contains("UUID", 8)) {
            cmp.putUUID("UUID", UUID.fromString(cmp.getString("UUID")));
         }

         return cmp;
      }
   }

   private static class DataConverterVBO implements PaperweightDataConverters.DataConverter {
      DataConverterVBO() {
      }

      @Override
      public int getDataVersion() {
         return 505;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         cmp.putString("useVbo", "true");
         return cmp;
      }
   }

   private static class DataConverterZombie implements PaperweightDataConverters.DataConverter {
      private static final Random a = new Random();

      DataConverterZombie() {
      }

      @Override
      public int getDataVersion() {
         return 502;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("Zombie".equals(cmp.getString("id")) && cmp.getBoolean("IsVillager")) {
            if (!cmp.contains("ZombieType", 99)) {
               int i = -1;
               if (cmp.contains("VillagerProfession", 99)) {
                  try {
                     i = this.convert(cmp.getInt("VillagerProfession"));
                  } catch (RuntimeException var4) {
                  }
               }

               if (i == -1) {
                  i = this.convert(a.nextInt(6));
               }

               cmp.putInt("ZombieType", i);
            }

            cmp.remove("IsVillager");
         }

         return cmp;
      }

      private int convert(int i) {
         return i >= 0 && i < 6 ? i : -1;
      }
   }

   private static class DataConverterZombieType implements PaperweightDataConverters.DataConverter {
      DataConverterZombieType() {
      }

      @Override
      public int getDataVersion() {
         return 702;
      }

      @Override
      public CompoundTag convert(CompoundTag cmp) {
         if ("Zombie".equals(cmp.getString("id"))) {
            int i = cmp.getInt("ZombieType");
            switch (i) {
               case 0:
               default:
                  break;
               case 1:
               case 2:
               case 3:
               case 4:
               case 5:
                  cmp.putString("id", "ZombieVillager");
                  cmp.putInt("Profession", i - 1);
                  break;
               case 6:
                  cmp.putString("id", "Husk");
            }

            cmp.remove("ZombieType");
         }

         return cmp;
      }
   }

   public interface DataInspector {
      CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer);
   }

   private static class DataInspectorBlockEntity implements PaperweightDataConverters.DataInspector {
      private static final Map<String, String> b = Maps.newHashMap();
      private static final Map<String, String> c = Maps.newHashMap();

      DataInspectorBlockEntity() {
      }

      @Nullable
      private static String convertEntityId(int i, String s) {
         String key = ResourceLocation.parse(s).toString();
         return i < 515 && b.containsKey(key) ? b.get(key) : c.get(key);
      }

      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         if (!cmp.contains("tag", 10)) {
            return cmp;
         } else {
            CompoundTag nbttagcompound1 = cmp.getCompound("tag");
            if (nbttagcompound1.contains("BlockEntityTag", 10)) {
               CompoundTag nbttagcompound2 = nbttagcompound1.getCompound("BlockEntityTag");
               String s = cmp.getString("id");
               String s1 = convertEntityId(sourceVer, s);
               boolean flag;
               if (s1 == null) {
                  flag = false;
               } else {
                  flag = !nbttagcompound2.contains("id");
                  nbttagcompound2.putString("id", s1);
               }

               PaperweightDataConverters.convert(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, nbttagcompound2, sourceVer, targetVer);
               if (flag) {
                  nbttagcompound2.remove("id");
               }
            }

            return cmp;
         }
      }

      static {
         Map map = b;
         map.put("minecraft:furnace", "Furnace");
         map.put("minecraft:lit_furnace", "Furnace");
         map.put("minecraft:chest", "Chest");
         map.put("minecraft:trapped_chest", "Chest");
         map.put("minecraft:ender_chest", "EnderChest");
         map.put("minecraft:jukebox", "RecordPlayer");
         map.put("minecraft:dispenser", "Trap");
         map.put("minecraft:dropper", "Dropper");
         map.put("minecraft:sign", "Sign");
         map.put("minecraft:mob_spawner", "MobSpawner");
         map.put("minecraft:noteblock", "Music");
         map.put("minecraft:brewing_stand", "Cauldron");
         map.put("minecraft:enhanting_table", "EnchantTable");
         map.put("minecraft:command_block", "CommandBlock");
         map.put("minecraft:beacon", "Beacon");
         map.put("minecraft:skull", "Skull");
         map.put("minecraft:daylight_detector", "DLDetector");
         map.put("minecraft:hopper", "Hopper");
         map.put("minecraft:banner", "Banner");
         map.put("minecraft:flower_pot", "FlowerPot");
         map.put("minecraft:repeating_command_block", "CommandBlock");
         map.put("minecraft:chain_command_block", "CommandBlock");
         map.put("minecraft:standing_sign", "Sign");
         map.put("minecraft:wall_sign", "Sign");
         map.put("minecraft:piston_head", "Piston");
         map.put("minecraft:daylight_detector_inverted", "DLDetector");
         map.put("minecraft:unpowered_comparator", "Comparator");
         map.put("minecraft:powered_comparator", "Comparator");
         map.put("minecraft:wall_banner", "Banner");
         map.put("minecraft:standing_banner", "Banner");
         map.put("minecraft:structure_block", "Structure");
         map.put("minecraft:end_portal", "Airportal");
         map.put("minecraft:end_gateway", "EndGateway");
         map.put("minecraft:shield", "Shield");
         map = c;
         map.put("minecraft:furnace", "minecraft:furnace");
         map.put("minecraft:lit_furnace", "minecraft:furnace");
         map.put("minecraft:chest", "minecraft:chest");
         map.put("minecraft:trapped_chest", "minecraft:chest");
         map.put("minecraft:ender_chest", "minecraft:enderchest");
         map.put("minecraft:jukebox", "minecraft:jukebox");
         map.put("minecraft:dispenser", "minecraft:dispenser");
         map.put("minecraft:dropper", "minecraft:dropper");
         map.put("minecraft:sign", "minecraft:sign");
         map.put("minecraft:mob_spawner", "minecraft:mob_spawner");
         map.put("minecraft:noteblock", "minecraft:noteblock");
         map.put("minecraft:brewing_stand", "minecraft:brewing_stand");
         map.put("minecraft:enhanting_table", "minecraft:enchanting_table");
         map.put("minecraft:command_block", "minecraft:command_block");
         map.put("minecraft:beacon", "minecraft:beacon");
         map.put("minecraft:skull", "minecraft:skull");
         map.put("minecraft:daylight_detector", "minecraft:daylight_detector");
         map.put("minecraft:hopper", "minecraft:hopper");
         map.put("minecraft:banner", "minecraft:banner");
         map.put("minecraft:flower_pot", "minecraft:flower_pot");
         map.put("minecraft:repeating_command_block", "minecraft:command_block");
         map.put("minecraft:chain_command_block", "minecraft:command_block");
         map.put("minecraft:shulker_box", "minecraft:shulker_box");
         map.put("minecraft:white_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:orange_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:magenta_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:light_blue_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:yellow_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:lime_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:pink_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:gray_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:silver_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:cyan_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:purple_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:blue_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:brown_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:green_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:red_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:black_shulker_box", "minecraft:shulker_box");
         map.put("minecraft:bed", "minecraft:bed");
         map.put("minecraft:standing_sign", "minecraft:sign");
         map.put("minecraft:wall_sign", "minecraft:sign");
         map.put("minecraft:piston_head", "minecraft:piston");
         map.put("minecraft:daylight_detector_inverted", "minecraft:daylight_detector");
         map.put("minecraft:unpowered_comparator", "minecraft:comparator");
         map.put("minecraft:powered_comparator", "minecraft:comparator");
         map.put("minecraft:wall_banner", "minecraft:banner");
         map.put("minecraft:standing_banner", "minecraft:banner");
         map.put("minecraft:structure_block", "minecraft:structure_block");
         map.put("minecraft:end_portal", "minecraft:end_portal");
         map.put("minecraft:end_gateway", "minecraft:end_gateway");
         map.put("minecraft:shield", "minecraft:shield");
      }
   }

   private static class DataInspectorChunks implements PaperweightDataConverters.DataInspector {
      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         if (cmp.contains("Level", 10)) {
            CompoundTag nbttagcompound1 = cmp.getCompound("Level");
            if (nbttagcompound1.contains("Entities", 9)) {
               ListTag nbttaglist = nbttagcompound1.getList("Entities", 10);

               for (int j = 0; j < nbttaglist.size(); j++) {
                  nbttaglist.set(
                     j, PaperweightDataConverters.convert(PaperweightDataConverters.LegacyType.ENTITY, (CompoundTag)nbttaglist.get(j), sourceVer, targetVer)
                  );
               }
            }

            if (nbttagcompound1.contains("TileEntities", 9)) {
               ListTag nbttaglist = nbttagcompound1.getList("TileEntities", 10);

               for (int j = 0; j < nbttaglist.size(); j++) {
                  nbttaglist.set(
                     j,
                     PaperweightDataConverters.convert(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, (CompoundTag)nbttaglist.get(j), sourceVer, targetVer)
                  );
               }
            }
         }

         return cmp;
      }
   }

   private static class DataInspectorCommandBlock implements PaperweightDataConverters.DataInspector {
      ResourceLocation tileEntityCommand = PaperweightDataConverters.getKey("TileEntityCommand");

      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         if (this.tileEntityCommand.equals(ResourceLocation.parse(cmp.getString("id")))) {
            cmp.putString("id", "Control");
            PaperweightDataConverters.convert(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, cmp, sourceVer, targetVer);
            cmp.putString("id", "MinecartCommandBlock");
         }

         return cmp;
      }
   }

   private static class DataInspectorEntity implements PaperweightDataConverters.DataInspector {
      DataInspectorEntity() {
      }

      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         CompoundTag nbttagcompound1 = cmp.getCompound("tag");
         if (nbttagcompound1.contains("EntityTag", 10)) {
            CompoundTag nbttagcompound2 = nbttagcompound1.getCompound("EntityTag");
            String s = cmp.getString("id");
            String s1;
            if ("minecraft:armor_stand".equals(s)) {
               s1 = sourceVer < 515 ? "ArmorStand" : "minecraft:armor_stand";
            } else {
               if (!"minecraft:spawn_egg".equals(s)) {
                  return cmp;
               }

               s1 = nbttagcompound2.getString("id");
            }

            boolean flag = !nbttagcompound2.contains("id", 8);
            nbttagcompound2.putString("id", s1);
            PaperweightDataConverters.convert(PaperweightDataConverters.LegacyType.ENTITY, nbttagcompound2, sourceVer, targetVer);
            if (flag) {
               nbttagcompound2.remove("id");
            }
         }

         return cmp;
      }
   }

   private static class DataInspectorEntityPassengers implements PaperweightDataConverters.DataInspector {
      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         if (cmp.contains("Passengers", 9)) {
            ListTag nbttaglist = cmp.getList("Passengers", 10);

            for (int j = 0; j < nbttaglist.size(); j++) {
               nbttaglist.set(
                  j, PaperweightDataConverters.convert(PaperweightDataConverters.LegacyType.ENTITY, nbttaglist.getCompound(j), sourceVer, targetVer)
               );
            }
         }

         return cmp;
      }
   }

   private static class DataInspectorItem extends PaperweightDataConverters.DataInspectorTagged {
      private final String[] keys;

      DataInspectorItem(String oclass, String... astring) {
         super(oclass);
         this.keys = astring;
      }

      @Override
      CompoundTag inspectChecked(CompoundTag nbttagcompound, int sourceVer, int targetVer) {
         for (String key : this.keys) {
            PaperweightDataConverters.convertItem(nbttagcompound, key, sourceVer, targetVer);
         }

         return nbttagcompound;
      }
   }

   private static class DataInspectorItemList extends PaperweightDataConverters.DataInspectorTagged {
      private final String[] keys;

      DataInspectorItemList(String oclass, String... astring) {
         super(oclass);
         this.keys = astring;
      }

      @Override
      CompoundTag inspectChecked(CompoundTag nbttagcompound, int sourceVer, int targetVer) {
         for (String s : this.keys) {
            PaperweightDataConverters.convertItems(nbttagcompound, s, sourceVer, targetVer);
         }

         return nbttagcompound;
      }
   }

   private static class DataInspectorLevelPlayer implements PaperweightDataConverters.DataInspector {
      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         if (cmp.contains("Player", 10)) {
            PaperweightDataConverters.convertCompound(PaperweightDataConverters.LegacyType.PLAYER, cmp, "Player", sourceVer, targetVer);
         }

         return cmp;
      }
   }

   private static class DataInspectorMobSpawnerMinecart implements PaperweightDataConverters.DataInspector {
      ResourceLocation entityMinecartMobSpawner = PaperweightDataConverters.getKey("EntityMinecartMobSpawner");
      ResourceLocation tileEntityMobSpawner = PaperweightDataConverters.getKey("TileEntityMobSpawner");

      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         String s = cmp.getString("id");
         if (this.entityMinecartMobSpawner.equals(ResourceLocation.parse(s))) {
            cmp.putString("id", this.tileEntityMobSpawner.toString());
            PaperweightDataConverters.convert(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, cmp, sourceVer, targetVer);
            cmp.putString("id", s);
         }

         return cmp;
      }
   }

   private static class DataInspectorMobSpawnerMobs implements PaperweightDataConverters.DataInspector {
      ResourceLocation tileEntityMobSpawner = PaperweightDataConverters.getKey("TileEntityMobSpawner");

      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         if (this.tileEntityMobSpawner.equals(ResourceLocation.parse(cmp.getString("id")))) {
            if (cmp.contains("SpawnPotentials", 9)) {
               ListTag nbttaglist = cmp.getList("SpawnPotentials", 10);

               for (int j = 0; j < nbttaglist.size(); j++) {
                  CompoundTag nbttagcompound1 = nbttaglist.getCompound(j);
                  PaperweightDataConverters.convertCompound(PaperweightDataConverters.LegacyType.ENTITY, nbttagcompound1, "Entity", sourceVer, targetVer);
               }
            }

            PaperweightDataConverters.convertCompound(PaperweightDataConverters.LegacyType.ENTITY, cmp, "SpawnData", sourceVer, targetVer);
         }

         return cmp;
      }
   }

   private static class DataInspectorPlayer implements PaperweightDataConverters.DataInspector {
      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         PaperweightDataConverters.convertItems(cmp, "Inventory", sourceVer, targetVer);
         PaperweightDataConverters.convertItems(cmp, "EnderItems", sourceVer, targetVer);
         if (cmp.contains("ShoulderEntityLeft", 10)) {
            PaperweightDataConverters.convertCompound(PaperweightDataConverters.LegacyType.ENTITY, cmp, "ShoulderEntityLeft", sourceVer, targetVer);
         }

         if (cmp.contains("ShoulderEntityRight", 10)) {
            PaperweightDataConverters.convertCompound(PaperweightDataConverters.LegacyType.ENTITY, cmp, "ShoulderEntityRight", sourceVer, targetVer);
         }

         return cmp;
      }
   }

   private static class DataInspectorPlayerVehicle implements PaperweightDataConverters.DataInspector {
      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         if (cmp.contains("RootVehicle", 10)) {
            CompoundTag nbttagcompound1 = cmp.getCompound("RootVehicle");
            if (nbttagcompound1.contains("Entity", 10)) {
               PaperweightDataConverters.convertCompound(PaperweightDataConverters.LegacyType.ENTITY, nbttagcompound1, "Entity", sourceVer, targetVer);
            }
         }

         return cmp;
      }
   }

   private static class DataInspectorStructure implements PaperweightDataConverters.DataInspector {
      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         if (cmp.contains("entities", 9)) {
            ListTag nbttaglist = cmp.getList("entities", 10);

            for (int j = 0; j < nbttaglist.size(); j++) {
               CompoundTag nbttagcompound1 = (CompoundTag)nbttaglist.get(j);
               if (nbttagcompound1.contains("nbt", 10)) {
                  PaperweightDataConverters.convertCompound(PaperweightDataConverters.LegacyType.ENTITY, nbttagcompound1, "nbt", sourceVer, targetVer);
               }
            }
         }

         if (cmp.contains("blocks", 9)) {
            ListTag nbttaglist = cmp.getList("blocks", 10);

            for (int jx = 0; jx < nbttaglist.size(); jx++) {
               CompoundTag nbttagcompound1 = (CompoundTag)nbttaglist.get(jx);
               if (nbttagcompound1.contains("nbt", 10)) {
                  PaperweightDataConverters.convertCompound(PaperweightDataConverters.LegacyType.BLOCK_ENTITY, nbttagcompound1, "nbt", sourceVer, targetVer);
               }
            }
         }

         return cmp;
      }
   }

   private abstract static class DataInspectorTagged implements PaperweightDataConverters.DataInspector {
      private final ResourceLocation key;

      DataInspectorTagged(String type) {
         this.key = PaperweightDataConverters.getKey(type);
      }

      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         if (this.key.equals(ResourceLocation.parse(cmp.getString("id")))) {
            cmp = this.inspectChecked(cmp, sourceVer, targetVer);
         }

         return cmp;
      }

      abstract CompoundTag inspectChecked(CompoundTag nbttagcompound, int sourceVer, int targetVer);
   }

   private static class DataInspectorVillagers implements PaperweightDataConverters.DataInspector {
      ResourceLocation entityVillager = PaperweightDataConverters.getKey("EntityVillager");

      @Override
      public CompoundTag inspect(CompoundTag cmp, int sourceVer, int targetVer) {
         if (this.entityVillager.equals(ResourceLocation.parse(cmp.getString("id"))) && cmp.contains("Offers", 10)) {
            CompoundTag nbttagcompound1 = cmp.getCompound("Offers");
            if (nbttagcompound1.contains("Recipes", 9)) {
               ListTag nbttaglist = nbttagcompound1.getList("Recipes", 10);

               for (int j = 0; j < nbttaglist.size(); j++) {
                  CompoundTag nbttagcompound2 = nbttaglist.getCompound(j);
                  PaperweightDataConverters.convertItem(nbttagcompound2, "buy", sourceVer, targetVer);
                  PaperweightDataConverters.convertItem(nbttagcompound2, "buyB", sourceVer, targetVer);
                  PaperweightDataConverters.convertItem(nbttagcompound2, "sell", sourceVer, targetVer);
                  nbttaglist.set(j, nbttagcompound2);
               }
            }
         }

         return cmp;
      }
   }

   public static enum LegacyType {
      LEVEL(References.LEVEL),
      PLAYER(References.PLAYER),
      CHUNK(References.CHUNK),
      BLOCK_ENTITY(References.BLOCK_ENTITY),
      ENTITY(References.ENTITY),
      ITEM_INSTANCE(References.ITEM_STACK),
      OPTIONS(References.OPTIONS),
      STRUCTURE(References.STRUCTURE);

      private final TypeReference type;

      private LegacyType(TypeReference type) {
         this.type = type;
         PaperweightDataConverters.DFU_TO_LEGACY.put(type.typeName(), this);
      }

      public TypeReference getDFUType() {
         return this.type;
      }
   }

   private class WrappedDataFixer implements com.mojang.datafixers.DataFixer {
      private final com.mojang.datafixers.DataFixer realFixer;

      WrappedDataFixer(com.mojang.datafixers.DataFixer realFixer) {
         this.realFixer = realFixer;
      }

      public <T> Dynamic<T> update(TypeReference type, Dynamic<T> dynamic, int sourceVer, int targetVer) {
         PaperweightDataConverters.LegacyType legacyType = PaperweightDataConverters.DFU_TO_LEGACY.get(type.typeName());
         if (sourceVer < 1343 && legacyType != null) {
            CompoundTag cmp = (CompoundTag)dynamic.getValue();
            int desiredVersion = Math.min(targetVer, 1343);
            cmp = this.convert(legacyType, cmp, sourceVer, desiredVersion);
            sourceVer = desiredVersion;
            dynamic = new Dynamic(PaperweightDataConverters.OPS_NBT, cmp);
         }

         return this.realFixer.update(type, dynamic, sourceVer, targetVer);
      }

      private CompoundTag convert(PaperweightDataConverters.LegacyType type, CompoundTag cmp, int sourceVer, int desiredVersion) {
         List<PaperweightDataConverters.DataConverter> converters = PaperweightDataConverters.this.converters.get(type);
         if (converters != null && !converters.isEmpty()) {
            for (PaperweightDataConverters.DataConverter converter : converters) {
               int dataVersion = converter.getDataVersion();
               if (dataVersion > sourceVer && dataVersion <= desiredVersion) {
                  cmp = converter.convert(cmp);
               }
            }
         }

         List<PaperweightDataConverters.DataInspector> inspectors = PaperweightDataConverters.this.inspectors.get(type);
         if (inspectors != null && !inspectors.isEmpty()) {
            for (PaperweightDataConverters.DataInspector inspector : inspectors) {
               cmp = inspector.inspect(cmp, sourceVer, desiredVersion);
            }
         }

         return cmp;
      }

      public Schema getSchema(int i) {
         return this.realFixer.getSchema(i);
      }
   }
}
