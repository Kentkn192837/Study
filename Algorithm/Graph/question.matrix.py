from collections import deque

matrix = [
    [0, 1, 0, 1, 0, 0, 0],
    [0, 0, 0, 1, 0, 1, 0],
    [0, 1, 0, 0, 0, 1, 0],
    [0, 0, 1, 1, 1, 1, 0],
    [1, 0, 0, 0, 0, 0, 0],
]

queue = deque([(0, 0, 0)])
visited = set([])

while queue:

