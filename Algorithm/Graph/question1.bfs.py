graph = [
    [2, 4, 5], # Aliceと友好関係あり
    [3, 6, 7], # Benと友好関係あり
    [0, 4],    # Bobと友好関係あり
    [1],       # Chrisと友好関係あり
    [0, 2],    # Layと友好関係あり
    [0],       # Paulと友好関係あり
    [1],       # Shellyと友好関係あり
    [1],       # Steveと友好関係あり
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
