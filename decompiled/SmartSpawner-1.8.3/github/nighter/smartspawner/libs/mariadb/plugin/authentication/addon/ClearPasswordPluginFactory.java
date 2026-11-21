package github.nighter.smartspawner.libs.mariadb.plugin.authentication.addon;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.HostAddress;
import github.nighter.smartspawner.libs.mariadb.plugin.AuthenticationPlugin;
import github.nighter.smartspawner.libs.mariadb.plugin.AuthenticationPluginFactory;

public class ClearPasswordPluginFactory implements AuthenticationPluginFactory {
   @Override
   public String type() {
      return "mysql_clear_password";
   }

   @Override
   public boolean requireSecure() {
      return true;
   }

   @Override
   public AuthenticationPlugin initialize(String authenticationData, byte[] seed, Configuration conf, HostAddress hostAddress) {
      return new ClearPasswordPlugin(authenticationData, hostAddress, conf);
   }
}
