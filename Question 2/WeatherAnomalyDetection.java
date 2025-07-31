// Scenario: Weather Anomaly Detection 🌦️📊
// A climate scientist is analyzing temperature variations over a given period to detect unusual patterns
// in weather changes.
// The scientist has a dataset containing the daily temperature changes (increase or decrease in °C)
// relative to the previous day.
// They want to count the number of continuous time periods where the total temperature change falls
// within a specified anomaly range
// [𝑙𝑜𝑤_𝑡ℎ𝑟𝑒𝑠ℎ𝑜𝑙𝑑,ℎ𝑖𝑔ℎ_𝑡ℎ𝑟𝑒𝑠ℎ𝑜𝑙𝑑]
// Each period is defined as a continuous range of days, and the total anomaly for that period is the sum
// of temperature changes within that range.
// Example 1
// Input:
// temperature_changes = [3, -1, -4, 6, 2]
// low_threshold = 2
// high_threshold = 5
// Output: 3
// Explanation:
// We consider all possible subarrays and their total temperature change:
// Day 0 to Day 0 → Total change = 3 ✅ (within range [2, 5])
// Day 3 to Day 3 → Total change = 6 ❌ (out of range)
// Day 3 to Day 4 → Total change = 6 + 2 = 8 ❌ (out of range)
// Day 1 to Day 3 → Total change = (-1) + (-4) + 6 = 1 ❌ (out of range)
// Day 2 to Day 4 → Total change = (-4) + 6 + 2 = 4 ✅ (within range [2, 5])
// Day 1 to Day 4 → Total change = (-1) + (-4) + 6 + 2 = 3 ✅ (within range [2,5 ])
// Day 0 to Day 2 → Total change = 3 + (-1) + (-4) = -2 ❌ (out of range)
// Day 0 to Day 4 → Total change = 3 + (-1) + (-4) + 6 + 2 = 6 ❌ (out of range)
// Thus, total valid periods = 4.
// Example 2
// Input:
// temperature_changes = [-2, 3, 1, -5, 4]
// low_threshold = -1
// high_threshold = 2
// Output: 4
// Explanation:
// Valid subarrays where the total temperature change falls within [-1, 2]:
// Day 1 to Day 2 → Total change = 3 + 1 = 4 ❌ (out of range)
// Day 2 to Day 3 → Total change = 1 + (-5) = -4 ❌ (out of range)
// Day 1 to Day 3 → Total change = 3 + 1 + (-5) = -1 ✅
// Day 2 to Day 4 → Total change = 1 + (-5) + 4 = 0 ✅
// Day 0 to Day 2 → Total change = (-2) + 3 + 1 = 2 ✅
// Day 1 to Day 4 → Total change = 3 + 1 + (-5) + 4 = 3 ❌ (out of range)
// Day 0 to Day 4 → Total change = (-2) + 3 + 1 + (-5) + 4 = 1 ✅
// Thus, total valid periods = 5




//Solution
public class WeatherAnomalyDetection {

    /**
     * Counts the number of contiguous subarrays where the sum of temperature changes
     * falls within the specified range [low_threshold, high_threshold].
     *
     * @param temperatureChanges array of daily temp changes
     * @param lowThreshold       lower bound of anomaly range (inclusive)
     * @param highThreshold      upper bound of anomaly range (inclusive)
     * @return count of valid subarrays
     */
    public static int countAnomalyPeriods(int[] temperatureChanges, int lowThreshold, int highThreshold) {
        if (temperatureChanges == null || temperatureChanges.length == 0) {
            return 0;
        }

        int n = temperatureChanges.length;
        int count = 0;

        // Iterate over all starting indices
        for (int i = 0; i < n; i++) {
            int sum = 0;
            // Extend subarray from index i to j
            for (int j = i; j < n; j++) {
                sum += temperatureChanges[j];
                if (sum >= lowThreshold && sum <= highThreshold) {
                    count++;
                }
            }
        }

        return count;
    }

    // ==================== Test Cases ====================

    public static void main(String[] args) {
        // Test Case 1: Corrected
        int[] temp1 = {3, -1, -4, 6, 2};
        int low1 = 2, high1 = 5;
        int expected1 = 7; // As computed manually
        int result1 = countAnomalyPeriods(temp1, low1, high1);
        System.out.println("Test Case 1:");
        System.out.println("Input: " + java.util.Arrays.toString(temp1) + ", Range: [" + low1 + "," + high1 + "]");
        System.out.println("Expected: " + expected1 + ", Got: " + result1);
        System.out.println("Pass: " + (result1 == expected1 ? "yes" : "no") + "\n");

        // Test Case 2: Corrected
        int[] temp2 = {-2, 3, 1, -5, 4};
        int low2 = -1, high2 = 2;
        int expected2 = 7;
        int result2 = countAnomalyPeriods(temp2, low2, high2);
        System.out.println("Test Case 2:");
        System.out.println("Input: " + java.util.Arrays.toString(temp2) + ", Range: [" + low2 + "," + high2 + "]");
        System.out.println("Expected: " + expected2 + ", Got: " + result2);
        System.out.println("Pass: " + (result2 == expected2 ? "yes" : "no") + "\n");

        // Test Case 3: Single element within range
        int[] temp3 = {5};
        int low3 = 5, high3 = 5;
        int expected3 = 1;
        int result3 = countAnomalyPeriods(temp3, low3, high3);
        System.out.println("Test Case 3:");
        System.out.println("Input: " + java.util.Arrays.toString(temp3) + ", Range: [" + low3 + "," + high3 + "]");
        System.out.println("Expected: " + expected3 + ", Got: " + result3);
        System.out.println("Pass: " + (result3 == expected3 ? "yes" : "no") + "\n");

        // Test Case 4: Single element out of range
        int[] temp4 = {-10};
        int low4 = -5, high4 = 5;
        int expected4 = 0;
        int result4 = countAnomalyPeriods(temp4, low4, high4);
        System.out.println("Test Case 4:");
        System.out.println("Input: " + java.util.Arrays.toString(temp4) + ", Range: [" + low4 + "," + high4 + "]");
        System.out.println("Expected: " + expected4 + ", Got: " + result4);
        System.out.println("Pass: " + (result4 == expected4 ? "yes" : "no") + "\n");

        // Test Case 5: Empty array
        int[] temp5 = {};
        int expected5 = 0;
        int result5 = countAnomalyPeriods(temp5, 0, 10);
        System.out.println("Test Case 5:");
        System.out.println("Input: " + java.util.Arrays.toString(temp5) + ", Range: [0,10]");
        System.out.println("Expected: " + expected5 + ", Got: " + result5);
        System.out.println("Pass: " + (result5 == expected5 ? "yes" : "no") + "\n");

        // Test Case 6: All zeros, range includes 0
        int[] temp6 = {0, 0, 0};
        int expected6 = 6; // All 6 subarrays have sum 0
        int result6 = countAnomalyPeriods(temp6, 0, 0);
        System.out.println("Test Case 6:");
        System.out.println("Input: " + java.util.Arrays.toString(temp6) + ", Range: [0,0]");
        System.out.println("Expected: " + expected6 + ", Got: " + result6);
        System.out.println("Pass: " + (result6 == expected6 ? "yes" : "no") + "\n");
    }
}

//output

// Test Case 1:
// Input: [3, -1, -4, 6, 2], Range: [2,5]
// Expected: 7, Got: 7
// Pass: yes

// Test Case 2:
// Input: [-2, 3, 1, -5, 4], Range: [-1,2]
// Expected: 7, Got: 7
// Pass: yes

// Test Case 3:
// Input: [5], Range: [5,5]
// Expected: 1, Got: 1
// Pass: yes

// Test Case 4:
// Input: [-10], Range: [-5,5]
// Expected: 0, Got: 0
// Pass: yes

// Test Case 5:
// Input: [], Range: [0,10]
// Expected: 0, Got: 0
// Pass: yes

// Test Case 6:
// Input: [0, 0, 0], Range: [0,0]
// Expected: 6, Got: 6
// Pass: yes
