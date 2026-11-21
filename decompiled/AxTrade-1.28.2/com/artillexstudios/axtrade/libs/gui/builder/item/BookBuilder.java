package com.artillexstudios.axtrade.libs.gui.builder.item;

import com.artillexstudios.axtrade.libs.gui.components.exception.GuiException;
import com.artillexstudios.axtrade.libs.gui.components.util.Legacy;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BookBuilder extends BaseItemBuilder<BookBuilder> {
   private static final EnumSet<Material> BOOKS = EnumSet.of(Material.WRITABLE_BOOK, Material.WRITTEN_BOOK);

   BookBuilder(@NotNull ItemStack itemStack, @NotNull NameLoreHandler nameLoreHandler) {
      super(itemStack, nameLoreHandler);
      if (!BOOKS.contains(itemStack.getType())) {
         throw new GuiException("BookBuilder requires the material to be a WRITABLE_BOOK/WRITTEN_BOOK!");
      }
   }

   @NotNull
   @Contract("_ -> this")
   public BookBuilder author(@Nullable Component author) {
      BookMeta bookMeta = (BookMeta)this.getMeta();
      if (author == null) {
         bookMeta.setAuthor(null);
         this.setMeta(bookMeta);
         return this;
      } else {
         bookMeta.setAuthor(Legacy.SERIALIZER.serialize(author));
         this.setMeta(bookMeta);
         return this;
      }
   }

   @NotNull
   @Contract("_ -> this")
   public BookBuilder generation(@Nullable Generation generation) {
      BookMeta bookMeta = (BookMeta)this.getMeta();
      bookMeta.setGeneration(generation);
      this.setMeta(bookMeta);
      return this;
   }

   @NotNull
   @Contract("_ -> this")
   public BookBuilder page(@NotNull Component... pages) {
      return this.page(Arrays.asList(pages));
   }

   @NotNull
   @Contract("_ -> this")
   public BookBuilder page(@NotNull List<Component> pages) {
      BookMeta bookMeta = (BookMeta)this.getMeta();

      for (Component page : pages) {
         bookMeta.addPage(new String[]{Legacy.SERIALIZER.serialize(page)});
      }

      this.setMeta(bookMeta);
      return this;
   }

   @NotNull
   @Contract("_, _ -> this")
   public BookBuilder page(int page, @NotNull Component data) {
      BookMeta bookMeta = (BookMeta)this.getMeta();
      bookMeta.setPage(page, Legacy.SERIALIZER.serialize(data));
      this.setMeta(bookMeta);
      return this;
   }

   @NotNull
   @Contract("_ -> this")
   public BookBuilder title(@Nullable Component title) {
      BookMeta bookMeta = (BookMeta)this.getMeta();
      if (title == null) {
         bookMeta.setTitle(null);
         this.setMeta(bookMeta);
         return this;
      } else {
         bookMeta.setTitle(Legacy.SERIALIZER.serialize(title));
         this.setMeta(bookMeta);
         return this;
      }
   }
}
