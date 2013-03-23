package org.k2htm.tnc;

import java.util.ArrayList;

import android.app.Application;
import android.util.Log;
import edu.k2htm.datahelper.Report;

public class TrafficNetworkClient extends Application {
	private int timeFilter;
	private String user;
	private ArrayList<Report> reportList;
	private String fbJson = "";
	private boolean loginAsFb = false;
	private boolean showType[] = { true, true, true };

	public static final String TAG = "Application Object";

	public ArrayList<Report> getReportList() {
		return reportList;
	}

	public void setReportList(ArrayList<Report> reportList) {
		this.reportList = reportList;
	}

	public static final int DEFAULT_TIME_FILTER = 720;
	public static final String DEFAULT_USER = "Guest";
	public static final String ADDRESS = "192.168.100.56:8080";

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		timeFilter = DEFAULT_TIME_FILTER;
		user = DEFAULT_USER;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public int getTimeFilter() {
		return timeFilter;
	}

	public void setTimeFilter(int time_filter) {
		this.timeFilter = time_filter;
	}

	public String getFbJson() {
		return fbJson;
	}

	public void setFbJson(String fbJson) {
		this.fbJson = fbJson;
	}

	public boolean isLoginAsFb() {
		return loginAsFb;
	}

	public void setLoginAsFb(boolean loginAsFb) {
		this.loginAsFb = loginAsFb;
	}

	public boolean[] getShowType() {
		return showType;
	}

	public void setShowType(boolean[] showType) {
		this.showType = showType;
	}
	

}
