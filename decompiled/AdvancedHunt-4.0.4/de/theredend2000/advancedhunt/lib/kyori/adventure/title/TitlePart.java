package de.theredend2000.advancedhunt.lib.kyori.adventure.title;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;

public sealed interface TitlePart<T> permits TitlePartImpl {
   TitlePart<Component> TITLE = new TitlePartImpl<>("TITLE");
   TitlePart<Component> SUBTITLE = new TitlePartImpl<>("SUBTITLE");
   TitlePart<Title.Times> TIMES = new TitlePartImpl<>("TIMES");
}
