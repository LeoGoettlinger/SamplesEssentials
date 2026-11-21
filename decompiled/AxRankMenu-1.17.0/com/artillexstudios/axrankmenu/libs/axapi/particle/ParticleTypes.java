package com.artillexstudios.axrankmenu.libs.axapi.particle;

import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.ColorParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.DustColorTransitionParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.DustParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.FloatParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.GeyserParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.IntegerParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.ItemStackParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.TrailParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.particle.option.VibrationParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.ColorParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.DustColorTransitionParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.DustParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.FloatParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.GeyserParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.IntegerParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.ItemStackParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.SimpleParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.SpellParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.TrailParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.VarIntParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.type.VibrationParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.utils.Maps;
import com.artillexstudios.axrankmenu.libs.axapi.utils.Version;
import com.artillexstudios.axrankmenu.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class ParticleTypes {
   private static final Int2ObjectArrayMap<ParticleType<?>> registry = new Int2ObjectArrayMap();
   private static final Object2IntArrayMap<ParticleType<?>> reverseRegistry = new Object2IntArrayMap();
   public static final ParticleType<?> AMBIENT_ENTITY_EFFECT = new SimpleParticleType();
   public static final ParticleType<?> DRIPPING_CHERRY_LEAVES = new SimpleParticleType();
   public static final ParticleType<?> FALLING_CHERRY_LEAVES = new SimpleParticleType();
   public static final ParticleType<?> LANDING_CHERRY_LEAVES = new SimpleParticleType();
   public static final ParticleType<?> GUST_DUST = new SimpleParticleType();
   public static final ParticleType<?> GUST_EMITTER = new SimpleParticleType();
   public static final ParticleType<?> GUST_EMITTER_SMALL = new SimpleParticleType();
   public static final ParticleType<ColorParticleOption> TINTED_LEAVES = new ColorParticleType();
   public static final ParticleType<?> ANGRY_VILLAGER = new SimpleParticleType();
   public static final ParticleType<IntegerParticleOption> BLOCK = new VarIntParticleType();
   public static final ParticleType<IntegerParticleOption> BLOCK_MARKER = new VarIntParticleType();
   public static final ParticleType<?> BUBBLE = new SimpleParticleType();
   public static final ParticleType<?> SULFUR_BUBBLES = new SimpleParticleType();
   public static final ParticleType<?> NOXIOUS_GAS = new SimpleParticleType();
   public static final ParticleType<?> NOXIOUS_GAS_CLOUD = new SimpleParticleType();
   public static final ParticleType<GeyserParticleOption> GEYSER = new GeyserParticleType();
   public static final ParticleType<GeyserParticleOption> GEYSER_BASE = new GeyserParticleType();
   public static final ParticleType<GeyserParticleOption> GEYSER_POOF = new GeyserParticleType();
   public static final ParticleType<GeyserParticleOption> GEYSER_PLUME = new GeyserParticleType();
   public static final ParticleType<?> CLOUD = new SimpleParticleType();
   public static final ParticleType<?> COPPER_FIRE_FLAME = new SimpleParticleType();
   public static final ParticleType<?> CRIT = new SimpleParticleType();
   public static final ParticleType<?> DAMAGE_INDICATOR = new SimpleParticleType();
   public static final ParticleType<?> DRAGON_BREATH = (ParticleType<?>)(Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_7)
      ? new FloatParticleType()
      : new SimpleParticleType());
   public static final ParticleType<?> DRIPPING_LAVA = new SimpleParticleType();
   public static final ParticleType<?> FALLING_LAVA = new SimpleParticleType();
   public static final ParticleType<?> LANDING_LAVA = new SimpleParticleType();
   public static final ParticleType<?> DRIPPING_WATER = new SimpleParticleType();
   public static final ParticleType<?> FALLING_WATER = new SimpleParticleType();
   public static final ParticleType<DustParticleOption> DUST = new DustParticleType();
   public static final ParticleType<DustColorTransitionParticleOption> DUST_COLOR_TRANSITION = new DustColorTransitionParticleType();
   public static final ParticleType<?> EFFECT = (ParticleType<?>)(Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_7)
      ? new SpellParticleType()
      : new SimpleParticleType());
   public static final ParticleType<?> ELDER_GUARDIAN = new SimpleParticleType();
   public static final ParticleType<?> ENCHANTED_HIT = new SimpleParticleType();
   public static final ParticleType<?> ENCHANT = new SimpleParticleType();
   public static final ParticleType<?> END_ROD = new SimpleParticleType();
   public static final ParticleType<IntegerParticleOption> ENTITY_EFFECT = new IntegerParticleType();
   public static final ParticleType<?> EXPLOSION_EMITTER = new SimpleParticleType();
   public static final ParticleType<?> EXPLOSION = new SimpleParticleType();
   public static final ParticleType<?> GUST = new SimpleParticleType();
   public static final ParticleType<?> SMALL_GUST = new SimpleParticleType();
   public static final ParticleType<?> GUST_EMITTER_LARGE = new SimpleParticleType();
   public static final ParticleType<?> SONIC_BOOM = new SimpleParticleType();
   public static final ParticleType<IntegerParticleOption> FALLING_DUST = new VarIntParticleType();
   public static final ParticleType<?> FIREWORK = new SimpleParticleType();
   public static final ParticleType<?> FISHING = new SimpleParticleType();
   public static final ParticleType<?> FLAME = new SimpleParticleType();
   public static final ParticleType<?> INFESTED = new SimpleParticleType();
   public static final ParticleType<?> CHERRY_LEAVES = new SimpleParticleType();
   public static final ParticleType<?> PALE_OAK_LEAVES = new SimpleParticleType();
   public static final ParticleType<?> RED_POPLAR_LEAVES = new SimpleParticleType();
   public static final ParticleType<?> ORANGE_POPLAR_LEAVES = new SimpleParticleType();
   public static final ParticleType<?> YELLOW_POPLAR_LEAVES = new SimpleParticleType();
   public static final ParticleType<?> SCULK_SOUL = new SimpleParticleType();
   public static final ParticleType<FloatParticleOption> SCULK_CHARGE = new FloatParticleType();
   public static final ParticleType<?> SCULK_CHARGE_POP = new SimpleParticleType();
   public static final ParticleType<?> SOUL_FIRE_FLAME = new SimpleParticleType();
   public static final ParticleType<?> SOUL = new SimpleParticleType();
   public static final ParticleType<ColorParticleOption> FLASH = new ColorParticleType();
   public static final ParticleType<?> HAPPY_VILLAGER = new SimpleParticleType();
   public static final ParticleType<?> COMPOSTER = new SimpleParticleType();
   public static final ParticleType<?> HEART = new SimpleParticleType();
   public static final ParticleType<?> INSTANT_EFFECT = (ParticleType<?>)(Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_7)
      ? new SpellParticleType()
      : new SimpleParticleType());
   public static final ParticleType<ItemStackParticleOption> ITEM = new ItemStackParticleType();
   public static final ParticleType<VibrationParticleOption> VIBRATION = new VibrationParticleType();
   public static final ParticleType<TrailParticleOption> TRAIL = new TrailParticleType();
   public static final ParticleType<?> PAUSE_MOB_GROWTH = new SimpleParticleType();
   public static final ParticleType<?> RESET_MOB_GROWTH = new SimpleParticleType();
   public static final ParticleType<?> ITEM_SLIME = new SimpleParticleType();
   public static final ParticleType<?> ITEM_COBWEB = new SimpleParticleType();
   public static final ParticleType<?> ITEM_SNOWBALL = new SimpleParticleType();
   public static final ParticleType<?> LARGE_SMOKE = new SimpleParticleType();
   public static final ParticleType<?> LAVA = new SimpleParticleType();
   public static final ParticleType<?> MYCELIUM = new SimpleParticleType();
   public static final ParticleType<?> NOTE = new SimpleParticleType();
   public static final ParticleType<?> POOF = new SimpleParticleType();
   public static final ParticleType<?> PORTAL = new SimpleParticleType();
   public static final ParticleType<?> RAIN = new SimpleParticleType();
   public static final ParticleType<?> SMOKE = new SimpleParticleType();
   public static final ParticleType<?> WHITE_SMOKE = new SimpleParticleType();
   public static final ParticleType<?> SNEEZE = new SimpleParticleType();
   public static final ParticleType<?> SPIT = new SimpleParticleType();
   public static final ParticleType<?> SQUID_INK = new SimpleParticleType();
   public static final ParticleType<?> SWEEP_ATTACK = new SimpleParticleType();
   public static final ParticleType<?> TOTEM_OF_UNDYING = new SimpleParticleType();
   public static final ParticleType<?> UNDERWATER = new SimpleParticleType();
   public static final ParticleType<?> SPLASH = new SimpleParticleType();
   public static final ParticleType<?> WITCH = new SimpleParticleType();
   public static final ParticleType<?> BUBBLE_POP = new SimpleParticleType();
   public static final ParticleType<?> CURRENT_DOWN = new SimpleParticleType();
   public static final ParticleType<?> BUBBLE_COLUMN_UP = new SimpleParticleType();
   public static final ParticleType<?> NAUTILUS = new SimpleParticleType();
   public static final ParticleType<?> DOLPHIN = new SimpleParticleType();
   public static final ParticleType<?> CAMPFIRE_COSY_SMOKE = new SimpleParticleType();
   public static final ParticleType<?> CAMPFIRE_SIGNAL_SMOKE = new SimpleParticleType();
   public static final ParticleType<?> DRIPPING_HONEY = new SimpleParticleType();
   public static final ParticleType<?> FALLING_HONEY = new SimpleParticleType();
   public static final ParticleType<?> LANDING_HONEY = new SimpleParticleType();
   public static final ParticleType<?> FALLING_NECTAR = new SimpleParticleType();
   public static final ParticleType<?> FALLING_SPORE_BLOSSOM = new SimpleParticleType();
   public static final ParticleType<?> ASH = new SimpleParticleType();
   public static final ParticleType<?> CRIMSON_SPORE = new SimpleParticleType();
   public static final ParticleType<?> WARPED_SPORE = new SimpleParticleType();
   public static final ParticleType<?> SPORE_BLOSSOM_AIR = new SimpleParticleType();
   public static final ParticleType<?> DRIPPING_OBSIDIAN_TEAR = new SimpleParticleType();
   public static final ParticleType<?> FALLING_OBSIDIAN_TEAR = new SimpleParticleType();
   public static final ParticleType<?> LANDING_OBSIDIAN_TEAR = new SimpleParticleType();
   public static final ParticleType<?> REVERSE_PORTAL = new SimpleParticleType();
   public static final ParticleType<?> WHITE_ASH = new SimpleParticleType();
   public static final ParticleType<?> SMALL_FLAME = new SimpleParticleType();
   public static final ParticleType<?> SNOWFLAKE = new SimpleParticleType();
   public static final ParticleType<?> DRIPPING_DRIPSTONE_LAVA = new SimpleParticleType();
   public static final ParticleType<?> FALLING_DRIPSTONE_LAVA = new SimpleParticleType();
   public static final ParticleType<?> DRIPPING_DRIPSTONE_WATER = new SimpleParticleType();
   public static final ParticleType<?> FALLING_DRIPSTONE_WATER = new SimpleParticleType();
   public static final ParticleType<?> GLOW_SQUID_INK = new SimpleParticleType();
   public static final ParticleType<?> GLOW = new SimpleParticleType();
   public static final ParticleType<?> WAX_ON = new SimpleParticleType();
   public static final ParticleType<?> WAX_OFF = new SimpleParticleType();
   public static final ParticleType<?> ELECTRIC_SPARK = new SimpleParticleType();
   public static final ParticleType<?> SCRAPE = new SimpleParticleType();
   public static final ParticleType<IntegerParticleOption> SHRIEK = new VarIntParticleType();
   public static final ParticleType<?> EGG_CRACK = new SimpleParticleType();
   public static final ParticleType<?> DUST_PLUME = new SimpleParticleType();
   public static final ParticleType<?> TRIAL_SPAWNER_DETECTION = new SimpleParticleType();
   public static final ParticleType<?> TRIAL_SPAWNER_DETECTION_OMINOUS = new SimpleParticleType();
   public static final ParticleType<?> VAULT_CONNECTION = new SimpleParticleType();
   public static final ParticleType<IntegerParticleOption> DUST_PILLAR = new VarIntParticleType();
   public static final ParticleType<?> OMINOUS_SPAWNING = new SimpleParticleType();
   public static final ParticleType<?> RAID_OMEN = new SimpleParticleType();
   public static final ParticleType<?> TRIAL_OMEN = new SimpleParticleType();
   public static final ParticleType<IntegerParticleOption> BLOCK_CRUMBLE = new VarIntParticleType();
   public static final ParticleType<IntegerParticleOption> FIREFLY = new VarIntParticleType();
   public static final ParticleType<?> SULPHUR_CUBE_GOO = new SimpleParticleType();

   public static void init() {
      if (Version.getServerVersion().isOlderThan(Version.v1_20_4)) {
         register(AMBIENT_ENTITY_EFFECT);
      }

      register(ANGRY_VILLAGER);
      register(BLOCK);
      register(BLOCK_MARKER);
      register(BUBBLE);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_2)) {
         register(SULFUR_BUBBLES);
         register(NOXIOUS_GAS);
         register(NOXIOUS_GAS_CLOUD);
         register(GEYSER);
         register(GEYSER_BASE);
         register(GEYSER_POOF);
         register(GEYSER_PLUME);
      }

      register(CLOUD);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_7)) {
         register(COPPER_FIRE_FLAME);
      }

      register(CRIT);
      register(DAMAGE_INDICATOR);
      register(DRAGON_BREATH);
      register(DRIPPING_LAVA);
      register(FALLING_LAVA);
      register(LANDING_LAVA);
      register(DRIPPING_WATER);
      register(FALLING_WATER);
      register(DUST);
      register(DUST_COLOR_TRANSITION);
      register(EFFECT);
      register(ELDER_GUARDIAN);
      register(ENCHANTED_HIT);
      register(ENCHANT);
      register(END_ROD);
      register(ENTITY_EFFECT);
      register(EXPLOSION_EMITTER);
      register(EXPLOSION);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_3)) {
         register(GUST);
         if (Version.getServerVersion().isOlderThan(Version.v1_20_4)) {
            register(GUST_EMITTER);
         } else {
            register(SMALL_GUST);
            register(GUST_EMITTER_LARGE);
            register(GUST_EMITTER_SMALL);
         }
      }

      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_19)) {
         register(SONIC_BOOM);
      }

      register(FALLING_DUST);
      register(FIREWORK);
      register(FISHING);
      register(FLAME);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_4)) {
         register(INFESTED);
      }

      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_19_3) && Version.getServerVersion().isOlderThan(Version.v1_20_1)) {
         register(DRIPPING_CHERRY_LEAVES);
         register(FALLING_CHERRY_LEAVES);
         register(LANDING_CHERRY_LEAVES);
      } else if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_1)) {
         register(CHERRY_LEAVES);
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_3)) {
            register(PALE_OAK_LEAVES);
            if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_4)) {
               if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_3)) {
                  register(RED_POPLAR_LEAVES);
                  register(ORANGE_POPLAR_LEAVES);
                  register(YELLOW_POPLAR_LEAVES);
               }

               register(TINTED_LEAVES);
            }
         }
      }

      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_19)) {
         register(SCULK_SOUL);
         register(SCULK_CHARGE);
         register(SCULK_CHARGE_POP);
      }

      register(SOUL_FIRE_FLAME);
      register(SOUL);
      register(FLASH);
      register(HAPPY_VILLAGER);
      register(COMPOSTER);
      register(HEART);
      register(INSTANT_EFFECT);
      register(ITEM);
      register(VIBRATION);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_2)) {
         register(TRAIL);
      }

      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)) {
         register(PAUSE_MOB_GROWTH);
         register(RESET_MOB_GROWTH);
      }

      register(ITEM_SLIME);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_4)) {
         register(ITEM_COBWEB);
      }

      register(ITEM_SNOWBALL);
      register(LARGE_SMOKE);
      register(LAVA);
      register(MYCELIUM);
      register(NOTE);
      register(POOF);
      register(PORTAL);
      register(RAIN);
      register(SMOKE);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_3)) {
         register(WHITE_SMOKE);
      }

      register(SNEEZE);
      register(SPIT);
      register(SQUID_INK);
      register(SWEEP_ATTACK);
      register(TOTEM_OF_UNDYING);
      register(UNDERWATER);
      register(SPLASH);
      register(WITCH);
      register(BUBBLE_POP);
      register(CURRENT_DOWN);
      register(BUBBLE_COLUMN_UP);
      register(NAUTILUS);
      register(DOLPHIN);
      register(CAMPFIRE_COSY_SMOKE);
      register(CAMPFIRE_SIGNAL_SMOKE);
      register(DRIPPING_HONEY);
      register(FALLING_HONEY);
      register(LANDING_HONEY);
      register(FALLING_NECTAR);
      register(FALLING_SPORE_BLOSSOM);
      register(ASH);
      register(CRIMSON_SPORE);
      register(WARPED_SPORE);
      register(SPORE_BLOSSOM_AIR);
      register(DRIPPING_OBSIDIAN_TEAR);
      register(FALLING_OBSIDIAN_TEAR);
      register(LANDING_OBSIDIAN_TEAR);
      register(REVERSE_PORTAL);
      register(WHITE_ASH);
      register(SMALL_FLAME);
      register(SNOWFLAKE);
      register(DRIPPING_DRIPSTONE_LAVA);
      register(FALLING_DRIPSTONE_LAVA);
      register(DRIPPING_DRIPSTONE_WATER);
      register(FALLING_DRIPSTONE_WATER);
      register(GLOW_SQUID_INK);
      register(GLOW);
      register(WAX_ON);
      register(WAX_OFF);
      register(ELECTRIC_SPARK);
      register(SCRAPE);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_19)) {
         register(SHRIEK);
      }

      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_1)) {
         register(EGG_CRACK);
      }

      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_3)) {
         register(DUST_PLUME);
         if (Version.getServerVersion().isOlderThan(Version.v1_20_4)) {
            register(GUST_DUST);
         }

         register(TRIAL_SPAWNER_DETECTION);
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_4)) {
            register(TRIAL_SPAWNER_DETECTION_OMINOUS);
            register(VAULT_CONNECTION);
            register(DUST_PILLAR);
            register(OMINOUS_SPAWNING);
            register(RAID_OMEN);
            register(TRIAL_OMEN);
            if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_2)) {
               register(BLOCK_CRUMBLE);
               if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_4)) {
                  register(FIREFLY);
                  if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_2)) {
                     register(SULPHUR_CUBE_GOO);
                  }
               }
            }
         }
      }
   }

   public static <T extends ParticleOption> ParticleData<T> read(FriendlyByteBuf buf) {
      int id = buf.readVarInt();
      ParticleType<T> type = (ParticleType<T>)registry.get(id);
      if (type == null) {
         LogUtils.error(
            "Failed to find particle type with id {}! Report this on our issue tracker! Version: {} Particles: {}",
            id,
            Version.getServerVersion().getNMSVersion(),
            Maps.orderByValue(particles())
         );
         return new ParticleData<>((ParticleType<T>)HEART, null);
      } else {
         return new ParticleData<>(type, type.read(buf));
      }
   }

   public static void write(ParticleData<ParticleOption> data, FriendlyByteBuf buf) {
      buf.writeVarInt(reverseRegistry.getInt(data.type()));
      data.type().write(data.option(), buf);
   }

   public static int getId(ParticleType<?> type) {
      return reverseRegistry.getInt(type);
   }

   public static <T extends ParticleOption> ParticleType<T> register(ParticleType<T> type) {
      registry.put(registry.size(), type);
      reverseRegistry.put(type, reverseRegistry.size());
      if (FeatureFlags.DEBUG.get()) {
         LogUtils.debug("Registering particle: {}", type);
      }

      return type;
   }

   private static Map<String, String> particles() {
      Map<String, String> registered = new HashMap<>();

      for (Field field : ParticleTypes.class.getFields()) {
         try {
            Object type = field.get(null);
            int id = reverseRegistry.getOrDefault(type, -1);
            if (id == -1) {
               registered.put(field.getName(), "Unregistered");
            } else {
               registered.put(field.getName(), String.valueOf(id));
            }
         } catch (IllegalAccessException var7) {
         }
      }

      return registered;
   }
}
