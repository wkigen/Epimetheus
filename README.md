# Epimetheus

原理：
-------

根据阿里的《深入探索Android热修复技术原理》，实现的Android热修复

功能：
-------

Dalvik和art下的代码冷启动修复

Dalvik和art下的代码实时修复

实时修复只有在类结构没有改变的时候才能修复，补丁生成工具会自动判断是否能自动修复

补丁包生成： 
-------

[EpimetheusTool](https://github.com/wkigen/EpimetheusTool) 

自动对比新旧两个apk，无侵入，在属性、注解和指令上对比，尽可能找出差异，然后生成补丁包
