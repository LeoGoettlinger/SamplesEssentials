package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public class EvtPressurePlate extends SkriptEvent {
   private boolean tripwire;

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parser) {
      this.tripwire = matchedPattern == 1;
      return true;
   }

   @Override
   public boolean check(Event event) {
      PlayerInteractEvent interactEvent = (PlayerInteractEvent)event;
      Block clickedBlock = interactEvent.getClickedBlock();
      Material type = clickedBlock == null ? null : clickedBlock.getType();
      if (type == null || interactEvent.getAction() != Action.PHYSICAL) {
         return false;
      } else {
         return !this.tripwire ? Tag.PRESSURE_PLATES.isTagged(type) : type == Material.TRIPWIRE || type == Material.TRIPWIRE_HOOK;
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return this.tripwire ? "trip" : "stepping on a pressure plate";
   }

   static {
      Skript.registerEvent(
            "Pressure Plate / Trip",
            EvtPressurePlate.class,
            PlayerInteractEvent.class,
            "[step[ping] on] [a] [pressure] plate",
            "(trip|[step[ping] on] [a] tripwire)"
         )
         .description("Called when a <i>player</i> steps on a pressure plate or tripwire respectively.")
         .examples("on step on pressure plate:")
         .since("1.0 (pressure plate), 1.4.4 (tripwire)");
   }
}
