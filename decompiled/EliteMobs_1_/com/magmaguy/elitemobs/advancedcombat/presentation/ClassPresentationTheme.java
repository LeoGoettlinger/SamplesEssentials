package com.magmaguy.elitemobs.advancedcombat.presentation;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassResourceType;
import java.util.Objects;

public final class ClassPresentationTheme {
   public static final String ELITE = "#8B0000:#CC4400:#DAA520";
   public static final String GOLD = "#B8860B:#F0C040";
   public static final String ORANGE = "#A63D2F:#E97932";
   public static final String BLUE = "#355CA8:#5FA9E8";
   public static final String TEAL = "#267A78:#58B8A9";
   public static final String GREEN = "#2E7D4F:#69C56F";
   public static final String RED = "#7A1F2B:#C2414A";
   public static final String PURPLE = "#6D3AA8:#A855F7";

   private ClassPresentationTheme() {
   }

   public static String gradient(String colors, String text) {
      Objects.requireNonNull(colors, "colors");
      Objects.requireNonNull(text, "text");
      return "<g:" + colors + ">" + text + "</g>";
   }

   public static String resourceGradient(ClassResourceType resourceType, String text) {
      return gradient(colors(resourceType), text);
   }

   public static String abilityDamage(String text) {
      return gradient("#A63D2F:#E97932", text);
   }

   public static String colors(ClassResourceType resourceType) {
      return switch ((ClassResourceType)Objects.requireNonNull(resourceType, "resourceType")) {
         case STAMINA, RESOLVE -> "#B8860B:#F0C040";
         case FURY -> "#A63D2F:#E97932";
         case FOCUS -> "#2E7D4F:#69C56F";
         case GRACE -> "#355CA8:#5FA9E8";
         case MANA -> "#6D3AA8:#A855F7";
      };
   }
}
