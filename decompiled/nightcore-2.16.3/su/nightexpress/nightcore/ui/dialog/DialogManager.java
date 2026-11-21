package su.nightexpress.nightcore.ui.dialog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.ui.menu.Menu;
import su.nightexpress.nightcore.ui.menu.MenuRegistry;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.util.Placeholders;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bridge.wrapper.ClickEventType;
import su.nightexpress.nightcore.util.text.tag.Tags;
import su.nightexpress.nightcore.util.time.TimeFormats;

@Deprecated
public class DialogManager {
   private static final Map<UUID, Dialog> DIALOG_MAP = new HashMap<>();
   public static final String EXIT = "#exit";
   public static final String VALUES = "#values";

   public static void shutdown() {
      DIALOG_MAP.clear();
   }

   @NonNull
   public static Set<Dialog> getDialogs() {
      return new HashSet<>(DIALOG_MAP.values());
   }

   public static void tickDialogs() {
      getDialogs().forEach(DialogManager::tickDialog);
   }

   private static void tickDialog(@NonNull Dialog dialog) {
      if (dialog.isExpired()) {
         stopDialog(dialog);
      } else {
         displayPrompt(dialog, 0);
         dialog.tick();
      }
   }

   private static void displayPrompt(@NonNull Dialog dialog, int fade) {
      Player player = dialog.getPlayer();
      String title = CoreLang.DIALOG_HEADER.getString().replace("%time%", TimeFormats.toLiteral(dialog.getLifetimeMillis()));
      String sub = dialog.getPrompt();
      Players.sendTitles(player, title, sub, fade, 40, 20);
   }

   public static boolean isInDialog(@NonNull Player player) {
      return getDialog(player) != null;
   }

   @Nullable
   public static Dialog getDialog(@NonNull Player player) {
      return DIALOG_MAP.get(player.getUniqueId());
   }

   public static void startDialog(@NonNull Dialog.Builder builder) {
      startDialog(builder.build());
   }

   public static void startDialog(@NonNull Dialog dialog) {
      Player player = dialog.getPlayer();
      MenuViewer viewer = MenuRegistry.getViewer(player);
      if (viewer != null) {
         dialog.setLastMenu(viewer.getMenu());
         dialog.setLastPage(viewer.getPage());
      }

      displaySuggestions(dialog, 1);
      displayPrompt(dialog, 20);
      DIALOG_MAP.put(player.getUniqueId(), dialog);
      CoreLang.DIALOG_INFO_EXIT.getMessage().send(player);
   }

   public static void stopDialog(@NonNull Player player) {
      Dialog dialog = getDialog(player);
      if (dialog != null) {
         stopDialog(dialog);
      }
   }

   public static void stopDialog(@NonNull Dialog dialog) {
      Player player = dialog.getPlayer();
      Menu menu = dialog.getLastMenu();
      if (menu != null) {
         menu.flush(player, viewer -> viewer.setPage(dialog.getLastPage()));
      }

      DIALOG_MAP.remove(player.getUniqueId());
   }

   public static void displaySuggestions(@NonNull Dialog dialog, int page) {
      List<String> suggestions = dialog.getSuggestions();
      if (suggestions != null && !suggestions.isEmpty()) {
         boolean autoRun = dialog.isSuggestionAutoRun();
         int perPage = 10;
         int pages = (int)Math.ceil((double)suggestions.size() / perPage);
         if (page < 1) {
            page = 1;
         } else if (page > pages) {
            page = pages;
         }

         int skip = (page - 1) * perPage;
         boolean isLastPage = page == pages;
         boolean isFirstPage = page == 1;
         List<String> items = suggestions.stream().skip(skip).limit(perPage).toList();
         ClickEventType action = autoRun ? ClickEventType.RUN_COMMAND : ClickEventType.SUGGEST_COMMAND;
         StringBuilder builder = new StringBuilder()
            .append(Tags.ORANGE.enclose("=".repeat(8) + "[ " + Tags.YELLOW.enclose("Value Helper") + " ]" + "=".repeat(8)))
            .append(Placeholders.TAG_LINE_BREAK);
         items.forEach(
            element -> {
               String hoverHint = Tags.GRAY.enclose("Click me to select " + Tags.CYAN.enclose(element) + ".");
               String clickCommand = element.charAt(0) == '/' ? element : "/" + element;
               builder.append(Tags.DARK_GRAY.enclose("> "))
                  .append(Tags.GREEN.enclose(Tags.HOVER.wrapShowText(Tags.CLICK.wrap(element, action, clickCommand), hoverHint)));
               builder.append(Placeholders.TAG_LINE_BREAK);
            }
         );
         builder.append(Tags.ORANGE.enclose("=".repeat(9))).append(" ");
         if (isFirstPage) {
            builder.append(Tags.GRAY.enclose("[<]"));
         } else {
            builder.append(
               Tags.LIGHT_RED.enclose(Tags.HOVER.wrapShowText(Tags.CLICK.wrapRunCommand("[<]", "/#values " + (page - 1)), Tags.GRAY.enclose("Previous Page")))
            );
         }

         builder.append(Tags.YELLOW.enclose(" " + page));
         builder.append(Tags.ORANGE.enclose("/"));
         builder.append(Tags.YELLOW.enclose(pages + " "));
         if (isLastPage) {
            builder.append(Tags.GRAY.enclose("[>]"));
         } else {
            builder.append(
               Tags.LIGHT_RED.enclose(Tags.HOVER.wrapShowText(Tags.CLICK.wrapRunCommand("[>]", "/#values " + (page + 1)), Tags.GRAY.enclose("Next Page")))
            );
         }

         builder.append(Tags.ORANGE.enclose(" " + "=".repeat(9)));
         Players.sendModernMessage(dialog.getPlayer(), builder.toString());
      }
   }
}
