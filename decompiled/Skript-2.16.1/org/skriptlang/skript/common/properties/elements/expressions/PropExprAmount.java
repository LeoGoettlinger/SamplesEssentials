package org.skriptlang.skript.common.properties.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RelatedProperty;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Amount")
@Description({"The amount of something.\nUsing 'amount of {list::*}' will return the length of the list, so if you want the amounts of the things inside the lists, use 'amounts of {list::*}'.\n"})
@Example.Examples({@Example("message \"There are %amount of all players% players online!\""), @Example("if amount of player's tool > 5:"), @Example("if amounts of player's tool and player's offhand tool > 5:")})
@Since({"1.0", "2.13 (amounts of)"})
@RelatedProperty("amount")
public class PropExprAmount extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {
   private ExpressionList<?> exprs;
   @Nullable
   private Variable<?> list;
   private boolean useProperties;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)PropertyExpression.infoBuilder(PropExprAmount.class, Object.class, "amount[:s]", "objects", false)
               .supplier(PropExprAmount::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.useProperties = parseResult.hasTag("s") || expressions[0].isSingle();
      if (this.useProperties) {
         return super.init(expressions, matchedPattern, isDelayed, parseResult);
      } else {
         this.exprs = asExprList(expressions[0]);
         if (expressions[0] instanceof Variable<?> variable) {
            this.list = variable;
         }

         return LiteralUtils.canInitSafely(this.exprs);
      }
   }

   @Internal
   public static ExpressionList<?> asExprList(Expression<?> expr) {
      ExpressionList<?> exprs;
      if (expr instanceof ExpressionList<?> exprList) {
         exprs = exprList;
      } else {
         exprs = new ExpressionList<>(new Expression[]{expr}, Object.class, false);
      }

      return (ExpressionList<?>)LiteralUtils.defendExpression(exprs);
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      if (this.useProperties) {
         return super.get(event);
      } else {
         return this.list != null ? new Long[]{(long)this.list.size(event)} : new Long[]{(long)this.exprs.getArray(event).length};
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return this.useProperties ? super.acceptChange(mode) : null;
   }

   @NotNull
   @Override
   public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
      return Property.AMOUNT;
   }

   @Override
   public boolean isSingle() {
      return this.useProperties ? super.isSingle() : true;
   }

   @Override
   public Class<?> getReturnType() {
      return this.useProperties ? super.getReturnType() : Long.class;
   }

   @Override
   public Class<?>[] possibleReturnTypes() {
      return this.useProperties ? super.possibleReturnTypes() : new Class[]{Long.class};
   }

   @Override
   public String toString(Event event, boolean debug) {
      return this.useProperties ? super.toString(event, debug) : "amount of " + this.exprs.toString(event, debug);
   }
}
