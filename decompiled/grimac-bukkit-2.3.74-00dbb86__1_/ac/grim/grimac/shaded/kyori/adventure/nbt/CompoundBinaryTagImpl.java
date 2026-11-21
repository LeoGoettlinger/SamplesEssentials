package ac.grim.grimac.shaded.kyori.adventure.nbt;

import ac.grim.grimac.shaded.jetbrains.annotations.Debug;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.examination.ExaminableProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Debug.Renderer(
   text = "\"CompoundBinaryTag[length=\" + this.tags.size() + \"]\"",
   childrenArray = "this.tags.entrySet().toArray()",
   hasChildren = "!this.tags.isEmpty()"
)
final class CompoundBinaryTagImpl extends AbstractBinaryTag implements CompoundBinaryTag {
   static final CompoundBinaryTag EMPTY = new CompoundBinaryTagImpl(Collections.emptyMap());
   private final Map<String, BinaryTag> tags;
   private final int hashCode;

   CompoundBinaryTagImpl(final Map<String, BinaryTag> tags) {
      this.tags = Collections.unmodifiableMap(tags);
      this.hashCode = tags.hashCode();
   }

   @Override
   public boolean contains(@NotNull final String key) {
      return this.tags.containsKey(key);
   }

   @Override
   public boolean contains(@NotNull final String key, @NotNull final BinaryTagType<?> type) {
      BinaryTag tag = this.tags.get(Objects.requireNonNull(key, "key"));
      return tag != null && Objects.requireNonNull(type, "type").test(tag.type());
   }

   @NotNull
   @Override
   public Set<String> keySet() {
      return Collections.unmodifiableSet(this.tags.keySet());
   }

   @Nullable
   @Override
   public BinaryTag get(final String key) {
      return this.tags.get(key);
   }

   @Override
   public int size() {
      return this.tags.size();
   }

   @Override
   public boolean isEmpty() {
      return this.tags.isEmpty();
   }

   @NotNull
   public CompoundBinaryTag put(@NotNull final String key, @NotNull final BinaryTag tag) {
      return this.edit(map -> map.put(key, tag));
   }

   @NotNull
   public CompoundBinaryTag put(@NotNull final CompoundBinaryTag tag) {
      return this.edit(map -> {
         for (String key : tag.keySet()) {
            map.put(key, tag.get(key));
         }
      });
   }

   @NotNull
   public CompoundBinaryTag put(@NotNull final Map<String, ? extends BinaryTag> tags) {
      return this.edit(map -> map.putAll(tags));
   }

   @NotNull
   public CompoundBinaryTag remove(@NotNull final String key, @Nullable final Consumer<? super BinaryTag> removed) {
      return (CompoundBinaryTag)(!this.tags.containsKey(key) ? this : this.edit(map -> {
         BinaryTag tag = map.remove(key);
         if (removed != null) {
            removed.accept(tag);
         }
      }));
   }

   @Override
   public byte getByte(@NotNull final String key, final byte defaultValue) {
      return this.contains(key, BinaryTagTypes.BYTE) ? ((NumberBinaryTag)this.tags.get(key)).byteValue() : defaultValue;
   }

   @Override
   public short getShort(@NotNull final String key, final short defaultValue) {
      return this.contains(key, BinaryTagTypes.SHORT) ? ((NumberBinaryTag)this.tags.get(key)).shortValue() : defaultValue;
   }

   @Override
   public int getInt(@NotNull final String key, final int defaultValue) {
      return this.contains(key, BinaryTagTypes.INT) ? ((NumberBinaryTag)this.tags.get(key)).intValue() : defaultValue;
   }

   @Override
   public long getLong(@NotNull final String key, final long defaultValue) {
      return this.contains(key, BinaryTagTypes.LONG) ? ((NumberBinaryTag)this.tags.get(key)).longValue() : defaultValue;
   }

   @Override
   public float getFloat(@NotNull final String key, final float defaultValue) {
      return this.contains(key, BinaryTagTypes.FLOAT) ? ((NumberBinaryTag)this.tags.get(key)).floatValue() : defaultValue;
   }

   @Override
   public double getDouble(@NotNull final String key, final double defaultValue) {
      return this.contains(key, BinaryTagTypes.DOUBLE) ? ((NumberBinaryTag)this.tags.get(key)).doubleValue() : defaultValue;
   }

   @NotNull
   @Override
   public byte[] getByteArray(@NotNull final String key) {
      return this.contains(key, BinaryTagTypes.BYTE_ARRAY) ? ((ByteArrayBinaryTag)this.tags.get(key)).value() : new byte[0];
   }

   @NotNull
   @Override
   public byte[] getByteArray(@NotNull final String key, @NotNull final byte[] defaultValue) {
      return this.contains(key, BinaryTagTypes.BYTE_ARRAY) ? ((ByteArrayBinaryTag)this.tags.get(key)).value() : defaultValue;
   }

   @NotNull
   @Override
   public String getString(@NotNull final String key, @NotNull final String defaultValue) {
      return this.contains(key, BinaryTagTypes.STRING) ? ((StringBinaryTag)this.tags.get(key)).value() : defaultValue;
   }

   @NotNull
   @Override
   public ListBinaryTag getList(@NotNull final String key, @NotNull final ListBinaryTag defaultValue) {
      return this.contains(key, BinaryTagTypes.LIST) ? (ListBinaryTag)this.tags.get(key) : defaultValue;
   }

   @NotNull
   @Override
   public ListBinaryTag getList(
      @NotNull final String key, @NotNull final BinaryTagType<? extends BinaryTag> expectedType, @NotNull final ListBinaryTag defaultValue
   ) {
      if (this.contains(key, BinaryTagTypes.LIST)) {
         ListBinaryTag tag = (ListBinaryTag)this.tags.get(key);
         if (expectedType.test(tag.elementType())) {
            return tag;
         }
      }

      return defaultValue;
   }

   @NotNull
   @Override
   public CompoundBinaryTag getCompound(@NotNull final String key, @NotNull final CompoundBinaryTag defaultValue) {
      return this.contains(key, BinaryTagTypes.COMPOUND) ? (CompoundBinaryTag)this.tags.get(key) : defaultValue;
   }

   @NotNull
   @Override
   public int[] getIntArray(@NotNull final String key) {
      return this.contains(key, BinaryTagTypes.INT_ARRAY) ? ((IntArrayBinaryTag)this.tags.get(key)).value() : new int[0];
   }

   @NotNull
   @Override
   public int[] getIntArray(@NotNull final String key, @NotNull final int[] defaultValue) {
      return this.contains(key, BinaryTagTypes.INT_ARRAY) ? ((IntArrayBinaryTag)this.tags.get(key)).value() : defaultValue;
   }

   @NotNull
   @Override
   public long[] getLongArray(@NotNull final String key) {
      return this.contains(key, BinaryTagTypes.LONG_ARRAY) ? ((LongArrayBinaryTag)this.tags.get(key)).value() : new long[0];
   }

   @NotNull
   @Override
   public long[] getLongArray(@NotNull final String key, @NotNull final long[] defaultValue) {
      return this.contains(key, BinaryTagTypes.LONG_ARRAY) ? ((LongArrayBinaryTag)this.tags.get(key)).value() : defaultValue;
   }

   @Override
   public Stream<Entry<String, ? extends BinaryTag>> stream() {
      return this.tags.entrySet().stream();
   }

   private CompoundBinaryTag edit(final Consumer<Map<String, BinaryTag>> consumer) {
      Map<String, BinaryTag> tags = new HashMap<>(this.tags);
      consumer.accept(tags);
      return new CompoundBinaryTagImpl(new HashMap<>(tags));
   }

   @Override
   public boolean equals(final Object that) {
      return this == that || that instanceof CompoundBinaryTagImpl && this.tags.equals(((CompoundBinaryTagImpl)that).tags);
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   @NotNull
   @Override
   public Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("tags", this.tags));
   }

   @NotNull
   @Override
   public Iterator<Entry<String, ? extends BinaryTag>> iterator() {
      return this.tags.entrySet().iterator();
   }

   @Override
   public void forEach(@NotNull final Consumer<? super Entry<String, ? extends BinaryTag>> action) {
      this.tags.entrySet().forEach(Objects.requireNonNull(action, "action"));
   }
}
