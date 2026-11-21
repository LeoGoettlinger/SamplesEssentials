package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Toggle")
@Description({"Toggle the state of a block or boolean."})
@Example.Examples({@Example("# use arrows to toggle switches, doors, etc.\non projectile hit:\n\tprojectile is arrow\n\ttoggle the block at the arrow\n"), @Example("# With booleans\ntoggle gravity of player\n")})
@Since({"1.4, 2.12 (booleans)"})
public class EffToggle extends Effect {
   private static final Patterns<EffToggle.Action> patterns = new Patterns<>(
      new Object[][]{
         {"(open|turn on|activate) %blocks%", EffToggle.Action.ACTIVATE},
         {"(close|turn off|de[-]activate) %blocks%", EffToggle.Action.DEACTIVATE},
         {"(toggle|switch) [[the] state of] %blocks/booleans%", EffToggle.Action.TOGGLE}
      }
   );
   private Expression<?> togglables;
   private EffToggle.Action action;
   private EffToggle.Type type;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.togglables = expressions[0];
      this.action = patterns.getInfo(matchedPattern);
      this.type = EffToggle.Type.fromClass(this.togglables);
      if (this.type == EffToggle.Type.BOOLEANS && !Changer.ChangerUtils.acceptsChange(this.togglables, Changer.ChangeMode.SET, Boolean.class)) {
         Skript.error("Cannot toggle '" + this.togglables + "' as it cannot be set to booleans.");
         return false;
      } else if (this.type == EffToggle.Type.MIXED && !Changer.ChangerUtils.acceptsChange(this.togglables, Changer.ChangeMode.SET, Block.class, Boolean.class)) {
         Skript.error("Cannot toggle '" + this.togglables + "' as it cannot be set to both blocks and booleans.");
         return false;
      } else {
         return true;
      }
   }

   @Override
   protected void execute(Event event) {
      switch (this.type) {
         case BLOCKS:
            this.toggleBlocks(event);
            break;
         case BOOLEANS:
            this.toggleBooleans(event);
            break;
         case MIXED:
            this.toggleMixed(event);
      }
   }

   private void toggleBlocks(Event event) {
      for (Object obj : this.togglables.getArray(event)) {
         if (obj instanceof Block block) {
            this.toggleSingleBlock(block);
         }
      }
   }

   private void toggleSingleBlock(@NotNull Block block) {
      BlockData data = block.getBlockData();
      if (data instanceof Openable openable) {
         openable.setOpen(this.action.apply(openable.isOpen()));
      } else if (data instanceof Powerable powerable) {
         powerable.setPowered(this.action.apply(powerable.isPowered()));
      }

      block.setBlockData(data);
   }

   private void toggleBooleans(Event event) {
      this.togglables.changeInPlace(event, obj -> obj instanceof Boolean bool ? this.action.apply(bool) : null);
   }

   private void toggleMixed(Event event) {
      this.togglables.changeInPlace(event, obj -> {
         if (obj instanceof Block block) {
            this.toggleSingleBlock(block);
            return block;
         } else {
            return obj instanceof Boolean bool ? this.action.apply(bool) : obj;
         }
      });
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      String actionText = switch (this.action) {
         case ACTIVATE -> "activate";
         case DEACTIVATE -> "deactivate";
         case TOGGLE -> "toggle";
      };
      return actionText + " " + this.togglables.toString(event, debug);
   }

   static {
      Skript.registerEffect(EffToggle.class, patterns.getPatterns());
   }

   private static enum Action {
      ACTIVATE,
      DEACTIVATE,
      TOGGLE;

      public boolean apply(boolean current) {
         return switch (this) {
            case ACTIVATE -> true;
            case DEACTIVATE -> false;
            case TOGGLE -> !current;
         };
      }
   }

   private static enum Type {
      BLOCKS,
      BOOLEANS,
      MIXED;

      public static EffToggle.Type fromClass(Expression<?> expression) {
         boolean isBlockType = expression.canReturn(Block.class);
         boolean isBooleanType = expression.canReturn(Boolean.class);
         if (isBlockType && !isBooleanType) {
            return BLOCKS;
         } else {
            return isBooleanType && !isBlockType ? BOOLEANS : MIXED;
         }
      }
   }
}
