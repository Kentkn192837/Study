graph = [
    [(1, 6), (2, 8), (3, 2)],
    [(4, 4)],
    [(3, 5)],
    [(1, 3), (4, 10), (5, 3), (6, 5)],
    [],
    [(2, 1)],
    [(4, 2), (5, 7)],
]

undecided = set([i for i in range(len(graph))])
distance = [float("inf") for _ in range(len(graph))]
distance[0] = 0
while len(undecided) > 0:
    target_node = 0
    undecided.remove(target_node)
    for current in graph[target_node]:
        next_node_idx, d = current
        print(next_node_idx, d)
        if distance[next_node_idx] > distance[target_node] + d:
            distance[next_node_idx] = distance[target_node] + d
    break
print(undecided)
print(distance)
