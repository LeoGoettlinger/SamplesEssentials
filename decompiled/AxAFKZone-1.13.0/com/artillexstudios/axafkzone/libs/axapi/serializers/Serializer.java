package com.artillexstudios.axafkzone.libs.axapi.serializers;

public interface Serializer<T, Z> {
   Z serialize(T object);

   T deserialize(Z value);
}
