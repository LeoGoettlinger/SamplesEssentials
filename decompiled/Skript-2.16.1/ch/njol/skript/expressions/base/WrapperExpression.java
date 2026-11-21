package ch.njol.skript.expressions.base;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import java.util.Iterator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public abstract class WrapperExpression<T> extends SimpleExpression<T> {
   private Expression<? extends T> expr;

   protected WrapperExpression() {
   }

   public WrapperExpression(Expression<? extends T> expr) {
      this.expr = expr;
   }

   protected void setExpr(Expression<? extends T> expr) {
      this.expr = expr;
   }

   public Expression<?> getExpr() {
      return this.expr;
   }

   @Override
   protected T[] get(Event event) {
      return (T[])this.expr.getArray(event);
   }

   @Nullable
   @Override
   public Iterator<? extends T> iterator(Event event) {
      return this.expr.iterator(event);
   }

   @Override
   public boolean isSingle() {
      return this.expr.isSingle();
   }

   @Override
   public boolean getAnd() {
      return this.expr.getAnd();
   }

   @Override
   public Class<? extends T> getReturnType() {
      return this.expr.getReturnType();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return this.expr.acceptChange(mode);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      this.expr.change(event, delta, mode);
   }

   @Override
   public boolean setTime(int time) {
      return this.expr.setTime(time);
   }

   @Override
   public int getTime() {
      return this.expr.getTime();
   }

   @Override
   public boolean returnNestedStructures(boolean nested) {
      return this.expr.returnNestedStructures(nested);
   }

   @Override
   public boolean returnsNestedStructures() {
      return this.expr.returnsNestedStructures();
   }

   @Override
   public boolean isDefault() {
      return this.expr.isDefault();
   }

   @Override
   public Expression<? extends T> simplify() {
      this.setExpr(this.expr.simplify());
      return (Expression<? extends T>)(this.getExpr() instanceof Literal ? SimplifiedLiteral.fromExpression(this) : this);
   }

   @Nullable
   @Override
   public Object[] beforeChange(Expression<?> changed, @Nullable Object[] delta) {
      return this.expr.beforeChange(changed, delta);
   }

   @Override
   public Class<? extends T>[] possibleReturnTypes() {
      return this.expr.possibleReturnTypes();
   }

   @Override
   public boolean canReturn(Class<?> returnType) {
      return this.expr.canReturn(returnType);
   }
}
