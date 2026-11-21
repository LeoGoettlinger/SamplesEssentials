package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.mind.EliteMindProgram;
import com.magmaguy.elitemobs.magmacore.ai.MindProgram;
import org.bukkit.plugin.Plugin;

interface EliteMindProgramEntry {
   Plugin owner();

   EliteMindProgram descriptor();

   MindProgram instantiate();
}
