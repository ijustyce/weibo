package com.ijustyce.weibo;

import org.gemini.httpengine.listener.OnResponseListener;
import org.gemini.httpengine.net.GMHttpParameters;
import org.gemini.httpengine.net.GMHttpRequest;
import org.gemini.httpengine.net.GMHttpResponse;
import org.gemini.httpengine.net.GMHttpService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.util.AccessTokenKeeper;

public class MainActivity extends ActionBarActivity implements OnResponseListener {

	private GMHttpRequest httpRequest;
	private GMHttpParameters httpParam;
	private GMHttpService httpService;
	
	private Weibo mWeibo;
	private Oauth2AccessToken mAccessToken;
	
	private String method;
	private String sharedName;
	
	private String uid;
	
	public void btClick(View v){
		
		switch(v.getId()){
		case R.id.login:
			login();
			break;
		case R.id.friends:
			renMai();
			break;
		case R.id.commonFriends:
			commonFriends();
			break;
		default:
			break;
		}
	}
	
	/**
	 * init some variable 
	 */
	private void init(){
		
		httpRequest = new GMHttpRequest(this.getBaseContext());
		httpParam = new GMHttpParameters();
		httpService = new GMHttpService();
		
		SharedPreferences shared = getSharedPreferences("weibo" , Context.MODE_PRIVATE);
		uid = shared.getString("uid", "");
	}
	
	private void login(){
		
		if(!getToken().equals("")){
			
			showToast("login success");
			return ;
		}
		
		mWeibo = Weibo.getInstance(Constants.APP_KEY, Constants.REDIRECT_URL,
				Constants.SCOPE);
		mWeibo.anthorize(this, new AuthDialogListener());
	}
	
	private void renMai(){
		
		login();
		
		init();
		
		httpRequest.setUri(Constants.GET_FRIENDS);
		httpRequest.setOnResponseListener(this);
		httpParam.setParameter("access_token" , getToken());
		httpParam.setParameter("uid", uid);
		httpRequest.setHttpParameters(httpParam);
		excute();
		
		method = Constants.METHOD_FRIENDS;
		sharedName = Constants.SHARED_WEIBO;
	}
	
	private void commonFriends(){
		
		login();
		init();
		
		SharedPreferences shared = getSharedPreferences("weibo" , Context.MODE_PRIVATE);
		String id = shared.getString("uid1", "");
		
		httpRequest.setUri(Constants.GET_COMMON_FRIENDS);
		httpRequest.setOnResponseListener(this);
		httpParam.setParameter("access_token" , getToken());
		httpParam.setParameter("uid", uid);
		httpParam.setParameter("suid", id);
		httpRequest.setHttpParameters(httpParam);
		excute();
		
		method = Constants.METHOD_FRIENDS;
		sharedName = Constants.SHARED_COMMON;
	}
	
	/**
	 * 取消微博的授权
	 */
	public void Cancel() {

		if (getToken().equals("")) {

			showToast("success");
			return;
		}
		
		init();
		method = Constants.METHOD_CANCEL;
		
		httpRequest.setUri(Constants.CANCEL);
		httpRequest.setOnResponseListener(this);
		httpParam.setParameter("access_token" , getToken());
		httpRequest.setHttpParameters(httpParam);
		excute();
		
		AccessTokenKeeper.clear(MainActivity.this);
	}
	
	/**
	 * 得到授权
	 * 
	 * @return
	 */
	private String getToken() {

		mAccessToken = AccessTokenKeeper.readAccessToken(this);
		String token = mAccessToken.getToken();
		Log.i("---justyce---", "token:" + token);
		return token;
	}
	
	/**
	 * excute a http request
	 */
	private void excute(){
		
		if(isConnected()){
			httpService.executeHttpMethod(httpRequest);
		}
		
		else{
			Toast.makeText(this, "please connect to net first", 
					Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * 
	 * is phone connect to network
	 * 
	 * @return true if connected to network or return false
	 */

	public boolean isConnected() {

		ConnectivityManager conManager = (ConnectivityManager) this

		.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = conManager.getActiveNetworkInfo();

		if (networkInfo != null) {

			return networkInfo.isAvailable();

		}

		return false;
	}
	
	/**
	 * deal response of cancel auth
	 * @param json
	 */
	
	private void doCancel(JSONObject json){
		
		try {
			String result = json.getString("result");
			Log.i("---cancel---", result);
		} catch (JSONException e) {
			// 
			e.printStackTrace();
		}
	}
	
	private void doUid(JSONObject json){
		
		try {
			uid = json.getString("uid");
			SharedPreferences shared = getSharedPreferences("weibo" , Context.MODE_PRIVATE);
			shared.edit().putString("uid", uid).commit();
			Log.i("---uid---", uid);
		} catch (JSONException e) {
			// 
			e.printStackTrace();
		}	
	}
	
	private void doFriends(JSONObject json){
		
		try {
			JSONArray jsonArray = json.getJSONArray("users");
			SharedPreferences shared = getSharedPreferences(sharedName , Context.MODE_PRIVATE);
			shared.edit().putInt("total" , jsonArray.length()).commit();
			for(int i = 0;i<jsonArray.length();i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String id = jsonObject.getString("id");
				String name = jsonObject.getString("name");
	//			String location = jsonObject.getString("location");
	//			String description = jsonObject.getString("description");
	//			String statuses_count = "" + jsonObject.getString("statuses_count");
	//			String followers_count = "" + jsonObject.getInt("followers_count");
	//			String friends_count = "" + jsonObject.getInt("friends_count");
				Log.i("---friends---", "id: " + id + "  nickName: " + name);
				
				shared.edit().putString("uid" + i, id).commit();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		
		httpRequest = null;
		httpParam = null;
		httpService = null;
	}
	
	private void getUid(){
		
		init();
		method = Constants.METHOD_UID;
		
		httpRequest.setUri(Constants.GET_UID);
		httpRequest.setOnResponseListener(this);
		httpParam.setParameter("access_token" , getToken());
		httpRequest.setHttpParameters(httpParam);
		excute();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			
			Cancel();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	public void onResponse(GMHttpResponse response, GMHttpRequest request) {
		
		if(!request.isFailed()){
			
			Log.i("---response---", response.parseAsString());
			if(method.equals(Constants.METHOD_CANCEL)){
				
				doCancel(response.parseAsJSON());
				return;
			}
			
			else if(method.equals(Constants.METHOD_UID)){
				doUid(response.parseAsJSON());
				return ;
			}
			
			else if(method.equals(Constants.METHOD_FRIENDS)){
				doFriends(response.parseAsJSON());
				return ;
			}
		}
		
		else{
			Log.i("---request---", "Failed");
		}
	}
	
	private void showToast(String text){
		
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}
	
	class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onCancel() {

			showToast("cancel");
		}

		@Override
		public void onComplete(Bundle values) {

			String token = values.getString("access_token");
			Log.i("---login---", token);
			String expires_in = values.getString("expires_in");
			mAccessToken = new Oauth2AccessToken(token, expires_in);
			if (mAccessToken.isSessionValid()) {

				AccessTokenKeeper.keepAccessToken(MainActivity.this,mAccessToken);
			}

			showToast("success");			
			getUid();
		}

		@Override
		public void onError(WeiboDialogError e) {

			showToast(e.getMessage());
		}

		@Override
		public void onWeiboException(WeiboException e) {

			showToast(e.getMessage());
		}
	}

}
