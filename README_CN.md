# Jetbrains Code Remark

> 一款Jetbrains系IDE代码备注插件，不同于代码注释，它可被应用于只读的源码文件，让你在阅读源码时如虎添翼。

[English](./README.md) | [报告问题](https://github.com/wenzewoo/jetbrains-code-remark/issues)

![](./screenshots/example.png)

# 如何安装

在IDE插件市场搜索 `Code Remark` 或通过[releases](https://github.com/wenzewoo/jetbrains-code-remark/releases)下载安装包进行手动安装。

# 使用说明

源码是只读文件，如何添加备注？使用该插件让你在阅读框架源码时如虎添翼。

- 将源码文件使用IDEA打开。

- 在任何需要的地方，以下三种方式任选其一，触发Popup弹出层，添加内容。

  - `Alt` + `ENTER` ->  `[MARK] Add/Edit remark`
  - `EditorPopupMenu` / `Tools` ->  `Add remark`
  - `shift + R` [推荐]

- 在编辑器中使用右键菜单 `Remark Navigation` 预览当前文件添加的所有备注信息。

- 在`View` -> `Tool Windows` -> `Favorites` -> `Remarks` 视图中查看当前项目所有的备注信息。

# 快捷键

- 添加/编辑：<`shift` + `R`>
- 导航视图（当前文件）: <`shift` + `N`>
- 保存：<`shift` + `ENTER`>
- 删除：<`shift` + `DETELE`>
- 取消：<`ESC`>

> 可以通过 `Preferences` - `Keymap` 搜索 `Code Remark` ，为指定的操作配置相应的快捷键