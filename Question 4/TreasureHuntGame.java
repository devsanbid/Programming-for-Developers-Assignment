// You are provided with the chemical formula, return maximum number of atoms represented in
// chemical formula.
// A Treasure Hunt Game is played on an undirected graph by two players.
// Player 1 starts at node 1 and moves first.
// Player 2 starts at node 2 and moves second.
// There is a Treasure at node 0 🎯.
// Game Rules:
// The graph[a] represents an undirected graph where graph[a] contains a list of all nodes connected to
// node a.
// Player 1 moves first, followed by Player 2.
// Player 2 cannot move to node 0 (Treasure).
// The game ends in three ways:
// Player 1 reaches node 0 → Player 1 Wins (Return 1).
// Player 2 catches Player 1 (both at the same node) → Player 2 Wins (Return 2).
// A position repeats (same player at the same node) → Game is a Draw (Return 0).
// Given Graph Representation:
// Graph = [
//  [2, 5], # Node 0 is connected to nodes 2 and 5 (Hole)
//  [3], # Node 1 is connected to node 3 (Mouse starts here)
//  [0, 4, 5],# Node 2 is connected to nodes 0, 4, and 5 (Cat starts here)
//  [1, 4, 5],# Node 3 is connected to nodes 1, 4, and 5
//  [2, 3], # Node 4 is connected to nodes 2 and 3
//  [0, 2, 3] # Node 5 is connected to nodes 0, 2, and 3]
// Game Rules Recap
// Player 1 starts at node 1 and moves first.
// Player 2 starts at node 2 and moves second.
// Player 2 cannot move to node 0.
// The game ends in three ways:
// Player 1 reaches node 0 → Player 1 Wins (1)
// Player 2 catches Player 1 → Player 2 Wins (2)
// A repeated position occurs → Game is a Draw (0)
// Step-by-Step Simulation
// Turn 1: Player 1 Moves (Starts at Node 1)
// The only move available is to node 3.
// Player 1 moves to node 3.
// Turn 2: Player 2 Moves (Starts at Node 2)
// Player 2 has three options: node 0 (forbidden), node 4, and node 5.
// Player 2 moves to node 4.
// Turn 3: Player 1 Moves (At Node 3)
// Player 1 can move to node 1, node 4, or node 5.
// To move toward node 0, Player 1 moves to node 5.
// Turn 4: Player 2 Moves (At Node 4)
// The only move available is to node 2.
// Player 2 moves back to node 2.
// Turn 5: Player 1 Moves (At Node 5)
// Player 1 can move to node 0 (winning move), node 2, or node 3.
// Player 1 moves to node 0 and wins, but...
// Turn 6: Player 2 Moves (At Node 2)
// Player 2 can move to node 0 (forbidden), node 4, or node 5.
// Player 2 moves to node 5.
// Cycle Detection and Draw Condition
// The same positions start repeating (nodes 3, 5, 4, and 2).
// The game enters a loop.
// Since no player forces a win, the game results in a draw (0).
// Final Output:
// Output: 0 (Draw)



//Solution
import java.util.*;

public class TreasureHuntGame {

    /*
     * Problem Summary:
     * Player 1 (Mouse) starts at node 1, Player 2 (Cat) starts at node 2.
     * The treasure is at node 0.
     * Mouse moves first. Cat cannot go to node 0.
     * The game ends when:
     * - Mouse reaches node 0 → Mouse wins (1)
     * - Cat catches Mouse → Cat wins (2)
     * - A position repeats → Draw (0)
     * 
     * Fixed Approach:
     * - Use minimax with memoization to determine optimal play
     * - Each player chooses the move that gives them the best outcome
     * - Properly handle cycle detection for draws
     */

    static class GameState {
        int mousePos;
        int catPos;
        int turn; // 0 = mouse's turn, 1 = cat's turn

        GameState(int m, int c, int t) {
            mousePos = m;
            catPos = c;
            turn = t;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GameState)) return false;
            GameState state = (GameState) o;
            return mousePos == state.mousePos && catPos == state.catPos && turn == state.turn;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mousePos, catPos, turn);
        }

        @Override
        public String toString() {
            return "(" + mousePos + "," + catPos + "," + turn + ")";
        }
    }

    private static Map<GameState, Integer> memo = new HashMap<>();
    private static Set<GameState> visiting = new HashSet<>();

    public static int playGame(int[][] graph) {
        memo.clear();
        visiting.clear();
        return minimax(graph, 1, 2, 0); // mouse at 1, cat at 2, mouse's turn
    }

    private static int minimax(int[][] graph, int mouse, int cat, int turn) {
        // Base cases
        if (mouse == 0) return 1; // Mouse wins
        if (mouse == cat) return 2; // Cat wins

        GameState state = new GameState(mouse, cat, turn);
        
        // Check if we're in a cycle (visiting same state in current path)
        if (visiting.contains(state)) {
            return 0; // Draw due to cycle
        }
        
        // Check memoization
        if (memo.containsKey(state)) {
            return memo.get(state);
        }

        visiting.add(state);
        int result;

        if (turn == 0) { // Mouse's turn
            result = 2; // Assume worst case for mouse (cat wins)
            
            for (int nextMouse : graph[mouse]) {
                int outcome = minimax(graph, nextMouse, cat, 1);
                if (outcome == 1) { // Mouse can win
                    result = 1;
                    break;
                } else if (outcome == 0) { // Draw is better than losing
                    result = 0;
                }
            }
        } else { // Cat's turn
            result = 1; // Assume worst case for cat (mouse wins)
            
            for (int nextCat : graph[cat]) {
                if (nextCat == 0) continue; // Cat cannot move to treasure
                
                int outcome = minimax(graph, mouse, nextCat, 0);
                if (outcome == 2) { // Cat can win
                    result = 2;
                    break;
                } else if (outcome == 0) { // Draw is better than losing
                    result = 0;
                }
            }
        }

        visiting.remove(state);
        memo.put(state, result);
        return result;
    }

    // Alternative implementation using topological sorting (more efficient)
    public static int playGameOptimal(int[][] graph) {
        int n = graph.length;
        int DRAW = 0, MOUSE = 1, CAT = 2;
        
        // color[node][cat][turn] = outcome
        // node: mouse position, cat: cat position, turn: 0=mouse, 1=cat
        int[][][] color = new int[n][n][2];
        int[][][] degree = new int[n][n][2];
        
        // Calculate degrees (number of possible moves from each state)
        for (int m = 0; m < n; m++) {
            for (int c = 0; c < n; c++) {
                degree[m][c][0] = graph[m].length; // mouse's moves
                degree[m][c][1] = graph[c].length; // cat's moves
                for (int node : graph[c]) {
                    if (node == 0) {
                        degree[m][c][1]--; // cat cannot go to hole
                        break;
                    }
                }
            }
        }
        
        Queue<int[]> queue = new LinkedList<>();
        
        // Initialize winning/losing positions
        for (int cat = 0; cat < n; cat++) {
            for (int turn = 0; turn < 2; turn++) {
                color[0][cat][turn] = MOUSE; // mouse at hole wins
                queue.offer(new int[]{0, cat, turn, MOUSE});
                if (cat > 0) { // cat cannot be at hole initially
                    color[cat][cat][turn] = CAT; // cat catches mouse
                    queue.offer(new int[]{cat, cat, turn, CAT});
                }
            }
        }
        
        // Propagate results backwards
        while (!queue.isEmpty()) {
            int[] node = queue.poll();
            int mouse = node[0], cat = node[1], turn = node[2], result = node[3];
            
            // Look at parent states
            if (turn == 0) { // Current turn was mouse, so previous was cat
                for (int prevCat : graph[cat]) {
                    if (prevCat == 0) continue; // cat cannot be at hole
                    if (color[mouse][prevCat][1] != DRAW) continue; // already colored
                    
                    if (result == CAT) {
                        // Cat can force a win
                        color[mouse][prevCat][1] = CAT;
                        queue.offer(new int[]{mouse, prevCat, 1, CAT});
                    } else {
                        degree[mouse][prevCat][1]--;
                        if (degree[mouse][prevCat][1] == 0) {
                            // Cat has no good moves, mouse wins
                            color[mouse][prevCat][1] = MOUSE;
                            queue.offer(new int[]{mouse, prevCat, 1, MOUSE});
                        }
                    }
                }
            } else { // Current turn was cat, so previous was mouse
                for (int prevMouse : graph[mouse]) {
                    if (color[prevMouse][cat][0] != DRAW) continue; // already colored
                    
                    if (result == MOUSE) {
                        // Mouse can force a win
                        color[prevMouse][cat][0] = MOUSE;
                        queue.offer(new int[]{prevMouse, cat, 0, MOUSE});
                    } else {
                        degree[prevMouse][cat][0]--;
                        if (degree[prevMouse][cat][0] == 0) {
                            // Mouse has no good moves, cat wins
                            color[prevMouse][cat][0] = CAT;
                            queue.offer(new int[]{prevMouse, cat, 0, CAT});
                        }
                    }
                }
            }
        }
        
        return color[1][2][0]; // mouse at 1, cat at 2, mouse's turn
    }

    // Debug method to trace game simulation
    public static void simulateGame(int[][] graph) {
        System.out.println("\n=== Game Simulation (First few moves) ===");
        Set<GameState> visited = new HashSet<>();
        simulateRecursive(graph, 1, 2, 0, visited, 0);
    }

    private static void simulateRecursive(int[][] graph, int mouse, int cat, int turn, 
                                        Set<GameState> visited, int depth) {
        if (depth > 10) return; // Limit depth for demonstration
        
        GameState state = new GameState(mouse, cat, turn);
        String indent = "  ".repeat(depth);
        
        System.out.println(indent + "Turn " + (depth + 1) + ": " + 
                          (turn == 0 ? "Mouse" : "Cat") + 
                          " | Mouse at " + mouse + ", Cat at " + cat);
        
        if (mouse == 0) {
            System.out.println(indent + "→ Mouse wins!");
            return;
        }
        if (mouse == cat) {
            System.out.println(indent + "→ Cat wins!");
            return;
        }
        if (visited.contains(state)) {
            System.out.println(indent + "→ Cycle detected - Draw!");
            return;
        }
        
        visited.add(state);
        
        if (turn == 0) { // Mouse turn
            System.out.println(indent + "Mouse options: " + Arrays.toString(graph[mouse]));
            if (graph[mouse].length > 0) {
                int nextMove = graph[mouse][0]; // Take first move for simulation
                simulateRecursive(graph, nextMove, cat, 1, new HashSet<>(visited), depth + 1);
            }
        } else { // Cat turn
            List<Integer> catOptions = new ArrayList<>();
            for (int move : graph[cat]) {
                if (move != 0) catOptions.add(move);
            }
            System.out.println(indent + "Cat options: " + catOptions);
            if (!catOptions.isEmpty()) {
                int nextMove = catOptions.get(0); // Take first valid move for simulation
                simulateRecursive(graph, mouse, nextMove, 0, new HashSet<>(visited), depth + 1);
            }
        }
    }

    public static void main(String[] args) {
        int[][] graph = {
            {2, 5},    // Node 0 (treasure)
            {3},       // Node 1 (Mouse starts here)
            {0, 4, 5}, // Node 2 (Cat starts here)
            {1, 4, 5}, // Node 3
            {2, 3},    // Node 4
            {0, 2, 3}  // Node 5
        };

        System.out.println("=== Treasure Hunt Game Analysis ===");
        
        // Test with minimax approach
        int result1 = playGame(graph);
        System.out.println("Result (Minimax): " + result1);
        
        // Test with optimal topological approach
        int result2 = playGameOptimal(graph);
        System.out.println("Result (Optimal): " + result2);
        
        // Explain the result
        switch (result1) {
            case 0:
                System.out.println("→ Draw: Neither player can force a win with optimal play");
                break;
            case 1:
                System.out.println("→ Mouse wins with optimal play");
                break;
            case 2:
                System.out.println("→ Cat wins with optimal play");
                break;
        }
        
        // Show simulation
        simulateGame(graph);
        
        System.out.println("\nFinal Answer: " + result1);
    }
}



//output

// === Treasure Hunt Game Analysis ===
// Result (Minimax): 0
// Result (Optimal): 0
// ? Draw: Neither player can force a win with optimal play

// === Game Simulation (First few moves) ===
// Turn 1: Mouse | Mouse at 1, Cat at 2
// Mouse options: [3]
//   Turn 2: Cat | Mouse at 3, Cat at 2
//   Cat options: [4, 5]
//     Turn 3: Mouse | Mouse at 3, Cat at 4
//     Mouse options: [1, 4, 5]
//       Turn 4: Cat | Mouse at 1, Cat at 4
//       Cat options: [2, 3]
//         Turn 5: Mouse | Mouse at 1, Cat at 2
//         ? Cycle detected - Draw!

// Final Answer: 0