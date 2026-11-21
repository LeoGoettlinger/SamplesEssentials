package ch.njol.skript.lang;

import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;

public class LiteralList<T> extends ExpressionList<T> implements Literal<T> {
   public LiteralList(Literal<? extends T>[] literals, Class<T> returnType, boolean and) {
      super(literals, returnType, and);
   }

   public LiteralList(Literal<? extends T>[] literals, Class<T> returnType, Class<?>[] possibleReturnTypes, boolean and) {
      super(literals, returnType, possibleReturnTypes, and);
   }

   public LiteralList(Literal<? extends T>[] literals, Class<T> returnType, boolean and, LiteralList<?> source) {
      super(literals, returnType, and, source);
   }

   public LiteralList(Literal<? extends T>[] literals, Class<T> returnType, Class<?>[] possibleReturnTypes, boolean and, LiteralList<?> source) {
      super(literals, returnType, possibleReturnTypes, and, source);
   }

   @Override
   public T[] getArray() {
      return this.getArray(null);
   }

   @Override
   public T getSingle() {
      return this.getSingle(null);
   }

   @Override
   public T[] getAll() {
      return this.getAll(null);
   }

   @Nullable
   @Override
   public <R> Literal<? extends R> getConvertedExpression(Class<R>... to) {
      Literal<? extends R>[] exprs = new Literal[this.expressions.length];
      Class<?>[] returnTypes = new Class[this.expressions.length];

      for (int i = 0; i < exprs.length; i++) {
         if ((exprs[i] = (Literal<? extends R>)this.expressions[i].<R>getConvertedExpression(to)) == null) {
            return null;
         }

         returnTypes[i] = exprs[i].getReturnType();
      }

      return new LiteralList<>(exprs, (Class<R>)Classes.getSuperClassInfo(returnTypes).getC(), returnTypes, this.and, this);
   }

   public Literal<? extends T>[] getExpressions() {
      return (Literal<? extends T>[])super.getExpressions();
   }

   @Override
   public Expression<T> simplify() {
      return this;
   }
}
