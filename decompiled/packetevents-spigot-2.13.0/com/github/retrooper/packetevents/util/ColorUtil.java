package com.github.retrooper.packetevents.util;

import net.kyori.adventure.text.format.NamedTextColor;

public class ColorUtil {
   public static String toString(NamedTextColor color) {
      String prefix = "§";
      if (color == null) {
         return prefix + "f";
      } else {
         String var2 = color.toString();
         switch (var2) {
            case "black":
               return prefix + "0";
            case "dark_blue":
               return prefix + "1";
            case "dark_green":
               return prefix + "2";
            case "dark_aqua":
               return prefix + "3";
            case "dark_red":
               return prefix + "4";
            case "dark_purple":
               return prefix + "5";
            case "gold":
               return prefix + "6";
            case "gray":
               return prefix + "7";
            case "dark_gray":
               return prefix + "8";
            case "blue":
               return prefix + "9";
            case "green":
               return prefix + "a";
            case "aqua":
               return prefix + "b";
            case "red":
               return prefix + "c";
            case "light_purple":
               return prefix + "d";
            case "yellow":
               return prefix + "e";
            case "white":
               return prefix + "f";
            default:
               return prefix + "f";
         }
      }
   }

   public static int getId(NamedTextColor color) {
      if (color == null) {
         return -1;
      } else {
         String var1 = color.toString();
         switch (var1) {
            case "black":
               return 0;
            case "dark_blue":
               return 1;
            case "dark_green":
               return 2;
            case "dark_aqua":
               return 3;
            case "dark_red":
               return 4;
            case "dark_purple":
               return 5;
            case "gold":
               return 6;
            case "gray":
               return 7;
            case "dark_gray":
               return 8;
            case "blue":
               return 9;
            case "green":
               return 10;
            case "aqua":
               return 11;
            case "red":
               return 12;
            case "light_purple":
               return 13;
            case "yellow":
               return 14;
            case "white":
               return 15;
            default:
               return 15;
         }
      }
   }

   public static NamedTextColor fromId(int id) {
      if (id < 0) {
         return null;
      } else {
         switch (id) {
            case 0:
               return NamedTextColor.BLACK;
            case 1:
               return NamedTextColor.DARK_BLUE;
            case 2:
               return NamedTextColor.DARK_GREEN;
            case 3:
               return NamedTextColor.DARK_AQUA;
            case 4:
               return NamedTextColor.DARK_RED;
            case 5:
               return NamedTextColor.DARK_PURPLE;
            case 6:
               return NamedTextColor.GOLD;
            case 7:
               return NamedTextColor.GRAY;
            case 8:
               return NamedTextColor.DARK_GRAY;
            case 9:
               return NamedTextColor.BLUE;
            case 10:
               return NamedTextColor.GREEN;
            case 11:
               return NamedTextColor.AQUA;
            case 12:
               return NamedTextColor.RED;
            case 13:
               return NamedTextColor.LIGHT_PURPLE;
            case 14:
               return NamedTextColor.YELLOW;
            case 15:
               return NamedTextColor.WHITE;
            default:
               return NamedTextColor.WHITE;
         }
      }
   }
}
