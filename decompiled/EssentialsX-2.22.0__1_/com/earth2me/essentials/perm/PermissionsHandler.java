package com.earth2me.essentials.perm;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.perm.impl.AbstractVaultHandler;
import com.earth2me.essentials.perm.impl.ConfigPermissionsHandler;
import com.earth2me.essentials.perm.impl.GenericVaultHandler;
import com.earth2me.essentials.perm.impl.LuckPermsHandler;
import com.earth2me.essentials.perm.impl.ModernVaultHandler;
import com.earth2me.essentials.perm.impl.SuperpermsHandler;
import com.earth2me.essentials.utils.TriState;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PermissionsHandler implements IPermissionsHandler {
   private final transient String defaultGroup = "default";
   private final transient Essentials ess;
   private transient IPermissionsHandler handler = null;
   private transient boolean useSuperperms;
   private Class<?> lastHandler = null;

   public PermissionsHandler(final Essentials plugin, final boolean useSuperperms) {
      this.ess = plugin;
      this.useSuperperms = useSuperperms;
   }

   @Override
   public String getGroup(final OfflinePlayer base) {
      long start = System.nanoTime();
      String group = this.handler.getGroup(base);
      if (group == null) {
         group = "default";
      }

      this.checkPermLag(start, String.format("Getting group for %s", base.getName()));
      return group;
   }

   @Override
   public List<String> getGroups(final OfflinePlayer base) {
      long start = System.nanoTime();
      List<String> groups = new ArrayList<>();
      groups.add("default");
      List<String> handlerGroups = this.handler.getGroups(base);
      if (handlerGroups != null && !handlerGroups.isEmpty()) {
         groups.addAll(handlerGroups);
      }

      this.checkPermLag(start, String.format("Getting groups for %s", base.getName()));
      return Collections.unmodifiableList(groups);
   }

   @Override
   public List<String> getGroups() {
      long start = System.nanoTime();
      List<String> groups = this.handler.getGroups();
      if (groups == null || groups.isEmpty()) {
         groups = Collections.singletonList("default");
      }

      this.checkPermLag(start, "Getting all groups");
      return Collections.unmodifiableList(groups);
   }

   @Override
   public boolean addToGroup(OfflinePlayer base, String group) {
      long start = System.nanoTime();
      boolean result = this.handler.addToGroup(base, group);
      this.checkPermLag(start, String.format("Adding group to %s", base.getName()));
      return result;
   }

   @Override
   public boolean removeFromGroup(OfflinePlayer base, String group) {
      long start = System.nanoTime();
      boolean result = this.handler.removeFromGroup(base, group);
      this.checkPermLag(start, String.format("Removing group from %s", base.getName()));
      return result;
   }

   @Override
   public boolean canBuild(final Player base, final String group) {
      return this.handler.canBuild(base, group);
   }

   @Override
   public boolean inGroup(final Player base, final String group) {
      long start = System.nanoTime();
      boolean result = this.handler.inGroup(base, group);
      this.checkPermLag(start, String.format("Checking if %s is in group %s", base.getName(), group));
      return result;
   }

   @Override
   public boolean hasPermission(final Player base, final String node) {
      return this.handler.hasPermission(base, node);
   }

   @Override
   public boolean hasPermissionCached(final Player base, final String node) {
      return this.handler.hasPermissionCached(base, node);
   }

   @Override
   public void invalidatePermissionCache(final UUID uuid) {
      this.handler.invalidatePermissionCache(uuid);
   }

   @Override
   public boolean isPermissionSet(final Player base, final String node) {
      return this.handler.isPermissionSet(base, node);
   }

   @Override
   public boolean isOfflinePermissionSet(UUID uuid, String node) {
      return this.handler.isOfflinePermissionSet(uuid, node);
   }

   @Override
   public TriState isPermissionSetExact(Player base, String node) {
      return this.handler.isPermissionSetExact(base, node);
   }

   @Override
   public String getPrefix(final Player base) {
      long start = System.nanoTime();
      String prefix = this.handler.getPrefix(base);
      if (prefix == null) {
         prefix = "";
      }

      this.checkPermLag(start, String.format("Getting prefix for %s", base.getName()));
      return prefix;
   }

   @Override
   public String getSuffix(final Player base) {
      long start = System.nanoTime();
      String suffix = this.handler.getSuffix(base);
      if (suffix == null) {
         suffix = "";
      }

      this.checkPermLag(start, String.format("Getting suffix for %s", base.getName()));
      return suffix;
   }

   @Override
   public void registerContext(final String context, final Function<User, Iterable<String>> calculator, final Supplier<Iterable<String>> suggestions) {
      this.handler.registerContext(context, calculator, suggestions);
   }

   @Override
   public void unregisterContexts() {
      this.handler.unregisterContexts();
   }

   @Override
   public String getBackendName() {
      return this.handler.getBackendName();
   }

   @Override
   public boolean tryProvider(Essentials ess) {
      return true;
   }

   public void checkPermissions() {
      for (Class<? extends IPermissionsHandler> providerClass : Arrays.asList(
         LuckPermsHandler.class, ModernVaultHandler.class, GenericVaultHandler.class, SuperpermsHandler.class
      )) {
         try {
            IPermissionsHandler provider = providerClass.newInstance();
            if (provider.tryProvider(this.ess)) {
               if (provider.getClass().isInstance(this.handler)) {
                  return;
               }

               if (this.handler != null) {
                  this.unregisterContexts();
               }

               this.handler = provider;
               this.initContexts();
               break;
            }
         } catch (Throwable var5) {
         }
      }

      if (this.handler == null) {
         if (this.useSuperperms) {
            this.handler = new SuperpermsHandler();
         } else {
            this.handler = new ConfigPermissionsHandler(this.ess);
         }
      }

      Class<?> handlerClass = this.handler.getClass();
      if (this.lastHandler == null || this.lastHandler != handlerClass) {
         this.lastHandler = handlerClass;
         if (this.handler instanceof AbstractVaultHandler) {
            String enabledPermsPlugin = ((AbstractVaultHandler)this.handler).getEnabledPermsPlugin();
            if (enabledPermsPlugin == null) {
               enabledPermsPlugin = "generic";
            }

            this.ess.getLogger().info("Using Vault based permissions (" + enabledPermsPlugin + ")");
         } else if (this.handler.getClass() == ConfigPermissionsHandler.class) {
            this.ess.getLogger().info("Using config file enhanced permissions.");
            this.ess.getLogger().info("Permissions listed in as player-commands will be given to all users.");
         } else if (this.handler.getClass() == SuperpermsHandler.class) {
            if (this.handler.tryProvider(this.ess)) {
               this.ess
                  .getLogger()
                  .warning("Detected supported permissions plugin " + ((SuperpermsHandler)this.handler).getEnabledPermsPlugin() + " without Vault installed.");
               this.ess.getLogger().warning("Features such as chat prefixes/suffixes and group-related functionality will not work until you install Vault.");
            }

            this.ess.getLogger().info("Using superperms-based permissions.");
         }
      }
   }

   public void setUseSuperperms(final boolean useSuperperms) {
      this.useSuperperms = useSuperperms;
   }

   public String getName() {
      return this.handler.getClass().getSimpleName().replace("Provider", "");
   }

   private void checkPermLag(final long start, final String summary) {
      long elapsed = System.nanoTime() - start;
      if (elapsed > this.ess.getSettings().getPermissionsLagWarning()) {
         this.ess
            .getLogger()
            .log(
               Level.WARNING, String.format("Permissions lag notice with (%s). Response took %fms. Summary: %s", this.getName(), elapsed / 1000000.0, summary)
            );
      }
   }

   private void initContexts() {
      this.registerContext("essentials:afk", user -> Collections.singleton(String.valueOf(user.isAfk())), () -> ImmutableSet.of("true", "false"));
      this.registerContext("essentials:muted", user -> Collections.singleton(String.valueOf(user.isMuted())), () -> ImmutableSet.of("true", "false"));
      this.registerContext("essentials:vanished", user -> Collections.singleton(String.valueOf(user.isHidden())), () -> ImmutableSet.of("true", "false"));
      this.registerContext("essentials:jailed", user -> Collections.singleton(String.valueOf(user.isJailed())), () -> ImmutableSet.of("true", "false"));
      this.registerContext(
         "essentials:jail", user -> Optional.ofNullable(user.getJail()).map(xva$0 -> Arrays.asList(xva$0)).orElse(Collections.emptyList()), () -> {
            try {
               return this.ess.getJails().getList();
            } catch (Exception var2) {
               return Collections.emptyList();
            }
         }
      );
   }
}
