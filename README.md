# 1. grpc简介
gRPC  是google提供的一个高性能、开源和通用的 RPC 框架，在gRPC里客户端应用可以像调用本地对象一样直接调用另一台不同的机器上服务端应用的方法，使得您能够更容易地创建分布式应用和服务

云原生兴起，云端服务架构不同语言的集成能力也越来越标准化和简单化，不同组件间的通讯也需要统一和标准化，而这个标准备的通讯协议不二人选就是grpc了．

使用grpc有几点优势：
* 更快的传输速度，底层序列化为二进制进行传输
* 基于IDL的统一的接口定义语言
* 跨平台多语言，支持C, C++, Python, PHP, Nodejs, C#, Objective-C、Golang、Java，并能够基于语言自动生成客户端和服务端功能库
* 基于HTTP 2.0标准设计，gRPC基于HTTP 2.0标准设计，带来了更多强大功能，如多路复用、二进制帧、头部压缩、推送机制


通过教程中例子，你可以学会以下技能：
* 如何定义服务接口
* 如何生成服务器和客户端代码。
* 如何集成spring boot 实现一个简单的客户端和服务器

> 官网rpc基础示例https://grpc.io/docs/languages/java/basics/

### 2. 需求
实现对数据库中用户信息的新增和查询服务


### 3. 准备名为grpc-demo的普通maven项目
pom中集成好spring boot和mybatis plus相关依赖
```
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>com.hushow</groupId>
	<artifactId>grpc-demo</artifactId>
	<packaging>jar</packaging>
	<version>0.0.1-SNAPSHOT</version>
	
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.2.6.RELEASE</version>
		<relativePath />
	</parent>
	
	<properties>
		<maven-jar-plugin.version>3.1.1</maven-jar-plugin.version>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<grpc.version>1.31.1</grpc.version><!-- CURRENT_GRPC_VERSION -->
		<protobuf.version>3.12.0</protobuf.version>
		<protoc.version>3.12.0</protoc.version>
		<!-- required for jdk9 -->
		<maven.compiler.source>1.7</maven.compiler.source>
		<maven.compiler.target>1.7</maven.compiler.target>
		<mybatis-plus-boot-starter.version>3.3.0</mybatis-plus-boot-starter.version>
		<mysql-connector.version>8.0.19</mysql-connector.version>
		<druid-spring-boot-starter.version>1.1.10</druid-spring-boot-starter.version>
		<alibaba-fastjson.version>1.2.58</alibaba-fastjson.version>
	</properties>

	<dependencies>

		<!-- mybatis-plus -->
		<dependency>
			<groupId>com.baomidou</groupId>
			<artifactId>mybatis-plus-boot-starter</artifactId>
			<version>${mybatis-plus-boot-starter.version}</version>
		</dependency>

		<!-- mysql 连接 -->
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<scope>runtime</scope>
		</dependency>
		
		<!-- druid -->
		<dependency>
			<groupId>com.alibaba</groupId>
			<artifactId>druid-spring-boot-starter</artifactId>
			<version>${druid-spring-boot-starter.version}</version>
		</dependency>

		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-api</artifactId>
		</dependency>
		
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
		</dependency>

		<dependency>
			<groupId>io.swagger</groupId>
			<artifactId>swagger-annotations</artifactId>
			<version>1.5.20</version>
		</dependency>

		<dependency>
			<groupId>com.alibaba</groupId>
			<artifactId>fastjson</artifactId>
			<version>${alibaba-fastjson.version}</version>
		</dependency>
		
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
		</dependency>
	</dependencies>
	
</project>
```

### 4. 如何定义服务接口
使用ProtoBuf语法进行IDL定义，在项目src/main/proto目录下创建以下两文件

![file](http://www.hushowly.com/buploads/2020/08/image-1598433338076.png)

* 创建user_service.proto定义服务

```
//声明protobuf版本为proto3
syntax = "proto3";

//引入实体定义
import "user_dto.proto";

//如果为true时message会生成多个类
option java_multiple_files = true;

//此处要注意和user_dto.proto相同包，否则找不到实体
package com.hushow.demo.grpc.user.dto;

//服务定义生成的包
option java_package = "com.hushow.demo.grpc.user.service";

//指定生成Java的类名，如果没有该字段则根据proto文件名称以驼峰的形式生成类名
option java_outer_classname = "UserProto";

//服务定义
service User {

  //查询用户列表，以流式返回多个对象
  rpc list (SearchUserRequest) returns (stream UserResponse) {}
  
  //添加用户信息，返回单个对象
  rpc add (AddUserRequest) returns (UserResponse) {}
}
```

* 创建user_dto.proto定义消息实体

```
//声明protobuf版本为proto3
syntax = "proto3";

//消息实体生成包路径
package com.hushow.demo.grpc.user.dto;

//如果为true时message会生成多个类
option java_multiple_files = true;

//查询列表请求实体
message SearchUserRequest {
  int32 id = 1;
  string name = 2;
  int32 age =3;
  string address = 4;
}

//返回用户信息实体
message UserResponse {
  int32 id = 1;
  string name = 2;
  int32 age =3;
  string address = 4;
}

//添加用户实体
message AddUserRequest {
  string name = 1;
  int32 age =2;
  string address = 3;
}

```

### 5. 如何生成服务器和客户端代码

#### 5.1 集成grpc自动代码生成相关依赖和插件
```
<!-- grpc自动代码生成maven插件配置 -->
<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>io.grpc</groupId>
			<artifactId>grpc-bom</artifactId>
			<version>${grpc.version}</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>

<build>
	<!-- os系统信息插件, protobuf-maven-plugin需要获取系统信息下载相应的protobuf程序 -->
	<extensions>
		<extension>
			<groupId>kr.motd.maven</groupId>
			<artifactId>os-maven-plugin</artifactId>
			<version>1.6.2</version>
		</extension>
	</extensions>
	<plugins>
		<!-- grpc代码生成插件 -->
		<plugin>
			<groupId>org.xolstice.maven.plugins</groupId>
			<artifactId>protobuf-maven-plugin</artifactId>
			<version>0.6.1</version>
			<configuration>
				<protocArtifact>com.google.protobuf:protoc:${protoc.version}:exe:${os.detected.classifier}</protocArtifact>
				<pluginId>grpc-java</pluginId>
				<pluginArtifact>io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}</pluginArtifact>
			</configuration>
			<executions>
				<execution>
					<goals>
						<goal>compile</goal>
						<goal>compile-custom</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
		<!-- jar版本冲突检测插件 -->
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-enforcer-plugin</artifactId>
			<version>1.4.1</version>
			<executions>
				<execution>
					<id>enforce</id>
					<goals>
						<goal>enforce</goal>
					</goals>
					<configuration>
						<rules>
							<requireUpperBoundDeps />
						</rules>
					</configuration>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```

#### 5.2 在项目下执行mvn compile 自动生成以下代码

![file](http://www.hushowly.com/buploads/2020/08/image-1598496637006.png)

> 生成原理：其实主要是通过protobuf-maven-plugin插件生成实体和服务接口到target目录下，最后自动关联到eclipse的classpath中．


>  在实际项目中，建议将生成的代码放到一个共用的api项目中，以便server和client端引用．

> 暂且不管生成代码具体内容，反正是一堆接口及实体模型的定义．


### 6. 如何集成spring boot 实现一个简单的客户端和服务器

#### 6.1 为server和client端集成以下依赖
> 为了简化，本示例将server和client集成到同一项目

> 详细用法参考grpc-spring-boot-starter框架文档地址
> https://yidongnan.github.io/grpc-spring-boot-starter/zh-CN/

```
<!-- grpc server和spring-boot集成框架 -->
<dependency>
	<groupId>net.devh</groupId>
	<artifactId>grpc-server-spring-boot-starter</artifactId>
	<version>2.10.1.RELEASE</version>
	<exclusions>
		<exclusion>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</exclusion>
	</exclusions>
</dependency>

<!-- grpc client和spring-boot集成框架 -->
<dependency>
	<groupId>net.devh</groupId>
	<artifactId>grpc-client-spring-boot-starter</artifactId>
	<version>2.10.1.RELEASE</version>
	<exclusions>
		<exclusion>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</exclusion>
	</exclusions>
</dependency>
```

#### 6.2 如何为server端实现rpc接口

要点：
* 实现上述自动生成代码UserGrpc.UserImplBase接口
* 使用@GrpcService注解，自动注册rpc服务实现
* application.yml配置rpc端口：grpc.server.port:8080

```
/**
 * 用户管理grpc服务
 * @author   hushow
 * @date     2020年8月27日 上午9:51:26
 */
@GrpcService
@Slf4j
public class UserServiceGrpcImpl extends UserGrpc.UserImplBase {

    @Resource
    UserDemoMapper userDemoMapper;

    /**
     * 添加用户信息rpc接口
     * @param request rpc接口请求参数
     * @param responseObserver rpc流式响应
     */
    @Override
    public void add(AddUserRequest request, StreamObserver<UserResponse> responseObserver) {

        log.info("start add");

        UserDemo ud = new UserDemo();
        ud.setName(request.getName());
        ud.setAge(request.getAge());
        ud.setAddress(request.getAddress());
        userDemoMapper.insert(ud);
        
        //构造rpc响应参数
        UserResponse reply = UserResponse.newBuilder().setId(ud.getId()).setName(request.getName()).setAge(ud.getAge())
            .setAddress(ud.getAddress()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();

        log.info("end add");
    }
    
    /**
     * 查询用户列表
     * @param request rpc接口请求参数
     * @param responseObserver rpc流式响应，返回多个消息
     */
    @Override
    public void list(SearchUserRequest request, StreamObserver<UserResponse> responseObserver) {

        LambdaQueryWrapper<UserDemo> lq = new LambdaQueryWrapper<UserDemo>();
        if(StringUtils.hasText(request.getName())) {
            lq.eq(UserDemo::getName, request.getName());
        }
        List<UserDemo> list = userDemoMapper.selectList(lq);
        list.stream().forEach(c -> {
            UserResponse ur = UserResponse.newBuilder().setAddress(c.getAddress()).setAge(c.getAge()).setId(c.getId())
                .setName(c.getName()).build();
            responseObserver.onNext(ur);
        });
        responseObserver.onCompleted();
    }
}
```
启动spring boot项目，效果如下：
![file](http://www.hushowly.com/buploads/2020/08/image-1598494303166.png)

至此，grpc服务端完成启动并监听于8080端口，底层使用netty通讯端口替换了spring boot web容器端口。


#### 6.3 如何为client端实现调用grpc
由于方便测试，本示例使用junit方式展示client的调用
> 如何不熟悉spring boot junit测试参考：http://www.hushowly.com/articles/1106

**要点说明**：
* `client项目application.yml配置`： 

```
grpc:
   client:
      userDemoClient:
         #禁用传输层安全(https://yidongnan.github.io/grpc-spring-boot-starter/zh-CN/client/security.html)
         negotiationType: PLAINTEXT
         #grpc服务地址配置(https://yidongnan.github.io/grpc-spring-boot-starter/zh-CN/client/configuration.html#configuration-via-properties)
         address: static://localhost:8080
```
* `使用@GrpcClient("userDemoClient")注解让框架自动注入UserGrpc.UserBlockingStub 调用类`

```
/**
 * grpc client调用测试用例
 * @author hushow
 * @date 2020年8月27日 上午10:27:26
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GrpcApplication.class)
@Slf4j
public class UserClientTest {
    //注入阻塞的stub
    @GrpcClient("userDemoClient")
    private UserGrpc.UserBlockingStub blockingStub;

    @Test
    public void addTest() {

        log.info("开始testAdd");
        AddUserRequest request = AddUserRequest.newBuilder().setAddress("长沙").setAge(28).setName("啊虎1").build();
        UserResponse response;
        try {
            response = blockingStub.add(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: " + e.getMessage(), e);
            throw e;
        }
        log.info("testAdd结果: " + response.getName());
    }

    @Test
    public void listTest() {

        log.info("开始listTest");
        SearchUserRequest request = SearchUserRequest.newBuilder().setName("虎").build();
        Iterator<UserResponse> response;
        try {
            response = blockingStub.list(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: " + e.getMessage(), e);
            throw e;
        }

        while (response.hasNext()) {
            log.info("listTest结果: " + response.next());
        }
    }
}
```