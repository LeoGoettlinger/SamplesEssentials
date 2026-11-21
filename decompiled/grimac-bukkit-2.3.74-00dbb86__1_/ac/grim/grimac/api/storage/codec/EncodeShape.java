package ac.grim.grimac.api.storage.codec;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;

@ApiStatus.Experimental
public record EncodeShape(
   @NotNull String idField,
   @ac.grim.grimac.shaded.jetbrains.annotations.Nullable String timestampField,
   @NotNull List<String> partitionFields,
   @NotNull List<String> indexedFields,
   @NotNull List<String> searchableFields,
   @NotNull List<EncodeShape.FieldDef> fields,
   int totalComponentCount
) {
   public EncodeShape(
      @NotNull String idField,
      @ac.grim.grimac.shaded.jetbrains.annotations.Nullable String timestampField,
      @NotNull List<String> partitionFields,
      @NotNull List<String> indexedFields,
      @NotNull List<String> searchableFields,
      @NotNull List<EncodeShape.FieldDef> fields,
      int totalComponentCount
   ) {
      partitionFields = List.copyOf(partitionFields);
      indexedFields = List.copyOf(indexedFields);
      searchableFields = List.copyOf(searchableFields);
      fields = List.copyOf(fields);
      if (totalComponentCount < fields.size()) {
         throw new IllegalArgumentException("totalComponentCount (" + totalComponentCount + ") < persistent field count (" + fields.size() + ")");
      } else {
         this.idField = idField;
         this.timestampField = timestampField;
         this.partitionFields = partitionFields;
         this.indexedFields = indexedFields;
         this.searchableFields = searchableFields;
         this.fields = fields;
         this.totalComponentCount = totalComponentCount;
      }
   }

   public record FieldDef(
      @NotNull String name,
      @NotNull Class<?> javaType,
      @NotNull FieldKind kind,
      boolean nullable,
      @ac.grim.grimac.shaded.jetbrains.annotations.Nullable SearchType searchType,
      int vectorDimension,
      int recordIndex,
      @NotNull MergeMode mergeMode,
      long sentinelValue
   ) {
      public FieldDef(
         @NotNull String name,
         @NotNull Class<?> javaType,
         @NotNull FieldKind kind,
         boolean nullable,
         @ac.grim.grimac.shaded.jetbrains.annotations.Nullable SearchType searchType,
         int vectorDimension,
         int recordIndex,
         @NotNull MergeMode mergeMode
      ) {
         this(name, javaType, kind, nullable, searchType, vectorDimension, recordIndex, mergeMode, 0L);
      }

      public FieldDef(
         @NotNull String name,
         @NotNull Class<?> javaType,
         @NotNull FieldKind kind,
         boolean nullable,
         @ac.grim.grimac.shaded.jetbrains.annotations.Nullable SearchType searchType,
         int vectorDimension,
         int recordIndex
      ) {
         this(name, javaType, kind, nullable, searchType, vectorDimension, recordIndex, MergeMode.OVERWRITE, 0L);
      }
   }
}
