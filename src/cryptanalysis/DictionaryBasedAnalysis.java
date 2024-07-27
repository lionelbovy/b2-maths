package cryptanalysis;

import tree.LexicographicTree;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class DictionaryBasedAnalysis {
	private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String DICTIONARY = "mots/dictionnaire_FR_sans_accents.txt";
	private static final String CRYPTOGRAM_FILE = "txt/Plus fort que Sherlock Holmes (cryptogram).txt";
	private static final String DECODING_ALPHABET = "VNSTBIQLWOZUEJMRYGCPDKHXAF"; // Sherlock

	private final String cryptogram;
	private final LexicographicTree dict;

	/*
	 * CONSTRUCTOR
	 */
	public DictionaryBasedAnalysis(String cryptogram, LexicographicTree dict) {
		this.cryptogram = cryptogram;
		this.dict = dict;
	}

	/*
	 * PUBLIC METHODS
	 */

	/**
	 * Performs a dictionary-based analysis of the cryptogram and returns an
	 * approximated decoding alphabet.
	 *
	 * @param alphabet The decoding alphabet from which the analysis starts
	 * @return The decoding alphabet at the end of the analysis process
	 */
	public String guessApproximatedAlphabet(String alphabet) {
		List<String> originalWords = new ArrayList<>(getCryptogramWordsByDescendingOrder());
		String approxAlphabet = alphabet;
		int score = 0, bestScore = 0, wordLength = 0;

		Queue<String> queue = new LinkedList<>(originalWords);
		String currentWord = queue.poll();
		List<String> cache = new ArrayList<>();
		while (currentWord != null) {
			if (wordLength != currentWord.length()) {
				System.out.println(">>> Words of length " + currentWord.length());
				cache = dict.getWordsOfLength(currentWord.length());
				wordLength = currentWord.length();
			}

			String invalidDecoded = applySubstitution(currentWord, approxAlphabet);
			if (dict.containsWord(invalidDecoded.toLowerCase())) {
				currentWord = queue.poll();
				continue;
			}
			String potentialCandidate = findPotentialCandidate(invalidDecoded, cache);

			String newApproxAlphabet = getNewApproxAlphabet(approxAlphabet, invalidDecoded, potentialCandidate);
			for (String w : originalWords) {
				if (dict.containsWord(applySubstitution(w, newApproxAlphabet).toLowerCase())) {
					score++;
				}
			}

			if (score > bestScore) {
				bestScore = score;
				System.out.printf("%-20s -> %s\n", "Cryptogram word", currentWord);
				System.out.printf("%-20s -> %s\n", "Invalid decoded word", invalidDecoded);
				System.out.printf("%-20s -> %s\n", "Candidate word", potentialCandidate);
				System.out.println();
				System.out.printf("%-20s -> %s\n", "Standard alphabet", alphabet);
				System.out.printf("%-20s -> %s\n", "Approximated alphabet", approxAlphabet);
				approxAlphabet = newApproxAlphabet;
				System.out.printf("%-20s -> %s\n", "New approxim alphabet", newApproxAlphabet);
				System.out.println();
				System.out.printf("=> Score decoded : words = %d / valid = %d / invalid = %d\n", originalWords.size(),
						bestScore, originalWords.size() - bestScore);
				System.out.println();
			}
			score = 0;
			currentWord = queue.poll();
		}
		return approxAlphabet;
	}

	/**
	 * Applies an alphabet-specified substitution to a text.
	 *
	 * @param text     A text
	 * @param alphabet A substitution alphabet
	 * @return The substituted text
	 */
	public static String applySubstitution(String text, String alphabet) {
		StringBuilder substitutedText = new StringBuilder();

		for (char c : text.toCharArray()) {
			if (Character.isLetter(c)) {
				substitutedText.append(alphabet.charAt(c - 'A'));
			} else {
				substitutedText.append(c);
			}
		}

		return substitutedText.toString();
	}

	/*
	 * PRIVATE METHODS
	 */
	private List<String> getCryptogramWordsByDescendingOrder() {
		return Stream.of(this.cryptogram.split("[\\s*]"))
				.distinct()
				.filter(w -> w.length() >= 3)
				.sorted(Comparator.comparingInt(String::length)
						.reversed()
						.thenComparing(Comparator.naturalOrder()))
				.toList();
	}

	protected static boolean comparePattern(String word1, String word2) {
	    for (int i = 0; i < word1.length(); i++) {
	        if (word1.indexOf(word1.substring(i, i + 1), i + 1) != word2.indexOf(word2.substring(i, i + 1), i + 1))
	            return false;
	    }
	    return true;
	}

	private String findPotentialCandidate(String word, List<String> candidates) {
		for (String candidate : candidates) {
			if (comparePattern(word, candidate)) {
				return candidate.toUpperCase();
			}
		}
		return "";
	}

	private String getNewApproxAlphabet(String approxAlphabet, String invalidDecoded, String potentialCandidate) {
		String result = approxAlphabet;
		for (int i = 0; i < potentialCandidate.length(); i++) {
			result = swapLetters(result, invalidDecoded.charAt(i), potentialCandidate.charAt(i));
		}
		return result;
	}

	private String swapLetters(String alphabet, char letter1, char letter2) {
	    int index1 = alphabet.indexOf(letter1);
	    int index2 = alphabet.indexOf(letter2);

	    if (index1 == -1 || index2 == -1 || letter1 == letter2) {
	        // l'une des lettres n'est pas présente dans l'alphabet
	        return alphabet;
	    }

	    char[] chars = alphabet.toCharArray();
	    chars[index1] = letter2;
	    chars[index2] = letter1;

	    return new String(chars);
	}

	/**
	 * Compares two substitution alphabets.
	 *
	 * @param a First substitution alphabet
	 * @param b Second substitution alphabet
	 * @return A string where differing positions are indicated with an 'x'
	 */
	private static String compareAlphabets(String a, String b) {
		String result = "";
		for (int i = 0; i < a.length(); i++) {
			result += (a.charAt(i) == b.charAt(i)) ? " " : "x";
		}
		return result;
	}

	/**
	 * Load the text file pointed to by pathname into a String.
	 *
	 * @param pathname A path to text file.
	 * @param encoding Character set used by the text file.
	 * @return A String containing the text in the file.
	 * @throws IOException
	 */
	private static String readFile(String pathname, Charset encoding) {
		String data = "";
		try {
			data = Files.readString(Paths.get(pathname), encoding);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	/*
	 * MAIN PROGRAM
	 */

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		/*
		 * Load dictionary
		 */
		System.out.print("Loading dictionary... ");
		LexicographicTree dict = new LexicographicTree(DICTIONARY);
		System.out.println("done.");
		System.out.println("Duration : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println("Number of words : " + dict.size());
		System.out.println();
		System.out.println("-----------------------------------------------------------------");

		/*
		 * Load cryptogram
		 */
		String cryptogram = readFile(CRYPTOGRAM_FILE, StandardCharsets.UTF_8);
//		System.out.println("*** CRYPTOGRAM ***\n" + cryptogram.substring(0, 100));
//		System.out.println();

		/*
		 * Decode cryptogram
		 */
		DictionaryBasedAnalysis dba = new DictionaryBasedAnalysis(cryptogram, dict);
//		String startAlphabet = LETTERS;
		String startAlphabet = "ZISHNFOBMAVQLPEUGWXTDYRJKC"; // Random alphabet
		startTime = System.currentTimeMillis();
		String finalAlphabet = dba.guessApproximatedAlphabet(startAlphabet);

		// Display final results
		System.out.println();
		System.out.println("Decoding     alphabet : " + DECODING_ALPHABET);
		System.out.println("Approximated alphabet : " + finalAlphabet);
		System.out.println("Remaining differences : " + compareAlphabets(DECODING_ALPHABET, finalAlphabet));
		System.out.println();
		System.out.println("Analysis duration : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println();

		// Display decoded text
		System.out.println("*** DECODED TEXT ***\n" + applySubstitution(cryptogram, finalAlphabet).substring(0, 200));
		System.out.println();
	}
}
