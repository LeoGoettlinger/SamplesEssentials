package de.oliver.fancynpcs.commands;

import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.fancylib.translations.Language;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancylib.translations.message.SimpleMessage;
import de.oliver.fancynpcs.FancyNpcs;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class FancyNpcsCMD {
   public static final FancyNpcsCMD INSTANCE = new FancyNpcsCMD();
   private final FancyNpcs plugin = FancyNpcs.getInstance();
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   private FancyNpcsCMD() {
   }

   @Command("fancynpcs version")
   @Permission({"fancynpcs.command.fancynpcs.version"})
   public void onVersion(final CommandSender sender) {
      this.plugin.getVersionConfig().checkVersionAndDisplay(sender, false);
   }

   @Command("fancynpcs reload")
   @Permission({"fancynpcs.command.fancynpcs.reload"})
   public void onReload(final CommandSender sender) {
      this.translator.loadLanguages(this.plugin.getDataFolder().getAbsolutePath());
      this.plugin.getFancyNpcConfig().reload();
      Language selectedLanguage = this.translator
         .getLanguages()
         .stream()
         .filter(language -> language.getLanguageName().equals(this.plugin.getFancyNpcConfig().getLanguage()))
         .findFirst()
         .orElse(this.translator.getFallbackLanguage());
      this.translator.setSelectedLanguage(selectedLanguage);
      this.plugin.getNpcManagerImpl().reloadNpcs();
      this.translator.translate("fancynpcs_reload_success").withPrefix().send(sender);
   }

   @Command("fancynpcs save")
   @Permission({"fancynpcs.command.fancynpcs.save"})
   public void onSave(final CommandSender sender) {
      this.plugin.getNpcManagerImpl().saveNpcs(true);
      this.translator.translate("fancynpcs_save_success").withPrefix().send(sender);
   }

   @Command("fancynpcs feature_flags")
   @Permission({"fancynpcs.command.fancynpcs.feature_flags"})
   public void onFeatureFlags(final CommandSender sender) {
      this.translator.translate("fancynpcs_feature_flags_header").send(sender);
      this.translator
         .translate("fancynpcs_feature_flags_entry")
         .replace("number", "1")
         .replace("name", "Player NPCs")
         .replace("id", FancyNpcs.PLAYER_NPCS_FEATURE_FLAG.getName())
         .replace("state", this.getTranslatedState(FancyNpcs.PLAYER_NPCS_FEATURE_FLAG.isEnabled()))
         .send(sender);
      this.translator
         .translate("fancynpcs_feature_flags_entry")
         .replace("number", "2")
         .replace("name", "Native threads")
         .replace("id", FancyNpcs.USE_NATIVE_THREADS_FEATURE_FLAG.getName())
         .replace("state", this.getTranslatedState(FancyNpcs.USE_NATIVE_THREADS_FEATURE_FLAG.isEnabled()))
         .send(sender);
      this.translator
         .translate("fancynpcs_feature_flags_entry")
         .replace("number", "3")
         .replace("name", "Debug mode")
         .replace("id", FancyNpcs.ENABLE_DEBUG_MODE_FEATURE_FLAG.getName())
         .replace("state", this.getTranslatedState(FancyNpcs.ENABLE_DEBUG_MODE_FEATURE_FLAG.isEnabled()))
         .send(sender);
      this.translator
         .translate("fancynpcs_feature_flags_footer")
         .replace("count", "2")
         .replace("count_formatted", "· · 3")
         .replace("total", String.valueOf(FancyNpcs.getInstance().getNpcManager().getAllNpcs().size()))
         .replace("total_formatted", "· · 3")
         .send(sender);
   }

   @NotNull
   private String getTranslatedState(final boolean bool) {
      return bool ? ((SimpleMessage)this.translator.translate("enabled")).getMessage() : ((SimpleMessage)this.translator.translate("disabled")).getMessage();
   }
}
