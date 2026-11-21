package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

import java.util.TimeZone;
import java.util.function.Supplier;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntity;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.RowList;

public class SqlSingleResult extends RowResultImpl implements SqlResult {
   public SqlSingleResult(ColumnDefinition metadata, TimeZone defaultTimeZone, RowList rows, Supplier<ProtocolEntity> completer, PropertySet pset) {
      super(metadata, defaultTimeZone, rows, completer, pset);
   }
}
