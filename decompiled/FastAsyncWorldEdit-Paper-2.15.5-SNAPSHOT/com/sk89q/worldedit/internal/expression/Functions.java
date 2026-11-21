package com.sk89q.worldedit.internal.expression;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.Doubles;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.noise.PerlinNoise;
import com.sk89q.worldedit.math.noise.RidgedMultiFractalNoise;
import com.sk89q.worldedit.math.noise.VoronoiNoise;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.concurrent.ThreadLocalRandom;

public final class Functions {
   private static final MethodHandle DOUBLE_VALUE;
   private static final Int2ObjectMap<double[]> globalMegaBuffer;
   private final Int2ObjectMap<double[]> megaBuffer = new Int2ObjectOpenHashMap();
   private final SetMultimap<String, MethodHandle> map;
   private ExpressionEnvironment environment;
   private static final ThreadLocal<PerlinNoise> localPerlin;
   private static final ThreadLocal<VoronoiNoise> localVoronoi;
   private static final ThreadLocal<RidgedMultiFractalNoise> localRidgedMulti;

   static Functions create() {
      return new Functions();
   }

   private static MethodHandle clean(MethodHandle handle) {
      boolean wasVarargs = handle.isVarargsCollector();
      handle = handle.asType(handle.type().wrap());
      if (handle.type().returnType() != Double.class) {
         Preconditions.checkState(Number.class.isAssignableFrom(handle.type().returnType()), "Function does not return a number");
         handle = handle.asType(handle.type().changeReturnType(Number.class));
         handle = MethodHandles.filterReturnValue(handle, DOUBLE_VALUE);
         handle = handle.asType(handle.type().wrap());
      }

      if (wasVarargs) {
         handle = handle.asVarargsCollector(handle.type().parameterType(handle.type().parameterCount() - 1));
      }

      return handle;
   }

   private static void addMathHandles(SetMultimap<String, MethodHandle> map, Lookup lookup) throws NoSuchMethodException, IllegalAccessException {
      UnmodifiableIterator var2 = ImmutableList.of(
            "sin",
            "cos",
            "tan",
            "asin",
            "acos",
            "atan",
            "sinh",
            "cosh",
            "tanh",
            "sqrt",
            "cbrt",
            "abs",
            new String[]{"ceil", "floor", "rint", "exp", "log", "log10"}
         )
         .iterator();

      while (var2.hasNext()) {
         String name = (String)var2.next();
         map.put(name, lookup.findStatic(Math.class, name, MethodType.methodType(double.class, double.class)));
      }

      map.put("ln", lookup.findStatic(Math.class, "log", MethodType.methodType(double.class, double.class)));
      map.put("round", lookup.findStatic(Math.class, "round", MethodType.methodType(long.class, double.class)));
      map.put("atan2", lookup.findStatic(Math.class, "atan2", MethodType.methodType(double.class, double.class, double.class)));
      map.put("min", lookup.findStatic(Doubles.class, "min", MethodType.methodType(double.class, double[].class)).asVarargsCollector(double[].class));
      map.put("max", lookup.findStatic(Doubles.class, "max", MethodType.methodType(double.class, double[].class)).asVarargsCollector(double[].class));
   }

   private static void addStaticFunctionHandles(SetMultimap<String, MethodHandle> map, Lookup lookup) throws NoSuchMethodException, IllegalAccessException {
      map.put(
         "rotate",
         lookup.findStatic(Functions.class, "rotate", MethodType.methodType(double.class, LocalSlot.Variable.class, LocalSlot.Variable.class, double.class))
      );
      map.put("swap", lookup.findStatic(Functions.class, "swap", MethodType.methodType(double.class, LocalSlot.Variable.class, LocalSlot.Variable.class)));
      map.put("random", lookup.findStatic(Functions.class, "random", MethodType.methodType(double.class)));
      map.put("randint", lookup.findStatic(Functions.class, "randint", MethodType.methodType(double.class, double.class)));
      map.put(
         "perlin",
         lookup.findStatic(
            Functions.class,
            "perlin",
            MethodType.methodType(double.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class)
         )
      );
      map.put(
         "voronoi",
         lookup.findStatic(
            Functions.class, "voronoi", MethodType.methodType(double.class, double.class, double.class, double.class, double.class, double.class)
         )
      );
      map.put(
         "ridgedmulti",
         lookup.findStatic(
            Functions.class,
            "ridgedmulti",
            MethodType.methodType(double.class, double.class, double.class, double.class, double.class, double.class, double.class)
         )
      );
   }

   private void addInstanceFunctionHandles(SetMultimap<String, MethodHandle> map, Lookup lookup) throws NoSuchMethodException, IllegalAccessException {
      map.put("megabuf", lookup.findSpecial(Functions.class, "megabuf", MethodType.methodType(double.class, double.class), Functions.class).bindTo(this));
      map.put(
         "megabuf",
         lookup.findSpecial(Functions.class, "megabuf", MethodType.methodType(double.class, double.class, double.class), Functions.class).bindTo(this)
      );
      map.put(
         "closest",
         lookup.findSpecial(
               Functions.class,
               "closest",
               MethodType.methodType(double.class, double.class, double.class, double.class, double.class, double.class, double.class),
               Functions.class
            )
            .bindTo(this)
      );
      map.put(
         "query",
         lookup.findSpecial(
               Functions.class,
               "query",
               MethodType.methodType(double.class, double.class, double.class, double.class, LocalSlot.class, LocalSlot.class),
               Functions.class
            )
            .bindTo(this)
      );
      map.put(
         "queryAbs",
         lookup.findSpecial(
               Functions.class,
               "queryAbs",
               MethodType.methodType(double.class, double.class, double.class, double.class, LocalSlot.class, LocalSlot.class),
               Functions.class
            )
            .bindTo(this)
      );
      map.put(
         "queryRel",
         lookup.findSpecial(
               Functions.class,
               "queryRel",
               MethodType.methodType(double.class, double.class, double.class, double.class, LocalSlot.class, LocalSlot.class),
               Functions.class
            )
            .bindTo(this)
      );
   }

   private static double rotate(LocalSlot.Variable x, LocalSlot.Variable y, double angle) {
      double cosF = Math.cos(angle);
      double sinF = Math.sin(angle);
      double xOld = x.value();
      double yOld = y.value();
      x.setValue(xOld * cosF - yOld * sinF);
      y.setValue(xOld * sinF + yOld * cosF);
      return 0.0;
   }

   private static double swap(LocalSlot.Variable x, LocalSlot.Variable y) {
      double tmp = x.value();
      x.setValue(y.value());
      y.setValue(tmp);
      return 0.0;
   }

   private Functions() {
      Lookup lookup = MethodHandles.lookup();
      SetMultimap<String, MethodHandle> map = HashMultimap.create();

      try {
         addMathHandles(map, lookup);
         addStaticFunctionHandles(map, lookup);
         this.addInstanceFunctionHandles(map, lookup);
      } catch (IllegalAccessException | NoSuchMethodException var4) {
         throw new IllegalStateException(var4);
      }

      this.map = ImmutableSetMultimap.copyOf(Multimaps.transformValues(map, Functions::clean));
   }

   public SetMultimap<String, MethodHandle> getMap() {
      return this.map;
   }

   public ExpressionEnvironment getEnvironment() {
      return this.environment;
   }

   public void setEnvironment(ExpressionEnvironment environment) {
      this.environment = environment;
   }

   private static double[] getSubBuffer(Int2ObjectMap<double[]> megabuf, int key) {
      if (megabuf.size() > 16384) {
         megabuf.clear();
         throw new ExpressionException(-1, "memory limit reached");
      } else {
         return (double[])megabuf.computeIfAbsent(key, k -> new double[1024]);
      }
   }

   private static double getBufferItem(final Int2ObjectMap<double[]> megabuf, final int index) {
      return getSubBuffer(megabuf, index & -1024)[index & 1023];
   }

   private static double setBufferItem(final Int2ObjectMap<double[]> megabuf, final int index, double value) {
      return getSubBuffer(megabuf, index & -1024)[index & 1023] = value;
   }

   private static double gmegabuf(double index) {
      return getBufferItem(globalMegaBuffer, (int)index);
   }

   private static double gmegabuf(double index, double value) {
      return setBufferItem(globalMegaBuffer, (int)index, value);
   }

   private double megabuf(double index) {
      return getBufferItem(this.megaBuffer, (int)index);
   }

   private double megabuf(double index, double value) {
      return setBufferItem(this.megaBuffer, (int)index, value);
   }

   private double closest(double x, double y, double z, double index, double count, double stride) {
      return findClosest(this.megaBuffer, x, y, z, (int)index, (int)count, (int)stride);
   }

   private static double gclosest(double x, double y, double z, double index, double count, double stride) {
      return findClosest(globalMegaBuffer, x, y, z, (int)index, (int)count, (int)stride);
   }

   private static double findClosest(Int2ObjectMap<double[]> megabuf, double x, double y, double z, int index, int count, int stride) {
      int closestIndex = -1;
      double minDistanceSquared = Double.MAX_VALUE;

      for (int i = 0; i < count; i++) {
         double currentX = getBufferItem(megabuf, index) - x;
         double currentY = getBufferItem(megabuf, index + 1) - y;
         double currentZ = getBufferItem(megabuf, index + 2) - z;
         double currentDistanceSquared = currentX * currentX + currentY * currentY + currentZ * currentZ;
         if (currentDistanceSquared < minDistanceSquared) {
            minDistanceSquared = currentDistanceSquared;
            closestIndex = index;
         }

         index += stride;
      }

      return closestIndex;
   }

   private static double random() {
      return ThreadLocalRandom.current().nextDouble();
   }

   private static double randint(double max) {
      return ThreadLocalRandom.current().nextInt((int)Math.floor(max));
   }

   private static double perlin(double seed, double x, double y, double z, double frequency, double octaves, double persistence) {
      PerlinNoise perlin = localPerlin.get();

      try {
         perlin.setSeed((int)seed);
         perlin.setFrequency(frequency);
         perlin.setOctaveCount((int)octaves);
         perlin.setPersistence(persistence);
      } catch (IllegalArgumentException var16) {
         throw new EvaluationException(0, "Perlin noise error: " + var16.getMessage());
      }

      return perlin.noise(Vector3.at(x, y, z));
   }

   private static double voronoi(double seed, double x, double y, double z, double frequency) {
      VoronoiNoise voronoi = localVoronoi.get();

      try {
         voronoi.setSeed((int)seed);
         voronoi.setFrequency(frequency);
      } catch (IllegalArgumentException var12) {
         throw new EvaluationException(0, "Voronoi error: " + var12.getMessage());
      }

      return voronoi.noise(Vector3.at(x, y, z));
   }

   private static double ridgedmulti(double seed, double x, double y, double z, double frequency, double octaves) {
      RidgedMultiFractalNoise ridgedMulti = localRidgedMulti.get();

      try {
         ridgedMulti.setSeed((int)seed);
         ridgedMulti.setFrequency(frequency);
         ridgedMulti.setOctaveCount((int)octaves);
      } catch (IllegalArgumentException var14) {
         throw new EvaluationException(0, "Ridged multi error: " + var14.getMessage());
      }

      return ridgedMulti.noise(Vector3.at(x, y, z));
   }

   private static double queryInternal(LocalSlot type, LocalSlot data, double typeId, double dataValue) {
      double ret = type.value() != -1.0 && typeId != type.value() || data.value() != -1.0 && dataValue != data.value() ? 0.0 : 1.0;
      if (type instanceof LocalSlot.Variable) {
         ((LocalSlot.Variable)type).setValue(typeId);
      }

      if (data instanceof LocalSlot.Variable) {
         ((LocalSlot.Variable)data).setValue(dataValue);
      }

      return ret;
   }

   private double query(double x, double y, double z, LocalSlot type, LocalSlot data) {
      double typeId = this.environment.getBlockType(x, y, z);
      double dataValue = this.environment.getBlockData(x, y, z);
      return queryInternal(type, data, typeId, dataValue);
   }

   private double queryAbs(double x, double y, double z, LocalSlot type, LocalSlot data) {
      double typeId = this.environment.getBlockTypeAbs(x, y, z);
      double dataValue = this.environment.getBlockDataAbs(x, y, z);
      return queryInternal(type, data, typeId, dataValue);
   }

   private double queryRel(double x, double y, double z, LocalSlot type, LocalSlot data) {
      double typeId = this.environment.getBlockTypeRel(x, y, z);
      double dataValue = this.environment.getBlockDataRel(x, y, z);
      return queryInternal(type, data, typeId, dataValue);
   }

   static {
      Lookup lookup = MethodHandles.lookup();

      try {
         DOUBLE_VALUE = lookup.findVirtual(Number.class, "doubleValue", MethodType.methodType(double.class));
      } catch (IllegalAccessException | NoSuchMethodException var2) {
         throw new IllegalStateException(var2);
      }

      globalMegaBuffer = new Int2ObjectOpenHashMap();
      localPerlin = ThreadLocal.withInitial(PerlinNoise::new);
      localVoronoi = ThreadLocal.withInitial(VoronoiNoise::new);
      localRidgedMulti = ThreadLocal.withInitial(RidgedMultiFractalNoise::new);
   }
}
