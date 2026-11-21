package su.nightexpress.nightcore.core.tag;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCore;
import su.nightexpress.nightcore.bridge.text.NightTextDecoration;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.night.tag.TagHandlerRegistry;
import su.nightexpress.nightcore.util.text.night.tag.handler.ClickTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.ColorTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.DecorationTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.FontTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.GradientTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.HeadTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.HoverTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.InsertionTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.KeybindTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.LangTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.PlaceholderTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.ResetTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.ShadowOffTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.ShadowTagHandler;
import su.nightexpress.nightcore.util.text.night.tag.handler.SpriteTagHandler;
import su.nightexpress.nightcore.util.text.tag.Tags;

public class TagManager extends AbstractManager<NightCore> {
   public static final String COLORS_FILE = FileConfig.withExtension("color_schemes");
   private final Map<String, ColorScheme> colorSchemeByIdMap = new HashMap<>();
   private ColorScheme colorScheme;

   public TagManager(@NonNull NightCore plugin) {
      super(plugin);
   }

   @Override
   protected void onLoad() {
      this.loadColorSchemes();
      this.loadColorTags();
      this.loadTags();
   }

   @Override
   protected void onShutdown() {
      if (this.colorScheme != null) {
         this.colorScheme.getColors().forEach(code -> TagHandlerRegistry.unregister(code.name()));
         this.colorScheme = null;
      }

      TagHandlerRegistry.clear();
   }

   public void loadTags() {
      TagHandlerRegistry.register(ColorTagHandler::new, "c", "color", "colour");
      TagHandlerRegistry.register(GradientTagHandler::new, "gradient");
      TagHandlerRegistry.register(ShadowTagHandler::new, "shadow");
      TagHandlerRegistry.register(ShadowOffTagHandler::new, "!shadow");
      TagHandlerRegistry.register(FontTagHandler::new, "font");
      TagHandlerRegistry.register(LangTagHandler::new, "lang", "lang_or", "tr", "translate", "tr_or", "translate_or");
      TagHandlerRegistry.register(HoverTagHandler::new, "hover");
      TagHandlerRegistry.register(ClickTagHandler::new, "click");
      TagHandlerRegistry.register(ResetTagHandler::new, "r", "reset");
      TagHandlerRegistry.register(KeybindTagHandler::new, "key");
      TagHandlerRegistry.register(InsertionTagHandler::new, "insertion");
      TagHandlerRegistry.register(SpriteTagHandler::new, "sprite");
      TagHandlerRegistry.register(HeadTagHandler::new, "head");
      TagHandlerRegistry.register(() -> DecorationTagHandler.normal(NightTextDecoration.BOLD, true), "b", "bold");
      TagHandlerRegistry.register(() -> DecorationTagHandler.normal(NightTextDecoration.ITALIC, true), "i", "italic", "em");
      TagHandlerRegistry.register(() -> DecorationTagHandler.normal(NightTextDecoration.OBFUSCATED, true), "obf", "obfuscated");
      TagHandlerRegistry.register(() -> DecorationTagHandler.normal(NightTextDecoration.STRIKETHROUGH, true), "st", "strikethrough");
      TagHandlerRegistry.register(() -> DecorationTagHandler.normal(NightTextDecoration.UNDERLINED, true), "u", "underlined");
      TagHandlerRegistry.register(() -> DecorationTagHandler.normal(NightTextDecoration.BOLD, false), "!b", "!bold");
      TagHandlerRegistry.register(() -> DecorationTagHandler.normal(NightTextDecoration.ITALIC, false), "!i", "!italic", "!em");
      TagHandlerRegistry.register(() -> DecorationTagHandler.normal(NightTextDecoration.OBFUSCATED, false), "!obf", "!obfuscated");
      TagHandlerRegistry.register(() -> DecorationTagHandler.normal(NightTextDecoration.STRIKETHROUGH, false), "!st", "!strikethrough");
      TagHandlerRegistry.register(() -> DecorationTagHandler.normal(NightTextDecoration.UNDERLINED, false), "!u", "!underlined");
      TagHandlerRegistry.register(() -> new PlaceholderTagHandler("\n"), "br", "newline");
   }

   public void loadColorTags() {
      if (this.colorScheme != null) {
         this.colorScheme.getColors().forEach(code -> TagHandlerRegistry.register(code::createHandler, code.name()));
         Tags.registerFromScheme(this.colorScheme);
      }
   }

   public void loadColorSchemes() {
      FileConfig config = FileConfig.loadOrExtract(this.plugin, COLORS_FILE);
      String schemeId = ConfigValue.create("Selected", "custom").read(config);
      String path = "ColorSchemes";
      if (config.getSection(path).isEmpty()) {
         ColorScheme.getDefaultSchemes().forEach(scheme -> scheme.write(config, path + "." + scheme.getId()));
      }

      this.migrateOldSettings(config);
      config.getSection(path).forEach(sId -> {
         ColorScheme scheme = ColorScheme.read(config, path + "." + sId, sId);
         this.colorSchemeByIdMap.put(scheme.getId(), scheme);
      });
      config.saveChanges();
      this.plugin.info("Loaded " + this.colorSchemeByIdMap.size() + " color schemes.");
      this.setColorScheme(schemeId);
   }

   public void setColorScheme(@NonNull String name) {
      ColorScheme scheme = this.getColorScheme(name);
      if (scheme == null) {
         if (!name.equalsIgnoreCase("default")) {
            this.plugin.error("Color scheme '" + name + "' not found. Try fallback to the 'default' one...");
            this.setColorScheme("default");
         } else {
            this.plugin.error("No default color scheme was found. Color tags will be unavailable.");
         }
      } else {
         this.colorScheme = scheme;
         this.plugin.info("Using '" + this.colorScheme.getId() + "' color scheme.");
      }
   }

   private void migrateOldSettings(@NonNull FileConfig target) {
      File fromFile = new File(this.plugin.getDataFolder(), "colors.yml");
      if (fromFile.exists()) {
         FileConfig from = new FileConfig(fromFile);
         if (from.contains("Colors")) {
            ColorScheme scheme = ColorScheme.read(from, "", "custom");
            scheme.getColors()
               .forEach(code -> target.set("ColorSchemes." + scheme.getId() + ".Colors." + code.name(), ParserUtils.colorToHexString(code.color())));
            fromFile.renameTo(new File(this.plugin.getDataFolder(), "colors.old"));
         }
      }
   }

   @NonNull
   public Map<String, ColorScheme> getColorSchemeByIdMap() {
      return this.colorSchemeByIdMap;
   }

   @Nullable
   public ColorScheme getColorScheme(@NonNull String id) {
      return this.colorSchemeByIdMap.get(id.toLowerCase());
   }

   @NonNull
   public Set<ColorScheme> getColorSchemes() {
      return new HashSet<>(this.colorSchemeByIdMap.values());
   }

   @NonNull
   public Optional<ColorScheme> getColorScheme() {
      return Optional.ofNullable(this.colorScheme);
   }
}
