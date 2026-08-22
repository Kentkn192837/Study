graph = [
    [1, 2, 3],
    [0, 4, 5],
    [0, 5],
    [0, 6],
    [1],
    [1, 2, 7, 8],
    [3],
    [5],
    [5],
]


visited = [False for _ in range(len(graph))]
queue = [0]

while queue:
    current = queue.pop(0)
    print("current: ", current, "graph: ", graph[current], "visited: ", visited[current])
    if visited[current]:
        continue
    visited[current] = True
    for idx in graph[current]:
        if visited[idx]:
            continue
        queue.append(idx)

print(visited)
if not all(visited):
    print("このグラフはつながっていません")
