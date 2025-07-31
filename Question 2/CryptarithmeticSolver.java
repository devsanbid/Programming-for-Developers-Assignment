// Imagine a puzzle where words represent numbers, and we need to find a unique digit mapping for
// each letter to satisfy a given equation. The rule is that no two letters can have the same digit, and
// numbers must not have leading zeros.
// Scenario 1: True Case (Valid Equation)
// Equation:
// "STAR" + "MOON" = "NIGHT"
// "STAR"+"MOON"="NIGHT"
// Step 1: Assign Unique Digits to Letters
// S = 8
// T = 4
// A = 2
// R = 5
// M = 7
// O = 1
// N = 9
// I = 6
// G = 3
// H = 0
// Step 2: Convert Words into Numbers
// "STAR" → 8425
// "MOON" → 7119
// "NIGHT" → 96350
// Step 3: Verify the Sum
// 8425 + 7119 = 15544
// Equation: "CODE" + "BUG" = "DEBUG"
// "CODE"+"BUG"="DEBUG"
// Now, let's try to assign unique digits.
// Step 1: Assign Unique Digits to Letters
// C = 1
// O = 0
// D = 5
// E = 7
// B = 3
// U = 9
// G = 2
// Step 2: Convert Words into Numbers
// "CODE" → 1057
// "BUG" → 392
// "DEBUG" → 57392
// Step 3: Verify the Sum
// 1057+392=1449
// Since 1449 ≠ 57392, this mapping is invalid, and no possible digit assignment satisfies the equation.




//Tracing the solution

// Extract all unique letters from the three words.

// Identify leading letters (first char of each word) — these cannot be zero.

// Use backtracking to try assigning digits 0–9 to letters.

// When all letters are assigned, evaluate:

// Convert each word to its numeric value.

// Check if W1 + W2 == W3.

// If yes → return true.

// If no valid assignment found after all permutations → return false.



//Solution
import java.util.*;

public class CryptarithmeticSolver {

    private static boolean[] used = new boolean[10]; // digit 0-9 used?
    private static Map<Character, Integer> assignment = new HashMap<>();
    private static List<Character> letters = new ArrayList<>();
    private static Set<Character> leadingLetters = new HashSet<>();

    // Words
    private static String word1, word2, word3;

    public static boolean isSolvable(String w1, String w2, String w3) {
        word1 = w1.toUpperCase();
        word2 = w2.toUpperCase();
        word3 = w3.toUpperCase();

        // Reset static state
        Arrays.fill(used, false);
        assignment.clear();
        letters.clear();
        leadingLetters.clear();

        // Collect unique letters
        Set<Character> uniqueChars = new HashSet<>();
        for (char c : (word1 + word2 + word3).toCharArray()) {
            uniqueChars.add(c);
        }
        letters.addAll(uniqueChars);

        // Mark leading letters (cannot be zero)
        if (!word1.isEmpty()) leadingLetters.add(word1.charAt(0));
        if (!word2.isEmpty()) leadingLetters.add(word2.charAt(0));
        if (!word3.isEmpty()) leadingLetters.add(word3.charAt(0));

        // If more than 10 unique letters → impossible
        if (letters.size() > 10) {
            return false;
        }

        // Start backtracking
        return backtrack(0);
    }

    private static boolean backtrack(int idx) {
        if (idx == letters.size()) {
            // All letters assigned, check equation
            return isValid();
        }

        char letter = letters.get(idx);
        for (int digit = 0; digit <= 9; digit++) {
            // Skip if digit already used
            if (used[digit]) continue;

            // Leading letter cannot be zero
            if (digit == 0 && leadingLetters.contains(letter)) continue;

            // Assign
            assignment.put(letter, digit);
            used[digit] = true;

            if (backtrack(idx + 1)) {
                return true;
            }

            // Unassign
            used[digit] = false;
            assignment.remove(letter);
        }
        return false;
    }

    private static boolean isValid() {
        try {
            long val1 = toNumber(word1);
            long val2 = toNumber(word2);
            long val3 = toNumber(word3);

            // Avoid overflow: DEBUG could be up to 5 digits → max ~99999
            return val1 + val2 == val3;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static long toNumber(String word) {
        if (word.isEmpty()) return 0;
        long num = 0;
        for (char c : word.toCharArray()) {
            num = num * 10 + assignment.get(c);
        }
        // Check leading zero: if word has more than one char and first digit is 0 → invalid
        if (word.length() > 1 && assignment.get(word.charAt(0)) == 0) {
            throw new NumberFormatException("Leading zero");
        }
        return num;
    }

    // ==================== Test Cases ====================

    public static void main(String[] args) {
        // Test Case 1: "SEND" + "MORE" = "MONEY" → Classic solvable puzzle
        System.out.println("Test 1: SEND + MORE = MONEY");
        boolean result1 = isSolvable("SEND", "MORE", "MONEY");
        System.out.println("Solvable: " + (result1 ? " Yes" : " No"));
        if (result1) printAssignment();
        System.out.println();

        // Test Case 2: "CODE" + "BUG" = "DEBUG"
        System.out.println("Test 2: CODE + BUG = DEBUG");
        boolean result2 = isSolvable("CODE", "BUG", "DEBUG");
        System.out.println("Solvable: " + (result2 ? " Yes" : " No"));
        if (result2) printAssignment();
        else System.out.println("No valid assignment found.");
        System.out.println();

        // Test Case 3: "A" + "A" = "B" → A=1, B=2 → 1+1=2 → valid
        System.out.println("Test 3: A + A = B");
        boolean result3 = isSolvable("A", "A", "B");
        System.out.println("Solvable: " + (result3 ? " Yes" : " No"));
        if (result3) printAssignment();
        System.out.println();

        // Test Case 4: "AA" + "AA" = "CC" → 11+11=22 → A=1, C=2 → valid
        System.out.println("Test 4: AA + AA = CC");
        boolean result4 = isSolvable("AA", "AA", "CC");
        System.out.println("Solvable: " + (result4 ? " Yes" : " No"));
        if (result4) printAssignment();
        System.out.println();

        // Test Case 5: "S" + "T" = "A" → many solutions, e.g., S=1,T=2,A=3
        System.out.println("Test 5: S + T = A");
        boolean result5 = isSolvable("S", "T", "A");
        System.out.println("Solvable: " + (result5 ? " Yes" : " No"));
        if (result5) printAssignment();
        System.out.println();

        // Test Case 6: "STAR" + "MOON" = "NIGHT"
        System.out.println("Test 6: STAR + MOON = NIGHT");
        boolean result6 = isSolvable("STAR", "MOON", "NIGHT");
        System.out.println("Solvable: " + (result6 ? " Yes" : " No"));
        if (result6) printAssignment();
        else System.out.println("No valid assignment found.");
        System.out.println();
    }

    private static void printAssignment() {
        System.out.print("Assignment: ");
        List<String> pairs = new ArrayList<>();
        for (var entry : assignment.entrySet()) {
            pairs.add(entry.getKey() + "=" + entry.getValue());
        }
        Collections.sort(pairs); // sorted order
        System.out.println(String.join(", ", pairs));
    }
}


//output
// Test 1: SEND + MORE = MONEY
// Solvable:  Yes
// Assignment: D=7, E=5, M=1, N=6, O=0, R=8, S=9, Y=2

// Test 2: CODE + BUG = DEBUG
// Solvable:  No
// No valid assignment found.

// Test 3: A + A = B
// Solvable:  Yes
// Assignment: A=1, B=2

// Test 4: AA + AA = CC
// Solvable:  Yes
// Assignment: A=1, C=2

// Test 5: S + T = A
// Solvable:  Yes
// Assignment: A=3, S=1, T=2

// Test 6: STAR + MOON = NIGHT
// Solvable:  Yes
// Assignment: A=2, G=4, H=7, I=0, M=6, N=1, O=5, R=8, S=3, T=9