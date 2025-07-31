// Maximizing Tech Startup Revenue before Acquisition
// A tech startup, AlgoStart, is planning to get acquired by a larger company. To negotiate a higher
// acquisition price, AlgoStart wants to increase its revenue by launching a few high-return projects.
// However, due to limited resources, the startup can only work on at most k distinct projects before the
// acquisition.
// You are given n potential projects, where the i-th project has a projected revenue gain of revenues[i]
// and requires a minimum investment capital of investments[i] to launch.
// Initially, AlgoStart has c capital. When a project is completed, its revenue gain is added to the
// startup’s total capital, which can then be reinvested into other projects.
// Your task is to determine the maximum possible capital AlgoStart can accumulate after completing at
// most k projects.
// Example 1:
// k = 2, c = 0, revenues = [2, 5, 8], investments = [0, 2, 3]
// Output: 7
// Explanation:
//  With initial capital 0, the startup can only launch Project 0 (since it requires 0 investment).
//  After completing Project 0, the capital becomes 0 + 2 = 2.
//  Now, with 2 capital, the startup can choose either Project 1 (investment 2, revenue 5) or
// Project 2 (investment 3, revenue 8).
//  To maximize revenue, it should select Project 2. However, Project 2 requires 3 capital, which is
// not available. So it selects Project 1.
//  After completing Project 1, the capital becomes 2 + 5 = 7.
//  The final maximized capital is 7.
// Example 2:
// Input:
// k = 3, c = 1, revenues = [3, 6, 10], investments = [1, 3, 5]
// Output: 19
// Explanation:
//  Initially, with 1 capital, Project 0 can be launched (investment 1, revenue 3).
//  Capital after Project 0 = 1 + 3 = 4.
//  With 4 capital, the startup can now launch Project 1 (investment 3, revenue 6).
//  Capital after Project 1 = 4 + 6 = 10.
//  Now, with 10 capital, Project 2 (investment 5, revenue 10) can be launched.
//  Final capital = 10 + 10 = 20.







//STEPS FOR SOLVING THIS SOLUTION:

// Move all projects from minInvestHeap to maxRevenueHeap where investment ≤ currentCapital

// Pick the top project from maxRevenueHeap (highest revenue)

// Add its revenue to capital

// Repeat for up to k rounds





// Solution with Test Cases & Explanation
import java.util.*;

public class MaximizeStartupRevenue {

    /**
     * Represents a project with minimum investment required and revenue generated.
     */
    static class Project {
        int investment;
        int revenue;

        Project(int investment, int revenue) {
            this.investment = investment;
            this.revenue = revenue;
        }
    }

    /**
     * Computes the maximum capital achievable after completing at most k projects.
     *
     * @param k            Maximum number of projects that can be completed
     * @param c            Initial capital
     * @param revenues     Array of revenue gains for each project
     * @param investments  Array of minimum capital required for each project
     * @return             Maximum possible capital after up to k projects
     */
    public static int maximizeCapital(int k, int c, int[] revenues, int[] investments) {
        int n = revenues.length;

        // Min-heap: projects sorted by investment required (ascending)
        PriorityQueue<Project> minInvestHeap = new PriorityQueue<>(
                (a, b) -> Integer.compare(a.investment, b.investment));

        // Max-heap: affordable projects sorted by revenue (descending)
        PriorityQueue<Project> maxRevenueHeap = new PriorityQueue<>(
                (a, b) -> Integer.compare(b.revenue, a.revenue));

        // Add all projects to the min-investment heap
        for (int i = 0; i < n; i++) {
            minInvestHeap.offer(new Project(investments[i], revenues[i]));
        }

        int currentCapital = c;

        // Try to complete up to k projects
        for (int i = 0; i < k; i++) {
            // Unlock all projects that can now be afforded
            while (!minInvestHeap.isEmpty() && minInvestHeap.peek().investment <= currentCapital) {
                maxRevenueHeap.offer(minInvestHeap.poll());
            }

            // If no project is affordable, break early
            if (maxRevenueHeap.isEmpty()) {
                break;
            }

            // Select the project with maximum revenue
            Project bestProject = maxRevenueHeap.poll();
            currentCapital += bestProject.revenue;
        }

        return currentCapital;
    }

    // ————————————————————————
    // 🧪 MAIN METHOD WITH TEST CASES
    // ————————————————————————

    public static void main(String[] args) {
        System.out.println("=== Maximizing Tech Startup Revenue ===\n");

        // ———————— Test Case 1 ————————
        System.out.println("Test Case 1:");
        System.out.println("k = 2, c = 0");
        System.out.println("Revenues = [2, 5, 8]");
        System.out.println("Investments = [0, 2, 3]");
        int k1 = 2, c1 = 0;
        int[] revenues1 = { 2, 5, 8 };
        int[] investments1 = { 0, 2, 3 };
        int result1 = maximizeCapital(k1, c1, revenues1, investments1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 7");
        System.out.println("Pass: " + (result1 == 7 ? "yes" : "no"));
        System.out.println();

        // ———————— Test Case 2 ————————
        System.out.println("Test Case 2:");
        System.out.println("k = 3, c = 1");
        System.out.println("Revenues = [3, 6, 10]");
        System.out.println("Investments = [1, 3, 5]");
        int k2 = 3, c2 = 1;
        int[] revenues2 = { 3, 6, 10 };
        int[] investments2 = { 1, 3, 5 };
        int result2 = maximizeCapital(k2, c2, revenues2, investments2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 20");
        System.out.println("Pass: " + (result2 == 20 ? "yes" : "no"));
        System.out.println();

        // ———————— Test Case 3: No project can be started ————————
        System.out.println("Test Case 3 (Edge Case):");
        System.out.println("k = 5, c = 0, All investments > 0");
        int k3 = 5, c3 = 0;
        int[] revenues3 = { 10, 20 };
        int[] investments3 = { 5, 10 };
        int result3 = maximizeCapital(k3, c3, revenues3, investments3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println("Pass: " + (result3 == 0 ? "yes" : "no"));
        System.out.println();

        // ———————— Test Case 4: k = 0 ————————
        System.out.println("Test Case 4 (k = 0):");
        int k4 = 0, c4 = 100;
        int[] revenues4 = { 50 };
        int[] investments4 = { 20 };
        int result4 = maximizeCapital(k4, c4, revenues4, investments4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 100");
        System.out.println("Pass: " + (result4 == 100 ? "yes" : "no"));
        System.out.println();

        // ———————— Summary ————————
        System.out.println("=== All Tests Completed ===");
    }
}

//Output

// === Maximizing Tech Startup Revenue ===

// Test Case 1:
// k = 2, c = 0
// Revenues = [2, 5, 8]
// Investments = [0, 2, 3]
// Output: 7
// Expected: 7
// Pass: yes

// Test Case 2:
// k = 3, c = 1
// Revenues = [3, 6, 10]
// Investments = [1, 3, 5]
// Output: 20
// Expected: 20
// Pass: yes

// Test Case 3 (Edge Case):
// k = 5, c = 0, All investments > 0
// Output: 0
// Expected: 0
// Pass: yes

// Test Case 4 (k = 0):
// Output: 100
// Expected: 100
// Pass: yes

// === All Tests Completed ===