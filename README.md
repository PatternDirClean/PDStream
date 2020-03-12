<p align="center">
<img src="https://images.gitee.com/uploads/images/2018/0908/101534_53a66adb_2071767.png" width="210" height="210" alt="Icon"/>
</p>

# PDStrem

![java library](https://img.shields.io/badge/type-Libary-gr.svg "type")
![JDK 13](https://img.shields.io/badge/JDK-13+-green.svg "SDK")
![Apache 2](https://img.shields.io/badge/license-Apache%202-blue.svg "License")

-- [Java Doc](https://apidoc.gitee.com/PatternDirClean/PDStream) --

-------------------------------------------------------------------------------

## 简介

一个用于读写流的基础库，采用无异常读写方式。

后续将会跟进我的其他项目进一步完善

### 数据对照

不同状态返回的数值在 `OPC` 对象中已定义，可在项目初始化的时候修改

#### 字符读取

|状态|对应值|默认
|---|---|---|
|无流可读取 / 指定长度为 0|`CHAR_DEFAULT_DATA`|""|
|发生异常 / 无数据可读取|`CHAR_EMPTY_DATA`|null|

#### 字节读取

|状态|对应值|默认
|---|---|---|
|无流可读取 / 指定长度为 0|`BYTE_DEFAULT_DATA`|[]|
|发生异常 / 无数据可读取|`BYTE_EMPTY_DATA`|null|

#### 写入

|状态|对应值
|---|---|
|无流可写入 / 无数据可写入|`true`|
|发生异常|`false`|

## 使用方法
请导入其 `jar` 文件,文件在 **发行版** 或项目的 **jar** 文件夹下可以找到
>发行版中可以看到全部版本<br/>项目下的 jar 文件夹是当前最新的每夜版

要导入其依赖库,在项目的 **lib** 文件夹下可以找到

依赖的同系列的项目
- [PDConcurrent](../PDConcurrent)

可通过 **WIKI** / **主页** 或者 **测试类** 查看示例

## 分支说明
dev-master：当前的开发分支，可以拿到最新的每夜版 jar

releases：当前发布分支，稳定版的源码

-------------------------------------------------------------------------------

### 提供bug反馈或建议

- [码云Gitee](https://gitee.com/PatternDirClean/PDStream/issues)
- [Github](https://github.com/PatternDirClean/PDStream/issues)