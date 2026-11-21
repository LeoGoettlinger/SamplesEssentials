package ch.njol.skript.classes;

import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.OperationInfo;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.converter.Converters;

public interface Changer<T> {
   @Nullable
   Class<?>[] acceptChange(Changer.ChangeMode var1);

   void change(T[] var1, @Nullable Object[] var2, Changer.ChangeMode var3);

   public static enum ChangeMode {
      ADD,
      SET,
      REMOVE,
      REMOVE_ALL,
      DELETE,
      RESET;

      public boolean supportsKeyedChange() {
         return this == SET;
      }
   }

   public abstract static class ChangerUtils {
      public static <T> void change(@NotNull Changer<T> changer, Object[] what, @Nullable Object[] delta, Changer.ChangeMode mode) {
         changer.change((T[])what, delta, mode);
      }

      public static boolean acceptsChange(@NotNull Expression<?> expression, Changer.ChangeMode mode, Class<?>... types) {
         Class<?>[] validTypes = expression.acceptChange(mode);
         if (validTypes == null) {
            return false;
         } else {
            for (int i = 0; i < validTypes.length; i++) {
               if (validTypes[i].isArray()) {
                  validTypes[i] = validTypes[i].getComponentType();
               }
            }

            return acceptsChangeTypes(validTypes, types);
         }
      }

      @Internal
      @Nullable
      public static <T> Expression<T> acceptsChangeWithConverters(@NotNull Expression<T> expression, Changer.ChangeMode mode, Class<?>... types) {
         final Class<?>[] validTypes = expression.acceptChange(mode);
         if (validTypes == null) {
            return null;
         } else {
            for (int i = 0; i < validTypes.length; i++) {
               if (validTypes[i].isArray()) {
                  validTypes[i] = validTypes[i].getComponentType();
               }
            }

            if (acceptsChangeTypes(validTypes, types)) {
               return expression;
            } else {
               for (Class<?> type : types) {
                  if (Converters.converterExists(type, validTypes)) {
                     class ChangeWrapper extends WrapperExpression<T> {
                        public ChangeWrapper(Expression<T> expression) {
                           super(expression);
                        }

                        @Override
                        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
                           throw new UnsupportedOperationException();
                        }

                        @Override
                        public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
                           super.change(event, Converters.convert(delta, (Class<? extends Object>[])validTypes, Object.class), mode);
                        }

                        @Override
                        public <R> void changeInPlace(Event event, Function<T, R> changeFunction, boolean getAll) {
                           T[] values = (T[])(getAll ? this.getAll(event) : this.getArray(event));
                           if (values.length != 0) {
                              List<R> newValues = new ArrayList<>();

                              for (T value : values) {
                                 newValues.add(changeFunction.apply(value));
                              }

                              this.change(event, newValues.toArray(), Changer.ChangeMode.SET);
                           }
                        }

                        @Override
                        public String toString(@Nullable Event event, boolean debug) {
                           return this.getExpr().toString(event, debug);
                        }
                     }

                     return new ChangeWrapper(expression);
                  }
               }

               return null;
            }
         }
      }

      public static boolean acceptsChangeTypes(Class<?>[] validTypes, @NotNull Class<?>... types) {
         for (Class<?> type : types) {
            for (Class<?> validType : validTypes) {
               if (validType.isAssignableFrom(type)) {
                  return true;
               }
            }
         }

         return false;
      }

      public static <T> Class<?>[] getArithmeticChangeTypes(Class<T> type, Changer.ChangeMode mode, Predicate<OperationInfo<T, ?, ?>> filter) {
         Preconditions.checkArgument(
            mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE, "Only ADD and REMOVE modes are supported for arithmetic change types"
         );
         List<OperationInfo<T, ?, ?>> opInfos;
         if (mode == Changer.ChangeMode.ADD) {
            opInfos = Arithmetics.getOperations(Operator.ADDITION, type);
         } else {
            opInfos = Arithmetics.getOperations(Operator.SUBTRACTION, type);
         }

         return opInfos.stream().filter(filter).map(OperationInfo::right).toArray(Class[]::new);
      }
   }
}
