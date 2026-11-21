package org.jdbi.v3.core.mapper;

import org.jdbi.v3.core.config.JdbiConfig;

public class MapEntryMappers implements JdbiConfig<MapEntryMappers>, MapEntryConfig<MapEntryMappers> {
   private String keyColumn;
   private String valueColumn;

   public MapEntryMappers() {
   }

   private MapEntryMappers(MapEntryMappers that) {
      this.keyColumn = that.keyColumn;
      this.valueColumn = that.valueColumn;
   }

   @Override
   public String getKeyColumn() {
      return this.keyColumn;
   }

   public MapEntryMappers setKeyColumn(String keyColumn) {
      this.keyColumn = keyColumn;
      return this;
   }

   @Override
   public String getValueColumn() {
      return this.valueColumn;
   }

   public MapEntryMappers setValueColumn(String valueColumn) {
      this.valueColumn = valueColumn;
      return this;
   }

   public MapEntryMappers createCopy() {
      return new MapEntryMappers(this);
   }
}
