--[[
    version:1.0
    检测key是否存在，如果存在并设置过期时间
    入参列表：
        参数个数量：1
        ARGV[1]: postId  帖子id
        ARGV[2]: userId  用户id

    返回列表code:
        +0：点赞成功
        +1：取消点赞

--]]

local postId = ARGV[1]
local userId = ARGV[2]

local postKey = "friend:post:id:" .. postId
local thumbKey = "friend:post:thumb:ids:" .. postId

--if(redis.call("EXISTS", thumbKey) == 0) then
--    redis.call("SADD", thumbKey, userId)
--    redis.call("HINCRBY", postKey, "likeCount", 1)
--    return 0
--end

if(redis.call("SISMEMBER", thumbKey, userId) == 1) then
    redis.call("SREM", thumbKey, userId)
    redis.call("HINCRBY", postKey, "likeCount", -1)
    return 1
else
    redis.call("SADD", thumbKey, userId)
    redis.call("HINCRBY", postKey, "likeCount", 1)
    return 0
end





