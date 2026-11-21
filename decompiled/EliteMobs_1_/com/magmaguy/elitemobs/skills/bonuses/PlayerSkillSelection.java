package com.magmaguy.elitemobs.skills.bonuses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusesConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class PlayerSkillSelection {
   public static final int MAX_ACTIVE_SKILLS = 3;
   private static final Gson GSON = new GsonBuilder().create();
   private static final Type SELECTION_TYPE = (new TypeToken<Map<String, List<String>>>() {}).getType();
   private static final Map<UUID, Map<SkillType, List<String>>> activeSkills = new ConcurrentHashMap<>();
   private static final Set<UUID> failedLoads = ConcurrentHashMap.newKeySet();

   private PlayerSkillSelection() {
   }

   public static List<String> getActiveSkills(UUID uuid, SkillType skillType) {
      ensureLoaded(uuid);
      Map<SkillType, List<String>> playerSkills = activeSkills.get(uuid);
      if (playerSkills == null) {
         return Collections.emptyList();
      } else {
         List<String> skillIds = playerSkills.get(skillType);
         return skillIds != null && !skillIds.isEmpty() ? skillIds : Collections.emptyList();
      }
   }

   public static boolean addActiveSkill(UUID uuid, SkillType skillType, String skillId) {
      return addActiveSkill(uuid, skillType, skillId, false);
   }

   public static boolean addActiveSkill(UUID uuid, SkillType skillType, String skillId, boolean bypassLimit) {
      ensureLoaded(uuid);
      if (skillId != null && !skillId.isBlank()) {
         String normalizedSkillId = skillId.toLowerCase(Locale.ROOT);
         if (!isSkillEnabledForType(skillType, normalizedSkillId)) {
            return false;
         } else {
            Map<SkillType, List<String>> playerSkills = activeSkills.get(uuid);
            if (playerSkills == null) {
               return false;
            } else {
               List<String> typeSkills = new ArrayList<>(playerSkills.getOrDefault(skillType, List.of()));
               if (!bypassLimit && typeSkills.size() >= 3) {
                  return false;
               } else if (typeSkills.contains(normalizedSkillId)) {
                  return false;
               } else {
                  typeSkills.add(normalizedSkillId);
                  playerSkills.put(skillType, List.copyOf(typeSkills));
                  saveToDatabase(uuid);
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   public static boolean removeActiveSkill(UUID uuid, SkillType skillType, String skillId) {
      ensureLoaded(uuid);
      if (skillId != null && !skillId.isBlank()) {
         String normalizedSkillId = skillId.toLowerCase(Locale.ROOT);
         Map<SkillType, List<String>> playerSkills = activeSkills.get(uuid);
         if (playerSkills == null) {
            return false;
         } else {
            List<String> typeSkills = playerSkills.get(skillType);
            if (typeSkills == null) {
               return false;
            } else {
               List<String> updated = new ArrayList<>(typeSkills);
               boolean removed = updated.remove(normalizedSkillId);
               if (removed) {
                  playerSkills.put(skillType, List.copyOf(updated));
                  saveToDatabase(uuid);
               }

               return removed;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean isSkillActive(UUID uuid, String skillId) {
      if (skillId != null && !skillId.isBlank()) {
         String normalizedSkillId = skillId.toLowerCase(Locale.ROOT);
         ensureLoaded(uuid);
         Map<SkillType, List<String>> playerSkills = activeSkills.get(uuid);
         return playerSkills == null ? false : playerSkills.values().stream().anyMatch(skills -> skills.contains(normalizedSkillId));
      } else {
         return false;
      }
   }

   public static int getActiveSkillCount(UUID uuid, SkillType skillType) {
      return getActiveSkills(uuid, skillType).size();
   }

   public static boolean canAddSkill(UUID uuid, SkillType skillType) {
      return getActiveSkillCount(uuid, skillType) < 3;
   }

   public static List<String> getAllActiveSkills(UUID uuid) {
      ensureLoaded(uuid);
      Map<SkillType, List<String>> playerSkills = activeSkills.get(uuid);
      if (playerSkills == null) {
         return List.of();
      } else {
         List<String> allSkills = new ArrayList<>();
         playerSkills.values().forEach(allSkills::addAll);
         return allSkills;
      }
   }

   public static void clearAllSkills(UUID uuid) {
      if (PlayerData.isDataLoaded(uuid)) {
         failedLoads.remove(uuid);
         activeSkills.put(uuid, new EnumMap<>(SkillType.class));
         saveToDatabase(uuid);
      }
   }

   private static void ensureLoaded(UUID uuid) {
      if (!activeSkills.containsKey(uuid) && !failedLoads.contains(uuid) && PlayerData.isDataLoaded(uuid)) {
         loadFromDatabase(uuid);
      }
   }

   public static void loadFromDatabase(UUID uuid) {
      if (PlayerData.isDataLoaded(uuid)) {
         failedLoads.remove(uuid);
         String json = PlayerData.getSkillBonusSelections(uuid);
         if (json != null && !json.isEmpty() && !json.equals("{}")) {
            try {
               JsonElement selection = JsonParser.parseString(json);
               if (!selection.isJsonObject()) {
                  throw new IllegalArgumentException("Skill selections must be a JSON object");
               }

               Map<String, List<String>> parsed = (Map<String, List<String>>)GSON.fromJson(selection, SELECTION_TYPE);
               Map<SkillType, List<String>> converted = new EnumMap<>(SkillType.class);
               if (parsed != null) {
                  for (Entry<String, List<String>> entry : parsed.entrySet()) {
                     try {
                        SkillType skillType = SkillType.valueOf(entry.getKey());
                        List<String> normalizedIds = new ArrayList<>();
                        if (entry.getValue() != null) {
                           for (String rawId : entry.getValue()) {
                              if (rawId != null) {
                                 normalizedIds.add(rawId.toLowerCase(Locale.ROOT));
                              }
                           }
                        }

                        converted.put(skillType, normalizedIds);
                     } catch (IllegalArgumentException var11) {
                        throw new IllegalArgumentException("Unknown skill type in database: " + entry.getKey(), var11);
                     }
                  }
               }

               if (parsed == null) {
                  throw new IllegalArgumentException("Skill selections must be a JSON object");
               }

               boolean sanitized = sanitizeAllSkillLists(converted);
               converted.replaceAll((type, skills) -> List.copyOf(skills));
               activeSkills.put(uuid, converted);
               if (sanitized) {
                  saveToDatabase(uuid);
               }
            } catch (Exception var12) {
               Logger.warn("Failed to parse skill selections for player " + uuid + ": " + var12.getMessage());
               activeSkills.remove(uuid);
               failedLoads.add(uuid);
            }
         } else {
            activeSkills.put(uuid, new EnumMap<>(SkillType.class));
         }
      }
   }

   public static void saveToDatabase(UUID uuid) {
      Map<SkillType, List<String>> playerSkills = activeSkills.get(uuid);
      if (playerSkills != null && !failedLoads.contains(uuid) && PlayerData.isDataLoaded(uuid)) {
         if (playerSkills.isEmpty()) {
            PlayerData.setSkillBonusSelections(uuid, "{}");
         } else {
            Map<String, List<String>> toSerialize = new HashMap<>();

            for (Entry<SkillType, List<String>> entry : playerSkills.entrySet()) {
               if (!entry.getValue().isEmpty()) {
                  toSerialize.put(entry.getKey().name(), entry.getValue());
               }
            }

            String json = GSON.toJson(toSerialize);
            PlayerData.setSkillBonusSelections(uuid, json);
         }
      }
   }

   public static void onPlayerJoin(Player player) {
      loadFromDatabase(player.getUniqueId());
   }

   public static void onPlayerLeave(Player player) {
      saveToDatabase(player.getUniqueId());
      activeSkills.remove(player.getUniqueId());
      failedLoads.remove(player.getUniqueId());
   }

   public static void clearCache(UUID uuid) {
      activeSkills.remove(uuid);
      failedLoads.remove(uuid);
   }

   public static void shutdown() {
      for (UUID uuid : new ArrayList<>(activeSkills.keySet())) {
         saveToDatabase(uuid);
      }

      activeSkills.clear();
      failedLoads.clear();
   }

   private static boolean isSkillEnabledForType(SkillType skillType, String skillId) {
      SkillBonusConfigFields config = SkillBonusesConfig.getBySkillId(skillId);
      SkillBonus implementation = SkillBonusRegistry.getSkillById(skillId);
      return config != null && config.isEnabled() && config.getSkillType() == skillType && implementation != null && implementation.isEnabled();
   }

   private static boolean sanitizeSkillList(Map<SkillType, List<String>> playerSkills, SkillType skillType) {
      List<String> rawList = playerSkills.get(skillType);
      if (rawList != null && !rawList.isEmpty()) {
         List<String> sanitized = new ArrayList<>();
         Set<String> seen = new HashSet<>();

         for (String skillId : rawList) {
            if (skillId != null) {
               String normalizedId = skillId.toLowerCase(Locale.ROOT);
               if (seen.add(normalizedId) && isSkillEnabledForType(skillType, normalizedId)) {
                  sanitized.add(normalizedId);
                  if (sanitized.size() >= 3) {
                     break;
                  }
               }
            }
         }

         if (sanitized.equals(rawList)) {
            return false;
         } else {
            if (sanitized.isEmpty()) {
               playerSkills.remove(skillType);
            } else {
               playerSkills.put(skillType, sanitized);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean sanitizeAllSkillLists(Map<SkillType, List<String>> playerSkills) {
      boolean changed = false;

      for (SkillType skillType : SkillType.values()) {
         changed |= sanitizeSkillList(playerSkills, skillType);
      }

      return changed;
   }
}
