package org.lushplugins.lushrewards.libraries.mysql.cj.protocol;

import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionInterceptor;

public interface AuthenticationProvider<M extends Message> {
   void init(Protocol<M> var1, PropertySet var2, ExceptionInterceptor var3);

   void connect(String var1, String var2, String var3);

   void changeUser(String var1, String var2, String var3);
}
