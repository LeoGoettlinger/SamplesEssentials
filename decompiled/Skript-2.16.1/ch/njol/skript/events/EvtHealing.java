package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.jetbrains.annotations.Nullable;

public class EvtHealing extends SkriptEvent {
   @Nullable
   private Literal<EntityData<?>> entityDatas;
   @Nullable
   private Literal<RegainReason> healReasons;

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parser) {
      this.entityDatas = (Literal<EntityData<?>>)args[0];
      this.healReasons = (Literal<RegainReason>)args[1];
      return true;
   }

   @Override
   public boolean check(Event event) {
      if (!(event instanceof EntityRegainHealthEvent healthEvent)) {
         return false;
      } else {
         if (this.entityDatas != null) {
            Entity compare = healthEvent.getEntity();
            boolean result = false;

            for (EntityData<?> entityData : this.entityDatas.getAll()) {
               if (entityData.isInstance(compare)) {
                  result = true;
                  break;
               }
            }

            if (!result) {
               return false;
            }
         }

         if (this.healReasons != null) {
            RegainReason compare = healthEvent.getRegainReason();
            boolean result = false;

            for (RegainReason healReason : this.healReasons.getAll()) {
               if (healReason == compare) {
                  result = true;
                  break;
               }
            }

            if (!result) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "heal"
         + (this.entityDatas != null ? " of " + this.entityDatas.toString(event, debug) : "")
         + (this.healReasons != null ? " by " + this.healReasons.toString(event, debug) : "");
   }

   static {
      Skript.registerEvent(
            "Heal",
            EvtHealing.class,
            EntityRegainHealthEvent.class,
            "heal[ing] [of %-entitydatas%] [(from|due to|by) %-healreasons%]",
            "%entitydatas% heal[ing] [(from|due to|by) %-healreasons%]"
         )
         .description(
            "Called when an entity is healed, e.g. by eating (players), being fed (pets), or by the effect of a potion of healing (overworld mobs) or harm (nether mobs)."
         )
         .examples(
            "on heal:",
            "on player healing from a regeneration potion:",
            "on healing of a zombie, cow or a wither:",
            "\theal reason is healing potion",
            "\tcancel event"
         )
         .since("1.0, 2.9.0 (by reason)");
   }
}
