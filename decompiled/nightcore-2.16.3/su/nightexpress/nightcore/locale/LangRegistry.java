package su.nightexpress.nightcore.locale;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.message.MessageData;
import su.nightexpress.nightcore.manager.SimpleManager;
import su.nightexpress.nightcore.util.FileUtil;
import su.nightexpress.nightcore.util.Reflex;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

public class LangRegistry extends SimpleManager<NightPlugin> {
   public static final String DEFAULT_LANGUAGE = "en";
   public static final String DIRECTORY = "/lang/";
   private final List<LangElement> elements = new ArrayList<>();
   private FileConfig config;

   public LangRegistry(@NonNull NightPlugin plugin) {
      super(plugin);
   }

   @Override
   protected void onLoad() {
   }

   @Override
   protected void onShutdown() {
      this.elements.clear();
   }

   public void complete() {
      if (this.config != null) {
         this.config.saveChanges();
         this.config = null;
      }
   }

   @NonNull
   public static List<LangElement> getElements(@NonNull Class<?> source) {
      return Reflex.getStaticFields(source, LangElement.class, false);
   }

   public static void loadEntries(@NonNull Class<?> source, @NonNull NightPlugin plugin, @NonNull FileConfig config) {
      loadEntries(getElements(source), plugin, config);
   }

   public static void loadEntries(@NonNull Collection<LangElement> elements, @NonNull NightPlugin plugin, @NonNull FileConfig config) {
      elements.forEach(element -> element.load(plugin, config));
   }

   public boolean hasElements() {
      return !this.elements.isEmpty();
   }

   public void register(@NonNull Class<? extends LangContainer> clazz) {
      this.elements.addAll(getElements(clazz));
   }

   public void inject(@NonNull LangContainer langContainer) {
      this.inject((Class<? extends LangContainer>)langContainer.getClass());
   }

   public void inject(@NonNull Class<? extends LangContainer> langClass) {
      if (this.config != null) {
         loadEntries(langClass, this.plugin, this.config);
      } else {
         this.plugin.warn("Lang Container " + langClass.getSimpleName() + " is not injected due to be out of the #enable() phase.");
      }
   }

   public void loadLocale() {
      if (!this.elements.isEmpty()) {
         this.plugin.extractResources("/lang/");
         this.updateLegacy();
         String userLocale = this.plugin.getDetails().getLanguage();
         File file = new File(this.plugin.getDataFolder() + "/lang/", this.getFileName(userLocale));
         boolean isSupportedLocale = file.exists();
         String langCode = isSupportedLocale ? userLocale : "en";
         this.config = FileConfig.loadOrExtract(this.plugin, "/lang/", this.getFileName(langCode));
         if (!isSupportedLocale) {
            this.plugin.warn("Lang file for the '" + userLocale + "' locale does not exist. Will use the '" + langCode + "' one.");
         }

         loadEntries(this.elements, this.plugin, this.config);
      }
   }

   private void updateLegacy() {
      FileUtil.getConfigFiles(this.plugin.getDataFolder() + "/lang/").forEach(messagesFile -> {
         String messagesName = messagesFile.getName();
         if (messagesName.startsWith("messages_")) {
            String langCode = messagesName.substring("messages_".length(), messagesName.length() - 4);
            String dirPath = this.plugin.getDataFolder() + "/lang/";
            File langFile = new File(dirPath, this.getFileName(langCode));
            if (!langFile.exists()) {
               Path originPath = Paths.get(messagesFile.getPath());
               Path backupPath = Paths.get(messagesFile.getPath() + ".backup");
               Path renewPath = Paths.get(dirPath + this.getFileName(langCode));

               try {
                  Files.copy(originPath, backupPath);
                  FileConfig messagesConfig = new FileConfig(messagesFile);
                  this.elements.forEach(element -> {
                     if (element instanceof MessageLocale messageLocale) {
                        List<String> text = new ArrayList<>(messagesConfig.getStringList(messageLocale.getPath()));
                        if (text.isEmpty()) {
                           text.add(messagesConfig.getString(messageLocale.getPath(), ""));
                        }

                        String inline = String.join(TagWrappers.BR, text);
                        if (MessageData.hasLegacyData(inline)) {
                           MessageData.Builder builder = MessageData.builder();
                           String message = MessageData.extractAndParseOld(inline, builder);
                           MessageData data = builder.build();
                           String[] textLines = ParserUtils.breakDownLineSplitters(data.serialize() + message);
                           if (textLines.length == 1) {
                              messagesConfig.set(messageLocale.getPath(), textLines[0]);
                           } else {
                              messagesConfig.set(messageLocale.getPath(), Arrays.asList(textLines));
                           }
                        }
                     }
                  });
                  messagesConfig.saveChanges();
                  Files.move(originPath, renewPath);
               } catch (IOException var10) {
                  var10.printStackTrace();
               }
            }
         }
      });
   }

   @NonNull
   public String getFileName(@NonNull String langCode) {
      return FileConfig.withExtension("lang_" + langCode);
   }
}
