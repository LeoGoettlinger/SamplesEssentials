package dev.cwhead.GravesX.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveBreakEvent;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.event.graveevent.GravePlayerEvent;
import dev.cwhead.GravesX.integration.SkriptImpl;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;

@Name("Grave Break Event")
@Description({"Triggered when a grave block is broken. Provides access to the grave, player, block, and block type."})
@Examples({"on grave break:", "\tbroadcast \"%event-player% broke grave %event-grave% at block %event-block% with experience %event-blockexp%\""})
public class EvtGraveBreak extends SkriptEvent {
   private Literal<Player> player;
   private Literal<Grave> grave;
   private Literal<Block> block;
   private Literal<Integer> blockExp;

   public boolean init(@NotNull Literal<?>[] args, int matchedPattern, @NotNull ParseResult parseResult) {
      return true;
   }

   public boolean check(Event e) {
      if (e instanceof GraveBreakEvent event) {
         if (this.player != null && !this.player.check(event, playerValue -> playerValue.equals(event.getPlayer()))) {
            return false;
         } else if (this.grave != null && !this.grave.check(event, graveValue -> graveValue.equals(event.getGrave()))) {
            return false;
         } else {
            return this.block != null && !this.block.check(event, blockValue -> blockValue.equals(event.getBlock()))
               ? false
               : this.blockExp == null || this.blockExp.check(event, blockExpValue -> blockExpValue.equals(event.getBlockExp()));
         }
      } else {
         return false;
      }
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "Grave break event "
         + (this.player != null ? " with player " + this.player.toString(e, debug) : "")
         + (this.grave != null ? " with grave " + this.grave.toString(e, debug) : "")
         + (this.block != null ? " with block " + this.block.toString(e, debug) : "")
         + (this.blockExp != null ? " with block experience " + this.blockExp.toString(e, debug) : "");
   }

   static {
      SkriptAddon addon = SkriptImpl.getActiveSkriptAddon();
      addon.syntaxRegistry()
         .register(
            org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.KEY,
            ((Builder)org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.builder(EvtGraveBreak.class, "Grave Break")
                  .addEvent(GraveBreakEvent.class)
                  .addPatterns(new String[]{"[grave] br(eak|eaking|oken)"}))
               .addDescription("Triggered when a grave block is broken. Provides access to the grave, player, block, and block type.")
               .addExamples(
                  new String[]{
                     "on grave break:", "\tbroadcast \"%event-player% broke grave %event-grave% at block %event-block% with experience %event-blockexp%\""
                  }
               )
               .build()
         );
      EventValueRegistry registry = (EventValueRegistry)addon.registry(EventValueRegistry.class);
      registry.register(EventValue.builder(GraveBreakEvent.class, Player.class).getter(GravePlayerEvent::getPlayer).patterns(new String[]{"player"}).build());
      registry.register(EventValue.builder(GraveBreakEvent.class, Grave.class).getter(GraveEvent::getGrave).patterns(new String[]{"grave"}).build());
      registry.register(EventValue.builder(GraveBreakEvent.class, Block.class).getter(GraveEvent::getBlock).patterns(new String[]{"block"}).build());
      registry.register(
         EventValue.builder(GraveBreakEvent.class, Integer.class)
            .getter(GraveEvent::getBlockExp)
            .patterns(new String[]{"block[-]exp", "block[-]experience"})
            .build()
      );
   }
}
