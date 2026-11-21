package dogloverpink;

import dogloverpink.events.PlayerCommandExecute;
import dogloverpink.events.ServerCommandExecute;
import dogloverpink.events.TabEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class EssentialsXTargetSelectors extends JavaPlugin {
   private int pluginExtensions;

   @EventHandler
   public void onEnable() {
      this.pluginExtensions = this.registerPluginExtensions();
      this.getServer().getPluginManager().registerEvents(new PlayerCommandExecute(), this);
      this.getServer().getPluginManager().registerEvents(new ServerCommandExecute(), this);
      this.getServer().getPluginManager().registerEvents(new TabEvent(), this);
      PlayerCommandExecute.plugin = this;
   }
}
