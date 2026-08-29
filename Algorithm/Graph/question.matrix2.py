from collections import deque

matrix = [
    [1, 1, 1, 1, 1, 0, 1],
    [1, 1, 0, 0, 1, 0, 0],
    [1, 0, 1, 0, 0, 1, 0],
    [1, 0, 1, 0, 1, 0, 1],
    [1, 1, 0, 1, 1, 1, 1],
]

H, W = len(matrix), len(matrix[0])
N = H * W
visited = [False for _ in range(N)]

for i in range(N):
    que = deque([i])
    while que:
        current = que.popleft()
        if visited[current]:
            continue
