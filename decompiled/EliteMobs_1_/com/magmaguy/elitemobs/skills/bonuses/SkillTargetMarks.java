package com.magmaguy.elitemobs.skills.bonuses;

import com.magmaguy.elitemobs.MetadataHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.scheduler.BukkitRunnable;

public final class SkillTargetMarks {
   private final Map<UUID, SkillTargetMarks.Mark> byTarget = new HashMap<>();
   private final Map<UUID, Set<UUID>> targetsBySource = new HashMap<>();

   public void mark(UUID source, UUID target, long durationMillis) {
      SkillTargetMarks.Mark next = new SkillTargetMarks.Mark(source, target, System.nanoTime() + durationMillis * 1000000L);
      next.runTaskLater(MetadataHandler.PLUGIN, Math.max(1L, (durationMillis + 49L) / 50L));
      SkillTargetMarks.Mark previous = this.byTarget.put(target, next);
      if (previous != null) {
         this.retire(previous);
      }

      this.targetsBySource.computeIfAbsent(source, ignored -> new HashSet<>()).add(target);
   }

   public boolean isMarkedBy(UUID target, UUID source) {
      SkillTargetMarks.Mark mark = this.byTarget.get(target);
      if (mark == null) {
         return false;
      } else if (System.nanoTime() >= mark.expiresAtNanos) {
         this.retire(mark);
         return false;
      } else {
         return mark.source.equals(source);
      }
   }

   public void clearSource(UUID source) {
      Set<UUID> targets = this.targetsBySource.get(source);
      if (targets != null) {
         for (UUID target : List.copyOf(targets)) {
            SkillTargetMarks.Mark mark = this.byTarget.get(target);
            if (mark != null && mark.source.equals(source)) {
               this.retire(mark);
            }
         }
      }
   }

   public void clear() {
      this.byTarget.values().forEach(BukkitRunnable::cancel);
      this.byTarget.clear();
      this.targetsBySource.clear();
   }

   private void retire(SkillTargetMarks.Mark mark) {
      this.byTarget.remove(mark.target, mark);
      mark.cancel();
      Set<UUID> targets = this.targetsBySource.get(mark.source);
      if (targets != null) {
         targets.remove(mark.target);
         if (targets.isEmpty()) {
            this.targetsBySource.remove(mark.source);
         }
      }
   }

   private final class Mark extends BukkitRunnable {
      private final UUID source;
      private final UUID target;
      private final long expiresAtNanos;

      private Mark(UUID source, UUID target, long expiresAtNanos) {
         this.source = source;
         this.target = target;
         this.expiresAtNanos = expiresAtNanos;
      }

      public void run() {
         if (SkillTargetMarks.this.byTarget.get(this.target) == this) {
            SkillTargetMarks.this.retire(this);
         }
      }
   }
}
