package ch.njol.skript.lang;

import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffectSectionEffect extends Effect {
   private final EffectSection effectSection;

   public EffectSectionEffect(EffectSection effectSection) {
      this.effectSection = effectSection;
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return this.effectSection.init(expressions, matchedPattern, isDelayed, parseResult);
   }

   @Override
   protected void execute(Event event) {
   }

   @Nullable
   @Override
   protected TriggerItem walk(Event event) {
      return this.effectSection.walk(event);
   }

   @Override
   public String getIndentation() {
      return this.effectSection.getIndentation();
   }

   @Override
   public TriggerItem setParent(@Nullable TriggerSection parent) {
      return this.effectSection.setParent(parent);
   }

   @Override
   public TriggerItem setNext(@Nullable TriggerItem next) {
      return this.effectSection.setNext(next);
   }

   @Nullable
   @Override
   public TriggerItem getNext() {
      return this.effectSection.getNext();
   }

   @Nullable
   @Override
   public TriggerItem getActualNext() {
      return this.effectSection.getActualNext();
   }

   @Nullable
   @Override
   public ExecutionIntent executionIntent() {
      return this.effectSection.executionIntent();
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.effectSection.toString(event, debug);
   }
}
