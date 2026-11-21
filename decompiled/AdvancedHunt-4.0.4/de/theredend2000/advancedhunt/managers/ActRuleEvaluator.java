package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.lib.caffeine.cache.Cache;
import de.theredend2000.advancedhunt.lib.caffeine.cache.Caffeine;
import de.theredend2000.advancedhunt.lib.cronutils.model.CronType;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.time.ExecutionTime;
import de.theredend2000.advancedhunt.lib.cronutils.parser.CronParser;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.ActFormatParser;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

public class ActRuleEvaluator {
   private final JavaPlugin plugin;
   private final CronParser cronParser;
   private final Cache<UUID, ActRuleEvaluator.AvailabilityCacheEntry> availabilityCache;

   public ActRuleEvaluator(JavaPlugin plugin) {
      this.plugin = plugin;
      this.cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
      this.availabilityCache = Caffeine.newBuilder().maximumSize(1000L).build();
   }

   public boolean isCollectionAvailable(Collection collection) {
      ZonedDateTime now = ZonedDateTime.now();
      ActRuleEvaluator.AvailabilityCacheEntry cached = this.availabilityCache.getIfPresent(collection.getId());
      if (cached != null && !cached.isExpired(now)) {
         return cached.available;
      } else {
         boolean available = this.calculateAvailability(collection, now);
         Optional<ZonedDateTime> nextTransition = this.getNextStateTransition(collection, now, available);
         ZonedDateTime expiresAt = nextTransition.orElse(now.plusMinutes(1L));
         this.availabilityCache.put(collection.getId(), new ActRuleEvaluator.AvailabilityCacheEntry(available, expiresAt));
         return available;
      }
   }

   private boolean calculateAvailability(Collection collection, ZonedDateTime now) {
      if (!collection.isEnabled()) {
         return false;
      } else {
         List<ActRule> enabledRules = collection.getEnabledActRules();
         if (enabledRules.isEmpty()) {
            return true;
         } else {
            for (ActRule rule : enabledRules) {
               if (this.isRuleActive(collection.getId(), rule, now)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public boolean isRuleActive(UUID collectionId, ActRule rule, ZonedDateTime now) {
      try {
         String actFormat = rule.getActFormat();
         Optional<ActFormatParser.ActSchedule> scheduleOpt = ActFormatParser.parse(actFormat);
         if (!scheduleOpt.isPresent()) {
            this.plugin.getLogger().warning("Invalid ACT format for rule " + rule.getName() + ": " + actFormat);
            return false;
         } else {
            ActFormatParser.ActSchedule schedule = scheduleOpt.get();
            return schedule.isAvailable(now, null);
         }
      } catch (Exception var7) {
         this.plugin.getLogger().warning("Error checking ACT rule " + rule.getName() + ": " + var7.getMessage());
         return false;
      }
   }

   public Optional<ZonedDateTime> getNextActivation(Collection collection) {
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime earliest = null;

      for (ActRule rule : collection.getEnabledActRules()) {
         try {
            String actFormat = rule.getActFormat();
            Optional<ActFormatParser.ActSchedule> scheduleOpt = ActFormatParser.parse(actFormat);
            if (scheduleOpt.isPresent()) {
               Optional<ZonedDateTime> nextTrigger = scheduleOpt.get().getNextTrigger(now);
               if (nextTrigger.isPresent() && (earliest == null || nextTrigger.get().isBefore(earliest))) {
                  earliest = nextTrigger.get();
               }
            }
         } catch (Exception var9) {
         }
      }

      return Optional.ofNullable(earliest);
   }

   public Optional<ZonedDateTime> getNextDeactivation(Collection collection) {
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime earliest = null;

      for (ActRule rule : collection.getEnabledActRules()) {
         if (this.isRuleActive(collection.getId(), rule, now)) {
            try {
               String actFormat = rule.getActFormat();
               Optional<ActFormatParser.ActSchedule> scheduleOpt = ActFormatParser.parse(actFormat);
               if (scheduleOpt.isPresent()) {
                  ActFormatParser.ActSchedule schedule = scheduleOpt.get();
                  if (!schedule.isNone() && schedule.getActiveDuration() != null) {
                     ExecutionTime executionTime = ExecutionTime.forCron(this.cronParser.parse(schedule.getCron()));
                     Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(now);
                     if (lastExecution.isPresent()) {
                        ZonedDateTime deactivation = lastExecution.get().plus(schedule.getActiveDuration());
                        if (earliest == null || deactivation.isBefore(earliest)) {
                           earliest = deactivation;
                        }
                     }
                  }
               }
            } catch (Exception var12) {
            }
         }
      }

      return Optional.ofNullable(earliest);
   }

   public Optional<ZonedDateTime> getNextStateTransition(Collection collection, ZonedDateTime now, boolean currentlyAvailable) {
      return currentlyAvailable ? this.getNextDeactivation(collection) : this.getNextActivation(collection);
   }

   public void clearAvailabilityCache(UUID collectionId) {
      this.availabilityCache.invalidate(collectionId);
   }

   public void clearAllAvailabilityCache() {
      this.availabilityCache.invalidateAll();
   }

   private static class AvailabilityCacheEntry {
      final boolean available;
      final ZonedDateTime expiresAt;

      AvailabilityCacheEntry(boolean available, ZonedDateTime expiresAt) {
         this.available = available;
         this.expiresAt = expiresAt;
      }

      boolean isExpired(ZonedDateTime now) {
         return this.expiresAt != null && now.isAfter(this.expiresAt);
      }
   }
}
