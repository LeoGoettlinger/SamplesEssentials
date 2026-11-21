package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveCompassUseEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.event.graveevent.GravePlayerEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Compass Use Event")
@Description({"Triggered when a grave compass is fired."})
@Examples({"on grave compass use:", "\tbroadcast \"%event-player% used grave compass for %event-grave% for grave location %event-location%\""})
public class EvtGraveCompassUse extends SkriptEvent {
   private Literal<Player> player;
   private Literal<Grave> grave;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveCompassUseEvent event) {
         if (this.player != null) {
            this.player.check(event, new Predicate<Player>() {
               public boolean test(Player p) {
                  return p.equals(event.getPlayer());
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

         return true;
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave compass use event "
         + (this.player != null ? " with player " + this.player.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveCompassUse.class, "Grave Compass Use")
                  .addEvent(GraveCompassUseEvent.class)
                  .addPatterns(new String[]{"[grave] compas(s|ses) Us(e|ing|ed)"}))
               .addDescription("Triggered when a grave compass is fired.")
               .addExamples(
                  new String[]{
                     "on grave compass use:", "\tbroadcast \"%event-player% used grave compass for %event-grave% for grave location %event-location%\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(
         EventValue.builder(GraveCompassUseEvent.class, Player.class).getter(GravePlayerEvent::getPlayer).patterns(new String[]{"player"}).build()
      );
      registry.register(EventValue.builder(GraveCompassUseEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
   }
}
