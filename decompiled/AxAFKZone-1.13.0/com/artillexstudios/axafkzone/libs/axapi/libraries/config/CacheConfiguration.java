package com.artillexstudios.axafkzone.libs.axapi.libraries.config;

import com.artillexstudios.axafkzone.libs.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axafkzone.libs.axapi.libraries.Library;
import com.artillexstudios.axafkzone.libs.axapi.libraries.Relocation;
import com.artillexstudios.axafkzone.libs.axapi.libraries.Repository;
import java.util.ArrayList;
import java.util.List;

public class CacheConfiguration implements ConfigurationPart {
   public static List<Library> libraries = new ArrayList<>();
   public static List<Relocation> relocations = new ArrayList<>();
   public static List<Repository> repositories = new ArrayList<>();
}
