# [English](English.md) [中文](README.md)

<div align="center">
<img width="125" height="125" src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="legado"/>
<br>
阅读NG — Next Generation Legado
<br>
基于阅读Sigma继续演进的独立阅读器分支。
</div>

## 项目说明

阅读NG 只提供阅读器、规则引擎和相关管理工具，不提供任何内容。用户需要自行导入书源、订阅源或本地文件，并自行确认数据来源的合法性。

阅读NG 当前不再计划继续从旧上游合并代码，而是在阅读Sigma基础上独立演进。感谢 Legado 和阅读Sigma 打下的基础。

## 与其他版本共存

阅读NG 使用独立包名前缀 `io.legado.app.ng`，可与阅读原版、阅读Sigma同时安装，数据相互独立。

- 对外分发版：`io.legado.app.ng.release`
- 调试版：`io.legado.app.ng.debug`

## 主要功能

- 自定义书源和规则解析，支持搜索、发现、详情、目录和正文规则。
- 书架支持列表、网格、分组、排序和阅读记录。
- 支持本地 TXT、EPUB 阅读，支持本地扫描和手动导入。
- 支持 RSS、替换净化、文本目录规则、在线朗读引擎、主题和阅读排版导入。
- 阅读界面支持字体、颜色、背景、行距、段距、加粗、简繁转换和多种翻页模式。
- 支持 WebDAV 备份恢复、内容替换净化、书源调试和内置帮助文档。

## NG 改造

相比阅读Sigma，阅读NG当前主要增加和调整了以下内容：

### UI 改造

- 全局视觉改造，支持透明顶栏，并新增 3 种主题。
- 搜索栏增加进度条显示。
- 净化规则支持分组视图和作用域视图。

### 书源、调试功能优化

- 改造书源管理视图，新增多视角视图，并增加类型显示。
- 优化书源调试页面，支持可视化步骤显示和更详细的日志展示。
- 支持网络日志抓取
- 支持一键清空书源分组，支持基于书源类型和内容自动分组。
- 支持书源代码高亮显示。

### AI 相关

- 内置 MCP 服务（更多接口待完善）。
- 支持多种 AI 提供商接入（更多提供商待完善）。
- 支持 AI 段落净化（生成净化规则，错别字替换、异常字符删除）。
- 支持 AI 章节净化（生成净化规则，错别字替换、异常字符删除、广告删除）。

## 链接

- [Releases](https://github.com/joestar817/legado_NG/releases)
- [更新日志](app/src/main/assets/updateLog.md)
- [帮助文档](app/src/main/assets/web/help/md/appHelp.md)
- [书源规则教程](https://mgz0227.github.io/The-tutorial-of-Legado/)

## 致谢

- [gedoor/legado](https://github.com/gedoor/legado) - Legado 原项目。
- [Luoyacheng/legado](https://github.com/Luoyacheng/legado) - 阅读Sigma，Reading NG 的直接基础。
- [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub) - AI Provider 管理与模型配置设计参考。
- 感谢本项目使用的所有开源依赖。
