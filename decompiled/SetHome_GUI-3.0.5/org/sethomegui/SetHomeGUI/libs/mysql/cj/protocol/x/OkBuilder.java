package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.ExceptionFactory;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.WrongArgumentException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntity;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ResultBuilder;

public class OkBuilder implements ResultBuilder<Ok> {
   @Override
   public boolean addProtocolEntity(ProtocolEntity entity) {
      if (entity instanceof Notice) {
         return false;
      } else if (entity instanceof Ok) {
         return true;
      } else {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, "Unexpected protocol entity " + entity);
      }
   }

   public Ok build() {
      return new Ok();
   }
}
