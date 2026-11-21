package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Arrow Attached Block")
@Description({"Returns the attached block of an arrow.", "If running Paper 1.21.4+, the plural version of the expression should be used as it is more reliable compared to the single version."})
@Example.Examples({@Example("set hit block of last shot arrow to diamond block"), @Example("on projectile hit:\n\twait 1 tick\n\tbreak attached blocks of event-projectile\n\tkill event-projectile\n")})
@Since({"2.8.0, 2.12 (multiple blocks)"})
@RequiredPlugins({"Minecraft 1.21.4+ (multiple blocks)"})
public class ExprAttachedBlock extends PropertyExpression<Projectile, Block> {
   private boolean isMultiple;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.isMultiple = parseResult.hasTag("multiple");
      this.setExpr((Expression<? extends Projectile>)expressions[0]);
      if (!this.isMultiple) {
         this.isMultiple = true;
         String expr = this.toString(null, Skript.debug());
         this.isMultiple = false;
         Skript.warning("It is recommended to use the plural version of this expression instead: '" + expr + "'");
      }

      return true;
   }

   protected Block[] get(Event event, Projectile[] source) {
      Set<Object> blocks = new HashSet<>();

      for (Projectile projectile : source) {
         if (projectile instanceof AbstractArrow abstractArrow) {
            if (this.isMultiple) {
               blocks.addAll(abstractArrow.getAttachedBlocks());
            } else {
               blocks.add(abstractArrow.getAttachedBlock());
            }
         }
      }

      return blocks.toArray(new Block[0]);
   }

   @Override
   public Class<? extends Block> getReturnType() {
      return Block.class;
   }

   @Override
   public boolean isSingle() {
      return !this.isMultiple && this.getExpr().isSingle();
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "attached block" + (this.isMultiple ? "s" : "") + " of " + this.getExpr().toString(event, debug);
   }

   static {
      register(ExprAttachedBlock.class, Block.class, "(attached|hit) block[multiple:s]", "projectiles");
   }
}
