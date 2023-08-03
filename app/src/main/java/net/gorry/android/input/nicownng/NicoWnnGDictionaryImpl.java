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

package net.gorry.android.input.nicownng;

import java.io.File;

import android.R.integer;
import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;

import android.util.Log;

/**
 * The implementation class of WnnDictionary interface (JNI wrapper class).
 *
 * @author Copyright (C) 2008, 2009 OMRON SOFTWARE CO., LTD.  All Rights Reserved.
 */
public class NicoWnnGDictionaryImpl implements WnnDictionary {
	private static final String TAG = "NicoWnnGDictionaryImpl";
    /*
     * DEFINITION FOR JNI
     */
    static {
        /* Load the dictionary search library */ 
        System.loadLibrary( "nicownngdict" );
    }

	private static String M() {
		StackTraceElement[] es = new Exception().getStackTrace();
		int count = 1; while (es[count].getMethodName().contains("$")) count++;
		return es[count].getFileName()+"("+es[count].getLineNumber()+"): "+es[count].getMethodName()+"()";
	}

	private static final boolean T = false; // true;

    /*
     * DEFINITION OF CONSTANTS
     */
    /** The maximum length of stroke */
    public static final int MAX_STROKE_LENGTH       = 50;
    /** The maximum length of candidate */
    public static final int MAX_CANDIDATE_LENGTH    = 50;
    /** The table name of writable dictionary on the database */
    protected static final String TABLE_NAME_DIC    = "dic";
    /** The type name of user word */
    protected static final int TYPE_NAME_USER   = 0;
    /** The type name of learn word */
    protected static final int TYPE_NAME_LEARN  = 1;

    /** The column name of database */
    protected static final String COLUMN_NAME_ID                 = "rowid";
    /** The column name of database  */
    protected static final String COLUMN_NAME_TYPE               = "type";
    /** The column name of database  */
    protected static final String COLUMN_NAME_STROKE             = "stroke";
    /** The column name of database  */
    protected static final String COLUMN_NAME_CANDIDATE          = "candidate";
    /** The column name of database  */
    protected static final String COLUMN_NAME_POS_LEFT           = "posLeft";
    /** The column name of database  */
    protected static final String COLUMN_NAME_POS_RIGHT          = "posRight";
    /** The column name of database  */
    protected static final String COLUMN_NAME_PREVIOUS_STROKE    = "prevStroke";
    /** The column name of database  */
    protected static final String COLUMN_NAME_PREVIOUS_CANDIDATE = "prevCandidate";
    /** The column name of database  */
    protected static final String COLUMN_NAME_PREVIOUS_POS_LEFT  = "prevPosLeft";
    /** The column name of database  */
    protected static final String COLUMN_NAME_PREVIOUS_POS_RIGHT = "prevPosRight";

    /** Query for normal search */
    protected static final String NORMAL_QUERY =
        "select distinct " + COLUMN_NAME_STROKE + "," +
                             COLUMN_NAME_CANDIDATE + "," +
                             COLUMN_NAME_POS_LEFT + "," +
                             COLUMN_NAME_POS_RIGHT + "," +
                             COLUMN_NAME_TYPE +
                  " from " + TABLE_NAME_DIC + " where %s order by " +
                             COLUMN_NAME_TYPE + " DESC, %s";

    /** Query for link search */
    protected static final String LINK_QUERY =
        "select distinct " + COLUMN_NAME_STROKE + "," +
                             COLUMN_NAME_CANDIDATE + "," +
                             COLUMN_NAME_POS_LEFT + "," +
                             COLUMN_NAME_POS_RIGHT + "," +
                             COLUMN_NAME_TYPE +
                  " from " + TABLE_NAME_DIC + " where %s = ? and %s = ? and %s order by " +
                             COLUMN_NAME_TYPE + " DESC, %s";

    /** The max words of user dictionary */
    protected static final int MAX_WORDS_IN_USER_DICTIONARY     = 10000;
    /** The max words of learning dictionary */
    protected static final int MAX_WORDS_IN_LEARN_DICTIONARY    = 2000;

    /** The base frequency of user dictionary */
    protected static final int OFFSET_FREQUENCY_OF_USER_DICTIONARY  = 1000;
    /** The base frequency of learning dictionary */
    protected static final int OFFSET_FREQUENCY_OF_LEARN_DICTIONARY = 2000;

    /*
     * Constants to define the upper limit of query.
     *
     * That is used to fix the size of query expression.
     * If the number of approximate patterns for a character is exceeded MAX_PATTERN_OF_APPROX,
     * increase that constant to the maximum number of patterns.
     */
    /** Constants to define the upper limit of approximate patterns */
    protected final static int MAX_PATTERN_OF_APPROX    = 6;
    /** Constants to define the upper limit of length of a query */
    protected final static int MAX_LENGTH_OF_QUERY      = 50;
    /**
     * Constants to define the turn around time of query.
     * <br>
     * It can be set between 1 to {@code MAX_LENGTH_OF_QUERY}. If the length of query
     * string is shorter than {@code FAST_QUERY_LENGTH}, the simple search logic is applied.
     * Therefore, the turn around time for short query string is fast so that it is short.
     * However, the difference of turn around time at the border length grows big.
     * the value should be fixed carefully.
     */
    protected final static int FAST_QUERY_LENGTH        = 20;

    /*
     * DEFINITION OF PRIVATE FIELD
     */
    /** Internal work area for the dictionary search library */
    protected long mWnnWork = 0;

    /** The file path of the writable dictionary */
    protected String mDicFilePath = "";
    /** The writable dictionary object */
    protected SQLiteDatabase mDbDic = null;
    /** The search cursor of the writable dictionary */
    protected SQLiteCursor mDbCursor = null;
    /** The number of queried items */
    protected int mCountCursor = 0;
    /** The type of the search cursor object */
    protected int mTypeOfQuery = -1;

    /** The query base strings for query operation */
    protected String mExactQuerySqlOrderByFreq;
    /** The query base strings for query operation */
    protected String mExactQuerySqlOrderByKey;

    /** The query base strings for query operation */
    protected String mFullPrefixQuerySqlOrderByFreq;
    /** The query base strings for query operation */
    protected String mFastPrefixQuerySqlOrderByFreq;
    /** The query base strings for query operation */
    protected String mFullPrefixQuerySqlOrderByKey;
    /** The query base strings for query operation */
    protected String mFastPrefixQuerySqlOrderByKey;

    /** The query base strings for query operation */
    protected String mFullLinkQuerySqlOrderByFreq;
    /** The query base strings for query operation */
    protected String mFastLinkQuerySqlOrderByFreq;
    /** The query base strings for query operation */
    protected String mFullLinkQuerySqlOrderByKey;
    /** The query base strings for query operation */
    protected String mFastLinkQuerySqlOrderByKey;

    /** The string array used by query operation (for "selection") */
    protected String mExactQueryArgs[] = new String[ 1 ];
    /** The string array used by query operation (for "selection") */
    protected String mFullQueryArgs[] = new String[ MAX_LENGTH_OF_QUERY * (MAX_PATTERN_OF_APPROX+1) ];
    /** The string array used by query operation (for "selection") */
    protected String mFastQueryArgs[] = new String[ FAST_QUERY_LENGTH * (MAX_PATTERN_OF_APPROX+1) ];

    /** The Frequency offset of user dictionary */
    protected int mFrequencyOffsetOfUserDictionary = -1;
    /** The Frequency offset of learn dictionary */
    protected int mFrequencyOffsetOfLearnDictionary = -1;

    /*
     * DEFINITION OF METHODS
     */
    /**
     * The constructor of this class without writable dictionary.
     *
     * Create a internal work area for the search engine. It is allocated for each object.
     *
     * @param dicLibPath    The dictionary library file path
     */
    public NicoWnnGDictionaryImpl( String dicLibPath ) {
        this( dicLibPath, null );
    }

    /**
     * The constructor of this class with writable dictionary.
     *
     * Create a internal work area and the writable dictionary for the search engine. It is allocated for each object.
     *
     * @param dicLibPath    The dictionary library file path
     * @param dicFilePath   The path name of writable dictionary
     */
    public NicoWnnGDictionaryImpl( String dicLibPath, String dicFilePath ) {
        if (T) Log.v(TAG, M()+"@in: dicLibPath="+dicLibPath+", dicFilePath="+dicFilePath);

        /* Create the internal work area */
        this.mWnnWork = OpenWnnDictionaryImplJni.createWnnWork( dicLibPath );
        if (T) Log.v(TAG, M()+"mWnnWork="+this.mWnnWork);
        if ((-10 <= this.mWnnWork) && (this.mWnnWork < 0)) {
            this.mWnnWork = 0;
        }

        if( this.mWnnWork != 0 && dicFilePath != null ) {
            /* Create query base strings */
            String queryFullBaseString = 
                OpenWnnDictionaryImplJni.createQueryStringBase(
                    this.mWnnWork,
                    MAX_LENGTH_OF_QUERY,
                    MAX_PATTERN_OF_APPROX,
                    COLUMN_NAME_STROKE );

            String queryFastBaseString = 
                OpenWnnDictionaryImplJni.createQueryStringBase(
                    this.mWnnWork,
                    FAST_QUERY_LENGTH,
                    MAX_PATTERN_OF_APPROX,
                    COLUMN_NAME_STROKE );


            mExactQuerySqlOrderByFreq = String.format(
                NORMAL_QUERY,
                String.format( "%s=?", COLUMN_NAME_STROKE ), String.format( "%s DESC", COLUMN_NAME_ID ) );

            mExactQuerySqlOrderByKey = String.format(
                NORMAL_QUERY,
                String.format( "%s=?", COLUMN_NAME_STROKE ), COLUMN_NAME_STROKE );


            mFullPrefixQuerySqlOrderByFreq = String.format(
                NORMAL_QUERY,
                queryFullBaseString, String.format( "%s DESC", COLUMN_NAME_ID ) );

            mFastPrefixQuerySqlOrderByFreq = String.format(
                NORMAL_QUERY,
                queryFastBaseString, String.format( "%s DESC", COLUMN_NAME_ID ) );

            mFullPrefixQuerySqlOrderByKey = String.format(
                NORMAL_QUERY,
                queryFullBaseString, COLUMN_NAME_STROKE );

            mFastPrefixQuerySqlOrderByKey = String.format(
                NORMAL_QUERY,
                queryFastBaseString, COLUMN_NAME_STROKE );


            mFullLinkQuerySqlOrderByFreq = String.format(
                LINK_QUERY, COLUMN_NAME_PREVIOUS_STROKE, COLUMN_NAME_PREVIOUS_CANDIDATE,
                queryFullBaseString, String.format( "%s DESC", COLUMN_NAME_ID ) );

            mFastLinkQuerySqlOrderByFreq = String.format(
                LINK_QUERY, COLUMN_NAME_PREVIOUS_STROKE, COLUMN_NAME_PREVIOUS_CANDIDATE,
                queryFastBaseString, String.format( "%s DESC", COLUMN_NAME_ID ) );

            mFullLinkQuerySqlOrderByKey = String.format(
                LINK_QUERY, COLUMN_NAME_PREVIOUS_STROKE, COLUMN_NAME_PREVIOUS_CANDIDATE,
                queryFullBaseString, COLUMN_NAME_STROKE );

            mFastLinkQuerySqlOrderByKey = String.format(
                LINK_QUERY, COLUMN_NAME_PREVIOUS_STROKE, COLUMN_NAME_PREVIOUS_CANDIDATE,
                queryFastBaseString, COLUMN_NAME_STROKE );


            try {
                /* Create the database object */
                mDicFilePath = dicFilePath;
                setInUseState( true );

                /* Create the table if not exist */
                createDictionaryTable( TABLE_NAME_DIC );
            } catch( SQLException e ) {
            }
        }

        if (T) Log.v(TAG, M()+"@out");
    }

    /**
     * The finalizer of this class.
     * Destroy the internal work area for the search engine.
     */
    protected void finalize( ) {
        /* Free the internal work area */
        if( this.mWnnWork != 0 ) {
            OpenWnnDictionaryImplJni.freeWnnWork( this.mWnnWork );
            this.mWnnWork = 0;

            freeDatabase();
        }
    }

    /**
     * Create the table of writable dictionary.
     *
     * @param tableName     The name of table
     */
    protected void createDictionaryTable( String tableName ) {
        String sqlStr = "create table if not exists " + tableName +
            " (" + COLUMN_NAME_ID                 + " integer primary key autoincrement, " + 
                   COLUMN_NAME_TYPE               + " integer, " +
                   COLUMN_NAME_STROKE             + " text, " + 
                   COLUMN_NAME_CANDIDATE          + " text, " +
                   COLUMN_NAME_POS_LEFT           + " integer, " +
                   COLUMN_NAME_POS_RIGHT          + " integer, " +
                   COLUMN_NAME_PREVIOUS_STROKE    + " text, " + 
                   COLUMN_NAME_PREVIOUS_CANDIDATE + " text, " +
                   COLUMN_NAME_PREVIOUS_POS_LEFT  + " integer, " +
                   COLUMN_NAME_PREVIOUS_POS_RIGHT + " integer)";

        if( mDbDic != null ) {
            mDbDic.execSQL( sqlStr );
        }
    }

    /**
     * Free the {@link SQLiteDatabase} of writable dictionary.
     */
    protected void freeDatabase( ) {
        freeCursor();

        if( mDbDic != null ) {
            /* The SQLiteDataBase object must close() before releasing. */
            mDbDic.close();
            mDbDic = null;
        }
    }
    /**
     * Free the {@link SQLiteCursor} of writable dictionary.
     */
    protected void freeCursor( ) {
        if( mDbCursor != null) {
            /* The SQLiteCursor object must close() before releasing. */
            mDbCursor.close();
            mDbCursor = null;

            mTypeOfQuery = -1;
        }
    }

    
    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#setInUseState
     */
    public boolean isActive() {
        return (this.mWnnWork != 0);
    }
    
    public void setDicFilePermission600(String path) {
        if (T) Log.v(TAG, M()+"@in: path="+path);

    	int ret = OpenWnnDictionaryImplJni.setDicFilePermission600( path );
    	if (ret == 0) {
    		Log.w(TAG, "failed to chmod "+path);
    	} else if (ret == 1) {
    		Log.w(TAG, "success to chmod "+path);
    	}

        if (T) Log.v(TAG, M()+"@out");
    }
    
    public void moveOldDicFile(String pathOld, String pathNew) {
        if (T) Log.v(TAG, M()+"@in: pathOld="+pathOld+", pathNew="+pathNew);

    	int ret = OpenWnnDictionaryImplJni.moveOldDicFile(pathOld, pathNew);
    	if (ret == 0) {
    		Log.w(TAG, "failed to rename "+pathOld+" to "+pathNew);
    	} else if (ret == 1) {
    		Log.w(TAG, "success to rename "+pathOld+" to "+pathNew);
    	}

        if (T) Log.v(TAG, M()+"@out");
    }
    
    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#setInUseState
     */
    public void setInUseState( boolean flag ) {
        if (T) Log.v(TAG, M()+"@in: flag="+flag);

        if( flag ) {
            if( mDbDic == null ) {
            	String dicFolder = mDicFilePath.replaceFirst("/databases/.*", "/databases");
            	File dir = new File(dicFolder);
            	dir.mkdir();
            	String oldDicPath = mDicFilePath.replaceFirst("/databases/", "/");
            	moveOldDicFile(oldDicPath, mDicFilePath);
                mDbDic = SQLiteDatabase.openOrCreateDatabase( mDicFilePath, null );
            	setDicFilePermission600(mDicFilePath);
            }
        } else {
            freeDatabase();
        }

        if (T) Log.v(TAG, M()+"@out");
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#clearDictionary
     */
    public int clearDictionary( ) {
        if (T) Log.v(TAG, M()+"@in");

        int ret = -1;
        if( this.mWnnWork != 0 ) {
            mFrequencyOffsetOfUserDictionary  = -1;
            mFrequencyOffsetOfLearnDictionary = -1;

            ret = OpenWnnDictionaryImplJni.clearDictionaryParameters( this.mWnnWork );
            if (T) Log.v(TAG, M()+"@out: ret="+ret);
            return ret;
        }

        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#setDictionary
     */
    public int setDictionary(int index, int base, int high ) {
        if (T) Log.v(TAG, M()+"@in: index="+index+", base="+base+", high="+high);

        int ret = -1;
        if( this.mWnnWork != 0 ) {
            switch( index ) {
            case WnnDictionary.INDEX_USER_DICTIONARY:
                if( base < 0 || high < 0 || base > high
                    /* || base < OFFSET_FREQUENCY_OF_USER_DICTIONARY || high >= OFFSET_FREQUENCY_OF_LEARN_DICTIONARY */ ) {
                    mFrequencyOffsetOfUserDictionary = -1;
                } else {
                    mFrequencyOffsetOfUserDictionary = high;
                }
                ret = 0;
                if (T) Log.v(TAG, M()+"@out: ret="+ret);
                return ret;
            case WnnDictionary.INDEX_LEARN_DICTIONARY:
                if( base < 0 || high < 0 || base > high
                    /* || base < OFFSET_FREQUENCY_OF_LEARN_DICTIONARY */ ) {
                    mFrequencyOffsetOfLearnDictionary = -1;
                } else {
                    mFrequencyOffsetOfLearnDictionary = high;
                }
                ret = 0;
                if (T) Log.v(TAG, M()+"@out: ret="+ret);
                return ret;
            default:
                ret = OpenWnnDictionaryImplJni.setDictionaryParameter( this.mWnnWork, index, base, high );
                if (T) Log.v(TAG, M()+"@out: ret="+ret);
                return ret;
            }
        }

        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * Query to the database
     *
     * @param keyString     The key string
     * @param wnnWord      The previous word for link search
     * @param operation    The search operation
     * @param order         The type of sort order
     */
    protected void createQuery( String keyString, WnnWord wnnWord, int operation, int order) {
        if (T) Log.v(TAG, M()+"@in: keyString="+keyString+", wnnWord="+wnnWord+", operation="+operation+", order="+order);

        int newTypeOfQuery, maxBindsOfQuery;
        String querySqlOrderByFreq, querySqlOrderByKey;
        String queryArgs[];

        if( operation != WnnDictionary.SEARCH_LINK ) {
            wnnWord = null;
        }

        switch( operation ) {
        case WnnDictionary.SEARCH_EXACT:
            querySqlOrderByFreq = mExactQuerySqlOrderByFreq; 
            querySqlOrderByKey  = mExactQuerySqlOrderByKey;
            newTypeOfQuery      = 0;
            queryArgs           = mExactQueryArgs;

            queryArgs[ 0 ]      = keyString;
            break;

        case WnnDictionary.SEARCH_PREFIX:
        case WnnDictionary.SEARCH_LINK:
            /* Select the suitable parameters for the query */
            if( keyString.length() <= FAST_QUERY_LENGTH ) {
                if( wnnWord != null ) {
                    querySqlOrderByFreq = mFastLinkQuerySqlOrderByFreq; 
                    querySqlOrderByKey  = mFastLinkQuerySqlOrderByKey;
                    newTypeOfQuery      = 1;
                } else {
                    querySqlOrderByFreq = mFastPrefixQuerySqlOrderByFreq; 
                    querySqlOrderByKey  = mFastPrefixQuerySqlOrderByKey;
                    newTypeOfQuery      = 2;
                }
                maxBindsOfQuery     = FAST_QUERY_LENGTH;
                queryArgs           = mFastQueryArgs;
            } else {
                if( wnnWord != null ) {
                    querySqlOrderByFreq = mFullLinkQuerySqlOrderByFreq; 
                    querySqlOrderByKey  = mFullLinkQuerySqlOrderByKey;
                    newTypeOfQuery      = 3;
                } else {
                    querySqlOrderByFreq = mFullPrefixQuerySqlOrderByFreq; 
                    querySqlOrderByKey  = mFullPrefixQuerySqlOrderByKey;
                    newTypeOfQuery      = 4;
                }
                maxBindsOfQuery     = MAX_LENGTH_OF_QUERY;
                queryArgs           = mFullQueryArgs;
            }

            if( wnnWord != null ) {
                /* If link search is enabled, insert information of the previous word */
                String[] queryArgsTemp = OpenWnnDictionaryImplJni.createBindArray( this.mWnnWork, keyString, maxBindsOfQuery, MAX_PATTERN_OF_APPROX );

                queryArgs = new String[ queryArgsTemp.length + 2 ];
                for( int i = 0 ; i < queryArgsTemp.length ; i++ ) {
                    queryArgs[ i + 2 ] = queryArgsTemp[ i ];
                }

                queryArgs[ 0 ] = wnnWord.stroke;
                queryArgs[ 1 ] = wnnWord.candidate;
            } else {
                queryArgs = OpenWnnDictionaryImplJni.createBindArray( this.mWnnWork, keyString, maxBindsOfQuery, MAX_PATTERN_OF_APPROX );
            }
            break;

        default:
            mCountCursor = 0;
            freeCursor( );
            if (T) Log.v(TAG, M()+"@out");
            return;
        }

        /* Create the cursor and set arguments */
        mCountCursor = 0;

        if( mDbCursor == null || mTypeOfQuery != newTypeOfQuery ) {
            /* If the cursor is not exist or the type of query is changed, compile the query string and query words */
            freeCursor( );

            try {
                switch( order ) {
                case WnnDictionary.ORDER_BY_FREQUENCY:
                    mDbCursor = ( SQLiteCursor )mDbDic.rawQuery( querySqlOrderByFreq, queryArgs );
                    break;
                case WnnDictionary.ORDER_BY_KEY:
                    mDbCursor = ( SQLiteCursor )mDbDic.rawQuery( querySqlOrderByKey, queryArgs );
                    break;
                default:
                    if (T) Log.v(TAG, M()+"@out");
                    return;
                }
            } catch( SQLException e ) {
                if (T) Log.v(TAG, M()+"@out");
                return;
            }

            mTypeOfQuery = newTypeOfQuery;
        } else {
            /* If the cursor is exist, bind new arguments and re-query words (DO NOT recompile the query string) */
            try {
                mDbCursor.setSelectionArguments( queryArgs );
                mDbCursor.requery( );
            } catch( SQLException e ) {
                if (T) Log.v(TAG, M()+"@out");
                return;
            }
        }

        if( mDbCursor != null ) {
            /* If querying is succeed, count the number of words */
            mCountCursor = mDbCursor.getCount();
            if( mCountCursor == 0 ) {
                /* If no word is retrieved, deactivate the cursor for reduce the resource */
                mDbCursor.deactivate( );
            }
        }

        if (T) Log.v(TAG, M()+"@out");
        return;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#searchWord
     */
    public int searchWord( int operation, int order, String keyString ) {
        if (T) Log.v(TAG, M()+"@in: operation="+operation+", order="+order+", keyString="+keyString);
        int ret = -1;

        /* Unset the previous word information */
        OpenWnnDictionaryImplJni.clearResult( this.mWnnWork );

        /* Search to user/learn dictionary */
        if( mDbDic != null && ( mFrequencyOffsetOfUserDictionary  >= 0 ||
                                mFrequencyOffsetOfLearnDictionary >= 0 ) ) {
            try {
                if( keyString.length() > 0 ) {
                    createQuery( keyString, null, operation, order );
                    if( mDbCursor != null ) {
                        mDbCursor.moveToFirst();
                    }
                } else {
                    /* If the key string is "", no word is retrieved */
                    if( mDbCursor != null ) {
                        mDbCursor.deactivate();
                    }
                    mCountCursor = 0;
                }
            } catch( SQLException e ) {
                if( mDbCursor != null ) {
                    mDbCursor.deactivate();
                }
                mCountCursor = 0;
            }
        } else {
            mCountCursor = 0;
        }

        /* Search to fixed dictionary */
        if( this.mWnnWork != 0 ) {
            ret = OpenWnnDictionaryImplJni.searchWord( this.mWnnWork, operation, order, keyString );
            if (mCountCursor > 0) {
                ret = 1;
            }
            if (T) Log.v(TAG, M()+"@out: ret="+ret);
            return ret;
        }

        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#searchWord
     */
    public int searchWord( int operation, int order, String keyString, WnnWord wnnWord ) {
        if (T) Log.v(TAG, M()+"@in: operation="+operation+", order="+wnnWord+", keyString="+keyString+", wnnWord="+wnnWord);
        int ret = -1;

        if( wnnWord == null || wnnWord.partOfSpeech == null ) {
            if (T) Log.v(TAG, M()+"@out: ret="+ret);
            return ret;
        }

        /* Search to user/learn dictionary with link information */
        if( mDbDic != null && ( mFrequencyOffsetOfUserDictionary  >= 0 ||
                                mFrequencyOffsetOfLearnDictionary >= 0 ) ) {
            try {
                createQuery( keyString, wnnWord, operation, order );
                if( mDbCursor != null ) {
                    mDbCursor.moveToFirst();
                }
            } catch( SQLException e ) {
                if( mDbCursor != null ) {
                    mDbCursor.deactivate();
                }
                mCountCursor = 0;
            }
        } else {
            mCountCursor = 0;
        }

        /* Search to fixed dictionary with link information */
        OpenWnnDictionaryImplJni.clearResult( this.mWnnWork );
        OpenWnnDictionaryImplJni.setStroke( this.mWnnWork, wnnWord.stroke );
        OpenWnnDictionaryImplJni.setCandidate( this.mWnnWork, wnnWord.candidate );
        OpenWnnDictionaryImplJni.setLeftPartOfSpeech( this.mWnnWork, wnnWord.partOfSpeech.left );
        OpenWnnDictionaryImplJni.setRightPartOfSpeech( this.mWnnWork, wnnWord.partOfSpeech.right );
        OpenWnnDictionaryImplJni.selectWord( this.mWnnWork );

        if( this.mWnnWork != 0 ) {
            ret = OpenWnnDictionaryImplJni.searchWord( this.mWnnWork, operation, order, keyString );
            if (mCountCursor > 0) {
                ret = 1;
            }
            if (T) Log.v(TAG, M()+"@out: ret="+ret);
            return ret;
        }

        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#getNextWord
     */
    public WnnWord getNextWord( ) {
        if (T) Log.v(TAG, M()+"@in");

        WnnWord ret = getNextWord( 0 );

        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#getNextWord
     */
    public WnnWord getNextWord( int length ) {
        if (T) Log.v(TAG, M()+"@in: length="+length);
        WnnWord ret = null;

        if( this.mWnnWork != 0 ) {
            if( mDbDic != null && mDbCursor != null && mCountCursor > 0 ) {
                /* If the user/learn dictionary is queried, get the result from the user/learn dictionary */
                ret = new WnnWord( );
                try {
                    /* Skip results if that is not contained the type of search or length of stroke is not equal specified length */
                    while( mCountCursor > 0 &&
                           ( ( mFrequencyOffsetOfUserDictionary < 0  && mDbCursor.getInt( 4 ) == TYPE_NAME_USER      ) ||
                             ( mFrequencyOffsetOfLearnDictionary < 0 && mDbCursor.getInt( 4 ) == TYPE_NAME_LEARN     ) ||
                             ( length > 0                            && mDbCursor.getString( 0 ).length( ) != length ) ) ) {
                        mDbCursor.moveToNext();
                        mCountCursor--;
                    }

                    if( mCountCursor > 0 ) {
                        /* Get the information of word */
                        ret.stroke               = mDbCursor.getString( 0 );
                        ret.candidate            = mDbCursor.getString( 1 );
                        ret.partOfSpeech.left    = mDbCursor.getInt( 2 );
                        ret.partOfSpeech.right   = mDbCursor.getInt( 3 );

                        if( mDbCursor.getInt( 4 ) == TYPE_NAME_USER ) {
                            ret.frequency        = mFrequencyOffsetOfUserDictionary;
                        } else {
                            ret.frequency        = mFrequencyOffsetOfLearnDictionary;
                        }

                        /* Move cursor to next result. If the next result is not exist, deactivate the cursor */
                        mDbCursor.moveToNext();
                        if( --mCountCursor <= 0 ) {
                            mDbCursor.deactivate();
                        }

                        if (T) Log.v(TAG, M()+"@out: ret="+ret);
                        return ret;
                    } else {
                        /* if no result is found, terminate the searching of user/learn dictionary */
                        mDbCursor.deactivate();
                        ret = null;
                    }
                } catch( SQLException e ) {
                    mDbCursor.deactivate();
                    mCountCursor = 0;
                    ret = null;
                }
            }

            /* Get the result from fixed dictionary */
            int res = OpenWnnDictionaryImplJni.getNextWord( this.mWnnWork, length );
            if( res > 0 ) {
                ret = new WnnWord( );
                if( ret != null ) {
                    ret.stroke               = OpenWnnDictionaryImplJni.getStroke( this.mWnnWork );
                    ret.candidate            = OpenWnnDictionaryImplJni.getCandidate( this.mWnnWork );
                    ret.frequency            = OpenWnnDictionaryImplJni.getFrequency( this.mWnnWork );
                    ret.partOfSpeech.left    = OpenWnnDictionaryImplJni.getLeftPartOfSpeech( this.mWnnWork );
                    ret.partOfSpeech.right   = OpenWnnDictionaryImplJni.getRightPartOfSpeech( this.mWnnWork );
                }
                if (T) Log.v(TAG, M()+"@out: ret="+ret);
                return ret;
            } else if ( res == 0 ) {
                /* No result is found. */
                ret = null;
                if (T) Log.v(TAG, M()+"@out: ret="+ret);
                return ret;
            } else {
                /* An error occur (It is regarded as "No result is found".) */
                ret = null;
                if (T) Log.v(TAG, M()+"@out: ret="+ret);
                return ret;
            }
        }
        ret = null;
        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#getUserDictionaryWords
     */
    public WnnWord[] getUserDictionaryWords( ) {
        if (T) Log.v(TAG, M()+"@in");
        WnnWord[] ret = null;

        if( this.mWnnWork != 0 && mDbDic != null ) {
            int numOfWords, i;
            SQLiteCursor cursor = null;

            try {
                /* Count all words in the user dictionary */
                cursor = ( SQLiteCursor )mDbDic.query(
                    TABLE_NAME_DIC,
                    new String[] { COLUMN_NAME_STROKE, COLUMN_NAME_CANDIDATE },
                    String.format( "%s=%d", COLUMN_NAME_TYPE, TYPE_NAME_USER ),
                    null, null, null, null);
                numOfWords = cursor.getCount();

                if( numOfWords > 0 ) {
                    /* Retrieve all words in the user dictionary */
                    WnnWord[] words = new WnnWord[ numOfWords ];

                    cursor.moveToFirst();
                    for( i = 0 ; i < numOfWords ; i++ ) {
                        words[ i ] = new WnnWord();
                        words[ i ].stroke       = cursor.getString( 0 );
                        words[ i ].candidate    = cursor.getString( 1 );
                        cursor.moveToNext();
                    }

                    ret = words;
                    if (T) Log.v(TAG, M()+"@out: ret="+ret);
                    return ret;
                }
            } catch( SQLException e ) {
                /* An error occurs */
                ret = null;
                if (T) Log.v(TAG, M()+"@out: ret="+ret);
                return ret;
            } finally {
                if( cursor != null ) {
                    cursor.close( );
                }
            }
        }
        ret = null;
        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#clearApproxPattern
     */
    public void clearApproxPattern( ) {
        if (T) Log.v(TAG, M()+"@in");

        if( this.mWnnWork != 0 ) {
            OpenWnnDictionaryImplJni.clearApproxPatterns( this.mWnnWork );
        }

        if (T) Log.v(TAG, M()+"@out");
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#setApproxPattern
     */
    public int setApproxPattern( String src, String dst ) {
        if (T) Log.v(TAG, M()+"@in: src="+src+", dst="+dst);
        int ret = -1;

        if( this.mWnnWork != 0 ) {
            ret = OpenWnnDictionaryImplJni.setApproxPattern( this.mWnnWork, src, dst );
        }

        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#setApproxPattern
     */
    public int setApproxPattern( int approxPattern ) {
        if (T) Log.v(TAG, M()+"@in: approxPattern="+approxPattern);
        int ret = -1;

        if( this.mWnnWork != 0 ) {
            ret = OpenWnnDictionaryImplJni.setApproxPattern( this.mWnnWork, approxPattern );
        }

        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#getConnectMatrix
     */
    public byte[][] getConnectMatrix( ) {
        if (T) Log.v(TAG, M()+"@in");

        byte[][]    ret;
        int         lcount, i;

        if (this.mWnnWork != 0) {
            /* 1-origin */
            lcount = OpenWnnDictionaryImplJni.getNumberOfLeftPOS( this.mWnnWork );
            ret = new byte[ lcount + 1 ][ ];

            if( ret != null ) {
                for( i = 0 ; i < lcount + 1 ; i++ ) {
                    ret[ i ] = OpenWnnDictionaryImplJni.getConnectArray( this.mWnnWork, i );

                    if( ret[ i ] == null ) {
                        ret = null;
                        if (T) Log.v(TAG, M()+"@out: ret="+ret);
                        return ret;
                    }
                }
            }
        } else {
            ret = new byte[1][1];
        }

        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#getPOS
     */
    public WnnPOS getPOS( int type ) {
        if (T) Log.v(TAG, M()+"@in: type="+type);

        WnnPOS ret = new WnnPOS( );

        if( this.mWnnWork != 0 && ret != null ) {
            ret.left  = OpenWnnDictionaryImplJni.getLeftPartOfSpeechSpecifiedType( this.mWnnWork, type );
            ret.right = OpenWnnDictionaryImplJni.getRightPartOfSpeechSpecifiedType( this.mWnnWork, type );

            if( ret.left < 0 || ret.right < 0 ) {
                ret = null;
                if (T) Log.v(TAG, M()+"@out: ret="+ret);
                return ret;
            }
        }

        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#clearUserDictionary
     */
    public int clearUserDictionary() {
        if (T) Log.v(TAG, M()+"@in");

        if( mDbDic != null ) {
            mDbDic.execSQL( String.format( "delete from %s where %s=%d", TABLE_NAME_DIC, COLUMN_NAME_TYPE, TYPE_NAME_USER ) );
        }

        /* If no writable dictionary exists, no error occurs. */
        if (T) Log.v(TAG, M()+"@out");
        return 0;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#clearLearnDictionary
     */
    public int clearLearnDictionary() {
        if (T) Log.v(TAG, M()+"@in");

        if( mDbDic != null ) {
            mDbDic.execSQL( String.format( "delete from %s where %s=%d", TABLE_NAME_DIC, COLUMN_NAME_TYPE, TYPE_NAME_LEARN ) );
        }
        

        /* If no writable dictionary exists, no error occurs. */
        if (T) Log.v(TAG, M()+"@out");
        return 0;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#addWordToUserDictionary
     */
    public int addWordToUserDictionary( WnnWord[] word, Runnable progress ) {
        if (T) Log.v(TAG, M()+"@in: word="+word+", progress="+progress);

        int ret = 0;

        if( mDbDic != null ) {
            SQLiteCursor cursor;

            /* Count all words in the user dictionary */
            cursor = ( SQLiteCursor )mDbDic.query(
                TABLE_NAME_DIC,
                new String[] { COLUMN_NAME_ID },
                String.format( "%s=%d", COLUMN_NAME_TYPE, TYPE_NAME_USER ),
                null, null, null, null);

            int count = cursor.getCount();
            cursor.close();

            int words = word.length;
            if (words > MAX_WORDS_IN_USER_DICTIONARY-count) {
            	words = MAX_WORDS_IN_USER_DICTIONARY-count;
            }
			mDbDic.beginTransaction();
			try {
			    StringBuilder strokeSQL    = new StringBuilder();
			    StringBuilder candidateSQL = new StringBuilder();

			    for( int index=0; index < words; index++ ) {
			    	boolean noreg = false;
			    	if (progress != null) {
			    		if ((index % 100) == 0) {
			    			progress.run();
			    		}
			    	}
			    	WnnWord w = word[index];
			    	if (w.stroke.length()    > 0 && w.stroke.length()    <= MAX_STROKE_LENGTH &&
			            w.candidate.length() > 0 && w.candidate.length() <= MAX_CANDIDATE_LENGTH ) {
			            strokeSQL.setLength( 0 );
			            candidateSQL.setLength( 0 );
			            DatabaseUtils.appendEscapedSQLString( strokeSQL, w.stroke );
			            DatabaseUtils.appendEscapedSQLString( candidateSQL, w.candidate );

			            // if (false) {
			            	cursor = ( SQLiteCursor )mDbDic.query(
			            			TABLE_NAME_DIC,
			            			new String[] { COLUMN_NAME_ID },
			            			String.format( "%s=%d and %s=%s and %s=%s",
			            					COLUMN_NAME_TYPE, TYPE_NAME_USER,
			            					COLUMN_NAME_STROKE, strokeSQL.toString(),
			            					COLUMN_NAME_CANDIDATE, candidateSQL.toString() ),
			            					null, null, null, null );

			            	if( cursor.getCount() > 0 ) {
			            		/* if the specified word is exist, an error reported and skipped that word. */
			            		ret = -2;
			            		noreg = true;
			            	}
			            	cursor.close( );
			            	cursor = null;
			            // }
			            	if (!noreg) {
			            		ContentValues content = new ContentValues();
			            		content.clear();
			            		content.put( COLUMN_NAME_TYPE,      TYPE_NAME_USER );
			            		content.put( COLUMN_NAME_STROKE,    w.stroke );
			            		content.put( COLUMN_NAME_CANDIDATE, w.candidate );
			            		content.put( COLUMN_NAME_POS_LEFT,  w.partOfSpeech.left );
			            		content.put( COLUMN_NAME_POS_RIGHT, w.partOfSpeech.right );
			            		mDbDic.insert( TABLE_NAME_DIC, null, content );
			            }
			        }
			    }
			    mDbDic.setTransactionSuccessful();
			} catch( SQLException e ) {
			    /* An error occurs */
			    ret = -1;
			    if (T) Log.v(TAG, M()+"@out: ret="+ret);
			    return ret;
			} finally {
			    mDbDic.endTransaction();
			    if( cursor != null ) {
			        cursor.close( );
			    }
			}
        }

        /* If no writable dictionary exists, no error occurs. */
        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#addWordToUserDictionary
     */
    public int addWordToUserDictionary( WnnWord word ) {
        if (T) Log.v(TAG, M()+"@in: word="+word);
    	int ret = -1;

        WnnWord[] words = new WnnWord[1];
        words[0] = word;
   
        ret = addWordToUserDictionary( words, null );
        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#removeWordFromUserDictionary
     */
    public int removeWordFromUserDictionary( WnnWord[] word ) {
        if (T) Log.v(TAG, M()+"@in: word="+word);
    	int ret = 0;

        if( mDbDic != null ) {
            /* Remove the specified word */
            mDbDic.beginTransaction();
            try {
                StringBuilder strokeSQL    = new StringBuilder();
                StringBuilder candidateSQL = new StringBuilder();

                for( int index = 0 ; index < word.length ; index++ ) {
                    if( word[index].stroke.length()    > 0 && word[index].stroke.length()    <= MAX_STROKE_LENGTH &&
                        word[index].candidate.length() > 0 && word[index].candidate.length() <= MAX_CANDIDATE_LENGTH ) {
                        strokeSQL.setLength( 0 );
                        candidateSQL.setLength( 0 );
                        DatabaseUtils.appendEscapedSQLString( strokeSQL, word[index].stroke );
                        DatabaseUtils.appendEscapedSQLString( candidateSQL, word[index].candidate );

                        mDbDic.delete( TABLE_NAME_DIC,
                            String.format( "%s=%d and %s=%s and %s=%s",
                                           COLUMN_NAME_TYPE, TYPE_NAME_USER,
                                           COLUMN_NAME_STROKE, strokeSQL,
                                           COLUMN_NAME_CANDIDATE, candidateSQL ),
                            null );
                    }
                }
                mDbDic.setTransactionSuccessful();
            } catch( SQLException e ) {
                /* An error occurs */
                ret = -1;
                if (T) Log.v(TAG, M()+"@out: ret="+ret);
                return ret;
            } finally {
                mDbDic.endTransaction();
            }
        }

        /* If no writable dictionary exists, no error occurs. */
        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#removeWordFromUserDictionary
     */
    public int removeWordFromUserDictionary( WnnWord word ) {
        if (T) Log.v(TAG, M()+"@in: word="+word);
    	int ret = 0;

        WnnWord[] words = new WnnWord[1];
        words[0] = word;
   
        ret = removeWordFromUserDictionary( words );
        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * @see net.gorry.android.input.nicownng.WnnDictionary#learnWord
     */
    public int learnWord( WnnWord word ) {
        if (T) Log.v(TAG, M()+"@in: word="+word);
    	int ret = 0;

        ret = learnWord( word, null );

        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * Learn the word with connection.
     * 
     * @param word              The word to learn
     * @param previousWord      The word which is selected previously.
     * @return                  0 if success; minus value if fail.
     */
    public int learnWord( WnnWord word, WnnWord previousWord ) {
        if (T) Log.v(TAG, M()+"@in: word="+word+", previousWord="+previousWord);
        int ret = 0;

        if( mDbDic != null ) {
            StringBuilder previousStrokeSQL    = new StringBuilder();
            StringBuilder previousCandidateSQL = new StringBuilder();

            if( previousWord != null &&
                previousWord.stroke.length()    > 0 && previousWord.stroke.length()    <= MAX_STROKE_LENGTH &&
                previousWord.candidate.length() > 0 && previousWord.candidate.length() <= MAX_CANDIDATE_LENGTH ) {
                DatabaseUtils.appendEscapedSQLString( previousStrokeSQL, previousWord.stroke );
                DatabaseUtils.appendEscapedSQLString( previousCandidateSQL, previousWord.candidate );
                /* If the information of previous word is set, perform the link learning */
            }

            if( word.stroke.length()    > 0 && word.stroke.length()    <= MAX_STROKE_LENGTH &&
                word.candidate.length() > 0 && word.candidate.length() <= MAX_CANDIDATE_LENGTH ) {
                StringBuilder strokeSQL    = new StringBuilder();
                StringBuilder candidateSQL = new StringBuilder();
                DatabaseUtils.appendEscapedSQLString( strokeSQL, word.stroke );
                DatabaseUtils.appendEscapedSQLString( candidateSQL, word.candidate );

                SQLiteCursor cursor;

                /* Count the number of registered words and retrieve that words ascending by the ID */
                cursor = ( SQLiteCursor )mDbDic.query(
                    TABLE_NAME_DIC,
                    new String[] { COLUMN_NAME_STROKE, COLUMN_NAME_CANDIDATE },
                    String.format( "%s=%d", COLUMN_NAME_TYPE, TYPE_NAME_LEARN ),
                    null, null, null,
                    String.format( "%s ASC", COLUMN_NAME_ID ) );

                if( cursor.getCount( ) >= MAX_WORDS_IN_LEARN_DICTIONARY ) {
                    /* If a registering space is short, delete the words that contain same stroke and candidate to the oldest word */
                    mDbDic.beginTransaction();
                    try {
                        cursor.moveToFirst( );

                        StringBuilder oldestStrokeSQL    = new StringBuilder();
                        StringBuilder oldestCandidateSQL = new StringBuilder();
                        DatabaseUtils.appendEscapedSQLString( oldestStrokeSQL, cursor.getString( 0 ) );
                        DatabaseUtils.appendEscapedSQLString( oldestCandidateSQL, cursor.getString( 1 ) );

                        mDbDic.delete( TABLE_NAME_DIC,
                            String.format( "%s=%d and %s=%s and %s=%s",
                                           COLUMN_NAME_TYPE, TYPE_NAME_LEARN,
                                           COLUMN_NAME_STROKE, oldestStrokeSQL.toString( ),
                                           COLUMN_NAME_CANDIDATE, oldestCandidateSQL.toString( ) ),
                            null );

                        mDbDic.setTransactionSuccessful();
                    } catch( SQLException e ) {
                        ret = -1;
                        if (T) Log.v(TAG, M()+"@out: ret="+ret);
                        return ret;
                    } finally {
                        mDbDic.endTransaction();
                        cursor.close();
                    }
                } else {
                    cursor.close();
                }
                
                /* learning the word */
                ContentValues content = new ContentValues();

                content.clear();
                content.put( COLUMN_NAME_TYPE,                   TYPE_NAME_LEARN );
                content.put( COLUMN_NAME_STROKE,                 word.stroke );
                content.put( COLUMN_NAME_CANDIDATE,              word.candidate );
                content.put( COLUMN_NAME_POS_LEFT,               word.partOfSpeech.left );
                content.put( COLUMN_NAME_POS_RIGHT,              word.partOfSpeech.right );
                if( previousWord != null ) {
                    content.put( COLUMN_NAME_PREVIOUS_STROKE,    previousWord.stroke );
                    content.put( COLUMN_NAME_PREVIOUS_CANDIDATE, previousWord.candidate );
                    content.put( COLUMN_NAME_PREVIOUS_POS_LEFT,  previousWord.partOfSpeech.left );
                    content.put( COLUMN_NAME_PREVIOUS_POS_RIGHT, previousWord.partOfSpeech.right );
                }

                mDbDic.beginTransaction();
                try {
                    mDbDic.insert( TABLE_NAME_DIC, null, content );
                    mDbDic.setTransactionSuccessful();
                } catch( SQLException e ) {
                    mDbDic.endTransaction();
                    ret = -1;
                    if (T) Log.v(TAG, M()+"@out: ret="+ret);
                    return ret;
                } finally {
                    mDbDic.endTransaction();
                }
            }
        }

        /* If no writable dictionary exists, no error occurs. */
        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }

    /**
     * Forget the word with connection.
     * 
     * @param word              The word to forget
     * @return                  0 if success; minus value if fail.
     */
    public int forgetWord( WnnWord word ) {
        if (T) Log.v(TAG, M()+"@in: word="+word);
        int ret = 0;

        if( mDbDic != null ) {

            if( word.stroke.length()    > 0 && word.stroke.length()    <= MAX_STROKE_LENGTH &&
            		word.candidate.length() > 0 && word.candidate.length() <= MAX_CANDIDATE_LENGTH ) {
            	StringBuilder strokeSQL    = new StringBuilder();
            	StringBuilder candidateSQL = new StringBuilder();
            	DatabaseUtils.appendEscapedSQLString( strokeSQL, word.stroke );
            	DatabaseUtils.appendEscapedSQLString( candidateSQL, word.candidate );

            	mDbDic.beginTransaction();
            	try {
            		mDbDic.delete( TABLE_NAME_DIC,
            				String.format( "%s=%d and %s=%s and %s=%s",
            						COLUMN_NAME_TYPE, TYPE_NAME_LEARN,
            						COLUMN_NAME_STROKE, strokeSQL,
            						COLUMN_NAME_CANDIDATE, candidateSQL ),
            						null );

            		mDbDic.setTransactionSuccessful();
            	} catch( SQLException e ) {
            		ret = -1;
            	    if (T) Log.v(TAG, M()+"@out: ret="+ret);
            		return ret;
            	} finally {
            		mDbDic.endTransaction();
            	}
            }
        }

        /* If no writable dictionary exists, no error occurs. */
        if (T) Log.v(TAG, M()+"@out: ret="+ret);
        return ret;
    }
}
