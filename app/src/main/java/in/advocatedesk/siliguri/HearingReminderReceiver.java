package in.advocatedesk.siliguri;
import android.app.*; import android.content.*; import android.os.Build;
public class HearingReminderReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c, Intent i) {
  String title=i.getStringExtra("title"); String date=i.getStringExtra("date"); int id=i.getIntExtra("id",0);
  NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  if(Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(new NotificationChannel("hearings","Hearing reminders",NotificationManager.IMPORTANCE_HIGH));
  Intent open=new Intent(c,MainActivity.class);
  PendingIntent pi=PendingIntent.getActivity(c,id,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,"hearings"):new Notification.Builder(c);
  nm.notify(id,b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Court hearing reminder").setContentText(title+" • "+date).setAutoCancel(true).setContentIntent(pi).build());
 }
}
