/*******************************************************************************************
 * setup softkeyboard(2Touch)
 */
package net.gorry.android.input.nicownng.JAJP;

import java.util.HashMap;

import net.gorry.android.input.nicownng.DefaultSoftKeyboard;
import net.gorry.android.input.nicownng.MyHeightKeyboard;
import net.gorry.android.input.nicownng.NicoWnnG;
import net.gorry.android.input.nicownng.R;
import android.inputmethodservice.Keyboard;

public class SetupKeyboard2Touch extends SetupKeyboard {

	/*********************************
	 *
	 */
	@Override
	protected int[] getSelectedKeyboard(final int n) {
		switch (n) {
			case SELECT_SUBTEN_PORT_KEY_TABLE_3:
				return selectSubTenPortKeyTable3;
			case SELECT_SUBTEN_LAND_KEY_TABLE_3:
				return selectSubTenLandKeyTable3;
			case SELECT_SUBTEN_PORT_KEY_TABLE_2:
				return selectSubTenPortKeyTable2;
			case SELECT_SUBTEN_LAND_KEY_TABLE_2:
				return selectSubTenLandKeyTable2;
			case SELECT_FLICK_PORT_KEY_TABLE:
				return selectFlickPortKeyTable;
			case SELECT_FLICK_LAND_KEY_TABLE:
				return selectFlickLandKeyTable;
			case SELECT_PORT_KEY_TABLE:
				return selectPortKeyTable;
			case SELECT_LAND_KEY_TABLE:
				return selectLandKeyTable;
		}
		return super.getSelectedKeyboard(n);
	}

	/*********************************
	 *
	 */
	@Override
	public String[][][][] SetupCycleTable() {
		return JP_FULL_2TOUCH_CYCLE_TABLE;
	}
	/*********************************
	 *
	 */
	@Override
	public HashMap<String, String> SetupReplaceTable(final boolean du) {
		return (du ? JP_FULL_2TOUCH_REPLACE_TABLE_2 : JP_FULL_2TOUCH_REPLACE_TABLE_1);
	}
	/*********************************
	 *
	 */
	@Override
	public int SetupIcon() {
		return R.drawable.immodeic_2touch;
	}
	/*********************************
	 *
	 */
	@Override
	public int SetupModeKeyString() {
		return R.string.key_12key_mode_2touch;
	}
	/*********************************
	 *
	 */
	@Override
	public int GetFlickChangeMap(final int keymode, final int line, final int row) {
		switch (keymode) {
		  case DefaultSoftKeyboard.KEYMODE_JA_FULL_NICO:
			return flickHiraganaChangeMap[line][row];
		  case DefaultSoftKeyboard.KEYMODE_JA_FULL_NICO_KATAKANA:
			return flickHiraganaChangeMap[line][row];
		  case DefaultSoftKeyboard.KEYMODE_JA_HALF_NICO_KATAKANA:
			return flickHiraganaChangeMap[line][row];
		  case DefaultSoftKeyboard.KEYMODE_JA_HALF_ALPHABET:
			return flickAlphabetChangeMap[line][row];
		  case DefaultSoftKeyboard.KEYMODE_JA_FULL_ALPHABET:
		  default:
			return flickAlphabetChangeMap[line][row];
		}
	}
	/*********************************
	 *
	 */
	@Override
	public int GetCycleTableColumns() {
		return 10;
	}



	/** Toggle cycle table for full-width Nicotouch */
	private static final String[][][][] JP_FULL_2TOUCH_CYCLE_TABLE = {
		{
			{
				{"\u3042"}, {"\u3044"}, {"\u3046"}, {"\u3048"}, {"\u304a"},      {"a"},      {"b"},      {"c"},      {"d"}, {"e"},
				{"\u304b"}, {"\u304d"}, {"\u304f"}, {"\u3051"}, {"\u3053"},      {"f"},      {"g"},      {"h"},      {"i"}, {"j"},
				{"\u3055"}, {"\u3057"}, {"\u3059"}, {"\u305b"}, {"\u305d"},      {"k"},      {"l"},      {"m"},      {"n"}, {"o"},
				{"\u305f"}, {"\u3061"}, {"\u3064"}, {"\u3066"}, {"\u3068"},      {"p"},      {"q"},      {"r"},      {"s"}, {"t"},
				{"\u306a"}, {"\u306b"}, {"\u306c"}, {"\u306d"}, {"\u306e"},      {"u"},      {"v"},      {"w"},      {"x"}, {"y"},
				{"\u306f"}, {"\u3072"}, {"\u3075"}, {"\u3078"}, {"\u307b"},      {"z"},      {"?"},      {"!"},      {"\u30fc"}, {"/"},
				{"\u307e"}, {"\u307f"}, {"\u3080"}, {"\u3081"}, {"\u3082"},     {"\\"},      {"&"},      {" "},      {"="}, {"\u3001"},
				{"\u3084"},      {"("}, {"\u3086"},      {")"}, {"\u3088"},      {"*"},      {"#"},      {" "},      {"@"}, {"\u3002"},
				{"\u3089"}, {"\u308a"}, {"\u308b"}, {"\u308c"}, {"\u308d"},      {"1"},      {"2"},      {"3"},      {"4"}, {"5"},
				{"\u308f"}, {"\u3092"}, {"\u3093"}, {"\u309b"}, {"\u309c"},      {"6"},      {"7"},      {"8"},      {"9"}, {"0"},
				{"#"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "},
				{"*"}, {"\uff9e"}, {" "}, {"\uff9f"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}
			},
			{
				{"\u3042"}, {"\u3044"}, {"\u3046"}, {"\u3048"}, {"\u304a"},      {"A"},      {"B"},      {"C"},      {"D"}, {"E"},
				{"\u304b"}, {"\u304d"}, {"\u304f"}, {"\u3051"}, {"\u3053"},      {"F"},      {"G"},      {"H"},      {"I"}, {"J"},
				{"\u3055"}, {"\u3057"}, {"\u3059"}, {"\u305b"}, {"\u305d"},      {"K"},      {"L"},      {"M"},      {"N"}, {"O"},
				{"\u305f"}, {"\u3061"}, {"\u3064"}, {"\u3066"}, {"\u3068"},      {"P"},      {"Q"},      {"R"},      {"S"}, {"T"},
				{"\u306a"}, {"\u306b"}, {"\u306c"}, {"\u306d"}, {"\u306e"},      {"U"},      {"V"},      {"W"},      {"X"}, {"Y"},
				{"\u306f"}, {"\u3072"}, {"\u3075"}, {"\u3078"}, {"\u307b"},      {"Z"},      {"?"},      {"!"},      {"\u30fc"}, {"/"},
				{"\u307e"}, {"\u307f"}, {"\u3080"}, {"\u3081"}, {"\u3082"},     {"\\"},      {"&"},      {" "},      {"="}, {"\u3001"},
				{"\u3084"},      {"("}, {"\u3086"},      {")"}, {"\u3088"},      {"*"},      {"#"},      {" "},      {"@"}, {"\u3002"},
				{"\u3089"}, {"\u308a"}, {"\u308b"}, {"\u308c"}, {"\u308d"},      {"1"},      {"2"},      {"3"},      {"4"}, {"5"},
				{"\u308f"}, {"\u3092"}, {"\u3093"}, {"\u309b"}, {"\u309c"},      {"6"},      {"7"},      {"8"},      {"9"}, {"0"},
				{"#"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "},
				{"*"}, {"\uff9e"}, {" "}, {"\uff9f"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}
			}
		},
		{
			{
				{"\u30a2"}, {"\u30a4"}, {"\u30a6"}, {"\u30a8"}, {"\u30aa"},      {"a"},      {"b"},      {"c"},      {"d"}, {"e"},
				{"\u30ab"}, {"\u30ad"}, {"\u30af"}, {"\u30b1"}, {"\u30b3"},      {"f"},      {"g"},      {"h"},      {"i"}, {"j"},
				{"\u30b5"}, {"\u30b7"}, {"\u30b9"}, {"\u30bb"}, {"\u30bd"},      {"k"},      {"l"},      {"m"},      {"n"}, {"o"},
				{"\u30bf"}, {"\u30c1"}, {"\u30c4"}, {"\u30c6"}, {"\u30c8"},      {"p"},      {"q"},      {"r"},      {"s"}, {"t"},
				{"\u30ca"}, {"\u30cb"}, {"\u30cc"}, {"\u30cd"}, {"\u30ce"},      {"u"},      {"v"},      {"w"},      {"x"}, {"y"},
				{"\u30cf"}, {"\u30d2"}, {"\u30d5"}, {"\u30d8"}, {"\u30db"},      {"z"},      {"?"},      {"!"},      {"\u30fc"}, {"/"},
				{"\u30de"}, {"\u30df"}, {"\u30e0"}, {"\u30e1"}, {"\u30e2"},     {"\\"},      {"&"},      {" "},      {"="}, {"\u3001"},
				{"\u30e4"},      {"("}, {"\u30e6"},      {")"}, {"\u30e8"},      {"*"},      {"#"},      {" "},      {"@"}, {"\u3002"},
				{"\u30e9"}, {"\u30ea"}, {"\u30eb"}, {"\u30ec"}, {"\u30ed"},      {"1"},      {"2"},      {"3"},      {"4"}, {"5"},
				{"\u30ef"}, {"\u30f2"}, {"\u30f3"}, {"\u309b"}, {"\u309c"},      {"6"},      {"7"},      {"8"},      {"9"}, {"0"},
				{"#"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "},
				{"*"}, {"\uff9e"}, {" "}, {"\uff9f"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}
			},
			{
				{"\u30a2"}, {"\u30a4"}, {"\u30a6"}, {"\u30a8"}, {"\u30aa"},      {"A"},      {"B"},      {"C"},      {"D"}, {"E"},
				{"\u30ab"}, {"\u30ad"}, {"\u30af"}, {"\u30b1"}, {"\u30b3"},      {"F"},      {"G"},      {"H"},      {"I"}, {"J"},
				{"\u30b5"}, {"\u30b7"}, {"\u30b9"}, {"\u30bb"}, {"\u30bd"},      {"K"},      {"L"},      {"M"},      {"N"}, {"O"},
				{"\u30bf"}, {"\u30c1"}, {"\u30c4"}, {"\u30c6"}, {"\u30c8"},      {"P"},      {"Q"},      {"R"},      {"S"}, {"T"},
				{"\u30ca"}, {"\u30cb"}, {"\u30cc"}, {"\u30cd"}, {"\u30ce"},      {"U"},      {"V"},      {"W"},      {"X"}, {"Y"},
				{"\u30cf"}, {"\u30d2"}, {"\u30d5"}, {"\u30d8"}, {"\u30db"},      {"Z"},      {"?"},      {"!"},      {"\u30fc"}, {"/"},
				{"\u30de"}, {"\u30df"}, {"\u30e0"}, {"\u30e1"}, {"\u30e2"},     {"\\"},      {"&"},      {" "},      {"="}, {"\u3001"},
				{"\u30e4"},      {"("}, {"\u30e6"},      {")"}, {"\u30e8"},      {"*"},      {"#"},      {" "},      {"@"}, {"\u3002"},
				{"\u30e9"}, {"\u30ea"}, {"\u30eb"}, {"\u30ec"}, {"\u30ed"},      {"1"},      {"2"},      {"3"},      {"4"}, {"5"},
				{"\u30ef"}, {"\u30f2"}, {"\u30f3"}, {"\u309b"}, {"\u309c"},      {"6"},      {"7"},      {"8"},      {"9"}, {"0"},
				{"#"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "},
				{"*"}, {"\uff9e"}, {" "}, {"\uff9f"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}
			}
		},
		{
			{
				{"\uff71"}, {"\uff72"}, {"\uff73"}, {"\uff74"}, {"\uff75"},      {"a"},      {"b"},      {"c"},      {"d"}, {"e"},
				{"\uff76"}, {"\uff77"}, {"\uff78"}, {"\uff79"}, {"\uff7a"},      {"f"},      {"g"},      {"h"},      {"i"}, {"j"},
				{"\uff7b"}, {"\uff7c"}, {"\uff7d"}, {"\uff7e"}, {"\uff7f"},      {"k"},      {"l"},      {"m"},      {"n"}, {"o"},
				{"\uff80"}, {"\uff81"}, {"\uff82"}, {"\uff83"}, {"\uff84"},      {"p"},      {"q"},      {"r"},      {"s"}, {"t"},
				{"\uff85"}, {"\uff86"}, {"\uff87"}, {"\uff88"}, {"\uff89"},      {"u"},      {"v"},      {"w"},      {"x"}, {"y"},
				{"\uff8a"}, {"\uff8b"}, {"\uff8c"}, {"\uff8d"}, {"\uff8e"},      {"z"},      {"?"},      {"!"},      {"\uff70"}, {"/"},
				{"\uff8f"}, {"\uff90"}, {"\uff91"}, {"\uff92"}, {"\uff93"},     {"\\"},      {"&"},      {" "},      {"="}, {"\uff64"},
				{"\uff94"},      {"("}, {"\uff95"},      {")"}, {"\uff96"},      {"*"},      {"#"},      {" "},      {"@"}, {"\uff61"},
				{"\uff97"}, {"\uff98"}, {"\uff99"}, {"\uff9a"}, {"\uff9b"},      {"1"},      {"2"},      {"3"},      {"4"}, {"5"},
				{"\uff9c"}, {"\uff66"}, {"\uff9d"}, {"\uff9e"}, {"\uff9f"},      {"6"},      {"7"},      {"8"},      {"9"}, {"0"},
				{"#"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "},
				{"*"}, {"\uff9e"}, {" "}, {"\uff9f"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}
			},
			{
				{"\uff71"}, {"\uff72"}, {"\uff73"}, {"\uff74"}, {"\uff75"},      {"A"},      {"B"},      {"C"},      {"D"}, {"E"},
				{"\uff76"}, {"\uff77"}, {"\uff78"}, {"\uff79"}, {"\uff7a"},      {"F"},      {"G"},      {"H"},      {"I"}, {"J"},
				{"\uff7b"}, {"\uff7c"}, {"\uff7d"}, {"\uff7e"}, {"\uff7f"},      {"K"},      {"L"},      {"M"},      {"N"}, {"O"},
				{"\uff80"}, {"\uff81"}, {"\uff82"}, {"\uff83"}, {"\uff84"},      {"P"},      {"Q"},      {"R"},      {"S"}, {"T"},
				{"\uff85"}, {"\uff86"}, {"\uff87"}, {"\uff88"}, {"\uff89"},      {"U"},      {"V"},      {"W"},      {"X"}, {"Y"},
				{"\uff8a"}, {"\uff8b"}, {"\uff8c"}, {"\uff8d"}, {"\uff8e"},      {"Z"},      {"?"},      {"!"},      {"\uff70"}, {"/"},
				{"\uff8f"}, {"\uff90"}, {"\uff91"}, {"\uff92"}, {"\uff93"},     {"\\"},      {"&"},      {" "},      {"="}, {"\uff64"},
				{"\uff94"},      {"("}, {"\uff95"},      {")"}, {"\uff96"},      {"*"},      {"#"},      {" "},      {"@"}, {"\uff61"},
				{"\uff97"}, {"\uff98"}, {"\uff99"}, {"\uff9a"}, {"\uff9b"},      {"1"},      {"2"},      {"3"},      {"4"}, {"5"},
				{"\uff9c"}, {"\uff66"}, {"\uff9d"}, {"\uff9e"}, {"\uff9f"},      {"6"},      {"7"},      {"8"},      {"9"}, {"0"},
				{"#"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "},
				{"*"}, {"\uff9e"}, {" "}, {"\uff9f"}, {" "}, {" "}, {" "}, {" "}, {" "}, {" "}
			}
		},
	};

	/** Replace table for full-width Nicotouch */
	private static final HashMap<String, String> JP_FULL_2TOUCH_REPLACE_TABLE_1 = new HashMap<String, String>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		{
			put("\u3042", "\u3041"); put("\u3044", "\u3043"); put("\u3046", "\u3045"); put("\u3048", "\u3047"); put("\u304a", "\u3049"); // A
			put("\u3041", "\u3042"); put("\u3043", "\u3044"); put("\u3045", "\u30f4"); put("\u3047", "\u3048"); put("\u3049", "\u304a"); // a
			put("\u304b", "\u304c"); put("\u304d", "\u304e"); put("\u304f", "\u3050"); put("\u3051", "\u3052"); put("\u3053", "\u3054"); // K
			put("\u304c", "\u304b"); put("\u304e", "\u304d"); put("\u3050", "\u304f"); put("\u3052", "\u3051"); put("\u3054", "\u3053"); // k
			put("\u3055", "\u3056"); put("\u3057", "\u3058"); put("\u3059", "\u305a"); put("\u305b", "\u305c"); put("\u305d", "\u305e"); // S
			put("\u3056", "\u3055"); put("\u3058", "\u3057"); put("\u305a", "\u3059"); put("\u305c", "\u305b"); put("\u305e", "\u305d"); // s
			put("\u305f", "\u3060"); put("\u3061", "\u3062"); put("\u3064", "\u3063"); put("\u3066", "\u3067"); put("\u3068", "\u3069"); // T
			put("\u3060", "\u305f"); put("\u3062", "\u3061"); put("\u3063", "\u3065"); put("\u3067", "\u3066"); put("\u3069", "\u3068"); // t
			put("\u3065", "\u3064"); // du
			put("\u30f4", "\u3046"); // vu
			put("\u306f", "\u3070"); put("\u3072", "\u3073"); put("\u3075", "\u3076"); put("\u3078", "\u3079"); put("\u307b", "\u307c"); // H
			put("\u3070", "\u3071"); put("\u3073", "\u3074"); put("\u3076", "\u3077"); put("\u3079", "\u307a"); put("\u307c", "\u307d"); // h
			put("\u3071", "\u306f"); put("\u3074", "\u3072"); put("\u3077", "\u3075"); put("\u307a", "\u3078"); put("\u307d", "\u307b"); // h.
			put("\u3084", "\u3083"); put("\u3086", "\u3085"); put("\u3088", "\u3087"); // Y
			put("\u3083", "\u3084"); put("\u3085", "\u3086"); put("\u3087", "\u3088"); // y
			put("\u308f", "\u308e"); put("\u308e", "\u308f"); // W
			put("\u309b", "\u309c"); put("\u309c", "\u309b"); // dakuten

			put("\u30a2", "\u30a1"); put("\u30a4", "\u30a3"); put("\u30a6", "\u30a5"); put("\u30a8", "\u30a7"); put("\u30aa", "\u30a9"); // A
			put("\u30a1", "\u30a2"); put("\u30a3", "\u30a4"); put("\u30a5", "\u30f4"); put("\u30a7", "\u30a8"); put("\u30a9", "\u30aa"); // a
			put("\u30ab", "\u30ac"); put("\u30ad", "\u30ae"); put("\u30af", "\u30b0"); put("\u30b1", "\u30b2"); put("\u30b3", "\u30b4"); // K
			put("\u30ac", "\u30ab"); put("\u30ae", "\u30ad"); put("\u30b0", "\u30af"); put("\u30b2", "\u30b1"); put("\u30b4", "\u30b3"); // k
			put("\u30b5", "\u30b6"); put("\u30b7", "\u30b8"); put("\u30b9", "\u30ba"); put("\u30bb", "\u30bc"); put("\u30bd", "\u30be"); // S
			put("\u30b6", "\u30b5"); put("\u30b8", "\u30b7"); put("\u30ba", "\u30b9"); put("\u30bc", "\u30bb"); put("\u30be", "\u30bd"); // s
			put("\u30bf", "\u30c0"); put("\u30c1", "\u30c2"); put("\u30c4", "\u30c3"); put("\u30c6", "\u30c7"); put("\u30c8", "\u30c9"); // T
			put("\u30c0", "\u30bf"); put("\u30c2", "\u30c1"); put("\u30c3", "\u30c5"); put("\u30c7", "\u30c6"); put("\u30c9", "\u30c8"); // t
			put("\u30c5", "\u30c4"); // du
			put("\u30f4", "\u30a6"); // vu
			put("\u30cf", "\u30d0"); put("\u30d2", "\u30d3"); put("\u30d5", "\u30d6"); put("\u30d8", "\u30d9"); put("\u30db", "\u30dc"); // H
			put("\u30d0", "\u30d1"); put("\u30d3", "\u30d4"); put("\u30d6", "\u30d7"); put("\u30d9", "\u30da"); put("\u30dc", "\u30dd"); // h
			put("\u30d1", "\u30cf"); put("\u30d4", "\u30d2"); put("\u30d7", "\u30d5"); put("\u30da", "\u30d8"); put("\u30dd", "\u30db"); // h.
			put("\u30e4", "\u30e3"); put("\u30e6", "\u30e5"); put("\u30e8", "\u30e7"); // Y
			put("\u30e3", "\u30e4"); put("\u30e5", "\u30e6"); put("\u30e7", "\u30e8"); // y
			put("\u30ef", "\u30ee"); put("\u30ee", "\u30ef"); // W
			put("\u309b", "\u309c"); put("\u309c", "\u309b"); // dakuten

			put("\uff71", "\uff67"); put("\uff72", "\uff68"); put("\uff73", "\uff69"); put("\uff74", "\uff6a"); put("\uff75", "\uff6b"); // A
			put("\uff67", "\uff71"); put("\uff68", "\uff72"); put("\uff69", "\uff73"); put("\uff6a", "\uff74"); put("\uff6b", "\uff75"); // a
			put("\uff82", "\uff6f"); put("\uff6f", "\uff82"); // tu
			put("\uff94", "\uff6c"); put("\uff95", "\uff6d"); put("\uff96", "\uff6e"); // Y
			put("\uff6c", "\uff94"); put("\uff6d", "\uff95"); put("\uff6e", "\uff96"); // y
			put("\uff9e", "\uff9f"); put("\uff9f", "\uff9e"); // dakuten

			put("a", "A"); put("b", "B"); put("c", "C"); put("d", "D"); put("e", "E"); put("f", "F"); put("g", "G"); put("h", "H"); put("i", "I"); put("j", "J");
			put("k", "K"); put("l", "L"); put("m", "M"); put("n", "N"); put("o", "O"); put("p", "P"); put("q", "Q"); put("r", "R"); put("s", "S"); put("t", "T");
			put("u", "U"); put("v", "V"); put("w", "W"); put("x", "X"); put("y", "Y"); put("z", "Z");
			put("A", "a"); put("B", "b"); put("C", "c"); put("D", "d"); put("E", "e"); put("F", "f"); put("G", "g"); put("H", "h"); put("I", "i"); put("J", "j");
			put("K", "k"); put("L", "l"); put("M", "m"); put("N", "n"); put("O", "o"); put("P", "p"); put("Q", "q"); put("R", "r"); put("S", "s"); put("T", "t");
			put("U", "u"); put("V", "v"); put("W", "w"); put("X", "x"); put("Y", "y"); put("Z", "z");

			put("1", "\uff11"); put("2", "\uff12"); put("3", "\uff13"); put("4", "\uff14"); put("5", "\uff15");
			put("6", "\uff16"); put("7", "\uff17"); put("8", "\uff18"); put("9", "\uff19"); put("0", "\uff10");
			put("\uff11", "1"); put("\uff12", "2"); put("\uff13", "3"); put("\uff14", "4"); put("\uff15", "5");
			put("\uff16", "6"); put("\uff17", "7"); put("\uff18", "8"); put("\uff19", "9"); put("\uff10", "0");

			put(".", "\uff0e"); put("-", "\u2015"); put("@", "\uff20"); put("_", "\uff3f");
			put("\uff0e", "."); put("\u2015", "-"); put("\uff20", "@"); put("\uff3f", "_");

			put("\uff01", "!"); put("\uff1f", "?");
			put("!", "\uff01"); put("?", "\uff1f");

			put("\u3000", " "); put(" ", "\u3000");

			put("(", "["); put("[", "\u300c"); put("\u300c", "\u300e"); put("\u300e", "\u3010"); put("\u3010", "(");
			put(")", "]"); put("]", "\u300d"); put("\u300d", "\u300f"); put("\u300f", "\u3011"); put("\u3011", ")");
		}
	};

	private static final HashMap<String, String> JP_FULL_2TOUCH_REPLACE_TABLE_2 = new HashMap<String, String>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		{
			put("\u3042", "\u3041"); put("\u3044", "\u3043"); put("\u3046", "\u3045"); put("\u3048", "\u3047"); put("\u304a", "\u3049"); // A
			put("\u3041", "\u3042"); put("\u3043", "\u3044"); put("\u3045", "\u30f4"); put("\u3047", "\u3048"); put("\u3049", "\u304a"); // a
			put("\u304b", "\u304c"); put("\u304d", "\u304e"); put("\u304f", "\u3050"); put("\u3051", "\u3052"); put("\u3053", "\u3054"); // K
			put("\u304c", "\u304b"); put("\u304e", "\u304d"); put("\u3050", "\u304f"); put("\u3052", "\u3051"); put("\u3054", "\u3053"); // k
			put("\u3055", "\u3056"); put("\u3057", "\u3058"); put("\u3059", "\u305a"); put("\u305b", "\u305c"); put("\u305d", "\u305e"); // S
			put("\u3056", "\u3055"); put("\u3058", "\u3057"); put("\u305a", "\u3059"); put("\u305c", "\u305b"); put("\u305e", "\u305d"); // s
			put("\u305f", "\u3060"); put("\u3061", "\u3062"); put("\u3064", "\u3065"); put("\u3066", "\u3067"); put("\u3068", "\u3069"); // T
			put("\u3060", "\u305f"); put("\u3062", "\u3061"); put("\u3065", "\u3063"); put("\u3067", "\u3066"); put("\u3069", "\u3068"); // t
			put("\u3063", "\u3064"); // du
			put("\u30f4", "\u3046"); // vu
			put("\u306f", "\u3070"); put("\u3072", "\u3073"); put("\u3075", "\u3076"); put("\u3078", "\u3079"); put("\u307b", "\u307c"); // H
			put("\u3070", "\u3071"); put("\u3073", "\u3074"); put("\u3076", "\u3077"); put("\u3079", "\u307a"); put("\u307c", "\u307d"); // h
			put("\u3071", "\u306f"); put("\u3074", "\u3072"); put("\u3077", "\u3075"); put("\u307a", "\u3078"); put("\u307d", "\u307b"); // h.
			put("\u3084", "\u3083"); put("\u3086", "\u3085"); put("\u3088", "\u3087"); // Y
			put("\u3083", "\u3084"); put("\u3085", "\u3086"); put("\u3087", "\u3088"); // y
			put("\u308f", "\u308e"); put("\u308e", "\u308f"); // W
			put("\u309b", "\u309c"); put("\u309c", "\u309b"); // dakuten

			put("\u30a2", "\u30a1"); put("\u30a4", "\u30a3"); put("\u30a6", "\u30a5"); put("\u30a8", "\u30a7"); put("\u30aa", "\u30a9"); // A
			put("\u30a1", "\u30a2"); put("\u30a3", "\u30a4"); put("\u30a5", "\u30f4"); put("\u30a7", "\u30a8"); put("\u30a9", "\u30aa"); // a
			put("\u30ab", "\u30ac"); put("\u30ad", "\u30ae"); put("\u30af", "\u30b0"); put("\u30b1", "\u30b2"); put("\u30b3", "\u30b4"); // K
			put("\u30ac", "\u30ab"); put("\u30ae", "\u30ad"); put("\u30b0", "\u30af"); put("\u30b2", "\u30b1"); put("\u30b4", "\u30b3"); // k
			put("\u30b5", "\u30b6"); put("\u30b7", "\u30b8"); put("\u30b9", "\u30ba"); put("\u30bb", "\u30bc"); put("\u30bd", "\u30be"); // S
			put("\u30b6", "\u30b5"); put("\u30b8", "\u30b7"); put("\u30ba", "\u30b9"); put("\u30bc", "\u30bb"); put("\u30be", "\u30bd"); // s
			put("\u30bf", "\u30c0"); put("\u30c1", "\u30c2"); put("\u30c4", "\u30c5"); put("\u30c6", "\u30c7"); put("\u30c8", "\u30c9"); // T
			put("\u30c0", "\u30bf"); put("\u30c2", "\u30c1"); put("\u30c5", "\u30c3"); put("\u30c7", "\u30c6"); put("\u30c9", "\u30c8"); // t
			put("\u30c3", "\u30c4"); // ltu
			put("\u30f4", "\u30a6"); // vu
			put("\u30cf", "\u30d0"); put("\u30d2", "\u30d3"); put("\u30d5", "\u30d6"); put("\u30d8", "\u30d9"); put("\u30db", "\u30dc"); // H
			put("\u30d0", "\u30d1"); put("\u30d3", "\u30d4"); put("\u30d6", "\u30d7"); put("\u30d9", "\u30da"); put("\u30dc", "\u30dd"); // h
			put("\u30d1", "\u30cf"); put("\u30d4", "\u30d2"); put("\u30d7", "\u30d5"); put("\u30da", "\u30d8"); put("\u30dd", "\u30db"); // h.
			put("\u30e4", "\u30e3"); put("\u30e6", "\u30e5"); put("\u30e8", "\u30e7"); // Y
			put("\u30e3", "\u30e4"); put("\u30e5", "\u30e6"); put("\u30e7", "\u30e8"); // y
			put("\u30ef", "\u30ee"); put("\u30ee", "\u30ef"); // W
			put("\u309b", "\u309c"); put("\u309c", "\u309b"); // dakuten

			put("\uff71", "\uff67"); put("\uff72", "\uff68"); put("\uff73", "\uff69"); put("\uff74", "\uff6a"); put("\uff75", "\uff6b"); // A
			put("\uff67", "\uff71"); put("\uff68", "\uff72"); put("\uff69", "\uff73"); put("\uff6a", "\uff74"); put("\uff6b", "\uff75"); // a
			put("\uff82", "\uff6f"); put("\uff6f", "\uff82"); // tu
			put("\uff94", "\uff6c"); put("\uff95", "\uff6d"); put("\uff96", "\uff6e"); // Y
			put("\uff6c", "\uff94"); put("\uff6d", "\uff95"); put("\uff6e", "\uff96"); // y
			put("\uff9e", "\uff9f"); put("\uff9f", "\uff9e"); // dakuten

			put("a", "A"); put("b", "B"); put("c", "C"); put("d", "D"); put("e", "E"); put("f", "F"); put("g", "G"); put("h", "H"); put("i", "I"); put("j", "J");
			put("k", "K"); put("l", "L"); put("m", "M"); put("n", "N"); put("o", "O"); put("p", "P"); put("q", "Q"); put("r", "R"); put("s", "S"); put("t", "T");
			put("u", "U"); put("v", "V"); put("w", "W"); put("x", "X"); put("y", "Y"); put("z", "Z");
			put("A", "a"); put("B", "b"); put("C", "c"); put("D", "d"); put("E", "e"); put("F", "f"); put("G", "g"); put("H", "h"); put("I", "i"); put("J", "j");
			put("K", "k"); put("L", "l"); put("M", "m"); put("N", "n"); put("O", "o"); put("P", "p"); put("Q", "q"); put("R", "r"); put("S", "s"); put("T", "t");
			put("U", "u"); put("V", "v"); put("W", "w"); put("X", "x"); put("Y", "y"); put("Z", "z");

			put("1", "\uff11"); put("2", "\uff12"); put("3", "\uff13"); put("4", "\uff14"); put("5", "\uff15");
			put("6", "\uff16"); put("7", "\uff17"); put("8", "\uff18"); put("9", "\uff19"); put("0", "\uff10");
			put("\uff11", "1"); put("\uff12", "2"); put("\uff13", "3"); put("\uff14", "4"); put("\uff15", "5");
			put("\uff16", "6"); put("\uff17", "7"); put("\uff18", "8"); put("\uff19", "9"); put("\uff10", "0");

			put(".", "\uff0e"); put("-", "\u2015"); put("@", "\uff20"); put("_", "\uff3f");
			put("\uff0e", "."); put("\u2015", "-"); put("\uff20", "@"); put("\uff3f", "_");

			put("\uff01", "!"); put("\uff1f", "?");
			put("!", "\uff01"); put("?", "\uff1f");

			put("\u3000", " "); put(" ", "\u3000");

			put("(", "["); put("[", "\u300c"); put("\u300c", "\u300e"); put("\u300e", "\u3010"); put("\u3010", "(");
			put(")", "]"); put("]", "\u300d"); put("\u300d", "\u300f"); put("\u300f", "\u3011"); put("\u3011", ")");
		}
	};

	/**
	 * change map
	 */
	private static final int flickHiraganaChangeMap[][] = {
		{  0,  4,  1,  2,  3 }, // A
		{  0,  4,  1,  2,  3 }, // Ka
		{  0,  4,  1,  2,  3 }, // Sa
		{  0,  4,  1,  2,  3 }, // Ta
		{  0,  4,  1,  2,  3 }, // Na
		{  0,  4,  1,  2,  3 }, // Ha
		{  0,  4,  1,  2,  3 }, // Ma
		{  0,  4, -1,  2, -1 }, // Ya
		{  0,  4,  1,  2,  3 }, // Ra
		{  0, -1,  1,  2,  4 }, // Wa
		{ -1, -1, -1, -1, -1 }, // Sharp
		{ -1, -1,  1,  2,  3 }, // Aster
	};

	private static final int flickAlphabetChangeMap[][] = {
		{  0,  7,  4,  1,  5 }, // .1/@:
		{  0,  6,  1,  2,  7 }, // A2BC"
		{  0,  6,  1,  2,  7 }, // D3EF'
		{  0,  6,  1,  2,  7 }, // G4HI<
		{  0,  6,  1,  2,  7 }, // J5KL&
		{  0,  6,  1,  2,  7 }, // M6NO>
		{  0,  8,  1,  2,  3 }, // P7QRS
		{  0,  6,  1,  2,  7 }, // T8UV$
		{  0,  8,  1,  2,  3 }, // W9XYZ
		{  0, -1,  2,  1,  3 }, // - (0)
		{  0, -1,  1,  2,  3 }, // , .?!
		{  0, -1,  1,  2,  3 }, // Aster
	};

	private static final int selectLandKeyTable[] = {
		R.xml.key_2touch_top_0,
		R.xml.key_2touch_shift_0,
		R.xml.key_2touch_a_0,
		R.xml.key_2touch_k_0,
		R.xml.key_2touch_s_0,
		R.xml.key_2touch_t_0,
		R.xml.key_2touch_n_0,
		R.xml.key_2touch_h_0,
		R.xml.key_2touch_m_0,
		R.xml.key_2touch_y_0,
		R.xml.key_2touch_r_0,
		R.xml.key_2touch_w_0,
		R.xml.key_2touch_input_top_0,
		R.xml.key_2touch_input_shift_0,
		R.xml.key_2touch_input_a_0,
		R.xml.key_2touch_input_k_0,
		R.xml.key_2touch_input_s_0,
		R.xml.key_2touch_input_t_0,
		R.xml.key_2touch_input_n_0,
		R.xml.key_2touch_input_h_0,
		R.xml.key_2touch_input_m_0,
		R.xml.key_2touch_input_y_0,
		R.xml.key_2touch_input_r_0,
		R.xml.key_2touch_input_w_0,

		R.xml.key_2touch_katakana_full_top_0,
		R.xml.keyboard_12key_full_katakana_shift_0,
		R.xml.key_2touch_katakana_full_a_0,
		R.xml.key_2touch_katakana_full_k_0,
		R.xml.key_2touch_katakana_full_s_0,
		R.xml.key_2touch_katakana_full_t_0,
		R.xml.key_2touch_katakana_full_n_0,
		R.xml.key_2touch_katakana_full_h_0,
		R.xml.key_2touch_katakana_full_m_0,
		R.xml.key_2touch_katakana_full_y_0,
		R.xml.key_2touch_katakana_full_r_0,
		R.xml.key_2touch_katakana_full_w_0,
		R.xml.key_2touch_katakana_full_input_top_0,
		R.xml.keyboard_12key_full_katakana_input_shift_0,
		R.xml.key_2touch_katakana_full_input_a_0,
		R.xml.key_2touch_katakana_full_input_k_0,
		R.xml.key_2touch_katakana_full_input_s_0,
		R.xml.key_2touch_katakana_full_input_t_0,
		R.xml.key_2touch_katakana_full_input_n_0,
		R.xml.key_2touch_katakana_full_input_h_0,
		R.xml.key_2touch_katakana_full_input_m_0,
		R.xml.key_2touch_katakana_full_input_y_0,
		R.xml.key_2touch_katakana_full_input_r_0,
		R.xml.key_2touch_katakana_full_input_w_0,

		R.xml.key_2touch_katakana_half_top_0,
		R.xml.keyboard_12key_half_katakana_shift_0,
		R.xml.key_2touch_katakana_half_a_0,
		R.xml.key_2touch_katakana_half_k_0,
		R.xml.key_2touch_katakana_half_s_0,
		R.xml.key_2touch_katakana_half_t_0,
		R.xml.key_2touch_katakana_half_n_0,
		R.xml.key_2touch_katakana_half_h_0,
		R.xml.key_2touch_katakana_half_m_0,
		R.xml.key_2touch_katakana_half_y_0,
		R.xml.key_2touch_katakana_half_r_0,
		R.xml.key_2touch_katakana_half_w_0,
		R.xml.key_2touch_katakana_half_input_top_0,
		R.xml.keyboard_12key_half_katakana_input_shift_0,
		R.xml.key_2touch_katakana_half_input_a_0,
		R.xml.key_2touch_katakana_half_input_k_0,
		R.xml.key_2touch_katakana_half_input_s_0,
		R.xml.key_2touch_katakana_half_input_t_0,
		R.xml.key_2touch_katakana_half_input_n_0,
		R.xml.key_2touch_katakana_half_input_h_0,
		R.xml.key_2touch_katakana_half_input_m_0,
		R.xml.key_2touch_katakana_half_input_y_0,
		R.xml.key_2touch_katakana_half_input_r_0,
		R.xml.key_2touch_katakana_half_input_w_0,
	};
	private static final int selectPortKeyTable[] = {
		R.xml.key_2touch_top_0,
		R.xml.key_2touch_shift_0,
		R.xml.key_2touch_a_0,
		R.xml.key_2touch_k_0,
		R.xml.key_2touch_s_0,
		R.xml.key_2touch_t_0,
		R.xml.key_2touch_n_0,
		R.xml.key_2touch_h_0,
		R.xml.key_2touch_m_0,
		R.xml.key_2touch_y_0,
		R.xml.key_2touch_r_0,
		R.xml.key_2touch_w_0,
		R.xml.key_2touch_input_top_0,
		R.xml.key_2touch_input_shift_0,
		R.xml.key_2touch_input_a_0,
		R.xml.key_2touch_input_k_0,
		R.xml.key_2touch_input_s_0,
		R.xml.key_2touch_input_t_0,
		R.xml.key_2touch_input_n_0,
		R.xml.key_2touch_input_h_0,
		R.xml.key_2touch_input_m_0,
		R.xml.key_2touch_input_y_0,
		R.xml.key_2touch_input_r_0,
		R.xml.key_2touch_input_w_0,

		R.xml.key_2touch_katakana_full_top_0,
		R.xml.keyboard_12key_full_katakana_shift_0,
		R.xml.key_2touch_katakana_full_a_0,
		R.xml.key_2touch_katakana_full_k_0,
		R.xml.key_2touch_katakana_full_s_0,
		R.xml.key_2touch_katakana_full_t_0,
		R.xml.key_2touch_katakana_full_n_0,
		R.xml.key_2touch_katakana_full_h_0,
		R.xml.key_2touch_katakana_full_m_0,
		R.xml.key_2touch_katakana_full_y_0,
		R.xml.key_2touch_katakana_full_r_0,
		R.xml.key_2touch_katakana_full_w_0,
		R.xml.key_2touch_katakana_full_input_top_0,
		R.xml.keyboard_12key_full_katakana_input_shift_0,
		R.xml.key_2touch_katakana_full_input_a_0,
		R.xml.key_2touch_katakana_full_input_k_0,
		R.xml.key_2touch_katakana_full_input_s_0,
		R.xml.key_2touch_katakana_full_input_t_0,
		R.xml.key_2touch_katakana_full_input_n_0,
		R.xml.key_2touch_katakana_full_input_h_0,
		R.xml.key_2touch_katakana_full_input_m_0,
		R.xml.key_2touch_katakana_full_input_y_0,
		R.xml.key_2touch_katakana_full_input_r_0,
		R.xml.key_2touch_katakana_full_input_w_0,

		R.xml.key_2touch_katakana_half_top_0,
		R.xml.keyboard_12key_half_katakana_shift_0,
		R.xml.key_2touch_katakana_half_a_0,
		R.xml.key_2touch_katakana_half_k_0,
		R.xml.key_2touch_katakana_half_s_0,
		R.xml.key_2touch_katakana_half_t_0,
		R.xml.key_2touch_katakana_half_n_0,
		R.xml.key_2touch_katakana_half_h_0,
		R.xml.key_2touch_katakana_half_m_0,
		R.xml.key_2touch_katakana_half_y_0,
		R.xml.key_2touch_katakana_half_r_0,
		R.xml.key_2touch_katakana_half_w_0,
		R.xml.key_2touch_katakana_half_input_top_0,
		R.xml.keyboard_12key_half_katakana_input_shift_0,
		R.xml.key_2touch_katakana_half_input_a_0,
		R.xml.key_2touch_katakana_half_input_k_0,
		R.xml.key_2touch_katakana_half_input_s_0,
		R.xml.key_2touch_katakana_half_input_t_0,
		R.xml.key_2touch_katakana_half_input_n_0,
		R.xml.key_2touch_katakana_half_input_h_0,
		R.xml.key_2touch_katakana_half_input_m_0,
		R.xml.key_2touch_katakana_half_input_y_0,
		R.xml.key_2touch_katakana_half_input_r_0,
		R.xml.key_2touch_katakana_half_input_w_0,
	};

	// =========================================================

	private static final int selectSubTenLandKeyTable2[] = {
		R.xml.key_subten_12key2_full_hiragana_0,
		R.xml.key_subten_12key2_full_hiragana_shift_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		
		R.xml.key_subten_12key2_full_hiragana_input_0,
		R.xml.key_subten_12key2_full_hiragana_input_shift_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,

		R.xml.key_subten_12key2_full_katakana_0,
		R.xml.key_subten_12key2_full_katakana_shift_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_0,
		R.xml.key_subten_12key2_full_katakana_input_shift_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,

		R.xml.key_subten_12key2_half_katakana_0,
		R.xml.key_subten_12key2_half_katakana_shift_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_0,
		R.xml.key_subten_12key2_half_katakana_input_shift_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
	};

	private static final int selectSubTenPortKeyTable2[] = {
		R.xml.key_subten_12key2_full_hiragana_0,
		R.xml.key_subten_12key2_full_hiragana_shift_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_0,
		R.xml.key_subten_12key2_full_hiragana_input_shift_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key2_full_hiragana_input_2nd_0,

		R.xml.key_subten_12key2_full_katakana_0,
		R.xml.key_subten_12key2_full_katakana_shift_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_0,
		R.xml.key_subten_12key2_full_katakana_input_shift_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,
		R.xml.key_subten_12key2_full_katakana_input_2nd_0,

		R.xml.key_subten_12key2_half_katakana_0,
		R.xml.key_subten_12key2_half_katakana_shift_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_0,
		R.xml.key_subten_12key2_half_katakana_input_shift_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
		R.xml.key_subten_12key2_half_katakana_input_2nd_0,
	};

	// =========================================================

	private static final int selectSubTenLandKeyTable3[] = {
		R.xml.key_subten_12key3_full_hiragana_0,
		R.xml.key_subten_12key3_full_hiragana_shift_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		
		R.xml.key_subten_12key3_full_hiragana_input_0,
		R.xml.key_subten_12key3_full_hiragana_input_shift_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,

		R.xml.key_subten_12key3_full_katakana_0,
		R.xml.key_subten_12key3_full_katakana_shift_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_0,
		R.xml.key_subten_12key3_full_katakana_input_shift_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,

		R.xml.key_subten_12key3_half_katakana_0,
		R.xml.key_subten_12key3_half_katakana_shift_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_0,
		R.xml.key_subten_12key3_half_katakana_input_shift_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
	};

	private static final int selectSubTenPortKeyTable3[] = {
		R.xml.key_subten_12key3_full_hiragana_0,
		R.xml.key_subten_12key3_full_hiragana_shift_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_0,
		R.xml.key_subten_12key3_full_hiragana_input_shift_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,
		R.xml.key_subten_12key3_full_hiragana_input_2nd_0,

		R.xml.key_subten_12key3_full_katakana_0,
		R.xml.key_subten_12key3_full_katakana_shift_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_0,
		R.xml.key_subten_12key3_full_katakana_input_shift_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,
		R.xml.key_subten_12key3_full_katakana_input_2nd_0,

		R.xml.key_subten_12key3_half_katakana_0,
		R.xml.key_subten_12key3_half_katakana_shift_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_0,
		R.xml.key_subten_12key3_half_katakana_input_shift_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
		R.xml.key_subten_12key3_half_katakana_input_2nd_0,
	};

	// =========================================================

	private static final int selectFlickLandKeyTable[] = {
		R.xml.key_2touch_flick_top_0,
		R.xml.key_2touch_shift_0,
		R.xml.key_2touch_a_0,
		R.xml.key_2touch_k_0,
		R.xml.key_2touch_s_0,
		R.xml.key_2touch_t_0,
		R.xml.key_2touch_n_0,
		R.xml.key_2touch_h_0,
		R.xml.key_2touch_m_0,
		R.xml.key_2touch_y_0,
		R.xml.key_2touch_r_0,
		R.xml.key_2touch_w_0,
		R.xml.key_2touch_flick_input_top_0,
		R.xml.key_2touch_input_shift_0,
		R.xml.key_2touch_input_a_0,
		R.xml.key_2touch_input_k_0,
		R.xml.key_2touch_input_s_0,
		R.xml.key_2touch_input_t_0,
		R.xml.key_2touch_input_n_0,
		R.xml.key_2touch_input_h_0,
		R.xml.key_2touch_input_m_0,
		R.xml.key_2touch_input_y_0,
		R.xml.key_2touch_input_r_0,
		R.xml.key_2touch_input_w_0,

		R.xml.key_2touch_flick_katakana_full_top_0,
		R.xml.keyboard_12key_full_katakana_shift_0,
		R.xml.key_2touch_katakana_full_a_0,
		R.xml.key_2touch_katakana_full_k_0,
		R.xml.key_2touch_katakana_full_s_0,
		R.xml.key_2touch_katakana_full_t_0,
		R.xml.key_2touch_katakana_full_n_0,
		R.xml.key_2touch_katakana_full_h_0,
		R.xml.key_2touch_katakana_full_m_0,
		R.xml.key_2touch_katakana_full_y_0,
		R.xml.key_2touch_katakana_full_r_0,
		R.xml.key_2touch_katakana_full_w_0,
		R.xml.key_2touch_flick_katakana_full_input_top_0,
		R.xml.keyboard_12key_full_katakana_input_shift_0,
		R.xml.key_2touch_katakana_full_input_a_0,
		R.xml.key_2touch_katakana_full_input_k_0,
		R.xml.key_2touch_katakana_full_input_s_0,
		R.xml.key_2touch_katakana_full_input_t_0,
		R.xml.key_2touch_katakana_full_input_n_0,
		R.xml.key_2touch_katakana_full_input_h_0,
		R.xml.key_2touch_katakana_full_input_m_0,
		R.xml.key_2touch_katakana_full_input_y_0,
		R.xml.key_2touch_katakana_full_input_r_0,
		R.xml.key_2touch_katakana_full_input_w_0,

		R.xml.key_2touch_flick_katakana_half_top_0,
		R.xml.keyboard_12key_half_katakana_shift_0,
		R.xml.key_2touch_katakana_half_a_0,
		R.xml.key_2touch_katakana_half_k_0,
		R.xml.key_2touch_katakana_half_s_0,
		R.xml.key_2touch_katakana_half_t_0,
		R.xml.key_2touch_katakana_half_n_0,
		R.xml.key_2touch_katakana_half_h_0,
		R.xml.key_2touch_katakana_half_m_0,
		R.xml.key_2touch_katakana_half_y_0,
		R.xml.key_2touch_katakana_half_r_0,
		R.xml.key_2touch_katakana_half_w_0,
		R.xml.key_2touch_flick_katakana_half_input_top_0,
		R.xml.keyboard_12key_half_katakana_input_shift_0,
		R.xml.key_2touch_katakana_half_input_a_0,
		R.xml.key_2touch_katakana_half_input_k_0,
		R.xml.key_2touch_katakana_half_input_s_0,
		R.xml.key_2touch_katakana_half_input_t_0,
		R.xml.key_2touch_katakana_half_input_n_0,
		R.xml.key_2touch_katakana_half_input_h_0,
		R.xml.key_2touch_katakana_half_input_m_0,
		R.xml.key_2touch_katakana_half_input_y_0,
		R.xml.key_2touch_katakana_half_input_r_0,
		R.xml.key_2touch_katakana_half_input_w_0,
	};
	private static final int selectFlickPortKeyTable[] = {
		R.xml.key_2touch_flick_top_0,
		R.xml.key_2touch_shift_0,
		R.xml.key_2touch_a_0,
		R.xml.key_2touch_k_0,
		R.xml.key_2touch_s_0,
		R.xml.key_2touch_t_0,
		R.xml.key_2touch_n_0,
		R.xml.key_2touch_h_0,
		R.xml.key_2touch_m_0,
		R.xml.key_2touch_y_0,
		R.xml.key_2touch_r_0,
		R.xml.key_2touch_w_0,
		R.xml.key_2touch_flick_input_top_0,
		R.xml.key_2touch_input_shift_0,
		R.xml.key_2touch_input_a_0,
		R.xml.key_2touch_input_k_0,
		R.xml.key_2touch_input_s_0,
		R.xml.key_2touch_input_t_0,
		R.xml.key_2touch_input_n_0,
		R.xml.key_2touch_input_h_0,
		R.xml.key_2touch_input_m_0,
		R.xml.key_2touch_input_y_0,
		R.xml.key_2touch_input_r_0,
		R.xml.key_2touch_input_w_0,

		R.xml.key_2touch_flick_katakana_full_top_0,
		R.xml.keyboard_12key_full_katakana_shift_0,
		R.xml.key_2touch_katakana_full_a_0,
		R.xml.key_2touch_katakana_full_k_0,
		R.xml.key_2touch_katakana_full_s_0,
		R.xml.key_2touch_katakana_full_t_0,
		R.xml.key_2touch_katakana_full_n_0,
		R.xml.key_2touch_katakana_full_h_0,
		R.xml.key_2touch_katakana_full_m_0,
		R.xml.key_2touch_katakana_full_y_0,
		R.xml.key_2touch_katakana_full_r_0,
		R.xml.key_2touch_katakana_full_w_0,
		R.xml.key_2touch_flick_katakana_full_input_top_0,
		R.xml.keyboard_12key_full_katakana_input_shift_0,
		R.xml.key_2touch_katakana_full_input_a_0,
		R.xml.key_2touch_katakana_full_input_k_0,
		R.xml.key_2touch_katakana_full_input_s_0,
		R.xml.key_2touch_katakana_full_input_t_0,
		R.xml.key_2touch_katakana_full_input_n_0,
		R.xml.key_2touch_katakana_full_input_h_0,
		R.xml.key_2touch_katakana_full_input_m_0,
		R.xml.key_2touch_katakana_full_input_y_0,
		R.xml.key_2touch_katakana_full_input_r_0,
		R.xml.key_2touch_katakana_full_input_w_0,

		R.xml.key_2touch_flick_katakana_half_top_0,
		R.xml.keyboard_12key_half_katakana_shift_0,
		R.xml.key_2touch_katakana_half_a_0,
		R.xml.key_2touch_katakana_half_k_0,
		R.xml.key_2touch_katakana_half_s_0,
		R.xml.key_2touch_katakana_half_t_0,
		R.xml.key_2touch_katakana_half_n_0,
		R.xml.key_2touch_katakana_half_h_0,
		R.xml.key_2touch_katakana_half_m_0,
		R.xml.key_2touch_katakana_half_y_0,
		R.xml.key_2touch_katakana_half_r_0,
		R.xml.key_2touch_katakana_half_w_0,
		R.xml.key_2touch_flick_katakana_half_input_top_0,
		R.xml.keyboard_12key_half_katakana_input_shift_0,
		R.xml.key_2touch_katakana_half_input_a_0,
		R.xml.key_2touch_katakana_half_input_k_0,
		R.xml.key_2touch_katakana_half_input_s_0,
		R.xml.key_2touch_katakana_half_input_t_0,
		R.xml.key_2touch_katakana_half_input_n_0,
		R.xml.key_2touch_katakana_half_input_h_0,
		R.xml.key_2touch_katakana_half_input_m_0,
		R.xml.key_2touch_katakana_half_input_y_0,
		R.xml.key_2touch_katakana_half_input_r_0,
		R.xml.key_2touch_katakana_half_input_w_0,
	};
}
/******************** end of file ********************/
