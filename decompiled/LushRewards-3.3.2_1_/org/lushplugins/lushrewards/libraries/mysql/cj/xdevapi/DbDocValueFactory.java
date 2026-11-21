package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import java.io.IOException;
import java.io.StringReader;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.AssertionFailedException;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.DefaultValueFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.Field;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;

public class DbDocValueFactory extends DefaultValueFactory<DbDoc> {
   public DbDocValueFactory(PropertySet pset) {
      super(pset);
   }

   public DbDoc createFromBytes(byte[] bytes, int offset, int length, Field f) {
      try {
         return JsonParser.parseDoc(new StringReader(StringUtils.toString(bytes, offset, length, f.getEncoding())));
      } catch (IOException var6) {
         throw AssertionFailedException.shouldNotHappen(var6);
      }
   }

   public DbDoc createFromNull() {
      return null;
   }

   @Override
   public String getTargetTypeName() {
      return DbDoc.class.getName();
   }
}
