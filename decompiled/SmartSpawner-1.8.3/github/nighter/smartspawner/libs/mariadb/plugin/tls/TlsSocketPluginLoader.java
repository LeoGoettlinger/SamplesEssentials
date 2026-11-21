package github.nighter.smartspawner.libs.mariadb.plugin.tls;

import github.nighter.smartspawner.libs.mariadb.Driver;
import github.nighter.smartspawner.libs.mariadb.plugin.TlsSocketPlugin;
import github.nighter.smartspawner.libs.mariadb.plugin.tls.main.DefaultTlsSocketPlugin;
import java.sql.SQLException;
import java.util.ServiceLoader;

public final class TlsSocketPluginLoader {
   public static TlsSocketPlugin get(String type) throws SQLException {
      if (type == null) {
         return new DefaultTlsSocketPlugin();
      } else {
         for (TlsSocketPlugin implClass : ServiceLoader.load(TlsSocketPlugin.class, Driver.class.getClassLoader())) {
            if (type.equals(implClass.type())) {
               return implClass;
            }
         }

         throw new SQLException("Client has not found any TLS factory plugin with name '" + type + "'.", "08004", 1251);
      }
   }
}
