package org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions;

import java.util.Properties;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.log.Log;

public interface ExceptionInterceptor {
   ExceptionInterceptor init(Properties var1, Log var2);

   void destroy();

   Exception interceptException(Exception var1);
}
