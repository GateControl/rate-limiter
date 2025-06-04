local policy_key = KEYS[1]
local metrics_stream_key = KEYS[2]
local requested = tonumber(ARGV[1])

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
    last_timestamp = last_timestamp + refill_periods * refill_interval
end

local allowed = tokens >= requested

if allowed then
    tokens = tokens - requested
    redis.call('HINCRBY', policy_key, 'allowCount', 1)
else
    redis.call('HINCRBY', policy_key, 'throttleCount', 1)
end

redis.call('HMSET', policy_key,
    'tokens', tokens,
    'timestamp', last_timestamp)

local counts = redis.call('HMGET', policy_key, 'allowCount', 'throttleCount')
local allowCount = tonumber(counts[1]) or 0
local throttleCount = tonumber(counts[2]) or 0


redis.call('XADD', metrics_stream_key, '*',
    'policyId', policy_key,
    'allowed', allowed and 1 or 0,
    'tokens', tokens,
    'timestamp', last_timestamp,
    'capacity', capacity,
    'refillTokens', refill_tokens,
    'refillIntervalMillis', refill_interval,
    'refillPeriods', refill_periods,
    'allowCount', allowCount,
    'throttleCount', throttleCount
)

return { allowed and 1 or 0, tokens }