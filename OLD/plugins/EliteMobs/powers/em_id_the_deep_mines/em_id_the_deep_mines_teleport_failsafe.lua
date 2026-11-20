return {
  api_version = 1,

  on_game_tick = function(context)
    -- Check once per second. This catches the Fiend before its chunk can unload
    -- without running player/destination checks every tick.
    local CHECK_INTERVAL_TICKS = 20
    local RECOVERY_DISTANCE = 48
    local DEBUG = false

    context.state.fiend_failsafe_ticks = (context.state.fiend_failsafe_ticks or 0) + 1
    if context.state.fiend_failsafe_ticks < CHECK_INTERVAL_TICKS then
      return
    end
    context.state.fiend_failsafe_ticks = 0

    if not context.boss:is_alive() then
      return
    end

    local boss_location = context.boss:get_location()
    local players = context.players:all_players_in_world()
    local closest_player = nil
    local closest_distance_squared = nil

    -- This only loops through players in the Fiend's current world; it does not
    -- scan chunks or nearby entities.
    for index = 1, #players do
      local player = players[index]
      if player.game_mode ~= "SPECTATOR"
          and player:is_alive() then
        local player_location = player:get_location()
        local dx = boss_location.x - player_location.x
        local dy = boss_location.y - player_location.y
        local dz = boss_location.z - player_location.z
        local distance_squared = (dx * dx) + (dy * dy) + (dz * dz)

        if closest_distance_squared == nil or distance_squared < closest_distance_squared then
          closest_player = player
          closest_distance_squared = distance_squared
        end
      end
    end

    if closest_player == nil then
      if DEBUG then
        context.log:info("[FiendFailsafe] No eligible player is in the Fiend's world.")
      end
      return
    end

    if closest_distance_squared <= (RECOVERY_DISTANCE * RECOVERY_DISTANCE) then
      return
    end

    local player_location = closest_player:get_location()
    local boss_height = math.max(2, math.ceil(context.boss:get_height()))
    local offsets = {
      { 0, 0 },
      { 2, 0 },
      { -2, 0 },
      { 0, 2 },
      { 0, -2 },
      { 2, 2 },
      { 2, -2 },
      { -2, 2 },
      { -2, -2 },
      { 4, 0 },
      { -4, 0 },
      { 0, 4 },
      { 0, -4 }
    }
    local destination = nil

    -- Try a bounded set of positions 2-4 blocks above the player. This avoids
    -- an expensive open-ended safe-location search.
    for _, offset in ipairs(offsets) do
      if destination ~= nil then
        break
      end

      for height = 2, 4 do
        local candidate = {
          world = player_location.world,
          x = player_location.x + offset[1],
          y = player_location.y + height,
          z = player_location.z + offset[2],
          yaw = boss_location.yaw,
          pitch = boss_location.pitch
        }
        local clear = true

        for block_offset = 0, boss_height - 1 do
          local block_location = {
            world = candidate.world,
            x = candidate.x,
            y = candidate.y + block_offset,
            z = candidate.z
          }

          if not context.world:is_passable_at_location(block_location) then
            clear = false
            break
          end
        end

        if clear then
          destination = candidate
          break
        end
      end
    end

    if destination == nil then
      if DEBUG then
        context.log:warn("[FiendFailsafe] No passable teleport destination was found near " .. closest_player.name .. ".")
      end
      return
    end

    context.boss:teleport_to_location(destination)
    context.boss:set_velocity_vector({ x = 0, y = 0, z = 0 })

    if DEBUG then
      context.log:info(
        "[FiendFailsafe] Teleported to " .. closest_player.name
          .. " at " .. math.floor(destination.x)
          .. ", " .. math.floor(destination.y)
          .. ", " .. math.floor(destination.z) .. "."
      )
    end
  end
}
