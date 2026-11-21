package ch.njol.skript.util;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ClassInfoReference {
   private Kleenean plural;
   private ClassInfo<?> classInfo;

   @Nullable
   private static UnparsedLiteral getSourceUnparsedLiteral(Expression<?> expression) {
      while (!(expression instanceof UnparsedLiteral)) {
         Expression<?> nextEarliestExpression = expression.getSource();
         if (nextEarliestExpression == expression) {
            return null;
         }

         expression = nextEarliestExpression;
      }

      return (UnparsedLiteral)expression;
   }

   private static Kleenean determineIfPlural(Expression<ClassInfo<?>> classInfoExpression) {
      UnparsedLiteral sourceUnparsedLiteral = getSourceUnparsedLiteral(classInfoExpression);
      if (sourceUnparsedLiteral == null) {
         return Kleenean.UNKNOWN;
      } else {
         String originalExpr = sourceUnparsedLiteral.getData();
         boolean isPlural = Utils.getEnglishPlural(originalExpr).getSecond();
         return Kleenean.get(isPlural);
      }
   }

   @NotNull
   public static Expression<ClassInfoReference> wrap(@NotNull final Expression<ClassInfo<?>> classInfoExpression) {
      if (classInfoExpression instanceof ExpressionList<?> classInfoExpressionList) {
         Expression<ClassInfoReference>[] wrappedExpressions = Arrays.stream(classInfoExpressionList.getExpressions())
            .map(expression -> wrap((Expression<ClassInfo<?>>)expression))
            .toArray(Expression[]::new);
         return new ExpressionList<>(wrappedExpressions, ClassInfoReference.class, classInfoExpression.getAnd());
      } else {
         final Kleenean isPlural = determineIfPlural(classInfoExpression);
         if (classInfoExpression instanceof Literal<ClassInfo<?>> classInfoLiteral) {
            ClassInfo<?> classInfo = classInfoLiteral.getSingle();
            return new SimpleLiteral<>(new ClassInfoReference(classInfo, isPlural), classInfoLiteral.isDefault());
         } else {
            return new SimpleExpression<ClassInfoReference>() {
               @Nullable
               protected ClassInfoReference[] get(Event event) {
                  if (classInfoExpression.isSingle()) {
                     ClassInfo<?> classInfo = classInfoExpression.getSingle(event);
                     return classInfo == null ? new ClassInfoReference[0] : new ClassInfoReference[]{new ClassInfoReference(classInfo, isPlural)};
                  } else {
                     return classInfoExpression.stream(event).map(ClassInfoReference::new).toArray(ClassInfoReference[]::new);
                  }
               }

               @Override
               public boolean isSingle() {
                  return classInfoExpression.isSingle();
               }

               @Override
               public Class<? extends ClassInfoReference> getReturnType() {
                  return ClassInfoReference.class;
               }

               @Override
               public String toString(@Nullable Event event, boolean debug) {
                  return debug
                     ? classInfoExpression.toString(event, true) + "(wrapped by " + this.getClass().getSimpleName() + ")"
                     : classInfoExpression.toString(event, false);
               }

               @Override
               public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
                  return classInfoExpression.init(expressions, matchedPattern, isDelayed, parseResult);
               }
            };
         }
      }
   }

   public ClassInfoReference(ClassInfo<?> classInfo) {
      this(classInfo, Kleenean.UNKNOWN);
   }

   public ClassInfoReference(ClassInfo<?> classInfo, Kleenean plural) {
      this.classInfo = classInfo;
      this.plural = plural;
   }

   public Kleenean isPlural() {
      return this.plural;
   }

   public void setPlural(Kleenean plural) {
      this.plural = plural;
   }

   public ClassInfo<?> getClassInfo() {
      return this.classInfo;
   }

   public void setClassInfo(ClassInfo<?> classInfo) {
      this.classInfo = classInfo;
   }
}
