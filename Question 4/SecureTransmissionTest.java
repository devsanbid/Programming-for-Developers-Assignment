// A company's offices are connected by secure communication links, where:
// Each office is represented as a node.
// Each communication link is represented as an edge with a signal strength limit.
// Given a company network with 6 offices (nodes) and 5 communication links (edges), your task is to
// verify if a message can be securely transmitted from one office to another without exceeding the
// maximum signal strength.
// Graph Representation from the Image
// Offices (Nodes): {0, 1, 2, 3, 4, 5}
// Communication Links (Edges with Strengths):
// 0 ↔ 2 (Strength: 4)
// 2 ↔ 3 (Strength: 1)
// 2 ↔ 1 (Strength: 3)
// 4 ↔ 5 (Strength: 5)
// 3 ↔ 2 (Strength: 2)
// Class Implementation
// Implement the SecureTransmission class:
// SecureTransmission(int n, int[][] links)
// Initializes the system with n offices and a list of communication links.
// Each link is represented as [a, b, strength], indicating an undirected connection between office a and
// office b with a signal strength of strength.
// boolean canTransmit(int sender, int receiver, int maxStrength)
// Returns true if there exists a path between sender and receiver, where all links on the path have a
// strength strictly less than maxStrength.
// Otherwise, returns false.
// Example

// [figure ]

// Input:
// ["SecureTransmission", "canTransmit", "canTransmit", "canTransmit", "canTransmit"]
// [[6, [[0, 2, 4], [2, 3, 1], [2, 1, 3], [4, 5, 5]]], [2, 3, 2], [1, 3, 3], [2, 0, 3], [0, 5, 6]]
// Output: [null, true, false, true, false]
// Explanation
// SecureTransmission(6, [[0, 2, 4], [2, 3, 1], [2, 1, 3], [4, 5, 5]])
// Initializes a network with 6 offices and 4 communication links.
// canTransmit(2, 3, 2) → true
// A direct link 2 → 3 exists with strength 1, which is less than 2.
// canTransmit(1, 3, 3) → false
// 1 → 2 has strength 3, which is not strictly less than 3, so transmission fails.
// canTransmit(2, 0, 3) → true
// 2 → 3 → 0 is a valid path.
// Links (2 → 3) and (3 → 0) have strengths 1, 2 (both < 3), so successful transmission.
// canTransmit(0, 5, 6) → false
// There is no connection between 0 and 5, so transmission fails


// Solution
import java.util.*;

class SecureTransmission {
    private int n;
    private List<List<int[]>> graph;
    
    public SecureTransmission(int n, int[][] links) {
        this.n = n;
        this.graph = new ArrayList<>();
        
        // Initialize adjacency list
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        
        // Build the graph
        for (int[] link : links) {
            int a = link[0];
            int b = link[1];
            int strength = link[2];
            
            // Add bidirectional edges with their strengths
            graph.get(a).add(new int[]{b, strength});
            graph.get(b).add(new int[]{a, strength});
        }
    }
    
    public boolean canTransmit(int sender, int receiver, int maxStrength) {
        if (sender == receiver) {
            return true;
        }
        
        // Use BFS to find path with valid edge strengths
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();
        
        queue.offer(sender);
        visited[sender] = true;
        
        while (!queue.isEmpty()) {
            int current = queue.poll();
            
            // Check all neighbors
            for (int[] edge : graph.get(current)) {
                int neighbor = edge[0];
                int strength = edge[1];
                
                // Only use edges with strength strictly less than maxStrength
                if (strength < maxStrength && !visited[neighbor]) {
                    if (neighbor == receiver) {
                        return true;
                    }
                    
                    visited[neighbor] = true;
                    queue.offer(neighbor);
                }
            }
        }
        
        return false;
    }
}

// Test class to demonstrate the solution
public class SecureTransmissionTest {
    public static void main(String[] args) {
        // Test case from the problem
        int[][] links = {{0, 2, 4}, {2, 3, 1}, {2, 1, 3}, {4, 5, 5}};
        SecureTransmission st = new SecureTransmission(6, links);
        
        // Test cases
        System.out.println("canTransmit(2, 3, 2): " + st.canTransmit(2, 3, 2)); // Expected: true
        System.out.println("canTransmit(1, 3, 3): " + st.canTransmit(1, 3, 3)); // Expected: false
        System.out.println("canTransmit(2, 0, 3): " + st.canTransmit(2, 0, 3)); // Expected: true
        System.out.println("canTransmit(0, 5, 6): " + st.canTransmit(0, 5, 6)); // Expected: false
        
        // Additional test cases
        System.out.println("canTransmit(0, 1, 5): " + st.canTransmit(0, 1, 5)); // Should be true (0->2->1)
        System.out.println("canTransmit(4, 5, 5): " + st.canTransmit(4, 5, 5)); // Should be false (strength not < 5)
        System.out.println("canTransmit(4, 5, 6): " + st.canTransmit(4, 5, 6)); // Should be true (strength 5 < 6)
    }
}



// // Output
// canTransmit(2, 3, 2): true
// canTransmit(1, 3, 3): false
// canTransmit(2, 0, 3): false
// canTransmit(0, 5, 6): false
// canTransmit(0, 1, 5): true
// canTransmit(4, 5, 5): false
// canTransmit(4, 5, 6): true