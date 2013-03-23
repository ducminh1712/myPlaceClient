package org.k2htm.tnc;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

import edu.k2htm.datahelper.Report;

public class IncidentOverlayItem extends OverlayItem {
	private Report report;

	public IncidentOverlayItem(GeoPoint arg0, String arg1, String arg2) {
		super(arg0, arg1, arg2);
		this.report=null;;
		// TODO Auto-generated constructor stub
	}

	public IncidentOverlayItem(Report report) {
		super(new GeoPoint(report.getLat(), report.getLng()), report
				.getUsername(), report.getDescription());
		this.report = report;
		

	}

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}
}
