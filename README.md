```
网上找到学习微服务的项目
```

> 用户认证的时候会用到

xc_user表对应的数据
```
数据库中加密的密码都是111111
username itcast 
password 111111
```

> 分布式事务测试数据
```sql
INSERT INTO `xc_task_his` ( 
						`id`, `create_time`, 
						`update_time`, 
						`delete_time`, 
						`task_type`, 
						`mq_exchange`, 
						`mq_routingkey`, 
						`request_body`, 
						`version`, 
						`status`, 
						`errormsg` 
				)
VALUES	( 
						'10', 
						'2018-04-04 22:58:20', 
						'2018-04-04 22:58:54', 
						'2018-04-04 22:58:55', 
						NULL, 
						ex_learning_addchoosecourse, 
						addchoosecourse, 
						'{"userId":"49","courseId":"4028e581617f945f01617f9dabc40000"}',
						1,
						NULL,
						NULL 
				);
```