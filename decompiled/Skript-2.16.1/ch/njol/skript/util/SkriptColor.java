package ch.njol.skript.util;

import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.invoke.StringConcatFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum SkriptColor implements Color {
   BLACK(DyeColor.BLACK, ChatColor.BLACK),
   DARK_GREY(DyeColor.GRAY, ChatColor.DARK_GRAY),
   LIGHT_GREY(DyeColor.LIGHT_GRAY, ChatColor.GRAY),
   WHITE(DyeColor.WHITE, ChatColor.WHITE),
   DARK_BLUE(DyeColor.BLUE, ChatColor.DARK_BLUE),
   BROWN(DyeColor.BROWN, ChatColor.BLUE),
   DARK_CYAN(DyeColor.CYAN, ChatColor.DARK_AQUA),
   LIGHT_CYAN(DyeColor.LIGHT_BLUE, ChatColor.AQUA),
   DARK_GREEN(DyeColor.GREEN, ChatColor.DARK_GREEN),
   LIGHT_GREEN(DyeColor.LIME, ChatColor.GREEN),
   YELLOW(DyeColor.YELLOW, ChatColor.YELLOW),
   ORANGE(DyeColor.ORANGE, ChatColor.GOLD),
   DARK_RED(DyeColor.RED, ChatColor.DARK_RED),
   LIGHT_RED(DyeColor.PINK, ChatColor.RED),
   DARK_PURPLE(DyeColor.PURPLE, ChatColor.DARK_PURPLE),
   LIGHT_PURPLE(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE);

   private static final Map<String, SkriptColor> names = new HashMap<>();
   private static final Set<SkriptColor> colors = new HashSet<>();
   private static final String LANGUAGE_NODE = "colors";
   private static final Map<Character, SkriptColor> BY_CHAR = new HashMap<>();
   private ChatColor chat;
   private DyeColor dye;
   @Nullable
   private Adjective adjective;

   private SkriptColor(DyeColor dye, ChatColor chat) {
      this.chat = chat;
      this.dye = dye;
   }

   @Override
   public org.bukkit.Color asBukkitColor() {
      return this.dye.getColor();
   }

   @Override
   public int getAlpha() {
      return this.dye.getColor().getAlpha();
   }

   @Override
   public int getRed() {
      return this.dye.getColor().getRed();
   }

   @Override
   public int getGreen() {
      return this.dye.getColor().getGreen();
   }

   @Override
   public int getBlue() {
      return this.dye.getColor().getBlue();
   }

   @Override
   public DyeColor asDyeColor() {
      return this.dye;
   }

   @Override
   public String getName() {
      assert this.adjective != null;

      return this.adjective.toString();
   }

   @Override
   public Fields serialize() throws NotSerializableException {
      return new Fields(this, Variables.yggdrasil);
   }

   @Override
   public void deserialize(@NotNull Fields fields) throws StreamCorruptedException {
      this.dye = fields.getObject("dye", DyeColor.class);
      this.chat = fields.getObject("chat", ChatColor.class);

      try {
         this.adjective = fields.getObject("adjective", Adjective.class);
      } catch (StreamCorruptedException var3) {
      }
   }

   public String getFormattedChat() {
      return this.chat + "";
   }

   @Nullable
   public Adjective getAdjective() {
      return this.adjective;
   }

   public ChatColor asChatColor() {
      return this.chat;
   }

   @Deprecated(
      since = "2.3.6",
      forRemoval = true
   )
   public byte getWoolData() {
      return this.dye.getWoolData();
   }

   @Deprecated(
      since = "2.3.6",
      forRemoval = true
   )
   public byte getDyeData() {
      return (byte)(15 - this.dye.getWoolData());
   }

   private void setAdjective(@Nullable Adjective adjective) {
      this.adjective = adjective;
   }

   @Nullable
   public static SkriptColor fromName(String name) {
      return names.get(name);
   }

   public static SkriptColor fromDyeColor(DyeColor dye) {
      for (SkriptColor color : colors) {
         DyeColor c = color.asDyeColor();

         assert c != null;

         if (c.equals(dye)) {
            return color;
         }
      }

      assert false;

      return null;
   }

   public static SkriptColor fromBukkitColor(org.bukkit.Color color) {
      for (SkriptColor c : colors) {
         if (c.asBukkitColor().equals(color) || c.asDyeColor().getFireworkColor().equals(color)) {
            return c;
         }
      }

      return null;
   }

   @Deprecated(
      since = "2.3.6",
      forRemoval = true
   )
   @Nullable
   public static SkriptColor fromDyeData(short data) {
      if (data >= 0 && data < 16) {
         for (SkriptColor color : colors) {
            DyeColor c = color.asDyeColor();

            assert c != null;

            if (c.getDyeData() == data) {
               return color;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Deprecated(
      since = "2.3.6",
      forRemoval = true
   )
   @Nullable
   public static SkriptColor fromWoolData(short data) {
      if (data >= 0 && data < 16) {
         for (SkriptColor color : colors) {
            DyeColor c = color.asDyeColor();

            assert c != null;

            if (c.getWoolData() == data) {
               return color;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static String replaceColorChar(String s) {
      return s.replace('§', '&');
   }

   @Nullable
   public static SkriptColor fromColorChar(char character) {
      return BY_CHAR.get(character);
   }

   @Override
   public String toString() {
      return this.adjective == null
         ? StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.name())
         : this.adjective.toString(-1, 0);
   }

   static {
      colors.addAll(Arrays.asList(values()));
      Language.addListener(() -> {
         names.clear();

         for (SkriptColor color : values()) {
            String node = "colors." + color.name();
            color.setAdjective(new Adjective(node + ".adjective"));

            for (String name : Language.getList(node + ".names")) {
               names.put(name.toLowerCase(Locale.ENGLISH), color);
            }

            BY_CHAR.put(color.asChatColor().getChar(), color);
         }
      });
      Variables.yggdrasil.registerSingleClass(SkriptColor.class, "SkriptColor");
      Variables.yggdrasil.registerSingleClass(DyeColor.class, "DyeColor");
   }
}
