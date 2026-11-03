package in.advocatedesk.siliguri;
import android.content.*;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
 private final String passphrase;
 public DBHelper(Context c){
  super(c,"advocate_desk_siliguri_secure.db",null,5);
  SQLiteDatabase.loadLibs(c);
  String key;
  try{key=DatabaseKey.get(c.getApplicationContext());}
  catch(Exception e){throw new IllegalStateException("Unable to unlock encrypted case database",e);}
  passphrase=key;
 }
 public SQLiteDatabase getReadableDatabase(){return super.getReadableDatabase(passphrase);}
 public SQLiteDatabase getWritableDatabase(){return super.getWritableDatabase(passphrase);}
 @Override public void onCreate(SQLiteDatabase d){
  d.execSQL("CREATE TABLE cases (_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,case_no TEXT,cnr TEXT,petitioner TEXT,respondent TEXT,court TEXT,location TEXT,judge TEXT,type TEXT,advocate TEXT,filing TEXT,hearing TEXT,status TEXT,priority INTEGER DEFAULT 0,reminder_days INTEGER DEFAULT 1,notes TEXT,created_at TEXT)");
  d.execSQL("CREATE TABLE history (_id INTEGER PRIMARY KEY AUTOINCREMENT,case_id INTEGER,event_date TEXT,event_type TEXT,details TEXT)");
  d.execSQL("ALTER TABLE cases ADD COLUMN outcome TEXT DEFAULT ''");
  d.execSQL("ALTER TABLE cases ADD COLUMN judgment_date TEXT DEFAULT ''");
  d.execSQL("ALTER TABLE cases ADD COLUMN judgment_summary TEXT DEFAULT ''");
  d.execSQL("ALTER TABLE cases ADD COLUMN relief TEXT DEFAULT ''");
  d.execSQL("ALTER TABLE cases ADD COLUMN amount_awarded TEXT DEFAULT ''");
  d.execSQL("ALTER TABLE cases ADD COLUMN observations TEXT DEFAULT ''");
  d.execSQL("ALTER TABLE cases ADD COLUMN appeal_filed TEXT DEFAULT 'Not recorded'");
  d.execSQL("ALTER TABLE cases ADD COLUMN appeal_number TEXT DEFAULT ''");
  d.execSQL("ALTER TABLE cases ADD COLUMN appeal_court TEXT DEFAULT ''");
  d.execSQL("ALTER TABLE cases ADD COLUMN appeal_status TEXT DEFAULT ''");
  d.execSQL("ALTER TABLE cases ADD COLUMN next_action TEXT DEFAULT ''");
  d.execSQL("ALTER TABLE cases ADD COLUMN action_deadline TEXT DEFAULT ''");
  d.execSQL("CREATE INDEX idx_case_hearing ON cases(hearing)");
  d.execSQL("CREATE INDEX idx_case_cnr ON cases(cnr)");
 }
 @Override public void onUpgrade(SQLiteDatabase d,int oldV,int newV){
  if(oldV<2)d.execSQL("CREATE TABLE IF NOT EXISTS history (_id INTEGER PRIMARY KEY AUTOINCREMENT,case_id INTEGER,event_date TEXT,event_type TEXT,details TEXT)");
  if(oldV<3){try{d.execSQL("ALTER TABLE cases ADD COLUMN priority INTEGER DEFAULT 0");}catch(Exception ignored){}try{d.execSQL("ALTER TABLE cases ADD COLUMN reminder_days INTEGER DEFAULT 1");}catch(Exception ignored){}}
  if(oldV<5){String[][] cols={{"outcome","TEXT DEFAULT ''"},{"judgment_date","TEXT DEFAULT ''"},{"judgment_summary","TEXT DEFAULT ''"},{"relief","TEXT DEFAULT ''"},{"amount_awarded","TEXT DEFAULT ''"},{"observations","TEXT DEFAULT ''"},{"appeal_filed","TEXT DEFAULT 'Not recorded'"},{"appeal_number","TEXT DEFAULT ''"},{"appeal_court","TEXT DEFAULT ''"},{"appeal_status","TEXT DEFAULT ''"},{"next_action","TEXT DEFAULT ''"},{"action_deadline","TEXT DEFAULT ''"}};for(String[] col:cols){try{d.execSQL("ALTER TABLE cases ADD COLUMN "+col[0]+" "+col[1]);}catch(Exception ignored){}}}
 }
}
