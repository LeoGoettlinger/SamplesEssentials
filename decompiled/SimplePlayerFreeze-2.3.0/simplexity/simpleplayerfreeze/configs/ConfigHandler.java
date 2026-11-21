package simplexity.simpleplayerfreeze.configs;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import simplexity.simpleplayerfreeze.SimplePlayerFreeze;

public class ConfigHandler {
   private static ConfigHandler instance;
   private String placeholderAPIFormat;
   private boolean freezePersist;
   private boolean freezeGlow;
   private boolean freezeDismount;
   private boolean freezeFlight;
   private boolean freezeInvulnerability;
   private boolean preventMovement;
   private boolean preventInteract;
   private boolean preventCrafting;
   private boolean preventXPPickup;
   private boolean preventItemPickup;
   private boolean preventItemDrop;
   private boolean preventItemUse;
   private boolean preventHotbarSwitch;
   private boolean preventInventoryInteraction;
   private boolean preventInventoryOpen;
   private boolean preventWalking;
   private boolean preventCommands;
   private boolean preventAttack;
   private boolean consoleSeesMutedMessages;
   private boolean consoleNotify;
   private boolean preventJumping;
   private boolean freezeNewLogins;
   private boolean freezeWorldChange;
   private boolean hideFromDiscordSrv;
   private ChatBehavior chatBehavior;
   private final ArrayList<String> whitelistedCommandList = new ArrayList<>();

   private ConfigHandler() {
   }

   public static ConfigHandler getInstance() {
      if (instance == null) {
         instance = new ConfigHandler();
      }

      return instance;
   }

   public void reloadConfigSettings() {
      SimplePlayerFreeze.getInstance().reloadConfig();
      LocaleHandler.getInstance().loadLocale();
      this.reloadConfigBooleans();
      this.reloadMessages();
      this.reloadConfigCommands();
      this.reloadConfigIntegers();
   }

   private void reloadMessages() {
      FileConfiguration config = SimplePlayerFreeze.getInstance().getConfig();
      this.placeholderAPIFormat = config.getString("placeholder-api-format");
   }

   private void reloadConfigBooleans() {
      FileConfiguration config = SimplePlayerFreeze.getInstance().getConfig();
      this.freezePersist = config.getBoolean("freeze-persist", true);
      this.freezeGlow = config.getBoolean("freeze-glow", true);
      this.freezeDismount = config.getBoolean("freeze-dismount", true);
      this.freezeFlight = config.getBoolean("freeze-flight", false);
      this.freezeInvulnerability = config.getBoolean("freeze-invulnerability", true);
      this.preventWalking = config.getBoolean("prevent-walking", true);
      this.preventMovement = config.getBoolean("prevent-movement", true);
      this.preventJumping = config.getBoolean("prevent-jumping", true);
      this.preventInteract = config.getBoolean("prevent-interact", true);
      this.preventCrafting = config.getBoolean("prevent-crafting", true);
      this.preventXPPickup = config.getBoolean("prevent-xp-pickup", true);
      this.preventItemPickup = config.getBoolean("prevent-item-pickup", true);
      this.preventItemDrop = config.getBoolean("prevent-item-drop", true);
      this.preventItemUse = config.getBoolean("prevent-item-use", true);
      this.preventHotbarSwitch = config.getBoolean("prevent-hotbar-switch", true);
      this.preventCommands = config.getBoolean("prevent-commands", true);
      this.preventAttack = config.getBoolean("prevent-attack", true);
      this.preventInventoryInteraction = config.getBoolean("prevent-inventory-interaction", true);
      this.preventInventoryOpen = config.getBoolean("prevent-inventory-open", true);
      this.consoleSeesMutedMessages = config.getBoolean("console-sees-muted-messages", true);
      this.consoleNotify = config.getBoolean("console-notify", true);
      this.freezeNewLogins = config.getBoolean("freeze-new-logins", true);
      this.freezeWorldChange = config.getBoolean("freeze-world-change", true);
      this.hideFromDiscordSrv = config.getBoolean("hide-from-discord", true);
   }

   private void reloadConfigCommands() {
      FileConfiguration config = SimplePlayerFreeze.getInstance().getConfig();
      this.whitelistedCommandList.clear();
      List<String> commandList = config.getStringList("whitelisted-commands");
      this.whitelistedCommandList.addAll(commandList);
   }

   private void reloadConfigIntegers() {
      FileConfiguration config = SimplePlayerFreeze.getInstance().getConfig();
      int chatBehaviorInt = config.getInt("chat-behavior");
      if (ChatBehavior.getBehaviorFromInt(chatBehaviorInt) == null) {
         SimplePlayerFreeze.getInstance().getLogger().warning("Chat behavior value is invalid. Defaulting to NO_CHANGE.");
         this.chatBehavior = ChatBehavior.NO_CHANGE;
      } else {
         this.chatBehavior = ChatBehavior.getBehaviorFromInt(chatBehaviorInt);
      }
   }

   @NotNull
   public ChatBehavior getChatBehavior() {
      return this.chatBehavior;
   }

   @NotNull
   public ArrayList<String> getWhitelistedCommandList() {
      return this.whitelistedCommandList;
   }

   public boolean shouldFreezePersist() {
      return this.freezePersist;
   }

   public boolean shouldFreezeGlow() {
      return this.freezeGlow;
   }

   public boolean shouldFreezeDismount() {
      return this.freezeDismount;
   }

   public boolean shouldFreezeFlight() {
      return this.freezeFlight;
   }

   public boolean shouldFreezeGiveInvulnerability() {
      return this.freezeInvulnerability;
   }

   public boolean shouldPreventMovement() {
      return this.preventMovement;
   }

   public boolean shouldPreventInteract() {
      return this.preventInteract;
   }

   public boolean shouldPreventCrafting() {
      return this.preventCrafting;
   }

   public boolean shouldPreventXPPickup() {
      return this.preventXPPickup;
   }

   public boolean shouldPreventItemPickup() {
      return this.preventItemPickup;
   }

   public boolean shouldPreventItemDrop() {
      return this.preventItemDrop;
   }

   public boolean shouldPreventItemUse() {
      return this.preventItemUse;
   }

   public boolean shouldPreventHotbarSwitch() {
      return this.preventHotbarSwitch;
   }

   public boolean shouldPreventInventoryInteraction() {
      return this.preventInventoryInteraction;
   }

   public boolean shouldPreventWalking() {
      return this.preventWalking;
   }

   public boolean shouldPreventCommands() {
      return this.preventCommands;
   }

   public boolean shouldConsoleSeesMutedMessages() {
      return this.consoleSeesMutedMessages;
   }

   public boolean shouldConsoleBeNotified() {
      return this.consoleNotify;
   }

   public String getPlaceholderAPIFormat() {
      return this.placeholderAPIFormat;
   }

   public boolean shouldPreventJumping() {
      return this.preventJumping;
   }

   public boolean shouldPreventAttack() {
      return this.preventAttack;
   }

   public boolean shouldPreventInventoryOpen() {
      return this.preventInventoryOpen;
   }

   public boolean shouldFreezeNewLogins() {
      return this.freezeNewLogins;
   }

   public boolean shouldFreezeWorldChange() {
      return this.freezeWorldChange;
   }

   public boolean shouldHideFromDiscordSrv() {
      return this.hideFromDiscordSrv;
   }
}
