package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class EvtBeaconEffect extends SkriptEvent {
   @Nullable
   private Literal<PotionEffectType> potionTypes;
   @Nullable
   private Boolean primaryCheck;

   @Override
   public boolean init(Literal<?>[] exprs, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.potionTypes = (Literal<PotionEffectType>)exprs[0];
      if (parseResult.hasTag("primary")) {
         this.primaryCheck = true;
      } else if (parseResult.hasTag("secondary")) {
         this.primaryCheck = false;
      }

      return true;
   }

   @Override
   public boolean check(Event event) {
      if (event instanceof BeaconEffectEvent effectEvent) {
         if (this.primaryCheck != null && effectEvent.isPrimary() != this.primaryCheck) {
            return false;
         } else {
            return this.potionTypes != null ? this.potionTypes.check(event, type -> effectEvent.getEffect().getType() == type) : true;
         }
      } else {
         return false;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.primaryCheck == null ? "" : (this.primaryCheck ? "primary " : "secondary "))
         + "beacon effect"
         + (this.potionTypes == null ? "" : " of " + this.potionTypes.toString(event, debug));
   }

   static {
      if (Skript.classExists("com.destroystokyo.paper.event.block.BeaconEffectEvent")) {
         Skript.registerEvent(
               "Beacon Effect",
               EvtBeaconEffect.class,
               BeaconEffectEvent.class,
               "[:primary|:secondary] beacon effect [of %-potioneffecttypes%]",
               "application of [:primary|:secondary] beacon effect [of %-potioneffecttypes%]",
               "[:primary|:secondary] beacon effect apply [of %-potioneffecttypes%]"
            )
            .description("Called when a player gets an effect from a beacon.")
            .examples(
               "on beacon effect:",
               "\tbroadcast applied effect",
               "\tbroadcast event-player",
               "\tbroadcast event-block",
               "on primary beacon effect apply of haste:",
               "on application of secondary beacon effect:",
               "on beacon effect of speed:"
            )
            .since("2.10");
      }
   }
}
