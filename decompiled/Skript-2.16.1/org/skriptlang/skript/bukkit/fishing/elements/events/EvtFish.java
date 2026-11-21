package org.skriptlang.skript.bukkit.fishing.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.EventValues;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtFish extends SkriptEvent {
   private EvtFish.State state;

   public static void register(SyntaxRegistry registry) {
      List<String> patterns = new ArrayList<>();

      for (EvtFish.State state : EvtFish.State.values()) {
         if (state.state != null || state == EvtFish.State.STATE_CHANGE) {
            patterns.add(state.pattern);
         }
      }

      registry.register(
         BukkitSyntaxInfos.Event.KEY,
         ((BukkitSyntaxInfos.Event.Builder)((BukkitSyntaxInfos.Event.Builder)BukkitSyntaxInfos.Event.builder(EvtFish.class, "Fishing").addPatterns(patterns))
               .addEvent(PlayerFishEvent.class)
               .addDescription(
                  "Called when a player triggers a fishing event.",
                  "An entity hooked event is triggered when an entity gets caught by a fishing rod.",
                  "A fish escape event is called when the player fails to click on time, and the fish escapes.",
                  "A fish approaching event is when the bobber is waiting to be hooked, and a fish is approaching.",
                  "A fishing state change event is triggered whenever the fishing state changes."
               )
               .addExample("on fishing line cast:\n\tsend \"You caught a fish!\" to player\n")
               .addExample("on entity caught:\n\tpush event-entity vector from entity to player\n")
               .addExample("on fishing state change:\n\tif event-fishing state is fish caught:\n\t\tbroadcast \"A fish has been caught!\"\n")
               .addSince("2.10", "2.11 (state change)")
               .supplier(EvtFish::new))
            .build()
      );
      EventValues.registerEventValue(PlayerFishEvent.class, Entity.class, PlayerFishEvent::getCaught);
      EventValues.registerEventValue(PlayerFishEvent.class, org.bukkit.event.player.PlayerFishEvent.State.class, PlayerFishEvent::getState);
      EventValues.registerEventValue(PlayerFishEvent.class, FishHook.class, PlayerFishEvent::getHook);
   }

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.state = EvtFish.State.values()[matchedPattern];
      return true;
   }

   @Override
   public boolean check(Event event) {
      return !(event instanceof PlayerFishEvent fishEvent) ? false : this.state == EvtFish.State.STATE_CHANGE || this.state.state == fishEvent.getState();
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.state.toString;
   }

   private static enum State {
      FISHING(org.bukkit.event.player.PlayerFishEvent.State.FISHING, "[fishing] (line|rod) cast", "fishing line cast"),
      CAUGHT_FISH(org.bukkit.event.player.PlayerFishEvent.State.CAUGHT_FISH, "fish (caught|catch)", "fish caught"),
      CAUGHT_ENTITY(org.bukkit.event.player.PlayerFishEvent.State.CAUGHT_ENTITY, "entity (hook[ed]|caught|catch)", "entity hooked"),
      IN_GROUND(org.bukkit.event.player.PlayerFishEvent.State.IN_GROUND, "(bobber|hook) (in|hit) ground", "bobber hit ground"),
      FISH_ESCAPE(org.bukkit.event.player.PlayerFishEvent.State.FAILED_ATTEMPT, "fish (escape|get away)", "fish escape"),
      REEL_IN(org.bukkit.event.player.PlayerFishEvent.State.REEL_IN, "[fishing] (rod|line) reel in", "fishing rod reel in"),
      BITE(org.bukkit.event.player.PlayerFishEvent.State.BITE, "fish bit(e|ing)", "fish bite"),
      LURED(getOptional("LURED"), "(fish approach[ing]|(bobber|hook) lure[d])", "fish approaching"),
      STATE_CHANGE(null, "fishing state change[d]", "fishing state changed");

      @Nullable
      private final org.bukkit.event.player.PlayerFishEvent.State state;
      private final String pattern;
      private final String toString;

      private State(@Nullable org.bukkit.event.player.PlayerFishEvent.State state, @NotNull String pattern, @NotNull String toString) {
         this.state = state;
         this.pattern = pattern;
         this.toString = toString;
      }

      private static org.bukkit.event.player.PlayerFishEvent.State getOptional(String name) {
         try {
            return org.bukkit.event.player.PlayerFishEvent.State.valueOf(name);
         } catch (IllegalArgumentException var2) {
            return null;
         }
      }
   }
}
