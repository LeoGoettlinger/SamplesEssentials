package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntity;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Field;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.RowList;

public class RowResultImpl extends AbstractDataResult<Row> implements RowResult {
   private ColumnDefinition metadata;

   public RowResultImpl(ColumnDefinition metadata, TimeZone defaultTimeZone, RowList rows, Supplier<ProtocolEntity> completer, PropertySet pset) {
      super(rows, completer, new RowFactory(metadata, defaultTimeZone, pset));
      this.metadata = metadata;
   }

   @Override
   public int getColumnCount() {
      return this.metadata.getFields().length;
   }

   @Override
   public List<Column> getColumns() {
      return Arrays.stream(this.metadata.getFields()).map(ColumnImpl::new).collect(Collectors.toList());
   }

   @Override
   public List<String> getColumnNames() {
      return Arrays.stream(this.metadata.getFields()).map(Field::getColumnLabel).collect(Collectors.toList());
   }
}
