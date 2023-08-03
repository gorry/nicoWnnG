/*
 * Copyright (C) 2008,2009  OMRON SOFTWARE Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gorry.android.input.nicownng.JAJP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;

import net.gorry.android.input.nicownng.CandidateFilter;
import net.gorry.android.input.nicownng.ComposingText;
import net.gorry.android.input.nicownng.NicoWnnG;
import net.gorry.android.input.nicownng.NicoWnnGDictionaryImpl;
import net.gorry.android.input.nicownng.StrSegmentClause;
import net.gorry.android.input.nicownng.WnnClause;
import net.gorry.android.input.nicownng.WnnDictionary;
import net.gorry.android.input.nicownng.WnnEngine;
import net.gorry.android.input.nicownng.WnnSentence;
import net.gorry.android.input.nicownng.WnnWord;


import android.content.SharedPreferences;
import android.util.Log;

/**
 * The OpenWnn engine class for Japanese IME.
 * 
 * @author Copyright (C) 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class NicoWnnGEngineJAJP implements WnnEngine {
    /** Current dictionary type */
    private int mDictType = DIC_LANG_INIT;
    /** Dictionary type (default) */
    public static final int DIC_LANG_INIT = 0;
    /** Dictionary type (Japanese standard) */
    public static final int DIC_LANG_JP = 0;
    /** Dictionary type (English standard) */
    public static final int DIC_LANG_EN = 1;
    /** Dictionary type (Japanese person's name) */
    public static final int DIC_LANG_JP_PERSON_NAME = 2;
    /** Dictionary type (User dictionary) */
    public static final int DIC_USERDIC = 3;
    /** Dictionary type (Japanese EISU-KANA conversion) */
    public static final int DIC_LANG_JP_EISUKANA = 4;
    /** Dictionary type (e-mail/URI) */
    public static final int DIC_LANG_EN_EMAIL_ADDRESS = 5;
    /** Dictionary type (Japanese postal address) */
    public static final int DIC_LANG_JP_POSTAL_ADDRESS = 6;

    /** Type of the keyboard */
    private int mKeyboardType = KEYBOARD_UNDEF;
    /** Keyboard type (not defined) */
    public static final int KEYBOARD_UNDEF = 0;
    /** Keyboard type (12-keys) */
    public static final int KEYBOARD_KEYPAD12 = 1;
    /** Keyboard type (Qwerty) */
    public static final int KEYBOARD_QWERTY = 2;
    
    /** Score(frequency value) of word in the learning dictionary */
    public static final int FREQ_LEARN = 600;
    /** Score(frequency value) of word in the user dictionary */
    public static final int FREQ_USER = 500;

    /** Maximum limit length of output */
    public static final int MAX_OUTPUT_LENGTH = 50;
    /** Limitation of predicted candidates */
    public static final int PREDICT_LIMIT = 100;
   
    /** OpenWnn dictionary */
    private WnnDictionary mDictionaryJP;

    /** Word list */
    private ArrayList<WnnWord> mConvResult;

    /** HashMap for checking duplicate word */
    private HashMap<String, WnnWord> mCandTable;

    /** Input string (Hiragana) */
    private String mInputHiragana;
    
    /** Input string (Romaji) */
    private String mInputRomaji;
    
    /** Number of output candidates */
    private int mOutputNum;
    
    /**
     * Where to get the next candidates from.<br>
     * (0:prefix search from the dictionary, 1:single clause converter, 2:Kana converter)
     */
    private int mGetCandidateFrom;
    
    /** Previously selected word */
    private WnnWord mPreviousWord;

    /** Converter for single/consecutive clause conversion */
    private NicoWnnGClauseConverterJAJP mClauseConverter;

    /** Kana converter (for EISU-KANA conversion) */
    private KanaConverter mKanaConverter;

    /** Whether exact match search or prefix match search */
    private boolean mExactMatchMode;

    /** Whether displaying single clause candidates or not */
    private boolean mSingleClauseMode;

    /** A result of consecutive clause conversion */
    private WnnSentence mConvertSentence;
    
    /** Add Special Candidate on 12key toggle kana mode */
    private int mSpecialCandidateOnKana12KeyToggleMode;

    /** The candidate filter */
    private CandidateFilter mFilter = null;

    /**
     * Constructor
     * 
     * @param writableDictionaryName    Writable dictionary file name(null if not use)
     */
    public NicoWnnGEngineJAJP(String writableDictionaryName) {
        /* load Japanese dictionary library */
        mDictionaryJP = new NicoWnnGDictionaryImpl(
                // "/data/data/net.gorry.android.input.nicownng/lib/libnicoWnnGJpnDic.so",
                "libnicoWnnGJpnDic.so",
                writableDictionaryName );
        if (!mDictionaryJP.isActive()) {
            mDictionaryJP = new NicoWnnGDictionaryImpl(
                    "/system/lib/libnicoWnnGJpnDic.so",
                    writableDictionaryName );
        }

        /* clear dictionary settings */
        mDictionaryJP.clearDictionary();
        mDictionaryJP.clearApproxPattern();
        mDictionaryJP.setInUseState(false);

        /* work buffers */
        mConvResult = new ArrayList<WnnWord>();
        mCandTable = new HashMap<String, WnnWord>();

        /* converters */
        mClauseConverter = new NicoWnnGClauseConverterJAJP();
        mKanaConverter = new KanaConverter();
    }

    /**
     * Set dictionary for prediction.
     * 
     * @param strlen        Length of input string
     */
    private void setDictionaryForPrediction(int strlen) {
        WnnDictionary dict = mDictionaryJP;

        dict.clearDictionary();

        if (mDictType != DIC_LANG_JP_EISUKANA) {
            dict.clearApproxPattern();
            if (strlen == 0) {
                dict.setDictionary(2, 245, 245);
                dict.setDictionary(3, 100, 244);
                
                dict.setDictionary(WnnDictionary.INDEX_LEARN_DICTIONARY, FREQ_LEARN, FREQ_LEARN);
            } else {
                dict.setDictionary(0, 100, 400);
                if (strlen > 1) {
                    dict.setDictionary(1, 100, 400);
                }
                dict.setDictionary(2, 245, 245);
                dict.setDictionary(3, 100, 244);
                
                dict.setDictionary(WnnDictionary.INDEX_USER_DICTIONARY, FREQ_USER, FREQ_USER);
                dict.setDictionary(WnnDictionary.INDEX_LEARN_DICTIONARY, FREQ_LEARN, FREQ_LEARN);
                if (mSpecialCandidateOnKana12KeyToggleMode > 0) {
                    dict.setApproxPattern(WnnDictionary.APPROX_PATTERN_JAJP_12KEY_NORMAL);
                }
            }
        }
    }

    /**
     * Get a candidate.
     *
     * @param index     Index of a candidate.
     * @return          The candidate; {@code null} if there is no candidate.
     */
    private WnnWord getCandidate(int index) {
        WnnWord word;

        if (mGetCandidateFrom == 0) {
            if (mDictType == NicoWnnGEngineJAJP.DIC_LANG_JP_EISUKANA) {
                /* skip to Kana conversion if EISU-KANA conversion mode */
                mGetCandidateFrom = 2;
            } else if (mSingleClauseMode) {
                /* skip to single clause conversion if single clause conversion mode */
                mGetCandidateFrom = 1;
            } else {
                if (mConvResult.size() < PREDICT_LIMIT) {
                    /* get prefix matching words from the dictionaries */
                    while (index >= mConvResult.size()) {
                        if ((word = mDictionaryJP.getNextWord()) == null) {
                            mGetCandidateFrom = 1;
                            break;
                        }
                        if (!mExactMatchMode || mInputHiragana.equals(word.stroke)) {
                            addCandidate(word);
                            if (mConvResult.size() >= PREDICT_LIMIT) {
                                mGetCandidateFrom = 1;
                                break;
                            }
                        }
                    }
                } else {
                    mGetCandidateFrom = 1;
                }
            }
        }

        /* get candidates by single clause conversion */
        if (mGetCandidateFrom == 1) {
            Iterator<?> convResult = mClauseConverter.convert(mInputHiragana);
            if (convResult != null) {
                while (convResult.hasNext()) {
                    addCandidate((WnnWord)convResult.next());
                }
            }
            /* end of candidates by single clause conversion */
            mGetCandidateFrom = 2;
        }
        
        /* get candidates from Kana converter */
        if (mGetCandidateFrom == 2) {
            List<WnnWord> addCandidateList
            = mKanaConverter.createPseudoCandidateList(mInputHiragana, mInputRomaji, mKeyboardType, mSpecialCandidateOnKana12KeyToggleMode);
            
            Iterator<WnnWord> it = addCandidateList.iterator();
            while(it.hasNext()) {
                addCandidate(it.next());
            }

            mGetCandidateFrom = 3;
        }

        if (index >= mConvResult.size()) {
            return null;
        }
        return (WnnWord)mConvResult.get(index);
    }

    /**
     * Add a candidate to the conversion result buffer.
     * <br>
     * This method adds a word to the result buffer if there is not
     * the same one in the buffer and the length of the candidate
     * string is not longer than {@code MAX_OUTPUT_LENGTH}.
     *
     * @param word      A word to be add
     * @return          {@code true} if the word added; {@code false} if not.
     */
    private boolean addCandidate(WnnWord word) {
        if (word.candidate == null || mCandTable.containsKey(word.candidate)
                || word.candidate.length() > MAX_OUTPUT_LENGTH) {
            return false;
        }
        if (mFilter != null && !mFilter.isAllowed(word)) {
            return false;
        }
        mCandTable.put(word.candidate, word);
        mConvResult.add(word);
        return true;
    }

    /**
     * Clear work area that hold candidates information.
     */
    private void clearCandidates() {
        mConvResult.clear();
        mCandTable.clear();
        mOutputNum = 0;
        mInputHiragana = null;
        mInputRomaji = null;
        mGetCandidateFrom = 0;
        mSingleClauseMode = false;
    }

    /**
     * Set dictionary type.
     *
     * @param type      Type of dictionary
     * @return          {@code true} if the dictionary is changed; {@code false} if not.
     */
    public boolean setDictionary(int type) {
        mDictType = type;
        return true;
    }

    /**
     * Set the search key and the search mode from {@link ComposingText}.
     *
     * @param text      Input text
     * @param maxLen    Maximum length to convert
     * @return          Length of the search key
     */
    private int setSearchKey(ComposingText text, int maxLen) {
        String input = text.toString(ComposingText.LAYER1);
        if (0 <= maxLen && maxLen <= input.length()) {
            input = input.substring(0, maxLen);
            mExactMatchMode = true;
        } else {
            mExactMatchMode = false;
        }

        if (input.length() == 0) {
            mInputHiragana = "";
            mInputRomaji = "";
            return 0;
        }

        mInputHiragana = input;
        mInputRomaji = text.toString(ComposingText.LAYER0);

        return input.length();
    }

    /**
     * Clear the previous word's information.
     */
    public void clearPreviousWord() {
        mPreviousWord = null;
    }

    /**
     * Set keyboard type.
     * 
     * @param keyboardType      Type of keyboard
     */
    public void setKeyboardType(int keyboardType) {
        mKeyboardType = keyboardType;
    }

    /**
     * Set the candidate filter
     * 
     * @param filter    The candidate filter
     */
    public void setFilter(CandidateFilter filter) {
        mFilter = filter;
        mClauseConverter.setFilter(filter);
    }
    
    /***********************************************************************
     * WnnEngine's interface
     **********************************************************************/
    /** @see net.gorry.android.input.nicownng.WnnEngine#init */
    public void init() {
        clearPreviousWord();
        mClauseConverter.setDictionary(mDictionaryJP);
        mKanaConverter.setDictionary(mDictionaryJP);
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#close */
    public void close() {}

    /** @see net.gorry.android.input.nicownng.WnnEngine#predict */
    public int predict(ComposingText text, int minLen, int maxLen) {
        clearCandidates();
        if (text == null) { return 0; }

        /* set mInputHiragana and mInputRomaji */
        int len = setSearchKey(text, maxLen);

        /* set dictionaries by the length of input */
        setDictionaryForPrediction(len);
        
        /* search dictionaries */
        mDictionaryJP.setInUseState( true );

        if (len == 0) {
            /* search by previously selected word */
            return mDictionaryJP.searchWord(WnnDictionary.SEARCH_LINK, WnnDictionary.ORDER_BY_FREQUENCY,
                                            mInputHiragana, mPreviousWord);
        } else {
            if (mExactMatchMode) {
                /* exact matching */
                mDictionaryJP.searchWord(WnnDictionary.SEARCH_EXACT, WnnDictionary.ORDER_BY_FREQUENCY,
                                         mInputHiragana);
            } else {
                /* prefix matching */
                mDictionaryJP.searchWord(WnnDictionary.SEARCH_PREFIX, WnnDictionary.ORDER_BY_FREQUENCY,
                                         mInputHiragana);
            }
            return 1;
        }
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#convert */
    public int convert(ComposingText text) {
        clearCandidates();

        if (text == null) {
            return 0;
        }

        mDictionaryJP.setInUseState( true );

        int cursor = text.getCursor(ComposingText.LAYER1);
        String input;
        WnnClause head = null;
        if (cursor > 0) {
            /* convert previous part from cursor */
            input = text.toString(ComposingText.LAYER1, 0, cursor - 1);
            Iterator headCandidates = mClauseConverter.convert(input);
            if ((headCandidates == null) || (!headCandidates.hasNext())) {
                return 0;
            }
            head = new WnnClause(input, (WnnWord)headCandidates.next());

            /* set the rest of input string */
            input = text.toString(ComposingText.LAYER1, cursor, text.size(ComposingText.LAYER1) - 1);
        } else {
            /* set whole of input string */
            input = text.toString(ComposingText.LAYER1);
        }

        WnnSentence sentence = null;
        if (input.length() != 0) {
            sentence = mClauseConverter.consecutiveClauseConvert(input);
        }
        if (head != null) {
            sentence = new WnnSentence(head, sentence);
        }
        if (sentence == null) {
            return 0;
        }

        StrSegmentClause[] ss = new StrSegmentClause[sentence.elements.size()];
        int pos = 0;
        int idx = 0;
        Iterator<WnnClause> it = sentence.elements.iterator();
        while(it.hasNext()) {
            WnnClause clause = (WnnClause)it.next();
            int len = clause.stroke.length();
            ss[idx] = new StrSegmentClause(clause, pos, pos + len - 1);
            pos += len;
            idx += 1;
        }
        text.setCursor(ComposingText.LAYER2, text.size(ComposingText.LAYER2));
        text.replaceStrSegment(ComposingText.LAYER2, ss, 
                               text.getCursor(ComposingText.LAYER2));
        mConvertSentence = sentence;

        return 0;
    }
    
    /** @see net.gorry.android.input.nicownng.WnnEngine#searchWords */
    public int searchWords(String key) {
        clearCandidates();
        return 0;
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#searchWords */
    public int searchWords(WnnWord word) {
        clearCandidates();
        return 0;
    }

    /** reset word count */
    public void resetCandidate() {
        mOutputNum = 0;
    }
    /** @see net.gorry.android.input.nicownng.WnnEngine#getNextCandidate */
    public WnnWord getNextCandidate() {
        if (mInputHiragana == null) {
            return null;
        }
        WnnWord word = getCandidate(mOutputNum);
        if (word != null) {
            mOutputNum++;
        }
        return word;
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#learn */
    public boolean learn(WnnWord word) {
        int ret = -1;
        if (word.partOfSpeech.right == 0) {
            word.partOfSpeech = mDictionaryJP.getPOS(WnnDictionary.POS_TYPE_MEISI);
        }

        WnnDictionary dict = mDictionaryJP;
        if (word instanceof WnnSentence) {
            Iterator<WnnClause> clauses = ((WnnSentence)word).elements.iterator();
            while (clauses.hasNext()) {
                WnnWord wd = clauses.next();
                if (mPreviousWord != null) {
                    ret = dict.learnWord(wd, mPreviousWord);
                } else {
                    ret = dict.learnWord(wd);
                }
                mPreviousWord = wd;
                if (ret != 0) {
                    break;
                }
            }
        } else {
            if (mPreviousWord != null) {
                ret = dict.learnWord(word, mPreviousWord);
            } else {
                ret = dict.learnWord(word);
            }
            mPreviousWord = word;
            mClauseConverter.setDictionary(dict);
        }

        return (ret == 0);
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#forget */
    public boolean forget(WnnWord word) {
        int ret = -1;
        if (word.partOfSpeech.right == 0) {
            word.partOfSpeech = mDictionaryJP.getPOS(WnnDictionary.POS_TYPE_MEISI);
        }

        WnnDictionary dict = mDictionaryJP;
        ret = dict.forgetWord(word);
        mClauseConverter.setDictionary(dict);

        return (ret == 0);
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#addWord */
    public int addWord(WnnWord word) {
        mDictionaryJP.setInUseState( true );
        if (word.partOfSpeech.right == 0) {
            word.partOfSpeech = mDictionaryJP.getPOS(WnnDictionary.POS_TYPE_MEISI);
        }
        mDictionaryJP.addWordToUserDictionary(word);
        mDictionaryJP.setInUseState( false );
        return 0;
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#addWord */
    public int addWords(WnnWord[] words, Runnable progress) {
        mDictionaryJP.setInUseState( true );
        for (int i=0; i<words.length; i++) {
        	if (words[i].partOfSpeech.right == 0) {
        		words[i].partOfSpeech = mDictionaryJP.getPOS(WnnDictionary.POS_TYPE_MEISI);
        	}
        }
        mDictionaryJP.addWordToUserDictionary(words, progress);
        mDictionaryJP.setInUseState( false );
        return 0;
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#deleteWord */
    public boolean deleteWord(WnnWord word) {
        mDictionaryJP.setInUseState( true );
        mDictionaryJP.removeWordFromUserDictionary(word);
        mDictionaryJP.setInUseState( false );
        return false;
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#setPreferences */
    public void setPreferences(SharedPreferences pref) {}

    /** @see net.gorry.android.input.nicownng.WnnEngine#breakSequence */
    public void breakSequence()  {
        clearPreviousWord();
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#makeCandidateListOf */
    public int makeCandidateListOf(int clausePosition)  {
        clearCandidates();

        if ((mConvertSentence == null) || (mConvertSentence.elements.size() <= clausePosition)) {
            return 0;
        }
        mSingleClauseMode = true;
        WnnClause clause = mConvertSentence.elements.get(clausePosition);
        mInputHiragana = clause.stroke;
        mInputRomaji = clause.candidate;

        return 1;
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#initializeDictionary */
    public boolean initializeDictionary(int dictionary)  {
        switch( dictionary ) {
        case WnnEngine.DICTIONARY_TYPE_LEARN:
            mDictionaryJP.setInUseState( true );
            mDictionaryJP.clearLearnDictionary();
            mDictionaryJP.setInUseState( false );
            return true;

        case WnnEngine.DICTIONARY_TYPE_USER:
            mDictionaryJP.setInUseState( true );
            mDictionaryJP.clearUserDictionary();
            mDictionaryJP.setInUseState( false );
            return true;
        }
        return false;
    }

    /** @see net.gorry.android.input.nicownng.WnnEngine#initializeDictionary */
    public boolean initializeDictionary(int dictionary, int type) {
        return initializeDictionary(dictionary);
    }
    
    /** @see net.gorry.android.input.nicownng.WnnEngine#getUserDictionaryWords */
    public WnnWord[] getUserDictionaryWords( ) {
        /* get words in the user dictionary */
        mDictionaryJP.setInUseState(true);
        WnnWord[] result = mDictionaryJP.getUserDictionaryWords( );
        mDictionaryJP.setInUseState(false);

        /* sort the array of words */
        Arrays.sort(result, new WnnWordComparator());

        return result;
    }

    /* {@link WnnWord} comparator for listing up words in the user dictionary */
    private class WnnWordComparator implements java.util.Comparator {
        public int compare(Object object1, Object object2) {
            WnnWord wnnWord1 = (WnnWord) object1;
            WnnWord wnnWord2 = (WnnWord) object2;
            return wnnWord1.stroke.compareTo(wnnWord2.stroke);
        }
    }
    
    public void setSpecialCandidateOnKana12KeyToggleMode(int sw) {
        mSpecialCandidateOnKana12KeyToggleMode = sw;
    }
    
    public int getSpecialCandidateOnKana12KeyToggleMode() {
        return mSpecialCandidateOnKana12KeyToggleMode;
    }
}
