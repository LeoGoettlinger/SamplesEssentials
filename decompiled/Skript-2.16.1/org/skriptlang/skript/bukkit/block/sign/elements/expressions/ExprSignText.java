package org.skriptlang.skript.bukkit.block.sign.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Sign Text")
@Description({"A line of text on a sign. Can be changed, but note that there is a 16 character limit per line."})
@Example("on right click:\n\tclicked block is tagged as \"minecraft:all_signs\"\n\tif line 2 of the clicked block is \"[Heal]\":\n\t\theal the player\n")
@Since({"1.3"})
public class ExprSignText extends SimpleExpression<Component> {
   private Expression<Integer> line;
   private Expression<Block> block;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprSignText.class, Component.class
                     )
                     .supplier(ExprSignText::new))
                  .priority(PropertyExpression.DEFAULT_PRIORITY))
               .addPatterns(new String[]{"line %integer% [of %block%]", "[the] (1:1st|1:first|2:2nd|2:second|3:3rd|3:third|4:4th|4:fourth) line [of %block%]"}))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (matchedPattern == 0) {
         this.line = (Expression<Integer>)exprs[0];
      } else {
         this.line = new SimpleLiteral<>(parseResult.mark, false);
      }

      this.block = (Expression<Block>)exprs[exprs.length - 1];
      return true;
   }

   private int getLine(Event event) {
      Integer line = this.line.getSingle(event);
      if (line == null) {
         return -1;
      } else if (line >= 1 && line <= 4) {
         line = line - 1;
         return line;
      } else {
         this.error("Signs only have lines from 1 to 4, but tried to obtain line " + line);
         return -1;
      }
   }

   protected Component[] get(Event event) {
      int line = this.getLine(event);
      if (line == -1) {
         return new Component[0];
      } else if (this.getTime() >= 0 && this.block.isDefault() && event instanceof SignChangeEvent signEvent && !Delay.isDelayed(event)) {
         return new Component[]{signEvent.line(line)};
      } else {
         Block block = this.block.getSingle(event);
         return block != null && block.getState() instanceof Sign signState ? new Component[]{signState.getSide(Side.FRONT).line(line)} : new Component[0];
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, DELETE -> (Class[])CollectionUtils.array(Component.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int line = this.getLine(event);
      if (line != -1) {
         if (this.getTime() >= 0 && this.block.isDefault() && event instanceof SignChangeEvent signEvent && !Delay.isDelayed(event)) {
            signEvent.line(line, delta == null ? null : (Component)delta[0]);
         } else {
            Block block = this.block.getSingle(event);
            if (block != null && block.getState() instanceof Sign signState) {
               signState.getSide(Side.FRONT).line(line, (Component)(delta == null ? Component.empty() : (Component)delta[0]));
               signState.update(false, false);
            }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Component> getReturnType() {
      return Component.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "line " + this.line.toString(event, debug) + " of " + this.block.toString(event, debug);
   }

   @Override
   public boolean setTime(int time) {
      return super.setTime(time, SignChangeEvent.class, this.block);
   }
}
