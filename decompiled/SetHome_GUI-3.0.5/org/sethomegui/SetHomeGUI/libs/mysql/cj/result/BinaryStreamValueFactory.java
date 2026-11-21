package org.sethomegui.SetHomeGUI.libs.mysql.cj.result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;

public class BinaryStreamValueFactory extends DefaultValueFactory<InputStream> {
   public BinaryStreamValueFactory(PropertySet pset) {
      super(pset);
   }

   public InputStream createFromBytes(byte[] bytes, int offset, int length, Field f) {
      return new ByteArrayInputStream(bytes, offset, length);
   }

   @Override
   public String getTargetTypeName() {
      return InputStream.class.getName();
   }
}
