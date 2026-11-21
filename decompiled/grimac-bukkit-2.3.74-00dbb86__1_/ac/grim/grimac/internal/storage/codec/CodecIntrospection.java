package ac.grim.grimac.internal.storage.codec;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.codec.FieldKind;
import ac.grim.grimac.api.storage.codec.Id;
import ac.grim.grimac.api.storage.codec.Indexed;
import ac.grim.grimac.api.storage.codec.InsertOnly;
import ac.grim.grimac.api.storage.codec.MergeMax;
import ac.grim.grimac.api.storage.codec.MergeMin;
import ac.grim.grimac.api.storage.codec.MergeMode;
import ac.grim.grimac.api.storage.codec.Name;
import ac.grim.grimac.api.storage.codec.Nullable;
import ac.grim.grimac.api.storage.codec.Partition;
import ac.grim.grimac.api.storage.codec.Persistent;
import ac.grim.grimac.api.storage.codec.PreserveOnNonNull;
import ac.grim.grimac.api.storage.codec.SearchType;
import ac.grim.grimac.api.storage.codec.Searchable;
import ac.grim.grimac.api.storage.codec.Sentinel;
import ac.grim.grimac.api.storage.codec.Timestamp;
import ac.grim.grimac.api.storage.codec.Transient;
import ac.grim.grimac.api.storage.codec.Value;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@ApiStatus.Internal
public final class CodecIntrospection {
   private static final Set<String> RESERVED_FIELD_NAMES = Set.of("_v", "_id");
   private static final Set<String> RESERVED_TIMESERIES_TOP_LEVEL_NAMES = Set.of("meta");

   private CodecIntrospection() {
   }

   @NotNull
   public static EncodeShape inspect(@NotNull Class<?> recordType) {
      EncodeShape captured = CapturedBindings.shape(recordType);
      return captured != null ? captured : inspectReflective(recordType);
   }

   @NotNull
   public static EncodeShape inspectReflective(@NotNull Class<?> recordType) {
      if (!recordType.isRecord()) {
         throw new IllegalArgumentException("not a record: " + recordType.getName());
      } else {
         Persistent persistent = recordType.getAnnotation(Persistent.class);
         if (persistent == null) {
            throw new IllegalArgumentException("missing @Persistent on " + recordType.getName());
         } else {
            RecordComponent[] components = recordType.getRecordComponents();
            List<EncodeShape.FieldDef> fields = new ArrayList<>(components.length);
            String idField = null;
            String timestampField = null;
            List<String> partitionFields = new ArrayList<>();
            List<String> indexedFields = new ArrayList<>();
            List<String> searchableFields = new ArrayList<>();

            for (int componentIndex = 0; componentIndex < components.length; componentIndex++) {
               RecordComponent c = components[componentIndex];
               if (c.isAnnotationPresent(Transient.class)) {
                  validateTransient(recordType, c);
               } else {
                  String name = encodedName(c);
                  FieldKind kind = roleOf(c, recordType);
                  boolean nullable = isNullable(c);
                  Class<?> javaType = c.getType();
                  if (nullable && javaType.isPrimitive()) {
                     throw new IllegalArgumentException(recordType.getName() + "." + c.getName() + ": @Nullable on primitive type " + javaType);
                  }

                  if (nullable && javaType.isEnum()) {
                     throw new IllegalArgumentException(recordType.getName() + "." + c.getName() + ": @Nullable on enum not yet supported by the BSON codec");
                  }

                  if (isBoxedNumeric(javaType)) {
                     throw new IllegalArgumentException(
                        recordType.getName()
                           + "."
                           + c.getName()
                           + ": boxed numeric ("
                           + javaType.getSimpleName()
                           + ") not supported as a persistent field — use the primitive form"
                     );
                  }

                  SearchType searchType = null;
                  int vectorDim = 0;
                  Searchable searchable = c.getAnnotation(Searchable.class);
                  if (searchable != null) {
                     searchType = searchable.value();
                     vectorDim = searchable.dimension();
                     validateSearchableType(recordType, c, searchType, vectorDim);
                     searchableFields.add(name);
                  }

                  switch (kind) {
                     case ID:
                        if (idField != null) {
                           throw new IllegalArgumentException(recordType.getName() + ": multiple @Id components");
                        }

                        idField = name;
                        break;
                     case TIMESTAMP:
                        if (timestampField != null) {
                           throw new IllegalArgumentException(recordType.getName() + ": multiple @Timestamp components");
                        }

                        if (javaType != long.class) {
                           throw new IllegalArgumentException(
                              recordType.getName() + "." + c.getName() + ": @Timestamp requires long (epoch millis), got " + javaType.getName()
                           );
                        }

                        timestampField = name;
                        break;
                     case PARTITION:
                        partitionFields.add(name);
                        break;
                     case INDEXED:
                        indexedFields.add(name);
                  }

                  CodecIntrospection.MergeResolution merge = resolveMergeMode(recordType, c, kind, nullable, javaType);
                  fields.add(new EncodeShape.FieldDef(name, javaType, kind, nullable, searchType, vectorDim, componentIndex, merge.mode(), merge.sentinel()));
               }
            }

            if (idField == null) {
               throw new IllegalArgumentException(recordType.getName() + ": @Persistent record must declare exactly one @Id component");
            } else {
               long uniqueNames = fields.stream().map(EncodeShape.FieldDef::name).distinct().count();
               if (uniqueNames != fields.size()) {
                  throw new IllegalArgumentException(recordType.getName() + ": encoded field names must be unique — check @Name overrides");
               } else {
                  for (EncodeShape.FieldDef f : fields) {
                     if (RESERVED_FIELD_NAMES.contains(f.name())) {
                        throw new IllegalArgumentException(
                           recordType.getName()
                              + ": field name '"
                              + f.name()
                              + "' is reserved by the codec layer — pick a different @Name or rename the component"
                        );
                     }

                     if (RESERVED_TIMESERIES_TOP_LEVEL_NAMES.contains(f.name())) {
                        throw new IllegalArgumentException(
                           recordType.getName() + ": field name '" + f.name() + "' is reserved for the timeseries meta sub-document — pick a different @Name"
                        );
                     }
                  }

                  return new EncodeShape(idField, timestampField, partitionFields, indexedFields, searchableFields, fields, components.length);
               }
            }
         }
      }
   }

   private static void validateTransient(@NotNull Class<?> owner, @NotNull RecordComponent c) {
      if (c.isAnnotationPresent(Id.class)
         || c.isAnnotationPresent(Partition.class)
         || c.isAnnotationPresent(Timestamp.class)
         || c.isAnnotationPresent(Indexed.class)
         || c.isAnnotationPresent(Searchable.class)
         || c.isAnnotationPresent(Value.class)) {
         throw new IllegalArgumentException(owner.getName() + "." + c.getName() + ": @Transient cannot combine with any persistence-role annotation");
      }
   }

   @NotNull
   private static CodecIntrospection.MergeResolution resolveMergeMode(
      @NotNull Class<?> owner, @NotNull RecordComponent c, @NotNull FieldKind kind, boolean nullable, @NotNull Class<?> javaType
   ) {
      boolean insertOnly = c.isAnnotationPresent(InsertOnly.class);
      boolean preserve = c.isAnnotationPresent(PreserveOnNonNull.class);
      boolean max = c.isAnnotationPresent(MergeMax.class);
      boolean min = c.isAnnotationPresent(MergeMin.class);
      Sentinel sentinel = c.getAnnotation(Sentinel.class);
      int count = (insertOnly ? 1 : 0) + (preserve ? 1 : 0) + (max ? 1 : 0) + (min ? 1 : 0) + (sentinel != null ? 1 : 0);
      if (count == 0) {
         return new CodecIntrospection.MergeResolution(MergeMode.OVERWRITE, 0L);
      } else if (count > 1) {
         throw new IllegalArgumentException(
            owner.getName() + "." + c.getName() + ": at most one of @InsertOnly / @PreserveOnNonNull / @Sentinel / @MergeMax / @MergeMin"
         );
      } else if (insertOnly) {
         if (kind == FieldKind.ID) {
            throw new IllegalArgumentException(owner.getName() + "." + c.getName() + ": @InsertOnly is redundant on @Id (PK is insert-once by definition)");
         } else {
            return new CodecIntrospection.MergeResolution(MergeMode.INSERT_ONLY, 0L);
         }
      } else if (preserve) {
         if (nullable && !javaType.isPrimitive()) {
            return new CodecIntrospection.MergeResolution(MergeMode.PRESERVE_ON_NON_NULL, 0L);
         } else {
            throw new IllegalArgumentException(owner.getName() + "." + c.getName() + ": @PreserveOnNonNull requires a @Nullable reference-typed field");
         }
      } else if (sentinel != null) {
         if (javaType != long.class) {
            throw new IllegalArgumentException(owner.getName() + "." + c.getName() + ": @Sentinel requires primitive long, got " + javaType.getName());
         } else if (kind == FieldKind.ID) {
            throw new IllegalArgumentException(owner.getName() + "." + c.getName() + ": @Sentinel is redundant on @Id (PK is insert-once by definition)");
         } else {
            return new CodecIntrospection.MergeResolution(MergeMode.PRESERVE_ON_NON_SENTINEL, sentinel.value());
         }
      } else if (!max && !min) {
         return new CodecIntrospection.MergeResolution(MergeMode.OVERWRITE, 0L);
      } else if (!isNumericForMerge(javaType)) {
         throw new IllegalArgumentException(owner.getName() + "." + c.getName() + ": @MergeMax/@MergeMin require a numeric type, got " + javaType.getName());
      } else {
         return new CodecIntrospection.MergeResolution(max ? MergeMode.MAX : MergeMode.MIN, 0L);
      }
   }

   private static boolean isNumericForMerge(@NotNull Class<?> t) {
      return t == long.class || t == int.class || t == double.class || t == float.class;
   }

   @NotNull
   private static FieldKind roleOf(@NotNull RecordComponent c, @NotNull Class<?> owner) {
      boolean isId = c.isAnnotationPresent(Id.class);
      boolean isPartition = c.isAnnotationPresent(Partition.class);
      boolean isTimestamp = c.isAnnotationPresent(Timestamp.class);
      boolean isIndexed = c.isAnnotationPresent(Indexed.class);
      boolean isValue = c.isAnnotationPresent(Value.class);
      int primary = (isId ? 1 : 0) + (isPartition ? 1 : 0) + (isTimestamp ? 1 : 0);
      if (primary > 1) {
         throw new IllegalArgumentException(owner.getName() + "." + c.getName() + ": cannot combine @Id / @Partition / @Timestamp");
      } else if (isId) {
         return FieldKind.ID;
      } else if (isPartition) {
         return FieldKind.PARTITION;
      } else if (isTimestamp) {
         return FieldKind.TIMESTAMP;
      } else if (isIndexed) {
         return FieldKind.INDEXED;
      } else {
         return isValue ? FieldKind.VALUE : FieldKind.VALUE;
      }
   }

   @NotNull
   private static String encodedName(@NotNull RecordComponent c) {
      Name override = c.getAnnotation(Name.class);
      if (override != null) {
         String v = override.value();
         if (v != null && !v.isEmpty()) {
            return v;
         } else {
            throw new IllegalArgumentException("@Name value must be non-empty on " + c.getName());
         }
      } else {
         return snakeCase(c.getName());
      }
   }

   private static boolean isNullable(@NotNull RecordComponent c) {
      return c.isAnnotationPresent(Nullable.class);
   }

   private static boolean isBoxedNumeric(@NotNull Class<?> t) {
      return t == Integer.class || t == Long.class || t == Double.class || t == Float.class || t == Short.class || t == Byte.class;
   }

   private static void validateSearchableType(@NotNull Class<?> owner, @NotNull RecordComponent c, @NotNull SearchType st, int vectorDim) {
      Class<?> t = c.getType();
      switch (st) {
         case KEYWORD:
         case KEYWORD_PREFIX:
         case TEXT:
            if (t != String.class) {
               throw new IllegalArgumentException(owner.getName() + "." + c.getName() + ": @Searchable(" + st + ") requires String, got " + t.getName());
            }
            break;
         case NUMERIC:
            if (t != int.class && t != long.class && t != double.class && t != float.class) {
               throw new IllegalArgumentException(
                  owner.getName() + "." + c.getName() + ": @Searchable(NUMERIC) requires int/long/double/float primitive, got " + t.getName()
               );
            }
            break;
         case VECTOR:
            if (t != float[].class) {
               throw new IllegalArgumentException(owner.getName() + "." + c.getName() + ": @Searchable(VECTOR) requires float[], got " + t.getName());
            }

            if (vectorDim <= 0) {
               throw new IllegalArgumentException(owner.getName() + "." + c.getName() + ": @Searchable(VECTOR) requires dimension > 0");
            }
      }
   }

   @NotNull
   public static String snakeCase(@NotNull String camel) {
      StringBuilder sb = new StringBuilder(camel.length() + 4);

      for (int i = 0; i < camel.length(); i++) {
         char c = camel.charAt(i);
         if (Character.isUpperCase(c)) {
            if (i > 0) {
               sb.append('_');
            }

            sb.append(Character.toLowerCase(c));
         } else {
            sb.append(c);
         }
      }

      return sb.toString();
   }

   public static RecordComponent componentByJavaName(@NotNull Class<?> recordType, @NotNull String javaName) {
      return Arrays.stream(recordType.getRecordComponents()).filter(rc -> rc.getName().equals(javaName)).findFirst().orElse(null);
   }

   private record MergeResolution(@NotNull MergeMode mode, long sentinel) {
   }
}
