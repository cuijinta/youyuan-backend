create table tag
(
    id         bigint                             not null
        primary key,
    tagName    varchar(256)                       null comment '用户昵称',
    userId     bigint                             null comment '用户id',
    parentId   bigint                             null comment '父标签id',
    isParent   tinyint                            null comment '是否是父标签 0 - 不是   1 - 是',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 null comment '是否删除',
    constraint uniIndex_tagName
        unique (tagName)
)
    comment '标签表';

create index idx_userId
    on tag (userId);

create table user
(
    id           bigint                             not null
        primary key,
    username     varchar(256)                       null comment '用户昵称',
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '用户头像',
    gender       tinyint                            null comment '用户性别',
    userPassword varchar(512)                       null comment '用户密码',
    phone        varchar(512)                       null comment '电话',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    userStatus   int      default 0                 null comment ' 用户状态 0 - 正常 ',
    isDelete     tinyint  default 0                 null comment '是否删除',
    email        varchar(512)                       null comment '邮箱',
    userRole     int      default 0                 null,
    code         varchar(512)                       null,
    tags         varchar(1024)                      null comment '标签列表'
)
    comment '用户表';

#添加个人简介字段
alter table user
    add profile varchar(512) null;

#修改 user 表主键id
alter table user
    modify id bigint auto_increment;

alter table user
    auto_increment = 1;
