package in.advocatedesk.siliguri;
import android.app.*; import android.content.*; import java.text.*; import java.util.*;
public class Reminder {
 static void schedule(Context c,int id,String title,String date,int days) {
  try {
   Date d=new SimpleDateFormat("dd-MM-yyyy",Locale.US).parse(date);
   Calendar cal=Calendar.getInstance(); cal.setTime(d); cal.add(Calendar.DAY_OF_YEAR,-days); cal.set(Calendar.HOUR_OF_DAY,9);cal.set(Calendar.MINUTE,0);cal.set(Calendar.SECOND,0);
   if(cal.getTimeInMillis()<=System.currentTimeMillis()) return;
   Intent in=new Intent(c,HearingReminderReceiver.class);in.putExtra("id",id);in.putExtra("title",title);in.putExtra("date",date);
   PendingIntent pi=PendingIntent.getBroadcast(c,id,in,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
   if(android.os.Build.VERSION.SDK_INT<31 || am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pi);
   else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pi);
  } catch(Exception ignored){}
 }
 static void cancel(Context c,int id){Intent in=new Intent(c,HearingReminderReceiver.class);PendingIntent pi=PendingIntent.getBroadcast(c,id,in,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).cancel(pi);}
}
