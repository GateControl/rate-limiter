local policy_key = KEYS[1]
local now = redis.call('TIME')
local current_time = tonumber(now[1]) * 1000 + math.floor(tonumber(now[2]) / 1000)

local data = redis.call('HMGET', policy_key, 'tokens', 'timestamp', 'capacity', 'refillTokens', 'refillIntervalMillis')
local tokens = tonumber(data[1]) or 0
local last_timestamp = tonumber(data[2]) or current_time
local capacity = tonumber(data[3]) or 0
local refill_tokens = tonumber(data[4]) or 0
local refill_interval = tonumber(data[5]) or 1

local elapsed = current_time - last_timestamp
local refill_periods = math.floor(elapsed / refill_interval)
if refill_periods > 0 then
    tokens = math.min(capacity, tokens + refill_periods * refill_tokens)
end

return tokens