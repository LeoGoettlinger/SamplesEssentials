package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.VersionUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.WritableBookMeta;

public class Commandbook extends EssentialsCommand {
   private static final Material WRITABLE_BOOK = EnumUtil.getMaterial("WRITABLE_BOOK", "BOOK_AND_QUILL");

   public Commandbook() {
      super("book");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      ItemStack item = Inventories.getItemInMainHand(user.getBase());
      String player = user.getName();
      if (item.getType() == Material.WRITTEN_BOOK) {
         BookMeta bmeta = (BookMeta)item.getItemMeta();
         if (args.length > 1 && args[0].equalsIgnoreCase("author")) {
            if (!user.isAuthorized("essentials.book.author") || !this.isAuthor(bmeta, player) && !user.isAuthorized("essentials.book.others")) {
               throw new TranslatableException("denyChangeAuthor");
            }

            String newAuthor = FormatUtil.formatString(user, "essentials.book.author", getFinalArg(args, 1)).trim();
            bmeta.setAuthor(newAuthor);
            item.setItemMeta(bmeta);
            user.sendTl("bookAuthorSet", newAuthor);
         } else if (args.length > 1 && args[0].equalsIgnoreCase("title")) {
            if (!user.isAuthorized("essentials.book.title") || !this.isAuthor(bmeta, player) && !user.isAuthorized("essentials.book.others")) {
               throw new TranslatableException("denyChangeTitle");
            }

            String newTitle = FormatUtil.formatString(user, "essentials.book.title", getFinalArg(args, 1)).trim();
            bmeta.setTitle(newTitle);
            item.setItemMeta(bmeta);
            user.sendTl("bookTitleSet", newTitle);
         } else {
            if (!this.isAuthor(bmeta, player) && !user.isAuthorized("essentials.book.others")) {
               throw new TranslatableException("denyBookEdit");
            }

            ItemStack newItem = new ItemStack(WRITABLE_BOOK, item.getAmount());
            if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_20_6_R01)) {
               WritableBookMeta wbmeta = (WritableBookMeta)newItem.getItemMeta();
               wbmeta.setPages(bmeta.getPages());
               newItem.setItemMeta(wbmeta);
            } else {
               newItem.setItemMeta(bmeta);
            }

            Inventories.setItemInMainHand(user.getBase(), newItem);
            user.sendTl("editBookContents");
         }
      } else {
         if (item.getType() != WRITABLE_BOOK) {
            throw new TranslatableException("holdBook");
         }

         BookMeta bmeta = (BookMeta)item.getItemMeta();
         if (!user.isAuthorized("essentials.book.author")) {
            bmeta.setAuthor(player);
         }

         ItemStack newItem = new ItemStack(Material.WRITTEN_BOOK, item.getAmount());
         if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_20_6_R01)) {
            BookMeta real = (BookMeta)newItem.getItemMeta();
            real.setAuthor(bmeta.getAuthor());
            real.setTitle(bmeta.getTitle());
            real.setPages(bmeta.getPages());
            newItem.setItemMeta(real);
         } else {
            newItem.setItemMeta(bmeta);
         }

         Inventories.setItemInMainHand(user.getBase(), newItem);
         user.sendTl("bookLocked");
      }
   }

   private boolean isAuthor(final BookMeta bmeta, final String player) {
      String author = bmeta.getAuthor();
      return author != null && author.equalsIgnoreCase(player);
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         List<String> options = Lists.newArrayList(new String[]{"sign", "unsign"});
         if (user.isAuthorized("essentials.book.author")) {
            options.add("author");
         }

         if (user.isAuthorized("essentials.book.title")) {
            options.add("title");
         }

         return options;
      } else if (args.length == 2 && args[0].equalsIgnoreCase("author") && user.isAuthorized("essentials.book.author")) {
         List<String> optionsx = this.getPlayers(user);
         optionsx.add("Herobrine");
         return optionsx;
      } else {
         return Collections.emptyList();
      }
   }
}
