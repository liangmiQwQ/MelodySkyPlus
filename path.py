'''
将该代码提取成一个函数 做到输入 angleNeed和maxAngle以及speedTick返回result
不需要借助turtle 做到直接返回result 具体实现方法随意 你可以通过几何学方法计算(当然贼复杂不推荐) 你也可以通过粗略的模拟一下后来计算
总而言之就是不借助turtle实现给参数出答案 你可以使用解析几何 建立平面直角坐标系 或者直接计算
当然你也可以直接模拟turtle的运动 后续可能需要在其他编程语言下实现同样功能(后续问题 现在只需要实现基础功能)
'''

from turtle import *  # type: ignore

speedTick = 20
angleNeed = 90
maxAngle = 50
ycor1 = 0
ycor2 = 0

width(5)
left(90)
angle_sum = 0


def process(angle):
    global angle_sum
    
    newAngle = min(angle / 2, maxAngle)
  
    
    left(newAngle)
    forward(speedTick)

    angle_sum += newAngle

    diff = abs(angleNeed - angle_sum)
    if (diff > 0.1):
      process(newAngle)
    else:
      left(diff)


process(angleNeed)

color('purple')
setx(100)
ycor1 = ycor()

penup()

home()

pendown()
color('black')
left(90)

color('red')
left(angleNeed)

setx(100)

ycor2 = ycor()
right(angleNeed)
result = ycor1 - ycor2
print(result)
done();
