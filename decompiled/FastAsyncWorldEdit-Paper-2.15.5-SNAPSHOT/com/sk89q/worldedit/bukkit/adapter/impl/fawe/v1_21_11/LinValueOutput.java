package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_11;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.DataResult.Success;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ProblemReporter.FieldPathElement;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.TypedOutputList;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class LinValueOutput implements ValueOutput {
   private final ProblemReporter problemReporter;
   private final DynamicOps<LinTag<?>> ops;
   private final Map<String, LinValueOutput.PendingEntry> collector;

   LinValueOutput(final ProblemReporter reporter, final DynamicOps<LinTag<?>> ops) {
      this.problemReporter = reporter;
      this.ops = ops;
      this.collector = new LinkedHashMap<>();
   }

   public static LinValueOutput createWithContext(ProblemReporter reporter, Provider lookup) {
      return new LinValueOutput(reporter, lookup.createSerializationContext(LinOps.INSTANCE));
   }

   public <T> void store(@NotNull final String key, @NotNull final Codec<T> codec, @NotNull final T value) {
      DataResult<LinTag<?>> result = codec.encodeStart(this.ops, value);
      switch (result) {
         case Success<LinTag<?>> success:
            this.collector.put(key, LinValueOutput.PendingEntry.ofTag((LinTag<?>)success.value()));
            break;
         case Error<LinTag<?>> error:
            this.problemReporter.report(() -> "Failed to encode value '" + value + "' to field '" + key + "': " + error.message());
            error.partialValue().ifPresent(tag -> this.collector.put(key, LinValueOutput.PendingEntry.ofTag((LinTag<?>)tag)));
            break;
         default:
            throw new MatchException(null, null);
      }
   }

   public <T> void storeNullable(@NotNull final String key, @NotNull final Codec<T> codec, @Nullable final T value) {
      if (value != null) {
         this.store(key, codec, value);
      }
   }

   public <T> void store(@NotNull final MapCodec<T> mapCodec, @NotNull final T value) {
      DataResult<LinTag<?>> result = mapCodec.encoder().encodeStart(this.ops, value);
      switch (result) {
         case Success<LinTag<?>> success:
            if (success.value() instanceof LinCompoundTag compoundTag) {
               this.merge(compoundTag);
            }
            break;
         case Error<LinTag<?>> error:
            this.problemReporter.report(() -> "Failed to merge value '" + value + "' to an object: " + error.message());
            error.partialValue().filter(tag -> tag instanceof LinCompoundTag).ifPresent(tag -> this.merge((LinCompoundTag)tag));
            break;
         default:
            throw new MatchException(null, null);
      }
   }

   @VisibleForTesting
   void merge(LinCompoundTag other) {
      other.value().forEach((key, tag) -> {
         if (tag instanceof LinCompoundTag compoundTag && this.collector.containsKey(key)) {
            LinValueOutput.PendingEntry entry = this.collector.get(key);
            if (entry instanceof LinValueOutput.PendingTagEntry(LinCompoundTag storedCompound)) {
               this.collector.put(key, LinValueOutput.PendingEntry.ofTag(merge(compoundTag, storedCompound)));
               return;
            }

            if (entry instanceof LinValueOutput.PendingLinValueOutputEntry(LinValueOutput patt3$temp)) {
               this.collector.put(key, LinValueOutput.PendingEntry.ofTag(merge(compoundTag, patt3$temp.buildResult())));
               return;
            }
         }

         this.collector.put(key, LinValueOutput.PendingEntry.ofTag((LinTag<?>)tag));
      });
   }

   private static LinCompoundTag merge(LinCompoundTag source, LinCompoundTag target) {
      LinCompoundTag.Builder builder = target.toBuilder();
      source.value().forEach((key, tag) -> {
         if (target.value().get(key) instanceof LinCompoundTag targetCompoundTag && tag instanceof LinCompoundTag sourceCompoundTag) {
            builder.put(key, merge(sourceCompoundTag, targetCompoundTag));
         } else {
            builder.put(key, (LinTag<?>)tag);
         }
      });
      return builder.build();
   }

   public void putBoolean(@NotNull final String key, final boolean value) {
      this.collector.put(key, LinValueOutput.PendingEntry.ofTag(LinByteTag.of((byte)(value ? 1 : 0))));
   }

   public void putByte(@NotNull final String key, final byte value) {
      this.collector.put(key, LinValueOutput.PendingEntry.ofTag(LinByteTag.of(value)));
   }

   public void putShort(@NotNull final String key, final short value) {
      this.collector.put(key, LinValueOutput.PendingEntry.ofTag(LinShortTag.of(value)));
   }

   public void putInt(@NotNull final String key, final int value) {
      this.collector.put(key, LinValueOutput.PendingEntry.ofTag(LinIntTag.of(value)));
   }

   public void putLong(@NotNull final String key, final long value) {
      this.collector.put(key, LinValueOutput.PendingEntry.ofTag(LinLongTag.of(value)));
   }

   public void putFloat(@NotNull final String key, final float value) {
      this.collector.put(key, LinValueOutput.PendingEntry.ofTag(LinFloatTag.of(value)));
   }

   public void putDouble(@NotNull final String key, final double value) {
      this.collector.put(key, LinValueOutput.PendingEntry.ofTag(LinDoubleTag.of(value)));
   }

   public void putString(@NotNull final String key, @NotNull final String value) {
      this.collector.put(key, LinValueOutput.PendingEntry.ofTag(LinStringTag.of(value)));
   }

   public void putIntArray(@NotNull final String key, @NotNull final int[] value) {
      this.collector.put(key, LinValueOutput.PendingEntry.ofTag(LinIntArrayTag.of(value)));
   }

   @NotNull
   public ValueOutput child(@NotNull final String key) {
      LinValueOutput output = new LinValueOutput(this.problemReporter.forChild(new FieldPathElement(key)), this.ops);
      this.collector.put(key, LinValueOutput.PendingEntry.ofLinValueOutputEntry(output));
      return output;
   }

   @NotNull
   public ValueOutputList childrenList(@NotNull final String key) {
      List<LinValueOutput.PendingEntry> list = new LinkedList<>();
      this.collector.put(key, LinValueOutput.PendingEntry.ofList(list));
      return new LinValueOutput.ListWrapper(key, this.problemReporter, this.ops, list);
   }

   @NotNull
   public <T> TypedOutputList<T> list(@NotNull final String key, @NotNull final Codec<T> codec) {
      List<LinValueOutput.PendingEntry> list = new LinkedList<>();
      this.collector.put(key, LinValueOutput.PendingEntry.ofList(list));
      return new LinValueOutput.TypedListWrapper<>(this.problemReporter, key, this.ops, codec, list);
   }

   public void discard(@NotNull final String key) {
      this.collector.remove(key);
   }

   public boolean isEmpty() {
      return this.collector.isEmpty();
   }

   public LinCompoundTag buildResult() {
      return this.isEmpty()
         ? LinCompoundTag.builder().build()
         : LinCompoundTag.of(this.collector.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> unwrapPendingEntry(entry.getValue()))));
   }

   public LinCompoundTag.Builder toBuilder() {
      LinCompoundTag.Builder builder = LinCompoundTag.builder();
      this.collector.forEach((key, value) -> builder.put(key, unwrapPendingEntry(value)));
      return builder;
   }

   private static LinTag<?> unwrapPendingEntry(final LinValueOutput.PendingEntry entry) {
      return (LinTag<?>)(switch (entry) {
         case LinValueOutput.PendingTagEntry e -> e.tag();
         case LinValueOutput.PendingLinValueOutputEntry ex -> ex.valueOutput().buildResult();
         case LinValueOutput.PendingListEntry exx -> LinOps.rawTagListToLinList(
            exx.entries().stream().map(LinValueOutput::unwrapPendingEntry).collect(Collectors.toList())
         );
         default -> throw new MatchException(null, null);
      });
   }

   private record ListWrapper(String fieldName, ProblemReporter problemReporter, DynamicOps<LinTag<?>> ops, List<LinValueOutput.PendingEntry> list)
      implements ValueOutputList {
      @NotNull
      public ValueOutput addChild() {
         LinValueOutput valueOutput = new LinValueOutput(
            this.problemReporter.forChild(new IndexedFieldPathElement(this.fieldName, this.list.size() - 1)), this.ops
         );
         this.list.add(LinValueOutput.PendingEntry.ofLinValueOutputEntry(valueOutput));
         return valueOutput;
      }

      public void discardLast() {
         this.list.removeLast();
      }

      public boolean isEmpty() {
         return this.list.isEmpty();
      }
   }

   private sealed interface PendingEntry permits LinValueOutput.PendingTagEntry, LinValueOutput.PendingListEntry, LinValueOutput.PendingLinValueOutputEntry {
      static LinValueOutput.PendingEntry ofTag(LinTag<?> tag) {
         return new LinValueOutput.PendingTagEntry(tag);
      }

      static LinValueOutput.PendingEntry ofList(List<LinValueOutput.PendingEntry> list) {
         return new LinValueOutput.PendingListEntry(list);
      }

      static LinValueOutput.PendingEntry ofLinValueOutputEntry(LinValueOutput valueOutput) {
         return new LinValueOutput.PendingLinValueOutputEntry(valueOutput);
      }
   }

   private record PendingLinValueOutputEntry(LinValueOutput valueOutput) implements LinValueOutput.PendingEntry {
   }

   private record PendingListEntry(List<LinValueOutput.PendingEntry> entries) implements LinValueOutput.PendingEntry {
   }

   private record PendingTagEntry(LinTag<?> tag) implements LinValueOutput.PendingEntry {
   }

   private record TypedListWrapper<T>(
      ProblemReporter problemReporter, String name, DynamicOps<LinTag<?>> ops, Codec<T> codec, List<LinValueOutput.PendingEntry> list
   ) implements TypedOutputList<T> {
      public void add(final T value) {
         DataResult<LinTag<?>> dataResult = this.codec.encodeStart(this.ops, value);
         switch (dataResult) {
            case Success<LinTag<?>> success:
               this.list.add(LinValueOutput.PendingEntry.ofTag((LinTag<?>)success.value()));
               break;
            case Error<LinTag<?>> error:
               this.problemReporter.report(() -> "Failed to append value '" + value + "' to list '" + this.name + "':" + error.message());
               error.partialValue().map(LinValueOutput.PendingEntry::ofTag).ifPresent(this.list::add);
               break;
            default:
               throw new MatchException(null, null);
         }
      }

      public boolean isEmpty() {
         return this.list.isEmpty();
      }
   }
}
