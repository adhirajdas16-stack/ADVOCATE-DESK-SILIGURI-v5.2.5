package in.advocatedesk.siliguri;

import android.app.*; import android.os.*; import android.content.*; import android.database.Cursor; import net.sqlcipher.database.SQLiteDatabase; import android.content.ContentValues; import android.graphics.Typeface; import android.net.Uri; import android.view.*; import android.widget.*; import android.text.*; import android.webkit.*; import java.text.*; import java.util.*; import java.io.*; import java.security.MessageDigest; import org.json.*;

public class MainActivity extends Activity {
 DBHelper helper; LinearLayout page, cards; String mode="ALL"; EditText query;
 final String[] keys={"title","case_no","cnr","petitioner","respondent","court","location","judge","type","advocate","filing","hearing","status","notes","outcome","judgment_date","judgment_summary","relief","amount_awarded","observations","appeal_filed","appeal_number","appeal_court","appeal_status","next_action","action_deadline"};
 final String[] labels={"Case title / name *","Case number","CNR number","Petitioner / plaintiff","Respondent / defendant","Court name","Court location","Judge name","Case type (Civil / Criminal / Other)","Advocate name","Filing date (dd-MM-yyyy)","Next hearing date (dd-MM-yyyy)","Case status","Notes / private case details","Case result / outcome","Judgment date (dd-MM-yyyy)","Final judgment / order summary","Relief granted or rejected","Compensation / amount awarded","Important observations","Appeal filed? (Yes / No / Not recorded)","Appeal number","Appeal court","Appeal status","Next legal action","Next action deadline (dd-MM-yyyy)"};
 DBHelper db(){return helper;}
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
 TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(0xfff4f7fb);return t;}
 Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextColor(0xffeaffff);if(Build.VERSION.SDK_INT>=21)b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff14232b));b.setAllCaps(false);b.setTextSize(14);b.setElevation(dp(2));return b;}

 static final int RESTORE_REQ=77;
 @Override public void onCreate(Bundle b){super.onCreate(b);helper=new DBHelper(this);if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=0)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},10); authenticate();}
 void authenticate(){
  android.content.SharedPreferences p=getSharedPreferences("secure",MODE_PRIVATE);
  String stored=p.getString("pin_hash",null);
  if(stored==null){ final EditText pin=new EditText(this);pin.setHint("Create a 4–8 digit app PIN");pin.setInputType(2);
   new AlertDialog.Builder(this).setTitle("Set up Advocate Desk").setMessage("Create a private PIN for opening this app. Keep it safe; there is no PIN recovery in this version.").setView(pin).setCancelable(false).setPositiveButton("Save PIN",(d,w)->{String v=pin.getText().toString();if(v.length()<4||v.length()>8){toast("PIN must be 4–8 digits. Restart the app to try again.");return;}p.edit().putString("pin_hash",hash(v)).apply();home();}).show();
  } else {final EditText pin=new EditText(this);pin.setHint("Enter app PIN");pin.setInputType(2);
   new AlertDialog.Builder(this).setTitle("Advocate Desk is locked").setView(pin).setCancelable(false).setPositiveButton("Unlock",(d,w)->{if(stored.equals(hash(pin.getText().toString())))home();else{toast("Incorrect PIN");authenticate();}}).show();}
 }
 String hash(String s){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"));StringBuilder x=new StringBuilder();for(byte z:b)x.append(String.format("%02x",z));return x.toString();}catch(Exception e){return "";}}

 void shell(String title){page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(18),dp(22),dp(18),dp(20));page.setBackgroundColor(0xff07090d);ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.addView(page);setContentView(sc);
  TextView brand=label("A D  /  ADVOCATE DESK     •     SILIGURI     •     v5.2.4",11);brand.setTextColor(0xff00e5ff);brand.setTypeface(null,Typeface.BOLD);page.addView(brand);
  TextView h=label(title,27);h.setTypeface(null,Typeface.BOLD);h.setTextColor(0xfff8fbff);h.setPadding(0,dp(12),0,dp(5));page.addView(h);
  TextView line=label("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",10);line.setTextColor(0xff00d9f5);page.addView(line);
 }
 String today(){return new SimpleDateFormat("dd-MM-yyyy",Locale.US).format(new Date());}
 void home(){shell("Dashboard");
  Cursor c=db().getReadableDatabase().rawQuery("SELECT COUNT(*),SUM(CASE WHEN hearing=? THEN 1 ELSE 0 END),SUM(CASE WHEN priority=1 THEN 1 ELSE 0 END) FROM cases",new String[]{today()});int total=0,td=0,prio=0;if(c.moveToFirst()){total=c.getInt(0);td=c.getInt(1);prio=c.getInt(2);}c.close();
  TextView stats=label("CASE OVERVIEW",12);stats.setTextColor(0xff00e5ff);stats.setTypeface(null,Typeface.BOLD);stats.setPadding(0,dp(18),0,dp(5));page.addView(stats);
  Cursor rc=db().getReadableDatabase().rawQuery("SELECT "+
   "SUM(CASE WHEN lower(outcome) LIKE '%favour%' OR lower(outcome) LIKE '%won%' THEN 1 ELSE 0 END),"+
   "SUM(CASE WHEN lower(outcome) LIKE '%against%' OR lower(outcome) LIKE '%lost%' THEN 1 ELSE 0 END),"+
   "SUM(CASE WHEN lower(outcome) LIKE '%partial%' THEN 1 ELSE 0 END),"+
   "SUM(CASE WHEN lower(outcome) LIKE '%settled%' OR lower(outcome) LIKE '%comprom%' THEN 1 ELSE 0 END),"+
   "SUM(CASE WHEN lower(outcome) LIKE '%pending judgment%' THEN 1 ELSE 0 END),"+
   "SUM(CASE WHEN outcome IS NULL OR trim(outcome)='' THEN 1 ELSE 0 END) FROM cases",null);
  int won=0,lost=0,partial=0,settled=0,pendingJudgment=0,unrecorded=0;
  if(rc.moveToFirst()){won=rc.getInt(0);lost=rc.getInt(1);partial=rc.getInt(2);settled=rc.getInt(3);pendingJudgment=rc.getInt(4);unrecorded=rc.getInt(5);}rc.close();
  int decided=won+lost+partial;int successPct=decided==0?0:(int)Math.round(((won+partial*0.5)*100.0)/decided);
  TextView resultMetric=label("  "+won+" WON / FAVOUR     •     "+lost+" AGAINST\n  "+partial+" PARTIAL     •     "+settled+" SETTLED     •     "+pendingJudgment+" PENDING JUDGMENT\n  "+unrecorded+" NO RESULT     •     "+successPct+"% FAVOURABLE OUTCOME RATE",13);
  resultMetric.setPadding(dp(12),dp(12),dp(12),dp(12));resultMetric.setBackgroundColor(0xff111820);page.addView(resultMetric);
  TextView metric=label("  "+total+"  TOTAL CASES     •     "+td+"  TODAY     •     "+prio+"  PRIORITY",16);metric.setTypeface(null,Typeface.BOLD);metric.setPadding(dp(12),dp(18),dp(12),dp(18));metric.setBackgroundColor(0xff111820);page.addView(metric);
  addHome("＋  Add New Case",()->form(-1));addHome("All Saved Cases",()->{mode="ALL";listing();});addHome("Today's Hearings",()->{mode="TODAY";listing();});addHome("Upcoming Hearings",()->{mode="UPCOMING";listing();});addHome("Past Hearings",()->{mode="PAST";listing();});addHome("Priority Cases",()->{mode="PRIORITY";listing();});addHome("Case Results & Judgments",()->{mode="RESULTS";listing();});addHome("Hearing Calendar / Date Search",()->dateSearch());addHome("Backup / Restore / Export",()->backupMenu());addHome("Team Cloud Sync / Login",()->cloudMenu());addHome("Siliguri Court & eCourts Links",()->courtLinks());addHome("Lock App",()->authenticate());
  TextView disclaimer=label("\nPRIVATE LEGAL WORKSPACE\nLocal-first case diary. Shared team access and verified court-feed updates will be activated only after secure cloud configuration and an authorised court-data source are connected.",12);disclaimer.setTextColor(0xffaebbc8);page.addView(disclaimer);
 }
 void addHome(String s,Runnable r){Button b=btn(s);page.addView(b);b.setOnClickListener(v->r.run());}
 void listing(){shell(mode.equals("TODAY")?"Today's Hearings":mode.equals("UPCOMING")?"Upcoming Hearings":mode.equals("PAST")?"Past Hearings":mode.equals("PRIORITY")?"Priority Cases":"All Cases");
  query=new EditText(this);query.setSingleLine();query.setHint("Search title, case no., CNR, party, court, advocate");page.addView(query);
  Button add=btn("+ Add New Case");page.addView(add);add.setOnClickListener(v->form(-1));
  cards=new LinearLayout(this);cards.setOrientation(1);page.addView(cards);
  query.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int before,int count){render(s.toString());}public void afterTextChanged(Editable e){}});
  render("");Button back=btn("Dashboard");page.addView(back);back.setOnClickListener(v->home());
 }
 int dateCompare(String a,String b){try{SimpleDateFormat f=new SimpleDateFormat("dd-MM-yyyy",Locale.US);f.setLenient(false);return f.parse(a).compareTo(f.parse(b));}catch(Exception e){return 1;}}
 void render(String q){cards.removeAllViews();Cursor c=db().getReadableDatabase().rawQuery("SELECT * FROM cases ORDER BY CASE WHEN hearing='' THEN 1 ELSE 0 END, hearing ASC",null);int count=0;
  while(c.moveToNext()){int id=c.getInt(c.getColumnIndexOrThrow("_id"));String title=s(c,"title"),hearing=s(c,"hearing");int cmp=dateCompare(hearing,today());
   if(!q.isEmpty()&&!(allSearch(c).toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT))))continue;
   if(mode.equals("TODAY")&&!today().equals(hearing))continue;if(mode.equals("UPCOMING")&&(hearing.isEmpty()||cmp<0))continue;if(mode.equals("PAST")&&(hearing.isEmpty()||cmp>=0))continue;if(mode.equals("PRIORITY")&&c.getInt(c.getColumnIndexOrThrow("priority"))!=1)continue;if(mode.equals("RESULTS")&&s(c,"outcome").isEmpty())continue;
   LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(12),dp(10),dp(12),dp(10));card.setBackgroundColor(0xff111820);
   TextView t=label((c.getInt(c.getColumnIndexOrThrow("priority"))==1?"✦ ":"")+title,18);t.setTypeface(null,Typeface.BOLD);t.setTextColor(0xff00e5ff);card.addView(t);
   card.addView(label("Case No: "+dash(s(c,"case_no"))+"  |  CNR: "+dash(s(c,"cnr")),13));
   card.addView(label("Hearing: "+dash(hearing)+"  |  "+dash(s(c,"type")),14));
   card.addView(label(dash(s(c,"petitioner"))+" vs "+dash(s(c,"respondent")),13));
   card.addView(label(dash(s(c,"court"))+" • "+dash(s(c,"location")),13));
   card.addView(label("Status: "+dash(s(c,"status"))+" • Advocate: "+dash(s(c,"advocate")),12));
   card.addView(label("Result: "+dash(s(c,"outcome"))+" • Judgment: "+dash(s(c,"judgment_date")),12));
   LinearLayout row=new LinearLayout(this);Button view=btn("View / Edit");Button hist=btn("History");Button del=btn("Delete");row.addView(view);row.addView(hist);row.addView(del);card.addView(row);
   view.setOnClickListener(v->form(id));hist.setOnClickListener(v->history(id,title));del.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Delete this case and its history?").setNegativeButton("Cancel",null).setPositiveButton("Delete",(x,y)->{Reminder.cancel(this,id);db().getWritableDatabase().delete("cases","_id=?",new String[]{""+id});db().getWritableDatabase().delete("history","case_id=?",new String[]{""+id});render(query.getText().toString());}).show());
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(7),0,dp(7));cards.addView(card,lp);count++;
  }c.close();if(count==0)cards.addView(label("No cases found.",15));
 }
 String s(Cursor c,String k){String x=c.getString(c.getColumnIndexOrThrow(k));return x==null?"":x;}String dash(String x){return x.isEmpty()?"—":x;}
 String allSearch(Cursor c){StringBuilder b=new StringBuilder();for(String k:new String[]{"title","case_no","cnr","petitioner","respondent","court","location","judge","type","advocate","status","outcome","judgment_summary","appeal_number"})b.append(s(c,k)).append(' ');return b.toString();}
 void form(int id){shell(id<0?"Register Case":"Case Details & Edit");HashMap<String,EditText> e=new HashMap<>();
  // Use a final holder so the case ID can be updated inside click-listener lambdas.
  final int[] caseId={id};
  for(int i=0;i<keys.length;i++){TextView l=label(labels[i],13);l.setPadding(0,dp(8),0,0);page.addView(l);EditText x=new EditText(this);x.setTextSize(15);x.setSingleLine(i!=13);if(i==13||i==16||i==17||i==19||i==24)x.setMinLines(3);page.addView(x);e.put(keys[i],x);}
  CheckBox priority=new CheckBox(this);priority.setText("Mark as priority / urgent");page.addView(priority);
  TextView rl=label("Reminder before hearing",13);page.addView(rl);Spinner rem=new Spinner(this);String[] opts={"On hearing day","1 day before","2 days before","3 days before","7 days before"};rem.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,opts));page.addView(rem);
  if(caseId[0]>=0){Cursor c=db().getReadableDatabase().rawQuery("SELECT * FROM cases WHERE _id=?",new String[]{""+caseId[0]});if(c.moveToFirst()){for(String k:keys)e.get(k).setText(s(c,k));priority.setChecked(c.getInt(c.getColumnIndexOrThrow("priority"))==1);rem.setSelection(Math.max(0,Math.min(4,c.getInt(c.getColumnIndexOrThrow("reminder_days")))));}c.close();}
  Button save=btn("Save Case");page.addView(save);save.setOnClickListener(v->{if(e.get("title").getText().toString().trim().isEmpty()){e.get("title").setError("Case title is required");return;}
   for(String k:new String[]{"filing","hearing","judgment_date","action_deadline"}){String d=e.get(k).getText().toString().trim();if(!d.isEmpty()&&!validDate(d)){e.get(k).setError("Use dd-MM-yyyy");return;}}
   ContentValues cv=new ContentValues();for(String k:keys)cv.put(k,e.get(k).getText().toString().trim());cv.put("priority",priority.isChecked()?1:0);cv.put("reminder_days",rem.getSelectedItemPosition());
   if(caseId[0]<0){cv.put("created_at",today());long newid=db().getWritableDatabase().insert("cases",null,cv);caseId[0]=(int)newid;if(!e.get("hearing").getText().toString().trim().isEmpty())addHistory(caseId[0],today(),"CASE CREATED","Next hearing entered: "+e.get("hearing").getText());}
   else {db().getWritableDatabase().update("cases",cv,"_id=?",new String[]{""+caseId[0]});addHistory(caseId[0],today(),"CASE UPDATED","Case details updated.");}
   String date=e.get("hearing").getText().toString().trim();if(!date.isEmpty())Reminder.schedule(this,caseId[0],e.get("title").getText().toString(),date,rem.getSelectedItemPosition());Toast.makeText(this,"Case saved",Toast.LENGTH_SHORT).show();mode="ALL";listing();
  });
  Button addHistory=btn("Add Hearing / Order History");page.addView(addHistory);addHistory.setOnClickListener(v->{if(caseId[0]<0){Toast.makeText(this,"Save the case first",Toast.LENGTH_SHORT).show();return;}history(caseId[0],e.get("title").getText().toString());});
  Button back=btn("Cancel");page.addView(back);back.setOnClickListener(v->listing());
 }
 boolean validDate(String x){try{SimpleDateFormat f=new SimpleDateFormat("dd-MM-yyyy",Locale.US);f.setLenient(false);f.parse(x);return true;}catch(Exception ex){return false;}}
 void addHistory(int id,String date,String type,String details){ContentValues v=new ContentValues();v.put("case_id",id);v.put("event_date",date);v.put("event_type",type);v.put("details",details);db().getWritableDatabase().insert("history",null,v);}
 void history(int id,String title){shell("Case History: "+title);Cursor c=db().getReadableDatabase().rawQuery("SELECT * FROM history WHERE case_id=? ORDER BY _id DESC",new String[]{""+id});while(c.moveToNext()){page.addView(label(s(c,"event_date")+" • "+s(c,"event_type")+"\n"+s(c,"details")+"\n",14));}c.close();
  Button add=btn("+ Add Hearing / Order / Note");page.addView(add);add.setOnClickListener(v->historyForm(id));Button back=btn("Back");page.addView(back);back.setOnClickListener(v->{mode="ALL";listing();});}
 void historyForm(int id){final EditText type=new EditText(this),detail=new EditText(this);type.setHint("Event type: Hearing / Order / Adjournment / Note");detail.setHint("Details / next date / order summary");new AlertDialog.Builder(this).setTitle("Add case history").setView(wrap(type,detail)).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{addHistory(id,today(),type.getText().toString(),detail.getText().toString());history(id,"Case");}).show();}
 View wrap(View a,View b){LinearLayout l=new LinearLayout(this);l.setPadding(dp(16),0,dp(16),0);l.setOrientation(1);l.addView(a);l.addView(b);return l;}
 void dateSearch(){shell("Hearing Date Search");EditText date=new EditText(this);date.setHint("Enter date dd-MM-yyyy");date.setText(today());page.addView(date);Button go=btn("Show hearings on this date");page.addView(go);go.setOnClickListener(v->{if(!validDate(date.getText().toString())){date.setError("Use dd-MM-yyyy");return;}mode="DATE:"+date.getText();dateResults(date.getText().toString());});}
 void dateResults(String date){shell("Hearings: "+date);Cursor c=db().getReadableDatabase().rawQuery("SELECT * FROM cases WHERE hearing=? ORDER BY court,title",new String[]{date});while(c.moveToNext())page.addView(label(s(c,"title")+" • "+s(c,"case_no")+"\n"+s(c,"court")+" • "+s(c,"petitioner")+" vs "+s(c,"respondent")+"\n",14));c.close();Button b=btn("Dashboard");page.addView(b);b.setOnClickListener(v->home());}
 void backupMenu(){shell("Backup & Export");addHome("Export CSV case list",()->exportCsv());addHome("Create JSON backup",()->exportJson());addHome("Restore JSON backup",()->restoreJson());addHome("Back to Dashboard",()->home());}
 void exportCsv(){try{File f=new File(getExternalFilesDir(null),"advocate_cases.csv");Cursor c=db().getReadableDatabase().rawQuery("SELECT * FROM cases",null);FileWriter w=new FileWriter(f);for(int i=0;i<c.getColumnCount();i++){if(i>0)w.append(',');w.append(c.getColumnName(i));}w.append('\n');while(c.moveToNext()){for(int i=0;i<c.getColumnCount();i++){if(i>0)w.append(',');String val=c.getString(i);w.append('"').append((val==null?"":val).replace("\"","\"\"")).append('"');}w.append('\n');}c.close();w.close();share(f,"text/csv");}catch(Exception e){toast("Export failed: "+e.getMessage());}}
 void exportJson(){try{File f=new File(getExternalFilesDir(null),"advocate_desk_backup.json");Cursor c=db().getReadableDatabase().rawQuery("SELECT * FROM cases",null);FileWriter w=new FileWriter(f);w.write("{\"cases\":[");boolean first=true;while(c.moveToNext()){if(!first)w.write(",");first=false;w.write("{");for(int i=0;i<c.getColumnCount();i++){if(i>0)w.write(",");w.write("\""+c.getColumnName(i)+"\":\""+esc(c.getString(i))+"\"");}w.write("}");}w.write("],\"history\":[");Cursor h=db().getReadableDatabase().rawQuery("SELECT * FROM history",null);boolean hf=true;while(h.moveToNext()){if(!hf)w.write(",");hf=false;w.write("{");for(int i=0;i<h.getColumnCount();i++){if(i>0)w.write(",");w.write("\""+h.getColumnName(i)+"\":\""+esc(h.getString(i))+"\"");}w.write("}");}h.close();w.write("]}");w.close();c.close();share(f,"application/json");}catch(Exception e){toast("Backup failed: "+e.getMessage());}}
 String esc(String s){if(s==null)return "";return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","");}
 
void restoreJson(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");startActivityForResult(i,RESTORE_REQ);}
@Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode==RESTORE_REQ&&resultCode==RESULT_OK&&data!=null){try{InputStream in=getContentResolver().openInputStream(data.getData());ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);in.close();JSONObject root=new JSONObject(new String(out.toByteArray(),"UTF-8"));JSONArray arr=root.getJSONArray("cases");SQLiteDatabase sql=db().getWritableDatabase();int imported=0;sql.beginTransaction();try{for(int j=0;j<arr.length();j++){JSONObject o=arr.getJSONObject(j);ContentValues cv=new ContentValues();for(String k:keys)cv.put(k,o.optString(k,""));cv.put("priority",o.optInt("priority",0));cv.put("reminder_days",o.optInt("reminder_days",1));cv.put("created_at",o.optString("created_at",today()));sql.insert("cases",null,cv);imported++;}if(root.has("history")){JSONArray hist=root.getJSONArray("history");for(int j=0;j<hist.length();j++){JSONObject o=hist.getJSONObject(j);ContentValues hv=new ContentValues();hv.put("case_id",o.optInt("case_id"));hv.put("event_date",o.optString("event_date",""));hv.put("event_type",o.optString("event_type",""));hv.put("details",o.optString("details",""));sql.insert("history",null,hv);}}sql.setTransactionSuccessful();}finally{sql.endTransaction();}toast("Imported "+imported+" cases. Check for duplicates.");home();}catch(Exception e){toast("Restore failed: invalid backup or read error: "+e.getMessage());}}}

 void share(File f,String mime){Uri u=androidxUri(f);Intent i=new Intent(Intent.ACTION_SEND);i.setType(mime);i.putExtra(Intent.EXTRA_STREAM,u);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Share backup"));}
 Uri androidxUri(File f){return androidx.core.content.FileProvider.getUriForFile(this,"in.advocatedesk.siliguri.fileprovider",f);}

 void cloudMenu(){shell("Team Cloud & Sync");SupabaseCloud cloud=new SupabaseCloud(this);
  page.addView(label("Connect your private Supabase project. Use the project URL, publishable anon key and workspace UUID. Never enter a service_role key here.",13));
  Button cfg=btn("Configure Cloud Project / Team Login");page.addView(cfg);cfg.setOnClickListener(v->cloudConfig());
  TextView state=label("Project configured: "+(cloud.isConfigured()?"YES":"NO")+"\nSigned in: "+(cloud.isSignedIn()?"YES":"NO")+"\nWorkspace: "+(cloud.workspace().isEmpty()?"Not set":cloud.workspace()),14);state.setPadding(0,dp(12),0,dp(12));page.addView(state);
  Button sign=btn("Sign In / Create Team Account");page.addView(sign);sign.setOnClickListener(v->cloudAuth());
  Button workspace=btn("Create First Team Workspace (Owner)");page.addView(workspace);workspace.setOnClickListener(v->{if(!cloud.isSignedIn()){toast("Sign in with the owner account first");return;}new android.os.AsyncTask<Void,Void,String>(){protected String doInBackground(Void...x){try{return "Workspace created: "+cloud.createFirstWorkspace("ADVOCATE DESK – SILIGURI");}catch(Exception e){return "Workspace setup failed: "+e.getMessage();}}protected void onPostExecute(String m){toast(m);cloudMenu();}}.execute();});
  Button sync=btn("Sync Cases Now (Upload then Download)");page.addView(sync);sync.setOnClickListener(v->{if(!cloud.isConfigured()||!cloud.isSignedIn()){toast("Configure cloud and sign in first");return;}new android.os.AsyncTask<Void,Void,String>(){protected String doInBackground(Void...x){try{int up=cloud.pushLocalCases(db());int down=cloud.pullCases(db());return "Uploaded "+up+" local cases; downloaded/updated "+down+" cloud cases.";}catch(Exception e){return "Sync failed: "+e.getMessage();}}protected void onPostExecute(String m){toast(m);home();}}.execute();});
  Button out=btn("Sign Out");page.addView(out);out.setOnClickListener(v->{cloud.signOut();toast("Signed out on this device");cloudMenu();});
  page.addView(label("Before team use: run both SQL migrations, create a private case-documents bucket, create the first workspace, and add each colleague as a workspace member. See SUPABASE_SETUP.md.",12));Button back=btn("Dashboard");page.addView(back);back.setOnClickListener(v->home());
 }
 void cloudConfig(){SupabaseCloud cloud=new SupabaseCloud(this);LinearLayout l=new LinearLayout(this);l.setPadding(dp(18),0,dp(18),0);l.setOrientation(1);EditText url=new EditText(this),key=new EditText(this),wid=new EditText(this);url.setHint("Supabase Project URL");key.setHint("Publishable anon key");wid.setHint("Workspace UUID");url.setText(getSharedPreferences("cloud_config",MODE_PRIVATE).getString("url",""));key.setText(getSharedPreferences("cloud_config",MODE_PRIVATE).getString("anon",""));wid.setText(getSharedPreferences("cloud_config",MODE_PRIVATE).getString("workspace",""));l.addView(url);l.addView(key);l.addView(wid);new AlertDialog.Builder(this).setTitle("Cloud Project Configuration").setView(l).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{try{cloud.configure(url.getText().toString(),key.getText().toString(),wid.getText().toString());toast("Cloud settings saved on this device");cloudMenu();}catch(Exception e){toast(e.getMessage());}}).show();}
 void cloudAuth(){SupabaseCloud cloud=new SupabaseCloud(this);if(!cloud.isConfigured()){toast("Configure project first");return;}LinearLayout l=new LinearLayout(this);l.setPadding(dp(18),0,dp(18),0);l.setOrientation(1);EditText email=new EditText(this),pass=new EditText(this);email.setHint("Team member email");email.setInputType(33);pass.setHint("Password");pass.setInputType(129);l.addView(email);l.addView(pass);new AlertDialog.Builder(this).setTitle("Team Account").setView(l).setNegativeButton("Cancel",null).setNeutralButton("Create Account",(d,w)->new android.os.AsyncTask<Void,Void,String>(){protected String doInBackground(Void...x){try{cloud.signUp(email.getText().toString().trim(),pass.getText().toString());return "Account created. Verify email if enabled, then sign in. An owner must add this user to the workspace.";}catch(Exception e){return "Sign-up failed: "+e.getMessage();}}protected void onPostExecute(String m){toast(m);}}.execute()).setPositiveButton("Sign In",(d,w)->new android.os.AsyncTask<Void,Void,String>(){protected String doInBackground(Void...x){try{cloud.signIn(email.getText().toString().trim(),pass.getText().toString());return "Signed in. Check workspace membership before syncing.";}catch(Exception e){return "Sign-in failed: "+e.getMessage();}}protected void onPostExecute(String m){toast(m);cloudMenu();}}.execute()).show();}

 void courtLinks(){
  shell("Siliguri & Jalpaiguri Courts");
  TextView intro=label("OFFICIAL COURT WEBSITES • OPEN INSIDE ADVOCATE DESK\nChoose the court where the case is listed. You can search the official case-status service and complete any CAPTCHA yourself.",13);
  intro.setTextColor(0xffb8c7d4);intro.setPadding(0,dp(8),0,dp(14));page.addView(intro);
  TextView sil=label("SILIGURI / DARJEELING JUDGESHIP",12);sil.setTextColor(0xff00e5ff);sil.setTypeface(null,Typeface.BOLD);page.addView(sil);
  addHome("Open Siliguri Sub-Division Court website",()->openCourtWeb("https://darjeeling.dcourts.gov.in/","Siliguri / Darjeeling Court"));
  addHome("Siliguri Court – Judge / Court list",()->openCourtWeb("https://darjeeling.dcourts.gov.in/list-of-judges/","Siliguri Court List"));
  TextView jpg=label("JALPAIGURI JUDGESHIP",12);jpg.setTextColor(0xff00e5ff);jpg.setTypeface(null,Typeface.BOLD);jpg.setPadding(0,dp(12),0,0);page.addView(jpg);
  addHome("Open Jalpaiguri District Court website",()->openCourtWeb("https://jalpaiguri.dcourts.gov.in/","Jalpaiguri District Court"));
  addHome("Jalpaiguri Court – Case Status / Orders / Cause List",()->openCourtWeb("https://jalpaiguri.dcourts.gov.in/","Jalpaiguri Court Services"));
  TextView common=label("COMMON OFFICIAL CASE SERVICES",12);common.setTextColor(0xff00e5ff);common.setTypeface(null,Typeface.BOLD);common.setPadding(0,dp(12),0,0);page.addView(common);
  addHome("Search case by CNR number",()->openCourtWeb("https://services.ecourts.gov.in/ecourtindia_v6/","eCourts CNR Search"));
  addHome("Search by case number / party / advocate",()->openCourtWeb("https://services.ecourts.gov.in/ecourtindia_v6/casestatus/","eCourts Case Status"));
  addHome("Calcutta High Court – Case Status",()->openCourtWeb("https://hcservices.ecourts.gov.in/hcservices/main.php","High Court Services"));
  TextView note=label("HOW TO USE\n1. Choose Siliguri / Darjeeling or Jalpaiguri.\n2. Open Case Status or CNR Search.\n3. Enter the case details and complete CAPTCHA if requested.\n4. Check the latest hearing date, case status and orders on the official page.\n\nIMPORTANT\nThese official sites are now accessible from inside this app. The government portal does not provide a confirmed free automatic data feed for this app, so it cannot silently update saved cases in the background. After checking the court website, update the case diary and hearing date in Advocate Desk. Always verify court information on the official source.",12);
  note.setTextColor(0xffd4dde5);note.setPadding(dp(12),dp(14),dp(12),dp(14));note.setBackgroundColor(0xff111820);page.addView(note);
  addHome("Back to Dashboard",()->home());
 }
 void openCourtWeb(String url,String title){
  final Dialog dialog=new Dialog(this,android.R.style.Theme_Material_NoActionBar_Fullscreen);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(0xff07090d);
  LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(10),dp(8),dp(10),dp(8));bar.setBackgroundColor(0xff101820);
  Button back=btn("← Back");TextView heading=label(title,15);heading.setTypeface(null,Typeface.BOLD);heading.setPadding(dp(10),0,0,0);bar.addView(back);bar.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
  ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);
  WebView web=new WebView(this);WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setLoadsImagesAutomatically(true);settings.setSupportZoom(true);settings.setBuiltInZoomControls(true);settings.setDisplayZoomControls(false);settings.setJavaScriptCanOpenWindowsAutomatically(false);
  web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView view,String next){return !next.startsWith("https://");}@Override public void onPageStarted(WebView view,String u,android.graphics.Bitmap icon){progress.setVisibility(View.VISIBLE);}@Override public void onPageFinished(WebView view,String u){progress.setVisibility(View.GONE);}});
  web.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView view,int value){progress.setProgress(value);if(value>=100)progress.setVisibility(View.GONE);}});
  root.addView(bar);root.addView(progress,new LinearLayout.LayoutParams(-1,dp(3)));root.addView(web,new LinearLayout.LayoutParams(-1,0,1));dialog.setContentView(root);back.setOnClickListener(v->{if(web.canGoBack())web.goBack();else dialog.dismiss();});dialog.setOnKeyListener((d,key,event)->{if(key==android.view.KeyEvent.KEYCODE_BACK&&event.getAction()==android.view.KeyEvent.ACTION_UP){if(web.canGoBack())web.goBack();else dialog.dismiss();return true;}return false;});dialog.show();dialog.getWindow().setLayout(-1,-1);web.loadUrl(url);
 }
 void open(String u){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception e){toast("Could not open the court website. Please check your internet connection.");}}
 void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
