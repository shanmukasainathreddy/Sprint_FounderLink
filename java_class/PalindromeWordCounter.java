public class PalindromeWordCounter {

    // Method to check if a word is palindrome
    public static boolean isPalindrome(String word) {
        int left = 0, right = word.length() - 1;

        while (left < right) {
            if (word.charAt(left) != word.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    // Method to check if word contains all specified characters
    public static boolean containsSpecifiedChars(String word, String specifiedChars) {
        for (char ch : specifiedChars.toCharArray()) {
            if (word.indexOf(ch) == -1) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String paragraph = "madam level civic radar apple refer noon";
        String specifiedChars = "a"; // characters to be present

        String[] words = paragraph.toLowerCase().split("\\s+");
        int count = 0;

        for (String word : words) {
            if (isPalindrome(word) && containsSpecifiedChars(word, specifiedChars)) {
                count++;
            }
        }

        System.out.println("Count of palindrome words with specified characters: " + count);
    }
}
