package ac.grim.grimac.platform.bukkit.manager;

import ac.grim.grimac.platform.api.manager.PermissionRegistrationManager;
import ac.grim.grimac.platform.api.permissions.PermissionDefaultValue;
import ac.grim.grimac.platform.bukkit.utils.convert.BukkitConversionUtils;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class BukkitPermissionRegistrationManager implements PermissionRegistrationManager {
   @Override
   public void registerPermission(String name, PermissionDefaultValue defaultValue) {
      Permission bukkitPermission = Bukkit.getPluginManager().getPermission(name);
      PermissionDefault permissionDefault = BukkitConversionUtils.toBukkitPermissionDefault(defaultValue);
      if (bukkitPermission == null) {
         Bukkit.getPluginManager().addPermission(new Permission(name, permissionDefault));
      } else {
         bukkitPermission.setDefault(permissionDefault);
      }
   }
}
