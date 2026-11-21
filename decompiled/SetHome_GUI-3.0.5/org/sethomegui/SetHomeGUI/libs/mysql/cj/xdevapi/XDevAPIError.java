package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJException;

public class XDevAPIError extends CJException {
   private static final long serialVersionUID = 9102723045325569686L;

   public XDevAPIError(String message) {
      super(message);
   }

   public XDevAPIError(String message, Throwable t) {
      super(message, t);
   }
}
