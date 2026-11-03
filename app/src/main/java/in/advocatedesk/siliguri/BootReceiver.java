package in.advocatedesk.siliguri;
import android.content.*; import android.database.Cursor;
public class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i) {
  if(!Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())) return;
  DBHelper db=new DBHelper(c); Cursor cur=db.getReadableDatabase().rawQuery("SELECT _id,title,hearing,reminder_days FROM cases WHERE hearing<>'' AND reminder_days<>''",null);
  while(cur.moveToNext()) Reminder.schedule(c,cur.getInt(0),cur.getString(1),cur.getString(2),cur.getInt(3));
  cur.close(); db.close();
 }
}
