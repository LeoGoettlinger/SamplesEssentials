package org.jdbi.v3.core.mapper;

public interface MapEntryConfig<This> {
   String getKeyColumn();

   This setKeyColumn(String keyColumn);

   String getValueColumn();

   This setValueColumn(String valueColumn);
}
