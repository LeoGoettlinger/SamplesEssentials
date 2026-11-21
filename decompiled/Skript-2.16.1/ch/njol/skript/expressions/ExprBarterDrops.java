package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
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
import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Barter Drops")
@Description({"The items dropped by the piglin in a piglin bartering event."})
@Example("on piglin barter:\n\tif the bartering drops contain a jack o lantern:\n\t\tremove jack o lantern from bartering output\n\t\tbroadcast \"it's not halloween yet!\"\n")
@Since({"2.10"})
public class ExprBarterDrops extends SimpleExpression<ItemType> implements EventRestrictedSyntax {
   private Kleenean delay;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult result) {
      this.delay = isDelayed;
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(PiglinBarterEvent.class);
   }

   @Nullable
   protected ItemType[] get(Event event) {
      return !(event instanceof PiglinBarterEvent) ? null : ((PiglinBarterEvent)event).getOutcome().stream().map(ItemType::new).toArray(ItemType[]::new);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!this.delay.isFalse()) {
         Skript.error("Can't change the piglin bartering drops after the event has already passed");
         return null;
      } else {
         switch (mode) {
            case SET:
            case ADD:
            case REMOVE:
            case REMOVE_ALL:
            case DELETE:
               return CollectionUtils.array(ItemType[].class);
            default:
               return null;
         }
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof PiglinBarterEvent) {
         List<ItemStack> outcome = ((PiglinBarterEvent)event).getOutcome();
         switch (mode) {
            case SET:
               outcome.clear();
            case ADD:
               for (Object item : delta) {
                  ((ItemType)item).addTo(outcome);
               }
               break;
            case REMOVE:
               for (Object item : delta) {
                  ((ItemType)item).removeFrom(false, outcome);
               }
               break;
            case REMOVE_ALL:
               for (Object item : delta) {
                  ((ItemType)item).removeAll(false, outcome);
               }
               break;
            case DELETE:
               outcome.clear();
         }
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends ItemType> getReturnType() {
      return ItemType.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the barter drops";
   }

   static {
      if (Skript.classExists("org.bukkit.event.entity.PiglinBarterEvent")) {
         Skript.registerExpression(ExprBarterDrops.class, ItemType.class, ExpressionType.SIMPLE, "[the] [piglin] barter[ing] drops");
      }
   }
}
