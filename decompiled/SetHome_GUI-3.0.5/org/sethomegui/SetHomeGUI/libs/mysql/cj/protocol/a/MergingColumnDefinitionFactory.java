package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.WrongArgumentException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntityFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.DefaultColumnDefinition;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.Field;

public class MergingColumnDefinitionFactory extends ColumnDefinitionFactory implements ProtocolEntityFactory<ColumnDefinition, NativePacketPayload> {
   public MergingColumnDefinitionFactory(long columnCount, ColumnDefinition columnDefinitionFromCache) {
      super(columnCount, columnDefinitionFromCache);
   }

   @Override
   public boolean mergeColumnDefinitions() {
      return true;
   }

   @Override
   public ColumnDefinition createFromFields(Field[] fields) {
      if (this.columnDefinitionFromCache != null) {
         if (fields.length != this.columnCount) {
            throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, "Wrong number of ColumnDefinition fields.");
         }

         Field[] f = this.columnDefinitionFromCache.getFields();

         for (int i = 0; i < fields.length; i++) {
            fields[i].setFlags(f[i].getFlags());
         }
      }

      return new DefaultColumnDefinition(fields);
   }
}
