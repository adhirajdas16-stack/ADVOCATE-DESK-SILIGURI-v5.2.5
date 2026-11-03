package in.advocatedesk.siliguri;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.content.ContentValues;
import net.sqlcipher.database.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.UUID;

/** Supabase REST/Auth/Storage bridge. Uses the publishable anon key only; never embed service_role. */
public final class SupabaseCloud {
  private final Context context;
  private final SharedPreferences prefs;
  public SupabaseCloud(Context c){ context=c.getApplicationContext(); prefs=context.getSharedPreferences("cloud_config",Context.MODE_PRIVATE); }
  public void configure(String url,String anonKey,String workspaceId){
    prefs.edit().putString("url",url.trim().replaceAll("/$", "")).putString("anon",anonKey.trim()).putString("workspace",workspaceId.trim()).apply();
  }
  public boolean isConfigured(){return !get("url").isEmpty()&&!get("anon").isEmpty()&&!get("workspace").isEmpty();}
  public String workspace(){return get("workspace");}
  public String userId(){return get("user_id");}
  public boolean isSignedIn(){return !get("access_token").isEmpty();}
  public void signOut(){prefs.edit().remove("access_token").remove("refresh_token").remove("user_id").apply();}
  private String get(String k){return prefs.getString(k,"");}
  private String base(){if(!isConfigured())throw new IllegalStateException("Cloud URL, anon key and workspace ID are required");return get("url");}
  private JSONObject request(String method,String path,JSONObject body,boolean auth,String prefer) throws Exception {
    URL u=new URL(base()+path); HttpURLConnection c=(HttpURLConnection)u.openConnection(); c.setRequestMethod(method);c.setConnectTimeout(15000);c.setReadTimeout(30000);
    c.setRequestProperty("apikey",get("anon"));c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Accept","application/json");
    if(auth){if(!isSignedIn())throw new IllegalStateException("Sign in first");c.setRequestProperty("Authorization","Bearer "+get("access_token"));}else c.setRequestProperty("Authorization","Bearer "+get("anon"));
    if(prefer!=null&&!prefer.isEmpty())c.setRequestProperty("Prefer",prefer);
    if(body!=null){c.setDoOutput(true);byte[] b=body.toString().getBytes("UTF-8");c.getOutputStream().write(b);}
    int code=c.getResponseCode();InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);in.close();String s=new String(out.toByteArray(),"UTF-8");c.disconnect();
    if(code<200||code>=300)throw new IOException("Cloud HTTP "+code+": "+s);
    if(s.trim().isEmpty())return new JSONObject(); if(s.trim().startsWith("[")){JSONObject x=new JSONObject();x.put("items",new JSONArray(s));return x;}if(s.trim().startsWith("\"")){JSONObject x=new JSONObject();x.put("value",new JSONArray("["+s+"]").getString(0));return x;}return new JSONObject(s);
  }
  public void signIn(String email,String password) throws Exception {
    JSONObject b=new JSONObject();b.put("email",email);b.put("password",password);
    JSONObject r=request("POST","/auth/v1/token?grant_type=password",b,false,null);
    JSONObject user=r.getJSONObject("user");prefs.edit().putString("access_token",r.getString("access_token")).putString("refresh_token",r.optString("refresh_token")).putString("user_id",user.getString("id")).apply();
  }
  public void signUp(String email,String password) throws Exception {
    JSONObject b=new JSONObject();b.put("email",email);b.put("password",password);
    request("POST","/auth/v1/signup",b,false,null);
  }
  public String createFirstWorkspace(String name) throws Exception {
    JSONObject b=new JSONObject();b.put("workspace_name",name);
    JSONObject r=request("POST","/rest/v1/rpc/create_first_workspace",b,true,null);
    String id=r.optString("value","");if(id.isEmpty())id=r.optString("create_first_workspace","");if(id.isEmpty())id=r.optString("id","");if(id.isEmpty())throw new IOException("Workspace created but RPC response did not include its UUID: "+r.toString());
    configure(get("url"),get("anon"),id);return id;
  }
  public int pushLocalCases(DBHelper helper) throws Exception {
    if(!isSignedIn())throw new IllegalStateException("Sign in first");
    SQLiteDatabase db=helper.getWritableDatabase();
    try{db.execSQL("ALTER TABLE cases ADD COLUMN cloud_id TEXT DEFAULT ''");}catch(Exception ignored){}
    Cursor c=db.rawQuery("SELECT * FROM cases",null);int sent=0;
    while(c.moveToNext()){
      String cloudId=col(c,"cloud_id");if(cloudId.isEmpty()){cloudId=UUID.randomUUID().toString();ContentValues cv=new ContentValues();cv.put("cloud_id",cloudId);db.update("cases",cv,"_id=?",new String[]{String.valueOf(c.getInt(c.getColumnIndexOrThrow("_id")))});}
      JSONObject o=new JSONObject();o.put("id",cloudId);o.put("workspace_id",workspace());o.put("title",col(c,"title"));o.put("case_no",col(c,"case_no"));o.put("cnr",col(c,"cnr"));o.put("petitioner",col(c,"petitioner"));o.put("respondent",col(c,"respondent"));o.put("court",col(c,"court"));o.put("location",col(c,"location"));o.put("judge",col(c,"judge"));o.put("case_type",col(c,"type"));o.put("advocate",col(c,"advocate"));o.put("filing_date",date(col(c,"filing")));o.put("next_hearing",date(col(c,"hearing")));o.put("status",empty(col(c,"status"),"Pending"));o.put("priority",c.getInt(c.getColumnIndexOrThrow("priority")));o.put("reminder_days",c.getInt(c.getColumnIndexOrThrow("reminder_days")));o.put("notes",col(c,"notes"));o.put("outcome",col(c,"outcome"));o.put("judgment_date",date(col(c,"judgment_date")));o.put("judgment_summary",col(c,"judgment_summary"));o.put("relief",col(c,"relief"));o.put("amount_awarded",col(c,"amount_awarded"));o.put("observations",col(c,"observations"));o.put("appeal_filed",empty(col(c,"appeal_filed"),"Not recorded"));o.put("appeal_number",col(c,"appeal_number"));o.put("appeal_court",col(c,"appeal_court"));o.put("appeal_status",col(c,"appeal_status"));o.put("next_action",col(c,"next_action"));o.put("action_deadline",date(col(c,"action_deadline")));o.put("created_by",userId());o.put("updated_by",userId());
      request("POST","/rest/v1/cases?on_conflict=id",o,true,"resolution=merge-duplicates");sent++;
    }c.close();return sent;
  }
  public int pullCases(DBHelper helper) throws Exception {
    JSONObject r=request("GET","/rest/v1/cases?select=*&workspace_id=eq."+URLEncoder.encode(workspace(),"UTF-8")+"&deleted_at=is.null",null,true,null);JSONArray a=r.getJSONArray("items");SQLiteDatabase db=helper.getWritableDatabase();
    try{db.execSQL("ALTER TABLE cases ADD COLUMN cloud_id TEXT DEFAULT ''");}catch(Exception ignored){}int count=0;
    for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);String id=o.getString("id");Cursor exists=db.rawQuery("SELECT _id FROM cases WHERE cloud_id=?",new String[]{id});boolean found=exists.moveToFirst();int local=found?exists.getInt(0):-1;exists.close();ContentValues v=new ContentValues();v.put("cloud_id",id);v.put("title",o.optString("title"));v.put("case_no",o.optString("case_no"));v.put("cnr",o.optString("cnr"));v.put("petitioner",o.optString("petitioner"));v.put("respondent",o.optString("respondent"));v.put("court",o.optString("court"));v.put("location",o.optString("location"));v.put("judge",o.optString("judge"));v.put("type",o.optString("case_type"));v.put("advocate",o.optString("advocate"));v.put("filing",fromDate(o.optString("filing_date")));v.put("hearing",fromDate(o.optString("next_hearing")));v.put("status",o.optString("status"));v.put("priority",o.optInt("priority"));v.put("reminder_days",o.optInt("reminder_days",1));v.put("notes",o.optString("notes"));v.put("outcome",o.optString("outcome"));v.put("judgment_date",fromDate(o.optString("judgment_date")));v.put("judgment_summary",o.optString("judgment_summary"));v.put("relief",o.optString("relief"));v.put("amount_awarded",o.optString("amount_awarded"));v.put("observations",o.optString("observations"));v.put("appeal_filed",o.optString("appeal_filed"));v.put("appeal_number",o.optString("appeal_number"));v.put("appeal_court",o.optString("appeal_court"));v.put("appeal_status",o.optString("appeal_status"));v.put("next_action",o.optString("next_action"));v.put("action_deadline",fromDate(o.optString("action_deadline")));if(found)db.update("cases",v,"_id=?",new String[]{String.valueOf(local)});else{v.put("created_at",o.optString("created_at"));db.insert("cases",null,v);}count++;}
    return count;
  }
  public String uploadDocument(String caseId,String fileName,String mime,byte[] bytes) throws Exception {
    if(!isSignedIn())throw new IllegalStateException("Sign in first");String path=workspace()+"/"+caseId+"/"+UUID.randomUUID()+"_"+fileName.replaceAll("[^A-Za-z0-9._-]","_");URL u=new URL(base()+"/storage/v1/object/case-documents/"+path);HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(15000);c.setReadTimeout(60000);c.setRequestProperty("apikey",get("anon"));c.setRequestProperty("Authorization","Bearer "+get("access_token"));c.setRequestProperty("Content-Type",mime==null?"application/octet-stream":mime);c.setRequestProperty("x-upsert","false");c.getOutputStream().write(bytes);int code=c.getResponseCode();if(code<200||code>=300)throw new IOException("Storage upload failed HTTP "+code);c.disconnect();return path;
  }
  private String col(Cursor c,String k){int i=c.getColumnIndex(k);return i<0||c.isNull(i)?"":c.getString(i);}
  private String date(String d){if(d==null||d.trim().isEmpty())return null;try{java.text.SimpleDateFormat a=new java.text.SimpleDateFormat("dd-MM-yyyy",java.util.Locale.US);java.text.SimpleDateFormat b=new java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US);a.setLenient(false);return b.format(a.parse(d));}catch(Exception e){return null;}}
  private String fromDate(String d){if(d==null||d.isEmpty()||"null".equals(d))return "";try{java.text.SimpleDateFormat a=new java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US);java.text.SimpleDateFormat b=new java.text.SimpleDateFormat("dd-MM-yyyy",java.util.Locale.US);return b.format(a.parse(d));}catch(Exception e){return "";}}
  private String empty(String a,String b){return a==null||a.isEmpty()?b:a;}
}
