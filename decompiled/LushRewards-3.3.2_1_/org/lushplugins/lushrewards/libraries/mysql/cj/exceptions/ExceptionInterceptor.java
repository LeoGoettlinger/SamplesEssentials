package org.lushplugins.lushrewards.libraries.mysql.cj.exceptions;

import java.util.Properties;
import org.lushplugins.lushrewards.libraries.mysql.cj.log.Log;

public interface ExceptionInterceptor {
   ExceptionInterceptor init(Properties var1, Log var2);

   void destroy();

   Exception interceptException(Exception var1);
}
