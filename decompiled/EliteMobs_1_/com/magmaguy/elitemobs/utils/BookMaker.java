package com.magmaguy.elitemobs.utils;

import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookMaker {
   private BookMaker() {
   }

   public static ItemStack generateBook(Player player, List<String> pages) {
      ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
      BookMeta bookMeta = (BookMeta)writtenBook.getItemMeta();
      bookMeta.setTitle("EliteMobs Book");
      bookMeta.setAuthor("MagmaGuy");
      bookMeta.setPages(pages);
      writtenBook.setItemMeta(bookMeta);
      player.openBook(writtenBook);
      return writtenBook;
   }

   public static ItemStack generateBook(Player player, TextComponent[] pages) {
      ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
      BookMeta bookMeta = (BookMeta)writtenBook.getItemMeta();
      bookMeta.setTitle("EliteMobs Book");
      bookMeta.setAuthor("MagmaGuy");

      for (TextComponent textComponent : pages) {
         TextComponent[] stupid = new TextComponent[]{textComponent};
         bookMeta.spigot().addPage(new BaseComponent[][]{stupid});
      }

      writtenBook.setItemMeta(bookMeta);
      player.openBook(writtenBook);
      return writtenBook;
   }
}
