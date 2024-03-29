package com.example.cpexample;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class SQLiteProvider extends ContentProvider {
    
   /** URIのauthority. */
   private static final String AUTHORITY = "com.example.test.cp";
    
   /** SQLiteデータベースのファイル名. */
   private static final String SQLITE_FILENAME = "example.sqlite";
    
   /**
    * コンテンツプロバイダ利用者との「契約」を定義する列挙型定数クラス.
    */
   public enum Contract {
        
       /** Table1 テーブル. */
       Table1(BaseColumns._ID, "title", "note"),
        
       /** Table2 テーブル. */
       Table2(BaseColumns._ID, "date", "title2", "note2");
        
       /**
        * コンストラクタ. カラムを定義する.
        *
        * @param columns 対象テーブルで定義されているカラム
        */
       Contract(final String...columns) {
           this.columns = Collections.unmodifiableList(Arrays.asList(columns));
       }
        
       /** テーブル名. enum定数を小文字にしたものとする. */
       private final String tableName = name().toLowerCase(Locale.US);
        
       /** テーブル全体のデータに対して処理をしに行く時のコード. */
       private final int allCode = ordinal() * 10;
        
       /** 対象IDのデータに対して処理をしに行く時のコード. */
       private final int byIdCode = ordinal() * 10 + 1;
       
      /** 対象IDのデータに対して処理をしに行く時のコード. */
      private final int byDateAllCode = ordinal() * 10 + 2;
       
      /** 対象IDのデータに対して処理をしに行く時のコード. */
      private final int byDateCode = ordinal() * 10 + 3;
        
       /** そのテーブル固有のCONTENT_URI表現. コンテンツリゾルバからこれを使用してアクセスする. */
       public final Uri contentUri = Uri.parse("content://" + AUTHORITY + "/" + tableName);
        
       /** MIMEタイプ（単数）. */
       public final String mimeTypeForOne = "vnd.android.cursor.item/vnd.sample." + tableName;
        
       /** MIMEタイプ（複数）. */
       public final String mimeTypeForMany = "vnd.android.cursor.dir/vnd.sample." + tableName;
        
       /** カラムのリスト. */
       public final List<String> columns;
   }
    
   /** 既定のUriパターンで絞り込む為のMatcher. */
   private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
   static {
       sUriMatcher.addURI(AUTHORITY, Contract.Table1.tableName, Contract.Table1.allCode);
       sUriMatcher.addURI(AUTHORITY, Contract.Table1.tableName + "/#", Contract.Table1.byIdCode);
       sUriMatcher.addURI(AUTHORITY, Contract.Table2.tableName, Contract.Table2.allCode);
       sUriMatcher.addURI(AUTHORITY, Contract.Table2.tableName + "/#", Contract.Table2.byIdCode);
       sUriMatcher.addURI(AUTHORITY, Contract.Table2.tableName + "/date", Contract.Table2.byDateAllCode);
       sUriMatcher.addURI(AUTHORITY, Contract.Table2.tableName + "/date/*", Contract.Table2.byDateCode);
   }

   /** SQLiteOpenHelperのインスタンス. */
   private SQLite mOpenHelper;

   /**
    * コンテンツプロバイダが生成された際に呼ばれる.
    * SQLiteデータベースのファイルが存在しなかった場合は作成し, テーブルを作成する.
    * SQLiteデータベースのファイルが既に存在した場合は, それを開いて返す.
    * データベースのバージョンは, 管理しやすいようにアプリのversionCodeをそのまま使用するものとする.
    *
    * @return SQLiteデータベースが開けたかどうか
    */
   @Override
   public boolean onCreate() {
       final int version;
       try {
           version = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode;
       } catch (NameNotFoundException e) {
           e.printStackTrace();
           return false;
       }
       mOpenHelper = new SQLite(getContext(), SQLITE_FILENAME, null, version);
       return true;
   }

   /**
    * 単数または複数検索して返す.
    *
    * @return クエリ結果が格納されたCursor
    */
   @Override
   public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
       checkUri(uri);
       final int code = sUriMatcher.match(uri);
       SQLiteDatabase db = mOpenHelper.getReadableDatabase();

       for (final Contract contract : Contract.values()) {
           if (code == contract.byDateAllCode) {
               String sql = "select date from table2 group by date";
               return db.rawQuery(sql, null);
           } else if (code == contract.byDateCode) {
               String sql = "select _id, date, title2, note2 from table2 where date = \"" + 
            		   uri.getPathSegments().get(2) + 
            		   "\"";
               Log.d("SQLiteProvider", "sql string is " + sql);
               return db.rawQuery(sql, null);
           }
       }

       Cursor cursor = db.query(uri.getPathSegments().get(0), projection, appendSelection(uri, selection),
               appendSelectionArgs(uri, selectionArgs), null, null, sortOrder);
       cursor.setNotificationUri(getContext().getContentResolver(), uri);
       return cursor;
   }

   /**
    * 対象テーブルにデータを挿入する. Uriに_idを付与してリクエストしても_idは無視する.
    *
    * @return 作成されたデータのUri表現
    */
   @Override
   public Uri insert(Uri uri, ContentValues values) {
       checkUri(uri);
       SQLiteDatabase db = mOpenHelper.getWritableDatabase();
       final long rowId = db.insertOrThrow(uri.getPathSegments().get(0), null, values);
       Uri returnUri = ContentUris.withAppendedId(uri, rowId);
       getContext().getContentResolver().notifyChange(returnUri, null);
       return returnUri;
   }

   /**
    * 対象テーブルの対象データを更新する. _idやselectionArgsの指定が無い場合は全件更新する.
    *
    * @return 更新件数
    */
   @Override
   public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
       checkUri(uri);
       SQLiteDatabase db = mOpenHelper.getWritableDatabase();
       final int count = db.update(uri.getPathSegments().get(0), values, appendSelection(uri, selection),
           appendSelectionArgs(uri, selectionArgs));
       getContext().getContentResolver().notifyChange(uri, null);
       return count;
   }
    
   /**
    * 対象テーブルの対象データを削除する. _idやselectionArgsの指定が無い場合は全件削除する.
    *
    * @return 削除件数
    */
   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs) {
       checkUri(uri);
       SQLiteDatabase db = mOpenHelper.getWritableDatabase();
       final int count = db.delete(uri.getPathSegments().get(0), appendSelection(uri, selection),
           appendSelectionArgs(uri, selectionArgs));
       getContext().getContentResolver().notifyChange(uri, null);
       return count;
   }

   /**
    * 対象UriのMIMEタイプを返却する.
    *
    * @return 対象UriのMIMEタイプ
    * @throws IllegalArgumentException このコンテンツプロバイダで扱えるUriパターンでなかった場合
    */
   @Override
   public String getType(Uri uri) {
       final int code = sUriMatcher.match(uri);
       for (final Contract contract : Contract.values()) {
           if (code == contract.allCode) {
               return contract.mimeTypeForMany;
           } else if (code == contract.byDateAllCode) {
               return contract.mimeTypeForMany;
           } else if (code == contract.byDateCode) {
               return contract.mimeTypeForMany;
           } else if (code == contract.byIdCode) {
               return contract.mimeTypeForOne;
           }
       }
       throw new IllegalArgumentException("unknown uri : " + uri);
   }
    
   /**
    * 対象Uriがこのコンテンツプロバイダで扱えるUriパターンかどうかを検証する.
    *
    * @throws IllegalArgumentException このコンテンツプロバイダで扱えるUriパターンでなかった場合
    */
   private void checkUri(Uri uri) {
       final int code = sUriMatcher.match(uri);
       for (final Contract contract : Contract.values()) {
           if (code == contract.allCode) {
               return;
           } else if (code == contract.byIdCode) {
               return;
           } else if (code == contract.byDateAllCode) {
               return;
           } else if (code == contract.byDateCode) {
               return;
           }
       }
       throw new IllegalArgumentException("unknown uri : " + uri);
   }
    
   /**
    * Uriで_idの指定があった場合, selectionにそれを連結して返す.
    *
    * @param uri Uri
    * @param selection 絞り込み条件
    * @return _idの条件が連結されたselection
    */
   private String appendSelection(Uri uri, String selection) {
       List<String> pathSegments = uri.getPathSegments();
       if (pathSegments.size() == 1) {
           return selection;
       }
       return BaseColumns._ID + " = ?" + (selection == null ? "" : " AND (" + selection + ")");
   }
    
   /**
    * Uriで_idの指定があった場合, selectionArgsにそれを連結して返す.
    *
    * @param uri Uri
    * @param selectionArgs 絞り込み条件の引数
    * @return _idの条件が連結されたselectionArgs
    */
   private String[] appendSelectionArgs(Uri uri, String[] selectionArgs) {
       List<String> pathSegments = uri.getPathSegments();
       if (pathSegments.size() == 1) {
           return selectionArgs;
       }
       if (selectionArgs == null || selectionArgs.length == 0) {
           return new String[] {pathSegments.get(1)};
       }
       String[] returnArgs = new String[selectionArgs.length + 1];
       returnArgs[0] = pathSegments.get(1);
       System.arraycopy(selectionArgs, 0, returnArgs, 1, selectionArgs.length);
       return returnArgs;
   }
    
   /**
    * SQLiteを扱うクラス. ContentProvider内で使用されるに留まる.
    *
    * @author Hideyuki Kojima
    */
   private static class SQLite extends SQLiteOpenHelper {
        
       /**
        * コンストラクタ.
        *
        * @param context コンテキスト
        * @param name SQLiteファイル名
        * @param factory CursorFactory
        * @param version DBバージョン
        */
       public SQLite(Context context, String name, CursorFactory factory, int version) {
           super(context, name, factory, version);
       }

       @Override
       public void onCreate(SQLiteDatabase db) {
           db.beginTransaction();
           try {
               db.execSQL("CREATE TABLE table1 (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
               		"title TEXT, " +
               		"note TEXT" +
               		")");
               db.execSQL("CREATE TABLE table2 (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
               		"date TEXT, " +
               		"title2 TEXT, " +
               		"note2 TEXT" +
               		")");
               db.setTransactionSuccessful();
           } finally {
               db.endTransaction();
           }
       }

       @Override
       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           // TODO 本来は移行用のコードを書く.
       }
   }
}
