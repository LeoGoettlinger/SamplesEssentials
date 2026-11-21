package github.nighter.smartspawner.libs.mariadb.plugin.authentication;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.Driver;
import github.nighter.smartspawner.libs.mariadb.plugin.AuthenticationPluginFactory;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ServiceLoader;

public final class AuthenticationPluginLoader {
   private AuthenticationPluginLoader() {
   }

   public static AuthenticationPluginFactory get(String type, Configuration conf) throws SQLException {
      ServiceLoader<AuthenticationPluginFactory> loader = ServiceLoader.load(AuthenticationPluginFactory.class, Driver.class.getClassLoader());
      String[] authList = conf.restrictedAuth() != null ? conf.restrictedAuth().split(",") : null;

      for (AuthenticationPluginFactory implClass : loader) {
         if (type.equals(implClass.type())) {
            if (authList != null && !Arrays.stream(authList).anyMatch(type::equals)) {
               throw new SQLException(
                  String.format(
                     "Client restrict authentication plugin to a limited set of authentication plugin and doesn't permit requested plugin ('%s'). Current list is `restrictedAuth=%s`",
                     type,
                     conf.restrictedAuth()
                  ),
                  "08004",
                  1251
               );
            }

            return implClass;
         }
      }

      throw new SQLException("Client does not support authentication protocol requested by server. plugin type was = '" + type + "'", "08004", 1251);
   }
}
