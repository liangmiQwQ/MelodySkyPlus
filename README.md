# MelodySky Plus

给MelodySky添加failsafe以及更多好用的功能 让Admin感到人间美好

你需要运行以下命令以安装melody依赖 否则会无法运行

```bash
cd ./melody
mvn install:install-file -Dfile=melodysky.jar -DgroupId=xyz.melody -DartifactId=melodySky -Dversion=1.0.0 -Dpackaging=jar
```

不使用`files()`的原因是因为`files()`无法兼容`exclude`导致mixin失败 