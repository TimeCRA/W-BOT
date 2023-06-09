## W-BOT

### 简介

W是一款明日方舟主题的聊天机器人，用于QQ平台群聊。

主要功能有模拟寻访、公开招募识别、游戏王查卡、AI聊天等等。

通信采用go-cqhttp，Java进行逻辑处理，数据库MySQL+Redis。

（文档刚刚编写，比较简陋，更多内容后续会添加）

### 立即上手

1. 根据系统下载[go-cqhttp](https://github.com/Mrs4s/go-cqhttp)，根据提示填写 QQ 号和密码等信息，参考文档 https://docs.go-cqhttp.org/guide/quick_start.html。

2. 将go-cqhttp的配置文件中的上报方式修改为Array。

3. git-clone 基础项目 [Erekilu/bot-base (github.com)](https://github.com/Erekilu/bot-base)。

4. 将刚刚的项目maven-install到本地。

5. git-clone [Erekilu/W-BOT (github.com)](https://github.com/Erekilu/W-BOT)。

6. 下载安装redis，mysql（linux上装方便），连接mysql执行dump.sql。

7. 将static-resource解压到本地。linux："/qqbot_resource/" win："E:/qqbot_resource/"

8. 修改W-BOT项目中，application.dev中的配置

   ```
   jasypt.encryptor.password: 加密的扰乱码。如果不需要加密，不填
   
   下面这些项，如果你需要加密，则在Test/EncryptTest中运行出加密结果，然后在配置文件中改写成ENC(结果)
   如果不加密，就直接写就行
   oss-path: 本机公网IP+该项目的端口（如果是本地部署没有公网IP，填写你服务器的IP，如：www.xxxyyy.com:8088/）
   bot.websocketUrl: 同go-cqhttp配置文件 ws正向服务器监听地址
   bot.accessToken: 同go-cqhttp配置文件 access-token
   datasource.url: 数据库访问链接（如：jdbc:mysql://123.123.12.12:3306/dbname?useUnicode=true&characterEncoding=utf-8&serverTimeZone=GMT）
   datasource.username: 数据库用户名（如：root）
   datasource.password: 数据库密码（如：123456）
   redis.host: redis连接（如：123.123.12.12）
   redis.password: redis密码（如：123456）
   ```

9. 进入数据库，修改tbl_system_config的内容

   ```
   name：机器人名字、昵称等等
   master_qq：数组，机器人管理员的qq号，将你的qq设置成管理员
   ```

10. 启动项目（java -jar xxxx.jar）

### 流程设计

待完善

### 操作指南

暂无前端界面，正在考虑开发...

#### 1. 新增预设回复

在tbl_key_info中添加一行，key通常是正则表达式，如"你好.*"，然后在tbl_value_info中添加多行，关联上tbl_key_info的主键，就能从中随机挑选一项回复。

可以不重启项目，直接输入"/refresh"，就能刷新缓存。

#### 2. 新增涉及逻辑处理的回复

例如：抽一张塔罗牌

你需要在tbl_key_info中添加一行，把special字段设置为1。然后在SpecialService.java中定义和实现接口，然后在GroupMessageServiceImpl.java中的regularKeyMatch方法中，接上你刚刚实现的方法，用id匹配。

```java
else if (valueDto.getId() == 315) {
	specialService.innerRefresh(group, member, valueDto, content);
}
```

#### 3. 拉群

在tbl_group_info中加一条记录。然后管理员输入指令/flush刷新群聊信息。

#### 4. 封群测试

运行GroupSwitch.java中的closeAll方法，关闭所有群聊。测试结束后，使用notifyGroup方法将群聊变成上一次的状态。

#### 5. 明日方舟新增干员

在tbl_agent_info中新增一条记录，注意参数：是否支持公招，是否为限定干员等等。

#### 6. 明日方舟更新寻访UP池

在tbl_system_config中，更改下列项：

```
recruit_activity_percent：UP率，活动池50，限定池70
activity_new_up_names：新活动干员。复刻活动不用填，限定时端不用填
activity_old_up_names：老活动干员。限定池不用填
normal_six_up_names：标准寻访6星干员
normal_five_up_names：标准寻访5星干员
limitive_six_up_names：限定池6星干员。非限定时端不用填
limitive_run_six_up_names：限定池6星陪跑干员。非限定时端不用填
limitive_weight_six_up_names：限定池5被权重干员。非限定时端不用填
limitive_five_up_names：限定池5星干员。非限定时端不用填
```

#### 7. GPT配置KEY

在tbl_system_config中，ai_chat_config为白名单群聊，例如你的测试群。

ai_chat_keys为JSONObject，键为群号，值为key数组。还有一个default键，白名单群聊如果没有配置key，直接用default中的key。

ai_chat_free_config是GPT试用版的设置。

待完善

### 反馈与交流

欢迎加入QQ群：282231556来讨论和交流W的相关问题。