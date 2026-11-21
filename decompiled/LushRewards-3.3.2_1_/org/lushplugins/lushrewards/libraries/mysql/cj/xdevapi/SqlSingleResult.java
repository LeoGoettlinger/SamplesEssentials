package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import java.util.TimeZone;
import java.util.function.Supplier;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ColumnDefinition;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntity;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.RowList;

public class SqlSingleResult extends RowResultImpl implements SqlResult {
   public SqlSingleResult(ColumnDefinition metadata, TimeZone defaultTimeZone, RowList rows, Supplier<ProtocolEntity> completer, PropertySet pset) {
      super(metadata, defaultTimeZone, rows, completer, pset);
   }
}
