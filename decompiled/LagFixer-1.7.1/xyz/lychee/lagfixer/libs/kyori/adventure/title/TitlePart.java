package xyz.lychee.lagfixer.libs.kyori.adventure.title;

import org.jetbrains.annotations.ApiStatus;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;

@ApiStatus.NonExtendable
public interface TitlePart<T> {
   TitlePart<Component> TITLE = new TitlePart<Component>() {
      @Override
      public String toString() {
         return "TitlePart.TITLE";
      }
   };
   TitlePart<Component> SUBTITLE = new TitlePart<Component>() {
      @Override
      public String toString() {
         return "TitlePart.SUBTITLE";
      }
   };
   TitlePart<Title.Times> TIMES = new TitlePart<Title.Times>() {
      @Override
      public String toString() {
         return "TitlePart.TIMES";
      }
   };
}
