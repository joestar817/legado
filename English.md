# [English](English.md) [中文](README.md)

<div align="center">
<img width="125" height="125" src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="legado"/>
<br>
Reading NG - Next Generation Legado
<br>
An independent reader fork evolved from Reading Sigma.
</div>

## Notice

Reading NG only provides a reader, rule engine, and management tools. It does not provide content. Users need to import their own book sources, RSS sources, or local files and are responsible for the legality of their content sources.

## Package Names

Reading NG uses the independent package prefix `io.legado.app.ng`.

- Beta: `io.legado.app.ng.release`
- Debug: `io.legado.app.ng.debug`

It can coexist with the original Legado and Reading Sigma. App data is isolated between versions.

## Features

- Custom book source rules for search, explore, book info, table of contents, and chapter content.
- Bookshelf list/grid views, grouping, sorting, and reading history.
- Local TXT and EPUB reading with scan and manual import.
- RSS, replacement rules, text TOC rules, online TTS engines, themes, and read config import.
- Highly customizable reading UI with fonts, colors, backgrounds, spacing, conversion, and page-turning modes.
- WebDAV backup/restore, replacement purification, book source debugging, and built-in help documents.

## NG Changes

Compared with Reading Sigma, Reading NG currently adds and adjusts the following areas:

### UI

1. Global visual updates, transparent top bars, and 3 built-in themes.
2. Search progress display in the search bar.
3. Replacement purification rules support group and scope views.

### Book Source And Debugging

1. Reworked book source management with multiple views and source type display.
2. Improved book source debugging with visualized steps and more detailed logs.
3. Runtime network log capture, enabled from Other Settings.
4. One-click source group clearing and automatic grouping by source type.
5. Syntax highlighting for book source code.

### AI

1. Built-in native MCP service (more APIs to be improved).
2. Multiple AI provider support (more providers to be improved).
3. AI paragraph purification (generates replacement rules for typo replacement and abnormal character removal).
4. AI chapter purification (generates replacement rules for typo replacement, abnormal character removal, and ad removal).

## Links

- [Releases](https://github.com/joestar817/legado/releases)
- [Update log](app/src/main/assets/updateLog.md)
- [Help document](app/src/main/assets/web/help/md/appHelp.md)

## Thanks

- [gedoor/legado](https://github.com/gedoor/legado) - original Legado project
- [Luoyacheng/legado](https://github.com/Luoyacheng/legado) - Reading Sigma, direct upstream of Reading NG
- [rikkahub/rikkahub](https://github.com/rikkahub/rikkahub) - AI provider management and model configuration reference
- All open source dependencies used by this project
