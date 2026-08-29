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
dy = [-1, 0, 1, 0]
dx = [0, 1, 0, -1]
ans = []

for i in range(N):
    current_y, current_x = i // W, i % W
    if visited[i] or matrix[current_y][current_x] == 1:
        continue
    # セルが0で、かつ未訪問の場合、BFSを開始する
    connected_graph_size = 0

    que = deque([i])
    while que:
        current = que.popleft()
        if visited[current]:
            continue
        visited[current] = True
        connected_graph_size += 1
        current_y, current_x = current // W, current % W

        for j in range(4):
            next_y, next_x = current_y + dy[j], current_x + dx[j]
            next_idx = next_y * W + next_x
            if next_y < 0 or next_y >= H:
                continue
            if next_x < 0 or next_x >= W:
                continue
            if matrix[next_y][next_x] == 1:
                continue
            if visited[next_idx]:
                continue
            # 探索したセルが範囲内でかつ未探索の場合、次の探索対象としてキューに追加する
            que.append(next_idx)
        ans.append(connected_graph_size)
print(ans)
