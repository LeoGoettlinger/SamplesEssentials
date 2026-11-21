package ch.njol.skript.lang;

public interface Unit extends Cloneable {
   int getAmount();

   void setAmount(double var1);

   @Override
   String toString();

   String toString(int var1);

   Unit clone();
}
