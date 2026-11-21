package su.nightexpress.nightcore.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.menu.api.Menu;
import su.nightexpress.nightcore.menu.impl.AbstractMenu;
import su.nightexpress.nightcore.util.Placeholders;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bridge.wrapper.ClickEventType;
import su.nightexpress.nightcore.util.text.TextRoot;
import su.nightexpress.nightcore.util.text.night.NightMessage;
import su.nightexpress.nightcore.util.text.tag.Tags;

@Deprecated
public class Dialog {
   private static final Map<UUID, Dialog> DIALOG_MAP = new ConcurrentHashMap<>();
   private static final int DEFAULT_TIMEOUT = 60;
   public static final String EXIT = "#exit";
   public static final String VALUES = "#values";
   private final Player player;
   private final DialogHandler handler;
   private Menu lastMenu;
   private List<String> suggestions;
   private long timeoutDate;

   public Dialog(@NonNull Player player, @NonNull DialogHandler handler) {
      this.player = player;
      this.handler = handler;
      this.suggestions = new ArrayList<>();
      this.setTimeout(60);
   }

   public boolean isTimedOut() {
      return this.timeoutDate > 0L && System.currentTimeMillis() >= this.timeoutDate;
   }

   @NonNull
   public Player getPlayer() {
      return this.player;
   }

   @NonNull
   public DialogHandler getHandler() {
      return this.handler;
   }

   @Nullable
   public Menu getLastMenu() {
      return this.lastMenu;
   }

   @NonNull
   public Dialog setLastMenu(@Nullable Menu lastMenu) {
      this.lastMenu = lastMenu;
      return this;
   }

   @NonNull
   public List<String> getSuggestions() {
      return this.suggestions;
   }

   @NonNull
   public Dialog setSuggestions(@NonNull Collection<String> suggestions, boolean autoRun) {
      this.suggestions = suggestions.stream().sorted(String::compareTo).collect(Collectors.toCollection(ArrayList::new));
      this.displaySuggestions(autoRun, 1);
      return this;
   }

   public long getTimeoutDate() {
      return this.timeoutDate;
   }

   public void setTimeout(int timeout) {
      this.timeoutDate = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, TimeUnit.SECONDS);
   }

   public void displaySuggestions(boolean autoRun, int page) {
      List<String> values = this.getSuggestions();
      if (!values.isEmpty()) {
         int perPage = 10;
         int pages = (int)Math.ceil((double)values.size() / perPage);
         if (page < 1) {
            page = 1;
         } else if (page > pages) {
            page = pages;
         }

         int skip = (page - 1) * perPage;
         boolean isLastPage = page == pages;
         boolean isFirstPage = page == 1;
         List<String> items = values.stream().skip(skip).limit(perPage).toList();
         ClickEventType action = autoRun ? ClickEventType.RUN_COMMAND : ClickEventType.SUGGEST_COMMAND;
         StringBuilder builder = new StringBuilder()
            .append(Tags.ORANGE.enclose("=".repeat(8) + "[ " + Tags.YELLOW.enclose("Value Helper") + " ]" + "=".repeat(8)))
            .append(Placeholders.TAG_LINE_BREAK);
         items.forEach(
            element -> {
               String hoverHint = Tags.GRAY.enclose("Click me to select " + Tags.CYAN.enclose(element) + ".");
               String clickCommand = element.charAt(0) == '/' ? element : "/" + element;
               builder.append(Tags.DARK_GRAY.enclose("> "))
                  .append(Tags.GREEN.enclose(Tags.HOVER.wrapShowText(Tags.CLICK.enclose(element, action, clickCommand), hoverHint)));
               builder.append(Placeholders.TAG_LINE_BREAK);
            }
         );
         builder.append(Tags.ORANGE.enclose("=".repeat(9))).append(" ");
         if (isFirstPage) {
            builder.append(Tags.GRAY.enclose("[<]"));
         } else {
            builder.append(
               Tags.LIGHT_RED
                  .enclose(
                     Tags.HOVER.wrapShowText(Tags.CLICK.wrapRunCommand("[<]", "/#values " + (page - 1) + " " + autoRun), Tags.GRAY.enclose("Previous Page"))
                  )
            );
         }

         builder.append(Tags.YELLOW.enclose(" " + page));
         builder.append(Tags.ORANGE.enclose("/"));
         builder.append(Tags.YELLOW.enclose(pages + " "));
         if (isLastPage) {
            builder.append(Tags.GRAY.enclose("[>]"));
         } else {
            builder.append(
               Tags.LIGHT_RED
                  .enclose(Tags.HOVER.wrapShowText(Tags.CLICK.wrapRunCommand("[>]", "/#values " + (page + 1) + " " + autoRun), Tags.GRAY.enclose("Next Page")))
            );
         }

         builder.append(Tags.ORANGE.enclose(" " + "=".repeat(9)));
         Players.sendModernMessage(this.player, builder.toString());
      }
   }

   @Deprecated
   public void prompt(@NonNull String text) {
      this.sendInfo(CoreLang.EDITOR_INPUT_HEADER_MAIN.getMessage().toLegacy(), NightMessage.asLegacy(text));
   }

   public void prompt(@NonNull TextRoot text) {
      this.info(CoreLang.EDITOR_INPUT_HEADER_MAIN.getMessage(), text);
   }

   @Deprecated
   public void error(@NonNull String text) {
      this.sendInfo(CoreLang.EDITOR_INPUT_HEADER_ERROR.getMessage().toLegacy(), NightMessage.asLegacy(text));
   }

   public void error(@NonNull TextRoot text) {
      this.info(CoreLang.EDITOR_INPUT_HEADER_ERROR.getMessage(), text);
   }

   public void info(@NonNull TextRoot title, @NonNull TextRoot text) {
      this.sendInfo(title.toLegacy(), text.toLegacy());
   }

   @Deprecated
   public void info(@NonNull String title, @NonNull String text) {
      this.sendInfo(NightMessage.asLegacy(title), NightMessage.asLegacy(text));
   }

   private void sendInfo(@NonNull String title, @NonNull String text) {
      this.getPlayer().sendTitle(title, text, 20, 32767, 20);
   }

   public static void checkTimeOut() {
      new HashSet<>(DIALOG_MAP.values()).forEach(dialog -> {
         if (dialog.isTimedOut()) {
            stop(dialog.player);
         }
      });
   }

   @NonNull
   public static Dialog create(@NonNull Player player, @NonNull DialogHandler handler) {
      Dialog dialog = new Dialog(player, handler).setLastMenu(AbstractMenu.getMenu(player));
      DIALOG_MAP.put(player.getUniqueId(), dialog);
      CoreLang.EDITOR_ACTION_EXIT.getMessage().send(player);
      return dialog;
   }

   @Nullable
   public static Dialog get(@NonNull Player player) {
      return DIALOG_MAP.get(player.getUniqueId());
   }

   public static void stop(@NonNull Player player) {
      Dialog dialog = DIALOG_MAP.remove(player.getUniqueId());
      if (dialog != null) {
         Menu menu = dialog.getLastMenu();
         if (menu != null) {
            menu.open(player);
         }

         player.sendTitle("", "", 1, 1, 1);
      }
   }

   public static void shutdown() {
      DIALOG_MAP.clear();
   }

   public static boolean contains(@NonNull Player player) {
      return get(player) != null;
   }
}
