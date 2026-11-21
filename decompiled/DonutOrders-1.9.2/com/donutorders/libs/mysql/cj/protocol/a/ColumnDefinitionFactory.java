package com.donutorders.libs.mysql.cj.protocol.a;

import com.donutorders.libs.mysql.cj.protocol.ColumnDefinition;
import com.donutorders.libs.mysql.cj.protocol.ProtocolEntityFactory;
import com.donutorders.libs.mysql.cj.result.DefaultColumnDefinition;
import com.donutorders.libs.mysql.cj.result.Field;

public class ColumnDefinitionFactory implements ProtocolEntityFactory<ColumnDefinition, NativePacketPayload> {
   protected long columnCount;
   protected ColumnDefinition columnDefinitionFromCache;

   public ColumnDefinitionFactory(long columnCount, ColumnDefinition columnDefinitionFromCache) {
      this.columnCount = columnCount;
      this.columnDefinitionFromCache = columnDefinitionFromCache;
   }

   public long getColumnCount() {
      return this.columnCount;
   }

   public ColumnDefinition getColumnDefinitionFromCache() {
      return this.columnDefinitionFromCache;
   }

   public ColumnDefinition createFromMessage(NativePacketPayload packetPayload) {
      return null;
   }

   public boolean mergeColumnDefinitions() {
      return false;
   }

   public ColumnDefinition createFromFields(Field[] fields) {
      return new DefaultColumnDefinition(fields);
   }
}
