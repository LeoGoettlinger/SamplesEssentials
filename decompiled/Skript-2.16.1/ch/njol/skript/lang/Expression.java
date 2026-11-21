package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.simplification.Simplifiable;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.slot.Slot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface Expression<T> extends SyntaxElement, Debuggable, Loopable<T>, Simplifiable<Expression<? extends T>> {
   @Nullable
   T getSingle(Event var1);

   default Optional<T> getOptionalSingle(Event event) {
      return Optional.ofNullable(this.getSingle(event));
   }

   T[] getArray(Event var1);

   T[] getAll(Event var1);

   default Stream<? extends T> stream(Event event) {
      Iterator<? extends T> iterator = this.iterator(event);
      return iterator == null ? Stream.empty() : StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
   }

   default Stream<? extends T> streamAll(Event event) {
      Iterator<? extends T> iterator = Arrays.stream(this.getAll(event)).iterator();
      return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
   }

   boolean isSingle();

   default boolean canBeSingle() {
      return this.isSingle();
   }

   boolean check(Event var1, Predicate<? super T> var2, boolean var3);

   boolean check(Event var1, Predicate<? super T> var2);

   @Nullable
   <R> Expression<? extends R> getConvertedExpression(Class<R>... var1);

   Class<? extends T> getReturnType();

   default Class<? extends T>[] possibleReturnTypes() {
      return new Class[]{this.getReturnType()};
   }

   default boolean canReturn(Class<?> returnType) {
      for (Class<?> type : this.possibleReturnTypes()) {
         if (returnType.isAssignableFrom(type) || type == Object.class) {
            return true;
         }
      }

      return false;
   }

   default boolean canReturnAnyOf(Class<?>... returnTypes) {
      for (Class<?> type : returnTypes) {
         if (this.canReturn(type)) {
            return true;
         }
      }

      return false;
   }

   boolean getAnd();

   boolean setTime(int var1);

   int getTime();

   default boolean returnNestedStructures(boolean nested) {
      return false;
   }

   default boolean returnsNestedStructures() {
      return false;
   }

   boolean isDefault();

   Expression<?> getSource();

   default Expression<? extends T> simplify() {
      return this;
   }

   @Nullable
   Class<?>[] acceptChange(Changer.ChangeMode var1);

   default Map<Changer.ChangeMode, Class<?>[]> getAcceptedChangeModes() {
      Map<Changer.ChangeMode, Class<?>[]> map = new HashMap<>();

      for (Changer.ChangeMode mode : Changer.ChangeMode.values()) {
         Class<?>[] validClasses = this.acceptChange(mode);
         if (validClasses != null) {
            map.put(mode, validClasses);
         }
      }

      return map;
   }

   void change(Event var1, @Nullable Object[] var2, Changer.ChangeMode var3);

   @Internal
   default <R> void changeInPlace(Event event, Function<T, R> changeFunction) {
      this.changeInPlace(event, changeFunction, false);
   }

   @Internal
   default <R> void changeInPlace(Event event, Function<T, R> changeFunction, boolean getAll) {
      T[] values = getAll ? this.getAll(event) : this.getArray(event);
      if (values.length != 0) {
         Class<?>[] validClasses = Arrays.stream(this.acceptChange(Changer.ChangeMode.SET))
            .map(c -> c.isArray() ? c.getComponentType() : c)
            .toArray(Class[]::new);
         List<R> newValues = new ArrayList<>();

         for (T value : values) {
            R newValue = changeFunction.apply(value);
            if (newValue != null && Changer.ChangerUtils.acceptsChangeTypes(validClasses, newValue.getClass())) {
               newValues.add(newValue);
            }
         }

         this.change(event, newValues.toArray(), Changer.ChangeMode.SET);
      }
   }

   @Nullable
   default Object[] beforeChange(Expression<?> changed, @Nullable Object[] delta) {
      if (delta != null && delta.length != 0) {
         Object[] newDelta = null;
         if (changed instanceof Variable) {
            newDelta = new Object[delta.length];

            for (int i = 0; i < delta.length; i++) {
               Object value = delta[i];
               if (value instanceof Slot) {
                  ItemStack item = ((Slot)value).getItem();
                  if (item != null) {
                     item = item.clone();
                  }

                  newDelta[i] = item;
               } else {
                  newDelta[i] = Classes.clone(delta[i]);
               }
            }
         }

         return newDelta == null ? delta : newDelta;
      } else {
         return null;
      }
   }

   @NotNull
   @Override
   default String getSyntaxTypeName() {
      return "expression";
   }
}
