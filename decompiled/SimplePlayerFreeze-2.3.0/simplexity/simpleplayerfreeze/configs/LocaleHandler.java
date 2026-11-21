package simplexity.simpleplayerfreeze.configs;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import simplexity.simpleplayerfreeze.SimplePlayerFreeze;

public class LocaleHandler {
   private static LocaleHandler instance;
   private final String fileName = "locale.yml";
   private final File localeFile = new File(SimplePlayerFreeze.getInstance().getDataFolder(), "locale.yml");
   private final FileConfiguration localeConfig = new YamlConfiguration();
   private final Logger logger = SimplePlayerFreeze.getInstance().getLogger();
   private String prefix;
   private String cannotChat;
   private String cannotUseCommand;
   private String freezeMessage;
   private String unfreezeMessage;
   private String freezeServerMessage;
   private String freezeWorldMessage;
   private String reloadMessage;
   private String haveBeenFrozen;
   private String haveBeenUnfrozen;
   private String noPermission;
   private String noPlayer;
   private String loginMessage;
   private String loginNotif;
   private String loginNotifNowUnfrozen;
   private String cannotBeFrozen;
   private String notFrozen;
   private String shadowMuteFormat;
   private String freezeSpyEnabled;
   private String freezeSpyDisabled;
   private String onlyPlayer;
   private String freezeWorldChange;
   private String loginNotifServerFrozen;
   private String worldNotFound;
   private String unfreezeWorldMessage;
   private String unfreezeServerMessage;

   private LocaleHandler() {
      if (!this.localeFile.exists()) {
         SimplePlayerFreeze.getInstance().saveResource("locale.yml", false);
      }
   }

   public static LocaleHandler getInstance() {
      if (instance == null) {
         instance = new LocaleHandler();
      }

      return instance;
   }

   public FileConfiguration getLocaleConfig() {
      return this.localeConfig;
   }

   public void loadLocale() {
      try {
         this.localeConfig.load(this.localeFile);
      } catch (InvalidConfigurationException | IOException var2) {
         this.logger.severe("Issue loading locale.yml");
         var2.printStackTrace();
      }

      this.prefix = this.localeConfig.getString("prefix", "<green><bold>[</bold><yellow>SPF</yellow><bold>]</bold></green> ");
      this.cannotChat = this.localeConfig.getString("cannot-chat", "You are frozen and cannot chat");
      this.cannotUseCommand = this.localeConfig.getString("cannot-use-command", "You are frozen and cannot use that command");
      this.freezeMessage = this.localeConfig.getString("freeze-message", "<green><name></green> <aqua>has been frozen</aqua>");
      this.freezeServerMessage = this.localeConfig.getString("freeze-server-message", "<aqua>The server has been frozen</aqua>");
      this.freezeWorldMessage = this.localeConfig.getString("freeze-world-message", "<aqua>The <green><name></green> world has been frozen<aqua>");
      this.unfreezeMessage = this.localeConfig.getString("unfreeze-message", "<green><name></green> <yellow>has been unfrozen</yellow>");
      this.reloadMessage = this.localeConfig.getString("reload-message", "<gold>The Simple Player Freeze config has been reloaded</gold>");
      this.haveBeenFrozen = this.localeConfig
         .getString("have-been-frozen", "<red><bold>[NOTICE]</bold></red> <gray>You have been frozen. You cannot move, interact, or chat");
      this.haveBeenUnfrozen = this.localeConfig
         .getString("have-been-unfrozen", "<green><bold>[NOTICE]</bold></green> <gray>You have been unfrozen. You can move, interact, and chat again.");
      this.noPermission = this.localeConfig.getString("no-permission", "<red>You do not have permission to run this command!</red>");
      this.noPlayer = this.localeConfig.getString("no-player", "<red>You did not provide a valid player, please check your spelling and try again</red>");
      this.loginMessage = this.localeConfig
         .getString("login-message", "<red><bold>[NOTICE]</bold></red> <gray>You were frozen during a previous session. You cannot move, interact, or chat");
      this.loginNotif = this.localeConfig
         .getString("login-notif", "<dark_gray><bold><name></bold></dark_gray> <gray>has logged in. They were frozen during a previous session.");
      this.loginNotifNowUnfrozen = this.localeConfig
         .getString(
            "login-notif-now-unfrozen",
            "<dark_gray><bold><name></bold></dark_gray> <gray>has logged in. They were frozen during a previous session. They will now be unfrozen."
         );
      this.cannotBeFrozen = this.localeConfig.getString("cannot-be-frozen", "<red>That player cannot be frozen!</red>");
      this.notFrozen = this.localeConfig.getString("not-frozen", "<red>That player is not frozen!</red>");
      this.shadowMuteFormat = this.localeConfig.getString("shadow-mute-format", "<dark_gray>[Frozen]</dark_gray> <gray><player>: <message></gray>");
      this.freezeSpyEnabled = this.localeConfig.getString("freeze-spy-enabled", "<green>Freeze Spy has been toggled <bold><yellow>On</yellow></bold>!</green>");
      this.freezeSpyDisabled = this.localeConfig.getString("freeze-spy-disabled", "<grey>Freeze Spy has been toggled <bold><red>Off</red></bold>!</grey>");
      this.onlyPlayer = this.localeConfig.getString("only-player", "<red>Sorry! Only a player can run that command</red>");
      this.freezeWorldChange = this.localeConfig
         .getString(
            "freeze-world-change",
            "<dark_gray><bold><name></bold></dark_gray> <gray>has switched to <world>. Since that world is frozen, they will now be frozen"
         );
      this.loginNotifServerFrozen = this.localeConfig
         .getString(
            "login-notif-server-frozen",
            "<dark_gray><bold><name></bold></dark_gray> <gray>has logged in. Since the server is currently frozen, they will now be frozen"
         );
      this.worldNotFound = this.localeConfig
         .getString("world-not-found", "<red>The world <yellow><name></yellow> could not be found. Please check your spelling and try again</red>");
      this.unfreezeWorldMessage = this.localeConfig.getString("unfreeze-world-message", "<yellow>The <green><world></green> world has been unfrozen</yellow>");
      this.unfreezeServerMessage = this.localeConfig.getString("unfreeze-server-message", "<yellow>The server has been frozen</yellow>");
   }

   public String getCannotChat() {
      return this.cannotChat;
   }

   public String getCannotUseCommand() {
      return this.cannotUseCommand;
   }

   public String getFreezeMessage() {
      return this.freezeMessage;
   }

   public String getUnfreezeMessage() {
      return this.unfreezeMessage;
   }

   public String getReloadMessage() {
      return this.reloadMessage;
   }

   public String getHaveBeenFrozen() {
      return this.haveBeenFrozen;
   }

   public String getHaveBeenUnfrozen() {
      return this.haveBeenUnfrozen;
   }

   public String getNoPermission() {
      return this.noPermission;
   }

   public String getNoPlayer() {
      return this.noPlayer;
   }

   public String getLoginMessage() {
      return this.loginMessage;
   }

   public String getLoginNotif() {
      return this.loginNotif;
   }

   public String getLoginNotifNowUnfrozen() {
      return this.loginNotifNowUnfrozen;
   }

   public String getCannotBeFrozen() {
      return this.cannotBeFrozen;
   }

   public String getNotFrozen() {
      return this.notFrozen;
   }

   public String getShadowMuteFormat() {
      return this.shadowMuteFormat;
   }

   public String getFreezeSpyEnabled() {
      return this.freezeSpyEnabled;
   }

   public String getFreezeSpyDisabled() {
      return this.freezeSpyDisabled;
   }

   public String getOnlyPlayer() {
      return this.onlyPlayer;
   }

   public String getPrefix() {
      return this.prefix;
   }

   public String getFreezeServerMessage() {
      return this.freezeServerMessage;
   }

   public String getFreezeWorldMessage() {
      return this.freezeWorldMessage;
   }

   public String getFreezeWorldChange() {
      return this.freezeWorldChange;
   }

   public String getLoginNotifServerFrozen() {
      return this.loginNotifServerFrozen;
   }

   public String getWorldNotFound() {
      return this.worldNotFound;
   }

   public String getUnfreezeWorldMessage() {
      return this.unfreezeWorldMessage;
   }

   public String getUnfreezeServerMessage() {
      return this.unfreezeServerMessage;
   }
}
