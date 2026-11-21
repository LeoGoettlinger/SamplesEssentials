package su.nightexpress.nightcore.ui.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCore;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.ui.inventory.listener.MenuInventoryListener;

public class MenuRegistry extends AbstractManager<NightCore> {
   private final Map<UUID, Menu> activeMenus = new HashMap<>();

   public MenuRegistry(@NonNull NightCore core) {
      super(core);
   }

   @Override
   protected void onLoad() {
      this.addListener(new MenuInventoryListener(this.plugin, this));
      this.addTask(this::tickMenus, 1);
   }

   @Override
   protected void onShutdown() {
      this.getActiveMenus().forEach(Menu::close);
      this.activeMenus.clear();
   }

   public void tickMenus() {
      this.getActiveMenus().forEach(Menu::tick);
   }

   public void registerViewer(@NonNull Player player, @NonNull Menu menu) {
      this.activeMenus.put(player.getUniqueId(), menu);
   }

   public void unregisterViewer(@NonNull Player player) {
      this.activeMenus.remove(player.getUniqueId());
   }

   @Nullable
   public Menu getActiveMenu(@NonNull Player player) {
      return this.getActiveMenu(player.getUniqueId());
   }

   @Nullable
   public Menu getActiveMenu(@NonNull UUID playerId) {
      return this.activeMenus.get(playerId);
   }

   @NonNull
   public Set<Menu> getActiveMenus() {
      return Set.copyOf(this.activeMenus.values());
   }
}
