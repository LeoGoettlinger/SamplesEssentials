package com.earth2me.essentials.adventure;

import java.util.Locale;
import net.ess3.provider.AbstractChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.Builder;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PaperAdventureFacet implements AdventureFacet {
   private final LegacyComponentSerializer legacySerializer;
   private final LegacyComponentSerializer legacySerializerUrls;
   private final MiniMessage miniMessageNoTags;
   private final MiniMessage miniMessageInstance;
   private final String primaryColor;
   private final String secondaryColor;

   public PaperAdventureFacet(final String primaryColor, final String secondaryColor) {
      this.primaryColor = primaryColor != null ? primaryColor : "gold";
      this.secondaryColor = secondaryColor != null ? secondaryColor : "red";
      Builder builder = LegacyComponentSerializer.builder().flattener(ComponentFlattener.basic()).hexColors().useUnusualXRepeatedCharacterHexFormat();
      this.legacySerializer = builder.build();
      this.legacySerializerUrls = builder.extractUrls(AbstractChatEvent.URL_PATTERN).build();
      this.miniMessageNoTags = MiniMessage.builder().strict(true).build();
      this.miniMessageInstance = MiniMessage.builder()
         .tags(
            TagResolver.builder()
               .resolvers(new TagResolver[]{TagResolver.standard()})
               .resolver(TagResolver.resolver("primary", this.supplyTag(true)))
               .resolver(TagResolver.resolver("secondary", this.supplyTag(false)))
               .build()
         )
         .build();
   }

   private Tag supplyTag(final boolean primary) {
      String color = primary ? this.primaryColor : this.secondaryColor;
      TextColor textColor;
      if (color.startsWith("#")) {
         textColor = TextColor.color(Color.fromRGB(Integer.decode(color)).asRGB());
      } else {
         textColor = (TextColor)NamedTextColor.NAMES.value(color.toLowerCase(Locale.ENGLISH));
      }

      return textColor != null
         ? Tag.styling(new StyleBuilderApplicable[]{textColor})
         : Tag.styling(new StyleBuilderApplicable[]{primary ? NamedTextColor.GOLD : NamedTextColor.RED});
   }

   @Override
   public ComponentHolder deserializeMiniMessage(String message) {
      return new ComponentHolder(this.miniMessageInstance.deserialize(message));
   }

   @Override
   public void send(CommandSender sender, ComponentHolder component) {
      sender.sendMessage((Component)component.getComponent());
   }

   @Override
   public void send(Player player, ComponentHolder component) {
      player.sendMessage((Component)component.getComponent());
   }

   @Override
   public String legacyToMini(String message) {
      return this.legacyToMini(message, true);
   }

   @Override
   public String legacyToMini(String message, boolean useCustomTags) {
      Component deserializedText = this.legacySerializer.deserialize(message);
      return useCustomTags ? (String)this.miniMessageInstance.serialize(deserializedText) : (String)this.miniMessageNoTags.serialize(deserializedText);
   }

   @Override
   public String legacyToMiniWithUrls(String message) {
      return (String)this.miniMessageInstance.serialize(this.legacySerializerUrls.deserialize(message));
   }

   @Override
   public String escapeTags(String input) {
      return this.miniMessageInstance.escapeTags(input);
   }

   @Override
   public String stripTags(String input) {
      return this.miniMessageInstance.stripTags(input);
   }

   @Override
   public ComponentHolder legacyToAdventure(String message) {
      return new ComponentHolder(this.legacySerializer.deserialize(message));
   }

   @Override
   public ComponentHolder text(String message) {
      return new ComponentHolder(Component.text(message));
   }

   @Override
   public String miniToLegacy(String message) {
      return this.adventureToLegacy(this.miniMessageInstance.deserialize(message));
   }

   @Override
   public String adventureToLegacy(ComponentHolder component) {
      return this.adventureToLegacy((Component)component.getComponent());
   }

   private String adventureToLegacy(Component component) {
      return this.legacySerializer.serialize(component);
   }

   @Override
   public ComponentHolder append(ComponentHolder base, ComponentHolder... addition) {
      Component baseComponent = (Component)base.getComponent();

      for (ComponentHolder holder : addition) {
         Component additionComponent = (Component)holder.getComponent();
         baseComponent = baseComponent.append(additionComponent);
      }

      return new ComponentHolder(baseComponent);
   }

   @Override
   public void close() {
   }
}
