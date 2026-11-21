package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.sections.EffSecSpawn;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.util.Direction;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import io.papermc.paper.entity.TeleportFlag;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.types.TeleportFlagClassInfo;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Teleport")
@Description({"Teleport an entity to a specific location.\nThis effect is delayed by default, as it waits for the chunk of the location to be loaded before teleporting.\nThe keyword 'force' may be used to bypass this behavior (preventing the delay), but note that it may cause lag spikes or other server performance issues when teleporting to unloaded chunks.\nTeleport flags are properties to retain during a teleport, such as direction, passengers, and velocities.\n"})
@Example.Examples({@Example("teleport the player to {home::%uuid of player%}"), @Example("teleport the attacker to the victim"), @Example("on dismount:\n\tcancel event\n\tteleport the player to {server::spawn} retaining vehicle and passengers\n")})
@Since({"1.0, 2.10 (flags)"})
public class EffTeleport extends Effect {
   private boolean async;
   private Expression<Entity> entities;
   private Expression<Location> location;
   @Nullable
   private Expression<TeleportFlagClassInfo.SkriptTeleportFlag> teleportFlags;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffTeleport.class)
            .supplier(EffTeleport::new)
            .addPattern("[:force] teleport %entities% (to|%direction%) %location% [[while] retaining %-teleportflags%]")
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (this.getParser().isCurrentEvent(EffSecSpawn.SpawnEvent.class)) {
         Skript.error("You cannot teleport an entity that hasn't spawned yet. Ensure you're using the location expression from the spawn section pattern.");
         return false;
      } else {
         this.async = !parseResult.hasTag("force");
         this.entities = (Expression<Entity>)exprs[0];
         this.location = Direction.combine((Expression<? extends Direction>)exprs[1], (Expression<? extends Location>)exprs[2]);
         this.teleportFlags = (Expression<TeleportFlagClassInfo.SkriptTeleportFlag>)exprs[3];
         if (this.async) {
            this.getParser().setHasDelayBefore(Kleenean.UNKNOWN);
         }

         return true;
      }
   }

   @Nullable
   @Override
   protected TriggerItem walk(Event event) {
      this.debug(event, true);
      TriggerItem next = this.getNext();
      Location location = this.location.getSingle(event);
      if (location == null) {
         return next;
      } else {
         boolean unknownWorld = !location.isWorldLoaded();
         Entity[] entityArray = this.entities.getArray(event);
         if (entityArray.length == 0) {
            return next;
         } else {
            if (!Delay.isDelayed(event)) {
               if (event instanceof PlayerRespawnEvent playerRespawnEvent && entityArray.length == 1 && entityArray[0].equals(playerRespawnEvent.getPlayer())) {
                  if (!unknownWorld) {
                     playerRespawnEvent.setRespawnLocation(location);
                  }

                  return next;
               }

               if (event instanceof PlayerMoveEvent playerMoveEvent && entityArray.length == 1 && entityArray[0].equals(playerMoveEvent.getPlayer())) {
                  if (unknownWorld) {
                     location = location.clone();
                     location.setWorld(playerMoveEvent.getFrom().getWorld());
                  }

                  playerMoveEvent.setTo(location);
                  return next;
               }
            }

            if (unknownWorld) {
               if (entityArray.length != 1) {
                  return next;
               }

               Entity entity = entityArray[0];
               if (entity == null) {
                  return next;
               }

               location = location.clone();
               location.setWorld(entity.getWorld());
            }

            TeleportFlag[] teleportFlags;
            if (this.teleportFlags == null) {
               teleportFlags = new TeleportFlag[0];
            } else {
               teleportFlags = this.teleportFlags
                  .stream(event)
                  .flatMap(teleportFlag -> Stream.of(teleportFlag.getTeleportFlags()))
                  .toArray(TeleportFlag[]::new);
            }

            if (this.async) {
               Location fixed = location;
               Object localVars = Variables.removeLocals(event);
               fixed.getWorld().getChunkAtAsync(fixed).thenAccept(ignored -> {
                  Delay.addDelayedEvent(event);

                  for (Entity entityx : entityArray) {
                     entityx.teleport(fixed, teleportFlags);
                  }

                  if (localVars != null) {
                     Variables.setLocalVariables(event, localVars);
                  }

                  Object timing = null;
                  if (next != null) {
                     if (SkriptTimings.enabled()) {
                        Trigger trigger = this.getTrigger();
                        if (trigger != null) {
                           timing = SkriptTimings.start(trigger.getDebugLabel());
                        }
                     }

                     TriggerItem.walk(next, event);
                  }

                  Variables.removeLocals(event);
                  SkriptTimings.stop(timing);
               });
               return null;
            } else {
               for (Entity entity : entityArray) {
                  entity.teleport(location, teleportFlags);
               }

               return next;
            }
         }
      }
   }

   @Override
   protected void execute(Event event) {
      assert false;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug)
         .append("teleport", this.entities, "to", this.location)
         .appendIf(this.teleportFlags != null, "retaining", this.teleportFlags)
         .toString();
   }
}
