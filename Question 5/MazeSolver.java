// A Maze Solver
// Functionality:
//  Graph (Adjacency List/Matrix): Use a graph to represent the maze where each cell is a node
// connected to its adjacent cells.
//  Stack (DFS) / Queue (BFS): Use a stack for Depth-First Search (DFS) or a queue for Breadth-First
// Search (BFS) to find a path from start to finish.
// GUI:
//  A grid-based maze where each cell is a node.
//  A start and end point for the player or algorithm.
//  Buttons to solve the maze using DFS or BFS.
//  A "Generate New Maze" button to create a randomized maze.
// Implementation:
// Initialization:
// 1. Generate a random maze using a grid where walls block movement.
// 2. Represent the maze as a graph (adjacency list or matrix).
// 3. Allow the user to choose a start and end point.
// 4. Display the maze in the GUI.
// Solving the Maze:
// While the path is not found:
// 1. Choose an algorithm:
// o If DFS is selected, use a stack.
// o If BFS is selected, use a queue.
// 2. Explore adjacent nodes:
// o If a path is found, mark it.
// o If a dead-end is reached, backtrack.
// 3. Highlight the solution path on the GUI.
// Game Over:
//  If the end point is reached, display a success message.
//  If no path exists, display a failure message.
// Data Structures:
//  Graph: Represent the maze where each cell is a node connected to adjacent walkable cells.
//  Queue: Used for BFS to find the shortest path.
//  Stack: Used for DFS to explore paths.
//  2D Array: Represents the grid-based maze.
// Additional Considerations:
//  Random Maze Generation: Use algorithms like Prim’s or Recursive Backtracking to generate
// new mazes dynamically.
//  User Input: Allow the player to manually navigate through the maze.
//  Path Animation: Visually show the algorithm exploring paths.
//  Scoring System: Award points based on efficiency (steps taken, time, etc.).





// solution
import javax.swing.*;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.Queue;

/**
 * Maze Solver Application using Java Swing
 * Features: Random maze generation, DFS/BFS pathfinding, interactive GUI
 * Data Structures: Graph (Adjacency List), Stack (DFS), Queue (BFS), 2D Array (Grid)
 */
public class MazeSolver extends JFrame {
    // Constants
    private static final int MAZE_SIZE = 25;
    private static final int CELL_SIZE = 20;
    private static final Color WALL_COLOR = Color.BLACK;
    private static final Color PATH_COLOR = Color.WHITE;
    private static final Color START_COLOR = Color.GREEN;
    private static final Color END_COLOR = Color.RED;
    private static final Color VISITED_COLOR = Color.LIGHT_GRAY;
    private static final Color SOLUTION_COLOR = Color.BLUE;
    private static final Color EXPLORING_COLOR = Color.YELLOW;
    
    // Cell types
    private static final int WALL = 0;
    private static final int PATH = 1;
    private static final int START = 2;
    private static final int END = 3;
    private static final int VISITED = 4;
    private static final int SOLUTION = 5;
    private static final int EXPLORING = 6;
    
    // Instance variables
    private int[][] maze;
    private Map<Point, List<Point>> graph;
    private MazePanel mazePanel;
    private Point startPoint;
    private Point endPoint;
    private boolean isSettingStart = false;
    private boolean isSettingEnd = false;
    private Timer animationTimer;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private int steps = 0;
    private long startTime;
    
    /**
     * Constructor - Initialize the maze solver application
     */
    public MazeSolver() {
        setTitle("Maze Solver - DFS & BFS Pathfinding");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        initializeComponents();
        generateNewMaze();
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    /**
     * Initialize GUI components
     */
    private void initializeComponents() {
        // Create maze panel
        mazePanel = new MazePanel();
        add(mazePanel, BorderLayout.CENTER);
        
        // Create control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        JButton generateBtn = new JButton("Generate New Maze");
        generateBtn.addActionListener(e -> generateNewMaze());
        
        JButton setStartBtn = new JButton("Set Start Point");
        setStartBtn.addActionListener(e -> {
            isSettingStart = true;
            isSettingEnd = false;
            statusLabel.setText("Click on a cell to set start point");
        });
        
        JButton setEndBtn = new JButton("Set End Point");
        setEndBtn.addActionListener(e -> {
            isSettingEnd = true;
            isSettingStart = false;
            statusLabel.setText("Click on a cell to set end point");
        });
        
        JButton solveWithDFS = new JButton("Solve with DFS");
        solveWithDFS.addActionListener(e -> solveMaze(true));
        
        JButton solveWithBFS = new JButton("Solve with BFS");
        solveWithBFS.addActionListener(e -> solveMaze(false));
        
        JButton clearPath = new JButton("Clear Path");
        clearPath.addActionListener(e -> clearSolution());
        
        controlPanel.add(generateBtn);
        controlPanel.add(setStartBtn);
        controlPanel.add(setEndBtn);
        controlPanel.add(solveWithDFS);
        controlPanel.add(solveWithBFS);
        controlPanel.add(clearPath);
        
        add(controlPanel, BorderLayout.NORTH);
        
        // Create status panel
        JPanel statusPanel = new JPanel(new FlowLayout());
        statusLabel = new JLabel("Click 'Generate New Maze' to start");
        scoreLabel = new JLabel("Steps: 0 | Time: 0ms");
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(scoreLabel);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Generate a new random maze using recursive backtracking algorithm
     */
    private void generateNewMaze() {
        maze = new int[MAZE_SIZE][MAZE_SIZE];
        graph = new HashMap<>();
        
        // Initialize maze with walls
        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                maze[i][j] = WALL;
            }
        }
        
        // Generate maze using recursive backtracking
        generateMazeRecursive(1, 1);
        
        // Set default start and end points
        startPoint = new Point(1, 1);
        endPoint = new Point(MAZE_SIZE - 2, MAZE_SIZE - 2);
        maze[startPoint.x][startPoint.y] = START;
        maze[endPoint.x][endPoint.y] = END;
        
        // Build graph representation
        buildGraph();
        
        statusLabel.setText("New maze generated! Set start/end points and solve.");
        scoreLabel.setText("Steps: 0 | Time: 0ms");
        mazePanel.repaint();
    }
    
    /**
     * Recursive backtracking maze generation algorithm
     */
    private void generateMazeRecursive(int x, int y) {
        maze[x][y] = PATH;
        
        // Create list of directions and shuffle
        List<int[]> directions = Arrays.asList(
            new int[]{0, 2}, new int[]{2, 0}, new int[]{0, -2}, new int[]{-2, 0}
        );
        Collections.shuffle(directions);
        
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            
            if (isValidCell(newX, newY) && maze[newX][newY] == WALL) {
                // Remove wall between current cell and new cell
                maze[x + dir[0] / 2][y + dir[1] / 2] = PATH;
                generateMazeRecursive(newX, newY);
            }
        }
    }
    
    /**
     * Build graph representation of the maze
     */
    private void buildGraph() {
        graph.clear();
        
        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                if (maze[i][j] != WALL) {
                    Point current = new Point(i, j);
                    List<Point> neighbors = new ArrayList<>();
                    
                    // Check all four directions
                    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                    for (int[] dir : directions) {
                        int newX = i + dir[0];
                        int newY = j + dir[1];
                        
                        if (isValidCell(newX, newY) && maze[newX][newY] != WALL) {
                            neighbors.add(new Point(newX, newY));
                        }
                    }
                    
                    graph.put(current, neighbors);
                }
            }
        }
    }
    
    /**
     * Check if a cell is valid (within bounds)
     */
    private boolean isValidCell(int x, int y) {
        return x >= 0 && x < MAZE_SIZE && y >= 0 && y < MAZE_SIZE;
    }
    
    /**
     * Solve the maze using DFS or BFS
     * @param useDFS true for DFS, false for BFS
     */
    private void solveMaze(boolean useDFS) {
        if (startPoint == null || endPoint == null) {
            JOptionPane.showMessageDialog(this, "Please set both start and end points!");
            return;
        }
        
        clearSolution();
        startTime = System.currentTimeMillis();
        steps = 0;
        
        if (useDFS) {
            statusLabel.setText("Solving with DFS (Depth-First Search)...");
            solveDFS();
        } else {
            statusLabel.setText("Solving with BFS (Breadth-First Search)...");
            solveBFS();
        }
    }
    
    /**
     * Solve maze using Depth-First Search (Stack-based)
     */
    private void solveDFS() {
        Stack<Point> stack = new Stack<>();
        Set<Point> visited = new HashSet<>();
        Map<Point, Point> parent = new HashMap<>();
        
        stack.push(startPoint);
        visited.add(startPoint);
        
        List<Point> explorationPath = new ArrayList<>();
        
        while (!stack.isEmpty()) {
            Point current = stack.pop();
            explorationPath.add(current);
            
            if (current.equals(endPoint)) {
                // Found solution, reconstruct path
                List<Point> solutionPath = reconstructPath(parent, startPoint, endPoint);
                animateSolution(explorationPath, solutionPath, "DFS");
                return;
            }
            
            List<Point> neighbors = graph.get(current);
            if (neighbors != null) {
                for (Point neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parent.put(neighbor, current);
                        stack.push(neighbor);
                    }
                }
            }
        }
        
        // No solution found
        statusLabel.setText("No solution exists!");
        JOptionPane.showMessageDialog(this, "No path found from start to end!");
    }
    
    /**
     * Solve maze using Breadth-First Search (Queue-based)
     */
    private void solveBFS() {
        Queue<Point> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();
        Map<Point, Point> parent = new HashMap<>();
        
        queue.offer(startPoint);
        visited.add(startPoint);
        
        List<Point> explorationPath = new ArrayList<>();
        
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            explorationPath.add(current);
            
            if (current.equals(endPoint)) {
                // Found solution, reconstruct path
                List<Point> solutionPath = reconstructPath(parent, startPoint, endPoint);
                animateSolution(explorationPath, solutionPath, "BFS");
                return;
            }
            
            List<Point> neighbors = graph.get(current);
            if (neighbors != null) {
                for (Point neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parent.put(neighbor, current);
                        queue.offer(neighbor);
                    }
                }
            }
        }
        
        // No solution found
        statusLabel.setText("No solution exists!");
        JOptionPane.showMessageDialog(this, "No path found from start to end!");
    }
    
    /**
     * Reconstruct the solution path from parent mapping
     */
    private List<Point> reconstructPath(Map<Point, Point> parent, Point start, Point end) {
        List<Point> path = new ArrayList<>();
        Point current = end;
        
        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }
        
        Collections.reverse(path);
        return path;
    }
    
    /**
     * Animate the solution with exploration and final path
     */
    private void animateSolution(List<Point> explorationPath, List<Point> solutionPath, String algorithm) {
        final int[] index = {0};
        final boolean[] showingExploration = {true};
        
        animationTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showingExploration[0]) {
                    // Show exploration phase
                    if (index[0] < explorationPath.size()) {
                        Point p = explorationPath.get(index[0]);
                        if (!p.equals(startPoint) && !p.equals(endPoint)) {
                            maze[p.x][p.y] = EXPLORING;
                        }
                        steps++;
                        index[0]++;
                        mazePanel.repaint();
                        
                        long currentTime = System.currentTimeMillis();
                        scoreLabel.setText("Steps: " + steps + " | Time: " + (currentTime - startTime) + "ms");
                    } else {
                        // Switch to showing solution
                        showingExploration[0] = false;
                        index[0] = 0;
                        
                        // Clear exploration colors except start and end
                        for (Point p : explorationPath) {
                            if (!p.equals(startPoint) && !p.equals(endPoint)) {
                                maze[p.x][p.y] = VISITED;
                            }
                        }
                    }
                } else {
                    // Show solution path
                    if (index[0] < solutionPath.size()) {
                        Point p = solutionPath.get(index[0]);
                        if (!p.equals(startPoint) && !p.equals(endPoint)) {
                            maze[p.x][p.y] = SOLUTION;
                        }
                        index[0]++;
                        mazePanel.repaint();
                    } else {
                        // Animation complete
                        animationTimer.stop();
                        long totalTime = System.currentTimeMillis() - startTime;
                        statusLabel.setText(algorithm + " completed! Path found in " + 
                                          solutionPath.size() + " steps.");
                        scoreLabel.setText("Explored: " + explorationPath.size() + 
                                         " | Solution: " + solutionPath.size() + 
                                         " | Time: " + totalTime + "ms");
                        
                        JOptionPane.showMessageDialog(MazeSolver.this, 
                            algorithm + " Solution Found!\n" +
                            "Exploration steps: " + explorationPath.size() + "\n" +
                            "Solution path length: " + solutionPath.size() + "\n" +
                            "Time taken: " + totalTime + "ms");
                    }
                }
            }
        });
        
        animationTimer.start();
    }
    
    /**
     * Clear the solution and visited cells
     */
    private void clearSolution() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        
        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                if (maze[i][j] == VISITED || maze[i][j] == SOLUTION || maze[i][j] == EXPLORING) {
                    maze[i][j] = PATH;
                }
            }
        }
        
        // Restore start and end points
        if (startPoint != null) maze[startPoint.x][startPoint.y] = START;
        if (endPoint != null) maze[endPoint.x][endPoint.y] = END;
        
        statusLabel.setText("Path cleared. Ready to solve again.");
        scoreLabel.setText("Steps: 0 | Time: 0ms");
        mazePanel.repaint();
    }
    
    /**
     * Custom JPanel for drawing the maze
     */
    private class MazePanel extends JPanel {
        public MazePanel() {
            setPreferredSize(new Dimension(MAZE_SIZE * CELL_SIZE, MAZE_SIZE * CELL_SIZE));
            setBackground(Color.WHITE);
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int x = e.getY() / CELL_SIZE;
                    int y = e.getX() / CELL_SIZE;
                    
                    if (isValidCell(x, y) && maze[x][y] != WALL) {
                        if (isSettingStart) {
                            // Clear previous start point
                            if (startPoint != null) {
                                maze[startPoint.x][startPoint.y] = PATH;
                            }
                            
                            startPoint = new Point(x, y);
                            maze[x][y] = START;
                            isSettingStart = false;
                            statusLabel.setText("Start point set at (" + x + ", " + y + ")");
                            buildGraph(); // Rebuild graph to include new start point
                        } else if (isSettingEnd) {
                            // Clear previous end point
                            if (endPoint != null) {
                                maze[endPoint.x][endPoint.y] = PATH;
                            }
                            
                            endPoint = new Point(x, y);
                            maze[x][y] = END;
                            isSettingEnd = false;
                            statusLabel.setText("End point set at (" + x + ", " + y + ")");
                            buildGraph(); // Rebuild graph to include new end point
                        }
                        
                        repaint();
                    }
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            for (int i = 0; i < MAZE_SIZE; i++) {
                for (int j = 0; j < MAZE_SIZE; j++) {
                    Color cellColor = getCellColor(maze[i][j]);
                    g.setColor(cellColor);
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    
                    // Draw grid lines
                    g.setColor(Color.GRAY);
                    g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        
        /**
         * Get the appropriate color for a cell type
         */
        private Color getCellColor(int cellType) {
            switch (cellType) {
                case WALL: return WALL_COLOR;
                case PATH: return PATH_COLOR;
                case START: return START_COLOR;
                case END: return END_COLOR;
                case VISITED: return VISITED_COLOR;
                case SOLUTION: return SOLUTION_COLOR;
                case EXPLORING: return EXPLORING_COLOR;
                default: return PATH_COLOR;
            }
        }
    }
    
    /**
     * Main method to run the application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new MazeSolver().setVisible(true);
        });
    }
}