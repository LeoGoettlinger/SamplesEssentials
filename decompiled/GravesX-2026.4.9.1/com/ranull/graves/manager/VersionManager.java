package com.ranull.graves.manager;

import dev.cwhead.GravesX.compatibility.CompatibilityParticleEnum;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

public class VersionManager {
   private final String version = this.getVersion();
   private final boolean hasConfigContains = !this.is_v1_7() && !this.is_v1_8() && !this.is_v1_9();
   private final boolean hasAPIVersion = !this.is_v1_7() && !this.is_v1_8() && !this.is_v1_9() && !this.is_v1_10() && !this.is_v1_11() && !this.is_v1_12();
   private final boolean hasBlockData = !this.is_v1_7() && !this.is_v1_8() && !this.is_v1_9() && !this.is_v1_10() && !this.is_v1_11() && !this.is_v1_12();
   private final boolean hasPersistentData = !this.is_v1_7()
      && !this.is_v1_8()
      && !this.is_v1_9()
      && !this.is_v1_10()
      && !this.is_v1_11()
      && !this.is_v1_12()
      && !this.is_v1_13();
   private final boolean hasScoreboardTags = !this.is_v1_7() && !this.is_v1_8() && !this.is_v1_9() && !this.is_v1_10();
   private final boolean hasHexColors = !this.is_v1_7()
      && !this.is_v1_8()
      && !this.is_v1_9()
      && !this.is_v1_10()
      && !this.is_v1_11()
      && !this.is_v1_12()
      && !this.is_v1_13()
      && !this.is_v1_14()
      && !this.is_v1_15()
      && !this.isBukkit();
   private final boolean hasCompassMeta = !this.is_v1_7()
      && !this.is_v1_8()
      && !this.is_v1_9()
      && !this.is_v1_10()
      && !this.is_v1_11()
      && !this.is_v1_12()
      && !this.is_v1_13()
      && !this.is_v1_14()
      && !this.is_v1_15()
      && !this.version.matches("(?i)v1_16_R1|");
   private final boolean hasSwingHand = !this.is_v1_7()
      && !this.is_v1_8()
      && !this.is_v1_9()
      && !this.is_v1_10()
      && !this.is_v1_11()
      && !this.is_v1_12()
      && !this.is_v1_13()
      && !this.is_v1_14()
      && !this.is_v1_15();
   private final boolean hasWorldHeight = !this.is_v1_7()
      && !this.is_v1_8()
      && !this.is_v1_9()
      && !this.is_v1_10()
      && !this.is_v1_11()
      && !this.is_v1_12()
      && !this.is_v1_13()
      && !this.is_v1_14()
      && !this.is_v1_15()
      && !this.is_v1_16();
   private final boolean hasSecondHand = !this.is_v1_7() && !this.is_v1_8();
   private final boolean hasEnchantmentCurse = !this.is_v1_7() && !this.is_v1_8() && !this.is_v1_9() && !this.is_v1_10();
   private final boolean hasParticle = !this.is_v1_7() && !this.is_v1_8();
   private boolean isBukkit;
   private boolean isMohist;
   private boolean isPaper;
   private boolean isFolia;
   private final boolean isPost1_20_5 = !this.is_v1_7()
      && !this.is_v1_8()
      && !this.is_v1_9()
      && !this.is_v1_10()
      && !this.is_v1_11()
      && !this.is_v1_12()
      && !this.is_v1_13()
      && !this.is_v1_14()
      && !this.is_v1_15()
      && !this.is_v1_16()
      && !this.is_v1_17()
      && !this.is_v1_18()
      && !this.is_v1_19()
      && !this.is_v1_20();
   private final boolean isPost1_21_9 = !this.is_v1_7()
      && !this.is_v1_8()
      && !this.is_v1_9()
      && !this.is_v1_10()
      && !this.is_v1_11()
      && !this.is_v1_12()
      && !this.is_v1_13()
      && !this.is_v1_14()
      && !this.is_v1_15()
      && !this.is_v1_16()
      && !this.is_v1_17()
      && !this.is_v1_18()
      && !this.is_v1_19()
      && !this.is_v1_20()
      && !this.is_v1_21();
   private boolean isLegacyVersion;
   private final boolean hasTextDisplays = !this.is_v1_7()
      && !this.is_v1_8()
      && !this.is_v1_9()
      && !this.is_v1_10()
      && !this.is_v1_11()
      && !this.is_v1_12()
      && !this.is_v1_13()
      && !this.is_v1_14()
      && !this.is_v1_15()
      && !this.is_v1_16()
      && !this.is_v1_17()
      && !this.is_v1_18();

   public VersionManager() {
      try {
         Class.forName("org.spigotmc.SpigotConfig", false, this.getClass().getClassLoader());
         this.isBukkit = false;
      } catch (ClassNotFoundException var5) {
         this.isBukkit = true;
      }

      try {
         Class.forName("io.papermc.paper.threadedregions.RegionizedServer", false, this.getClass().getClassLoader());
         this.isFolia = true;
      } catch (ClassNotFoundException var4) {
         this.isFolia = false;
      }

      try {
         Class.forName("com.mohistmc.config.MohistConfigUtil", false, this.getClass().getClassLoader());
         this.isMohist = true;
      } catch (ClassNotFoundException var3) {
         this.isMohist = false;
      }

      try {
         Class.forName("io.papermc.paper.configuration.ServerConfiguration", false, this.getClass().getClassLoader());
         this.isPaper = true;
      } catch (ClassNotFoundException var2) {
         this.isPaper = false;
      }
   }

   public String getVersion() {
      try {
         this.isLegacyVersion = true;
         return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
      } catch (Exception var2) {
         this.isLegacyVersion = false;
         return Bukkit.getServer().getVersion();
      }
   }

   public boolean isLegacyVersion() {
      try {
         return this.isLegacyVersion;
      } catch (NullPointerException var2) {
         return false;
      }
   }

   public boolean isBukkit() {
      return this.isBukkit;
   }

   public boolean isPaper() {
      return this.isPaper;
   }

   public boolean isMohist() {
      return this.isMohist;
   }

   public boolean isFolia() {
      return this.isFolia;
   }

   public boolean hasConfigContains() {
      return this.hasConfigContains;
   }

   public boolean hasAPIVersion() {
      return this.hasAPIVersion;
   }

   public boolean hasBlockData() {
      return this.hasBlockData;
   }

   public boolean hasPersistentData() {
      return this.hasPersistentData;
   }

   public boolean hasScoreboardTags() {
      return this.hasScoreboardTags;
   }

   public boolean hasHexColors() {
      return this.hasHexColors;
   }

   public boolean hasCompassMeta() {
      return this.hasCompassMeta;
   }

   public boolean hasSwingHand() {
      return this.hasSwingHand;
   }

   public boolean hasMinHeight() {
      return this.hasWorldHeight;
   }

   public boolean hasSecondHand() {
      return this.hasSecondHand;
   }

   public boolean hasEnchantmentCurse() {
      return this.hasEnchantmentCurse;
   }

   public boolean hasParticle() {
      return this.hasParticle;
   }

   public boolean is_v1_7() {
      return this.version.matches("(?i)v1_7_R1|v1_7_R2|v1_7_R3|v1_7_R4");
   }

   public boolean is_v1_8() {
      return this.version.matches("(?i)v1_8_R1|v1_8_R2|v1_8_R3");
   }

   public boolean is_v1_9() {
      return this.version.matches("(?i)v1_9_R1|v1_9_R2");
   }

   public boolean is_v1_10() {
      return this.version.matches("(?i)v1_10_R1");
   }

   public boolean is_v1_11() {
      return this.version.matches("(?i)v1_11_R1");
   }

   public boolean is_v1_12() {
      return this.version.matches("(?i)v1_12_R1");
   }

   public boolean is_v1_13() {
      return this.version.matches("(?i)v1_13_R1|v1_13_R2");
   }

   public boolean is_v1_14() {
      return this.version.matches("(?i)v1_14_R1");
   }

   public boolean is_v1_15() {
      return this.version.matches("(?i)v1_15_R1");
   }

   public boolean is_v1_16() {
      return this.version.matches("(?i)v1_16_R1|v1_16_R2|v1_16_R3");
   }

   public boolean is_v1_17() {
      return this.version.matches("(?i)v1_17_R1");
   }

   public boolean is_v1_18() {
      return this.version.matches("(?i)v1_18_R1|v1_18_R2");
   }

   public boolean is_v1_19() {
      return this.version.matches("(?i)v1_19_R1|v1_19_R2|v1_19_R3");
   }

   public boolean is_v1_20() {
      return this.version.matches("(?i)v1_20_R1|v1_20_R2|v1_20_R3");
   }

   public boolean is_v1_20_5() {
      return this.version.matches("(?i)v1_20_R4");
   }

   public boolean is_v1_21() {
      return this.version.matches("(?i)v1_21_R1|(?i)v1_21_R2|(?i)v1_21_R3|(?i)v1_21_R4");
   }

   public boolean isPost1_20_5() {
      return this.isPost1_20_5;
   }

   public boolean is_v1_21_R5() {
      return this.version.matches("(?i)v1_21_R5");
   }

   public boolean isPost1_21_9() {
      return this.isPost1_21_9;
   }

   public boolean isHasTextDisplays() {
      return this.hasTextDisplays;
   }

   public Particle getParticleForVersion(String particle) {
      return switch (particle) {
         case "REDSTONE", "REDDUST" -> {
            Particle var174;
            try {
               var174 = CompatibilityParticleEnum.valueOf("REDSTONE");
            } catch (Exception var61) {
               var174 = CompatibilityParticleEnum.valueOf("DUST");
               yield var174;
            }

            yield var174;
         }
         case "DUST" -> {
            Particle var172;
            try {
               var172 = CompatibilityParticleEnum.valueOf("DUST");
            } catch (Exception var60) {
               var172 = CompatibilityParticleEnum.valueOf("REDSTONE");
               yield var172;
            }

            yield var172;
         }
         case "ENCHANTMENT_TABLE", "ENCHANTMENTTABLE" -> {
            Particle var170;
            try {
               var170 = CompatibilityParticleEnum.valueOf("ENCHANTMENT_TABLE");
            } catch (Exception var59) {
               var170 = CompatibilityParticleEnum.valueOf("ENCHANT");
               yield var170;
            }

            yield var170;
         }
         case "ENCHANT" -> {
            Particle var168;
            try {
               var168 = CompatibilityParticleEnum.valueOf("ENCHANT");
            } catch (Exception var58) {
               var168 = CompatibilityParticleEnum.valueOf("ENCHANTMENT_TABLE");
               yield var168;
            }

            yield var168;
         }
         case "EXPLOSION_NORMAL", "EXPLODE" -> {
            Particle var166;
            try {
               var166 = CompatibilityParticleEnum.valueOf("EXPLOSION_NORMAL");
            } catch (Exception var57) {
               var166 = CompatibilityParticleEnum.valueOf("POOF");
               yield var166;
            }

            yield var166;
         }
         case "POOF" -> {
            Particle var164;
            try {
               var164 = CompatibilityParticleEnum.valueOf("POOF");
            } catch (Exception var56) {
               var164 = CompatibilityParticleEnum.valueOf("EXPLOSION_NORMAL");
               yield var164;
            }

            yield var164;
         }
         case "EXPLOSION_LARGE", "LARGE_EXPLOSION" -> {
            Particle var162;
            try {
               var162 = CompatibilityParticleEnum.valueOf("EXPLOSION_LARGE");
            } catch (Exception var55) {
               var162 = CompatibilityParticleEnum.valueOf("EXPLOSION");
               yield var162;
            }

            yield var162;
         }
         case "EXPLOSION" -> {
            Particle var160;
            try {
               var160 = CompatibilityParticleEnum.valueOf("EXPLOSION");
            } catch (Exception var54) {
               var160 = CompatibilityParticleEnum.valueOf("EXPLOSION_LARGE");
               yield var160;
            }

            yield var160;
         }
         case "EXPLOSION_HUGE", "HUGE_EXPLOSION" -> {
            Particle var158;
            try {
               var158 = CompatibilityParticleEnum.valueOf("EXPLOSION_HUGE");
            } catch (Exception var53) {
               var158 = CompatibilityParticleEnum.valueOf("EXPLOSION_EMITTER");
               yield var158;
            }

            yield var158;
         }
         case "EXPLOSION_EMITTER" -> {
            Particle var156;
            try {
               var156 = CompatibilityParticleEnum.valueOf("EXPLOSION_EMITTER");
            } catch (Exception var52) {
               var156 = CompatibilityParticleEnum.valueOf("EXPLOSION_HUGE");
               yield var156;
            }

            yield var156;
         }
         case "SMOKE_NORMAL" -> {
            Particle var154;
            try {
               var154 = CompatibilityParticleEnum.valueOf("SMOKE_NORMAL");
            } catch (Exception var51) {
               var154 = CompatibilityParticleEnum.valueOf("SMOKE");
               yield var154;
            }

            yield var154;
         }
         case "SMOKE" -> {
            Particle var152;
            try {
               var152 = CompatibilityParticleEnum.valueOf("SMOKE");
            } catch (Exception var50) {
               var152 = CompatibilityParticleEnum.valueOf("SMOKE_NORMAL");
               yield var152;
            }

            yield var152;
         }
         case "SMOKE_LARGE", "LARGESMOKE" -> {
            Particle var150;
            try {
               var150 = CompatibilityParticleEnum.valueOf("SMOKE_LARGE");
            } catch (Exception var49) {
               var150 = CompatibilityParticleEnum.valueOf("LARGE_SMOKE");
               yield var150;
            }

            yield var150;
         }
         case "LARGE_SMOKE" -> {
            Particle var148;
            try {
               var148 = CompatibilityParticleEnum.valueOf("LARGE_SMOKE");
            } catch (Exception var48) {
               var148 = CompatibilityParticleEnum.valueOf("SMOKE_LARGE");
               yield var148;
            }

            yield var148;
         }
         case "WATER_BUBBLE" -> {
            Particle var146;
            try {
               var146 = CompatibilityParticleEnum.valueOf("WATER_BUBBLE");
            } catch (Exception var47) {
               var146 = CompatibilityParticleEnum.valueOf("BUBBLE");
               yield var146;
            }

            yield var146;
         }
         case "BUBBLE" -> {
            Particle var144;
            try {
               var144 = CompatibilityParticleEnum.valueOf("BUBBLE");
            } catch (Exception var46) {
               var144 = CompatibilityParticleEnum.valueOf("WATER_BUBBLE");
               yield var144;
            }

            yield var144;
         }
         case "WATER_SPLASH", "SPLASH" -> {
            Particle var142;
            try {
               var142 = CompatibilityParticleEnum.valueOf("WATER_SPLASH");
            } catch (Exception var45) {
               var142 = CompatibilityParticleEnum.valueOf("SPLASH");
               yield var142;
            }

            yield var142;
         }
         case "WATER_WAKE" -> {
            Particle var140;
            try {
               var140 = CompatibilityParticleEnum.valueOf("WATER_WAKE");
            } catch (Exception var44) {
               var140 = CompatibilityParticleEnum.valueOf("FISHING");
               yield var140;
            }

            yield var140;
         }
         case "FISHING" -> {
            Particle var138;
            try {
               var138 = CompatibilityParticleEnum.valueOf("FISHING");
            } catch (Exception var43) {
               var138 = CompatibilityParticleEnum.valueOf("WATER_WAKE");
               yield var138;
            }

            yield var138;
         }
         case "WATER_DROP", "DROPLET" -> {
            Particle var136;
            try {
               var136 = CompatibilityParticleEnum.valueOf("WATER_DROP");
            } catch (Exception var42) {
               var136 = CompatibilityParticleEnum.valueOf("RAIN");
               yield var136;
            }

            yield var136;
         }
         case "RAIN" -> {
            Particle var134;
            try {
               var134 = CompatibilityParticleEnum.valueOf("RAIN");
            } catch (Exception var41) {
               var134 = CompatibilityParticleEnum.valueOf("WATER_DROP");
               yield var134;
            }

            yield var134;
         }
         case "DRIP_WATER", "DRIPWATER" -> {
            Particle var132;
            try {
               var132 = CompatibilityParticleEnum.valueOf("DRIP_WATER");
            } catch (Exception var40) {
               var132 = CompatibilityParticleEnum.valueOf("DRIPPING_WATER");
               yield var132;
            }

            yield var132;
         }
         case "DRIPPING_WATER" -> {
            Particle var130;
            try {
               var130 = CompatibilityParticleEnum.valueOf("DRIPPING_WATER");
            } catch (Exception var39) {
               var130 = CompatibilityParticleEnum.valueOf("DRIP_WATER");
               yield var130;
            }

            yield var130;
         }
         case "DRIP_LAVA", "DRIPLAVA" -> {
            Particle var128;
            try {
               var128 = CompatibilityParticleEnum.valueOf("DRIP_LAVA");
            } catch (Exception var38) {
               var128 = CompatibilityParticleEnum.valueOf("DRIPPING_LAVA");
               yield var128;
            }

            yield var128;
         }
         case "DRIPPING_LAVA" -> {
            Particle var126;
            try {
               var126 = CompatibilityParticleEnum.valueOf("DRIPPING_LAVA");
            } catch (Exception var37) {
               var126 = CompatibilityParticleEnum.valueOf("DRIP_LAVA");
               yield var126;
            }

            yield var126;
         }
         case "SUSPENDED" -> {
            Particle var124;
            try {
               var124 = CompatibilityParticleEnum.valueOf("SUSPENDED");
            } catch (Exception var36) {
               var124 = CompatibilityParticleEnum.valueOf("UNDERWATER");
               yield var124;
            }

            yield var124;
         }
         case "UNDERWATER" -> {
            Particle var122;
            try {
               var122 = CompatibilityParticleEnum.valueOf("UNDERWATER");
            } catch (Exception var35) {
               var122 = CompatibilityParticleEnum.valueOf("SUSPENDED");
               yield var122;
            }

            yield var122;
         }
         case "SUSPENDED_DEPTH", "DEPTHSUSPEND" -> {
            Particle var120;
            try {
               var120 = CompatibilityParticleEnum.valueOf("SUSPENDED_DEPTH");
            } catch (Exception var34) {
               var120 = CompatibilityParticleEnum.valueOf("UNDERWATER");
               yield var120;
            }

            yield var120;
         }
         case "TOWN_AURA", "TOWNAURA" -> {
            Particle var118;
            try {
               var118 = CompatibilityParticleEnum.valueOf("TOWN_AURA");
            } catch (Exception var33) {
               var118 = CompatibilityParticleEnum.valueOf("MYCELIUM");
               yield var118;
            }

            yield var118;
         }
         case "MYCELIUM" -> {
            Particle var116;
            try {
               var116 = CompatibilityParticleEnum.valueOf("MYCELIUM");
            } catch (Exception var32) {
               var116 = CompatibilityParticleEnum.valueOf("TOWN_AURA");
               yield var116;
            }

            yield var116;
         }
         case "VILLAGER_ANGRY", "ANGRYVILLAGER" -> {
            Particle var114;
            try {
               var114 = CompatibilityParticleEnum.valueOf("VILLAGER_ANGRY");
            } catch (Exception var31) {
               var114 = CompatibilityParticleEnum.valueOf("ANGRY_VILLAGER");
               yield var114;
            }

            yield var114;
         }
         case "ANGRY_VILLAGER" -> {
            Particle var112;
            try {
               var112 = CompatibilityParticleEnum.valueOf("ANGRY_VILLAGER");
            } catch (Exception var30) {
               var112 = CompatibilityParticleEnum.valueOf("VILLAGER_ANGRY");
               yield var112;
            }

            yield var112;
         }
         case "VILLAGER_HAPPY", "HAPPYVILLAGER" -> {
            Particle var110;
            try {
               var110 = CompatibilityParticleEnum.valueOf("VILLAGER_HAPPY");
            } catch (Exception var29) {
               var110 = CompatibilityParticleEnum.valueOf("HAPPY_VILLAGER");
               yield var110;
            }

            yield var110;
         }
         case "HAPPY_VILLAGER" -> {
            Particle var108;
            try {
               var108 = CompatibilityParticleEnum.valueOf("HAPPY_VILLAGER");
            } catch (Exception var28) {
               var108 = CompatibilityParticleEnum.valueOf("VILLAGER_HAPPY");
               yield var108;
            }

            yield var108;
         }
         case "SPELL" -> {
            Particle var106;
            try {
               var106 = CompatibilityParticleEnum.valueOf("SPELL");
            } catch (Exception var27) {
               var106 = CompatibilityParticleEnum.valueOf("EFFECT");
               yield var106;
            }

            yield var106;
         }
         case "EFFECT" -> {
            Particle var104;
            try {
               var104 = CompatibilityParticleEnum.valueOf("EFFECT");
            } catch (Exception var26) {
               var104 = CompatibilityParticleEnum.valueOf("SPELL");
               yield var104;
            }

            yield var104;
         }
         case "SPELL_INSTANT" -> {
            Particle var102;
            try {
               var102 = CompatibilityParticleEnum.valueOf("SPELL_INSTANT");
            } catch (Exception var25) {
               var102 = CompatibilityParticleEnum.valueOf("INSTANT_EFFECT");
               yield var102;
            }

            yield var102;
         }
         case "INSTANT_EFFECT" -> {
            Particle var100;
            try {
               var100 = CompatibilityParticleEnum.valueOf("INSTANT_EFFECT");
            } catch (Exception var24) {
               var100 = CompatibilityParticleEnum.valueOf("SPELL_INSTANT");
               yield var100;
            }

            yield var100;
         }
         case "SPELL_MOB", "SPELL_MOB_AMBIENT" -> {
            Particle var98;
            try {
               var98 = CompatibilityParticleEnum.valueOf("SPELL_MOB");
            } catch (Exception var23) {
               var98 = CompatibilityParticleEnum.valueOf("ENTITY_EFFECT");
               yield var98;
            }

            yield var98;
         }
         case "ENTITY_EFFECT" -> {
            Particle var96;
            try {
               var96 = CompatibilityParticleEnum.valueOf("ENTITY_EFFECT");
            } catch (Exception var22) {
               var96 = CompatibilityParticleEnum.valueOf("SPELL_MOB");
               yield var96;
            }

            yield var96;
         }
         case "SPELL_WITCH", "WITCHMAGIC" -> {
            Particle var94;
            try {
               var94 = CompatibilityParticleEnum.valueOf("SPELL_WITCH");
            } catch (Exception var21) {
               var94 = CompatibilityParticleEnum.valueOf("WITCH");
               yield var94;
            }

            yield var94;
         }
         case "WITCH" -> {
            Particle var92;
            try {
               var92 = CompatibilityParticleEnum.valueOf("WITCH");
            } catch (Exception var20) {
               var92 = CompatibilityParticleEnum.valueOf("SPELL_WITCH");
               yield var92;
            }

            yield var92;
         }
         case "CRIT_MAGIC", "MAGICCRIT" -> {
            Particle var90;
            try {
               var90 = CompatibilityParticleEnum.valueOf("CRIT_MAGIC");
            } catch (Exception var19) {
               var90 = CompatibilityParticleEnum.valueOf("ENCHANTED_HIT");
               yield var90;
            }

            yield var90;
         }
         case "ENCHANTED_HIT" -> {
            Particle var88;
            try {
               var88 = CompatibilityParticleEnum.valueOf("ENCHANTED_HIT");
            } catch (Exception var18) {
               var88 = CompatibilityParticleEnum.valueOf("CRIT_MAGIC");
               yield var88;
            }

            yield var88;
         }
         case "ICONCRACK", "ITEM_CRACK" -> {
            Particle var86;
            try {
               var86 = CompatibilityParticleEnum.valueOf("ITEM_CRACK");
            } catch (Exception var17) {
               var86 = CompatibilityParticleEnum.valueOf("ITEM");
               yield var86;
            }

            yield var86;
         }
         case "ITEM" -> {
            Particle var84;
            try {
               var84 = CompatibilityParticleEnum.valueOf("ITEM");
            } catch (Exception var16) {
               var84 = CompatibilityParticleEnum.valueOf("ITEM_CRACK");
               yield var84;
            }

            yield var84;
         }
         case "BLOCK_CRACK", "BLOCK_DUST" -> {
            Particle var82;
            try {
               var82 = CompatibilityParticleEnum.valueOf("BLOCK_CRACK");
            } catch (Exception var15) {
               var82 = CompatibilityParticleEnum.valueOf("BLOCK");
               yield var82;
            }

            yield var82;
         }
         case "BLOCK" -> {
            Particle var80;
            try {
               var80 = CompatibilityParticleEnum.valueOf("BLOCK");
            } catch (Exception var14) {
               var80 = CompatibilityParticleEnum.valueOf("BLOCK_CRACK");
               yield var80;
            }

            yield var80;
         }
         case "SNOWBALL" -> {
            Particle var78;
            try {
               var78 = CompatibilityParticleEnum.valueOf("SNOWBALL");
            } catch (Exception var13) {
               var78 = CompatibilityParticleEnum.valueOf("ITEM_SNOWBALL");
               yield var78;
            }

            yield var78;
         }
         case "ITEM_SNOWBALL" -> {
            Particle var76;
            try {
               var76 = CompatibilityParticleEnum.valueOf("ITEM_SNOWBALL");
            } catch (Exception var12) {
               var76 = CompatibilityParticleEnum.valueOf("SNOWBALL");
               yield var76;
            }

            yield var76;
         }
         case "SLIME" -> {
            Particle var74;
            try {
               var74 = CompatibilityParticleEnum.valueOf("SLIME");
            } catch (Exception var11) {
               var74 = CompatibilityParticleEnum.valueOf("ITEM_SLIME");
               yield var74;
            }

            yield var74;
         }
         case "ITEM_SLIME" -> {
            Particle var72;
            try {
               var72 = CompatibilityParticleEnum.valueOf("ITEM_SLIME");
            } catch (Exception var10) {
               var72 = CompatibilityParticleEnum.valueOf("SLIME");
               yield var72;
            }

            yield var72;
         }
         case "FIREWORKS_SPARK" -> {
            Particle var70;
            try {
               var70 = CompatibilityParticleEnum.valueOf("FIREWORKS_SPARK");
            } catch (Exception var9) {
               var70 = CompatibilityParticleEnum.valueOf("FIREWORK");
               yield var70;
            }

            yield var70;
         }
         case "FIREWORK" -> {
            Particle var68;
            try {
               var68 = CompatibilityParticleEnum.valueOf("FIREWORK");
            } catch (Exception var8) {
               var68 = CompatibilityParticleEnum.valueOf("FIREWORKS_SPARK");
               yield var68;
            }

            yield var68;
         }
         case "PORTAL", "NOTE", "FLAME", "HEART", "DRAGON_BREATH", "CLOUD", "CRIT", "LAVA", "ASH", "ELECTRIC_SPARK" -> {
            Particle var66 = CompatibilityParticleEnum.valueOf(particle);
            yield var66;
         }
         case "BARRIER", "LIGHT" -> {
            Particle var65;
            try {
               var65 = CompatibilityParticleEnum.valueOf(particle);
            } catch (Exception var7) {
               var65 = CompatibilityParticleEnum.valueOf("BLOCK_MARKER");
               yield var65;
            }

            yield var65;
         }
         case "BLOCK_MARKER" -> {
            Particle var63;
            try {
               var63 = CompatibilityParticleEnum.valueOf("BLOCK_MARKER");
            } catch (Exception var6) {
               var63 = CompatibilityParticleEnum.valueOf("BARRIER");
               yield var63;
            }

            yield var63;
         }
         default -> {
            Particle var4 = CompatibilityParticleEnum.valueOf(particle);
            yield var4;
         }
      };
   }

   public Enchantment getEnchantmentForVersion(String enchantment) {
      Enchantment toReturn = switch (enchantment) {
         case "DURABILITY" -> {
            Enchantment var9;
            try {
               Enchantment e = Enchantment.getByName("DURABILITY");
               if (e == null) {
                  e = Enchantment.getByName("UNBREAKING");
               }

               var9 = e;
            } catch (IllegalArgumentException | NullPointerException var7) {
               var9 = Enchantment.getByName("UNBREAKING");
               yield var9;
            }

            yield var9;
         }
         default -> {
            Object var5 = null;
            yield var5;
         }
      };
      if (toReturn == null) {
         throw new IllegalArgumentException("Enchantment can't be null. This is a bug.");
      } else {
         return toReturn;
      }
   }

   public Material getMaterialForVersion(String material) {
      Material toReturn = switch (material) {
         case "RECOVERY_COMPASS" -> {
            Material var9;
            try {
               var9 = Material.valueOf("RECOVERY_COMPASS");
            } catch (IllegalArgumentException | NullPointerException var7) {
               var9 = Material.valueOf("COMPASS");
               yield var9;
            }

            yield var9;
         }
         default -> {
            Object var5 = null;
            yield var5;
         }
      };
      if (toReturn == null) {
         throw new IllegalArgumentException("Material can't be null. This is a bug.");
      } else {
         return toReturn;
      }
   }

   public PotionEffectType getPotionEffectTypeFromVersion(String potionEffect) {
      PotionEffectType toReturn = switch (potionEffect) {
         case "RESISTANCE" -> {
            PotionEffectType var10;
            try {
               var10 = PotionEffectType.getByName("RESISTANCE");
            } catch (IllegalArgumentException | NullPointerException var7) {
               var10 = PotionEffectType.getByName("DAMAGE_RESISTANCE");
               yield var10;
            }

            yield var10;
         }
         case "FIRE_RESISTANCE" -> {
            PotionEffectType var8 = PotionEffectType.getByName("FIRE_RESISTANCE");
            yield var8;
         }
         default -> {
            Object var5 = null;
            yield var5;
         }
      };
      if (toReturn == null) {
         throw new IllegalArgumentException("Potion Effect Type can't be null. This is a bug.");
      } else {
         return toReturn;
      }
   }
}
