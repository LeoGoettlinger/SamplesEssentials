package su.nightexpress.nightcore.util.text.tag.decorator;

import java.awt.Color;
import java.nio.CharBuffer;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.text.impl.NightTextComponent;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

@Deprecated
public class GradientColorDecorator implements ColorDecorator {
   private final Color[] colorStops;
   private Color[] gradient;
   private int colorIndex;

   public GradientColorDecorator(@NonNull Color color, @NonNull Color colorEnd) {
      this(new Color[]{color, colorEnd});
   }

   public GradientColorDecorator(Color[] colorStops) {
      this.colorStops = colorStops;
   }

   public Color[] getColorStops() {
      return this.colorStops;
   }

   public boolean isCreated() {
      return this.gradient != null && this.gradient.length > 0;
   }

   public boolean hasNextColor() {
      return this.isCreated() && this.colorIndex < this.gradient.length;
   }

   public Color[] createGradient(int length) {
      this.gradient = new Color[length];
      int segments = this.colorStops.length - 1;
      int colorsPerSegment = length / segments;
      int remainder = length % segments;
      int index = 0;

      for (int i = 0; i < segments; i++) {
         Color start = this.colorStops[i];
         Color end = this.colorStops[i + 1];
         int segmentLength = colorsPerSegment + (i < remainder ? 1 : 0);

         for (int j = 0; j < segmentLength; j++) {
            double t = (double)j / segmentLength;
            int r = (int)(start.getRed() + t * (end.getRed() - start.getRed()));
            int g = (int)(start.getGreen() + t * (end.getGreen() - start.getGreen()));
            int b = (int)(start.getBlue() + t * (end.getBlue() - start.getBlue()));
            this.gradient[index++] = new Color(r, g, b);
         }
      }

      return this.gradient;
   }

   public Color[] getGradient() {
      return this.gradient;
   }

   public Color nextColor() {
      return this.gradient[this.colorIndex++];
   }

   public Color getLatestColor() {
      return this.gradient[this.gradient.length - 1];
   }

   @NonNull
   @Override
   public NightComponent decorate(@NonNull NightComponent component) {
      if (!(component instanceof NightTextComponent textComponent)) {
         if (!this.isCreated()) {
            this.createGradient(1);
         }

         return this.hasNextColor() ? component.color(this.nextColor()) : component;
      } else {
         String content = textComponent.content();
         char[] letters = content.toCharArray();
         if (!this.isCreated()) {
            int length = Math.toIntExact(CharBuffer.wrap(content.toCharArray()).chars().filter(c -> !Character.isWhitespace(c)).count());
            this.createGradient(length);
         }

         NightComponent fresh = textComponent.content("");

         for (char letter : letters) {
            Color color = this.hasNextColor() && !Character.isWhitespace(letter) ? this.nextColor() : fresh.color();
            fresh = fresh.append(textComponent.content(String.valueOf(letter)).color(color));
         }

         return fresh;
      }
   }
}
