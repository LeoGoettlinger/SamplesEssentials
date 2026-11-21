package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveZombieSpawnEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Zombie Spawn Event")
@Description({"Triggered when a zombie spawns targeting an entity. Provides access to the grave, target entity, and location."})
@Examples({"on grave zombie spawn:", "\tbroadcast \"A zombie targeting %event-target-entity% spawned at location %event-location% from grave %event-grave%\""})
public class EvtGraveZombieSpawn extends SkriptEvent {
   private Literal<LivingEntity> targetEntity;
   private Literal<Grave> grave;
   private Literal<Location> location;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveZombieSpawnEvent event) {
         if (this.targetEntity != null) {
            this.targetEntity.check(event, new Predicate<LivingEntity>() {
               public boolean test(LivingEntity le) {
                  return le.equals(event.getTargetEntity());
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

         return true;
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave zombie spawn event "
         + (this.targetEntity != null ? " targeting entity " + this.targetEntity.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.location != null ? " at location " + this.location.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveZombieSpawn.class, "Grave Zombie Spawn")
                  .addEvent(GraveZombieSpawnEvent.class)
                  .addPatterns(new String[]{"[grave] zombi(e|es) spaw(n|ning|ned)"}))
               .addDescription("Triggered when a zombie spawns targeting an entity. Provides access to the grave, target entity, and location.")
               .addExamples(
                  new String[]{
                     "on grave zombie spawn:",
                     "\tbroadcast \"A zombie targeting %event-target-entity% spawned at location %event-location% from grave %event-grave%\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(
         EventValue.builder(GraveZombieSpawnEvent.class, LivingEntity.class)
            .getter(GraveZombieSpawnEvent::getTargetEntity)
            .patterns(new String[]{"target[-]entity"})
            .build()
      );
      registry.register(EventValue.builder(GraveZombieSpawnEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(
         EventValue.builder(GraveZombieSpawnEvent.class, Location.class).getter(GraveEvent::getLocation).patterns(new String[]{"location"}).build()
      );
   }
}
