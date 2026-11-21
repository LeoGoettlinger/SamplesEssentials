package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveVirtualOpenEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Virtual Open Event")
@Description({"Triggered when a virtual grave open is about to be processed. Provides access to the entity, player (if applicable), grave, location, and distance."})
@Examples({"on grave virtual open:", "\tbroadcast \"%event-player% virtually opened grave %event-grave% from %event-number% blocks away at %event-location%\""})
public class EvtGraveVirtualOpen extends SkriptEvent {
   private Literal<Player> player;
   private Literal<Grave> grave;
   private Literal<Entity> entity;
   private Literal<Location> location;
   private Literal<Number> distance;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveVirtualOpenEvent event) {
         if (this.player != null) {
            this.player.check(event, new Predicate<Player>() {
               public boolean test(Player p) {
                  Entity ent = event.getEntity();
                  return ent instanceof Player && ent.equals(p);
               }
            });
         }

         if (this.entity != null) {
            this.entity.check(event, new Predicate<Entity>() {
               public boolean test(Entity ent) {
                  return ent.equals(event.getEntity());
               }
            });
         }

         if (this.grave != null) {
            this.grave.check(event, new Predicate<Grave>() {
               public boolean test(Grave g) {
                  return g.equals(event.getGrave());
               }
            });
         }

         if (this.location != null) {
            this.location.check(event, new Predicate<Location>() {
               public boolean test(Location l) {
                  return l.equals(event.getLocation());
               }
            });
         }

         if (this.distance != null) {
            this.distance.check(event, new Predicate<Number>() {
               public boolean test(Number n) {
                  return n.doubleValue() == event.getDistance();
               }
            });
         }

         return true;
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave virtual open event "
         + (this.player != null ? " with player " + this.player.toString(e, debug) : "")
         + (this.entity != null ? " with entity " + this.entity.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.location != null ? " with location " + this.location.toString(e, debug) : "")
         + (this.distance != null ? " with distance " + this.distance.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveVirtualOpen.class, "Grave Virtual Open")
                  .addEvent(GraveVirtualOpenEvent.class)
                  .addPatterns(new String[]{"[grave] virtua(l|ly) ope(n|ning|ned)"}))
               .addDescription(
                  "Triggered when a virtual grave open is about to be processed. Provides access to the entity, player (if applicable), grave, location, and distance."
               )
               .addExamples(
                  new String[]{
                     "on grave virtual open:",
                     "\tbroadcast \"%event-player% virtually opened grave %event-grave% from %event-number% blocks away at %event-location%\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(
         EventValue.builder(GraveVirtualOpenEvent.class, Player.class)
            .getter(event -> event.hasPlayer() ? event.getPlayer() : null)
            .patterns(new String[]{"player"})
            .build()
      );
      registry.register(
         EventValue.builder(GraveVirtualOpenEvent.class, Entity.class).getter(GraveEntityEvent::getEntity).patterns(new String[]{"entity"}).build()
      );
      registry.register(EventValue.builder(GraveVirtualOpenEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GraveVirtualOpenEvent.class, Location.class).getter(GraveEvent::getLocation).patterns(new String[]{"location"}).build()
      );
      registry.register(
         EventValue.builder(GraveVirtualOpenEvent.class, Number.class).getter(GraveVirtualOpenEvent::getDistance).patterns(new String[]{"distance"}).build()
      );
   }
}
