package github.nighter.smartspawner.libs.mariadb.plugin.credential;

import github.nighter.smartspawner.libs.mariadb.Driver;
import github.nighter.smartspawner.libs.mariadb.plugin.CredentialPlugin;
import java.util.ServiceLoader;

public final class CredentialPluginLoader {
   public static CredentialPlugin get(String type) {
      if (type == null) {
         return null;
      } else {
         for (CredentialPlugin implClass : ServiceLoader.load(CredentialPlugin.class, Driver.class.getClassLoader())) {
            if (type.equals(implClass.type())) {
               return implClass;
            }
         }

         throw new IllegalArgumentException("No identity plugin registered with the type \"" + type + "\".");
      }
   }
}
