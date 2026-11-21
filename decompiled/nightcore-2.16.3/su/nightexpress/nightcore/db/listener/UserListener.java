package su.nightexpress.nightcore.db.listener;

import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.db.AbstractUser;
import su.nightexpress.nightcore.db.AbstractUserManager;
import su.nightexpress.nightcore.manager.AbstractListener;

@Deprecated
public class UserListener<P extends NightPlugin, U extends AbstractUser> extends AbstractListener<P> {
   private final AbstractUserManager<P, U> manager;

   public UserListener(@NonNull P plugin, @NonNull AbstractUserManager<P, U> manager) {
      super(plugin);
      this.manager = manager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onUserLogin(AsyncPlayerPreLoginEvent event) {
      if (event.getLoginResult() == Result.ALLOWED) {
         UUID uuid = event.getUniqueId();
         if (!this.manager.isInDatabase(uuid)) {
            U user = this.manager.create(uuid, event.getName());
            this.manager.addInDatabase(user);
            this.manager.cacheTemporary(user);
         } else {
            this.manager.getOrFetch(uuid);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onUserJoin(PlayerJoinEvent event) {
      this.manager.handleJoin(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onUserQuit(PlayerQuitEvent event) {
      this.manager.handleQuit(event.getPlayer());
   }
}
