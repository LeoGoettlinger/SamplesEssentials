package su.nightexpress.nightcore.bridge.spigot.text;

import java.awt.Color;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentStyle;
import net.md_5.bungee.api.chat.ComponentStyleBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.ObjectComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.common.NightKey;
import su.nightexpress.nightcore.bridge.spigot.SpigotBridge;
import su.nightexpress.nightcore.bridge.spigot.click.SpigotClickEventAdapter;
import su.nightexpress.nightcore.bridge.text.NightStyle;
import su.nightexpress.nightcore.bridge.text.NightTextDecoration;
import su.nightexpress.nightcore.bridge.text.adapter.TextComponentAdapter;
import su.nightexpress.nightcore.bridge.text.event.NightClickEvent;
import su.nightexpress.nightcore.bridge.text.event.NightHoverEvent;
import su.nightexpress.nightcore.bridge.text.impl.NightKeybindComponent;
import su.nightexpress.nightcore.bridge.text.impl.NightObjectComponent;
import su.nightexpress.nightcore.bridge.text.impl.NightTextComponent;
import su.nightexpress.nightcore.bridge.text.impl.NightTranslatableComponent;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

public class SpigotTextComponentAdapter implements TextComponentAdapter<BaseComponent> {
   private final SpigotBridge bridge;

   public SpigotTextComponentAdapter(@NonNull SpigotBridge bridge) {
      this.bridge = bridge;
   }

   @Override
   public void send(@NonNull CommandSender sender, @NonNull NightComponent component) {
      sender.spigot().sendMessage(this.adaptComponent(component));
   }

   @Override
   public void sendActionBar(@NonNull Player player, @NonNull NightComponent component) {
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, this.adaptComponent(component));
   }

   @NonNull
   @Override
   public String toJson(@NonNull NightComponent component) {
      return ComponentSerializer.toString(this.adaptComponent(component));
   }

   @NonNull
   @Override
   public String toLegacy(@NonNull NightComponent component) {
      return TextComponent.toLegacyText(new BaseComponent[]{this.adaptComponent(component)});
   }

   @NonNull
   public ComponentStyle adaptStyle(@NonNull NightStyle nightStyle) {
      NightKey font = nightStyle.font();
      Color color = nightStyle.color();
      Color shadowColor = nightStyle.shadowColor();
      ComponentStyleBuilder builder = ComponentStyle.builder()
         .shadowColor(shadowColor)
         .font(font == null ? null : font.toString())
         .color(color == null ? null : ChatColor.of(color))
         .bold(nightStyle.decoration(NightTextDecoration.BOLD).bool())
         .italic(nightStyle.decoration(NightTextDecoration.ITALIC).bool())
         .obfuscated(nightStyle.decoration(NightTextDecoration.OBFUSCATED).bool())
         .strikethrough(nightStyle.decoration(NightTextDecoration.STRIKETHROUGH).bool())
         .underlined(nightStyle.decoration(NightTextDecoration.UNDERLINED).bool());
      return builder.build();
   }

   @NonNull
   public ClickEvent adaptClickEvent(@NonNull NightClickEvent event) {
      return SpigotClickEventAdapter.adaptClickEvent(event, this.bridge.getDialogAdapter());
   }

   @Nullable
   public HoverEvent adaptHoverEvent(@NonNull NightHoverEvent<?> event) {
      Object value = event.value();
      if (value instanceof ItemStack itemStack) {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            String nbt = meta.getAsString();
            String key = BukkitThing.getAsString(itemStack.getType());
            Item item = new Item(key, itemStack.getAmount(), ItemTag.ofNbt(nbt));
            return new HoverEvent(Action.SHOW_ITEM, new Content[]{item});
         }
      } else if (value instanceof NightComponent component) {
         Text text = new Text(this.adaptComponent(component));
         return new HoverEvent(Action.SHOW_TEXT, new Content[]{text});
      } else {
         return null;
      }
   }

   private void adaptProperties(@NonNull BaseComponent spigot, @NonNull NightComponent component) {
      NightClickEvent clickEvent = component.clickEvent();
      NightHoverEvent<?> hoverEvent = component.hoverEvent();
      spigot.setStyle(this.adaptStyle(component.style()));
      spigot.setInsertion(component.insertion());
      spigot.setClickEvent(clickEvent == null ? null : this.adaptClickEvent(clickEvent));
      spigot.setHoverEvent(hoverEvent == null ? null : this.adaptHoverEvent(hoverEvent));
      component.children().forEach(child -> spigot.addExtra(this.adaptComponent(child)));
   }

   @NonNull
   public List<BaseComponent> adaptComponents(@NonNull List<NightComponent> components) {
      return Lists.modify(components, this::adaptComponent);
   }

   @NonNull
   public BaseComponent adaptComponent(@NonNull NightComponent component) {
      return component.adapt(this);
   }

   @NonNull
   public TextComponent adaptComponent(@NonNull NightTextComponent component) {
      TextComponent spigot = new TextComponent(component.content());
      this.adaptProperties(spigot, component);
      return spigot;
   }

   @NonNull
   public KeybindComponent adaptComponent(@NonNull NightKeybindComponent component) {
      KeybindComponent spigot = new KeybindComponent(component.key());
      this.adaptProperties(spigot, component);
      return spigot;
   }

   @NonNull
   public TranslatableComponent adaptComponent(@NonNull NightTranslatableComponent component) {
      TranslatableComponent spigot = new TranslatableComponent(component.key(), new Object[0]);
      spigot.setFallback(component.fallback());
      spigot.setWith(Lists.modify(component.arguments(), argument -> this.adaptComponent(argument.asComponent())));
      this.adaptProperties(spigot, component);
      return spigot;
   }

   @NonNull
   public BaseComponent adaptComponent(@NonNull NightObjectComponent component) {
      ObjectComponent spigot = new ObjectComponent(SpigotObjectContentsAdapter.get().adaptContents(component.contents()));
      this.adaptProperties(spigot, component);
      return spigot;
   }
}
