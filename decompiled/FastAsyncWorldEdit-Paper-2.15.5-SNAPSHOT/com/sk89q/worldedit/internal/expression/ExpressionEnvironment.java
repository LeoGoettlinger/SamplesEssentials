package com.sk89q.worldedit.internal.expression;

public interface ExpressionEnvironment extends Cloneable {
   int getBlockType(double x, double y, double z);

   int getBlockData(double x, double y, double z);

   int getBlockTypeAbs(double x, double y, double z);

   int getBlockDataAbs(double x, double y, double z);

   int getBlockTypeRel(double x, double y, double z);

   int getBlockDataRel(double x, double y, double z);

   ExpressionEnvironment clone();
}
