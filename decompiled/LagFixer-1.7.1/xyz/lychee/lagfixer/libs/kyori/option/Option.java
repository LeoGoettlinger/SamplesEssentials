package xyz.lychee.lagfixer.libs.kyori.option;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.option.value.ValueType;

@ApiStatus.NonExtendable
public interface Option<V> {
   @Deprecated
   static Option<Boolean> booleanOption(final String id, final boolean defaultValue) {
      return OptionSchema.globalSchema().booleanOption(id, defaultValue);
   }

   @Deprecated
   static <E extends Enum<E>> Option<E> enumOption(final String id, final Class<E> enumClazz, final E defaultValue) {
      return OptionSchema.globalSchema().enumOption(id, enumClazz, defaultValue);
   }

   String id();

   @Deprecated
   default Class<V> type() {
      return this.valueType().type();
   }

   ValueType<V> valueType();

   @Nullable
   V defaultValue();
}
