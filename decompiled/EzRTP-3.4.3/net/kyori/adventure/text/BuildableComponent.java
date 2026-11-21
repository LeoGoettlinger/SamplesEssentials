package net.kyori.adventure.text;

import net.kyori.adventure.util.Buildable;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

@Deprecated(
   forRemoval = true
)
@ScheduledForRemoval(
   inVersion = "6.0.0"
)
public sealed interface BuildableComponent<C extends Component, B extends ComponentBuilder<C, ?>>
   extends Buildable<B>,
   Component
   permits KeybindComponent,
   NBTComponent,
   ObjectComponent,
   ScoreComponent,
   SelectorComponent,
   TextComponent,
   TranslatableComponent {
   @Override
   B toBuilder();
}
