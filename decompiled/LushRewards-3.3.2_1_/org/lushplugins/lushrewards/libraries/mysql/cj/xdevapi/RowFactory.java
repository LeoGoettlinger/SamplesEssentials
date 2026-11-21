package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import java.util.TimeZone;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ColumnDefinition;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntity;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntityFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x.XMessage;

public class RowFactory implements ProtocolEntityFactory<Row, XMessage> {
   private ColumnDefinition metadata;
   private TimeZone defaultTimeZone;
   private PropertySet pset;

   public RowFactory(ColumnDefinition metadata, TimeZone defaultTimeZone, PropertySet pset) {
      this.metadata = metadata;
      this.defaultTimeZone = defaultTimeZone;
      this.pset = pset;
   }

   public Row createFromProtocolEntity(ProtocolEntity internalRow) {
      return new RowImpl((org.lushplugins.lushrewards.libraries.mysql.cj.result.Row)internalRow, this.metadata, this.defaultTimeZone, this.pset);
   }
}
