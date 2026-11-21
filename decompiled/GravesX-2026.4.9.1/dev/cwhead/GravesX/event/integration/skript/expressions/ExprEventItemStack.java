package dev.cwhead.GravesX.event.integration.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dev.cwhead.GravesX.event.GraveItemTakeEvent;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ExprEventItemStack extends SimpleExpression<ItemStack> {
   @Nullable
   protected ItemStack[] get(Event e) {
      return e instanceof GraveItemTakeEvent event ? new ItemStack[]{event.getItem()} : null;
   }

   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      return true;
   }

   public Class<? extends ItemStack> getReturnType() {
      return ItemStack.class;
   }

   public boolean isSingle() {
      return true;
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "event itemstack";
   }
}
