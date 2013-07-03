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

public class MonthBarGraph {
	private static final String TITLE = "Water Drank In %s %d";
	private static final String X_AXIS_TITLE = "Day Of Month";
	private static final String Y_AXIS_TITLE = "Amount Drank (Ounces)";
	
	public Intent getIntent(Context c, final int month, final int year) {
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
		
		cal = Calendar.getInstance();
		cal.set(year, month, 0, 0, 0, 0);
		start = cal.getTime();
		
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DATE, -1);
		end = cal.getTime();
		
		dbHandler = new DatabaseHandler(c);
		data = dbHandler.getGulps(start, end);
		
		series = new CategorySeries("Data");
		
		days = new double[cal.getActualMaximum(Calendar.DAY_OF_MONTH)];
		Arrays.fill(days, 0);
		
		for (Iterator<Gulp> iter = data.iterator(); iter.hasNext();) {
			gulp = iter.next();
			cal.setTime(gulp.getDate());
			days[cal.get(Calendar.DATE)] += gulp.getAmount();
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
		
		for (int i = 1; i <= cal.getActualMaximum(Calendar.DAY_OF_MONTH); i += 7) {
			datasetRenderer.addXTextLabel(i, Integer.toString(i));
		}
		
		datasetRenderer.addXTextLabel(cal.getActualMaximum(Calendar.DAY_OF_MONTH), String.valueOf(cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
		
		datasetRenderer.setChartTitle(getTitle(month, year));
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
		datasetRenderer.setXAxisMax(cal.getActualMaximum(Calendar.DAY_OF_MONTH) + 1);
		
		seriesRenderer.setGradientEnabled(true);
		seriesRenderer.setGradientStart(0.0, Color.BLUE);
		seriesRenderer.setGradientStop(max, Color.CYAN);
		seriesRenderer.setDisplayChartValues(true);
		seriesRenderer.setChartValuesTextAlign(Align.CENTER);
		
		datasetRenderer.addSeriesRenderer(seriesRenderer);
		
		return ChartFactory.getBarChartIntent(c, dataset, datasetRenderer, Type.DEFAULT);
	}
	
	private static String getTitle(final int month, final int year) {
		String monthName;
		
		monthName = (new DateFormatSymbols()).getMonths()[month];
		
		return String.format(TITLE, monthName, year);
	}
}