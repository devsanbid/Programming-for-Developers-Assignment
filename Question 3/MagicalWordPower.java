// In a mystical world, powerful magical words exist. A magical word is a sequence of letters that reads
// the same forward and backward and always has an odd number of letters.
// A sorcerer has a long ancient manuscript represented as a string M. The sorcerer wants to extract two
// non-overlapping magical words from M in order to maximize their power combination.
// The power of a magical word is equal to its length, and the total power is the product of the lengths of
// the two chosen magical words.
// Task
// Given the manuscript M, determine the maximum possible power combination that can be achieved
// by selecting two non-overlapping magical words.
// Example 1
// Input:
// M = "xyzyxabc"
// Output:5
// Explanation:
// The magical word "xyzyx" (length 5) at [0:4]
// The magical word "a" (length 1) at [5:5]
// The product is 5 × 1 = 5
// Even if we instead choose:
// "xyzyx" (length 5)
// "c" (length 1)
// Max product = 5 × 1 = 5
// So the best possible product is 5.
// Example 2
// Input: M = "levelwowracecar"
// Output: 35
// Explanation:
// "level" (length 5)
// "racecar" (length 7)
// The product is 5 × 7 = 35.


//solution
public class MagicalWordPower {

    /**
     * Finds the maximum product of lengths of two non-overlapping magical words
     * (odd-length palindromes) in the manuscript M.
     *
     * @param M the manuscript string
     * @return maximum product, or 0 if no two non-overlapping magical words exist
     */
    public static int maxPowerCombination(String M) {
        if (M == null || M.length() < 2) {
            return 0;
        }

        int n = M.length();
        int[] leftMax = new int[n];  // leftMax[i] = max palindrome length ending <= i
        int[] rightMax = new int[n]; // rightMax[i] = max palindrome length starting >= i

        // Initialize arrays to 0
        for (int i = 0; i < n; i++) {
            leftMax[i] = 0;
            rightMax[i] = 0;
        }

        // Step 1: Expand around each center to find all odd-length palindromes
        for (int center = 0; center < n; center++) {
            int l = center, r = center;
            while (l >= 0 && r < n && M.charAt(l) == M.charAt(r)) {
                int length = r - l + 1;
                // Update leftMax and rightMax
                if (length > leftMax[r]) {
                    leftMax[r] = length;
                }
                if (length > rightMax[l]) {
                    rightMax[l] = length;
                }
                l--;
                r++;
            }
        }

        // Step 2: Make leftMax[i] = max palindrome length ending <= i
        for (int i = 1; i < n; i++) {
            leftMax[i] = Math.max(leftMax[i], leftMax[i - 1]);
        }

        // Step 3: Make rightMax[i] = max palindrome length starting >= i
        for (int i = n - 2; i >= 0; i--) {
            rightMax[i] = Math.max(rightMax[i], rightMax[i + 1]);
        }

        // Step 4: Try every split point
        int maxProduct = 0;
        for (int i = 0; i < n - 1; i++) {
            int leftPart = leftMax[i];        // best palindrome in [0, i]
            int rightPart = rightMax[i + 1];  // best palindrome in [i+1, n-1]
            if (leftPart > 0 && rightPart > 0) {
                int product = leftPart * rightPart;
                if (product > maxProduct) {
                    maxProduct = product;
                }
                // Early break if possible? Not really.
            }
        }

        return maxProduct;
    }

    // ==================== Test Cases ====================

    public static void main(String[] args) {
        // Test Case 1
        System.out.println("Test Case 1: M = \"xyzyxabc\"");
        int result1 = maxPowerCombination("xyzyxabc");
        System.out.println("Result: " + result1);
        System.out.println("Expected: 5");
        System.out.println("Pass: " + (result1 == 5 ? "Yes" : "No"));
        System.out.println();

        // Test Case 2
        System.out.println("Test Case 2: M = \"levelwowracecar\"");
        int result2 = maxPowerCombination("levelwowracecar");
        System.out.println("Result: " + result2);
        System.out.println("Expected: 35");
        System.out.println("Pass: " + (result2 == 35 ? "Yes" : "No"));
        System.out.println();

        // Test Case 3: No two non-overlapping
        System.out.println("Test Case 3: M = \"abc\"");
        // Only single letters: "a", "b", "c" → 1×1 = 1
        int result3 = maxPowerCombination("abc");
        System.out.println("Result: " + result3);
        System.out.println("Expected: 1");
        System.out.println("Pass: " + (result3 == 1 ? "Yes" : "No"));
        System.out.println();

        // Test Case 4: One long palindrome, rest small
        System.out.println("Test Case 4: M = \"racecarab\"");
        // "racecar" (0-6, len=7), "a", "b", "c" → best: 7×1 = 7
        int result4 = maxPowerCombination("racecarab");
        System.out.println("Result: " + result4);
        System.out.println("Expected: 7");
        System.out.println("Pass: " + (result4 == 7 ? "Yes" : "No"));
        System.out.println();

        // Test Case 5: Two overlapping long palindromes
        System.out.println("Test Case 5: M = \"abacaba\"");
        // "abacaba" is palindrome (len=7), but only one
        // Also "aba" at start and end
        // Can we pick two non-overlapping?
        // "aba" at [0,2] and "aba" at [4,6] → non-overlapping? 2 < 4 → yes
        // So 3×3 = 9
        int result5 = maxPowerCombination("abacaba");
        System.out.println("Result: " + result5);
        System.out.println("Expected: 9");
        System.out.println("Pass: " + (result5 == 9 ? "Yes" : "No"));
        System.out.println();

        // Test Case 6: Empty or single char
        System.out.println("Test Case 6: M = \"a\"");
        int result6 = maxPowerCombination("a");
        System.out.println("Result: " + result6);
        System.out.println("Expected: 0");
        System.out.println("Pass: " + (result6 == 0 ? "Yes" : "No"));
        System.out.println();
    }
}

//Output
// Test Case 1: M = "xyzyxabc"
// Result: 5
// Expected: 5
// Pass: Yes

// Test Case 2: M = "levelwowracecar"
// Result: 35
// Expected: 35
// Pass: Yes

// Test Case 3: M = "abc"
// Result: 1
// Expected: 1
// Pass: Yes

// Test Case 4: M = "racecarab"
// Result: 9
// Expected: 7
// Pass: No

// Test Case 5: M = "abacaba"
// Result: 9
// Expected: 9
// Pass: Yes

// Test Case 6: M = "a"
// Result: 0
// Expected: 0
// Pass: Yes