package ac.grim.grimac.internal.storage.codec;

import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.codec.MergeMode;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

@ApiStatus.Internal
public class MethodHandleCodec<R> implements Codec<R> {
   @NotNull
   private final Class<R> recordType;
   @NotNull
   private final EncodeShape shape;
   private final int version;
   @NotNull
   protected final String[] fieldNames;
   @NotNull
   protected final TypeTag[] typeTags;
   @NotNull
   protected final Object[] accessors;
   protected final boolean[] nullable;
   @NotNull
   protected final Class<?>[] javaTypes;
   @NotNull
   protected final Enum<?>[][] enumConstants;
   @NotNull
   private final Map<String, Integer> fieldIndex;
   private final int[] recordPositions;
   private final int totalComponents;
   @NotNull
   protected final MethodHandle constructor;

   public MethodHandleCodec(@NotNull Class<R> recordType, @NotNull EncodeShape shape) {
      this.recordType = recordType;
      this.shape = shape;
      RecordLayout layout = CapturedBindings.layout(recordType);
      if (layout == null) {
         layout = RecordLayout.fromReflection(recordType, shape);
      }

      this.version = layout.version();
      List<EncodeShape.FieldDef> defs = shape.fields();
      int n = defs.size();
      this.fieldNames = new String[n];
      this.typeTags = new TypeTag[n];
      this.accessors = new Object[n];
      this.nullable = new boolean[n];
      this.javaTypes = new Class[n];
      this.enumConstants = new Enum[n][];
      this.recordPositions = new int[n];
      this.fieldIndex = new HashMap<>(n * 2);
      this.totalComponents = shape.totalComponentCount();
      String[] accessorNames = layout.accessorNames();

      for (int i = 0; i < n; i++) {
         EncodeShape.FieldDef def = defs.get(i);
         this.fieldNames[i] = def.name();
         this.typeTags[i] = TypeTag.of(def.javaType());
         this.accessors[i] = Accessors.build(recordType, accessorNames[i], def.javaType());
         this.nullable[i] = def.nullable();
         this.javaTypes[i] = def.javaType();
         this.recordPositions[i] = def.recordIndex();
         if (this.typeTags[i] == TypeTag.ENUM) {
            Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>)def.javaType();
            this.enumConstants[i] = (Enum<?>[])enumType.getEnumConstants();
         }

         this.fieldIndex.put(def.name(), i);
      }

      try {
         this.constructor = canonicalConstructorHandle(recordType, layout.ctorParamTypes());
      } catch (Throwable var10) {
         throw new IllegalStateException("could not resolve canonical constructor for " + recordType.getName(), var10);
      }
   }

   public int recordPositionAt(int i) {
      return this.recordPositions[i];
   }

   public int totalComponentCount() {
      return this.totalComponents;
   }

   @NotNull
   @Override
   public Class<R> recordType() {
      return this.recordType;
   }

   @NotNull
   @Override
   public EncodeShape shape() {
      return this.shape;
   }

   @Override
   public int version() {
      return this.version;
   }

   @Nullable
   @Override
   public Object indexField(@NotNull R record, @NotNull String fieldName) {
      Integer idx = this.fieldIndex.get(fieldName);
      if (idx == null) {
         throw new IllegalArgumentException("no such field on " + this.recordType.getName() + ": " + fieldName);
      } else {
         return this.readField(record, idx);
      }
   }

   @Nullable
   protected Object readField(@NotNull R record, int i) {
      Object acc = this.accessors[i];

      return switch (this.typeTags[i]) {
         case INT -> ((ToIntFunction)acc).applyAsInt(record);
         case LONG -> ((ToLongFunction)acc).applyAsLong(record);
         case DOUBLE, FLOAT -> ((ToDoubleFunction)acc).applyAsDouble(record);
         case ENUM, BOOLEAN, STRING, BYTES, UUID, FLOAT_ARRAY, NESTED_SEALED -> ((Function)acc).apply(record);
      };
   }

   protected int readInt(@NotNull R record, int i) {
      return ((ToIntFunction)this.accessors[i]).applyAsInt(record);
   }

   protected long readLong(@NotNull R record, int i) {
      return ((ToLongFunction)this.accessors[i]).applyAsLong(record);
   }

   protected double readDouble(@NotNull R record, int i) {
      return ((ToDoubleFunction)this.accessors[i]).applyAsDouble(record);
   }

   @Nullable
   protected Object readRef(@NotNull R record, int i) {
      return ((Function)this.accessors[i]).apply(record);
   }

   protected int readEnumOrdinal(@NotNull R record, int i) {
      Enum<?> e = (Enum<?>)((Function)this.accessors[i]).apply(record);
      if (e == null) {
         throw new IllegalStateException("null enum value for non-nullable component " + this.fieldNames[i] + " on " + this.recordType.getName());
      } else {
         return e.ordinal();
      }
   }

   @NotNull
   public Enum<?> enumFromOrdinal(int i, int ordinal) {
      Enum<?>[] consts = this.enumConstants[i];
      if (consts == null) {
         throw new IllegalStateException("enumFromOrdinal called for non-enum field " + this.fieldNames[i] + " on " + this.recordType.getName());
      } else if (ordinal >= 0 && ordinal < consts.length) {
         return consts[ordinal];
      } else {
         throw new IllegalArgumentException(
            "enum ordinal " + ordinal + " out of range for " + this.fieldNames[i] + " on " + this.recordType.getName() + " (max " + (consts.length - 1) + ")"
         );
      }
   }

   @NotNull
   protected R construct(@NotNull Object[] values) {
      try {
         return (R)this.constructor.invokeWithArguments(values);
      } catch (Throwable var3) {
         throw new IllegalStateException("decode failed for " + this.recordType.getName(), var3);
      }
   }

   @NotNull
   protected Object[] freshArgsArray() {
      return new Object[this.totalComponents];
   }

   @NotNull
   private static MethodHandle canonicalConstructorHandle(@NotNull Class<?> recordType, @NotNull Class<?>[] paramTypes) throws Throwable {
      return MethodHandles.lookup().findConstructor(recordType, MethodType.methodType(void.class, paramTypes));
   }

   protected int requireIndex(@NotNull String name) {
      Integer i = this.fieldIndex.get(name);
      if (i == null) {
         throw new IllegalArgumentException("no such field on " + this.recordType.getName() + ": " + name);
      } else {
         return i;
      }
   }

   @NotNull
   public TypeTag typeTagAt(int i) {
      return this.typeTags[i];
   }

   @NotNull
   public String fieldNameAt(int i) {
      return this.fieldNames[i];
   }

   public int fieldCount() {
      return this.fieldNames.length;
   }

   @Nullable
   public Object requireFieldOrDefault(int i, @Nullable Object value) {
      if (value != null) {
         return value;
      } else if (this.nullable[i]) {
         return null;
      } else {
         EncodeShape.FieldDef def = this.shape.fields().get(i);
         if (def.mergeMode() == MergeMode.PRESERVE_ON_NON_SENTINEL) {
            return def.sentinelValue();
         } else {
            throw new IllegalStateException("missing non-nullable field " + this.fieldNames[i] + " on " + this.recordType.getName());
         }
      }
   }
}
