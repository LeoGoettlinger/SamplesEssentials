package org.lushplugins.lushrewards.libraries.mysql.cj.interceptors;

import java.util.Properties;
import java.util.function.Supplier;
import org.lushplugins.lushrewards.libraries.mysql.cj.MysqlConnection;
import org.lushplugins.lushrewards.libraries.mysql.cj.Query;
import org.lushplugins.lushrewards.libraries.mysql.cj.log.Log;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Message;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Resultset;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ServerSession;

public interface QueryInterceptor {
   QueryInterceptor init(MysqlConnection var1, Properties var2, Log var3);

   <T extends Resultset> T preProcess(Supplier<String> var1, Query var2);

   default <M extends Message> M preProcess(M queryPacket) {
      return null;
   }

   boolean executeTopLevelOnly();

   void destroy();

   <T extends Resultset> T postProcess(Supplier<String> var1, Query var2, T var3, ServerSession var4);

   default <M extends Message> M postProcess(M queryPacket, M originalResponsePacket) {
      return null;
   }
}
