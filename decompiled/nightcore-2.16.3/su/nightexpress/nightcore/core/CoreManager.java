package su.nightexpress.nightcore.core;

import java.util.HashSet;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.NightCore;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.core.listener.CoreListener;
import su.nightexpress.nightcore.dialog.Dialog;
import su.nightexpress.nightcore.dialog.DialogListener;
import su.nightexpress.nightcore.integration.item.ItemBridge;
import su.nightexpress.nightcore.integration.item.adapter.impl.CraftEngineAdapter;
import su.nightexpress.nightcore.integration.item.adapter.impl.ECratesCrateAdapter;
import su.nightexpress.nightcore.integration.item.adapter.impl.ECratesKeyAdapter;
import su.nightexpress.nightcore.integration.item.adapter.impl.ExcellentCratesHandler;
import su.nightexpress.nightcore.integration.item.adapter.impl.ExecutableItemsAdapter;
import su.nightexpress.nightcore.integration.item.adapter.impl.ItemsAdderAdapter;
import su.nightexpress.nightcore.integration.item.adapter.impl.MMOItemsAdapter;
import su.nightexpress.nightcore.integration.item.adapter.impl.NexoAdapter;
import su.nightexpress.nightcore.integration.item.adapter.impl.OraxenAdapter;
import su.nightexpress.nightcore.integration.item.adapter.impl.VanillaItemAdapter;
import su.nightexpress.nightcore.integration.permission.PermissionBridge;
import su.nightexpress.nightcore.integration.permission.PermissionProvider;
import su.nightexpress.nightcore.integration.permission.impl.LuckPermissionProvider;
import su.nightexpress.nightcore.integration.permission.impl.VaultPermissionProvider;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.menu.MenuListener;
import su.nightexpress.nightcore.menu.impl.AbstractMenu;
import su.nightexpress.nightcore.ui.UIListener;
import su.nightexpress.nightcore.ui.dialog.DialogManager;
import su.nightexpress.nightcore.ui.menu.Menu;
import su.nightexpress.nightcore.ui.menu.MenuRegistry;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.profile.CachedProfile;
import su.nightexpress.nightcore.util.profile.PlayerProfiles;

public class CoreManager extends AbstractManager<NightCore> {
   public CoreManager(@NonNull NightCore core) {
      super(core);
   }

   @Override
   protected void onLoad() {
      this.loadPermissionIntegrations();
      this.loadItemIntegrations();
      this.addListener(new CoreListener(this.plugin));
      this.addListener(new DialogListener(this.plugin));
      this.addListener(new MenuListener(this.plugin));
      this.addListener(new UIListener(this.plugin));
      this.addTask(this::tickMenusAndDialogs, 1);
      this.addAsyncTask(PlayerProfiles::purgeProfiles, CoreConfig.PROFILE_PURGE_INTERVAL.get());
      this.addAsyncTask(this::updateProfiles, CoreConfig.PROFILE_UPDATE_INTERVAL.get());
   }

   @Override
   protected void onShutdown() {
      Dialog.shutdown();
      DialogManager.shutdown();
      ItemBridge.unregisterAll();
      PermissionBridge.unregisterAll();
   }

   private void loadPermissionIntegrations() {
      if (Plugins.isInstalled("LuckPerms")) {
         this.registerPermissionProvider(new LuckPermissionProvider());
      } else if (Plugins.isInstalled("Vault")) {
         this.registerPermissionProvider(new VaultPermissionProvider());
      }
   }

   private void registerPermissionProvider(@NonNull PermissionProvider provider) {
      PermissionBridge.register(provider);
      this.plugin.info("Registered permissions provider: " + provider.getName());
   }

   private void loadItemIntegrations() {
      this.registerItemProvider(VanillaItemAdapter.INSTANCE);
      this.registerExternalItemProvider("ExcellentCrates", ExcellentCratesHandler::new);
      this.registerExternalItemProvider("ExcellentCrates", ECratesCrateAdapter::new);
      this.registerExternalItemProvider("ExcellentCrates", ECratesKeyAdapter::new);
      this.registerExternalItemProvider("ExecutableItems", ExecutableItemsAdapter::new);
      this.registerExternalItemProvider("ItemsAdder", ItemsAdderAdapter::new);
      this.registerExternalItemProvider("MMOItems", MMOItemsAdapter::new);
      this.registerExternalItemProvider("Nexo", NexoAdapter::new);
      this.registerExternalItemProvider("Oraxen", OraxenAdapter::new);
      this.registerExternalItemProvider("CraftEngine", CraftEngineAdapter::new);
   }

   private <T> boolean registerExternalItemProvider(@NonNull String pluginName, @NonNull Supplier<ItemAdapter<T>> supplier) {
      return !Plugins.isInstalled(pluginName) ? false : this.registerItemProvider(supplier.get());
   }

   private <T> boolean registerItemProvider(@NonNull ItemAdapter<T> provider) {
      String name = provider.getName();
      if (CoreConfig.ITEMS_DISABLED_PROVIDERS.get().contains(name)) {
         return false;
      } else {
         ItemBridge.register(provider);
         this.plugin.info("Registered item provider: '" + name + "'.");
         return true;
      }
   }

   private void updateProfiles() {
      CachedProfile.updateCandidates(CoreConfig.PROFILE_UPDATE_AMOUNT.get());
   }

   private void tickMenusAndDialogs() {
      this.tickDialogs();
      this.tickMenus();
   }

   @Deprecated
   private void tickDialogs() {
      Dialog.checkTimeOut();
      DialogManager.tickDialogs();
   }

   private void tickMenus() {
      new HashSet<>(AbstractMenu.PLAYER_MENUS.values()).forEach(menu -> {
         if (menu.getOptions().isReadyToRefresh()) {
            menu.flush();
            menu.getOptions().setLastAutoRefresh(System.currentTimeMillis());
         }
      });
      MenuRegistry.getViewers().stream().map(MenuViewer::getMenu).distinct().forEach(Menu::tick);
   }
}
