package github.nighter.smartspawner.commands.reload;

import com.mojang.brigadier.context.CommandContext;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.BaseSubCommand;
import github.nighter.smartspawner.config.Config;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ReloadSubCommand extends BaseSubCommand {
   public ReloadSubCommand(SmartSpawner plugin) {
      super(plugin);
   }

   @Override
   public String getName() {
      return "reload";
   }

   @Override
   public String getPermission() {
      return "smartspawner.command.reload";
   }

   @Override
   public String getDescription() {
      return "Reload the plugin configuration and data";
   }

   @Override
   public int execute(CommandContext<CommandSourceStack> context) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      this.reloadAll(sender);
      return 1;
   }

   private void reloadAll(CommandSender sender) {
      try {
         this.plugin.getMessageService().sendMessage(sender, "reload.start");
         this.plugin.getSpawnerItemFactory().clearAllCaches();
         this.plugin.getMessageService().clearKeyExistsCache();
         this.plugin.reloadConfig();
         Config.reload(this.plugin);
         this.plugin.setUpHopperHandler();
         this.plugin.getItemPriceManager().reload();
         this.plugin.getSpawnerSettingsConfig().reload();
         this.plugin.getItemSpawnerSettingsConfig().reload();
         this.plugin.getSpawnerManager().reloadSpawnerDropsAndConfigs();
         this.plugin.getLanguageManager().reloadLanguages();
         this.plugin.getGuiLayoutConfig().loadLayout();
         this.plugin.getGuiButtonInteractionService().clear();
         this.plugin.getSpawnerMenuUI().loadConfig();
         if (this.plugin.getSpawnerClickManager() != null) {
            this.plugin.getSpawnerClickManager().loadConfig();
         }

         this.plugin.getSpawnerExplosionListener().loadConfig();
         this.plugin.getSpawnerGuiViewManager().recheckTimerPlaceholders();
         this.plugin.getSpawnerItemFactory().reload();
         this.plugin.getSpawnerManager().reloadAllHolograms();
         this.plugin.reload();
         if (this.plugin.getSpawnerStorage() != null) {
            this.plugin.getSpawnerStorage().reloadSettings();
         }

         this.plugin.getMessageService().sendMessage(sender, "reload.success");
      } catch (Exception var3) {
         this.plugin.getLogger().severe("Error during reload: " + var3.getMessage());
         var3.printStackTrace();
         this.plugin.getMessageService().sendMessage(sender, "reload.error");
      }
   }
}
