package ch.njol.skript.lang;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;

@Deprecated(
   since = "2.14",
   forRemoval = true
)
public class ExpressionInfo<E extends ch.njol.skript.lang.Expression<T>, T> extends SyntaxElementInfo<E> {
   @Nullable
   public ExpressionType expressionType;
   public Class<T> returnType;

   public ExpressionInfo(String[] patterns, Class<T> returnType, Class<E> expressionClass, String originClassPath) throws IllegalArgumentException {
      this(patterns, returnType, expressionClass, originClassPath, null);
   }

   public ExpressionInfo(String[] patterns, Class<T> returnType, Class<E> expressionClass, String originClassPath, @Nullable ExpressionType expressionType) throws IllegalArgumentException {
      super(patterns, expressionClass, originClassPath);
      this.returnType = returnType;
      this.expressionType = expressionType;
   }

   @Internal
   protected ExpressionInfo(DefaultSyntaxInfos.Expression<E, T> source) {
      super(source);
      this.returnType = source.returnType();
      this.expressionType = ExpressionType.fromModern(source.priority());
   }

   public Class<T> getReturnType() {
      return this.returnType;
   }

   @Nullable
   public ExpressionType getExpressionType() {
      return this.expressionType;
   }
}
