# GeoWebCache (GWC) SpringBoot 集成版 使用手册
## 概述
本项目是基于 Java 8 开发的 GeoWebCache (GWC) 1.20 版本 SpringBoot 集成版，将传统 GWC 的 XML 配置迁移至 SpringBoot 配置体系，简化部署与拓展流程，内置数据服务管理页面、代码生成器等便捷功能。
## 演示地址
 暂无演示地址
## 环境准备
### 基础依赖
- JDK：1.8（必须，适配 GWC 1.20 版本）
- 数据库：PostgreSQL + PostGIS 插件（需提前安装 PostGIS 扩展）
- Redis（可选）：单节点部署时可移除 Redis 相关依赖，集群部署建议保留
- Maven：3.6+（用于编译打包）

### 数据库初始化
1. 安装 PostgreSQL 后，执行以下命令安装 PostGIS 插件：
   ```sql
   -- 切换至目标数据库
   \c your_database_name;
   -- 安装 PostGIS 核心插件
   CREATE EXTENSION postgis;
   -- 验证安装
   SELECT postgis_version();
   ```
2. 无需手动创建业务表，项目启动时会自动初始化基础表结构。

## 配置说明
### 核心配置（application-local.yml）
#### 1. 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database_name?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: your_db_user
    password: your_db_password
    driver-class-name: org.postgresql.Driver
```

#### 2. Redis 配置（可选）
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_redis_password # 无密码则留空
    database: 0
```
> 单节点部署移除 Redis 步骤：
> 1. 全局搜索代码中 `redisTemplate` 关键字，删除所有相关注入、调用代码；
> 2. 移除 pom.xml 中 Redis 相关依赖（如 `spring-boot-starter-data-redis`）。

#### 3. GWC 核心配置
```yaml
geoair:
  gwc:
    cache:
      gwc-cache-dir:  # 原 GEOWEBCACHE_DIR，初次启动置空，项目自动初始化目录与配置文件
      gtc-cache-dir:  # 自定义缓存目录，按需配置
      admin-password: geoair  # GWC 首页登录密码
      admin-user-name: geoair # GWC 首页登录用户名
```

#### 4. 接口文档（Swagger）配置
```yaml
geoair:
  apidoc:
    enable: false  # false 关闭 Swagger，true 开启（开发环境建议开启）
```

#### 5. 数据服务（ds-server）配置
```yaml
geoair:
  dynamic:
    db:
      service:
        default-password: geoair  # ds-server 页面登录默认密码
        default-user: geoair     # ds-server 页面登录默认用户名
```

#### 6. MinIO 配置（无需关注）
项目中包含 MinIO 相关配置代码，但未接入核心功能，无需配置，不影响 GWC 正常运行（为模板工程残留，可按需删除）。

## 启动步骤
1. 完善 `gwc-boot/gwc-wcs/src/main/resources/application-local.yml` 中的数据库、Redis（可选）配置；
2. 启动 SpringBoot 主类（推荐 IDE 直接运行 `GwcBootApplication`）；
3. 验证启动成功：
    - 访问 GWC 首页：`http://localhost:26888/geowebcache`（默认端口 26888，可在 application.yml 中修改 `server.port`）；
    - 访问 ds-server 管理页面：`http://localhost:26888/geowebcache/dsView/index.html`。

## 功能使用
### 1. 登录验证
- GWC 首页登录：使用 `geoair.gwc.cache.admin-user-name` 和 `geoair.gwc.cache.admin-password` 配置的账号密码；
- ds-server 页面登录：使用 `geoair.dynamic.db.service.default-user` 和 `geoair.dynamic.db.service.default-password` 配置的账号密码。

### 2. 代码拓展（gwc-code-gen 模块）
项目内置代码生成器模块 `gwc-code-gen`，可快速生成业务代码，使用方式：
1. 运行代码生成器主类；
2. 参照 `gwc-wcs` 模块中的样例代码，适配生成的代码结构；
3. 生成的代码可直接集成至现有模块，无需手动配置基础依赖。

## 注意事项
1. 初次启动时 `gwc-cache-dir` 必须置空，否则可能导致配置文件初始化失败；
2. 生产环境建议关闭 Swagger（`geoair.apidoc.enable=false`），避免接口信息泄露；
3. 若需修改端口，在 `application.yml` 中配置 `server.port: 自定义端口`；
4. Redis 移除后需确保代码中无残留的 `redisTemplate` 调用，否则启动会报空指针异常；
5. gwowebcache.xml 已完全迁移至 SpringBoot 配置，无需手动修改该 XML 文件。

## 常见问题
1. 启动报错“找不到 PostGIS 插件”：确认数据库已安装 PostGIS，且连接的数据库执行过 `CREATE EXTENSION postgis;`；
2. Redis 移除后启动失败：检查是否遗漏 `redisTemplate` 相关代码，或未移除 Redis 依赖；
3. 访问 ds-server 页面 404：确认项目启动成功，且访问路径为 `http://localhost:端口/geowebcache/dsView/index.html`。

### 总结
1. 核心配置聚焦 `application-local.yml`，重点完善数据库、GWC 账号密码配置，Redis 可按需移除；
2. 初次启动需置空 `gwc-cache-dir`，项目自动初始化 GWC 核心目录与配置文件；
3. 拓展开发可复用 `gwc-code-gen` 模块生成代码，参照 `gwc-wcs` 样例快速集成。
