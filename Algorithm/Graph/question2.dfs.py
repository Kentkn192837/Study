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
def dfs(idx):
    current = graph[idx]
    print("current: ", idx, "graph: ", graph[idx], "visited: ", visited[idx])
    visited[idx] = True
    for next_idx in current:
        if visited[next_idx]:
            continue
        dfs(next_idx)

dfs(0)
print(visited)
if not all(visited):
    print("このグラフはつながっていません")
