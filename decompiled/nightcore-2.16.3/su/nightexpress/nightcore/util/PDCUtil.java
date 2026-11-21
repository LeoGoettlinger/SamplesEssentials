package su.nightexpress.nightcore.util;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PDCUtil {
   public static final PersistentDataType<byte[], UUID> UUID = new PDCUtil.UUIDDataType();

   @NonNull
   public static <Z> Optional<Z> get(@NonNull ItemStack holder, @NonNull PersistentDataType<?, Z> type, @NonNull NamespacedKey key) {
      ItemMeta meta = holder.getItemMeta();
      return meta == null ? Optional.empty() : get(meta, type, key);
   }

   @NonNull
   public static <Z> Optional<Z> get(@NonNull PersistentDataHolder holder, @NonNull PersistentDataType<?, Z> type, @NonNull NamespacedKey key) {
      PersistentDataContainer container = holder.getPersistentDataContainer();
      return container.has(key, type) ? Optional.ofNullable((Z)container.get(key, type)) : Optional.empty();
   }

   public static void set(@NonNull ItemStack holder, @NonNull NamespacedKey key, boolean value) {
      set(holder, PersistentDataType.INTEGER, key, value ? 1 : 0);
   }

   public static void set(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key, boolean value) {
      set(holder, PersistentDataType.INTEGER, key, value ? 1 : 0);
   }

   public static void set(@NonNull ItemStack holder, @NonNull NamespacedKey key, double value) {
      set(holder, PersistentDataType.DOUBLE, key, value);
   }

   public static void set(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key, double value) {
      set(holder, PersistentDataType.DOUBLE, key, value);
   }

   public static void set(@NonNull ItemStack holder, @NonNull NamespacedKey key, int value) {
      set(holder, PersistentDataType.INTEGER, key, value);
   }

   public static void set(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key, int value) {
      set(holder, PersistentDataType.INTEGER, key, value);
   }

   public static void set(@NonNull ItemStack holder, @NonNull NamespacedKey key, long value) {
      set(holder, PersistentDataType.LONG, key, value);
   }

   public static void set(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key, long value) {
      set(holder, PersistentDataType.LONG, key, value);
   }

   public static void set(@NonNull ItemStack holder, @NonNull NamespacedKey key, @Nullable String value) {
      set(holder, PersistentDataType.STRING, key, value);
   }

   public static void set(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key, @Nullable String value) {
      set(holder, PersistentDataType.STRING, key, value);
   }

   public static void set(@NonNull ItemStack holder, @NonNull NamespacedKey key, @Nullable UUID value) {
      set(holder, UUID, key, value);
   }

   public static void set(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key, @Nullable UUID value) {
      set(holder, UUID, key, value);
   }

   public static <T, Z> void set(@NonNull ItemStack item, @NonNull PersistentDataType<T, Z> dataType, @NonNull NamespacedKey key, @Nullable Z value) {
      ItemUtil.editMeta(item, meta -> set(meta, dataType, key, value));
   }

   public static <T, Z> void set(
      @NonNull PersistentDataHolder holder, @NonNull PersistentDataType<T, Z> dataType, @NonNull NamespacedKey key, @Nullable Z value
   ) {
      if (value == null) {
         remove(holder, key);
      } else {
         PersistentDataContainer container = holder.getPersistentDataContainer();
         container.set(key, dataType, value);
      }
   }

   public static void remove(@NonNull ItemStack holder, @NonNull NamespacedKey key) {
      ItemUtil.editMeta(holder, meta -> remove(meta, key));
   }

   public static void remove(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key) {
      PersistentDataContainer container = holder.getPersistentDataContainer();
      container.remove(key);
   }

   @NonNull
   public static Optional<String> getString(@NonNull ItemStack holder, @NonNull NamespacedKey key) {
      return get(holder, PersistentDataType.STRING, key);
   }

   @NonNull
   public static Optional<String> getString(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key) {
      return get(holder, PersistentDataType.STRING, key);
   }

   @NonNull
   public static Optional<Integer> getInt(@NonNull ItemStack holder, @NonNull NamespacedKey key) {
      return get(holder, PersistentDataType.INTEGER, key);
   }

   @NonNull
   public static Optional<Integer> getInt(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key) {
      return get(holder, PersistentDataType.INTEGER, key);
   }

   @NonNull
   public static Optional<Long> getLong(@NonNull ItemStack holder, @NonNull NamespacedKey key) {
      return get(holder, PersistentDataType.LONG, key);
   }

   @NonNull
   public static Optional<Long> getLong(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key) {
      return get(holder, PersistentDataType.LONG, key);
   }

   @NonNull
   public static Optional<Double> getDouble(@NonNull ItemStack holder, @NonNull NamespacedKey key) {
      return get(holder, PersistentDataType.DOUBLE, key);
   }

   @NonNull
   public static Optional<Double> getDouble(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key) {
      return get(holder, PersistentDataType.DOUBLE, key);
   }

   @NonNull
   public static Optional<Boolean> getBoolean(@NonNull ItemStack holder, @NonNull NamespacedKey key) {
      return get(holder, PersistentDataType.INTEGER, key).map(i -> i != 0);
   }

   @NonNull
   public static Optional<Boolean> getBoolean(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key) {
      return get(holder, PersistentDataType.INTEGER, key).map(i -> i != 0);
   }

   @NonNull
   public static Optional<UUID> getUUID(@NonNull ItemStack holder, @NonNull NamespacedKey key) {
      return get(holder, UUID, key);
   }

   @NonNull
   public static Optional<UUID> getUUID(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key) {
      return get(holder, UUID, key);
   }

   public static class UUIDDataType implements PersistentDataType<byte[], UUID> {
      @NonNull
      public Class<byte[]> getPrimitiveType() {
         return byte[].class;
      }

      @NonNull
      public Class<UUID> getComplexType() {
         return UUID.class;
      }

      @NonNull
      public byte[] toPrimitive(UUID complex, @NonNull PersistentDataAdapterContext context) {
         ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
         bb.putLong(complex.getMostSignificantBits());
         bb.putLong(complex.getLeastSignificantBits());
         return bb.array();
      }

      @NonNull
      public UUID fromPrimitive(@NonNull byte[] primitive, @NonNull PersistentDataAdapterContext context) {
         ByteBuffer bb = ByteBuffer.wrap(primitive);
         long firstLong = bb.getLong();
         long secondLong = bb.getLong();
         return new UUID(firstLong, secondLong);
      }
   }
}
