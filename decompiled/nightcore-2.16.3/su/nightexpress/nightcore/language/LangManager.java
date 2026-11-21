package su.nightexpress.nightcore.language;

import java.io.File;
import java.io.InputStream;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.Engine;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.entry.LangElement;
import su.nightexpress.nightcore.manager.SimpleManager;
import su.nightexpress.nightcore.util.Reflex;
import su.nightexpress.nightcore.util.StringUtil;

@Deprecated
public class LangManager extends SimpleManager<NightCorePlugin> {
   public static final String DEFAULT_LANGUAGE = "en";
   public static final String DIR_LANG = "/lang/";
   private FileConfig config;

   public LangManager(@NonNull NightCorePlugin plugin) {
      super(plugin);
   }

   @Override
   protected void onLoad() {
      this.plugin.extractResources("/lang/");
      String langCode = this.plugin.getLanguage();
      String realCode = this.validateConfig(langCode);
      this.config = FileConfig.loadOrExtract(this.plugin, "/lang/", this.getFileName(realCode));
   }

   @Override
   protected void onShutdown() {
   }

   private boolean isPacked(@NonNull String filePath) {
      if (!filePath.startsWith("/")) {
         filePath = "/" + filePath;
      }

      try {
         boolean var3;
         try (InputStream inputStream = this.plugin.getClass().getResourceAsStream(filePath)) {
            var3 = inputStream != null;
         }

         return var3;
      } catch (Exception var7) {
         return false;
      }
   }

   @NonNull
   private String validateConfig(@NonNull String langCode) {
      String fileName = this.getFileName(langCode);
      File file = new File(this.plugin.getDataFolder() + "/lang/", fileName);
      if (file.exists() || isDefault(langCode)) {
         return langCode;
      } else if (this.isPacked("/lang/" + fileName)) {
         return langCode;
      } else {
         this.plugin.warn("Locale file for '" + langCode + "' language not found. Using default 'en' locale.");
         return "en";
      }
   }

   public void loadEntries(@NonNull Class<?> clazz) {
      Reflex.getStaticFields(clazz, LangElement.class, false).forEach(entry -> entry.load(this.plugin));
   }

   @Deprecated
   public void loadEnum(@NonNull Class<? extends Enum<?>> clazz) {
      for (Object eName : clazz.getEnumConstants()) {
         String name = eName.toString();
         String path = clazz.getSimpleName() + "." + name;
         String val = StringUtil.capitalizeUnderscored(name);
         this.getConfig().addMissing(path, val);
      }
   }

   @Deprecated
   @NonNull
   public String getEnum(@NonNull Enum<?> entry) {
      String path = entry.getDeclaringClass().getSimpleName() + "." + entry.name();
      String locEnum = this.config.getString(path);
      if (locEnum == null && Engine.core() != this.plugin) {
         return Engine.core().getLangManager().getEnum(entry);
      } else {
         return locEnum == null ? StringUtil.capitalizeFully(entry.name()) : locEnum;
      }
   }

   public static boolean isDefault(@NonNull String langCode) {
      return langCode.equalsIgnoreCase("en");
   }

   @NonNull
   public String getFileName(@NonNull String langCode) {
      return "messages_" + langCode + ".yml";
   }

   @NonNull
   public FileConfig getConfig() {
      return this.config;
   }
}
