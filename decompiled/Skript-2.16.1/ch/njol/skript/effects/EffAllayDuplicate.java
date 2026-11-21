package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Allay Duplicate")
@Description({"Make an allay duplicate itself.", "This effect will always make an allay duplicate regardless of whether the duplicate attribute is disabled."})
@Example("make all allays duplicate")
@Since({"2.11"})
public class EffAllayDuplicate extends Effect {
   private Expression<LivingEntity> entities;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.entities = (Expression<LivingEntity>)exprs[0];
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (LivingEntity entity : this.entities.getArray(event)) {
         if (entity instanceof Allay allay) {
            allay.duplicateAllay();
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "make " + this.entities.toString(event, debug) + " duplicate";
   }

   static {
      Skript.registerEffect(EffAllayDuplicate.class, "make %livingentities% (duplicate|clone)");
   }
}
