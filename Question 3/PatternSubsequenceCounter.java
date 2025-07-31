// A pattern sequence is formed by taking a base pattern and repeating it a given number of times.
// For example, if pattern = "xyz" and times = 5, the resulting sequence is: xyzxyzxyzxyzxyz
// We define that one sequence can be derived from another if we can remove some characters (without
// reordering) to form the target sequence.
// You are given two base patterns p1 and p2, and two integers’ t1 and t2.
//  seqA = [p1, t1] is the sequence formed by repeating p1 exactly t1 times.
//  seqB = [p2, t2] is the sequence formed by repeating p2 exactly t2 times.
// Fi Input:
// p1 = "bca"
// t1 = 6
// p2 = "ba"
// t2 = 3
// Output: 3.
// Explanation
// We are given:
// p1 = "bca", t1 = 6
// This means we repeat "bca" 6 times to form seqA.
// So, seqA = "bcabcabcabcabcabcabc"
// p2 = "ba", t2 = 3
// This means we need to check how many times "ba" can be extracted from seqA.
// t2 = 3 means that in the original problem, we want to form seqB = "ba" × 3 = "babababa".
// We need to check whether this is possible by removing some characters (while keeping order) from
// seqA.
// We need to extract "ba" as many times as possible while maintaining order.
// Let's check where "ba" appears:
// 1. First "ba" → "bcabcabcabcabcabcabc" → Use 'b' at index 0, 'a' at index 2
// o Remaining sequence: "bcabcabcabcabcabc"
// 2. Second "ba" → "bcabcabcabcabcabc" → Use 'b' at index 3, 'a' at index 5
// o Remaining sequence: "bcabcabcabcabc"
// 3. Third "ba" → "bcabcabcabcabc" → Use 'b' at index 6, 'a' at index 8
// o Remaining sequence: "bcabcabcabc"
// At this point, there are not enough 'b' and 'a' pairs left to form another "ba."
// Thus, we cannot form more than 3 instances of "ba", so the maximum x = 3.
// The output represents the maximum number of full p2 = "ba" sequences that can be extracted from
// seqA.
// Even though t2 = 3, which means we ideally want 3 copies of "ba", we solve the problem to find out if
// that is possible. In this case, the answer is yes, so the output is 3.
// If t2 = 5, we would try to extract "ba" 5 times, but we would fail because we can only extract it 3
// times. In that case, the output would be 3, since it's the maximum possible.






//Solution
public class PatternSubsequenceCounter {

    /**
     * Counts the maximum number of times p2 can appear as a subsequence
     * in the string formed by repeating p1 exactly t1 times.
     *
     * @param p1 Base pattern to repeat
     * @param t1 Number of times to repeat p1
     * @param p2 Pattern to extract as subsequence
     * @param t2 Target number of times (used for context, but output is maximum possible)
     * @return Maximum number of times p2 can be extracted as a subsequence from repeated p1
     */
    public static int maxSubsequenceCount(String p1, long t1, String p2, long t2) {
        if (p1 == null || p2 == null || p1.isEmpty() || p2.isEmpty() || t1 == 0) {
            return 0;
        }

        int n = p1.length();
        int m = p2.length();

        int totalCount = 0;
        int j = 0; // current position in p2

        // Simulate t1 repetitions of p1
        for (long rep = 0; rep < t1; rep++) {
            for (int i = 0; i < n; i++) {
                char c = p1.charAt(i);
                if (c == p2.charAt(j)) {
                    j++;
                    if (j == m) {
                        totalCount++;
                        j = 0; // reset to allow overlapping matches
                    }
                }
            }
        }

        return totalCount;
    }

    // ==================== Test Cases ====================

    public static void main(String[] args) {
        // Test Case 1: Given example (but corrected)
        System.out.println("Test Case 1: p1='bca', t1=6, p2='ba', t2=3");
        int result1 = maxSubsequenceCount("bca", 6, "ba", 3);
        System.out.println("Expected (corrected): 6, Got: " + result1);
        System.out.println("Pass: " + (result1 == 6 ? "Yes" : "No"));
        System.out.println();

        // Test Case 2: p2 not possible
        System.out.println("Test Case 2: p1='abc', t1=3, p2='xyz', t2=1");
        int result2 = maxSubsequenceCount("abc", 3, "xyz", 1);
        System.out.println("Result: " + result2);
        System.out.println("Pass: " + (result2 == 0 ? "Yes" : "No"));
        System.out.println();

        // Test Case 3: p2 = "aa", p1 = "aa", t1 = 3 → "aaaaaa"
        System.out.println("Test Case 3: p1='aa', t1=3, p2='aa'");
        int result3 = maxSubsequenceCount("aa", 3, "aa", 2);
        // Can we extract "aa" multiple times?
        // a0,a1 → 1st
        // a2,a3 → 2nd
        // a4,a5 → 3rd
        // Also a0,a2 etc., but greedy catches 3
        System.out.println("Result: " + result3);
        System.out.println("Pass: " + (result3 == 3 ? "Yes" : "No"));
        System.out.println();

      

        // Test Case 4: Single char p2
        System.out.println("Test Case 4: p1='abc', t1=2, p2='b'");
        int result5 = maxSubsequenceCount("abc", 2, "b", 2);
        // 'b' appears once per "abc" → 2 times
        System.out.println("Result: " + result5);
        System.out.println("Pass: " + (result5 == 2 ? "Yes" : "No"));
        System.out.println();

     
    }
}

//output
// Test Case 1: p1='bca', t1=6, p2='ba', t2=3
// Expected (corrected): 6, Got: 6
// Pass: Yes

// Test Case 2: p1='abc', t1=3, p2='xyz', t2=1
// Result: 0
// Pass: Yes

// Test Case 3: p1='aa', t1=3, p2='aa'
// Result: 3
// Pass: Yes

// Test Case 4: p1='abc', t1=2, p2='b'
// Result: 2
// Pass: Yes