package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Utils;
import ch.njol.util.Math2;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.DefaultFunction;

public class DefaultFunctions {
   private static final DecimalFormat DEFAULT_INTEGER_FORMAT = new DecimalFormat("###,###");
   private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("###,###.##");

   private static String str(double n) {
      return StringUtils.toString(n, 4);
   }

   static {
      SkriptAddon skript = Skript.instance();
      Parameter<?>[] numberParam = new Parameter[]{new Parameter<>("n", DefaultClasses.NUMBER, true, null)};
      Parameter<?>[] numbersParam = new Parameter[]{new Parameter<>("ns", DefaultClasses.NUMBER, false, null)};
      Functions.register(
         DefaultFunction.builder(skript, "floor", Long.class)
            .description("Rounds a number down, i.e. returns the closest integer smaller than or equal to the argument.")
            .examples("floor(2.34) = 2", "floor(2) = 2", "floor(2.99) = 2")
            .since("2.2")
            .parameter("n", Number.class)
            .build(args -> {
               Number value = args.get("n");
               return value instanceof Long l ? l : Math2.floor(value.doubleValue());
            })
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>(
               "round",
               new Parameter[]{
                  new Parameter<>("n", DefaultClasses.NUMBER, true, null), new Parameter<>("d", DefaultClasses.NUMBER, true, new SimpleLiteral<>(0, false))
               },
               DefaultClasses.NUMBER,
               true
            ) {
               public Number[] executeSimple(Object[][] params) {
                  if (params[0][0] instanceof Long longValue) {
                     return new Long[]{longValue};
                  } else {
                     double value = ((Number)params[0][0]).doubleValue();
                     if (!Double.isFinite(value)) {
                        return new Double[]{value};
                     } else {
                        double placementDouble = ((Number)params[1][0]).doubleValue();
                        if (Double.isFinite(placementDouble) && !(placementDouble >= 2.147483647E9) && !(placementDouble <= -2.1474836E9F)) {
                           int placement = (int)placementDouble;
                           if (placement == 0) {
                              return new Long[]{Math2.round(value)};
                           } else if (placement >= 0) {
                              BigDecimal decimal = new BigDecimal(Double.toString(value));
                              decimal = decimal.setScale(placement, RoundingMode.HALF_UP);
                              return new Double[]{decimal.doubleValue()};
                           } else {
                              long rounded = Math2.round(value);
                              return new Double[]{(int)Math2.round(rounded * Math.pow(10.0, placement)) / Math.pow(10.0, placement)};
                           }
                        } else {
                           return new Double[]{Double.NaN};
                        }
                     }
                  }
               }
            })
            .description(
               new String[]{"Rounds a number, i.e. returns the closest integer to the argument. Place a second argument to define the decimal placement."}
            )
            .examples("round(2.34) = 2", "round(2) = 2", "round(2.99) = 3", "round(2.5) = 3")
            .since("2.2, 2.7 (decimal placement)")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Long>("ceil", numberParam, DefaultClasses.LONG, true) {
               public Long[] executeSimple(Object[][] params) {
                  return params[0][0] instanceof Long ? new Long[]{(Long)params[0][0]} : new Long[]{Math2.ceil(((Number)params[0][0]).doubleValue())};
               }
            })
            .description(new String[]{"Rounds a number up, i.e. returns the closest integer larger than or equal to the argument."})
            .examples("ceil(2.34) = 3", "ceil(2) = 2", "ceil(2.99) = 3")
            .since("2.2")
      );
      Functions.registerFunction((new SimpleJavaFunction<Long>("ceiling", numberParam, DefaultClasses.LONG, true) {
         public Long[] executeSimple(Object[][] params) {
            return params[0][0] instanceof Long ? new Long[]{(Long)params[0][0]} : new Long[]{Math2.ceil(((Number)params[0][0]).doubleValue())};
         }
      }).description(new String[]{"Alias of <a href='#ceil'>ceil</a>."}).examples("ceiling(2.34) = 3", "ceiling(2) = 2", "ceiling(2.99) = 3").since("2.2"));
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("abs", numberParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  Number n = (Number)params[0][0];
                  return (Number[])(!(n instanceof Byte) && !(n instanceof Short) && !(n instanceof Integer) && !(n instanceof Long)
                     ? new Double[]{Math.abs(n.doubleValue())}
                     : new Long[]{Math.abs(n.longValue())});
               }
            })
            .description(new String[]{"Returns the absolute value of the argument, i.e. makes the argument positive."})
            .examples("abs(3) = 3", "abs(-2) = 2")
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>(
               "mod",
               new Parameter[]{new Parameter<>("d", DefaultClasses.NUMBER, true, null), new Parameter<>("m", DefaultClasses.NUMBER, true, null)},
               DefaultClasses.NUMBER,
               true
            ) {
               public Number[] executeSimple(Object[][] params) {
                  Number d = (Number)params[0][0];
                  Number m = (Number)params[1][0];
                  double mm = m.doubleValue();
                  return mm == 0.0 ? new Double[]{Double.NaN} : new Double[]{Math2.mod(d.doubleValue(), mm)};
               }
            })
            .description(
               new String[]{
                  "Returns the modulo of the given arguments, i.e. the remainder of the division <code>d/m</code>, where d and m are the arguments of this function.",
                  "The returned value is always positive. Returns NaN (not a number) if the second argument is zero."
               }
            )
            .examples("mod(3, 2) = 1", "mod(256436, 100) = 36", "mod(-1, 10) = 9")
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("exp", numberParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  return new Double[]{Math.exp(((Number)params[0][0]).doubleValue())};
               }
            })
            .description(new String[]{"The exponential function. You probably don't need this if you don't know what this is."})
            .examples("exp(0) = 1", "exp(1) = " + str(Math.exp(1.0)))
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("ln", numberParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  return new Double[]{Math.log(((Number)params[0][0]).doubleValue())};
               }
            })
            .description(
               new String[]{
                  "The natural logarithm. You probably don't need this if you don't know what this is.",
                  "Returns NaN (not a number) if the argument is negative."
               }
            )
            .examples("ln(1) = 0", "ln(exp(5)) = 5", "ln(2) = " + StringUtils.toString(Math.log(2.0), 4))
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>(
               "log",
               new Parameter[]{
                  new Parameter<>("n", DefaultClasses.NUMBER, true, null), new Parameter<>("base", DefaultClasses.NUMBER, true, new SimpleLiteral<>(10, false))
               },
               DefaultClasses.NUMBER,
               true
            ) {
               public Number[] executeSimple(Object[][] params) {
                  return new Double[]{Math.log10(((Number)params[0][0]).doubleValue()) / Math.log10(((Number)params[1][0]).doubleValue())};
               }
            })
            .description(
               new String[]{
                  "A logarithm, with base 10 if none is specified. This is the inverse operation to exponentiation (for positive bases only), i.e. <code>log(base ^ exponent, base) = exponent</code> for any positive number 'base' and any number 'exponent'.",
                  "Another useful equation is <code>base ^ log(a, base) = a</code> for any numbers 'base' and 'a'.",
                  "Please note that due to how numbers are represented in computers, these equations do not hold for all numbers, as the computed values may slightly differ from the correct value.",
                  "Returns NaN (not a number) if any of the arguments are negative."
               }
            )
            .examples("log(100) = 2 # 10^2 = 100", "log(16, 2) = 4 # 2^4 = 16")
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("sqrt", numberParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  return new Double[]{Math.sqrt(((Number)params[0][0]).doubleValue())};
               }
            })
            .description(
               new String[]{
                  "The square root, which is the inverse operation to squaring a number (for positive numbers only). This is the same as <code>(argument) ^ (1/2)</code> – other roots can be calculated via <code>number ^ (1/root)</code>, e.g. <code>set {_l} to {_volume}^(1/3)</code>.",
                  "Returns NaN (not a number) if the argument is negative."
               }
            )
            .examples("sqrt(4) = 2", "sqrt(2) = " + str(Math.sqrt(2.0)), "sqrt(-1) = " + str(Math.sqrt(-1.0)))
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("sin", numberParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  return new Double[]{Math.sin(Math.toRadians(((Number)params[0][0]).doubleValue()))};
               }
            })
            .description(
               new String[]{
                  "The sine function. It starts at 0° with a value of 0, goes to 1 at 90°, back to 0 at 180°, to -1 at 270° and then repeats every 360°. Uses degrees, not radians."
               }
            )
            .examples("sin(90) = 1", "sin(60) = " + str(Math.sin(Math.toRadians(60.0))))
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("cos", numberParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  return new Double[]{Math.cos(Math.toRadians(((Number)params[0][0]).doubleValue()))};
               }
            })
            .description(
               new String[]{
                  "The cosine function. This is basically the <a href='#sin'>sine</a> shifted by 90°, i.e. <code>cos(a) = sin(a + 90°)</code>, for any number a. Uses degrees, not radians."
               }
            )
            .examples("cos(0) = 1", "cos(90) = 0")
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("tan", numberParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  return new Double[]{Math.tan(Math.toRadians(((Number)params[0][0]).doubleValue()))};
               }
            })
            .description(
               new String[]{
                  "The tangent function. This is basically <code><a href='#sin'>sin</a>(arg)/<a href='#cos'>cos</a>(arg)</code>. Uses degrees, not radians."
               }
            )
            .examples("tan(0) = 0", "tan(45) = 1", "tan(89.99) = " + str(Math.tan(Math.toRadians(89.99))))
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("asin", numberParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  return new Double[]{Math.toDegrees(Math.asin(((Number)params[0][0]).doubleValue()))};
               }
            })
            .description(
               new String[]{
                  "The inverse of the <a href='#sin'>sine</a>, also called arcsin. Returns result in degrees, not radians. Only returns values from -90 to 90."
               }
            )
            .examples("asin(0) = 0", "asin(1) = 90", "asin(0.5) = " + str(Math.toDegrees(Math.asin(0.5))))
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("acos", numberParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  return new Double[]{Math.toDegrees(Math.acos(((Number)params[0][0]).doubleValue()))};
               }
            })
            .description(
               new String[]{
                  "The inverse of the <a href='#cos'>cosine</a>, also called arccos. Returns result in degrees, not radians. Only returns values from 0 to 180."
               }
            )
            .examples("acos(0) = 90", "acos(1) = 0", "acos(0.5) = " + str(Math.toDegrees(Math.asin(0.5))))
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("atan", numberParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  return new Double[]{Math.toDegrees(Math.atan(((Number)params[0][0]).doubleValue()))};
               }
            })
            .description(
               new String[]{
                  "The inverse of the <a href='#tan'>tangent</a>, also called arctan. Returns result in degrees, not radians. Only returns values from -90 to 90."
               }
            )
            .examples("atan(0) = 0", "atan(1) = 45", "atan(10000) = " + str(Math.toDegrees(Math.atan(10000.0))))
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>(
               "atan2",
               new Parameter[]{new Parameter<>("x", DefaultClasses.NUMBER, true, null), new Parameter<>("y", DefaultClasses.NUMBER, true, null)},
               DefaultClasses.NUMBER,
               true
            ) {
               public Number[] executeSimple(Object[][] params) {
                  return new Double[]{Math.toDegrees(Math.atan2(((Number)params[1][0]).doubleValue(), ((Number)params[0][0]).doubleValue()))};
               }
            })
            .description(
               new String[]{
                  "Similar to <a href='#atan'>atan</a>, but requires two coordinates and returns values from -180 to 180.",
                  "The returned angle is measured counterclockwise in a standard mathematical coordinate system (x to the right, y to the top)."
               }
            )
            .examples("atan2(0, 1) = 0", "atan2(10, 0) = 90", "atan2(-10, 5) = " + str(Math.toDegrees(Math.atan2(-10.0, 5.0))))
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("sum", numbersParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  Object[] ns = params[0];
                  double sum = ((Number)ns[0]).doubleValue();

                  for (int i = 1; i < ns.length; i++) {
                     sum += ((Number)ns[i]).doubleValue();
                  }

                  return new Double[]{sum};
               }
            })
            .description(new String[]{"Sums a list of numbers."})
            .examples("sum(1) = 1", "sum(2, 3, 4) = 9", "sum({some list variable::*})", "sum(2, {_v::*}, and the player's y-coordinate)")
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("product", numbersParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  Object[] ns = params[0];
                  double product = ((Number)ns[0]).doubleValue();

                  for (int i = 1; i < ns.length; i++) {
                     product *= ((Number)ns[i]).doubleValue();
                  }

                  return new Double[]{product};
               }
            })
            .description(new String[]{"Calculates the product of a list of numbers."})
            .examples("product(1) = 1", "product(2, 3, 4) = 24", "product({some list variable::*})", "product(2, {_v::*}, and the player's y-coordinate)")
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("max", numbersParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  Object[] ns = params[0];
                  double max = ((Number)ns[0]).doubleValue();

                  for (int i = 1; i < ns.length; i++) {
                     double d = ((Number)ns[i]).doubleValue();
                     if (d > max || Double.isNaN(max)) {
                        max = d;
                     }
                  }

                  return new Double[]{max};
               }
            })
            .description(new String[]{"Returns the maximum number from a list of numbers."})
            .examples("max(1) = 1", "max(1, 2, 3, 4) = 4", "max({some list variable::*})")
            .since("2.2")
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Number>("min", numbersParam, DefaultClasses.NUMBER, true) {
               public Number[] executeSimple(Object[][] params) {
                  Object[] ns = params[0];
                  double min = ((Number)ns[0]).doubleValue();

                  for (int i = 1; i < ns.length; i++) {
                     double d = ((Number)ns[i]).doubleValue();
                     if (d < min || Double.isNaN(min)) {
                        min = d;
                     }
                  }

                  return new Double[]{min};
               }
            })
            .description(new String[]{"Returns the minimum number from a list of numbers."})
            .examples("min(1) = 1", "min(1, 2, 3, 4) = 1", "min({some list variable::*})")
            .since("2.2")
      );
      Functions.registerFunction(
            new SimpleJavaFunction<Number>(
               "clamp",
               new Parameter[]{
                  new Parameter<>("values", DefaultClasses.NUMBER, false, null, true),
                  new Parameter<>("min", DefaultClasses.NUMBER, true, null),
                  new Parameter<>("max", DefaultClasses.NUMBER, true, null)
               },
               DefaultClasses.NUMBER,
               false,
               new Contract() {
                  @Override
                  public boolean isSingle(Expression<?>... arguments) {
                     return arguments[0].isSingle();
                  }

                  @Override
                  public Class<?> getReturnType(Expression<?>... arguments) {
                     return Number.class;
                  }
               }
            ) {
               @Nullable
               public Number[] executeSimple(Object[][] params) {
                  KeyedValue<Number>[] values = (KeyedValue<Number>[])params[0];
                  Double[] clampedValues = new Double[values.length];
                  double min = ((Number)params[1][0]).doubleValue();
                  double max = ((Number)params[2][0]).doubleValue();
                  double trueMin = Math.min(min, max);
                  double trueMax = Math.max(min, max);

                  for (int i = 0; i < values.length; i++) {
                     double value = values[i].value().doubleValue();
                     clampedValues[i] = Math.max(Math.min(value, trueMax), trueMin);
                  }

                  this.setReturnedKeys(KeyedValue.unzip(values).keys().toArray(new String[0]));
                  return clampedValues;
               }
            }
         )
         .description("Clamps one or more values between two numbers.", "This function retains indices")
         .examples(
            "clamp(5, 0, 10) = 5",
            "clamp(5.5, 0, 5) = 5",
            "clamp(0.25, 0, 0.5) = 0.25",
            "clamp(5, 7, 10) = 7",
            "clamp((5, 0, 10, 9, 13), 7, 10) = (7, 7, 10, 9, 10)",
            "set {_clamped::*} to clamp({_values::*}, 0, 10)"
         )
         .since("2.8.0");
      Functions.register(
         DefaultFunction.builder(skript, "toBase", String[].class)
            .description(
               "Turns a number in a string using a specific base (decimal, hexadecimal, octal).\nFor example, converting 32 to hexadecimal (base 16) would be 'toBase(32, 16)', which would return \"20\".\nYou can use any base between 2 and 36.\n"
            )
            .examples("send \"Decode this binary number for a prize! %toBase({_guess}, 2)%\"")
            .since("2.14")
            .parameter("n", Long[].class)
            .parameter("base", Long.class, org.skriptlang.skript.common.function.Parameter.Modifier.ranged(2, 36))
            .contract(new Contract() {
               @Override
               public boolean isSingle(Expression<?>... arguments) {
                  return arguments[0].isSingle();
               }

               @Override
               public Class<?> getReturnType(Expression<?>... arguments) {
                  return String.class;
               }
            })
            .build(args -> {
               Long[] n = args.get("n");
               Long base = args.get("base");
               String[] results = new String[n.length];

               for (int i = 0; i < n.length; i++) {
                  results[i] = Long.toString(n[i], base.intValue());
               }

               return results;
            })
      );
      Functions.register(
         DefaultFunction.builder(skript, "fromBase", Long[].class)
            .description(
               "Turns a text version of a number in a specific base (decimal, hexadecimal, octal) into an actual number.\nFor example, converting \"20\" in hexadecimal (base 16) would be 'fromBase(\"20\", 16)', which would return 32.\nYou can use any base between 2 and 36.\n"
            )
            .examples(
               "# /binaryText 01110011 01101011 01110010 01101001 01110000 01110100 00100001\n# sends \"skript!\"\ncommand binaryText <text>:\n\ttrigger:\n\tset {_characters::*} to argument split at \" \" without trailing empty string\n\t\ttransform {_characters::*} with fromBase(input, 2) # convert to codepoints\n\t\ttransform {_characters::*} with character from codepoint input # convert to characters\n\t\tsend join {_characters::*}\n"
            )
            .since("2.14")
            .parameter("string value", String[].class)
            .parameter("base", Long.class, org.skriptlang.skript.common.function.Parameter.Modifier.ranged(2, 36))
            .contract(new Contract() {
               @Override
               public boolean isSingle(Expression<?>... arguments) {
                  return arguments[0].isSingle();
               }

               @Override
               public Class<?> getReturnType(Expression<?>... arguments) {
                  return Long.class;
               }
            })
            .build(args -> {
               String[] n = args.get("string value");
               Long base = args.get("base");
               Long[] results = new Long[n.length];

               try {
                  for (int i = 0; i < n.length; i++) {
                     results[i] = Long.parseLong(n[i], base.intValue());
                  }

                  return results;
               } catch (NumberFormatException var5) {
                  return null;
               }
            })
      );
      Functions.registerFunction(
            new SimpleJavaFunction<World>("world", new Parameter[]{new Parameter<>("name", DefaultClasses.STRING, true, null)}, DefaultClasses.WORLD, true) {
               public World[] executeSimple(Object[][] params) {
                  World w = Bukkit.getWorld((String)params[0][0]);
                  return w == null ? new World[0] : new World[]{w};
               }
            }
         )
         .description("Gets a world from its name.")
         .examples("set {_nether} to world(\"%{_world}%_nether\")")
         .since("2.2");
      Functions.register(
         DefaultFunction.builder(skript, "location", Location.class)
            .description(
               "Creates a location from a world and 3 coordinates, with an optional yaw and pitch.",
               "If for whatever reason the world is not found, it will fallback to the server's main world."
            )
            .examples(
               "# TELEPORTING\nteleport player to location(1,1,1, world \"world\")\nteleport player to location(1,1,1, world \"world\", 100, 0)\nteleport player to location(1,1,1, world \"world\", yaw of player, pitch of player)\nteleport player to location(1,1,1, world of player)\nteleport player to location(1,1,1, world(\"world\"))\nteleport player to location({_x}, {_y}, {_z}, {_w}, {_yaw}, {_pitch})\n\n# SETTING BLOCKS\nset block at location(1,1,1, world \"world\") to stone\nset block at location(1,1,1, world \"world\", 100, 0) to stone\nset block at location(1,1,1, world of player) to stone\nset block at location(1,1,1, world(\"world\")) to stone\nset block at location({_x}, {_y}, {_z}, {_w}) to stone\n\n# USING VARIABLES\nset {_l1} to location(1,1,1)\nset {_l2} to location(10,10,10)\nset blocks within {_l1} and {_l2} to stone\nif player is within {_l1} and {_l2}:\n\n# OTHER\nkill all entities in radius 50 around location(1,65,1, world \"world\")\ndelete all entities in radius 25 around location(50,50,50, world \"world_nether\")\nignite all entities in radius 25 around location(1,1,1, world of player)\n"
            )
            .since("2.2")
            .parameter("x", Number.class)
            .parameter("y", Number.class)
            .parameter("z", Number.class)
            .parameter("world", World.class, org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL)
            .parameter("yaw", Float.class, org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL)
            .parameter("pitch", Float.class, org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL)
            .build(
               args -> {
                  World world = args.getOrDefault("world", (World)Bukkit.getWorlds().get(0));
                  return new Location(
                     world,
                     args.<Number>get("x").doubleValue(),
                     args.<Number>get("y").doubleValue(),
                     args.<Number>get("z").doubleValue(),
                     args.getOrDefault("yaw", 0.0F),
                     args.getOrDefault("pitch", 0.0F)
                  );
               }
            )
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Date>(
               "date",
               new Parameter[]{
                  new Parameter<>("year", DefaultClasses.NUMBER, true, null),
                  new Parameter<>("month", DefaultClasses.NUMBER, true, null),
                  new Parameter<>("day", DefaultClasses.NUMBER, true, null),
                  new Parameter<>("hour", DefaultClasses.NUMBER, true, new SimpleLiteral<>(0, true)),
                  new Parameter<>("minute", DefaultClasses.NUMBER, true, new SimpleLiteral<>(0, true)),
                  new Parameter<>("second", DefaultClasses.NUMBER, true, new SimpleLiteral<>(0, true)),
                  new Parameter<>("millisecond", DefaultClasses.NUMBER, true, new SimpleLiteral<>(0, true)),
                  new Parameter<>("zone_offset", DefaultClasses.NUMBER, true, new SimpleLiteral<>(Double.NaN, true)),
                  new Parameter<>("dst_offset", DefaultClasses.NUMBER, true, new SimpleLiteral<>(Double.NaN, true))
               },
               DefaultClasses.DATE,
               true
            ) {
               private final int[] fields = new int[]{1, 2, 5, 11, 12, 13, 14, 15, 16};
               private final int[] offsets = new int[]{0, -1, 0, 0, 0, 0, 0, 0, 0};
               private final double[] scale = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 60000.0, 60000.0};
               private final double[] relations = new double[]{
                  0.08333333333333333, 0.03333333333333333, 0.041666666666666664, 0.016666666666666666, 0.016666666666666666, 0.001, 0.0, 0.0, 0.0
               };

               {
                  int length = this.getSignature().getMaxParameters();

                  assert this.fields.length == length && this.offsets.length == length && this.scale.length == length && this.relations.length == length;
               }

               public Date[] executeSimple(Object[][] params) {
                  Calendar c = Calendar.getInstance();
                  c.setLenient(true);
                  double carry = 0.0;

                  for (int i = 0; i < this.fields.length; i++) {
                     int field = this.fields[i];
                     Number n = (Number)params[i][0];
                     double value = n.doubleValue() * this.scale[i] + this.offsets[i] + carry;
                     int v = (int)Math2.floor(value);
                     carry = (value - v) * this.relations[i];
                     c.set(field, v);
                  }

                  return new Date[]{new Date(c.getTimeInMillis(), c.getTimeZone())};
               }
            })
            .description(
               new String[]{
                  "Creates a date from a year, month, and day, and optionally also from hour, minute, second and millisecond.",
                  "A time zone and DST offset can be specified as well (in minutes), if they are left out the server's time zone and DST offset are used (the created date will not retain this information)."
               }
            )
            .examples(
               "date(2014, 10, 1) # 0:00, 1st October 2014",
               "date(1990, 3, 5, 14, 30) # 14:30, 5th May 1990",
               "date(1999, 12, 31, 23, 59, 59, 999, -3*60, 0) # almost year 2000 in parts of Brazil (-3 hours offset, no DST)"
            )
            .since("2.2")
      );
      Functions.register(
         DefaultFunction.builder(skript, "vector", Vector.class)
            .description("Creates a vector from a single argument. Equivalent to vector(n, n, n).")
            .examples("vector(1) # = vector(1, 1, 1)")
            .since("2.15")
            .parameter("n", Number.class)
            .build(args -> {
               double value = args.<Number>get("n").doubleValue();
               return new Vector(value, value, value);
            })
      );
      Functions.register(
         DefaultFunction.builder(skript, "vector", Vector.class)
            .description("Creates a new vector, which can be used with various expressions, effects and functions.")
            .examples("vector(0, 0, 0)")
            .since("2.2-dev23")
            .parameter("x", Number.class)
            .parameter("y", Number.class)
            .parameter("z", Number.class)
            .build(args -> new Vector(args.<Number>get("x").doubleValue(), args.<Number>get("y").doubleValue(), args.<Number>get("z").doubleValue()))
      );
      Functions.registerFunction(
         (new SimpleJavaFunction<Long>("calcExperience", new Parameter[]{new Parameter<>("level", DefaultClasses.LONG, true, null)}, DefaultClasses.LONG, true) {
               public Long[] executeSimple(Object[][] params) {
                  long level = (Long)params[0][0];
                  long exp;
                  if (level <= 0L) {
                     exp = 0L;
                  } else if (level >= 1L && level <= 15L) {
                     exp = level * level + 6L * level;
                  } else if (level >= 16L && level <= 30L) {
                     exp = (int)(2.5 * level * level - 40.5 * level + 360.0);
                  } else {
                     exp = (int)(4.5 * level * level - 162.5 * level + 2220.0);
                  }

                  return new Long[]{exp};
               }
            })
            .description(new String[]{"Calculates the total amount of experience needed to achieve given level from scratch in Minecraft."})
            .since("2.2-dev32")
      );
      Functions.register(
         DefaultFunction.builder(skript, "rgb", Color.class)
            .description(
               "Returns a RGB color from the given red, green and blue parameters.\nAlpha values can be added optionally but these only take affect in certain situations, like text display backgrounds."
            )
            .examples("dye player's leggings rgb(120, 30, 45)", "set the colour of a text display to rgb(10, 50, 100, 50)")
            .since("2.5, 2.10 (alpha)")
            .parameter("red", Long.class, org.skriptlang.skript.common.function.Parameter.Modifier.ranged(0, 255))
            .parameter("green", Long.class, org.skriptlang.skript.common.function.Parameter.Modifier.ranged(0, 255))
            .parameter("blue", Long.class, org.skriptlang.skript.common.function.Parameter.Modifier.ranged(0, 255))
            .parameter(
               "alpha",
               Long.class,
               org.skriptlang.skript.common.function.Parameter.Modifier.ranged(0, 255),
               org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL
            )
            .build(
               args -> ColorRGB.fromRGBA(
                  args.<Long>get("red").intValue(),
                  args.<Long>get("green").intValue(),
                  args.<Long>get("blue").intValue(),
                  args.getOrDefault("alpha", 255L).intValue()
               )
            )
      );
      Functions.register(
         DefaultFunction.builder(skript, "player", Player.class)
            .description(
               "Returns an online player from their name or UUID, if player is offline function will return nothing.",
               "Setting 'getExactPlayer' parameter to true will return the player whose name is exactly equal to the provided name instead of returning a player that their name starts with the provided name."
            )
            .examples(
               "set {_p} to player(\"Notch\") # will return an online player whose name is or starts with 'Notch'",
               "set {_p} to player(\"Notch\", true) # will return the only online player whose name is 'Notch'",
               "set {_p} to player(\"069a79f4-44e9-4726-a5be-fca90e38aaf5\") # <none> if player is offline"
            )
            .since("2.8.0")
            .parameter("nameOrUUID", String.class)
            .parameter("getExactPlayer", Boolean.class, org.skriptlang.skript.common.function.Parameter.Modifier.OPTIONAL)
            .build(args -> {
               String name = args.get("nameOrUUID");
               boolean isExact = args.getOrDefault("getExactPlayer", false);
               UUID uuid = null;
               if ((name.length() > 16 || name.contains("-")) && Utils.isValidUUID(name)) {
                  uuid = UUID.fromString(name);
               }

               return uuid != null ? Bukkit.getPlayer(uuid) : (isExact ? Bukkit.getPlayerExact(name) : Bukkit.getPlayer(name));
            })
      );
      final boolean hasIfCached = Skript.methodExists(Bukkit.class, "getOfflinePlayerIfCached", String.class);
      List<Parameter<?>> params = new ArrayList<>();
      params.add(new Parameter<>("nameOrUUID", DefaultClasses.STRING, true, null));
      if (hasIfCached) {
         params.add(new Parameter<>("allowLookups", DefaultClasses.BOOLEAN, true, new SimpleLiteral<>(true, true)));
      }

      Functions.registerFunction(
            new SimpleJavaFunction<OfflinePlayer>("offlineplayer", params.toArray(new Parameter[0]), DefaultClasses.OFFLINE_PLAYER, true) {
               public OfflinePlayer[] executeSimple(Object[][] params) {
                  String name = (String)params[0][0];
                  UUID uuid = null;
                  if ((name.length() > 16 || name.contains("-")) && Utils.isValidUUID(name)) {
                     uuid = UUID.fromString(name);
                  }

                  OfflinePlayer result;
                  if (uuid != null) {
                     result = Bukkit.getOfflinePlayer(uuid);
                  } else if (hasIfCached && !(Boolean)params[1][0]) {
                     result = Bukkit.getOfflinePlayerIfCached(name);
                     if (result == null) {
                        return new OfflinePlayer[0];
                     }
                  } else {
                     result = Bukkit.getOfflinePlayer(name);
                  }

                  return CollectionUtils.array(result);
               }
            }
         )
         .description(
            "Returns a offline player from their name or UUID. This function will still return the player if they're online. If Paper 1.16.5+ is used, the 'allowLookup' parameter can be set to false to prevent this function from doing a web lookup for players who have not joined before. Lookups can cause lag spikes of up to multiple seconds, so use offline players with caution."
         )
         .examples(
            "set {_p} to offlineplayer(\"Notch\")",
            "set {_p} to offlineplayer(\"069a79f4-44e9-4726-a5be-fca90e38aaf5\")",
            "set {_p} to offlineplayer(\"Notch\", false)"
         )
         .since("2.8.0, 2.9.0 (prevent lookups)");
      Functions.register(
         DefaultFunction.builder(skript, "entity", Entity.class)
            .description("Returns an entity from a given UUID.", "If the entity is unloaded or an offline player, the function will return nothing.")
            .examples("set {_entity} to entity({uuid})", "kill entity({uuid})", "set {_type} to type of entity({uuid})")
            .since("2.16")
            .parameter("uuid", UUID.class)
            .build(args -> {
               UUID uuid = args.get("uuid");
               return Bukkit.getEntity(uuid);
            })
      );
      Functions.registerFunction(new SimpleJavaFunction<Boolean>("isNaN", numberParam, DefaultClasses.BOOLEAN, true) {
            public Boolean[] executeSimple(Object[][] params) {
               return new Boolean[]{Double.isNaN(((Number)params[0][0]).doubleValue())};
            }
         })
         .description("Returns true if the input is NaN (not a number).")
         .examples("isNaN(0) # false", "isNaN(0/0) # true", "isNaN(sqrt(-1)) # true")
         .since("2.8.0");
      Functions.register(
         DefaultFunction.builder(skript, "concat", String.class)
            .description("Joins the provided texts (and other things) into a single text.")
            .examples("concat(\"hello \", \"there\") # hello there", "concat(\"foo \", 100, \" bar\") # foo 100 bar")
            .since("2.9.0")
            .parameter("texts", Object[].class)
            .build(args -> {
               StringBuilder builder = new StringBuilder();
               Object[] objects = args.get("texts");

               for (Object object : objects) {
                  builder.append(Classes.toString(object));
               }

               return builder.toString();
            })
      );
      if (Skript.classExists("org.joml.Quaternionf")) {
         Functions.registerFunction(
               new SimpleJavaFunction<Quaternionf>(
                  "quaternion",
                  new Parameter[]{
                     new Parameter<>("w", DefaultClasses.NUMBER, true, null),
                     new Parameter<>("x", DefaultClasses.NUMBER, true, null),
                     new Parameter<>("y", DefaultClasses.NUMBER, true, null),
                     new Parameter<>("z", DefaultClasses.NUMBER, true, null)
                  },
                  Classes.getExactClassInfo(Quaternionf.class),
                  true
               ) {
                  public Quaternionf[] executeSimple(Object[][] params) {
                     double w = ((Number)params[0][0]).doubleValue();
                     double x = ((Number)params[1][0]).doubleValue();
                     double y = ((Number)params[2][0]).doubleValue();
                     double z = ((Number)params[3][0]).doubleValue();
                     return CollectionUtils.array(new Quaternionf(x, y, z, w));
                  }
               }
            )
            .description("Returns a quaternion from the given W, X, Y and Z parameters. ")
            .examples("quaternion(1, 5.6, 45.21, 10)")
            .since("2.10");
      }

      if (Skript.classExists("org.joml.AxisAngle4f")) {
         Functions.registerFunction(
               new SimpleJavaFunction<Quaternionf>(
                  "axisAngle",
                  new Parameter[]{new Parameter<>("angle", DefaultClasses.NUMBER, true, null), new Parameter<>("axis", DefaultClasses.VECTOR, true, null)},
                  Classes.getExactClassInfo(Quaternionf.class),
                  true
               ) {
                  public Quaternionf[] executeSimple(Object[][] params) {
                     float angle = (float)(((Number)params[0][0]).floatValue() / 180.0F * Math.PI);
                     Vector v = (Vector)params[1][0];
                     if (!v.isZero() && Double.isFinite(v.getX()) && Double.isFinite(v.getY()) && Double.isFinite(v.getZ())) {
                        Vector3f axis = ((Vector)params[1][0]).toVector3f();
                        return CollectionUtils.array(new Quaternionf(new AxisAngle4f(angle, axis)));
                     } else {
                        return new Quaternionf[0];
                     }
                  }
               }
            )
            .description(
               "Returns a quaternion from the given angle (in degrees) and axis (as a vector). This represents a rotation around the given axis by the given angle."
            )
            .examples("axisangle(90, (vector from player's facing))")
            .since("2.10");
      }

      Functions.registerFunction(
            new SimpleJavaFunction<String>(
               "formatNumber",
               new Parameter[]{
                  new Parameter<>("number", DefaultClasses.NUMBER, true, null),
                  new Parameter<>("format", DefaultClasses.STRING, true, new SimpleLiteral<>("", true))
               },
               DefaultClasses.STRING,
               true
            ) {
               public String[] executeSimple(Object[][] params) {
                  Number number = (Number)params[0][0];
                  String format = (String)params[1][0];
                  if (format.isEmpty()) {
                     return !(number instanceof Double) && !(number instanceof Float)
                        ? new String[]{DefaultFunctions.DEFAULT_INTEGER_FORMAT.format(number)}
                        : new String[]{DefaultFunctions.DEFAULT_DECIMAL_FORMAT.format(number)};
                  } else {
                     try {
                        return new String[]{new DecimalFormat(format).format(number)};
                     } catch (IllegalArgumentException var5) {
                        return null;
                     }
                  }
               }
            }
         )
         .description(
            "Converts numbers to human-readable format. By default, '###,###' (e.g. '123,456,789') will be used for whole numbers and '###,###.##' (e.g. '123,456,789.00) will be used for decimal numbers. A hashtag '#' represents a digit, a comma ',' is used to separate numbers, and a period '.' is used for decimals. ",
            "Will return none if the format is invalid.",
            "For further reference, see this <a href=\"https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html\" target=\"_blank\">article</a>."
         )
         .examples(
            "command /balance:",
            "\taliases: bal",
            "\texecutable by: players",
            "\ttrigger:",
            "\t\tset {_money} to formatNumber({money::%sender's uuid%})",
            "\t\tsend \"Your balance: %{_money}%\" to sender"
         )
         .since("2.10");
      Functions.registerFunction(
         (new SimpleJavaFunction<UUID>(
               "uuid", new Parameter[]{new Parameter<>("uuid", DefaultClasses.STRING, true, null)}, Classes.getExactClassInfo(UUID.class), true
            ) {
               public UUID[] executeSimple(Object[][] params) {
                  String uuid = (String)params[0][0];
                  return Utils.isValidUUID(uuid) ? CollectionUtils.array(UUID.fromString(uuid)) : new UUID[0];
               }
            })
            .description(new String[]{"Returns a UUID from the given string. The string must be in the format of a UUID."})
            .examples("uuid(\"069a79f4-44e9-4726-a5be-fca90e38aaf5\")")
            .since("2.11")
      );
      Functions.registerFunction(
            new SimpleJavaFunction<Number>("mean", new Parameter[]{new Parameter<>("numbers", DefaultClasses.NUMBER, false, null)}, DefaultClasses.NUMBER, true) {
               @Nullable
               public Number[] executeSimple(Object[][] params) {
                  Double total = 0.0;
                  int length = params[0].length;
                  int i = 0;

                  while (i < length) {
                     Number number = (Number)params[0][i];
                     if (!Double.isInfinite(number.doubleValue()) && !Double.isNaN(number.doubleValue())) {
                        if (!total.isInfinite() && !total.isNaN()) {
                           total = total + number.doubleValue() / length;
                           i++;
                           continue;
                        }

                        return null;
                     }

                     return null;
                  }

                  return new Number[]{total};
               }
            }
         )
         .description("Get the mean (average) of a list of numbers.", "You cannot get the mean of a set of numbers that includes infinity or NaN.")
         .examples("mean(1, 2, 3) = 2", "mean(0, 5, 10) = 5", "mean(13, 97, 376, 709) = 298.75")
         .since("2.11");
      Functions.registerFunction(
            new SimpleJavaFunction<Number>(
               "median", new Parameter[]{new Parameter<>("numbers", DefaultClasses.NUMBER, false, null)}, DefaultClasses.NUMBER, true
            ) {
               @Nullable
               public Number[] executeSimple(Object[][] params) {
                  AtomicBoolean invalid = new AtomicBoolean(false);
                  Number[] sorted = Arrays.stream(params[0]).filter(object -> {
                     if (object instanceof Number number) {
                        if (Double.isNaN(number.doubleValue())) {
                           invalid.set(true);
                           return false;
                        } else {
                           return true;
                        }
                     } else {
                        return false;
                     }
                  }).map(object -> (Number)object).sorted((o1, o2) -> {
                     Double n1 = o1.doubleValue();
                     Double n2 = o2.doubleValue();
                     return n1.compareTo(n2);
                  }).toArray(Number[]::new);
                  if (invalid.get()) {
                     return null;
                  } else {
                     int size = sorted.length;
                     if (size % 2 == 1) {
                        return new Number[]{sorted[Math2.ceil((float)(size / 2))]};
                     } else {
                        int half = size / 2;
                        double first = sorted[half - 1].doubleValue();
                        double second = sorted[half].doubleValue();
                        double median = (first + second) / 2.0;
                        return new Number[]{median};
                     }
                  }
               }
            }
         )
         .description(
            "Get the middle value of a sorted list of numbers. If the list has an even number of values, the median is the average of the two middle numbers.",
            "You cannot get the median of a set of numbers that includes NaN."
         )
         .examples("median(1, 2, 3, 4, 5) = 3", "median(1, 2, 3, 4, 5, 6) = 3.5", "median(0, 123, 456, 789) = 289.5")
         .since("2.11");
      Functions.registerFunction(
            new SimpleJavaFunction<Number>(
               "factorial", new Parameter[]{new Parameter<>("number", DefaultClasses.NUMBER, true, null)}, DefaultClasses.NUMBER, true
            ) {
               @Nullable
               public Number[] executeSimple(Object[][] params) {
                  Double number = ((Number)params[0][0]).doubleValue();
                  if (number < 0.0) {
                     return null;
                  } else if (number <= 1.0) {
                     return new Number[]{1};
                  } else if (number > 170.0) {
                     return new Number[]{Double.POSITIVE_INFINITY};
                  } else {
                     Double result = 1.0;

                     for (double i = number; i > 1.0 && !result.isInfinite() && !result.isNaN(); i--) {
                        result = result * i;
                     }

                     return new Number[]{result};
                  }
               }
            }
         )
         .description(
            "Get the factorial of a number.",
            "Getting the factorial of any number above 21 will return an approximation, not an exact value.",
            "Any number after 170 will always return Infinity.",
            "Should not be used to calculate permutations or combinations manually."
         )
         .examples("factorial(0) = 1", "factorial(3) = 3*2*1 = 6", "factorial(5) = 5*4*3*2*1 = 120", "factorial(171) = Infinity")
         .since("2.11");
      Functions.registerFunction(
            new SimpleJavaFunction<Number>(
               "root",
               new Parameter[]{new Parameter<>("n", DefaultClasses.NUMBER, true, null), new Parameter<>("number", DefaultClasses.NUMBER, true, null)},
               DefaultClasses.NUMBER,
               true
            ) {
               @Nullable
               public Number[] executeSimple(Object[][] params) {
                  Double n = ((Number)params[0][0]).doubleValue();
                  Double number = ((Number)params[1][0]).doubleValue();
                  if (n == 0.0) {
                     return null;
                  } else if (n == 1.0) {
                     return new Number[]{number};
                  } else {
                     return n == 2.0 ? new Number[]{Math.sqrt(number)} : new Number[]{Math.pow(number, 1.0 / n)};
                  }
               }
            }
         )
         .description("Calculates the <i>n</i>th root of a number.")
         .examples("root(2, 4) = 2 # same as sqrt(4)", "root(4, 16) = 2", "root(-4, 16) = 0.5 # same as 16^(-1/4)")
         .since("2.11");
      Functions.registerFunction(
            new SimpleJavaFunction<Number>(
               "permutations",
               new Parameter[]{new Parameter<>("options", DefaultClasses.NUMBER, true, null), new Parameter<>("selected", DefaultClasses.NUMBER, true, null)},
               DefaultClasses.NUMBER,
               true
            ) {
               @Nullable
               public Number[] executeSimple(Object[][] params) {
                  Double options = ((Number)params[0][0]).doubleValue();
                  Double selected = ((Number)params[1][0]).doubleValue();
                  if (selected > options || selected < 0.0) {
                     return null;
                  } else if (selected.equals(0.0)) {
                     return new Number[]{1};
                  } else if (selected.equals(1.0)) {
                     return new Number[]{options};
                  } else {
                     Double result = 1.0;

                     for (double i = options; i > options - selected && !result.isInfinite() && !result.isNaN(); i--) {
                        result = result * i;
                     }

                     return new Number[]{result};
                  }
               }
            }
         )
         .description(
            "Get the number of possible ordered arrangements from 1 to 'options' with each arrangement having a size equal to 'selected'",
            "For example, permutations with 3 options and an arrangement size of 1, returns 3: (1), (2), (3)",
            "Permutations with 3 options and an arrangement size of 2 returns 6: (1, 2), (1, 3), (2, 1), (2, 3), (3, 1), (3, 2)",
            "Note that the bigger the 'options' and lower the 'selected' may result in approximations or even infinity values.",
            "Permutations differ from combinations in that permutations account for the arrangement of elements within the set, whereas combinations focus on unique sets and ignore the order of elements.",
            "Example: (1, 2) and (2, 1) are two distinct permutations because the positions of '1' and '2' are different, but they represent a single combination since order doesn't matter in combinations."
         )
         .examples("permutations(10, 2) = 90", "permutations(10, 4) = 5040", "permutations(size of {some list::*}, 2)")
         .since("2.11");
      Functions.registerFunction(
            new SimpleJavaFunction<Number>(
               "combinations",
               new Parameter[]{new Parameter<>("options", DefaultClasses.NUMBER, true, null), new Parameter<>("selected", DefaultClasses.NUMBER, true, null)},
               DefaultClasses.NUMBER,
               true
            ) {
               @Nullable
               public Number[] executeSimple(Object[][] params) {
                  Double options = ((Number)params[0][0]).doubleValue();
                  Double selected = ((Number)params[1][0]).doubleValue();
                  if (selected > options || selected < 0.0) {
                     return null;
                  } else if (selected.equals(0.0)) {
                     return new Number[]{1};
                  } else if (selected.equals(1.0)) {
                     return new Number[]{options};
                  } else {
                     Double top = 1.0;

                     for (double i = options; i > options - selected; i--) {
                        if (top.isInfinite() || top.isNaN()) {
                           return new Number[]{top};
                        }

                        top = top * i;
                     }

                     Double bottom = selected;

                     for (double i = selected - 1.0; i > 1.0 && !bottom.isInfinite() && !bottom.isNaN(); i--) {
                        bottom = bottom * i;
                     }

                     return new Number[]{top / bottom};
                  }
               }
            }
         )
         .description(
            "Get the number of possible sets from 1 to 'options' with each set having a size equal to 'selected'",
            "For example, a combination with 3 options and a set size of 1, returns 3: (1), (2), (3)",
            "A combination of 3 options with a set size of 2 returns 3: (1, 2), (1, 3), (2, 3)",
            "Note that the bigger the 'options' and lower the 'selected' may result in approximations or even infinity values.",
            "Combinations differ from permutations in that combinations focus on unique sets, ignoring the order of elements, whereas permutations account for the arrangement of elements within the set.",
            "Example: (1, 2) and (2, 1) represent a single combination since order doesn't matter in combinations, but they are two distinct permutations because permutations consider the order."
         )
         .examples("combinations(10, 8) = 45", "combinations(5, 3) = 10", "combinations(size of {some list::*}, 2)")
         .since("2.11");
   }
}
