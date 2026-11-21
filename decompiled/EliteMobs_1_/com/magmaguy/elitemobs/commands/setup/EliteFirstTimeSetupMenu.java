package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.ReloadCommand;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.magmacore.menus.FirstTimeSetupMenu;
import com.magmaguy.elitemobs.magmacore.menus.MenuButton;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class EliteFirstTimeSetupMenu {
   public static void createMenu(Player player) {
      new FirstTimeSetupMenu(
         MetadataHandler.PLUGIN,
         player,
         InitializeConfig.getMenuTitle(),
         InitializeConfig.getMenuSubtitle(),
         createInfoItem(),
         List.of(createRecommendedItem(), createNothingItem(), createContentOnlyItem())
      );
   }

   private static MenuButton createInfoItem() {
      return new MenuButton(
         ItemStackGenerator.generateSkullItemStack(
            "magmaguy",
            InitializeConfig.getInfoButtonName(),
            List.of(InitializeConfig.getInfoButtonLore1(), InitializeConfig.getInfoButtonLore2(), InitializeConfig.getInfoButtonLore3())
         )
      ) {
         @Override
         public void onClick(Player player) {
            player.closeInventory();
            Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
            EliteFirstTimeSetupMenu.sendLink(
               player,
               InitializeConfig.getInfoButtonClickSetupLink(),
               InitializeConfig.getContentLinkDisplay(),
               InitializeConfig.getContentLinkHover(),
               "https://nightbreak.io/plugin/elitemobs/#setup"
            );
            player.spigot()
               .sendMessage(
                  new BaseComponent[]{
                     SpigotMessage.simpleMessage(InitializeConfig.getInfoButtonClickContentBrowse()),
                     SpigotMessage.commandHoverMessage(InitializeConfig.getEmSetupDisplay(), InitializeConfig.getEmSetupHover(), "/em setup"),
                     SpigotMessage.simpleMessage(InitializeConfig.getInfoButtonClickContentBrowseSuffix()),
                     SpigotMessage.hoverLinkMessage(
                        InitializeConfig.getContentLinkDisplay(), InitializeConfig.getContentLinkHover(), "https://nightbreak.io/plugin/elitemobs/"
                     )
                  }
               );
            EliteFirstTimeSetupMenu.sendLink(
               player,
               InitializeConfig.getInfoButtonClickDiscord(),
               InitializeConfig.getDiscordLinkDisplay(),
               InitializeConfig.getDiscordLinkHover(),
               "https://discord.gg/eSxvPbWYy4"
            );
            Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
         }
      };
   }

   private static MenuButton createRecommendedItem() {
      return new MenuButton(
         ItemStackGenerator.generateItemStack(
            Material.GREEN_STAINED_GLASS_PANE,
            InitializeConfig.getRecommendedPresetName(),
            List.of(
               InitializeConfig.getRecommendedPresetLore1(),
               InitializeConfig.getRecommendedPresetLore2(),
               InitializeConfig.getRecommendedPresetLore3(),
               InitializeConfig.getRecommendedPresetLore4(),
               InitializeConfig.getRecommendedPresetLore5()
            )
         )
      ) {
         @Override
         public void onClick(Player player) {
            player.closeInventory();
            DefaultConfig.toggleSetupDone(true);
            MobCombatSettingsConfig.toggleNaturalMobSpawning(true);
            SkillsConfig.toggleSkillSystem(true);
            ItemSettingsConfig.toggleEliteMobsLoot(true);
            EventsConfig.setEventsEnabled(true);
            EliteFirstTimeSetupMenu.sendPostClickMessages(
               player,
               InitializeConfig.getRecommendedPresetClickConfirm(),
               InitializeConfig.getRecommendedPresetClickNightbreakLogin(),
               InitializeConfig.getRecommendedPresetClickDownloadAll(),
               InitializeConfig.getRecommendedPresetClickDownloadAllSuffix(),
               InitializeConfig.getRecommendedPresetClickSetupGuide(),
               InitializeConfig.getRecommendedPresetClickDiscord(),
               InitializeConfig.getRecommendedPresetClickWiki()
            );
            ReloadCommand.reload(player);
         }
      };
   }

   private static MenuButton createContentOnlyItem() {
      return new MenuButton(
         ItemStackGenerator.generateItemStack(
            Material.YELLOW_STAINED_GLASS_PANE,
            InitializeConfig.getContentOnlyPresetName(),
            List.of(
               InitializeConfig.getContentOnlyPresetLore1(),
               InitializeConfig.getContentOnlyPresetLore2(),
               InitializeConfig.getContentOnlyPresetLore3(),
               InitializeConfig.getContentOnlyPresetLore4(),
               InitializeConfig.getContentOnlyPresetLore5()
            )
         )
      ) {
         @Override
         public void onClick(Player player) {
            player.closeInventory();
            DefaultConfig.toggleSetupDone(true);
            MobCombatSettingsConfig.toggleNaturalMobSpawning(false);
            SkillsConfig.toggleSkillSystem(true);
            ItemSettingsConfig.toggleEliteMobsLoot(true);
            EventsConfig.setEventsEnabled(false);
            EliteFirstTimeSetupMenu.sendPostClickMessages(
               player,
               InitializeConfig.getContentOnlyPresetClickConfirm(),
               InitializeConfig.getContentOnlyPresetClickNightbreakLogin(),
               InitializeConfig.getContentOnlyPresetClickDownloadAll(),
               InitializeConfig.getContentOnlyPresetClickDownloadAllSuffix(),
               InitializeConfig.getContentOnlyPresetClickSetupGuide(),
               InitializeConfig.getContentOnlyPresetClickDiscord(),
               InitializeConfig.getContentOnlyPresetClickWiki()
            );
            ReloadCommand.reload(player);
         }
      };
   }

   private static MenuButton createNothingItem() {
      List<String> lore = new ArrayList<>(
         List.of(
            InitializeConfig.getNothingPresetLore1(),
            InitializeConfig.getNothingPresetLore2(),
            InitializeConfig.getNothingPresetLore3(),
            InitializeConfig.getNothingPresetLore4(),
            InitializeConfig.getNothingPresetLore5()
         )
      );
      String lore6 = InitializeConfig.getNothingPresetLore6();
      if (lore6 != null && !lore6.isEmpty()) {
         lore.add(lore6);
      }

      return new MenuButton(ItemStackGenerator.generateItemStack(Material.RED_STAINED_GLASS_PANE, InitializeConfig.getNothingPresetName(), lore)) {
         @Override
         public void onClick(Player player) {
            player.closeInventory();
            DefaultConfig.toggleSetupDone(true);
            MobCombatSettingsConfig.toggleNaturalMobSpawning(false);
            SkillsConfig.toggleSkillSystem(false);
            ItemSettingsConfig.toggleEliteMobsLoot(false);
            EventsConfig.setEventsEnabled(false);
            Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
            EliteFirstTimeSetupMenu.sendText(player, InitializeConfig.getNothingPresetClickConfirm());
            EliteFirstTimeSetupMenu.sendText(player, InitializeConfig.getNothingPresetClickWarning());
            EliteFirstTimeSetupMenu.sendText(player, InitializeConfig.getNothingPresetClickBetterStructures());
            EliteFirstTimeSetupMenu.sendLink(
               player,
               InitializeConfig.getNothingPresetClickSetupGuide(),
               InitializeConfig.getDiscordLinkDisplay(),
               InitializeConfig.getDiscordLinkHover(),
               "https://discord.gg/eSxvPbWYy4"
            );
            Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
            ReloadCommand.reload(player);
         }
      };
   }

   private static void sendPostClickMessages(
      Player player,
      String confirm,
      String nightbreakLoginPrefix,
      String downloadAllPrefix,
      String downloadAllSuffix,
      String browseContentPrefix,
      String manualInstallPrefix,
      String discordPrefix
   ) {
      Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
      sendText(player, confirm);
      sendLink(
         player, nightbreakLoginPrefix, InitializeConfig.getAccountLinkDisplay(), InitializeConfig.getAccountLinkHover(), "https://nightbreak.io/account/"
      );
      sendCommand(player, downloadAllPrefix, downloadAllSuffix, InitializeConfig.getEmSetupDisplay(), InitializeConfig.getEmSetupHover(), "/em setup");
      sendLink(
         player,
         browseContentPrefix,
         InitializeConfig.getContentLinkDisplay(),
         InitializeConfig.getContentLinkHover(),
         "https://nightbreak.io/plugin/elitemobs/"
      );
      sendLink(
         player, manualInstallPrefix, InitializeConfig.getWikiLinkDisplay(), InitializeConfig.getWikiLinkHover(), "https://wiki.nightbreak.io/EliteMobs/setup/"
      );
      sendLink(player, discordPrefix, InitializeConfig.getDiscordLinkDisplay(), InitializeConfig.getDiscordLinkHover(), "https://discord.gg/eSxvPbWYy4");
      Logger.sendSimpleMessage(player, InitializeConfig.getSeparatorLine());
   }

   private static void sendText(Player player, String msg) {
      if (msg != null && !msg.isEmpty()) {
         Logger.sendSimpleMessage(player, msg);
      }
   }

   private static void sendLink(Player player, String prefix, String display, String hover, String url) {
      if (prefix != null && !prefix.isEmpty()) {
         player.spigot().sendMessage(new BaseComponent[]{SpigotMessage.simpleMessage(prefix), SpigotMessage.hoverLinkMessage(display, hover, url)});
      }
   }

   private static void sendCommand(Player player, String prefix, String suffix, String display, String hover, String command) {
      if (prefix != null && !prefix.isEmpty()) {
         player.spigot()
            .sendMessage(
               new BaseComponent[]{
                  SpigotMessage.simpleMessage(prefix), SpigotMessage.commandHoverMessage(display, hover, command), SpigotMessage.simpleMessage(suffix)
               }
            );
      }
   }
}
