package ac.grim.grimac.shaded.kyori.adventure.text;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ComponentApplicable {
   @NotNull
   Component componentApply(@NotNull final Component component);
}
