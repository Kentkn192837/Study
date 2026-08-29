from collections import deque

matrix = [
    [0, 1, 0, 1, 0, 0, 0],
    [0, 0, 0, 1, 0, 1, 0],
    [0, 1, 0, 0, 0, 1, 0],
    [0, 0, 1, 1, 1, 1, 0],
    [1, 0, 0, 0, 0, 0, 0],
]

que = deque([(0, 0, 0)])
visited = set([])

while que:
    current_y, current_x, d = que.popleft()
    if (current_y, current_x) in visited:
        continue
    visited.add((current_y, current_x))
