package com.ijustyce.weibo;

public interface Constants {
	
	public static final String APP_KEY = "2338577832";
	public static final String APP_SECRET = "5e1255a6018bb192cac8632e160bc783";
	public static final String REDIRECT_URL = "http://www.weibo.com";
	public static final String SCOPE = "all";
	public static final String WEIBOSERVER = "https://upload.api.weibo.com/2/";
	public static final String CANCEL="https://api.weibo.com/oauth2/revokeoauth2";
	public static final String GET_FRIENDS="https://api.weibo.com/2/friendships/friends.json";
	public static final String GET_UID="https://api.weibo.com/2/account/get_uid.json";
	public static final String GET_COMMON_FRIENDS="https://api.weibo.com/2/friendships/friends/in_common.json";
	
	public static final String METHOD_CANCEL="METHOD_CANCEL";
	public static final String METHOD_FRIENDS="METHOD_FRIENDS";
	public static final String METHOD_UID="METHOD_UID";
	public static final String SHARED_COMMON="common";
	public static final String SHARED_WEIBO="weibo";
}
