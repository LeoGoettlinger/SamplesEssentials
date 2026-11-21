package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.standard;

import com.earth2me.essentials.libs.kyori.adventure.text.format.NamedTextColor;
import com.earth2me.essentials.libs.kyori.adventure.text.format.TextColor;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.Context;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.Tag;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import com.earth2me.essentials.libs.kyori.examination.ExaminableProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

class GradientTag extends AbstractColorChangingTag {
   private static final String GRADIENT = "gradient";
   private static final TextColor DEFAULT_WHITE = TextColor.color(16777215);
   private static final TextColor DEFAULT_BLACK = TextColor.color(0);
   static final TagResolver RESOLVER = SerializableResolver.claimingComponent("gradient", GradientTag::create, AbstractColorChangingTag::claimComponent);
   private int index = 0;
   private double multiplier = 1.0;
   private final TextColor[] colors;
   @Range(
      from = -1L,
      to = 1L
   )
   double phase;
   private final boolean negativePhase;

   static Tag create(final ArgumentQueue args, final Context ctx) {
      double phase = 0.0;
      List<TextColor> textColors;
      if (!args.hasNext()) {
         textColors = Collections.emptyList();
      } else {
         textColors = new ArrayList<>();

         while (true) {
            if (args.hasNext()) {
               Tag.Argument arg = args.pop();
               String argValue = arg.value();
               TextColor color = ColorTagResolver.resolveColorOrNull(argValue);
               if (color != null) {
                  textColors.add(color);
                  continue;
               }

               if (args.hasNext()) {
                  throw ctx.newException(String.format("Unable to parse a color from '%s'. Please use named colors or hex (#RRGGBB) colors.", argValue), args);
               }

               OptionalDouble possiblePhase = arg.asDouble();
               if (!possiblePhase.isPresent()) {
                  throw ctx.newException(String.format("Unable to parse a color from '%s'. Please use named colors or hex (#RRGGBB) colors.", argValue), args);
               }

               phase = possiblePhase.getAsDouble();
               if (phase < -1.0 || phase > 1.0) {
                  throw ctx.newException(String.format("Gradient phase is out of range (%s). Must be in the range [-1.0, 1.0] (inclusive).", phase), args);
               }
            }

            if (textColors.size() == 1) {
               throw ctx.newException("Invalid gradient, not enough colors. Gradients must have at least two colors.", args);
            }
            break;
         }
      }

      return new GradientTag(phase, textColors, ctx);
   }

   GradientTag(final double phase, final List<TextColor> colors, final Context ctx) {
      super(ctx);
      if (colors.isEmpty()) {
         this.colors = new TextColor[]{DEFAULT_WHITE, DEFAULT_BLACK};
      } else {
         this.colors = colors.toArray(new TextColor[0]);
      }

      if (phase < 0.0) {
         this.negativePhase = true;
         this.phase = 1.0 + phase;
         Collections.reverse(Arrays.asList(this.colors));
      } else {
         this.negativePhase = false;
         this.phase = phase;
      }
   }

   @Override
   protected void init() {
      this.multiplier = this.size() == 1 ? 0.0 : (double)(this.colors.length - 1) / (this.size() - 1);
      this.phase = this.phase * (this.colors.length - 1);
      this.index = 0;
   }

   @Override
   protected void advanceColor() {
      this.index++;
   }

   @Override
   protected TextColor color() {
      double position = this.index * this.multiplier + this.phase;
      int lowUnclamped = (int)Math.floor(position);
      int high = (int)Math.ceil(position) % this.colors.length;
      int low = lowUnclamped % this.colors.length;
      return TextColor.lerp((float)position - lowUnclamped, this.colors[low], this.colors[high]);
   }

   @NotNull
   @Override
   protected Consumer<TokenEmitter> preserveData() {
      TextColor[] colors;
      double phase;
      if (this.negativePhase) {
         colors = Arrays.copyOf(this.colors, this.colors.length);
         Collections.reverse(Arrays.asList(colors));
         phase = this.phase - 1.0;
      } else {
         colors = this.colors;
         phase = this.phase;
      }

      return emit -> {
         emit.tag("gradient");
         if (colors.length != 2 || !colors[0].equals(DEFAULT_WHITE) || !colors[1].equals(DEFAULT_BLACK)) {
            for (TextColor color : colors) {
               if (color instanceof NamedTextColor) {
                  emit.argument(NamedTextColor.NAMES.keyOrThrow((NamedTextColor)color));
               } else {
                  emit.argument(color.asHexString());
               }
            }
         }

         if (phase != 0.0) {
            emit.argument(Double.toString(phase));
         }
      };
   }

   @NotNull
   @Override
   public Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("phase", this.phase), ExaminableProperty.of("colors", this.colors));
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         GradientTag that = (GradientTag)other;
         return this.index == that.index && this.phase == that.phase && Arrays.equals((Object[])this.colors, (Object[])that.colors);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hash(this.index, this.phase);
      return 31 * result + Arrays.hashCode((Object[])this.colors);
   }
}
