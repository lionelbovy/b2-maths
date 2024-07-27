package tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public final class LexicographicTree {

	private final TrieNode root;
	private int size = 0;

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Constructor : creates an empty lexicographic tree.
	 */
	public LexicographicTree() {
		root = new TrieNode('\0');
	}

	/**
	 * Constructor : creates a lexicographic tree populated with words
	 * 
	 * @param filename A text file containing the words to be inserted in the tree
	 */
	public LexicographicTree(String filename) {
		this();
		try (BufferedReader reader = Files.newBufferedReader(Path.of(filename))) {
			String line;
			while ((line = reader.readLine()) != null) {
				insertWord(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * PUBLIC METHODS
	 */

	/**
	 * Returns the number of words present in the lexicographic tree.
	 * 
	 * @return The number of words present in the lexicographic tree
	 */
	public int size() {
		return size;
	}

	/**
	 * Inserts a word in the lexicographic tree if not already present.
	 * 
	 * @param word A word
	 */
	public void insertWord(String word) {
		TrieNode currentNode = root;
		boolean hasNewNode = false;

		for (char c : word.toCharArray()) {
			TrieNode child = currentNode.getChildren(c);
			if (child == null) {
				var newNode = new TrieNode(c);
				currentNode.addChild(newNode);
				currentNode = newNode;
				hasNewNode = true;
			} else {
				currentNode = child;
			}
		}

		if (hasNewNode) {
			size++;
		}
		currentNode.markAsEndWord();
	}

	/**
	 * Determines if a word is present in the lexicographic tree.
	 * 
	 * @param word A word
	 * @return True if the word is present, false otherwise
	 */
	public boolean containsWord(String word) {
		TrieNode currentNode = root;
		for (char c : word.toCharArray()) {
			TrieNode child = currentNode.getChildren(c);
			if (child == null) {
				return false;
			}
			currentNode = child;
		}
		return currentNode.isEndWord();
	}

	/**
	 * Returns an alphabetic list of all words starting with the supplied prefix. If
	 * 'prefix' is an empty string, all words are returned.
	 * 
	 * @param prefix Expected prefix
	 * @return The list of words starting with the supplied prefix
	 */
	public List<String> getWords(String prefix) {
		List<String> words = new ArrayList<>();
		TrieNode currentNode = root;

		// Recherche/positionnement du noeud correspondant au préfixe
		for (char c : prefix.toCharArray()) {
			TrieNode child = currentNode.getChildren(c);
			if (child == null) {
				return words; // Préfixe non trouvé, retourne une liste vide
			}
			currentNode = child;
		}

		// Exploration récursive pour récupérer tous les mots ayant le préfixe
		getWordsByPrefixRecursively(currentNode, prefix, words);
		return words;
	}

	/**
	 * Permet de savoir s'il est possible de former un chemin dans l'arbre à l'aide
	 * d'un préfixe donné.
	 * 
	 * @param prefix le préfixe pour lequel on souhaiterait savoir s'il existe un
	 *               chemin
	 * @return la valeur {@code true} s'il est possible de former un chemin,
	 *         {@code false} sinon
	 */
	public boolean containsPrefix(String prefix) {
		TrieNode currentNode = root;
		for (char c : prefix.toCharArray()) {
			TrieNode child = currentNode.getChildren(c);
			if (child == null) {
				return false;
			}
			currentNode = child;
		}
		return true;
	}

	/**
	 * Returns an alphabetic list of all words of a given length. If 'length' is
	 * lower than or equal to zero, an empty list is returned.
	 * 
	 * @param length Expected word length
	 * @return The list of words with the given length
	 */
	public List<String> getWordsOfLength(int length) {
		List<String> words = new ArrayList<>();
		if (length > 0) {
			getWordsOfLengthRecursively(root, "", words, length);
		}
		return words;
	}

	/*
	 * PRIVATE METHODS
	 */

	/**
	 * Ajoute, de façon récursive, les mots à une collection de mots trouvés.
	 * 
	 * @param node        le noeud à analyser
	 * @param currentWord le mot construit ou en cours de construction
	 * @param words       la collection de mots à laquelle ajouter les mots trouvés
	 * @param length      la longueur du mot désirée
	 */
	private void getWordsOfLengthRecursively(TrieNode node, String currentWord, Collection<String> words, int length) {
		if (currentWord.length() == length) { // Cas de base
			if (node.isEndWord()) { // Cas "favorable", un mot de la longueur souhaitée et terminal a été trouvé
				words.add(currentWord);
			}
			return;
		}

		for (int i = 0; i < 28; i++) { // On boucle sur l'ensemble de l'alphabet (plus les caractères ' et -)
			TrieNode child = node.getChildren(i);
			if (child != null) {
				String newWord = currentWord + child.getLetter();
				getWordsOfLengthRecursively(child, newWord, words, length);
			}
		}
	}

	/**
	 * Ajoute, de façon récursive, tous les mots commençant par un préfixe donné.
	 * 
	 * @param node   le noeud courant à analyser
	 * @param prefix le préfixe de départ ou en cours de construction
	 * @param words  une collection des mots trouvés
	 */
	private void getWordsByPrefixRecursively(TrieNode node, String prefix, List<String> words) {
		if (node.isEndWord()) {
			words.add(prefix);
		}
		for (int i = 0; i < 28; i++) {
			TrieNode child = node.getChildren(i);
			if (child != null) {
				getWordsByPrefixRecursively(child, prefix + child.getLetter(), words);
			}
		}
	}

	/*
	 * TEST FUNCTIONS
	 */

	private static String numberToWordBreadthFirst(long number) {
		String word = "";
		int radix = 13;
		do {
			word = (char) ('a' + (int) (number % radix)) + word;
			number = number / radix;
		} while (number != 0);
		return word;
	}

	private static void testDictionaryPerformance(String filename) {
		long startTime;
		int repeatCount = 20;

		// Create tree from list of words
		startTime = System.currentTimeMillis();
		System.out.println("Loading dictionary...");
		LexicographicTree dico = null;
		for (int i = 0; i < repeatCount; i++) {
			dico = new LexicographicTree(filename);
		}
		System.out.println("Load time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println("Number of words : " + dico.size());
		System.out.println();

		// Search existing words in dictionary
		startTime = System.currentTimeMillis();
		System.out.println("Searching existing words in dictionary...");
		File file = new File(filename);
		for (int i = 0; i < repeatCount; i++) {
			Scanner input;
			try {
				input = new Scanner(file);
				while (input.hasNextLine()) {
					String word = input.nextLine();
					boolean found = dico.containsWord(word);
					if (!found) {
						System.out.println(word + " / " + word.length() + " -> " + found);
					}
				}
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Search time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println();

		// Search non-existing words in dictionary
		startTime = System.currentTimeMillis();
		System.out.println("Searching non-existing words in dictionary...");
		for (int i = 0; i < repeatCount; i++) {
			Scanner input;
			try {
				input = new Scanner(file);
				while (input.hasNextLine()) {
					String word = input.nextLine() + "xx";
					boolean found = dico.containsWord(word);
					if (found) {
						System.out.println(word + " / " + word.length() + " -> " + found);
					}
				}
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Search time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println();

		// Search words of increasing length in dictionary
		startTime = System.currentTimeMillis();
		System.out.println("Searching for words of increasing length...");
		for (int i = 0; i < 4; i++) {
			int total = 0;
			for (int n = 0; n <= 28; n++) {
				int count = dico.getWordsOfLength(n).size();
				total += count;
			}
			if (dico.size() != total) {
				System.out.printf("Total mismatch : dict size = %d / search total = %d\n", dico.size(), total);
			}
		}
		System.out.println("Search time : " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println();
	}

	private static void testDictionarySize() {
		final int MB = 1024 * 1024;
		System.out.print(Runtime.getRuntime().totalMemory() / MB + " / ");
		System.out.println(Runtime.getRuntime().maxMemory() / MB);

		LexicographicTree dico = new LexicographicTree();
		long count = 0;
		while (true) {
			dico.insertWord(numberToWordBreadthFirst(count));
			count++;
			if (count % MB == 0) {
				System.out.println(count / MB + "M -> " + Runtime.getRuntime().freeMemory() / MB);
			}
		}
	}

	/*
	 * MAIN PROGRAM
	 */

	public static void main(String[] args) {
		// CTT : test de performance insertion/recherche
		testDictionaryPerformance("mots/dictionnaire_FR_sans_accents.txt");

		// CST : test de taille maximale si VM -Xms2048m -Xmx2048m
		testDictionarySize();
	}
}
