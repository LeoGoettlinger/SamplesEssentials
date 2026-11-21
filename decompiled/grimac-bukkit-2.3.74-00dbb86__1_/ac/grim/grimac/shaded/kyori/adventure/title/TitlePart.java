package ac.grim.grimac.shaded.kyori.adventure.title;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;

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
