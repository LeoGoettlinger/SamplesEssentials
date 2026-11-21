package me.chancesd.pvpmanager.sdutils.display.chat;

import java.util.List;
import me.chancesd.pvpmanager.sdutils.display.chat.content.ContentProvider;
import me.chancesd.pvpmanager.sdutils.display.chat.content.StaticContentProvider;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatMenu {
   private final String header;
   private final String footer;
   private final int linesPerPage;
   private final NavigationButtons navigation;
   private final ContentProvider contentProvider;

   private ChatMenu(ChatMenu.Builder builder) {
      this.header = builder.header;
      this.footer = builder.footer;
      this.linesPerPage = builder.linesPerPage;
      this.navigation = builder.navigation;
      this.contentProvider = (ContentProvider)(builder.contentProvider != null ? builder.contentProvider : new StaticContentProvider());
   }

   public void show(@NotNull CommandSender sender) {
      this.show(sender, 1);
   }

   public void show(@NotNull CommandSender sender, int page) {
      MenuSession session = new MenuSession(sender, this);
      session.showPage(page);
   }

   @NotNull
   public List<ChatLine> getContent(@NotNull CommandSender sender) {
      return this.contentProvider.getContent(sender);
   }

   public static ChatMenu.Builder builder() {
      return new ChatMenu.Builder();
   }

   @Nullable
   public String getHeader() {
      return this.header;
   }

   @Nullable
   public String getFooter() {
      return this.footer;
   }

   public int getLinesPerPage() {
      return this.linesPerPage;
   }

   @Nullable
   public NavigationButtons getNavigation() {
      return this.navigation;
   }

   @NotNull
   public ContentProvider getContentProvider() {
      return this.contentProvider;
   }

   public static class Builder {
      private String header;
      private String footer;
      private int linesPerPage = 10;
      private NavigationButtons navigation;
      private ContentProvider contentProvider;

      public ChatMenu.Builder header(String header) {
         this.header = header;
         return this;
      }

      public ChatMenu.Builder footer(String footer) {
         this.footer = footer;
         return this;
      }

      public ChatMenu.Builder linesPerPage(int linesPerPage) {
         this.linesPerPage = linesPerPage;
         return this;
      }

      public ChatMenu.Builder navigation(NavigationButtons navigation) {
         this.navigation = navigation;
         return this;
      }

      public ChatMenu.Builder contentProvider(ContentProvider provider) {
         this.contentProvider = provider;
         return this;
      }

      public ChatMenu build() {
         return new ChatMenu(this);
      }
   }
}
