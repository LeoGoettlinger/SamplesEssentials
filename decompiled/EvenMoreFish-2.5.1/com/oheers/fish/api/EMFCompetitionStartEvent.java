package com.oheers.fish.api;

import com.oheers.fish.competition.Competition;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

public class EMFCompetitionStartEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Competition competition;

   @ApiStatus.Internal
   public EMFCompetitionStartEvent(@NonNull Competition competition) {
      this.competition = competition;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NonNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @NonNull
   public Competition getCompetition() {
      return this.competition;
   }
}
