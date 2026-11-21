package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.SoundUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Objects;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Block Sound")
@Description({"Gets the sound that a given block, blockdata, or itemtype will use in a specific scenario.", "This will return a string in the form of \"SOUND_EXAMPLE\", which can be used in the play sound syntax.", "", "Check out <a href=\"https://minecraft.wiki/w/Sounds.json\">this website</a> for a list of sounds in Minecraft, or <a href=\"https://minecraft.wiki/w/Sound\">this one</a> to go to the Sounds wiki page."})
@Example.Examples({@Example("play sound (break sound of dirt) at all players"), @Example("set {_sounds::*} to place sounds of dirt, grass block, blue wool and stone")})
@Since({"2.10"})
public class ExprBlockSound extends SimpleExpression<String> {
   private ExprBlockSound.SoundType soundType;
   private Expression<?> objects;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.soundType = ExprBlockSound.SoundType.values()[parseResult.mark - 1];
      this.objects = exprs[0];
      return true;
   }

   @Nullable
   protected String[] get(Event event) {
      return this.objects
         .stream(event)
         .map(this::convertAndGetSound)
         .filter(Objects::nonNull)
         .distinct()
         .map(sound -> SoundUtils.getKey(sound).getKey())
         .toArray(String[]::new);
   }

   @Nullable
   private SoundGroup getSoundGroup(Object object) {
      if (object instanceof Block block) {
         return block.getBlockData().getSoundGroup();
      } else if (object instanceof BlockData data) {
         return data.getSoundGroup();
      } else {
         return object instanceof ItemType item && item.hasBlock() ? item.getMaterial().createBlockData().getSoundGroup() : null;
      }
   }

   @Nullable
   private Sound convertAndGetSound(Object object) {
      SoundGroup group = this.getSoundGroup(object);
      return group == null ? null : this.soundType.getSound(group);
   }

   @Override
   public boolean isSingle() {
      return this.objects.isSingle();
   }

   @NotNull
   @Override
   public Class<? extends String> getReturnType() {
      return String.class;
   }

   @NotNull
   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.soundType.name().toLowerCase() + " sound of " + this.objects.toString(event, debug);
   }

   static {
      SimplePropertyExpression.register(ExprBlockSound.class, String.class, "(1:break|2:fall|3:hit|4:place|5:step) sound[s]", "blocks/blockdatas/itemtypes");
   }

   public static enum SoundType {
      BREAK {
         @Override
         public Sound getSound(SoundGroup group) {
            return group.getBreakSound();
         }
      },
      FALL {
         @Override
         public Sound getSound(SoundGroup group) {
            return group.getFallSound();
         }
      },
      HIT {
         @Override
         public Sound getSound(SoundGroup group) {
            return group.getHitSound();
         }
      },
      PLACE {
         @Override
         public Sound getSound(SoundGroup group) {
            return group.getPlaceSound();
         }
      },
      STEP {
         @Override
         public Sound getSound(SoundGroup group) {
            return group.getStepSound();
         }
      };

      @Nullable
      public abstract Sound getSound(SoundGroup var1);
   }
}
