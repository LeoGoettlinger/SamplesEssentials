package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_11;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.DataResult.Success;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ProblemReporter.FieldPathElement;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.TypedInputList;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class LinValueInput implements ValueInput {
   private final LinValueInput.ValueInputContext context;
   private final ProblemReporter problemReporter;
   private final LinCompoundTag input;

   private LinValueInput(LinValueInput.ValueInputContext context, ProblemReporter problemReporter, LinCompoundTag input) {
      this.context = context;
      this.problemReporter = problemReporter;
      this.input = input;
   }

   public static LinValueInput create(ProblemReporter problemReporter, Provider registryAccess, LinCompoundTag input) {
      return new LinValueInput(new LinValueInput.ValueInputContext(registryAccess), problemReporter, input);
   }

   @NonNull
   public <T> Optional<T> read(@NonNull final String key, @NonNull final Codec<T> codec) {
      LinTag<?> tagOrNull = this.input.value().get(key);
      return Optional.ofNullable(tagOrNull)
         .map(tag -> codec.parse(this.context.ops(), tag))
         .flatMap(
            result -> result.resultOrPartial(
               error -> this.problemReporter.report(() -> "Failed to decode value '" + tagOrNull + "' from field '" + key + "': " + error)
            )
         );
   }

   @NonNull
   public <T> Optional<T> read(@NonNull final MapCodec<T> mapCodec) {
      return this.context
         .ops()
         .getMap(this.input)
         .flatMap(mapLike -> mapCodec.decode(this.context.ops(), mapLike))
         .resultOrPartial(error -> this.problemReporter.report(() -> "Failed to decode from map: " + error));
   }

   @NonNull
   public Optional<ValueInput> child(@NonNull final String key) {
      return Optional.ofNullable(this.input.findTag(key, LinTagType.compoundTag()))
         .map(
            tag -> (ValueInput)(tag.value().isEmpty()
               ? this.context.empty()
               : new LinValueInput(this.context, this.problemReporter.forChild(new FieldPathElement(key)), tag))
         );
   }

   @NonNull
   public ValueInput childOrEmpty(@NonNull final String key) {
      return this.child(key).orElse(this.context.empty());
   }

   @NonNull
   public Optional<ValueInputList> childrenList(@NonNull final String key) {
      return Optional.ofNullable(this.input.value().get(key))
         .filter(linTag -> linTag instanceof LinListTag)
         .map(tag -> (LinListTag)tag)
         .map(
            listTag -> (ValueInputList)(listTag.value().isEmpty()
               ? this.context.emptyChildList()
               : new LinValueInput.ListWrapper(this.problemReporter, key, (LinListTag<LinTag<?>>)listTag, this.context))
         );
   }

   @NonNull
   public ValueInputList childrenListOrEmpty(@NonNull final String key) {
      return this.childrenList(key).orElse(this.context.emptyChildList());
   }

   @NonNull
   public <T> Optional<TypedInputList<T>> list(@NonNull final String key, @NonNull final Codec<T> codec) {
      return Optional.ofNullable(this.input.value().get(key))
         .filter(linTag -> linTag instanceof LinListTag)
         .map(tag -> (LinListTag)tag)
         .map(
            listTag -> (TypedInputList<T>)(listTag.value().isEmpty()
               ? this.context.emptySafelyTypedList()
               : new LinValueInput.TypedListWrapper<>(this.problemReporter, key, (LinListTag<LinTag<?>>)listTag, this.context, codec))
         );
   }

   @NonNull
   public <T> TypedInputList<T> listOrEmpty(@NonNull final String key, @NonNull final Codec<T> codec) {
      return this.<T>list(key, codec).orElse(this.context.emptySafelyTypedList());
   }

   public boolean getBooleanOr(@NonNull final String key, final boolean defaultValue) {
      return this.input.value().get(key) instanceof LinNumberTag<? extends Number> numberTag ? numberTag.value().byteValue() != 0 : defaultValue;
   }

   public byte getByteOr(@NonNull final String key, final byte defaultValue) {
      return this.input.value().get(key) instanceof LinByteTag byteTag ? byteTag.valueAsByte() : defaultValue;
   }

   public int getShortOr(@NonNull final String key, final short defaultValue) {
      return this.input.value().get(key) instanceof LinShortTag shortTag ? shortTag.valueAsShort() : defaultValue;
   }

   @NonNull
   public Optional<Integer> getInt(@NonNull final String key) {
      return this.input.value().get(key) instanceof LinNumberTag<? extends Number> numberTag ? Optional.of(numberTag.value().intValue()) : Optional.empty();
   }

   public int getIntOr(@NonNull final String key, final int defaultValue) {
      return this.input.value().get(key) instanceof LinNumberTag<? extends Number> numberTag ? numberTag.value().intValue() : defaultValue;
   }

   public long getLongOr(@NonNull final String key, final long defaultValue) {
      return this.input.value().get(key) instanceof LinNumberTag<? extends Number> numberTag ? numberTag.value().longValue() : defaultValue;
   }

   @NonNull
   public Optional<Long> getLong(@NonNull final String key) {
      return this.input.value().get(key) instanceof LinNumberTag<? extends Number> numberTag ? Optional.of(numberTag.value().longValue()) : Optional.empty();
   }

   public float getFloatOr(@NonNull final String key, final float defaultValue) {
      return this.input.value().get(key) instanceof LinNumberTag<? extends Number> numberTag ? numberTag.value().floatValue() : defaultValue;
   }

   public double getDoubleOr(@NonNull final String key, final double defaultValue) {
      return this.input.value().get(key) instanceof LinNumberTag<? extends Number> numberTag ? numberTag.value().doubleValue() : defaultValue;
   }

   @NonNull
   public Optional<String> getString(@NonNull final String key) {
      return this.input.value().get(key) instanceof LinStringTag stringTag ? Optional.of(stringTag.value()) : Optional.empty();
   }

   @NonNull
   public String getStringOr(@NonNull final String key, @NonNull final String defaultValue) {
      return this.input.value().get(key) instanceof LinStringTag stringTag ? stringTag.value() : defaultValue;
   }

   @NonNull
   public Optional<int[]> getIntArray(@NonNull final String key) {
      return this.input.value().get(key) instanceof LinIntArrayTag intArrayTag ? Optional.of(intArrayTag.value()) : Optional.empty();
   }

   @NonNull
   public Provider lookup() {
      return this.context.lookup();
   }

   private static final class EmptyTypedInputList implements TypedInputList<Object> {
      public boolean isEmpty() {
         return true;
      }

      @NonNull
      public Stream<Object> stream() {
         return Stream.empty();
      }

      @NonNull
      public Iterator<Object> iterator() {
         return Collections.emptyIterator();
      }
   }

   private record EmptyValueInput(Provider lookup, ValueInputList emptyChildList, TypedInputList<Object> emptyTypedList) implements ValueInput {
      @NonNull
      public <T> Optional<T> read(@NonNull final String key, @NonNull final Codec<T> codec) {
         return Optional.empty();
      }

      @NonNull
      public <T> Optional<T> read(@NonNull final MapCodec<T> mapCodec) {
         return Optional.empty();
      }

      @NonNull
      public Optional<ValueInput> child(@NonNull final String s) {
         return Optional.empty();
      }

      @NonNull
      public ValueInput childOrEmpty(@NonNull final String s) {
         return this;
      }

      @NonNull
      public Optional<ValueInputList> childrenList(@NonNull final String s) {
         return Optional.empty();
      }

      @NonNull
      public ValueInputList childrenListOrEmpty(@NonNull final String s) {
         return this.emptyChildList;
      }

      @NonNull
      public <T> Optional<TypedInputList<T>> list(@NonNull final String s, @NonNull final Codec<T> codec) {
         return Optional.empty();
      }

      @NonNull
      public <T> TypedInputList<T> listOrEmpty(@NonNull final String s, @NonNull final Codec<T> codec) {
         return (TypedInputList<T>)this.emptyTypedList;
      }

      public boolean getBooleanOr(@NonNull final String s, final boolean defaultValue) {
         return defaultValue;
      }

      public byte getByteOr(@NonNull final String s, final byte defaultValue) {
         return defaultValue;
      }

      public int getShortOr(@NonNull final String s, final short defaultValue) {
         return defaultValue;
      }

      @NonNull
      public Optional<Integer> getInt(@NonNull final String s) {
         return Optional.empty();
      }

      public int getIntOr(@NonNull final String s, final int defaultValue) {
         return defaultValue;
      }

      public long getLongOr(@NonNull final String s, final long defaultValue) {
         return defaultValue;
      }

      @NonNull
      public Optional<Long> getLong(@NonNull final String s) {
         return Optional.empty();
      }

      public float getFloatOr(@NonNull final String s, final float defaultValue) {
         return defaultValue;
      }

      public double getDoubleOr(@NonNull final String s, final double defaultValue) {
         return defaultValue;
      }

      @NonNull
      public Optional<String> getString(@NonNull final String s) {
         return Optional.empty();
      }

      @NonNull
      public String getStringOr(@NonNull final String s, @NonNull final String defaultValue) {
         return defaultValue;
      }

      @NonNull
      public Optional<int[]> getIntArray(@NonNull final String s) {
         return Optional.empty();
      }
   }

   private static final class EmptyValueInputList implements ValueInputList {
      public boolean isEmpty() {
         return true;
      }

      @NonNull
      public Stream<ValueInput> stream() {
         return Stream.empty();
      }

      @NonNull
      public Iterator<ValueInput> iterator() {
         return Collections.emptyIterator();
      }
   }

   private record ListWrapper(ProblemReporter problemReporter, String name, LinListTag<LinTag<?>> list, LinValueInput.ValueInputContext context)
      implements ValueInputList {
      public boolean isEmpty() {
         return this.list.value().isEmpty();
      }

      @NonNull
      public Stream<ValueInput> stream() {
         return Streams.mapWithIndex(
               this.list.value().stream(),
               (tag, index) -> {
                  if (tag instanceof LinCompoundTag compoundTag) {
                     return (ValueInput)(compoundTag.value().isEmpty()
                        ? this.context.empty()
                        : new LinValueInput(this.context, this.problemReporter.forChild(new IndexedFieldPathElement(this.name, (int)index)), compoundTag));
                  } else {
                     this.problemReporter
                        .report(
                           () -> "Expected list '"
                              + this.name
                              + "' to contain at index "
                              + index
                              + " value of type "
                              + LinTagType.compoundTag()
                              + ", but got "
                              + tag.type()
                        );
                     return null;
                  }
               }
            )
            .filter(Objects::nonNull);
      }

      @NonNull
      public Iterator<ValueInput> iterator() {
         final Iterator<LinTag<?>> iterator = this.list.value().iterator();
         return new AbstractIterator<ValueInput>() {
            private int index;

            @Nullable
            protected ValueInput computeNext() {
               while (iterator.hasNext()) {
                  LinTag<?> tag = iterator.next();
                  int i = this.index++;
                  if (tag instanceof LinCompoundTag compoundTag) {
                     return (ValueInput)(compoundTag.value().isEmpty()
                        ? ListWrapper.this.context.empty()
                        : new LinValueInput(
                           ListWrapper.this.context,
                           ListWrapper.this.problemReporter.forChild(new IndexedFieldPathElement(ListWrapper.this.name, i)),
                           compoundTag
                        ));
                  }

                  ListWrapper.this.problemReporter
                     .report(
                        () -> "Expected list '"
                           + ListWrapper.this.name
                           + "' to contain at index "
                           + this.index
                           + " value of type "
                           + LinTagType.compoundTag()
                           + ", but got "
                           + tag.type()
                     );
               }

               return (ValueInput)this.endOfData();
            }
         };
      }
   }

   private record TypedListWrapper<T>(
      ProblemReporter problemReporter, String name, LinListTag<LinTag<?>> list, LinValueInput.ValueInputContext context, Codec<T> codec
   ) implements TypedInputList<T> {
      public boolean isEmpty() {
         return this.list.value().isEmpty();
      }

      @NonNull
      public Stream<T> stream() {
         return Streams.mapWithIndex(
               this.list.value().stream(),
               (tag, index) -> this.codec
                  .parse(this.context.ops(), tag)
                  .resultOrPartial(
                     error -> this.problemReporter
                        .report(() -> "Failed to decode value '" + tag + "' from field '" + this.name + "' at index " + index + ": " + error)
                  )
                  .orElse(null)
            )
            .filter(Objects::nonNull);
      }

      @NonNull
      public Iterator<T> iterator() {
         final ListIterator<LinTag<?>> iterator = this.list.value().listIterator();
         return new AbstractIterator<T>() {
            @Nullable
            protected T computeNext() {
               while (iterator.hasNext()) {
                  int index = iterator.nextIndex();
                  LinTag<?> tag = iterator.next();
                  switch (TypedListWrapper.this.codec.parse(TypedListWrapper.this.context.ops(), tag)) {
                     case Success<T> success:
                        return (T)success.value();
                     case Error<T> error:
                        TypedListWrapper.this.problemReporter
                           .report(
                              () -> "Failed to decode value '" + tag + "' from field '" + TypedListWrapper.this.name + "' at index " + index + ": " + error
                           );
                        if (error.partialValue().isEmpty()) {
                           break;
                        }

                        return (T)error.partialValue().get();
                     default:
                        throw new MatchException(null, null);
                  }
               }

               return (T)this.endOfData();
            }
         };
      }
   }

   private record ValueInputContext(
      Provider lookup, DynamicOps<LinTag<?>> ops, ValueInput empty, ValueInputList emptyChildList, TypedInputList<?> emptyTypedList
   ) {
      ValueInputContext(Provider lookup) {
         this(
            lookup,
            lookup.createSerializationContext(LinOps.INSTANCE),
            new LinValueInput.EmptyValueInput(lookup, new LinValueInput.EmptyValueInputList(), new LinValueInput.EmptyTypedInputList()),
            new LinValueInput.EmptyValueInputList(),
            new LinValueInput.EmptyTypedInputList()
         );
      }

      public <T> TypedInputList<T> emptySafelyTypedList() {
         return (TypedInputList<T>)this.emptyTypedList;
      }
   }
}
