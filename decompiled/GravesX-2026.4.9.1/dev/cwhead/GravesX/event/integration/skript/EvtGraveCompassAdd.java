package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveCompassAddEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.event.graveevent.GravePlayerEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Compass Add Event")
@Description({"Triggered when a grave compass is added to a users inventory."})
@Examples({"on grave compass add:", "\tbroadcast \"Grave compass for %event-grave% for grave location %event-location% added to %event-player%\""})
public class EvtGraveCompassAdd extends SkriptEvent {
   private Literal<Player> player;
   private Literal<Grave> grave;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveCompassAddEvent event) {
         return this.player != null && !this.player.check(event, p -> p.equals(event.getPlayer()))
            ? false
            : this.grave == null || this.grave.check(event, g -> g.equals(event.getGrave()));
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave compass add event "
         + (this.player != null ? " with player " + this.player.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveCompassAdd.class, "Grave Compass Add")
                  .addEvent(GraveCompassAddEvent.class)
                  .addPatterns(new String[]{"[grave] compas(s|ses) ad(d|ding|ded)"}))
               .addDescription("Triggered when a grave compass is added to a users inventory.")
               .addExamples(
                  new String[]{
                     "on grave compass add:", "\tbroadcast \"Grave compass for %event-grave% for grave location %event-location% added to %event-player%\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(
         EventValue.builder(GraveCompassAddEvent.class, Player.class).getter(GravePlayerEvent::getPlayer).patterns(new String[]{"player"}).build()
      );
      registry.register(EventValue.builder(GraveCompassAddEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
   }
}
