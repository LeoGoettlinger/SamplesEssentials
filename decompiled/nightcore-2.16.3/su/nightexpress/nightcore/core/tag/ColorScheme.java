package su.nightexpress.nightcore.core.tag;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.text.night.ParserUtils;

public class ColorScheme implements Writeable {
   public static final String DEFAULT = "default";
   public static final String CUSTOM = "custom";
   private final String id;
   private final List<ColorCode> colors;

   public ColorScheme(@NonNull String id, @NonNull List<ColorCode> colors) {
      this.id = id.toLowerCase();
      this.colors = colors;
   }

   @NonNull
   public static ColorScheme read(@NonNull FileConfig config, @NonNull String path, @NonNull String id) {
      List<ColorCode> colors = new ArrayList<>();
      config.getSection(path + ".Colors").forEach(name -> {
         String code = config.getString(path + ".Colors." + name);
         if (code != null && code.length() == 7) {
            Color color = ParserUtils.colorFromHexString(code);
            colors.add(new ColorCode(name.toLowerCase(), color));
         }
      });
      return new ColorScheme(id, colors);
   }

   @NonNull
   public static List<ColorScheme> getDefaultSchemes() {
      ColorScheme vanilla = new ColorScheme(
         "default",
         Lists.newList(
            new ColorCode("black", new Color(0, 0, 0)),
            new ColorCode("white", new Color(255, 255, 255)),
            new ColorCode("gray", new Color(170, 170, 170)),
            new ColorCode("soft_gray", new Color(170, 170, 170)),
            new ColorCode("dark_gray", new Color(70, 70, 70)),
            new ColorCode("red", new Color(255, 85, 85)),
            new ColorCode("soft_red", new Color(255, 85, 85)),
            new ColorCode("dark_red", new Color(170, 0, 0)),
            new ColorCode("green", new Color(85, 255, 85)),
            new ColorCode("soft_green", new Color(85, 255, 85)),
            new ColorCode("dark_green", new Color(0, 170, 0)),
            new ColorCode("blue", new Color(85, 85, 255)),
            new ColorCode("soft_blue", new Color(85, 85, 255)),
            new ColorCode("dark_blue", new Color(0, 0, 170)),
            new ColorCode("yellow", new Color(255, 255, 85)),
            new ColorCode("soft_yellow", new Color(255, 255, 85)),
            new ColorCode("dark_yellow", new Color(180, 180, 50)),
            new ColorCode("orange", new Color(255, 170, 0)),
            new ColorCode("soft_orange", new Color(230, 170, 50)),
            new ColorCode("gold", new Color(255, 170, 0)),
            new ColorCode("aqua", new Color(85, 255, 255)),
            new ColorCode("soft_aqua", new Color(85, 255, 255)),
            new ColorCode("dark_aqua", new Color(0, 170, 170)),
            new ColorCode("purple", new Color(255, 85, 255)),
            new ColorCode("soft_purple", new Color(255, 85, 255)),
            new ColorCode("light_purple", new Color(255, 85, 255)),
            new ColorCode("dark_purple", new Color(170, 0, 170)),
            new ColorCode("pink", new Color(230, 50, 120)),
            new ColorCode("soft_pink", new Color(230, 90, 150))
         )
      );
      ColorScheme custom = new ColorScheme(
         "custom",
         Lists.newList(
            new ColorCode("black", new Color(0, 0, 0)),
            new ColorCode("white", new Color(255, 255, 255)),
            new ColorCode("gray", new Color(161, 161, 161)),
            new ColorCode("soft_gray", new Color(180, 180, 180)),
            new ColorCode("dark_gray", new Color(70, 70, 70)),
            new ColorCode("red", new Color(230, 50, 50)),
            new ColorCode("soft_red", new Color(230, 75, 75)),
            new ColorCode("dark_red", new Color(150, 50, 50)),
            new ColorCode("green", new Color(50, 230, 50)),
            new ColorCode("soft_green", new Color(120, 230, 80)),
            new ColorCode("dark_green", new Color(50, 120, 50)),
            new ColorCode("blue", new Color(50, 120, 230)),
            new ColorCode("soft_blue", new Color(50, 170, 230)),
            new ColorCode("dark_blue", new Color(50, 50, 150)),
            new ColorCode("yellow", new Color(230, 230, 50)),
            new ColorCode("soft_yellow", new Color(250, 240, 160)),
            new ColorCode("dark_yellow", new Color(180, 180, 50)),
            new ColorCode("orange", new Color(230, 120, 50)),
            new ColorCode("soft_orange", new Color(230, 170, 50)),
            new ColorCode("gold", new Color(230, 170, 50)),
            new ColorCode("aqua", new Color(50, 230, 230)),
            new ColorCode("soft_aqua", new Color(150, 230, 230)),
            new ColorCode("dark_aqua", new Color(50, 120, 120)),
            new ColorCode("purple", new Color(120, 50, 230)),
            new ColorCode("soft_purple", new Color(150, 90, 230)),
            new ColorCode("light_purple", new Color(150, 90, 230)),
            new ColorCode("dark_purple", new Color(75, 50, 150)),
            new ColorCode("pink", new Color(230, 50, 120)),
            new ColorCode("soft_pink", new Color(230, 90, 150)),
            new ColorCode("cyan", ParserUtils.colorFromHexString("#31eace")),
            new ColorCode("dgray", ParserUtils.colorFromHexString("#6c6c62")),
            new ColorCode("dark_gray", ParserUtils.colorFromHexString("#6c6c62")),
            new ColorCode("lgray", ParserUtils.colorFromHexString("#d4d9d8")),
            new ColorCode("light_gray", ParserUtils.colorFromHexString("#d4d9d8")),
            new ColorCode("lgreen", ParserUtils.colorFromHexString("#91f251")),
            new ColorCode("light_green", ParserUtils.colorFromHexString("#91f251")),
            new ColorCode("lyellow", ParserUtils.colorFromHexString("#ffeea2")),
            new ColorCode("light_yellow", ParserUtils.colorFromHexString("#ffeea2")),
            new ColorCode("lorange", ParserUtils.colorFromHexString("#fdba5e")),
            new ColorCode("light_orange", ParserUtils.colorFromHexString("#fdba5e")),
            new ColorCode("lred", ParserUtils.colorFromHexString("#fd5e5e")),
            new ColorCode("light_red", ParserUtils.colorFromHexString("#fd5e5e")),
            new ColorCode("lblue", ParserUtils.colorFromHexString("#5e9dfd")),
            new ColorCode("light_blue", ParserUtils.colorFromHexString("#5e9dfd")),
            new ColorCode("lcyan", ParserUtils.colorFromHexString("#5edefd")),
            new ColorCode("light_cyan", ParserUtils.colorFromHexString("#5edefd")),
            new ColorCode("lpurple", ParserUtils.colorFromHexString("#e39fff")),
            new ColorCode("light_purple", ParserUtils.colorFromHexString("#e39fff")),
            new ColorCode("lpink", ParserUtils.colorFromHexString("#fd8ddb")),
            new ColorCode("light_pink", ParserUtils.colorFromHexString("#fd8ddb"))
         )
      );
      return Lists.newList(vanilla, custom);
   }

   @Override
   public void write(@NonNull FileConfig config, @NonNull String path) {
      config.remove(path + ".Colors");
      this.colors.forEach(code -> config.set(path + ".Colors." + code.name(), ParserUtils.colorToHexString(code.color())));
   }

   @NonNull
   public String getId() {
      return this.id;
   }

   @NonNull
   public List<ColorCode> getColors() {
      return this.colors;
   }
}
