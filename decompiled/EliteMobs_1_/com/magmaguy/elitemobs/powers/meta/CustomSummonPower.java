package com.magmaguy.elitemobs.powers.meta;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.CustomSpawn;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEscapeMechanism;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.powers.specialpowers.EnderCrystalLightningRod;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class CustomSummonPower extends ElitePower implements Listener {
   private static final String DEBUG_PREFIX = "[CustomSummon] ";
   private final List<CustomSummonPower.CustomBossReinforcement> customBossReinforcements = new ArrayList<>();
   private final CustomBossesConfigFields customBossesConfigFields;

   public CustomSummonPower(Object powerObject, CustomBossesConfigFields customBossesConfigFields) {
      super(PowersConfig.getPower("custom_summon.yml"));
      this.customBossesConfigFields = customBossesConfigFields;
      this.addEntry(powerObject, customBossesConfigFields.getFilename());
   }

   public static CustomBossEntity summonReinforcement(EliteEntity summoningEntity, Location spawnLocation, String reinforcementFilename, int duration) {
      CustomBossesConfigFields fields = CustomBossesConfig.getCustomBoss(reinforcementFilename);
      if (fields == null) {
         Logger.warn("Attempted to summon reinforcement " + reinforcementFilename + " which is not a valid reinforcement!");
         return null;
      } else {
         CustomBossEntity customBossEntity = new CustomBossEntity(fields);
         customBossEntity.setSummoningEntity(summoningEntity);
         if (summoningEntity instanceof CustomBossEntity summoner && summoner.isNormalizedCombat()) {
            customBossEntity.setNormalizedCombat();
         }

         summoningEntity.addReinforcement(customBossEntity);
         if (duration > 0) {
            CustomBossEscapeMechanism.startEscapeTicks(duration, customBossEntity);
         }

         customBossEntity.spawn(spawnLocation, true);
         return customBossEntity;
      }
   }

   public static BukkitTask summonGlobalReinforcement(
      final CustomSummonPower.CustomBossReinforcement customBossReinforcement, final CustomBossEntity summoningEntity
   ) {
      if (customBossReinforcement.customSpawn != null && !customBossReinforcement.customSpawn.isEmpty()) {
         return (new BukkitRunnable() {
               public void run() {
                  Location source = summoningEntity.getSpawnLocation();
                  if (source != null && source.getWorld() != null) {
                     int maximumReinforcements = 30 * source.getWorld().getPlayers().size();

                     for (int i = 0; i < customBossReinforcement.amount; i++) {
                        if (summoningEntity.getGlobalReinforcementsCount() >= maximumReinforcements) {
                           return;
                        }

                        CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(customBossReinforcement.bossFileName);
                        if (customBossEntity == null) {
                           Logger.warn(
                              "Failed to spawn reinforcement because boss "
                                 + customBossReinforcement.bossFileName
                                 + " was invalid! Does the file exist? Is it configured correctly?"
                           );
                           return;
                        }

                        if (summoningEntity.isNormalizedCombat()) {
                           customBossEntity.setNormalizedCombat();
                        }

                        CustomSpawn customSpawn = new CustomSpawn(customBossReinforcement.customSpawn, customBossEntity);
                        if (customSpawn.getCustomSpawnConfigFields() == null) {
                           customBossEntity.remove(RemovalReason.REINFORCEMENT_CULL);
                        } else {
                           customSpawn.setWorld(source.getWorld());
                           summoningEntity.addGlobalReinforcement(customBossEntity);
                           customBossEntity.setSummoningEntity(summoningEntity);
                           customSpawn.queueSpawn();
                           customBossReinforcement.isSummoned = true;
                        }
                     }
                  }
               }
            })
            .runTaskTimer(MetadataHandler.PLUGIN, 0L, 200L);
      } else {
         Logger.warn(
            "Reinforcement for boss "
               + summoningEntity.getCustomBossesConfigFields().getFilename()
               + " has an incorrectly configured global reinforcement for "
               + customBossReinforcement.bossFileName
         );
         return null;
      }
   }

   public List<CustomSummonPower.CustomBossReinforcement> getCustomBossReinforcements() {
      return this.customBossReinforcements;
   }

   public void addEntry(Object powerEntry, String filename) {
      int previousSize = this.customBossReinforcements.size();

      try {
         if (powerEntry instanceof String legacy) {
            this.processNewFormat(normalizeLegacy(legacy), filename);
         } else {
            if (!(powerEntry instanceof Map<?, ?> map)) {
               throw new IllegalArgumentException("Expected a summon declaration");
            }

            this.processNewFormat((Map<String, ?>)map, filename);
         }
      } catch (RuntimeException var6) {
         this.customBossReinforcements.subList(previousSize, this.customBossReinforcements.size()).clear();
         Logger.warn("Invalid reinforcement in " + filename + ": " + var6.getMessage());
      }
   }

   private void processNewFormat(Map<String, ?> map, String configFilename) {
      CustomSummonPower.SummonType summonType = null;
      String filename = null;
      Vector location = null;
      Double chance = 1.0;
      boolean lightningRod = false;
      boolean inheritAggro = false;
      boolean inheritLevel = false;
      String customSpawn = "";
      int amount = 1;
      boolean spawnNearby = false;

      for (Entry<String, ?> entry : map.entrySet()) {
         String var15 = entry.getKey().toLowerCase(Locale.ROOT);
         switch (var15) {
            case "summonable":
            case "difficultyid":
               break;
            case "summontype":
               summonType = MapListInterpreter.parseEnum(
                  entry.getKey(), entry.getValue(), CustomSummonPower.SummonType.class, this.customBossesConfigFields.getFilename()
               );
               break;
            case "filename":
               filename = MapListInterpreter.parseString(entry.getKey(), entry.getValue(), this.customBossesConfigFields.getFilename());
               break;
            case "chance":
               chance = MapListInterpreter.parseDouble(entry.getKey(), entry.getValue(), this.customBossesConfigFields.getFilename());
               break;
            case "location":
               String locationString = MapListInterpreter.parseString(entry.getKey(), entry.getValue(), this.customBossesConfigFields.getFilename());

               try {
                  location = new Vector(
                     Double.parseDouble(locationString.split(",")[0]),
                     Double.parseDouble(locationString.split(",")[1]),
                     Double.parseDouble(locationString.split(",")[2])
                  );
                  location.checkFinite();
                  break;
               } catch (RuntimeException var19) {
                  throw new IllegalArgumentException("Invalid reinforcement location: " + locationString, var19);
               }
            case "lightningrod":
               lightningRod = MapListInterpreter.parseBoolean(entry.getKey(), entry.getValue(), this.customBossesConfigFields.getFilename());
               break;
            case "inheritaggro":
               inheritAggro = MapListInterpreter.parseBoolean(entry.getKey(), entry.getValue(), this.customBossesConfigFields.getFilename());
               break;
            case "amount":
               amount = MapListInterpreter.parseInteger(entry.getKey(), entry.getValue(), this.customBossesConfigFields.getFilename());
               break;
            case "inheritlevel":
               inheritLevel = MapListInterpreter.parseBoolean(entry.getKey(), entry.getValue(), this.customBossesConfigFields.getFilename());
               break;
            case "spawnnearby":
               spawnNearby = MapListInterpreter.parseBoolean(entry.getKey(), entry.getValue(), this.customBossesConfigFields.getFilename());
               break;
            case "customspawn":
               if (CustomSpawnConfig.getCustomEvent(
                     MapListInterpreter.parseString(entry.getKey(), entry.getValue(), this.customBossesConfigFields.getFilename())
                  )
                  == null) {
                  Logger.warn("Failed to determine Custom Spawn file for filename " + entry.getValue());
               } else {
                  customSpawn = MapListInterpreter.parseString(entry.getKey(), entry.getValue(), this.customBossesConfigFields.getFilename());
               }
               break;
            default:
               Logger.warn("Invalid boss reinforcement!");
               Logger.warn("Problematic entry: " + entry.getValue());
         }
      }

      if (summonType == null) {
         Logger.warn("No summon type detected in " + this.customBossesConfigFields.getFilename() + " ! This reinforcement will not work.");
      } else if (chance != null && Double.isFinite(chance)) {
         if (summonType == CustomSummonPower.SummonType.ON_COMBAT_ENTER_PLACE_CRYSTAL || filename != null && !filename.isBlank()) {
            if (summonType == CustomSummonPower.SummonType.ON_COMBAT_ENTER_PLACE_CRYSTAL && location == null) {
               throw new IllegalArgumentException("Missing crystal location");
            } else {
               CustomSummonPower.CustomBossReinforcement customBossReinforcement;
               switch (summonType) {
                  case ONCE:
                     customBossReinforcement = this.doOnce(filename);
                     break;
                  case ON_HIT:
                     customBossReinforcement = this.doOnHit(filename, chance);
                     break;
                  case ON_COMBAT_ENTER:
                     customBossReinforcement = this.doOnCombatEnter(filename);
                     break;
                  case ON_DEATH:
                     customBossReinforcement = this.doOnDeath(filename);
                     break;
                  case ON_COMBAT_ENTER_PLACE_CRYSTAL:
                     customBossReinforcement = this.doOnCombatEnterPlaceCrystal(location, lightningRod);
                     break;
                  case GLOBAL:
                     customBossReinforcement = this.doGlobalSummonReinforcement(filename);
                     break;
                  default:
                     customBossReinforcement = null;
                     Logger.warn(
                        "Failed to determine summon type for reinforcement in "
                           + this.customBossesConfigFields.getFilename()
                           + " ! Contact the developer with this error!"
                     );
               }

               if (summonType == CustomSummonPower.SummonType.ON_COMBAT_ENTER_PLACE_CRYSTAL
                  || customBossReinforcement != null
                     && customBossReinforcement.bossFileName != null
                     && CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) != null) {
                  if (customBossReinforcement != null) {
                     customBossReinforcement.inheritAggro = inheritAggro;
                     customBossReinforcement.amount = amount;
                     customBossReinforcement.inheritLevel = inheritLevel;
                     customBossReinforcement.spawnNearby = spawnNearby;
                     customBossReinforcement.customSpawn = customSpawn;
                     customBossReinforcement.summonChance = chance;
                     customBossReinforcement.setSpawnLocationOffset(location);
                  }
               } else {
                  Logger.warn("Could not get filename for reinforcement in file " + configFilename);
               }
            }
         } else {
            throw new IllegalArgumentException("Missing reinforcement filename");
         }
      } else {
         throw new IllegalArgumentException("Invalid summon chance");
      }
   }

   private static Map<String, Object> normalizeLegacy(String declaration) {
      String[] parts = declaration.split(":", -1);
      Map<String, Object> fields = new LinkedHashMap<>();
      if (parts[0].equalsIgnoreCase("summon")) {
         if (parts.length < 3) {
            throw new IllegalArgumentException("Incomplete summon declaration");
         }

         String var6 = parts[1].toLowerCase(Locale.ROOT);
         switch (var6) {
            case "once":
               requireParts(parts, 3);
               fields.put("summonType", "ONCE");
               fields.put("filename", parts[2]);
               break;
            case "onhit":
               requireParts(parts, 4);
               fields.put("summonType", "ON_HIT");
               fields.put("chance", parts[2]);
               fields.put("filename", parts[3]);
               break;
            case "oncombatenter":
               requireParts(parts, 4);
               fields.put("summonType", "ON_COMBAT_ENTER");
               fields.put("location", parts[2]);
               fields.put("filename", parts[3]);
               break;
            case "oncombatenterplacecrystal":
               requireParts(parts, 4);
               fields.put("summonType", "ON_COMBAT_ENTER_PLACE_CRYSTAL");
               fields.put("location", parts[2]);
               fields.put("lightningRod", parts[3]);
               break;
            default:
               throw new IllegalArgumentException("Unknown summon type: " + parts[1]);
         }
      } else {
         if (!parts[0].equalsIgnoreCase("summonable")) {
            throw new IllegalArgumentException("Unknown summon declaration: " + parts[0]);
         }

         for (int i = 1; i < parts.length; i++) {
            String[] field = parts[i].split("=", 2);
            if (field.length != 2 || field[1].isBlank()) {
               throw new IllegalArgumentException("Incomplete summon field: " + parts[i]);
            }

            String key = field[0].toLowerCase(Locale.ROOT);
            if (fields.putIfAbsent(key, field[1]) != null) {
               throw new IllegalArgumentException("Duplicate summon field: " + key);
            }
         }
      }

      return fields;
   }

   private static void requireParts(String[] parts, int expected) {
      if (parts.length != expected || Arrays.stream(parts).anyMatch(String::isBlank)) {
         throw new IllegalArgumentException("Incomplete or extra summon fields");
      }
   }

   private CustomSummonPower.CustomBossReinforcement doOnce(String filename) {
      CustomSummonPower.CustomBossReinforcement customBossReinforcement = new CustomSummonPower.CustomBossReinforcement(
         CustomSummonPower.SummonType.ONCE, filename
      );
      if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
         Logger.warn("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid! Filename: " + filename);
         return null;
      } else {
         this.customBossReinforcements.add(customBossReinforcement);
         return customBossReinforcement;
      }
   }

   private CustomSummonPower.CustomBossReinforcement doOnHit(String filename, double chance) {
      CustomSummonPower.CustomBossReinforcement customBossReinforcement = new CustomSummonPower.CustomBossReinforcement(
         CustomSummonPower.SummonType.ON_HIT, filename
      );
      customBossReinforcement.setSummonChance(chance);
      if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
         Logger.warn("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid! Filename: " + filename);
         return customBossReinforcement;
      } else {
         this.customBossReinforcements.add(customBossReinforcement);
         return customBossReinforcement;
      }
   }

   private CustomSummonPower.CustomBossReinforcement doOnDeath(String filename) {
      CustomSummonPower.CustomBossReinforcement customBossReinforcement = new CustomSummonPower.CustomBossReinforcement(
         CustomSummonPower.SummonType.ON_DEATH, filename
      );
      if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
         Logger.warn("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid! Filename: " + filename);
         return null;
      } else {
         this.customBossReinforcements.add(customBossReinforcement);
         return customBossReinforcement;
      }
   }

   private CustomSummonPower.CustomBossReinforcement doOnCombatEnter(String filename) {
      CustomSummonPower.CustomBossReinforcement customBossReinforcement = new CustomSummonPower.CustomBossReinforcement(
         CustomSummonPower.SummonType.ON_COMBAT_ENTER, filename
      );
      if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName) == null) {
         Logger.warn("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid! Filename: " + filename);
         return customBossReinforcement;
      } else {
         this.customBossReinforcements.add(customBossReinforcement);
         return customBossReinforcement;
      }
   }

   private CustomSummonPower.CustomBossReinforcement doOnCombatEnterPlaceCrystal(Vector location, boolean lightningRod) {
      CustomSummonPower.CustomBossReinforcement customBossReinforcement = new CustomSummonPower.CustomBossReinforcement(
         CustomSummonPower.SummonType.ON_COMBAT_ENTER, EntityType.END_CRYSTAL, lightningRod
      );
      customBossReinforcement.setSpawnLocationOffset(location);
      this.customBossReinforcements.add(customBossReinforcement);
      return customBossReinforcement;
   }

   private CustomSummonPower.CustomBossReinforcement doGlobalSummonReinforcement(String filename) {
      CustomSummonPower.CustomBossReinforcement customBossReinforcement = new CustomSummonPower.CustomBossReinforcement(
         CustomSummonPower.SummonType.GLOBAL, filename
      );
      CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName);
      if (customBossesConfigFields == null) {
         Logger.warn("Reinforcement mob " + customBossReinforcement.bossFileName + " is not valid! Filename: " + filename);
         return null;
      } else {
         customBossReinforcement.entityType = customBossesConfigFields.getEntityType();
         this.customBossReinforcements.add(customBossReinforcement);
         return customBossReinforcement;
      }
   }

   private void onHitSummonReinforcement(EliteEntity spawningEntity) {
      if (DebugMessage.isAnyDebugEnabled()) {
         DebugMessage.log(
            "[CustomSummon] onHitSummonReinforcement reached for "
               + this.customBossesConfigFields.getFilename()
               + " with "
               + this.customBossReinforcements.size()
               + " configured reinforcement entries"
         );
      }

      for (CustomSummonPower.CustomBossReinforcement customBossReinforcement : this.customBossReinforcements) {
         if (DebugMessage.isAnyDebugEnabled()) {
            if (!customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.ON_HIT)
               && !customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.ONCE)) {
               DebugMessage.log(
                  "[CustomSummon] skipped entry "
                     + customBossReinforcement.bossFileName
                     + " : summon type is "
                     + customBossReinforcement.summonType
                     + " , not ON_HIT / ONCE"
               );
            } else if (customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.ONCE) && customBossReinforcement.isSummoned) {
               DebugMessage.log("[CustomSummon] skipped entry " + customBossReinforcement.bossFileName + " : summon type ONCE and it has already been summoned");
            }
         }

         if (customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.ONCE) && !customBossReinforcement.isSummoned) {
            this.summonReinforcement(spawningEntity, customBossReinforcement);
         }

         if (customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.ON_HIT)) {
            this.summonReinforcement(spawningEntity, customBossReinforcement);
         }
      }
   }

   private void onCombatEnterSummonReinforcement(EliteEntity spawningEntity) {
      for (CustomSummonPower.CustomBossReinforcement customBossReinforcement : this.customBossReinforcements) {
         if (customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.ON_COMBAT_ENTER)) {
            if (customBossReinforcement.bossFileName != null) {
               this.summonReinforcement(spawningEntity, customBossReinforcement);
            } else {
               Location spawnLocation = this.getFinalSpawnLocation(spawningEntity, customBossReinforcement.spawnLocationOffset);
               Entity entity = spawnLocation.getWorld().spawnEntity(spawnLocation, customBossReinforcement.entityType);
               entity.getPersistentDataContainer().set(new NamespacedKey(MetadataHandler.PLUGIN, "eliteCrystal"), PersistentDataType.STRING, "eliteCrystal");
               entity.setPersistent(false);
               CrashFix.persistentTracker(entity);
               if (entity instanceof Mob && !spawningEntity.getDamagers().isEmpty()) {
                  Player target = null;
                  double damageDealt = 0.0;

                  for (Player player : spawningEntity.getDamagers().keySet()) {
                     if (!(spawningEntity.getDamagers().get(player) < damageDealt)) {
                        target = player;
                        damageDealt = spawningEntity.getDamagers().get(player);
                     }
                  }

                  if (target != null) {
                     ((Mob)entity).setTarget(target);
                  }
               }

               if (customBossReinforcement.isLightningRod) {
                  new EnderCrystalLightningRod(spawningEntity, (EnderCrystal)entity);
               }

               customBossReinforcement.isSummoned = true;
               spawningEntity.addReinforcement(entity);
            }
         }
      }
   }

   private void onDeathSummonReinforcement(EliteEntity spawningEntity) {
      for (CustomSummonPower.CustomBossReinforcement customBossReinforcement : this.customBossReinforcements) {
         if (customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.ON_DEATH)) {
            this.summonReinforcement(spawningEntity, customBossReinforcement);
         }
      }
   }

   private void summonReinforcement(EliteEntity eliteEntity, CustomSummonPower.CustomBossReinforcement customBossReinforcement) {
      if (DebugMessage.isAnyDebugEnabled()) {
         DebugMessage.log(
            "[CustomSummon] summonReinforcement reached for "
               + this.customBossesConfigFields.getFilename()
               + " -> "
               + customBossReinforcement.bossFileName
               + " (type="
               + customBossReinforcement.summonType
               + ", amount="
               + customBossReinforcement.amount
               + ", chance="
               + customBossReinforcement.summonChance
               + ", summonerWorld="
               + describeWorld(eliteEntity.getLocation())
               + ")"
         );
      }

      if (customBossReinforcement.summonChance != null && ThreadLocalRandom.current().nextDouble() > customBossReinforcement.summonChance) {
         if (DebugMessage.isAnyDebugEnabled()) {
            DebugMessage.log("[CustomSummon] aborted: summonChance roll failed for " + customBossReinforcement.bossFileName);
         }
      } else {
         for (int i = 0; i < customBossReinforcement.amount; i++) {
            Location spawnLocation = eliteEntity.getLocation();
            if (customBossReinforcement.spawnLocationOffset != null) {
               spawnLocation = this.getFinalSpawnLocation(eliteEntity, customBossReinforcement.spawnLocationOffset);
            }

            if (customBossReinforcement.spawnNearby) {
               for (int loc = 0; loc < 30; loc++) {
                  Location randomLocation = spawnLocation.clone()
                     .add(new Vector(ThreadLocalRandom.current().nextInt(-15, 15), 0, ThreadLocalRandom.current().nextInt(-15, 15)));
                  int height = CustomSpawn.getHighestValidBlock(randomLocation, randomLocation.getWorld().getMaxHeight() - 2);
                  if (height != Integer.MIN_VALUE) {
                     randomLocation.setY(height);
                     spawnLocation = randomLocation;
                     break;
                  }
               }
            }

            if (DebugMessage.isAnyDebugEnabled()) {
               DebugMessage.log("[CustomSummon] resolved spawn location for " + customBossReinforcement.bossFileName + " : " + describeLocation(spawnLocation));
            }

            if (CustomBossesConfig.getCustomBoss(customBossReinforcement.bossFileName).isRegionalBoss()) {
               RegionalBossEntity regionalBossEntity = RegionalBossEntity.createTemporaryRegionalBossEntity(customBossReinforcement.bossFileName, spawnLocation);
               if (regionalBossEntity == null) {
                  Logger.warn(
                     "Failed to spawn reinforcement for "
                        + eliteEntity.getName()
                        + " because boss "
                        + customBossReinforcement.bossFileName
                        + " was invalid! Does the file exist? Is it configured correctly?"
                  );
                  return;
               }

               if (eliteEntity instanceof CustomBossEntity summoner && summoner.isNormalizedCombat()) {
                  regionalBossEntity.setNormalizedCombat();
               }

               if (customBossReinforcement.inheritLevel) {
                  regionalBossEntity.setLevel(eliteEntity.getLevel());
               }

               if (!customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.ON_DEATH)) {
                  eliteEntity.addReinforcement(regionalBossEntity);
               }

               customBossReinforcement.isSummoned = true;
               regionalBossEntity.setSummoningEntity(eliteEntity);
               if (customBossReinforcement.inheritAggro) {
                  regionalBossEntity.inheritAggroFrom(eliteEntity);
               }

               regionalBossEntity.initialize();
               if (DebugMessage.isAnyDebugEnabled()) {
                  DebugMessage.log(
                     "[CustomSummon] queued regional reinforcement "
                        + customBossReinforcement.bossFileName
                        + " (regional bosses spawn through a delayed task, so a living entity is not expected yet)"
                  );
               }
            } else {
               CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(customBossReinforcement.bossFileName);
               if (customBossEntity == null) {
                  Logger.warn(
                     "Failed to spawn reinforcement for "
                        + eliteEntity.getName()
                        + " because boss "
                        + customBossReinforcement.bossFileName
                        + " was invalid! Does the file exist? Is it configured correctly?"
                  );
                  return;
               }

               if (eliteEntity instanceof CustomBossEntity summoner && summoner.isNormalizedCombat()) {
                  customBossEntity.setNormalizedCombat();
               }

               customBossEntity.setSpawnLocation(spawnLocation);
               customBossEntity.setBypassesProtections(eliteEntity.getBypassesProtections());
               if (customBossReinforcement.inheritLevel) {
                  customBossEntity.setLevel(eliteEntity.getLevel());
               }

               customBossEntity.setSummoningEntity(eliteEntity);

               try {
                  customBossEntity.spawn(false);
               } finally {
                  if (customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.ON_DEATH)) {
                     eliteEntity.removeReinforcement(customBossEntity);
                  }
               }

               if (DebugMessage.isAnyDebugEnabled()) {
                  DebugMessage.log(
                     "[CustomSummon] spawn() returned for "
                        + customBossReinforcement.bossFileName
                        + " , livingEntity="
                        + (customBossEntity.getLivingEntity() == null ? "null (spawn failed)" : "present")
                  );
               }

               if (customBossEntity.getLivingEntity() != null) {
                  customBossEntity.getLivingEntity()
                     .setVelocity(new Vector(ThreadLocalRandom.current().nextDouble(0.2), 0.2, ThreadLocalRandom.current().nextDouble(0.2)));
               }

               if (!customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.ON_DEATH)
                  && (customBossEntity.getLivingEntity() == null || !customBossEntity.getLivingEntity().isValid())) {
                  eliteEntity.addReinforcement(customBossEntity);
               }

               customBossReinforcement.isSummoned = true;
               if (customBossReinforcement.inheritAggro) {
                  customBossEntity.inheritAggroFrom(eliteEntity);
               }
            }
         }
      }
   }

   private static String describeWorld(Location location) {
      if (location == null) {
         return "unknown world (null location)";
      } else {
         return location.getWorld() == null ? "unknown world (null world)" : location.getWorld().getName();
      }
   }

   private static String describeLocation(Location location) {
      return location == null ? "null" : describeWorld(location) + " " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
   }

   private Location getFinalSpawnLocation(EliteEntity summoningEntity, Vector spawnLocationOffset) {
      Location finalSpawnLocation;
      if (summoningEntity instanceof RegionalBossEntity) {
         finalSpawnLocation = summoningEntity.getSpawnLocation().add(spawnLocationOffset);
      } else if (summoningEntity == null) {
         finalSpawnLocation = null;
      } else {
         finalSpawnLocation = summoningEntity.getLocation().add(spawnLocationOffset);
      }

      return finalSpawnLocation;
   }

   public class CustomBossReinforcement {
      public final CustomSummonPower.SummonType summonType;
      public Double summonChance;
      public String bossFileName = null;
      public Vector spawnLocationOffset;
      public EntityType entityType;
      public boolean isLightningRod;
      public boolean inheritAggro = false;
      public int amount = 1;
      public boolean inheritLevel = false;
      public boolean spawnNearby = false;
      public String customSpawn;
      private boolean isSummoned = false;

      public CustomBossReinforcement(CustomSummonPower.SummonType summonType, String bossFileName) {
         this.summonType = summonType;
         this.bossFileName = bossFileName;
      }

      public CustomBossReinforcement(CustomSummonPower.SummonType summonType, EntityType entityType, boolean isLightningRod) {
         this.summonType = summonType;
         this.entityType = entityType;
         this.isLightningRod = isLightningRod;
      }

      public void setSummonChance(double summonChance) {
         this.summonChance = summonChance;
      }

      public void setSpawnLocationOffset(Vector vector) {
         this.spawnLocationOffset = vector;
      }
   }

   public static class CustomSummonPowerEvent implements Listener {
      @EventHandler(
         ignoreCancelled = true
      )
      public void onHit(EliteMobDamagedByPlayerEvent event) {
         CustomSummonPower customSummonPower = (CustomSummonPower)event.getEliteMobEntity().getPower("custom_summon.yml");
         if (customSummonPower != null) {
            if (!ElitePower.eventIsValid(event, customSummonPower, true)) {
               logOnHitGateRejection(event);
            } else if (event.getDamage() < 3.0) {
               if (DebugMessage.isDebugEnabled(event.getPlayer())) {
                  logOnHitGate(event, "rejected: damage " + event.getDamage() + " is below the hard-coded 3 damage floor");
               }
            } else {
               logOnHitGate(event, "all gates passed, dispatching ON_HIT reinforcements");
               customSummonPower.onHitSummonReinforcement(event.getEliteMobEntity());
            }
         }
      }

      private static void logOnHitGateRejection(EliteMobDamagedByPlayerEvent event) {
         if (DebugMessage.isDebugEnabled(event.getPlayer())) {
            EliteEntity eliteEntity = event.getEliteMobEntity();
            if (event.isCancelled()) {
               logOnHitGate(event, "rejected: the damage event was cancelled");
            } else {
               LivingEntity livingEntity = eliteEntity.getLivingEntity();
               if (livingEntity == null) {
                  logOnHitGate(event, "rejected: the elite has no living entity");
               } else if (!eliteEntity.isAIActive()) {
                  logOnHitGate(event, "rejected: the living entity has AI disabled");
               } else {
                  logOnHitGate(event, "rejected: eventIsValid returned false but no individual gate reproduced it");
               }
            }
         }
      }

      private static void logOnHitGate(EliteMobDamagedByPlayerEvent event, String outcome) {
         if (DebugMessage.isDebugEnabled(event.getPlayer())) {
            EliteEntity eliteEntity = event.getEliteMobEntity();
            DebugMessage.log(
               event.getPlayer(),
               "[CustomSummon] ON_HIT gate for "
                  + (
                     eliteEntity instanceof CustomBossEntity customBossEntity
                        ? customBossEntity.getCustomBossesConfigFields().getFilename()
                        : eliteEntity.getName()
                  )
                  + " in "
                  + CustomSummonPower.describeWorld(eliteEntity.getLocation())
                  + " : "
                  + outcome
            );
         }
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onHitCancellationDiagnostic(EliteMobDamagedByPlayerEvent event) {
         if (DebugMessage.isAnyDebugEnabled()) {
            if (event.isCancelled()) {
               if (event.getEliteMobEntity().getPower("custom_summon.yml") != null) {
                  logOnHitGate(event, "rejected before the summon listener ran: the damage event was cancelled");
               }
            }
         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onCombatEnter(EliteMobEnterCombatEvent event) {
         CustomSummonPower customSummonPower = (CustomSummonPower)event.getEliteMobEntity().getPower("custom_summon.yml");
         if (customSummonPower != null) {
            customSummonPower.onCombatEnterSummonReinforcement(event.getEliteMobEntity());
         }
      }

      @EventHandler
      public void onDeath(EliteMobDeathEvent event) {
         CustomSummonPower customSummonPower = (CustomSummonPower)event.getEliteEntity().getPower("custom_summon.yml");
         if (customSummonPower != null) {
            customSummonPower.onDeathSummonReinforcement(event.getEliteEntity());
         }
      }
   }

   public static enum SummonType {
      ONCE,
      ON_HIT,
      ON_COMBAT_ENTER,
      ON_DEATH,
      ON_COMBAT_ENTER_PLACE_CRYSTAL,
      GLOBAL;
   }
}
