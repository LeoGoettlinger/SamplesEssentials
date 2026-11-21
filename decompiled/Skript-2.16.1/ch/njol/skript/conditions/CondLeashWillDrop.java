package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.jetbrains.annotations.Nullable;

@Name("Leash Will Drop")
@Description({"Checks whether the leash item will drop during the leash detaching in an unleash event."})
@Example("on unleash:\n\tif the leash will drop:\n\t\tprevent the leash from dropping\n\telse:\n\t\tallow the leash to drop\n")
@Keywords({"lead"})
@Events({"Leash / Unleash"})
@Since({"2.10"})
public class CondLeashWillDrop extends Condition implements EventRestrictedSyntax {
   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setNegated(parseResult.hasTag("not"));
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(EntityUnleashEvent.class);
   }

   @Override
   public boolean check(Event event) {
      return event instanceof EntityUnleashEvent unleashEvent ? unleashEvent.isDropLeash() ^ this.isNegated() : false;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the leash will" + (this.isNegated() ? " not" : "") + " be dropped";
   }

   static {
      if (Skript.methodExists(EntityUnleashEvent.class, "isDropLeash")) {
         Skript.registerCondition(CondLeashWillDrop.class, "[the] (lead|leash) [item] (will|not:(won't|will not)) (drop|be dropped)");
      }
   }
}
