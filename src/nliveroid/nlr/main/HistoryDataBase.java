package nliveroid.nlr.main;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HistoryDataBase extends SQLiteOpenHelper {
	private Activity act;
	private SQLiteDatabase db;
    // コンストラクタ
    public HistoryDataBase( Activity context ){
        // 任意のデータベースファイル名と、バージョンを指定する
        super( context, "his.db", null, 1 );
        this.act = context;
    }


    /**
     * このデータベースを初めて使用する時に実行される処理
     * テーブルの作成や初期データの投入を行う
     */
    @Override
    public void onCreate( SQLiteDatabase db ) {
        // テーブルを作成。SQLの文法は通常のSQLiteと同様
    	try{
        db.execSQL(
                "create table if not exists his ("+
                 "ID  integer primary key autoincrement not null, "+
                 "DATE  integer not null,"+
                 "KIND integer not null, "+
                 "LV text," +
                 "COCH text," +
                 "REMARK0 text," +
                 "REMARK1 text," +
                 "REMARK2 text" +
                 ")" );
        // 必要なら、ここで他のテーブルを作成したり、初期データを挿入したりする
    	} catch (SQLException e) {
    	    Log.e("ERROR", e.toString());
    	    act.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					MyToast.customToastShow(act, "履歴のDBテーブル作成に失敗");
				}
    	    });
    	}
    }
    /**
     * アプリケーションの更新などによって、データベースのバージョンが上がった場合に実行される処理
     * 今回は割愛
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 取りあえず、空実装でよい
    }
    @Override
    public void onOpen(SQLiteDatabase db) {
      // データベースが開かれたときに実行される
      // これの実装は任意
      super.onOpen(db);
      this.db = db;
      //IDを整列しなおす
      try{
			Log.d("NLiveRoid"," HISTORY_OPEN " );
			Cursor c = db.query("his", null, null, null, null, null, null);
			boolean isEOF = c.moveToFirst();
				if(isEOF && NLiveRoid.isDebugMode){
				Log.d("NLiveRoid"," TABLE COUNT " + c.getCount());
				Log.d("NLiveRoid"," TABLE COLUMN_COUNT" + c.getColumnCount());
				String[] names = c.getColumnNames();
				for(int i = 0; i < c.getCount() && isEOF; i++){
//					if(NLiveRoid.isDebugMode){
//						for(int j = 0; j < names.length;j++){
//							Log.d("NLiveRoid"," his "+i + "  " + names[j] + " " + c.getString(j));
//
//					}
//				}
					isEOF = c.moveToNext();
				}
			}else{
				Log.d("NLiveRoid","His ONOPEN NotDebug");
			}
			c.close();
		Cursor ids = db.query("his", new String[] { "ID" }, null, null, null, null, "ID ASC");
			isEOF = ids.moveToFirst();
			Log.d("NLiveRoid"," MIN ISEof" + isEOF);
				if(isEOF){
					long minVal = ids.getLong(0);
					Log.d("NLiveRoid","FirstID " + minVal);
					Log.d("NLiveRoid","COUNT " + ids.getCount());
						ContentValues values = new ContentValues();
						for(long id = 1; id < ids.getCount() && isEOF ; id++){
//							Log.d("NLiveRoid","id: "+ id + "ids.get: " + ids.getLong(0));
							if(ids.getLong(0) != id){
								values.put("ID", id);
								if(NLiveRoid.isDebugMode)Log.d("NLiveRoid","ID UPDATE ID: " + id + " DELETEID: " + ids.getString(0));
							int returnVal = db.update("his", values , "ID = ?", new String[]{ids.getString(0)});
								Log.d("NLiveRoid","His UPDATE RETURN: " + returnVal);
								if(returnVal < 0)break;
							}
							isEOF = ids.moveToNext();
						}
				}
				ids.close();
      }catch(Exception e){
    	  e.printStackTrace();
      }
    }

    public void deleteTable(){
    	try {//最初から無かったらエラーするのを防ぐ(テーブルを削除した後でもonCreateは自動的には呼ばれない)
    		this.db.execSQL(
                "create table if not exists his ("+
                        "ID  integer primary key autoincrement not null, "+
                        "DATE  integer not null,"+
                        "KIND integer not null, "+
                        "LV text," +
                        "COCH text," +
                        "REMARK0 text," +
                        "REMARK1 text," +
                        "REMARK2 text" +
                        ")" );
			this.db.delete("his",null , null);
    	    this.db.execSQL("drop table his;");
    	    this.db.execSQL(
                    "create table if not exists his ("+
                     "ID  integer primary key autoincrement not null, "+
                     "DATE  integer not null,"+
                     "KIND integer not null, "+
                     "LV text," +
                     "COCH text," +
                     "REMARK0 text," +
                     "REMARK1 text," +
                     "REMARK2 text" +
                     ")" );
    	} catch (SQLException e) {
    	    Log.e("ERROR", e.toString());
    	    act.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					MyToast.customToastShow(act, "履歴のDBテーブル削除に失敗");
				}
    	    });
    	}
    }


	public SQLiteDatabase getDB() {
		return this.db;
	}


}