package dev.vatten.baserad;

import dev.vatten.baserad.commands.Command;
import dev.vatten.baserad.commands.TagsCommand;
import dev.vatten.baserad.configs.PluginConfig;
import dev.vatten.baserad.configs.TagsConfig;
import dev.vatten.baserad.events.PlayerLoadInEvent;
import dev.vatten.baserad.interfaces.Interfaces;
import io.github.miniplaceholders.api.Expansion;
import java.nio.file.Path;
import java.util.function.Consumer;
import lombok.Generated;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.title.Title;

public class Plugin extends VattenPlugin {
   private MiniMessage MINIMESSAGE = null;
   private ConfigInstance<PluginConfig> PLUGIN_CONFIG;
   private ConfigInstance<TagsConfig> TAGS_CONFIG;
   private TagStore tagStore;
   private TagStore internalTagStore;
   private TextFormatter pluginTextFormatter;
   private TagGenerator tagGenerator;
   private Path importPath;
   private Interfaces interfaces;
   private ScheduledTask<?> timer;

   protected Plugin(VattenPlatform<?, ?> pluginInterface, VattenPlugin.Type type, Path path) {
      super(pluginInterface, type, path);
      this.onEnable();
      this.reload();
   }

   @Override
   protected void onEnable() {
      super.onEnable();
      this.api = new Plugin.API();
      this.PLUGIN_CONFIG = new ConfigInstance<>(this, "config", PluginConfig.class);
      this.TAGS_CONFIG = new ConfigInstance<>(this, "tags", TagsConfig.class);
      this.importPath = this.path.resolve("import");
      this.registerCommands(new Command[]{new TagsCommand(this, this.getPath().resolve("import"))});
      this.getEventHandler().registerEventHandler(PlayerLoadInEvent.class, this::onPlayerLoadIn);
      this.MINIMESSAGE = MiniMessage.builder()
         .tags(
            TagResolver.builder()
               .resolver(StandardTags.defaults())
               .resolver(
                  TagResolver.resolver(
                     "fancytags",
                     (queue, ctx) -> net.kyori.adventure.text.minimessage.tag.Tag.selfClosingInserting(
                        this.getTagStore().getTag(queue.pop().value()).asComponent()
                     )
                  )
               )
               .resolver(
                  TagResolver.resolver(
                     "fancytags_internal",
                     (queue, ctx) -> net.kyori.adventure.text.minimessage.tag.Tag.selfClosingInserting(
                        this.getInternalTagStore().getTag(queue.pop().value()).asComponent()
                     )
                  )
               )
               .build()
         )
         .build();
   }

   @Override
   protected void reload() {
      super.reload();
      if (!this.path.toFile().isDirectory()) {
         this.path.toFile().mkdirs();
      }

      if (!this.importPath.toFile().isDirectory()) {
         this.importPath.toFile().mkdirs();
      }

      this.PLUGIN_CONFIG.load();
      this.TAGS_CONFIG.load();
      this.internalTagStore = new InternalTagStore(this);
      this.pluginTextFormatter = new TextFormatter(this.internalTagStore.getTag("fancytags_logo").asComponent().appendSpace(), Style.empty());
      this.tagStore = new TagStore(this, this.getTagsConfig().getTags());
      this.tagGenerator = !this.getPluginConfig().getMineSkinSettings().getApiKey().isEmpty() ? new TagGenerator(this) : null;
      this.interfaces = new Interfaces(this);
      if (this.timer != null) {
         this.timer.cancel();
      }

      if (this.getPluginConfig().getCacheSettings().isEnabled() && this.getPluginConfig().getCacheSettings().isOnTimer()) {
         this.timer = this.pluginInterface
            .scheduleRepeatingTask(() -> this.broadcast(this::refreshTags), 0, this.PLUGIN_CONFIG.getData().getCacheSettings().getIntervalDuration() * 1000);
      }
   }

   private void onPlayerLoadIn(PlayerLoadInEvent event) {
      if (this.getPluginConfig().getCacheSettings().isEnabled() && this.getPluginConfig().getCacheSettings().isOnJoin()) {
         this.refreshTags(event.getPlayer());
      }
   }

   private void refreshTags(VattenPlayer tagsPlayer) {
      tagsPlayer.showTitle(
         Title.title(
            Component.text(
                  "  "
                     .repeat(
                        (int)Math.ceil(
                           (this.getTagStore().getTagAtlasLength() + this.getInternalTagStore().getTagAtlasLength())
                              * this.getPluginConfig().getCacheSettings().getOffScreenRatio()
                        )
                     )
               )
               .append(this.getInternalTagStore().getTagAtlas().append(this.getTagStore().getTagAtlas())),
            Component.empty(),
            0,
            40,
            0
         )
      );
   }

   public void saveTags() {
      this.TAGS_CONFIG.getData().setTags(this.getTagStore().getTags());
      this.TAGS_CONFIG.save();
   }

   private void broadcast(Consumer<VattenPlayer> consumer) {
      for (VattenPlayer player : this.players.values()) {
         consumer.accept(player);
      }
   }

   PluginConfig getPluginConfig() {
      return this.PLUGIN_CONFIG.getData();
   }

   TagsConfig getTagsConfig() {
      return this.TAGS_CONFIG.getData();
   }

   public MiniMessage getMiniMessage() {
      return this.MINIMESSAGE;
   }

   void registerMiniPlaceholders() {
      ((Expansion)Expansion.builder("fancytags")
            .author("SampleCraft")
            .version(this.pluginInfo.version())
            .globalPlaceholder(
               "tag",
               (queue, ctx) -> net.kyori.adventure.text.minimessage.tag.Tag.selfClosingInserting(this.getTagStore().getTag(queue.pop().value()).asComponent())
            )
            .globalPlaceholder(
               "internal",
               (queue, ctx) -> net.kyori.adventure.text.minimessage.tag.Tag.selfClosingInserting(
                  this.getInternalTagStore().getTag(queue.pop().value()).asComponent()
               )
            )
            .build())
         .register();
   }

   @Generated
   public TagStore getTagStore() {
      return this.tagStore;
   }

   @Generated
   public TagStore getInternalTagStore() {
      return this.internalTagStore;
   }

   @Generated
   public TextFormatter getPluginTextFormatter() {
      return this.pluginTextFormatter;
   }

   @Generated
   public TagGenerator getTagGenerator() {
      return this.tagGenerator;
   }

   @Generated
   public Path getImportPath() {
      return this.importPath;
   }

   @Generated
   public Interfaces getInterfaces() {
      return this.interfaces;
   }

   public class API extends VattenPlugin.API {
   }
}
