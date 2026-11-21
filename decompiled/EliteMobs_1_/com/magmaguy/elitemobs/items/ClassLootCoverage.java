package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.combatsystem.ScaledCombatRewardResolver;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.instanced.dungeons.DifficultyResolver;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.elitemobs.items.itemconstructor.ClassLootItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.parties.Party;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ClassLootCoverage {
   private ClassLootCoverage() {
   }

   public static boolean enabled(CustomBossEntity boss) {
      return ClassLootSettingsConfig.enabled()
         && boss.getCustomBossesConfigFields().isClassLoot()
         && !boss.getCustomBossesConfigFields().isReinforcement()
         && !boss.isReinforcementOrMount();
   }

   public static void dropLoot(CustomBossEntity boss) {
      if (enabled(boss) && !boss.isTriggeredAntiExploit() && boss.getLevel() >= 1) {
         boolean dungeonPool = boss instanceof InstancedBossEntity instanced && instanced.getDungeonInstance() != null;
         ClassLootSettingsConfig.Difficulty difficulty = difficulty(boss);
         ClassLootSettingsConfig.Rank rank = rank(boss);
         double chance = ClassLootSettingsConfig.dropChance(rank);
         List<Player> contributors = boss.getDamagers()
            .keySet()
            .stream()
            .filter(
               playerx -> !playerx.hasMetadata("NPC")
                  && PlayerData.isInMemory(playerx.getUniqueId())
                  && !(boss instanceof InstancedBossEntity instancedx && instancedx.getLockoutPlayers().contains(playerx))
                  && boss.getDamagers().get(playerx) / boss.getMaxHealth() >= 0.1
            )
            .toList();

         for (Player player : contributors) {
            if (!(ThreadLocalRandom.current().nextDouble() >= chance)) {
               int level = Math.max(1, (int)LootTables.setItemTier(ScaledCombatRewardResolver.getRewardLevel(boss, player)));
               boolean partyPool = !dungeonPool && PartyManager.shouldUsePartyLoot(player, boss);
               List<Player> audience = List.of(player);
               if (dungeonPool) {
                  audience = contributors;
               } else if (partyPool) {
                  Party party = PartyManager.getParty(player.getUniqueId());
                  List<Player> nearby = PartyManager.getNearbyMembers(party, boss.getLocation());
                  audience = contributors.stream().filter(nearby::contains).toList();
               }

               Player preferenceOwner = audience.isEmpty() ? player : audience.get(ThreadLocalRandom.current().nextInt(audience.size()));
               List<EliteCustomLootEntry> eligible = candidates(boss.getCustomBossesConfigFields(), difficulty, rank, preferenceOwner, boss);
               EliteCustomLootEntry selected = ClassLootSelection.selectEntry(eligible, preferenceOwner);
               if (selected != null && selected.willDrop(preferenceOwner, boss)) {
                  for (int copy = 0; copy < selected.getAmount(); copy++) {
                     ItemStack item = ClassLootItemConstructor.construct(
                        CustomItem.getCustomItem(selected.getFilename()), level, difficulty, rank, boss, !dungeonPool && !partyPool ? player : null
                     );
                     if (item != null) {
                        if (dungeonPool) {
                           SharedLootTable table = SharedLootTable.getSharedLootTables().get(boss);
                           if (table == null) {
                              table = new SharedLootTable(boss);
                           }

                           table.addLoot(item, selected.getPermission(), audience);
                        } else if ((!partyPool || !SharedLootTable.addPartyLoot(boss, player, item, selected.getPermission(), audience))
                           && (selected.getPermission().isEmpty() || player.hasPermission(selected.getPermission()))) {
                           LootTables.deliverGeneratedItem(player, boss, item);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static ClassLootSettingsConfig.Difficulty difficulty(CustomBossEntity boss) {
      String override = boss.getCustomBossesConfigFields().getClassLootDifficulty();
      if (!"AUTO".equals(override)) {
         return ClassLootSettingsConfig.difficulty(override, ClassLootSettingsConfig.defaultDifficulty());
      } else {
         return boss instanceof InstancedBossEntity instanced
            ? ClassLootSettingsConfig.forDifficultyId(instanced.getDifficultyID(), instanced.getResolvedDifficultyID())
            : ClassLootSettingsConfig.defaultDifficulty();
      }
   }

   public static ClassLootSettingsConfig.Rank rank(CustomBossEntity boss) {
      return rank(boss.getCustomBossesConfigFields());
   }

   public static ClassLootSettingsConfig.Rank rank(CustomBossesConfigFields fields) {
      try {
         return ClassLootSettingsConfig.Rank.valueOf(fields.getClassLootRank());
      } catch (IllegalArgumentException var2) {
         return switch (fields.getBossType()) {
            case BOSS, EVENT -> ClassLootSettingsConfig.Rank.BOSS;
            case MINIBOSS -> ClassLootSettingsConfig.Rank.MINIBOSS;
            default -> fields.getName().contains("$minibossLevel")
               ? ClassLootSettingsConfig.Rank.MINIBOSS
               : (
                  !fields.getName().contains("$bossLevel") && !fields.getName().contains("$eventBossLevel")
                     ? ClassLootSettingsConfig.Rank.TRASH
                     : ClassLootSettingsConfig.Rank.BOSS
               );
         };
      }
   }

   public static ItemStack generate(CustomBossEntity boss, int level, Player owner) {
      ClassLootSettingsConfig.Difficulty difficulty = difficulty(boss);
      ClassLootSettingsConfig.Rank rank = rank(boss);
      ClassLootFamily family = ClassLootSelection.select(
         candidates(boss.getCustomBossesConfigFields(), difficulty, rank, owner, boss)
            .stream()
            .map(entry -> CustomItem.getCustomItem(entry.getFilename()).getCustomItemsConfigFields().getClassLootFamily())
            .distinct()
            .toList(),
         owner
      );
      return family == null ? null : generate(boss.getCustomBossesConfigFields(), boss.getLevel(), level, difficulty, rank, family, boss, owner);
   }

   public static ItemStack preview(
      CustomBossesConfigFields fields,
      int level,
      ClassLootSettingsConfig.Difficulty difficulty,
      ClassLootSettingsConfig.Rank rank,
      ClassLootFamily family,
      Player owner
   ) {
      return !availableFamilies(fields, difficulty, rank, owner).contains(family)
         ? null
         : generate(fields, level, level, difficulty, rank, family, null, owner);
   }

   private static ItemStack generate(
      CustomBossesConfigFields fields,
      int mobLevel,
      int level,
      ClassLootSettingsConfig.Difficulty difficulty,
      ClassLootSettingsConfig.Rank rank,
      ClassLootFamily family,
      CustomBossEntity boss,
      Player owner
   ) {
      List<EliteCustomLootEntry> choices = candidates(fields, difficulty, rank, owner, boss)
         .stream()
         .filter(entry -> CustomItem.getCustomItem(entry.getFilename()).getCustomItemsConfigFields().getClassLootFamily() == family)
         .toList();
      if (choices.isEmpty()) {
         return null;
      } else {
         CustomItem item = CustomItem.getCustomItem(choices.get(ThreadLocalRandom.current().nextInt(choices.size())).getFilename());
         return ClassLootItemConstructor.construct(item, level, difficulty, rank, boss, owner, fields.getName());
      }
   }

   public static List<EliteCustomLootEntry> candidates(
      CustomBossesConfigFields fields, ClassLootSettingsConfig.Difficulty difficulty, ClassLootSettingsConfig.Rank rank, Player player, CustomBossEntity boss
   ) {
      if (fields.getCustomLootTable() == null) {
         return List.of();
      } else {
         DifficultyResolver resolver = new DifficultyResolver(fields.getFilename(), List.of());
         Predicate<List<String>> filter = ids -> resolver.matches(ids, Integer.toString(difficulty.ordinal()), fields.getFilename());
         if (boss instanceof InstancedBossEntity instanced) {
            filter = ids -> instanced.matchesDifficulty(ids, fields.getFilename());
         }

         Predicate<List<String>> difficultyFilter = filter;
         return fields.getCustomLootTable()
            .getEntries()
            .stream()
            .filter(EliteCustomLootEntry.class::isInstance)
            .map(EliteCustomLootEntry.class::cast)
            .filter(entry -> entry.eligibleForClassLoot(player, difficultyFilter))
            .filter(entry -> ClassLootItemConstructor.available(CustomItem.getCustomItem(entry.getFilename()), difficulty, rank))
            .toList();
      }
   }

   public static List<ClassLootFamily> availableFamilies(
      CustomBossesConfigFields fields, ClassLootSettingsConfig.Difficulty difficulty, ClassLootSettingsConfig.Rank rank, Player player
   ) {
      return candidates(fields, difficulty, rank, player, null)
         .stream()
         .map(entry -> CustomItem.getCustomItem(entry.getFilename()).getCustomItemsConfigFields().getClassLootFamily())
         .distinct()
         .toList();
   }
}
