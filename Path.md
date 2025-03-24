# PathTransferor PathExec 自定义脚本 语法声明

## 语法规则

1. 每条指令以`;`结尾
2. 每条指令由 `主指令` `参数` `条件` 构成, 如果其中有省略的部分应该使用对应的省略符号替代, 使用`<Space>`符号分割
3. 默认为同步指令 (类似JS加`await`), 如果需要异步需要在主指令前需要加`$`符号

## 主指令

### 基本规范

每条指令必须大写 必须是每一句指令的开头

### 主指令列表

- `WALK` 行走 必须参数 `key()`
- `ROTATE` 必须参数 `rotation()` 让玩家旋转
- `TURN` 必须参数 `rotation()` 让玩家拐弯
- `JUMP` 确保玩家在地面上后运行 `mc.thePlayer.jump()`

## 参数

### 基本规范

每个参数由参数名加`()`组成
例如 `key(W)`

括号内部若要传递参数的 使用`,`点开 不得添加多余的空格等无用字符

### 参数列表

- `key()` `(): W | A | S | D` 适用于 `WALK` 主命令 用于控制按下键盘的按键
- `rotation()` `(): (pitch: number, yaw: number)` 适用于 `ROTATE | TURN`用于控制具体方向

## 条件

### 基本规范

每个参数由参数名加`[]`组成
例如 `Delay[1000]`

### 条件列表

- `Delay[]` `[]: number` 延迟 `xx` ms
- `For[]` `[]: number` 持续 `xx`ms
- `UntilAt[]` `[]: [x,y,z]: [number,number,number]` 到达某BlockPos后停 (这里支持y写any)