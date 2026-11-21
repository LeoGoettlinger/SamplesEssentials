package su.nightexpress.nightcore.ui.menu.data;

import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.language.entry.LangItem;
import su.nightexpress.nightcore.language.entry.LangUIButton;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.ui.menu.Menu;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.item.ItemClick;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.util.bukkit.NightItem;

@Deprecated
public interface Linked<T> extends Menu {
   boolean open(@NonNull Player var1, @NonNull T var2);

   boolean open(@NonNull Player var1, @NonNull T var2, @NonNull Consumer<MenuViewer> var3);

   @NonNull
   LinkCache<T> getCache();

   boolean isCached(@NonNull Player var1);

   T getLink(@NonNull MenuViewer var1);

   T getLink(@NonNull Player var1);

   ItemClick manageLink(@NonNull LinkHandler<T> var1);

   @Deprecated
   void addItem(@NonNull Material var1, @NonNull LangItem var2, int var3, @NonNull LinkHandler<T> var4);

   @Deprecated
   void addItem(@NonNull Material var1, @NonNull LangItem var2, int var3, @NonNull LinkHandler<T> var4, @Nullable ItemOptions var5);

   @Deprecated
   void addItem(@NonNull ItemStack var1, @NonNull LangItem var2, int var3, @NonNull LinkHandler<T> var4);

   @Deprecated
   void addItem(@NonNull ItemStack var1, @NonNull LangItem var2, int var3, @NonNull LinkHandler<T> var4, @Nullable ItemOptions var5);

   @Deprecated
   void addItem(@NonNull NightItem var1, @NonNull LangItem var2, int var3, @NonNull LinkHandler<T> var4);

   @Deprecated
   void addItem(@NonNull NightItem var1, @NonNull LangItem var2, int var3, @NonNull LinkHandler<T> var4, @Nullable ItemOptions var5);

   @Deprecated
   void addItem(@NonNull Material var1, @NonNull LangUIButton var2, int var3, @NonNull LinkHandler<T> var4);

   @Deprecated
   void addItem(@NonNull Material var1, @NonNull LangUIButton var2, int var3, @NonNull LinkHandler<T> var4, @Nullable ItemOptions var5);

   @Deprecated
   void addItem(@NonNull ItemStack var1, @NonNull LangUIButton var2, int var3, @NonNull LinkHandler<T> var4);

   @Deprecated
   void addItem(@NonNull ItemStack var1, @NonNull LangUIButton var2, int var3, @NonNull LinkHandler<T> var4, @Nullable ItemOptions var5);

   @Deprecated
   void addItem(@NonNull NightItem var1, @NonNull LangUIButton var2, int var3, @NonNull LinkHandler<T> var4);

   @Deprecated
   void addItem(@NonNull NightItem var1, @NonNull LangUIButton var2, int var3, @NonNull LinkHandler<T> var4, @Nullable ItemOptions var5);

   void addItem(@NonNull Material var1, @NonNull IconLocale var2, int var3, @NonNull LinkHandler<T> var4);

   void addItem(@NonNull Material var1, @NonNull IconLocale var2, int var3, @NonNull LinkHandler<T> var4, @Nullable ItemOptions var5);

   void addItem(@NonNull ItemStack var1, @NonNull IconLocale var2, int var3, @NonNull LinkHandler<T> var4);

   void addItem(@NonNull ItemStack var1, @NonNull IconLocale var2, int var3, @NonNull LinkHandler<T> var4, @Nullable ItemOptions var5);

   void addItem(@NonNull NightItem var1, @NonNull IconLocale var2, int var3, @NonNull LinkHandler<T> var4);

   void addItem(@NonNull NightItem var1, @NonNull IconLocale var2, int var3, @NonNull LinkHandler<T> var4, @Nullable ItemOptions var5);

   void addItem(@NonNull NightItem var1, int var2, @NonNull LinkHandler<T> var3);

   void addItem(@NonNull NightItem var1, int var2, @NonNull LinkHandler<T> var3, @Nullable ItemOptions var4);
}
