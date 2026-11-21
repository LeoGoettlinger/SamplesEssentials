package su.nightexpress.nightcore.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.language.entry.LangItem;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderList;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;
import su.nightexpress.nightcore.util.placeholder.Replacer;
import su.nightexpress.nightcore.util.text.night.NightMessage;

@Deprecated
public class ItemReplacer {
   private final ItemStack item;
   private final ItemMeta meta;
   private final Replacer replacer;
   private String displayName;
   private List<String> lore;
   @Deprecated
   private boolean trimLore;
   private boolean hideFlags;

   public ItemReplacer(@NonNull ItemStack item) {
      this(item, item.getItemMeta());
   }

   public ItemReplacer(@NonNull ItemMeta meta) {
      this(null, meta);
   }

   public ItemReplacer(@Nullable ItemStack item, @Nullable ItemMeta meta) {
      this.item = item;
      this.meta = meta;
      this.replacer = new Replacer();
   }

   @NonNull
   public static ItemReplacer create(@NonNull ItemStack item) {
      return new ItemReplacer(item);
   }

   @NonNull
   public static ItemReplacer create(@NonNull ItemMeta meta) {
      return new ItemReplacer(meta);
   }

   @NonNull
   public ItemReplacer readMeta() {
      if (this.hasMeta()) {
         this.setDisplayName(this.meta.getDisplayName());
         this.setLore(this.meta.getLore());
      }

      return this;
   }

   @NonNull
   public ItemReplacer readLocale(@NonNull LangItem locale) {
      if (this.hasMeta()) {
         this.setDisplayName(locale.getLocalizedName());
         this.setLore(locale.getLocalizedLore());
      }

      return this;
   }

   public void writeMeta() {
      if (this.hasMeta()) {
         if (this.displayName != null) {
            this.meta.setDisplayName(NightMessage.asLegacy(this.replacer.apply(this.displayName)));
         }

         if (this.lore != null) {
            this.replacer.getReplacers().forEach(this::replaceLore);
            this.meta.setLore(this.packTrimmedLore());
         }

         if (this.hasItem()) {
            this.item.setItemMeta(this.meta);
         }

         if (this.isHideFlags()) {
            ItemUtil.hideAttributes(this.item);
         }

         this.replacer.clear();
      }
   }

   @Deprecated
   public static void replace(@NonNull ItemMeta meta, @NonNull UnaryOperator<String> replacer) {
      create(meta).trimmed().readMeta().replace(replacer).writeMeta();
   }

   @Deprecated
   public static void replace(@NonNull ItemStack item, @NonNull PlaceholderMap replacer) {
      create(item).trimmed().readMeta().replace(replacer).writeMeta();
   }

   @Deprecated
   public static void replace(@NonNull ItemMeta meta, @NonNull PlaceholderMap replacer) {
      create(meta).trimmed().readMeta().replace(replacer).writeMeta();
   }

   @Deprecated
   public static void replacePlaceholderAPI(@NonNull ItemMeta meta, @NonNull Player player) {
      create(meta).trimmed().readMeta().replacePlaceholderAPI(player).writeMeta();
   }

   public static void replace(@NonNull ItemStack itemStack, @NonNull UnaryOperator<String> operator) {
      create(itemStack).readMeta().replacement(replacer -> replacer.replace(operator)).writeMeta();
   }

   public static <T> void replace(@NonNull ItemStack itemStack, @NonNull PlaceholderList<T> placeholderList, @NonNull T source) {
      create(itemStack).readMeta().replacement(replacer -> replacer.replace(source, placeholderList)).writeMeta();
   }

   public static void replacePlaceholderAPI(@NonNull ItemStack item, @NonNull Player player) {
      create(item).readMeta().replacement(replacer -> replacer.replacePlaceholderAPI(player)).writeMeta();
   }

   public boolean hasMeta() {
      return this.meta != null;
   }

   public boolean hasItem() {
      return this.item != null;
   }

   @Deprecated
   public boolean isTrimLore() {
      return this.trimLore;
   }

   public boolean isHideFlags() {
      return this.hideFlags;
   }

   @Deprecated
   @NonNull
   public ItemReplacer trimmed() {
      this.setTrimLore(true);
      return this;
   }

   @NonNull
   public ItemReplacer hideFlags() {
      this.hideFlags = true;
      return this;
   }

   @Deprecated
   @NonNull
   public ItemReplacer setHideFlags(boolean hideFlags) {
      this.hideFlags = hideFlags;
      return this;
   }

   @Deprecated
   @NonNull
   public ItemReplacer setTrimLore(boolean trimLore) {
      this.trimLore = trimLore;
      return this;
   }

   @Deprecated
   @NonNull
   private ItemReplacer cachePlaceholder(@NonNull String placeholder, @NonNull Supplier<String> value) {
      this.replacer.replace(placeholder, value);
      return this;
   }

   @Deprecated
   @NonNull
   private ItemReplacer cachePlaceholder(@NonNull String placeholder, @NonNull String value) {
      this.replacer.replace(placeholder, value);
      return this;
   }

   @Deprecated
   @NonNull
   private ItemReplacer cacheOperator(@NonNull UnaryOperator<String> operator) {
      this.replacer.replace(operator);
      return this;
   }

   @Deprecated
   @NonNull
   public ItemReplacer replace(@NonNull String placeholder, @NonNull String value) {
      return this.cachePlaceholder(placeholder, value);
   }

   @Deprecated
   @NonNull
   public ItemReplacer replace(@NonNull String placeholder, @NonNull Supplier<String> value) {
      return this.cachePlaceholder(placeholder, value);
   }

   @Deprecated
   @NonNull
   public ItemReplacer replace(@NonNull String placeholder, @NonNull List<String> replacer) {
      return this.cacheOperator(str -> str.replace(placeholder, String.join("\n", replacer)));
   }

   @Deprecated
   @NonNull
   public <T> ItemReplacer replace(@NonNull String placeholder, @NonNull PlaceholderList<T> placeholderList, @NonNull T source) {
      return this.cacheOperator(placeholderList.replacer(source));
   }

   public ItemReplacer replacement(@NonNull Consumer<Replacer> consumer) {
      consumer.accept(this.replacer);
      return this;
   }

   @Deprecated
   @NonNull
   public ItemReplacer replace(PlaceholderMap... placeholderMaps) {
      for (PlaceholderMap placeholder : placeholderMaps) {
         this.replacer.replace(placeholder.replacer());
      }

      return this;
   }

   @Deprecated
   @NonNull
   public ItemReplacer replace(@NonNull UnaryOperator<String> replacer) {
      return this.cacheOperator(replacer);
   }

   @Deprecated
   @NonNull
   public ItemReplacer replacePlaceholderAPI(@NonNull Player player) {
      if (Plugins.hasPlaceholderAPI()) {
         this.cacheOperator(str -> CommonPlaceholders.setPAPIPlaceholders(player, str));
      }

      return this;
   }

   @Deprecated
   @NonNull
   public ItemReplacer replaceLoreExact(@NonNull String placeholder, @NonNull List<String> replacer) {
      return this.replace(placeholder, replacer);
   }

   @Deprecated
   @NonNull
   public ItemReplacer replaceLore(@NonNull String placeholder, @NonNull Supplier<List<String>> replacer) {
      return this.replace(placeholder, replacer.get());
   }

   @Deprecated
   @NonNull
   public ItemReplacer injectLore(@NonNull String placeholder, @NonNull List<String> replacer) {
      return this.replace(placeholder, replacer);
   }

   @Deprecated
   @NonNull
   public ItemReplacer injectPlaceholderAPI(@NonNull Player player) {
      return this.replacePlaceholderAPI(player);
   }

   @Deprecated
   @NonNull
   public ItemReplacer replaceLoreTrail(@NonNull String placeholder, @NonNull List<String> replacer) {
      if (this.getLore() == null) {
         return this;
      } else {
         List<String> loreReplaced = new ArrayList<>();

         for (String lineHas : this.packLore()) {
            if (lineHas.contains(placeholder)) {
               replacer.forEach(lineRep -> loreReplaced.add(lineHas.replace(placeholder, lineRep)));
            } else {
               loreReplaced.add(lineHas);
            }
         }

         this.setLore(loreReplaced);
         return this;
      }
   }

   @NonNull
   public List<String> packLore() {
      return (List<String>)(this.lore == null ? new ArrayList<>() : this.lore);
   }

   private void replaceLore(@NonNull UnaryOperator<String> operator) {
      List<String> replaced = new ArrayList<>();

      for (String line : this.lore) {
         if (!line.isBlank()) {
            line = operator.apply(line);
            if (!line.isBlank()) {
               replaced.addAll(Arrays.asList(line.split("\n")));
            }
         } else {
            replaced.add(line);
         }
      }

      this.setLore(replaced);
   }

   @NonNull
   public List<String> packTrimmedLore() {
      List<String> lore = this.packLore();
      if (this.isTrimLore()) {
         lore = Lists.stripEmpty(lore);
      }

      List<String> fixed = this.addEmptyLines(lore);
      return Lists.modify(fixed, NightMessage::asLegacy);
   }

   @NonNull
   private List<String> addEmptyLines(@NonNull List<String> lore) {
      for (int index = 0; index < lore.size(); index++) {
         String line = lore.get(index);
         if (line.equalsIgnoreCase("%empty-if-above%")) {
            if (index != 0 && !this.isEmpty(lore.get(index - 1))) {
               lore.set(index, "");
            } else {
               lore.remove(index);
            }

            return this.addEmptyLines(lore);
         }

         if (line.equalsIgnoreCase("%empty-if-below%")) {
            if (index != lore.size() - 1 && !this.isEmpty(lore.get(index + 1))) {
               lore.set(index, "");
            } else {
               lore.remove(index);
            }

            return this.addEmptyLines(lore);
         }
      }

      return lore;
   }

   private boolean isEmpty(@NonNull String line) {
      return line.isBlank() || line.equalsIgnoreCase("%empty-if-above%") || line.equalsIgnoreCase("%empty-if-below%");
   }

   @Nullable
   public String getDisplayName() {
      return this.displayName;
   }

   @NonNull
   public ItemReplacer setDisplayName(@Nullable String displayName) {
      this.displayName = displayName;
      return this;
   }

   @Nullable
   public List<String> getLore() {
      return this.lore;
   }

   @NonNull
   public ItemReplacer setLore(@Nullable List<String> lore) {
      this.lore = lore;
      return this;
   }

   @NonNull
   public ItemReplacer setLore(@Nullable String lore) {
      this.lore = lore == null ? null : Lists.newList(lore.split("\n"));
      return this;
   }
}
