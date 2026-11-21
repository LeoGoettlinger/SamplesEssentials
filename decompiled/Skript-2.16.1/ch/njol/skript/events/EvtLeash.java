package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.jetbrains.annotations.Nullable;

public class EvtLeash extends SkriptEvent {
   @Nullable
   private EntityData<?>[] types;
   private EvtLeash.EventType eventType;

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.types = args[0] == null ? null : (EntityData[])args[0].getAll();
      this.eventType = EvtLeash.EventType.LEASH;
      if (parseResult.hasTag("un")) {
         this.eventType = EvtLeash.EventType.UNLEASH;
         if (parseResult.hasTag("player")) {
            this.eventType = EvtLeash.EventType.UNLEASH_BY_PLAYER;
         }
      }

      return true;
   }

   @Override
   public boolean check(Event event) {
      Entity leashedEntity;
      switch (this.eventType) {
         case LEASH:
            if (!(event instanceof PlayerLeashEntityEvent playerLeash)) {
               return false;
            }

            leashedEntity = playerLeash.getEntity();
            break;
         case UNLEASH:
            if (!(event instanceof EntityUnleashEvent entityUnleash)) {
               return false;
            }

            leashedEntity = entityUnleash.getEntity();
            break;
         case UNLEASH_BY_PLAYER:
            if (!(event instanceof PlayerUnleashEntityEvent playerUnleash)) {
               return false;
            }

            leashedEntity = playerUnleash.getEntity();
            break;
         default:
            return false;
      }

      if (this.types == null) {
         return true;
      } else {
         for (EntityData<?> entityData : this.types) {
            if (entityData.isInstance(leashedEntity)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.eventType + (this.types != null ? " of " + Classes.toString(this.types, false) : "");
   }

   static {
      Skript.registerEvent(
            "Leash / Unleash",
            EvtLeash.class,
            CollectionUtils.array(PlayerLeashEntityEvent.class, EntityUnleashEvent.class),
            "[:player] [:un]leash[ing] [of %-entitydatas%]"
         )
         .description("Called when an entity is leashed or unleashed. Cancelling these events will prevent the leashing or unleashing from occurring.")
         .examples(
            "on player leash of a sheep:",
            "\tsend \"Baaaaa--\" to player",
            "",
            "on player leash:",
            "\tsend \"<%event-entity%> Let me go!\" to player",
            "",
            "on unleash:",
            "\tbroadcast \"<%event-entity%> I'm free\"",
            "",
            "on player unleash:",
            "\tsend \"<%event-entity%> Thanks for freeing me!\" to player"
         )
         .since("2.10");
      EventValues.registerEventValue(PlayerLeashEntityEvent.class, Player.class, PlayerLeashEntityEvent::getPlayer);
      EventValues.registerEventValue(PlayerLeashEntityEvent.class, Entity.class, PlayerLeashEntityEvent::getEntity);
      EventValues.registerEventValue(EntityUnleashEvent.class, UnleashReason.class, EntityUnleashEvent::getReason);
      EventValues.registerEventValue(PlayerUnleashEntityEvent.class, Player.class, PlayerUnleashEntityEvent::getPlayer);
   }

   private static enum EventType {
      LEASH("leash"),
      UNLEASH("unleash"),
      UNLEASH_BY_PLAYER("player unleash");

      private final String name;

      private EventType(String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return this.name;
      }
   }
}
