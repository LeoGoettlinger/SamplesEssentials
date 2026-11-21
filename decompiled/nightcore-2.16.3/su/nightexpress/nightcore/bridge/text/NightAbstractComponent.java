package su.nightexpress.nightcore.bridge.text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.common.NightKey;
import su.nightexpress.nightcore.bridge.text.adapter.TextComponentAdapter;
import su.nightexpress.nightcore.bridge.text.event.NightClickEvent;
import su.nightexpress.nightcore.bridge.text.event.NightHoverEvent;
import su.nightexpress.nightcore.bridge.text.impl.NightTextComponent;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bridge.Software;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

public abstract class NightAbstractComponent implements NightComponent {
   protected final List<NightComponent> children;
   protected final NightStyle style;

   protected NightAbstractComponent(@NonNull List<? extends NightComponent> children, @NonNull NightStyle style) {
      this.children = new ArrayList<>(children);
      this.style = style;
   }

   @NonNull
   @Override
   public <T> T adapt(@NonNull TextComponentAdapter<T> adapter) {
      return adapter.adaptComponent(this);
   }

   @NonNull
   @Override
   public NightComponent append(@NonNull List<? extends NightComponent> components) {
      ArrayList<NightComponent> children = new ArrayList<>(this.children());
      children.addAll(components);
      return this.children(children);
   }

   @NonNull
   @Override
   public final List<NightComponent> children() {
      return this.children;
   }

   @NonNull
   @Override
   public final NightStyle style() {
      return this.style;
   }

   @NonNull
   @Override
   public NightComponent appendNewline() {
      return this.append(NightComponent.newline());
   }

   @NonNull
   @Override
   public NightComponent appendSpace() {
      return this.append(NightComponent.space());
   }

   @NonNull
   @Override
   public NightComponent append(NightComponent... components) {
      return this.append(Lists.newList(components));
   }

   @NonNull
   @Override
   public NightComponent append(@NonNull NightComponent component) {
      return this.append(component);
   }

   @Nullable
   @Override
   public NightKey font() {
      return this.style().font();
   }

   @NonNull
   @Override
   public NightComponent font(@Nullable NightKey key) {
      return this.style(this.style().font(key));
   }

   @Nullable
   @Override
   public Color color() {
      return this.style().color();
   }

   @Nullable
   @Override
   public Color shadowColor() {
      return this.style().shadowColor();
   }

   @NonNull
   @Override
   public NightComponent color(@Nullable Color color) {
      return this.style(this.style().color(color));
   }

   @NonNull
   @Override
   public NightComponent shadowColor(@Nullable Color color) {
      return this.style(this.style().shadowColor(color));
   }

   @Override
   public boolean hasDecoration(@NonNull NightTextDecoration decoration) {
      return this.decoration(decoration) == NightTextDecoration.State.TRUE;
   }

   @NonNull
   @Override
   public NightComponent decorate(@NonNull NightTextDecoration decoration) {
      return this.style(this.style().decoration(decoration, NightTextDecoration.State.TRUE));
   }

   @NonNull
   @Override
   public NightTextDecoration.State decoration(@NonNull NightTextDecoration decoration) {
      return this.style().decoration(decoration);
   }

   @NonNull
   @Override
   public NightComponent decoration(@NonNull NightTextDecoration decoration, boolean flag) {
      return this.style(this.style().decoration(decoration, NightTextDecoration.State.byBoolean(flag)));
   }

   @NonNull
   @Override
   public NightComponent decoration(@NonNull NightTextDecoration decoration, @NonNull NightTextDecoration.State state) {
      return this.style(this.style().decoration(decoration, state));
   }

   @NonNull
   @Override
   public Map<NightTextDecoration, NightTextDecoration.State> decorations() {
      return this.style().decorations();
   }

   @Nullable
   @Override
   public NightClickEvent clickEvent() {
      return this.style().clickEvent();
   }

   @NonNull
   @Override
   public NightComponent clickEvent(@Nullable NightClickEvent event) {
      return this.style(this.style().clickEvent(event));
   }

   @Nullable
   @Override
   public NightHoverEvent<?> hoverEvent() {
      return this.style().hoverEvent();
   }

   @NonNull
   @Override
   public NightComponent hoverEvent(@Nullable NightHoverEvent<?> hoverEvent) {
      return this.style(this.style().hoverEvent(hoverEvent));
   }

   @Nullable
   @Override
   public String insertion() {
      return this.style().insertion();
   }

   @NonNull
   @Override
   public NightComponent insertion(@Nullable String insertion) {
      return this.style(this.style().insertion(insertion));
   }

   @Override
   public boolean hasStyling() {
      return !this.style().isEmpty();
   }

   @NonNull
   @Override
   public String toJson() {
      return Software.instance().getTextComponentAdapter().toJson(this);
   }

   @NonNull
   @Override
   public String toLegacy() {
      return Software.instance().getTextComponentAdapter().toLegacy(this);
   }

   @Override
   public void send(@NonNull CommandSender sender) {
      Software.instance().getTextComponentAdapter().send(sender, this);
   }

   @Override
   public void sendActionBar(@NonNull Player player) {
      Software.instance().getTextComponentAdapter().sendActionBar(player, this);
   }

   @Override
   public boolean isEmpty() {
      return this instanceof NightTextComponent textComponent && textComponent.content().isEmpty() && textComponent.children().isEmpty();
   }
}
