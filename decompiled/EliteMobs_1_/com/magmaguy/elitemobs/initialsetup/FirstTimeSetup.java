package com.magmaguy.elitemobs.initialsetup;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FirstTimeSetup implements Listener {
   private static final FirstTimeSetup.LanguageEntry[] LANGUAGES = new FirstTimeSetup.LanguageEntry[]{
      new FirstTimeSetup.LanguageEntry("en", "english", "English", "Switch EliteMobs to English"),
      new FirstTimeSetup.LanguageEntry("es", "spanish", "Español", "Cambiar EliteMobs a Español"),
      new FirstTimeSetup.LanguageEntry("fr", "french", "Français", "Passer EliteMobs en Français"),
      new FirstTimeSetup.LanguageEntry("de", "german", "Deutsch", "EliteMobs auf Deutsch umstellen"),
      new FirstTimeSetup.LanguageEntry("it", "italian", "Italiano", "Cambiare EliteMobs in Italiano"),
      new FirstTimeSetup.LanguageEntry("pt_br", "portugueseBrazilian", "Português (BR)", "Mudar EliteMobs para Português (BR)"),
      new FirstTimeSetup.LanguageEntry("pt", "portuguese", "Português", "Mudar EliteMobs para Português"),
      new FirstTimeSetup.LanguageEntry("ru", "russian", "Русский", "Переключить EliteMobs на Русский"),
      new FirstTimeSetup.LanguageEntry("zh_cn", "chineseSimplified", "简体中文", "将 EliteMobs 切换为简体中文"),
      new FirstTimeSetup.LanguageEntry("zh_tw", "chineseTraditional", "繁體中文", "將 EliteMobs 切換為繁體中文"),
      new FirstTimeSetup.LanguageEntry("ja", "japanese", "日本語", "EliteMobs を日本語に切り替える"),
      new FirstTimeSetup.LanguageEntry("ko", "korean", "한국어", "EliteMobs를 한국어로 전환"),
      new FirstTimeSetup.LanguageEntry("pl", "polish", "Polski", "Zmień EliteMobs na Polski"),
      new FirstTimeSetup.LanguageEntry("nl", "dutch", "Nederlands", "EliteMobs naar Nederlands schakelen"),
      new FirstTimeSetup.LanguageEntry("cs", "czech", "Čeština", "Přepnout EliteMobs do Češtiny"),
      new FirstTimeSetup.LanguageEntry("hu", "hungarian", "Magyar", "EliteMobs átváltása Magyarra"),
      new FirstTimeSetup.LanguageEntry("ro", "romanian", "Română", "Schimbă EliteMobs în Română"),
      new FirstTimeSetup.LanguageEntry("tr", "turkish", "Türkçe", "EliteMobs'u Türkçeye değiştir"),
      new FirstTimeSetup.LanguageEntry("vi", "vietnamese", "Tiếng Việt", "Chuyển EliteMobs sang Tiếng Việt"),
      new FirstTimeSetup.LanguageEntry("id", "indonesian", "Indonesia", "Ubah EliteMobs ke Bahasa Indonesia")
   };

   private static String resolveLocaleToLanguage(String locale) {
      if (locale.equals("pt_br")) {
         return "portugueseBrazilian";
      } else if (locale.equals("zh_cn")) {
         return "chineseSimplified";
      } else if (locale.equals("zh_tw")) {
         return "chineseTraditional";
      } else {
         String prefix = locale.contains("_") ? locale.substring(0, locale.indexOf(95)) : locale;
         switch (prefix) {
            case "es":
               return "spanish";
            case "fr":
               return "french";
            case "de":
               return "german";
            case "it":
               return "italian";
            case "pt":
               return "portuguese";
            case "ru":
               return "russian";
            case "ja":
               return "japanese";
            case "ko":
               return "korean";
            case "pl":
               return "polish";
            case "nl":
               return "dutch";
            case "cs":
               return "czech";
            case "hu":
               return "hungarian";
            case "ro":
               return "romanian";
            case "tr":
               return "turkish";
            case "vi":
               return "vietnamese";
            case "en":
               return "english";
            case "id":
               return "indonesian";
            default:
               return null;
         }
      }
   }

   @EventHandler
   public void onPlayerLogin(final PlayerJoinEvent event) {
      if (!DefaultConfig.isSetupDone()) {
         if (event.getPlayer().hasPermission("elitemobs.*")) {
            (new BukkitRunnable() {
                  public void run() {
                     if (event.getPlayer().isOnline()) {
                        Logger.sendSimpleMessage(event.getPlayer(), InitializeConfig.getSeparatorLine());

                        for (String msg : new String[]{InitializeConfig.getLoginHeader(), InitializeConfig.getLoginWelcome()}) {
                           if (msg != null && !msg.isEmpty()) {
                              Logger.sendSimpleMessage(event.getPlayer(), msg);
                           }
                        }

                        String helpHint = InitializeConfig.getLoginHelpHint();
                        if (helpHint != null && !helpHint.isEmpty()) {
                           event.getPlayer()
                              .spigot()
                              .sendMessage(
                                 new BaseComponent[]{
                                    SpigotMessage.simpleMessage(helpHint),
                                    SpigotMessage.commandHoverMessage(
                                       InitializeConfig.getEmInitializeDisplay(), InitializeConfig.getEmInitializeHover(), "/em initialize"
                                    ),
                                    SpigotMessage.simpleMessage(InitializeConfig.getLoginHelpHintSuffix())
                                 }
                              );
                        }

                        String discordPrefix = InitializeConfig.getLoginDiscord();
                        if (discordPrefix != null && !discordPrefix.isEmpty()) {
                           event.getPlayer()
                              .spigot()
                              .sendMessage(
                                 new BaseComponent[]{
                                    SpigotMessage.simpleMessage(discordPrefix),
                                    SpigotMessage.hoverLinkMessage(
                                       InitializeConfig.getDiscordLinkDisplay(), InitializeConfig.getDiscordLinkHover(), "https://discord.gg/eSxvPbWYy4"
                                    )
                                 }
                              );
                        }

                        String dismissMsg = InitializeConfig.getLoginDismiss();
                        if (dismissMsg != null && !dismissMsg.isEmpty()) {
                           Logger.sendSimpleMessage(event.getPlayer(), dismissMsg);
                        }

                        FirstTimeSetup.this.sendLanguageSelector(event.getPlayer());
                        Logger.sendSimpleMessage(event.getPlayer(), InitializeConfig.getSeparatorLine());
                     }
                  }
               })
               .runTaskLater(MetadataHandler.PLUGIN, 200L);
         }
      }
   }

   private void sendLanguageSelector(Player player) {
      String locale = player.getLocale().toLowerCase();
      String suggestedInternalName = resolveLocaleToLanguage(locale);
      String activeInternalName = DefaultConfig.getLanguage();
      List<BaseComponent> components = new ArrayList<>();
      components.add(SpigotMessage.simpleMessage(InitializeConfig.getLoginLanguagePrefix()));

      for (int i = 0; i < LANGUAGES.length; i++) {
         FirstTimeSetup.LanguageEntry entry = LANGUAGES[i];
         String displayName;
         if (entry.internalName.equals(activeInternalName)) {
            displayName = "<g:#228B22:#32CD32>" + entry.nativeDisplay + "</g>";
         } else if (entry.internalName.equals(suggestedInternalName)) {
            displayName = "<g:#DAA520:#F0C040>" + entry.nativeDisplay + "</g>";
         } else {
            displayName = "&7" + entry.nativeDisplay;
         }

         String hoverText = entry.nativeHoverText;
         components.add(SpigotMessage.commandHoverMessage(displayName, hoverText, "/em language " + entry.internalName));
         if (i < LANGUAGES.length - 1) {
            components.add(SpigotMessage.simpleMessage("&8 · "));
         }
      }

      player.spigot().sendMessage(components.toArray(new BaseComponent[0]));
   }

   private static class LanguageEntry {
      final String localePrefix;
      final String internalName;
      final String nativeDisplay;
      final String nativeHoverText;

      LanguageEntry(String localePrefix, String internalName, String nativeDisplay, String nativeHoverText) {
         this.localePrefix = localePrefix;
         this.internalName = internalName;
         this.nativeDisplay = nativeDisplay;
         this.nativeHoverText = nativeHoverText;
      }
   }
}
