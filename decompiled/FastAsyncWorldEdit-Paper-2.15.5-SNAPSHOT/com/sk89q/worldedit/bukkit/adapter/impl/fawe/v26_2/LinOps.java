package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v26_2;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractStringBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;
import org.jetbrains.annotations.Nullable;

public class LinOps implements DynamicOps<LinTag<?>> {
   static final DynamicOps<LinTag<?>> INSTANCE = new LinOps();

   private LinOps() {
   }

   public LinTag<?> empty() {
      return LinEndTag.instance();
   }

   public LinTag<?> emptyList() {
      return LinListTag.empty(LinTagType.endTag());
   }

   public LinTag<?> emptyMap() {
      return LinCompoundTag.builder().build();
   }

   public <U> U convertTo(final DynamicOps<U> outOps, final LinTag<?> input) {
      return (U)(switch (input) {
         case LinEndTag ignored -> outOps.empty();
         case LinByteTag tag -> outOps.createByte(tag.valueAsByte());
         case LinShortTag tagx -> outOps.createShort(tagx.valueAsShort());
         case LinIntTag tagxx -> outOps.createInt(tagxx.valueAsInt());
         case LinLongTag tagxxx -> outOps.createLong(tagxxx.valueAsLong());
         case LinFloatTag tagxxxx -> outOps.createFloat(tagxxxx.valueAsFloat());
         case LinDoubleTag tagxxxxx -> outOps.createDouble(tagxxxxx.valueAsDouble());
         case LinByteArrayTag tagxxxxxx -> outOps.createByteList(ByteBuffer.wrap(tagxxxxxx.value()));
         case LinStringTag tagxxxxxxx -> outOps.createString(tagxxxxxxx.value());
         case LinListTag<?> tagxxxxxxxx -> this.convertList(outOps, tagxxxxxxxx);
         case LinCompoundTag tagxxxxxxxxx -> this.convertMap(outOps, tagxxxxxxxxx);
         case LinIntArrayTag tagxxxxxxxxxx -> outOps.createIntList(Arrays.stream(tagxxxxxxxxxx.value()));
         case LinLongArrayTag tagxxxxxxxxxxx -> outOps.createLongList(Arrays.stream(tagxxxxxxxxxxx.value()));
         default -> throw new MatchException(null, null);
      });
   }

   public DataResult<Number> getNumberValue(final LinTag<?> input) {
      return input instanceof LinNumberTag<? extends Number> tag ? DataResult.success(tag.value()) : DataResult.error(() -> "Not a number");
   }

   public LinTag<?> createNumeric(final Number i) {
      return LinDoubleTag.of(i.doubleValue());
   }

   public LinTag<?> createByte(final byte value) {
      return LinByteTag.of(value);
   }

   public LinTag<?> createShort(final short value) {
      return LinShortTag.of(value);
   }

   public LinTag<?> createInt(final int value) {
      return LinIntTag.of(value);
   }

   public LinTag<?> createLong(final long value) {
      return LinLongTag.of(value);
   }

   public LinTag<?> createFloat(final float value) {
      return LinFloatTag.of(value);
   }

   public LinTag<?> createDouble(final double value) {
      return LinDoubleTag.of(value);
   }

   public DataResult<Boolean> getBooleanValue(final LinTag<?> input) {
      return this.getNumberValue(input).map(value -> value.doubleValue() != 0.0);
   }

   public LinTag<?> createBoolean(final boolean value) {
      return LinByteTag.of((byte)(value ? 1 : 0));
   }

   public DataResult<String> getStringValue(final LinTag<?> input) {
      return input instanceof LinStringTag tag ? DataResult.success(tag.value()) : DataResult.error(() -> "Not a string");
   }

   public LinTag<?> createString(final String value) {
      return LinStringTag.of(value);
   }

   public DataResult<LinTag<?>> mergeToList(final LinTag<?> list, final LinTag<?> value) {
      return createCollector(list)
         .map(collector -> DataResult.success(collector.accept(value).result()))
         .orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + list, list));
   }

   public DataResult<LinTag<?>> mergeToList(final LinTag<?> list, final List<LinTag<?>> values) {
      return createCollector(list)
         .map(collector -> DataResult.success(collector.acceptAll(values).result()))
         .orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + list, list));
   }

   public DataResult<LinTag<?>> mergeToMap(final LinTag<?> map, final LinTag<?> key, final LinTag<?> value) {
      if (!(map instanceof LinCompoundTag) && !(map instanceof LinEndTag)) {
         return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
      } else if (key instanceof LinStringTag keyStringTag) {
         if (map instanceof LinCompoundTag mapCompoundTag) {
            return value == this.empty()
               ? DataResult.success(mapCompoundTag)
               : DataResult.success(mapCompoundTag.toBuilder().put(keyStringTag.value(), value).build());
         } else {
            return value == this.empty()
               ? DataResult.success(this.emptyMap())
               : DataResult.success(LinCompoundTag.builder().put(keyStringTag.value(), value).build());
         }
      } else {
         return DataResult.error(() -> "key is not a string: " + key, map);
      }
   }

   public DataResult<LinTag<?>> mergeToMap(final LinTag<?> map, final MapLike<LinTag<?>> values) {
      if (!(map instanceof LinCompoundTag) && !(map instanceof LinEndTag)) {
         return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
      } else {
         Iterator<Pair<LinTag<?>, LinTag<?>>> iterator = values.entries().iterator();
         if (!iterator.hasNext()) {
            return map == this.empty() ? DataResult.success(this.emptyMap()) : DataResult.success(map);
         } else {
            LinCompoundTag.Builder resultBuilder = map instanceof LinCompoundTag compoundTag ? compoundTag.toBuilder() : LinCompoundTag.builder();
            List<LinTag<?>> nonStringKeys = new ArrayList<>();
            iterator.forEachRemaining(entry -> {
               LinTag<?> key = (LinTag<?>)entry.getFirst();
               if (key instanceof LinStringTag keyStringTag) {
                  if (entry.getSecond() != this.empty()) {
                     resultBuilder.put(keyStringTag.value(), (LinTag<?>)entry.getSecond());
                  }
               } else {
                  nonStringKeys.add(key);
               }
            });
            return nonStringKeys.isEmpty() ? DataResult.success(resultBuilder.build()) : DataResult.error(() -> "some keys are not strings: " + nonStringKeys);
         }
      }
   }

   public DataResult<LinTag<?>> mergeToMap(final LinTag<?> map, final Map<LinTag<?>, LinTag<?>> values) {
      if (!(map instanceof LinCompoundTag) && !(map instanceof LinEndTag)) {
         return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
      } else if (values.isEmpty()) {
         return map == this.empty() ? DataResult.success(this.emptyMap()) : DataResult.success(map);
      } else {
         LinCompoundTag.Builder resultBuilder = map instanceof LinCompoundTag compoundTag ? compoundTag.toBuilder() : LinCompoundTag.builder();
         List<LinTag<?>> nonStringKeys = new ArrayList<>();
         values.forEach((key, value) -> {
            if (key instanceof LinStringTag keyStringTag) {
               if (value != this.empty()) {
                  resultBuilder.put(keyStringTag.value(), (LinTag<?>)value);
               }
            } else {
               nonStringKeys.add((LinTag<?>)key);
            }
         });
         return nonStringKeys.isEmpty() ? DataResult.success(resultBuilder.build()) : DataResult.error(() -> "some keys are not strings: " + nonStringKeys);
      }
   }

   public DataResult<Stream<Pair<LinTag<?>, LinTag<?>>>> getMapValues(final LinTag<?> input) {
      if (input instanceof LinCompoundTag tag) {
         Map<String, LinTag<?>> value = tag.value();
         return DataResult.success(value.entrySet().stream().map(entry -> Pair.of(this.createString(entry.getKey()), entry.getValue())));
      } else {
         return DataResult.error(() -> "Not a map: " + input);
      }
   }

   public DataResult<Consumer<BiConsumer<LinTag<?>, LinTag<?>>>> getMapEntries(final LinTag<?> input) {
      return input instanceof LinCompoundTag tag
         ? DataResult.success((Consumer<BiConsumer>)consumer -> tag.value().forEach((s, linTag) -> consumer.accept(this.createString(s), linTag)))
         : DataResult.error(() -> "Not a map: " + input);
   }

   public DataResult<MapLike<LinTag<?>>> getMap(final LinTag<?> input) {
      if (input instanceof LinCompoundTag map) {
         final Map<String, LinTag<?>> value = map.value();
         return DataResult.success(new MapLike<LinTag<?>>() {
            @Nullable
            public LinTag<?> get(final LinTag<?> key) {
               if (key instanceof LinStringTag tag) {
                  return value.get(tag.value());
               } else {
                  throw new UnsupportedOperationException("Cannot get map entry with non-string key: " + key);
               }
            }

            @Nullable
            public LinTag<?> get(final String key) {
               return value.get(key);
            }

            public Stream<Pair<LinTag<?>, LinTag<?>>> entries() {
               return value.entrySet().stream().map(entry -> Pair.of(LinOps.this.createString(entry.getKey()), entry.getValue()));
            }

            @Override
            public String toString() {
               return "MapLike[" + map + "]";
            }
         });
      } else {
         return DataResult.error(() -> "Not a map: " + input);
      }
   }

   public LinTag<?> createMap(final Stream<Pair<LinTag<?>, LinTag<?>>> map) {
      LinCompoundTag.Builder builder = LinCompoundTag.builder();
      map.filter(pair -> pair.getSecond() != this.empty()).forEach(pair -> {
         if (pair.getFirst() instanceof LinStringTag key) {
            builder.put(key.value(), (LinTag<?>)pair.getSecond());
         } else {
            throw new UnsupportedOperationException("Cannot create map entry with non-string key: " + pair.getFirst());
         }
      });
      return builder.build();
   }

   public DataResult<Stream<LinTag<?>>> getStream(final LinTag<?> input) {
      return switch (input) {
         case LinListTag<?> tag -> DataResult.success(StreamSupport.stream(Spliterators.spliterator(new Iterator<LinTag<?>>() {
            private int index;

            @Override
            public boolean hasNext() {
               return this.index < tag.value().size();
            }

            public LinTag<?> next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return tag.get(this.index++);
               }
            }
         }, (long)tag.value().size(), 0), false));
         case LinByteArrayTag tagx -> DataResult.success(StreamSupport.stream(Spliterators.spliterator(new Iterator<LinByteTag>() {
            private int index;

            @Override
            public boolean hasNext() {
               return this.index < tag.value().length;
            }

            public LinByteTag next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  return LinByteTag.of(tag.value()[this.index++]);
               }
            }
         }, (long)tagx.value().length, 0), false));
         case LinIntArrayTag tagxx -> DataResult.success(StreamSupport.stream(Spliterators.spliterator(tagxx.value(), 0), false).map(LinIntTag::of));
         case LinLongArrayTag tagxxx -> DataResult.success(StreamSupport.stream(Spliterators.spliterator(tagxxx.value(), 0), false).map(LinLongTag::of));
         default -> DataResult.error(() -> "Not a list");
      };
   }

   public DataResult<Consumer<Consumer<LinTag<?>>>> getList(final LinTag<?> input) {
      if (input instanceof LinListTag<?> tag) {
         return DataResult.success((Consumer<Consumer>)consumer -> tag.value().forEach(entry -> {
            if (entry instanceof LinCompoundTag compoundTag && compoundTag.value().size() == 1 && compoundTag.value().containsKey("")) {
               consumer.accept(compoundTag.value().get(""));
            } else {
               consumer.accept(entry);
            }
         }));
      } else if (input instanceof LinByteArrayTag tag) {
         return DataResult.success((Consumer<Consumer>)consumer -> {
            for (byte b : tag.value()) {
               consumer.accept(this.createByte(b));
            }
         });
      } else if (input instanceof LinIntArrayTag tag) {
         return DataResult.success((Consumer<Consumer>)consumer -> {
            for (int i : tag.value()) {
               consumer.accept(this.createInt(i));
            }
         });
      } else {
         return input instanceof LinLongArrayTag tag ? DataResult.success((Consumer<Consumer>)consumer -> {
            for (long l : tag.value()) {
               consumer.accept(this.createLong(l));
            }
         }) : DataResult.error(() -> "Not a list: " + input);
      }
   }

   public DataResult<ByteBuffer> getByteBuffer(final LinTag<?> input) {
      return input instanceof LinByteArrayTag tag ? DataResult.success(ByteBuffer.wrap(tag.value())) : super.getByteBuffer(input);
   }

   public LinTag<?> createByteList(final ByteBuffer input) {
      ByteBuffer buffer = input.duplicate().clear();
      byte[] bytes = new byte[buffer.capacity()];
      buffer.get(0, bytes, 0, bytes.length);
      return LinByteArrayTag.of(bytes);
   }

   public DataResult<IntStream> getIntStream(final LinTag<?> input) {
      return input instanceof LinIntArrayTag tag ? DataResult.success(IntStream.of(tag.value())) : super.getIntStream(input);
   }

   public LinTag<?> createIntList(final IntStream input) {
      return LinIntArrayTag.of(input.toArray());
   }

   public DataResult<LongStream> getLongStream(final LinTag<?> input) {
      return input instanceof LinLongArrayTag tag ? DataResult.success(LongStream.of(tag.value())) : super.getLongStream(input);
   }

   public LinTag<?> createLongList(final LongStream input) {
      return LinLongArrayTag.of(input.toArray());
   }

   public LinTag<?> createList(final Stream<LinTag<?>> input) {
      return rawTagListToLinList(input.collect(Collectors.toList()));
   }

   public LinTag<?> remove(final LinTag<?> input, final String key) {
      return (LinTag<?>)(input instanceof LinCompoundTag tag ? tag.toBuilder().remove(key).build() : input);
   }

   @Override
   public String toString() {
      return "LinOps";
   }

   public RecordBuilder<LinTag<?>> mapBuilder() {
      return new LinOps.LinRecordBuilder();
   }

   private static Optional<LinOps.ListCollector> createCollector(LinTag<?> tag) {
      return switch (tag) {
         case LinEndTag ignored -> Optional.of(new LinOps.GenericListCollector());
         case LinListTag<?> listTag -> Optional.of(new LinOps.GenericListCollector(listTag));
         case LinByteArrayTag byteArrayTag -> Optional.of(new LinOps.ByteListCollector(byteArrayTag));
         case LinIntArrayTag intArrayTag -> Optional.of(new LinOps.IntListCollector(intArrayTag));
         case LinLongArrayTag longArrayTag -> Optional.of(new LinOps.LongListCollector(longArrayTag));
         default -> Optional.empty();
      };
   }

   private static LinTagId identifyTagTypeOfUncheckedList(List<LinTag<?>> list) {
      LinTagId type = LinTagId.END;

      for (LinTag<?> linTag : list) {
         if (type == LinTagId.END) {
            type = linTag.type().id();
         } else if (type != linTag.type().id()) {
            return LinTagId.COMPOUND;
         }
      }

      return type;
   }

   static LinTag<?> rawTagListToLinList(List<LinTag<?>> content) {
      LinTagId typeId = identifyTagTypeOfUncheckedList(content);
      LinListTag.Builder<LinTag<?>> builder = LinListTag.builder(LinTagType.fromId(typeId));

      for (LinTag<?> entry : content) {
         if (typeId == LinTagId.COMPOUND && !(entry instanceof LinCompoundTag)) {
            builder.add(LinCompoundTag.of(Map.of("", entry)));
         } else {
            builder.add(entry);
         }
      }

      return builder.build();
   }

   private record ByteListCollector(ByteList byteList) implements LinOps.ListCollector {
      private ByteListCollector(final LinByteArrayTag byteList) {
         this(ByteArrayList.of(byteList.value()));
      }

      @Override
      public LinOps.ListCollector accept(final LinTag<?> tag) {
         if (tag instanceof LinByteTag byteTag) {
            this.byteList.add(byteTag.valueAsByte());
            return this;
         } else {
            return new LinOps.GenericListCollector(this.byteList).accept(tag);
         }
      }

      @Override
      public LinTag<?> result() {
         return LinByteArrayTag.of(this.byteList.toByteArray());
      }
   }

   private static class GenericListCollector implements LinOps.ListCollector {
      private final List<LinTag<?>> list = new ArrayList<>();

      GenericListCollector() {
      }

      GenericListCollector(LinListTag<?> listTag) {
         this.list.addAll((Collection<? extends LinTag<?>>)listTag.value());
      }

      GenericListCollector(ByteList byteList) {
         byteList.forEach(value -> this.list.add(LinByteTag.of(value)));
      }

      GenericListCollector(IntList intList) {
         intList.forEach(value -> this.list.add(LinIntTag.of(value)));
      }

      GenericListCollector(LongList longList) {
         longList.forEach(value -> this.list.add(LinLongTag.of(value)));
      }

      @Override
      public LinOps.ListCollector accept(final LinTag<?> tag) {
         this.list.add(tag);
         return this;
      }

      @Override
      public LinTag<?> result() {
         return LinOps.rawTagListToLinList(this.list);
      }
   }

   private record IntListCollector(IntList intList) implements LinOps.ListCollector {
      private IntListCollector(final LinIntArrayTag intList) {
         this(IntArrayList.of(intList.value()));
      }

      @Override
      public LinOps.ListCollector accept(final LinTag<?> tag) {
         if (tag instanceof LinIntTag intTag) {
            this.intList.add(intTag.valueAsInt());
            return this;
         } else {
            return new LinOps.GenericListCollector(this.intList).accept(tag);
         }
      }

      @Override
      public LinTag<?> result() {
         return LinIntArrayTag.of(this.intList.toIntArray());
      }
   }

   private class LinRecordBuilder extends AbstractStringBuilder<LinTag<?>, LinCompoundTag.Builder> {
      protected LinRecordBuilder() {
         super(LinOps.this);
      }

      protected LinCompoundTag.Builder initBuilder() {
         return LinCompoundTag.builder();
      }

      protected LinCompoundTag.Builder append(final String key, final LinTag<?> value, final LinCompoundTag.Builder builder) {
         return value != this.ops().empty() ? builder.put(key, value) : builder;
      }

      protected DataResult<LinTag<?>> build(final LinCompoundTag.Builder builder, final LinTag<?> prefix) {
         if (prefix == null || prefix.type().equals(LinTagType.endTag())) {
            return DataResult.success(builder.build());
         } else if (prefix instanceof LinCompoundTag prefixCompound) {
            LinCompoundTag.Builder prefixedBuilder = prefixCompound.toBuilder();
            builder.build().value().forEach(prefixedBuilder::put);
            return DataResult.success(prefixedBuilder.build());
         } else {
            return DataResult.error(() -> "mergeToMap called with not a map: " + prefix, prefix);
         }
      }
   }

   private interface ListCollector {
      LinOps.ListCollector accept(LinTag<?> tag);

      default LinOps.ListCollector acceptAll(Iterable<LinTag<?>> iterable) {
         LinOps.ListCollector collector = this;

         for (LinTag<?> linTag : iterable) {
            collector = collector.accept(linTag);
         }

         return collector;
      }

      LinTag<?> result();
   }

   private record LongListCollector(LongList longList) implements LinOps.ListCollector {
      private LongListCollector(final LinLongArrayTag longList) {
         this(LongArrayList.of(longList.value()));
      }

      @Override
      public LinOps.ListCollector accept(final LinTag<?> tag) {
         if (tag instanceof LinLongTag longTag) {
            this.longList.add(longTag.valueAsLong());
            return this;
         } else {
            return new LinOps.GenericListCollector(this.longList).accept(tag);
         }
      }

      @Override
      public LinTag<?> result() {
         return LinLongArrayTag.of(this.longList.toLongArray());
      }
   }
}
