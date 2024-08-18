use friend;

create table user
(
    id              bigint auto_increment comment 'id'
        primary key,
    username        varchar(256)                       null comment '用户昵称',
    userDescription varchar(512)                       null comment '用户描述',
    userAccount     varchar(256)                       null comment '账号',
    avatarUrl       varchar(1024)                      null comment '用户头像',
    gender          tinyint                            null comment ' 性别 0 - 男  1 - 女',
    userPassword    varchar(512)                       not null comment '密码',
    qq              varchar(64)                        null comment 'qq号',
    phone           varchar(128)                       null comment '电话',
    email           varchar(512)                       null comment '邮箱',
    userStatus      int      default 0                 not null comment '状态 0 - 正常',
    createTime      datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete        tinyint  default 0                 not null comment '是否删除',
    userRole        int      default 0                 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    tags            varchar(1024)                      null comment '标签 json 数组'
)
    comment '用户';


create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint                             null comment '用户id',
    teamId     bigint                             null comment '队伍id',
    joinTime   datetime default CURRENT_TIMESTAMP null comment '用户加入队伍时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '用户-队伍关系表';


create table team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '队伍描述',
    maxNum      int      default 1                 null comment '队伍的最大人数',
    userId      bigint                             not null comment '创建人id',
    expireTime  datetime                           null comment '队伍过期时间',
    status      int      default 0                 null comment '队伍类型 0 - 公开， 1 - 私密， 2 - 加密',
    password    varchar(512)                       null comment '加密队伍的密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
)
    comment '队伍';