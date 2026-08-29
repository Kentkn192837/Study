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
dy = [-1, 0, 1, 0]
dx = [0, 1, 0, -1]
H, W = len(matrix), len(matrix[0])

while que:
    current_y, current_x, d = que.popleft()
    if (current_y, current_x) in visited:
        continue
    visited.add((current_y, current_x))
    for i in range(4):
        next_y = current_y + dy[i]
        next_x = current_x + dx[i]
        if next_y < 0 or next_y >= H:
            continue
        if next_x < 0 or next_x >= W:
            continue
        if matrix[next_y][next_x] == 1:
            continue
        if (next_y, next_x) in visited:
            continue
        que.append((next_y, next_x, d + 1))
