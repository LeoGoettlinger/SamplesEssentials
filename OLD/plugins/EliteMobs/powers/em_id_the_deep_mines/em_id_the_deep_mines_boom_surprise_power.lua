local function apply_full_heal(context)
  if context.boss:is_alive() then
    context.boss:apply_potion_effect("INSTANT_HEALTH", 20, 99)
  end
end

return {
  api_version = 1,
  priority = 0,

  on_spawn = function(context)
    -- Legacy repeatEvery/times behavior: apply immediately, then once after 9998 ticks.
    context.boss:apply_potion_effect("SLOWNESS", 9999, 99)

    context.scheduler:run_after(9998, function(context)
      if context.boss:is_alive() then
        context.boss:apply_potion_effect("SLOWNESS", 9999, 99)
      end
    end)
  end,

  on_enter_combat = function(context)
    apply_full_heal(context)
  end,

  on_boss_target_changed = function(context)
    apply_full_heal(context)
  end,

  on_death = function(context)
    local explosion_zone = context.script:zone({
      shape = "DOME",
      radius = 6,
      Target = {
        targetType = "SELF",
        offset = "4,0,0"
      }
    })

    local nearby_players = context.boss:get_nearby_players(15)
    for index = 1, #nearby_players do
      nearby_players[index]:play_sound_at_self("entity.generic.explode", 1, 1)
    end

    context.script:spawn_particles(
      explosion_zone:full_target(0.03),
      { particle = "EXPLOSION" }
    )

    context.script:damage(explosion_zone:full_target(), 1, 1)

    local locations = explosion_zone:full_locations()
    for index = 1, #locations do
      context.world:place_temporary_block_at_location(
        locations[index],
        "AIR",
        9999,
        false
      )
    end
  end,
}