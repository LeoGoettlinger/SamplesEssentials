package net.kyori.adventure.text.format;

import net.kyori.adventure.text.ComponentBuilderApplicable;

public sealed interface TextFormat extends ComponentBuilderApplicable permits TextDecoration, TextColor {
}
