package org.lushplugins.lushrewards.libraries.mysql.cj;

import java.util.List;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Message;

public interface MessageBuilder<M extends Message> {
   M buildSqlStatement(String var1);

   M buildSqlStatement(String var1, List<Object> var2);

   M buildClose();

   M buildComQuery(M var1, Session var2, PreparedQuery var3, QueryBindings var4, String var5);
}
