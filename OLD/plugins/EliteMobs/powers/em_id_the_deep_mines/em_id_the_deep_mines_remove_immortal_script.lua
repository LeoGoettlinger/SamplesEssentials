local IMMORTAL_OFF_TAG = "ImmortalOff"
local IMMORTAL_OFF_DURATION_TICKS = 9999

local EFFECT_RANGE = 25
local AURA_UPDATE_INTERVAL_TICKS = 6

local function apply_remove_immortal(context)
  if not context.boss:is_alive() then
    return
  end

  for _, player in ipairs(context.boss:get_nearby_players(EFFECT_RANGE)) do
    player:spawn_particle_at_self({
      particle = "FLAME",
      amount = 1,
    })
  end

  for _, elite in ipairs(context.entities:get_nearby_entities(EFFECT_RANGE, "elites")) do
    if not elite:has_tag(IMMORTAL_OFF_TAG) then
      elite:add_tag(IMMORTAL_OFF_TAG, IMMORTAL_OFF_DURATION_TICKS)
    end
  end
end

local function notify_aura_once(context, player)
  if not player.is_player then
    return
  end

  context.state.notified_aura_players = context.state.notified_aura_players or {}
  if context.state.notified_aura_players[player.uuid] then
    return
  end

  context.state.notified_aura_players[player.uuid] = true
  player:show_title(
    "&dFlame Aura Gained",
    "&dNether Fiend can now be hurt",
    10,
    60,
    10
  )
end

local function start_script(context)
  if context.state.remove_immortal_task == nil then
    context.state.remove_immortal_task = context.scheduler:run_every(AURA_UPDATE_INTERVAL_TICKS, function(task_context)
      apply_remove_immortal(task_context)
    end)
  end

  if context.state.flame_aura_watch_task == nil then
    local flame_aura = context.script:zone({
      shape = "CYLINDER",
      radius = 8,
      height = 10,
      filter = "PLAYER",
      target = {
        targetType = "SELF",
        offset = "0,-5,0",
      },
    })

    context.state.flame_aura_watch_task = flame_aura:watch({
      on_enter = function(player)
        notify_aura_once(context, player)
      end,
    })
  end
end

return {
  api_version = 1,
  priority = 0,

  on_spawn = function(context)
    start_script(context)
    context.log.info("Loaded dsf.lua.")
  end,
}