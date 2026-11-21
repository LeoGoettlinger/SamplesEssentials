package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.jetbrains.annotations.Nullable;

@Name("Barter Input")
@Description({"The item picked up by the piglin in a piglin bartering event."})
@Example("on piglin barter:\n\tif the bartering input is a gold ingot:\n\t\tbroadcast \"my precious...\"\n")
@Since({"2.10"})
public class ExprBarterInput extends SimpleExpression<ItemType> implements EventRestrictedSyntax {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult result) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(PiglinBarterEvent.class);
   }

   @Nullable
   protected ItemType[] get(Event event) {
      return !(event instanceof PiglinBarterEvent) ? null : new ItemType[]{new ItemType(((PiglinBarterEvent)event).getInput())};
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends ItemType> getReturnType() {
      return ItemType.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the barter input";
   }

   static {
      if (Skript.classExists("org.bukkit.event.entity.PiglinBarterEvent")) {
         Skript.registerExpression(ExprBarterInput.class, ItemType.class, ExpressionType.SIMPLE, "[the] [piglin] barter[ing] input");
      }
   }
}
