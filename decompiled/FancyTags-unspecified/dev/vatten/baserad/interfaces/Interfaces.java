package dev.vatten.baserad.interfaces;

import dev.vatten.baserad.Plugin;
import dev.vatten.baserad.Renderable;
import dev.vatten.baserad.Tag;
import dev.vatten.baserad.TagStore;
import dev.vatten.baserad.TextFormatter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class Interfaces {
   private final Plugin plugin;

   public Interfaces(Plugin plugin) {
      this.plugin = plugin;
   }

   public Component createTagInterface(String tagName) {
      RenderableComponent.MultiLineBuilder tagInterface = RenderableComponent.multiLine();
      TagStore tagStore = this.plugin.getTagStore();
      Tag tag = tagStore.getTag(tagName);
      tagInterface.addLine(Component.empty());
      tagInterface.addLine(
         RenderableComponent.field()
            .title(Component.text("Viewing tag: "))
            .addComponent(
               ((Builder)((Builder)((Builder)((Builder)Component.text().clickEvent(ClickEvent.copyToClipboard(tag.asMiniMessage())))
                           .append(Component.text("⛨ ", TextColor.color(14310143))))
                        .append(tag.asComponent()))
                     .hoverEvent(
                        HoverEvent.showText(
                           Component.text("[Click]", NamedTextColor.DARK_GRAY).append(Component.text(" Copy MiniMessage format", NamedTextColor.GRAY))
                        )
                     ))
                  .build()
            )
            .addComponent(Component.text("✎ " + tagName, TextColor.color(253948)))
            .spacing(3)
            .build()
            .asComponent()
      );
      tagInterface.addLine(Component.empty());
      tagInterface.addLine(
         this.createActionsField(
               ((Builder)((Builder)((Builder)Component.text().clickEvent(ClickEvent.suggestCommand("/tags rename " + tagName + " <new_name>")))
                        .append(this.plugin.getInternalTagStore().getTag("rename_btn").asComponent()))
                     .hoverEvent(
                        HoverEvent.showText(RenderableComponent.multiLine().addLines(TextFormatter.INFO.format("Rename this tag")).build().asComponent())
                     ))
                  .build(),
               ((Builder)((Builder)((Builder)Component.text().clickEvent(ClickEvent.runCommand("/tags delete " + tagName)))
                        .append(this.plugin.getInternalTagStore().getTag("delete_btn").asComponent()))
                     .hoverEvent(
                        RenderableComponent.multiLine()
                           .addLines(TextFormatter.INFO.format("Delete this tag"), TextFormatter.SEVERE.format("This action cannot be undone."))
                           .build()
                           .asComponent()
                     ))
                  .build()
            )
            .build()
            .asComponent()
      );
      return tagInterface.build().asComponent();
   }

   public Component createConfirmInterface(Component content, ClickCallback<Audience> callback) {
      RenderableComponent.MultiLineBuilder confirmInterface = RenderableComponent.multiLine();
      confirmInterface.addLine(Component.empty());
      confirmInterface.addLine(
         RenderableComponent.field()
            .addComponent(
               TextFormatter.INFO
                  .format(
                     ((Builder)((Builder)((Builder)Component.text().append(Component.text("Please confirm the following: "))).appendNewline()).append(content))
                        .build()
                  )
            )
            .build()
            .asComponent()
      );
      confirmInterface.addLine(Component.empty());
      confirmInterface.addLine(
         this.createActionsField(
               ((Builder)((Builder)Component.text().clickEvent(ClickEvent.callback(callback)))
                     .append(this.plugin.getInternalTagStore().getTag("confirm_btn").asComponent()))
                  .build()
            )
            .build()
            .asComponent()
      );
      return confirmInterface.build().asComponent();
   }

   public Component createTagListInterface(TagStore tagStore) {
      RenderableComponent.MultiLineBuilder listInterface = RenderableComponent.multiLine();
      listInterface.addLine(Component.empty());
      RenderableComponent.FieldBuilder currentField = RenderableComponent.field().title(Component.text("Tags: ")).spacing(3);
      int i = 0;

      for (String name : tagStore.getTagNames()) {
         Tag tag = tagStore.getTag(name);
         Renderable renderable = RenderableComponent.of(
            ((Builder)((Builder)((Builder)((Builder)((Builder)Component.text().append(Component.text(name + " ("))).append(tag.asComponent()))
                        .append(Component.text(")")))
                     .clickEvent(ClickEvent.runCommand("/tags view " + name)))
                  .hoverEvent(
                     RenderableComponent.multiLine()
                        .addLines(
                           tag.asComponent(),
                           Component.text("[Click]", NamedTextColor.DARK_GRAY).append(Component.text(" View more options", NamedTextColor.GRAY))
                        )
                        .build()
                        .asComponent()
                  ))
               .build()
         );
         i++;
         if (currentField.build().estimateWidth() + renderable.estimateWidth() > 300) {
            listInterface.addLine(currentField.build().asComponent());
            currentField = RenderableComponent.field().padding(7).spacing(3);
         }

         currentField.addComponent(renderable.asComponent());
      }

      listInterface.addLine(currentField.build().asComponent());
      listInterface.addLine(Component.empty());
      return listInterface.build().asComponent();
   }

   public RenderableComponent.FieldBuilder createActionsField(Component... components) {
      return RenderableComponent.field().title(Component.text("Actions: ")).addComponents(components).spacing(3);
   }
}
