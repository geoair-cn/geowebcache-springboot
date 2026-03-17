# 天地图WMTS服务接口文档
## 一、概述
本接口集合为天地图WMTS（Web Map Tile Service）服务提供支持，涵盖组图层、单图层以及图层与图层组叠加的请求处理功能，以满足多样化的地图数据获取需求。

## 二、接口列表
### （一）组图层请求接口
- **接口路径**：
  - `/geoair/group/{layer}/wmts`
  - `/v2/{layer}/group/wmts`
- **功能描述**：处理组图层的WMTS请求，根据请求参数获取相应的地图瓦片或能力信息等。
- **参数说明**：
  - `{layer}`：必填，图层组名称，用于指定请求的目标图层组。
  - `request`（隐式参数，通过`HttpServletRequest`获取）：包含请求头、请求参数等信息，用于获取具体的请求细节，如请求类型（GetCapabilities、GetTile等）、瓦片编号等。
- **请求示例**：
```
http://yourserver/geoair/group/world/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=world&style=&Format=image/png&TileMatrixSet=EPSG:4326&TileMatrix=EPSG:4326:0&TileRow=0&TileCol=0
```
- **处理逻辑**：
  1. 根据传入的`{layer}`图层组名称，调用`layerGroupAliasService.searchLayerStringByLayerGroupAlias`方法查找对应的真实图层字符串。若未找到匹配的别名，则使用传入的图层组名称作为真实图层字符串。
  2. 通过`GISHubUtil.checkParameter`方法检查请求参数的有效性，获取包含请求参数信息的`Map`对象。
  3. 从参数`Map`中获取`request`参数值（如`GetCapabilities`、`GetTile`等），根据不同的请求类型，创建`GtcGroupWmtsService`实例并调用相应的服务处理方法（`doServiceGetCapabilities`或`doService`）。

### （二）单图层请求接口
- **接口路径**：
  - `/geoair/layer/{layer}/wmts/**`
  - `/v2/{layer}/layer/wmts/**`
- **功能描述**：处理单图层的WMTS请求，根据请求参数获取相应的地图瓦片或能力信息等。
- **参数说明**：
  - `{layer}`：必填，图层名称，用于指定请求的目标图层。
- **请求示例**：
```
http://yourserver/geoair/layer/country/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=country&style=&Format=image/png&TileMatrixSet=EPSG:4326&TileMatrix=EPSG:4326:0&TileRow=0&TileCol=0
```
- **处理逻辑**：
  1. 根据传入的`{layer}`图层名称，调用`layerGroupAliasService.searchLayerStringByLayerGroupAlias`方法查找对应的真实图层字符串。若未找到匹配的别名，则使用传入的图层名称作为真实图层字符串。
  2. 通过`GISHubUtil.checkParameter`方法检查请求参数的有效性，获取包含请求参数信息的`Map`对象。
  3. 从参数`Map`中获取`request`参数值（如`GetCapabilities`、`GetTile`等），根据不同的请求类型，创建`SingletonLayerService`实例并调用相应的服务处理方法（`doServiceGetCapabilities`或`doService`）。

### （三）图层图层组叠加接口
- **接口路径**：
  - `/geoair/layer_group/{layer}/wmts`
  - `/v2/{layer}/layer_group/wmts`
- **功能描述**：处理图层与图层组叠加的WMTS请求，根据请求参数获取相应的叠加地图瓦片或能力信息等。
- **参数说明**：
  - `{layer}`：必填，格式为`layer,图层名@group,图层组名`或`layer|图层名@group|图层组名`，用于指定请求的图层和图层组组合。
- **请求示例**：
```
http://yourserver/geoair/layer_group/layer,country@group,world/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=country&style=&Format=image/png&TileMatrixSet=EPSG:4326&TileMatrix=EPSG:4326:0&TileRow=0&TileCol=0
```
- **处理逻辑**：
  1. 根据传入的`{layer}`组合名称，调用`layerGroupAliasService.searchLayerStringByLayerGroupAlias`方法查找对应的真实图层字符串。若未找到匹配的别名，则使用传入的组合名称作为真实图层字符串。
  2. 通过`GISHubUtil.checkParameter`方法检查请求参数的有效性，获取包含请求参数信息的`Map`对象。
  3. 从参数`Map`中获取`request`参数值（如`GetCapabilities`、`GetTile`等），根据不同的请求类型，创建`MixtureGroupService`实例并调用相应的服务处理方法（`doServiceGetCapabilities`或`doService`）。

## 三、异常处理
本控制器具备全局异常处理机制，可有效应对多种异常情况：
- **OWSException**：返回HTTP状态码400，错误信息以XML格式输出，通过`ResponseUtils.writeErrorAsXML`方法实现。
- **IOException**：返回HTTP状态码400，错误信息以页面形式输出，通过`ResponseUtils.writeErrorPage`方法实现。
- **GeoWebCacheException**：返回HTTP状态码400，错误信息以页面形式输出，通过`ResponseUtils.writeErrorPage`方法实现，将异常信息传递给该方法。
- **其他异常**：记录错误日志，日志内容包含异常原因、请求URL和详细异常信息。返回HTTP状态码200，通过`TC_ResponseUtils.writeErrorAsXML`方法返回自定义错误信息，错误信息格式为`OWSException`的字符串表示形式。

## 四、注意事项
1. 所有接口均支持跨域请求，方便不同域的客户端进行访问。
2. 图层名称和图层组名称支持别名机制，系统会自动根据别名查找对应的真实名称，提高使用的灵活性和便捷性。
3. 对于WMTS请求，支持常见的操作类型，如`GetCapabilities`（用于获取服务能力描述文档）和`GetTile`（用于获取地图瓦片）等。
4. 接口响应状态码通常为200，表示请求已成功处理，具体的处理结果通过响应内容返回。客户端在接收到响应时，应根据响应内容进行后续处理。 
