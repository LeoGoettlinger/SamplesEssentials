package su.nightexpress.nightcore.bridge.paper.text;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.Style.Builder;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.common.NightKey;
import su.nightexpress.nightcore.bridge.paper.PaperBridge;
import su.nightexpress.nightcore.bridge.text.NightStyle;
import su.nightexpress.nightcore.bridge.text.NightTextDecoration;
import su.nightexpress.nightcore.bridge.text.adapter.TextComponentAdapter;
import su.nightexpress.nightcore.bridge.text.event.NightClickEvent;
import su.nightexpress.nightcore.bridge.text.event.NightHoverEvent;
import su.nightexpress.nightcore.bridge.text.event.WrappedPayload;
import su.nightexpress.nightcore.bridge.text.impl.NightKeybindComponent;
import su.nightexpress.nightcore.bridge.text.impl.NightObjectComponent;
import su.nightexpress.nightcore.bridge.text.impl.NightTextComponent;
import su.nightexpress.nightcore.bridge.text.impl.NightTranslatableComponent;
import su.nightexpress.nightcore.bridge.text.impl.NightTranslationArgument;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

public class PaperTextComponentAdapter implements TextComponentAdapter<Component> {
   private final PaperBridge bridge;

   public PaperTextComponentAdapter(@NonNull PaperBridge bridge) {
      this.bridge = bridge;
   }

   @Override
   public void send(@NonNull CommandSender sender, @NonNull NightComponent component) {
      sender.sendMessage(this.adaptComponent(component));
   }

   @Override
   public void sendActionBar(@NonNull Player player, @NonNull NightComponent component) {
      player.sendActionBar(this.adaptComponent(component));
   }

   @NonNull
   @Override
   public String toJson(@NonNull NightComponent component) {
      return (String)JSONComponentSerializer.json().serialize(this.adaptComponent(component));
   }

   @NonNull
   @Override
   public String toLegacy(@NonNull NightComponent component) {
      return LegacyComponentSerializer.legacy('§').serialize(this.adaptComponent(component));
   }

   @NonNull
   public ClickEvent adaptClickEvent(@NonNull NightClickEvent nightClickEvent) {
      WrappedPayload payload = nightClickEvent.payload();

      return switch (nightClickEvent.action()) {
         case CUSTOM -> {
            WrappedPayload.Custom custom = (WrappedPayload.Custom)payload;
            NightKey namespacedKey = custom.key();
            Key key = namespacedKey.toBukkit();
            BinaryTagHolder nbt = BinaryTagHolder.binaryTagHolder(custom.nbt().asString());
            yield ClickEvent.custom(key, nbt);
         }
         case OPEN_URL -> ClickEvent.openUrl(((WrappedPayload.Text)payload).value());
         case OPEN_FILE -> ClickEvent.openFile(((WrappedPayload.Text)payload).value());
         case CHANGE_PAGE -> ClickEvent.changePage(((WrappedPayload.Int)payload).integer());
         case RUN_COMMAND -> ClickEvent.runCommand(((WrappedPayload.Text)payload).value());
         case SHOW_DIALOG -> ClickEvent.showDialog((DialogLike)this.bridge.getDialogAdapter().adaptDialog(((WrappedPayload.Dialog)payload).dialog()));
         case SUGGEST_COMMAND -> ClickEvent.suggestCommand(((WrappedPayload.Text)payload).value());
         case COPY_TO_CLIPBOARD -> ClickEvent.copyToClipboard(((WrappedPayload.Text)payload).value());
      };
   }

   @Nullable
   public <V> HoverEvent<?> adaptHoverEvent(@NonNull NightHoverEvent<V> event) {
      Object value = event.value();
      if (value instanceof ItemStack itemStack) {
         return itemStack.asHoverEvent();
      } else {
         return value instanceof NightComponent component ? HoverEvent.showText(this.adaptComponent(component)) : null;
      }
   }

   @NonNull
   public TextDecoration adaptTextDecoration(@NonNull NightTextDecoration nightDecoration) {
      return switch (nightDecoration) {
         case BOLD -> TextDecoration.BOLD;
         case ITALIC -> TextDecoration.ITALIC;
         case OBFUSCATED -> TextDecoration.OBFUSCATED;
         case UNDERLINED -> TextDecoration.UNDERLINED;
         case STRIKETHROUGH -> TextDecoration.STRIKETHROUGH;
      };
   }

   @NonNull
   public State adaptTextDecorationState(@NonNull NightTextDecoration.State nightState) {
      return switch (nightState) {
         case TRUE -> State.TRUE;
         case FALSE -> State.FALSE;
         case NOT_SET -> State.NOT_SET;
      };
   }

   @NonNull
   public Style adaptStyle(@NonNull NightStyle nightStyle) {
      NightKey font = nightStyle.font();
      Color color = nightStyle.color();
      Color shadowColor = nightStyle.shadowColor();
      Map<NightTextDecoration, NightTextDecoration.State> decorations = nightStyle.decorations();
      NightClickEvent clickEvent = nightStyle.clickEvent();
      NightHoverEvent<?> hoverEvent = nightStyle.hoverEvent();
      String insertion = nightStyle.insertion();
      return Style.style(
         builder -> {
            decorations.forEach(
               (nightDecoration, nightState) -> builder.decoration(this.adaptTextDecoration(nightDecoration), this.adaptTextDecorationState(nightState))
            );
            ((Builder)builder.shadowColor(
                  shadowColor == null
                     ? null
                     : ShadowColor.shadowColor(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), shadowColor.getAlpha())
               ))
               .font(font == null ? null : Key.key(font.namespace(), font.value()))
               .color(color == null ? null : TextColor.color(color.getRed(), color.getGreen(), color.getBlue()))
               .clickEvent(clickEvent == null ? null : this.adaptClickEvent(clickEvent))
               .hoverEvent(hoverEvent == null ? null : this.adaptHoverEvent(hoverEvent))
               .insertion(insertion);
         }
      );
   }

   @NonNull
   public List<Component> adaptComponents(@NonNull List<NightComponent> components) {
      return Lists.modify(components, this::adaptComponent);
   }

   @NonNull
   public Component adaptComponent(@NonNull NightComponent nightComponent) {
      return nightComponent.adapt(this);
   }

   @NonNull
   public TextComponent adaptComponent(@NonNull NightTextComponent component) {
      String content = component.content();
      return content.equals("\n")
         ? Component.newline()
         : Component.text(
            builder -> ((net.kyori.adventure.text.TextComponent.Builder)((net.kyori.adventure.text.TextComponent.Builder)builder.style(
                     this.adaptStyle(component.style())
                  ))
                  .append(this.adaptComponents(component.children())))
               .content(content)
         );
   }

   @NonNull
   public TranslationArgument adaptTranslatableArgument(@NonNull NightTranslationArgument nightArgument) {
      return TranslationArgument.component(this.adaptComponent(nightArgument.asComponent()));
   }

   @NonNull
   public TranslatableComponent adaptComponent(@NonNull NightTranslatableComponent component) {
      String fallback = component.fallback();
      List<NightTranslationArgument> nightArguments = component.arguments();
      List<TranslationArgument> arguments = Lists.modify(nightArguments, this::adaptTranslatableArgument);
      return Component.translatable(
         builder -> ((net.kyori.adventure.text.TranslatableComponent.Builder)((net.kyori.adventure.text.TranslatableComponent.Builder)builder.style(
                  this.adaptStyle(component.style())
               ))
               .append(this.adaptComponents(component.children())))
            .key(component.key())
            .fallback(fallback)
            .arguments(arguments)
      );
   }

   @NonNull
   public Component adaptComponent(@NonNull NightKeybindComponent component) {
      return Component.keybind(
         builder -> ((net.kyori.adventure.text.KeybindComponent.Builder)((net.kyori.adventure.text.KeybindComponent.Builder)builder.style(
                  this.adaptStyle(component.style())
               ))
               .append(this.adaptComponents(component.children())))
            .keybind(component.key())
      );
   }

   @NonNull
   public Component adaptComponent(@NonNull NightObjectComponent component) {
      return ((net.kyori.adventure.text.ObjectComponent.Builder)((net.kyori.adventure.text.ObjectComponent.Builder)Component.object()
               .style(this.adaptStyle(component.style())))
            .append(this.adaptComponents(component.children())))
         .contents(PaperObjectContantsAdapter.get().adaptContents(component.contents()))
         .build();
   }
}
