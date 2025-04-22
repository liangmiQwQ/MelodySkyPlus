'''
将该代码提取成一个函数 做到输入 angleNeed和maxAngle以及speedTick返回result
不需要借助turtle 做到直接返回result 具体实现方法随意 你可以通过几何学方法计算(当然贼复杂不推荐) 你也可以通过粗略的模拟一下后来计算
总而言之就是不借助turtle实现给参数出答案 你可以使用解析几何 建立平面直角坐标系 或者直接计算
当然你也可以直接模拟turtle的运动 后续可能需要在其他编程语言下实现同样功能(后续问题 现在只需要实现基础功能)
'''

import math
from turtle import *  # type: ignore

speedTick = 20
angleNeed = 55
maxAngle = 50
a = 0
ycor1 = 0
ycor2 = 0

width(5)
speed(0)
left(90)
angle_sum = 0


def process():
    global angle_sum

    diff = abs(angleNeed - angle_sum)

    newAngle = min(diff / 2, maxAngle)
    angle_sum += newAngle

    if diff > 0.1:
        # 如果偏差较大，继续递归分解角度
        left(newAngle)
        forward(speedTick)
        process()
    else:
        # 如果偏差已足够小，最后修正一下角度
        left(diff)


process()

color('purple')
a = xcor()
setx(100)
ycor1 = ycor()

penup()

home()

pendown()
color('black')
left(90)

color('red')
left(angleNeed)

c = -1 * 1 / math.sin(math.radians(angleNeed)) * a
forward(c)

color('blue')
setx(100)

ycor2 = ycor()
right(angleNeed)
result = ycor1 - ycor2
print(result)

import math


def calculate_result(angleNeed, maxAngle, speedTick):
    # 模拟紫色路径的process过程
    angle_sum = 0.0
    current_direction = 90.0  # 初始方向向上
    x, y = 0.0, 0.0

    while True:
        diff = angleNeed - angle_sum
        if abs(diff) <= 0.1:
            # 修正剩余角度，不移动
            angle_sum += diff
            current_direction += diff
            break
        else:
            if diff > 0:
                new_angle = min(diff / 2, maxAngle)
            else:
                new_angle = -min(abs(diff) / 2, maxAngle)

            angle_sum += new_angle
            current_direction += new_angle
            # 计算移动方向
            radian = math.radians(current_direction)
            dx = speedTick * math.cos(radian)
            dy = speedTick * math.sin(radian)
            x += dx
            y += dy

    a = x  # 紫色路径结束时的x坐标

    # 计算红色路径的y坐标
    theta_red = 90 + angleNeed
    radian_red = math.radians(theta_red)
    sin_angleNeed = math.sin(math.radians(angleNeed))

    if sin_angleNeed == 0:
        return 0  # 避免除以零错误

    c = -a / sin_angleNeed
    y_red = c * math.sin(radian_red)

    result = y - y_red
    return result


print(calculate_result(angleNeed, maxAngle, speedTick))

done();
