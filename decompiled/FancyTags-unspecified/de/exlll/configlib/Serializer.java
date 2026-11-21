package de.exlll.configlib;

public interface Serializer<T1, T2> {
   T2 serialize(T1 var1);

   T1 deserialize(T2 var1);
}
