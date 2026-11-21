package ac.grim.grimac.utils.data.attribute;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attribute;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import ac.grim.grimac.utils.latency.CompensatedEntities;
import ac.grim.grimac.utils.math.GrimMath;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public final class ValuedAttribute {
   private static final DoubleUnaryOperator DEFAULT_GET_REWRITE = DoubleUnaryOperator.identity();
   private final Attribute attribute;
   private final double min;
   private final double max;
   private final double defaultValue;
   private WrapperPlayServerUpdateAttributes.Property lastProperty;
   private double value;
   private DoubleBinaryOperator setRewriter;
   private DoubleUnaryOperator getRewriter;

   private ValuedAttribute(Attribute attribute, double defaultValue, double min, double max) {
      if (!(defaultValue < min) && !(defaultValue > max)) {
         this.attribute = attribute;
         this.defaultValue = defaultValue;
         this.value = defaultValue;
         this.min = min;
         this.max = max;
         this.getRewriter = DEFAULT_GET_REWRITE;
      } else {
         throw new IllegalArgumentException("Default value must be between min and max!");
      }
   }

   public static ValuedAttribute ranged(Attribute attribute, double defaultValue, double min, double max) {
      return new ValuedAttribute(attribute, defaultValue, min, max);
   }

   public ValuedAttribute withSetRewriter(DoubleBinaryOperator rewriteFunction) {
      this.setRewriter = rewriteFunction;
      return this;
   }

   public ValuedAttribute requiredVersion(GrimPlayer player, ClientVersion requiredVersion) {
      this.withSetRewriter((oldValue, newValue) -> player.getClientVersion().isOlderThan(requiredVersion) ? oldValue : newValue);
      return this;
   }

   public ValuedAttribute withGetRewriter(DoubleUnaryOperator getRewriteFunction) {
      this.getRewriter = getRewriteFunction;
      return this;
   }

   public Attribute attribute() {
      return this.attribute;
   }

   public void reset() {
      this.value = this.defaultValue;
      this.lastProperty = null;
   }

   public double get() {
      return this.getRewriter.applyAsDouble(this.value);
   }

   public void override(double value) {
      this.value = value;
   }

   @Deprecated
   public Optional<WrapperPlayServerUpdateAttributes.Property> property() {
      return Optional.ofNullable(this.lastProperty);
   }

   public void recalculate() {
      this.with(this.lastProperty);
   }

   public double with(WrapperPlayServerUpdateAttributes.Property property) {
      double baseValue = property.getValue();
      double additionSum = 0.0;
      double multiplyBaseSum = 0.0;
      double multiplyTotalProduct = 1.0;
      List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers = property.getModifiers();
      modifiers.removeIf(
         modifierx -> modifierx.getUUID().equals(CompensatedEntities.SPRINTING_MODIFIER_UUID) || modifierx.getName().getKey().equals("sprinting")
      );

      for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : modifiers) {
         switch (modifier.getOperation()) {
            case ADDITION:
               additionSum += modifier.getAmount();
               break;
            case MULTIPLY_BASE:
               multiplyBaseSum += modifier.getAmount();
               break;
            case MULTIPLY_TOTAL:
               multiplyTotalProduct *= 1.0 + modifier.getAmount();
         }
      }

      double newValue = GrimMath.clamp((baseValue + additionSum) * (1.0 + multiplyBaseSum) * multiplyTotalProduct, this.min, this.max);
      if (this.setRewriter != null) {
         newValue = this.setRewriter.applyAsDouble(this.value, newValue);
      }

      if (!(newValue < this.min) && !(newValue > this.max)) {
         this.lastProperty = property;
         return this.value = newValue;
      } else {
         throw new IllegalArgumentException("New value must be between min and max!");
      }
   }
}
