package edu.psu.graph;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import edu.psu.database.DatabaseHandler;
import edu.psu.database.Gulp;

public class WeekBarGraph {
	private static final String TITLE = "Water Drank On Week Of %d/%d/%d";
	private static final String X_AXIS_TITLE = "Day";
	private static final String Y_AXIS_TITLE = "Amount Drank (Ounces)";
	
	private static final int DAYS_PER_WEEK = 7;
	
	public Intent getIntent(Context c, final int week, final int year) {
		CategorySeries series;
		XYMultipleSeriesDataset dataset;
		XYMultipleSeriesRenderer datasetRenderer;
		SimpleSeriesRenderer seriesRenderer;
		DatabaseHandler dbHandler;
		List<Gulp> data;
		Calendar cal;
		Date start, end;
		double max;
		double[] days;
		Gulp gulp;
		DecimalFormat format;
		DateFormatSymbols dateFormatSymbols;
		
		cal = Calendar.getInstance();
		cal.set(Calendar.WEEK_OF_YEAR, week);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		start = cal.getTime();
		
		cal.add(Calendar.DATE, DAYS_PER_WEEK);
		end = cal.getTime();
		
		dbHandler = new DatabaseHandler(c);
		data = dbHandler.getGulps(start, end);
		
		series = new CategorySeries("Data");
		
		days = new double[DAYS_PER_WEEK];
		Arrays.fill(days, 0);
		
		for (Iterator<Gulp> iter = data.iterator(); iter.hasNext();) {
			gulp = iter.next();
			cal.setTime(gulp.getDate());
			days[cal.get(Calendar.DAY_OF_WEEK) - 2] += gulp.getAmount();
		}
		
		format = new DecimalFormat("#.#");
		
		max = 0;
		for (double day : days) {
			series.add(Double.parseDouble(format.format(day)));
			
			if (day > max) {
				max = day;
			}
		}
		
		dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series.toXYSeries());
		
		datasetRenderer = new XYMultipleSeriesRenderer();
		seriesRenderer = new SimpleSeriesRenderer();
		
		dateFormatSymbols = new DateFormatSymbols();
		for (int i = 1; i < DAYS_PER_WEEK + 1; i++) {
			datasetRenderer.addXTextLabel(i, dateFormatSymbols.getShortWeekdays()[i]);
		}
		
		cal.setTime(start);
		datasetRenderer.setChartTitle(getTitle(cal.get(Calendar.MONTH), cal.get(Calendar.DATE), year));
		datasetRenderer.setXTitle(X_AXIS_TITLE);
		datasetRenderer.setYTitle(Y_AXIS_TITLE);
		datasetRenderer.setAxesColor(Color.BLACK);
		datasetRenderer.setLabelsColor(Color.BLACK);
		datasetRenderer.setBackgroundColor(Color.WHITE);
		datasetRenderer.setMarginsColor(Color.WHITE);
		datasetRenderer.setApplyBackgroundColor(true);
		datasetRenderer.setXLabelsColor(Color.BLACK);
		datasetRenderer.setYLabelsColor(0, Color.BLACK);
		datasetRenderer.setYLabelsAlign(Align.RIGHT);
		datasetRenderer.setShowLegend(false);
		datasetRenderer.setXLabels(0);
		datasetRenderer.setPanEnabled(false);
		datasetRenderer.setBarSpacing(0.5);
		datasetRenderer.setYAxisMin(0, 0);
		datasetRenderer.setYAxisMax(max * 1.1, 0);
		datasetRenderer.setXAxisMin(0);
		datasetRenderer.setXAxisMax(DAYS_PER_WEEK + 1);
		
		seriesRenderer.setGradientEnabled(true);
		seriesRenderer.setGradientStart(0.0, Color.BLUE);
		seriesRenderer.setGradientStop(max, Color.CYAN);
		seriesRenderer.setDisplayChartValues(true);
		seriesRenderer.setChartValuesTextAlign(Align.CENTER);
		
		datasetRenderer.addSeriesRenderer(seriesRenderer);
		
		return ChartFactory.getBarChartIntent(c, dataset, datasetRenderer, Type.DEFAULT);
	}
	
	private static String getTitle(final int month, final int date, final int year) {
		return String.format(TITLE, month, date, year);
	}
}