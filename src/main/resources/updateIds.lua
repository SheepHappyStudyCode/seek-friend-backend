-- Lua script to overwrite a Redis list
local key = KEYS[1]
local values = ARGV

-- Remove all elements from the existing list
redis.call('DEL', key)

-- Add new elements to the list
for i, v in ipairs(values) do
    redis.call('RPUSH', key, v)
end

return #values