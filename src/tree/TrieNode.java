package tree;

/**
 * Classe représentant un noeud de la structure en arbre Trie (destiné à l'arbe
 * lexicographique).
 */
public final class TrieNode {

	private final char letter;
	private TrieNode[] children;
	private boolean isEndWord = false;

	/**
	 * Crée un noeud de la structure en arbre Trie.
	 * 
	 * @param letter la lettre représentée par le noeud
	 */
	public TrieNode(char letter) {
		this.letter = letter;
	}

	/**
	 * Ajoute un noeud à l'arbre Trie.
	 * 
	 * @param node le noeud Trie à ajouter
	 */
	public void addChild(TrieNode node) {
		/*
		 * Pour consommer moins d'espace (CST), on initialise le tableau de 28
		 * caractères le plus tard possible (Lazy-Loading).
		 */
		if (this.children == null) {
			this.children = new TrieNode[28];
		}
		if (node.letter == '-') {
			this.children[26] = node;
		} else if (node.letter == '\'') {
			this.children[27] = node;
		} else {
			this.children[node.letter - 'a'] = node;
		}
	}

	/**
	 * Récupère un noeud enfant selon une position donnée (comprise en 0 et 25
	 * compris).
	 * 
	 * @param index la position recherhée
	 * @return le noeud
	 */
	public TrieNode getChildren(int index) {
		if (this.children == null) {
			return null;
		}
		return this.children[index];
	}

	/**
	 * Récupère un noeud enfant selon une lettre donnée (s'il existe).
	 * 
	 * @param c le caractère recherché
	 * @return le noeud enfant correspondant au caractère recherché
	 */
	public TrieNode getChildren(char c) {
		if (this.children == null) {
			return null;
		}
		return this.children[c == '-' ? 26 : c == '\'' ? 27 : c - 'a'];
	}

	/**
	 * @return la valeur {@code true} si le noeud marque la fin d'un mot,
	 *         {@code false} sinon
	 */
	public boolean isEndWord() {
		return this.isEndWord;
	}

	/**
	 * Marque le noeud en tant que fin d'un mot.
	 */
	public void markAsEndWord() {
		isEndWord = true;
	}

	/**
	 * @return la lettre du noeud
	 */
	public char getLetter() {
		return this.letter;
	}
}