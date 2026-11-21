package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class EliteSkillXpGainEvent extends Event implements Cancellable {
   private static final HandlerList HANDLERS = new HandlerList();
   private final Player player;
   private final SkillType skillType;
   private final EliteEntity eliteEntity;
   private final long originalXp;
   private long xp;
   private boolean cancelled;

   public EliteSkillXpGainEvent(Player player, SkillType skillType, EliteEntity eliteEntity, long xp) {
      this.player = Objects.requireNonNull(player, "player");
      this.skillType = Objects.requireNonNull(skillType, "skillType");
      this.eliteEntity = Objects.requireNonNull(eliteEntity, "eliteEntity");
      this.setXp(xp);
      this.originalXp = xp;
   }

   public Player getPlayer() {
      return this.player;
   }

   public SkillType getSkillType() {
      return this.skillType;
   }

   public EliteEntity getEliteEntity() {
      return this.eliteEntity;
   }

   public EliteSkillXpGainEvent.Reason getReason() {
      return EliteSkillXpGainEvent.Reason.COMBAT;
   }

   public long getOriginalXp() {
      return this.originalXp;
   }

   public long getXp() {
      return this.xp;
   }

   public void setXp(long xp) {
      if (xp < 0L) {
         throw new IllegalArgumentException("Skill XP must be non-negative");
      } else {
         this.xp = xp;
      }
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   public static enum Reason {
      COMBAT;
   }
}
