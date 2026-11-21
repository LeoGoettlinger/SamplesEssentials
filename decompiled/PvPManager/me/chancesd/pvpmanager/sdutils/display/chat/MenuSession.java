package me.chancesd.pvpmanager.sdutils.display.chat;

import java.util.List;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MenuSession {
   private final CommandSender sender;
   private final ChatMenu menu;

   public MenuSession(@NotNull CommandSender sender, @NotNull ChatMenu menu) {
      this.sender = sender;
      this.menu = menu;
   }

   private void sendMessage(ChatLine chatLine) {
      if (!(this.sender instanceof Player player && MCVersion.isAtLeast(MCVersion.V1_16_5))) {
         this.sender.sendMessage(ChatUtils.colorize(chatLine.getPlainText()));
      } else if (MCVersion.isAtLeast(MCVersion.V1_20_4)) {
         player.spigot().sendMessage(chatLine.toComponent());
      } else {
         player.spigot().sendMessage(chatLine.toComponentArray());
      }
   }

   private void sendEmptyLine() {
      if (this.sender instanceof Player player) {
         player.spigot().sendMessage(new BaseComponent[0]);
      } else {
         this.sender.sendMessage("");
      }
   }

   public void showPage(int page) {
      List<ChatLine> content = this.menu.getContent(this.sender);
      int totalPages = this.calculateTotalPages(content.size());
      if (page >= 1 && page <= totalPages) {
         int startIndex = (page - 1) * this.menu.getLinesPerPage();
         int endIndex = Math.min(startIndex + this.menu.getLinesPerPage(), content.size());
         if (this.menu.getHeader() != null) {
            String header = this.replacePlaceholders(this.menu.getHeader(), page, totalPages);
            this.sendMessage(new ChatLine(header));
            this.sendEmptyLine();
         }

         for (int i = startIndex; i < endIndex; i++) {
            this.sendMessage(content.get(i));
         }

         this.sendEmptyLine();
         if (this.menu.getNavigation() != null && totalPages > 1) {
            ChatLine navigationLine = this.buildNavigationLine(page, totalPages);
            this.sendMessage(navigationLine);
         }

         if (this.menu.getFooter() != null) {
            String footer = this.replacePlaceholders(this.menu.getFooter(), page, totalPages);
            this.sendMessage(new ChatLine(footer));
         }
      }
   }

   private int calculateTotalPages(int contentSize) {
      return (int)Math.ceil((double)contentSize / this.menu.getLinesPerPage());
   }

   private String replacePlaceholders(String text, int page, int totalPages) {
      String playerName = this.sender instanceof Player ? ((Player)this.sender).getName() : "Console";
      return ChatUtils.colorize(text.replace("{page}", String.valueOf(page)).replace("{total}", String.valueOf(totalPages)).replace("{player}", playerName));
   }

   private ChatLine buildNavigationLine(int currentPage, int totalPages) {
      NavigationButtons nav = this.menu.getNavigation();
      if (nav == null) {
         return new ChatLine("");
      } else {
         ChatLine.Builder builder = ChatLine.builder();
         if (currentPage > 1) {
            String prevText = ChatUtils.colorize(nav.generatePrevious(currentPage - 1));
            String prevCommand = nav.getNavigationPrefix() + " " + (currentPage - 1);
            String prevHover = ChatUtils.colorize("&7Click to go to page " + (currentPage - 1));
            builder.addClickable(prevText, ChatLine.ClickAction.runCommand(prevCommand), prevHover);
         }

         if (currentPage > 1) {
            builder.add("  ");
         }

         String pageIndicator = ChatUtils.colorize("&7Page " + currentPage + "/" + totalPages);
         builder.add(pageIndicator);
         if (currentPage < totalPages) {
            builder.add("  ");
         }

         if (currentPage < totalPages) {
            String nextText = ChatUtils.colorize(nav.generateNext(currentPage + 1));
            String nextCommand = nav.getNavigationPrefix() + " " + (currentPage + 1);
            String nextHover = ChatUtils.colorize("&7Click to go to page " + (currentPage + 1));
            builder.addClickable(nextText, ChatLine.ClickAction.runCommand(nextCommand), nextHover);
         }

         return builder.build();
      }
   }

   public CommandSender getSender() {
      return this.sender;
   }
}
