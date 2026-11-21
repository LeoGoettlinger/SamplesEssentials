package su.nightexpress.nightcore.userdata;

import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NonNull;

public interface UserLoginListener {
   void onPreLogin(@NonNull AsyncPlayerPreLoginEvent var1);

   void onJoin(@NonNull PlayerJoinEvent var1);

   void onQuit(@NonNull PlayerQuitEvent var1);
}
