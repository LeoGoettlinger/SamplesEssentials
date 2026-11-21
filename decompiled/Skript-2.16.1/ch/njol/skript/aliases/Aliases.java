package ch.njol.skript.aliases;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.localization.RegexMessage;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.StringConcatFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

public abstract class Aliases {
   private static final AliasesProvider provider = createProvider(10000, null);
   private static final AliasesParser parser = createParser(provider);
   private static final ItemType everything = new ItemType();
   private static final Message m_empty_string = new Message("aliases.empty string");
   private static final ArgsMessage m_invalid_item_type = new ArgsMessage("aliases.invalid item type");
   private static final Message m_outside_section = new Message("aliases.outside section");
   private static final RegexMessage p_any = new RegexMessage("aliases.any", "", " (.+)", 2);
   private static final RegexMessage p_every = new RegexMessage("aliases.every", "", " (.+)", 2);
   private static final RegexMessage p_of_every = new RegexMessage("aliases.of every", "(\\d+) ", " (.+)", 2);
   private static final RegexMessage p_of = new RegexMessage("aliases.of", "(\\d+) (?:", " )?(.+)", 2);
   private static final Map<String, ItemType> trackedTypes = new HashMap<>();
   private static final boolean noHardExceptions = SkriptConfig.apiSoftExceptions.value();
   static String itemSingular = "item";
   static String itemPlural = "items";
   static String blockSingular = "block";
   static String blockPlural = "blocks";

   @Nullable
   private static ItemType getAlias_i(String s) {
      ScriptAliases aliases = getScriptAliases();
      return aliases != null ? aliases.provider.getAlias(s) : provider.getAlias(s);
   }

   private static AliasesProvider createProvider(int expectedCount, @Nullable AliasesProvider parent) {
      return new AliasesProvider(expectedCount, parent);
   }

   private static AliasesParser createParser(AliasesProvider provider) {
      AliasesParser parser = new AliasesParser(provider);
      parser.registerCondition("minecraft version", str -> {
         int orNewer = str.indexOf("or newer");
         if (orNewer != -1) {
            Version ver = new Version(str.substring(0, orNewer - 1));
            return Skript.getMinecraftVersion().compareTo(ver) >= 0;
         } else {
            int orOlder = str.indexOf("or older");
            if (orOlder != -1) {
               Version ver = new Version(str.substring(0, orOlder - 1));
               return Skript.getMinecraftVersion().compareTo(ver) <= 0;
            } else {
               int to = str.indexOf("to");
               if (to == -1) {
                  return Skript.getMinecraftVersion().equals(new Version(str));
               } else {
                  Version first = new Version(str.substring(0, to - 1));
                  Version second = new Version(str.substring(to + 3));
                  Version current = Skript.getMinecraftVersion();
                  return current.compareTo(first) >= 0 && current.compareTo(second) <= 0;
               }
            }
         }
      });
      return parser;
   }

   static String concatenate(String... parts) {
      assert parts.length >= 2;

      StringBuilder b = new StringBuilder();

      for (int i = 0; i < parts.length; i++) {
         if (!parts[i].isEmpty()) {
            if (b.length() == 0) {
               b.append(parts[i]);
            } else {
               char c = parts[i].charAt(0);
               if (Character.isUpperCase(c) && b.charAt(b.length() - 1) != ' ') {
                  b.append(Character.toLowerCase(c) + parts[i].substring(1));
               } else {
                  b.append(parts[i]);
               }
            }
         }
      }

      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(b.toString().replace("  ", " ").trim());
   }

   @Nullable
   private static MaterialName getMaterialNameData(ItemData type) {
      ScriptAliases aliases = getScriptAliases();
      return aliases != null ? aliases.provider.getMaterialName(type) : provider.getMaterialName(type);
   }

   public static String getMaterialName(ItemData type, boolean plural) {
      MaterialName name = getMaterialNameData(type);
      return name == null ? type.type + "" : name.toString(plural);
   }

   public static int getGender(ItemData item) {
      MaterialName n = getMaterialNameData(item);
      return n != null ? n.gender : -1;
   }

   @Nullable
   public static ItemType parseAlias(String s) {
      if (s.isEmpty()) {
         Skript.error(m_empty_string.toString());
         return null;
      } else if (s.equals("*")) {
         return everything;
      } else {
         ItemType t = new ItemType();
         String[] types = s.split("\\s*,\\s*");

         for (String type : types) {
            if (type == null || parseType(type, t, true) == null) {
               return null;
            }
         }

         return t;
      }
   }

   @Nullable
   public static ItemType parseItemType(String s) {
      if (s.isEmpty()) {
         return null;
      } else {
         s = s.trim();
         ItemType t = new ItemType();
         Matcher m;
         if ((m = p_of_every.matcher(s)).matches()) {
            t.setAmount(Utils.parseInt(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(m.group(1))));
            t.setAll(true);
            s = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(m.group(m.groupCount()));
         } else if ((m = p_of.matcher(s)).matches()) {
            t.setAmount(Utils.parseInt(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(m.group(1))));
            s = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(m.group(m.groupCount()));
         } else if ((m = p_every.matcher(s)).matches()) {
            t.setAll(true);
            s = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(m.group(m.groupCount()));
         } else {
            int l = s.length();
            s = Noun.stripIndefiniteArticle(s);
            if (s.length() != l) {
               t.setAmount(1);
            }
         }

         String lc = s.toLowerCase(Locale.ENGLISH);
         String of = Language.getSpaced("of").toLowerCase();
         int c = -1;

         label80:
         while ((c = lc.indexOf(of, c + 1)) != -1) {
            ItemType t2 = t.clone();

            try (BlockingLogHandler ignored = new BlockingLogHandler().start()) {
               if (parseType(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s.substring(0, c)), t2, false) == null) {
                  continue;
               }
            }

            if (t2.numTypes() != 0) {
               String[] enchs = lc.substring(c + of.length()).split("\\s*(,|" + Pattern.quote(Language.get("and")) + ")\\s*");

               for (String ench : enchs) {
                  EnchantmentType e = EnchantmentType.parse(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(ench));
                  if (e == null) {
                     continue label80;
                  }

                  t2.addEnchantments(e);
               }

               return t2;
            }
         }

         if (parseType(s, t, false) == null) {
            return null;
         } else {
            return t.numTypes() == 0 ? null : t;
         }
      }
   }

   @Nullable
   private static ItemType parseType(String s, ItemType t, boolean isAlias) {
      if (s.isEmpty()) {
         t.add(new ItemData(Material.AIR));
         return t;
      } else if (s.matches("\\d+")) {
         return null;
      } else {
         ItemType i;
         if ((i = getAlias(s)) == null) {
            if (isAlias) {
               Skript.error(m_invalid_item_type.toString(s));
            }

            return null;
         } else {
            for (ItemData d : i) {
               t.add(d.clone());
            }

            return t;
         }
      }
   }

   @Nullable
   private static ItemType getAlias(String rawInput) {
      String input = rawInput.toLowerCase(Locale.ENGLISH).trim();
      ItemType itemType = getAlias_i(input);
      if (itemType != null) {
         return itemType.clone();
      } else {
         if ((input.contains(":") || input.contains("_")) && !input.contains(" ")) {
            NamespacedKey namespacedKey = NamespacedKey.fromString(input);
            if (namespacedKey != null) {
               Material material = (Material)Registry.MATERIAL.get(namespacedKey);
               if (material != null) {
                  return new ItemType(material);
               }
            }
         }

         if (input.endsWith(" " + blockSingular) || input.endsWith(" " + blockPlural)) {
            String stripped = input.substring(0, input.lastIndexOf(" "));
            itemType = getAlias_i(stripped);
            if (itemType != null) {
               itemType = itemType.clone();

               for (int j = 0; j < itemType.numTypes(); j++) {
                  ItemData d = itemType.getTypes().get(j);
                  if (!d.getType().isBlock() || d.getType().getKey().getKey().endsWith(blockSingular)) {
                     itemType.remove(d);
                     j--;
                  }
               }

               if (itemType.getTypes().isEmpty()) {
                  return null;
               }

               return itemType;
            }
         } else if (input.endsWith(" " + itemSingular) || input.endsWith(" " + itemPlural)) {
            String stripped = input.substring(0, input.lastIndexOf(" "));
            itemType = getAlias_i(stripped);
            if (itemType != null) {
               itemType = itemType.clone();

               for (int jx = 0; jx < itemType.numTypes(); jx++) {
                  ItemData data = itemType.getTypes().get(jx);
                  if (!data.isAnything && !data.getType().isItem()) {
                     itemType.remove(data);
                     jx--;
                  }
               }

               if (itemType.getTypes().isEmpty()) {
                  return null;
               }

               return itemType;
            }
         }

         return null;
      }
   }

   public static void clear() {
      provider.clearAliases();
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public static void load() {
      try {
         long start = System.currentTimeMillis();
         loadInternal();
         Skript.info("Loaded " + provider.getAliasCount() + " aliases in " + (System.currentTimeMillis() - start) + "ms");
      } catch (IOException var2) {
         Skript.exception(var2);
      }
   }

   public static CompletableFuture<Boolean> loadAsync() {
      ItemUtils.applyComponentData(ItemStack.empty(), "[]");
      return CompletableFuture.supplyAsync(() -> {
         try {
            long start = System.currentTimeMillis();
            loadInternal();
            Skript.info("Loaded " + provider.getAliasCount() + " aliases in " + (System.currentTimeMillis() - start) + "ms");
            return true;
         } catch (StackOverflowError var2) {
            if (System.getProperty("java.vm.name").contains("32")) {
               Skript.error("");
               Skript.error("There was a StackOverflowError that occurred while loading aliases.");
               Skript.error("As you are currently using 32-bit Java, please update to 64-bit Java to resolve the error.");
               Skript.error("Please report this issue to our GitHub only if updating to 64-bit Java does not fix the issue.");
               Skript.error("");
            } else {
               Skript.exception(var2);
               Bukkit.getPluginManager().disablePlugin(Skript.getInstance());
            }

            return false;
         } catch (IOException var3) {
            Skript.exception(var3);
            return false;
         }
      });
   }

   private static void loadMissingAliases() {
      if (Skript.methodExists(Material.class, "getKey")) {
         boolean modItemRegistered = false;

         for (Material material : Material.values()) {
            if (!material.isLegacy() && !provider.hasAliasForMaterial(material)) {
               NamespacedKey key = material.getKey();
               if ("minecraft".equals(key.getNamespace())) {
                  parser.loadAlias(key.getKey().replace("_", " ") + "¦s", key.toString());
               } else {
                  if (!modItemRegistered) {
                     modItemRegistered = true;
                  }

                  parser.loadAlias((key.getNamespace() + "'s " + key.getKey() + "¦s").replace("_", " "), key.toString());
                  parser.loadAlias((key.getKey() + "¦s from " + key.getNamespace()).replace("_", " "), key.toString());
               }

               Skript.debug(ChatColor.YELLOW + "Creating temporary alias for: " + key);
            }
         }

         if (modItemRegistered) {
            Skript.warning("==============================================================");
            Skript.warning("Some materials were found that seem to be modded.");
            Skript.warning("An item that has the id 'mod:item' can be used as 'mod's item' or 'item from mod'.");
            Skript.warning("WARNING: Skript does not officially support any modded servers.");
            Skript.warning("Any issues you encounter related to modded items will be your responsibility to fix.");
            Skript.warning("==============================================================");
         }
      }
   }

   private static void loadInternal() throws IOException {
      Path dataFolder = Skript.getInstance().getDataFolder().toPath();
      Path zipPath = dataFolder.resolve("aliases-english.zip");
      if (SkriptConfig.loadDefaultAliases.value()) {
         if (Files.exists(zipPath)) {
            try (FileSystem zipFs = FileSystems.newFileSystem(zipPath, Skript.class.getClassLoader())) {
               assert zipFs != null;

               Path aliasesPath = zipFs.getPath("/");

               assert aliasesPath != null;

               loadDirectory(aliasesPath);
            }
         } else {
            try {
               URI jarUri = Skript.class.getProtectionDomain().getCodeSource().getLocation().toURI();

               try (FileSystem zipFs = FileSystems.newFileSystem(Paths.get(jarUri), Skript.class.getClassLoader())) {
                  assert zipFs != null;

                  Path aliasesPath = zipFs.getPath("/", "aliases-english");

                  assert aliasesPath != null;

                  loadDirectory(aliasesPath);
               }
            } catch (URISyntaxException var10) {
               assert false;
            }
         }
      }

      Path aliasesFolder = dataFolder.resolve("aliases");
      if (Files.exists(aliasesFolder)) {
         assert aliasesFolder != null;

         loadDirectory(aliasesFolder);
      }

      loadMissingAliases();

      for (Entry<String, ItemType> entry : trackedTypes.entrySet()) {
         ItemType type = parseItemType(entry.getKey());
         if (type == null) {
            Skript.warning(
               "Alias '" + entry.getKey() + "' is required by Skript, but does not exist anymore. Make sure to fix this before restarting the server."
            );
         } else {
            entry.getValue().setTo(type);
         }
      }
   }

   public static void loadDirectory(Path dir) throws IOException {
      try {
         Files.list(dir).sorted().forEach(f -> {
            assert f != null;

            try {
               String name = f.getFileName().toString();
               if (Files.isDirectory(f) && !name.startsWith(".")) {
                  loadDirectory(f);
               } else if (name.endsWith(".sk")) {
                  load(f);
               }
            } catch (IOException var2x) {
               throw new UncheckedIOException(var2x);
            }
         });
      } catch (UncheckedIOException var2) {
         throw var2.getCause();
      }
   }

   public static void load(Path f) throws IOException {
      Config config = new Config(f, false, false, "=");
      load(config);
   }

   public static void load(Config config) {
      for (Node n : config.getMainNode()) {
         if (!(n instanceof SectionNode)) {
            Skript.error(m_outside_section.toString());
         } else {
            parser.load((SectionNode)n);
         }
      }
   }

   @Nullable
   public static String getMinecraftId(ItemData data) {
      ScriptAliases aliases = getScriptAliases();
      return aliases != null ? aliases.provider.getMinecraftId(data) : provider.getMinecraftId(data);
   }

   @Nullable
   public static EntityData<?> getRelatedEntity(ItemData data) {
      ScriptAliases aliases = getScriptAliases();
      return aliases != null ? aliases.provider.getRelatedEntity(data) : provider.getRelatedEntity(data);
   }

   @Deprecated(
      since = "2.9.0",
      forRemoval = true
   )
   public static ItemType javaItemType(String name) {
      ItemType type = parseItemType(name);
      if (type == null) {
         if (!noHardExceptions) {
            throw new IllegalArgumentException("type " + name + " not found");
         }

         Skript.error("type " + name + " not found");
         type = new ItemType();
      }

      trackedTypes.put(name, type);
      return type;
   }

   public static AliasesProvider getAddonProvider(@Nullable SkriptAddon addon) {
      if (addon == null) {
         throw new IllegalArgumentException("addon needed");
      } else {
         return provider;
      }
   }

   public static ScriptAliases createScriptAliases(Script script) {
      AliasesProvider localProvider = createProvider(10, provider);
      ScriptAliases aliases = new ScriptAliases(localProvider, createParser(localProvider));
      script.addData(aliases);
      return aliases;
   }

   @Nullable
   private static ScriptAliases getScriptAliases() {
      ParserInstance parser = ParserInstance.get();
      return parser.isActive() ? getScriptAliases(parser.getCurrentScript()) : null;
   }

   @Nullable
   public static ScriptAliases getScriptAliases(Script script) {
      return script.getData(ScriptAliases.class);
   }

   static {
      everything.setAll(true);
      ItemData all = new ItemData(Material.AIR);
      all.isAnything = true;
      everything.add(all);
   }
}
