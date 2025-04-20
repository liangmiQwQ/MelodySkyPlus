from turtle import * # type: ignore

speedTick = 2;
angleNeed = 50;

forward(200)

def process(angle):
  forward(speedTick)
  left(angle / 2)
  
  if(angle /2 > 1):
    process(angle / 2)

  
process(angleNeed)

home()

forward(200)

color('red')
left(angleNeed)

forward(50)

right(angleNeed)

color("blue")

forward(200)

