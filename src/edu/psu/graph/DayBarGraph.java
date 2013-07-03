package edu.psu.graph;

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

public class DayBarGraph {
	private static final String TITLE = "Water Drank On %d/%d/%d";
	private static final String X_AXIS_TITLE = "Hour";
	private static final String Y_AXIS_TITLE = "Amount Drank (Ounces)";
	
	private static final int HOURS_PER_DAY = 24;
	
	public Intent getIntent(Context c, final int month, final int date, final int year) {
		CategorySeries series;
		XYMultipleSeriesDataset dataset;
		XYMultipleSeriesRenderer datasetRenderer;
		SimpleSeriesRenderer seriesRenderer;
		DatabaseHandler dbHandler;
		List<Gulp> data;
		Calendar cal;
		Date start, end;
		double max;
		double[] hours;
		Gulp gulp;
		DecimalFormat format;
		
		cal = Calendar.getInstance();
		cal.set(year, month, date, 0, 0, 0);
		start = cal.getTime();
		
		cal.add(Calendar.DATE, 1);
		end = cal.getTime();
		
		dbHandler = new DatabaseHandler(c);
		data = dbHandler.getGulps(start, end);
		
		series = new CategorySeries("Data");
		
		hours = new double[HOURS_PER_DAY];
		Arrays.fill(hours, 0);
		
		for (Iterator<Gulp> iter = data.iterator(); iter.hasNext();) {
			gulp = iter.next();
			cal.setTime(gulp.getDate());
			hours[cal.get(Calendar.HOUR_OF_DAY)] += gulp.getAmount();
		}
		
		format = new DecimalFormat("#.#");
		
		max = 0;
		for (double hour : hours) {
			series.add(Double.parseDouble(format.format(hour)));
			
			if (hour > max) {
				max = hour;
			}
		}
		
		dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series.toXYSeries());
		
		datasetRenderer = new XYMultipleSeriesRenderer();
		seriesRenderer = new SimpleSeriesRenderer();
		
		for (int i = 1; i <= HOURS_PER_DAY + 1; i += 2) {
			if (i == 1) {
				datasetRenderer.addXTextLabel(i, "12 am");
			} else if (i < 13) {
				datasetRenderer.addXTextLabel(i, (i - 1) + " am");
			} else if (i == 13) {
				datasetRenderer.addXTextLabel(i, "12 pm");
			} else {
				datasetRenderer.addXTextLabel(i, (i - 13) + " pm");
			}
		}
		
		datasetRenderer.setChartTitle(getTitle(month, date, year));
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
		datasetRenderer.setXAxisMax(25);
		
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