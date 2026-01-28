package com.inventorymanager.app.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.inventorymanager.app.R;
import com.inventorymanager.app.database.ItemEntity;
import com.inventorymanager.app.viewmodel.ItemViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {
    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;
    private ItemViewModel viewModel;

    private static final int[] CHART_COLORS = {
        Color.parseColor("#2563EB"),
        Color.parseColor("#0891B2"),
        Color.parseColor("#10B981"),
        Color.parseColor("#F59E0B"),
        Color.parseColor("#EC4899"),
        Color.parseColor("#8B5CF6"),
        Color.parseColor("#EF4444"),
        Color.parseColor("#64748B")
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        lineChart = view.findViewById(R.id.lineChart);

        viewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
        viewModel.getAllItems().observe(getViewLifecycleOwner(), this::updateCharts);
    }

    private void updateCharts(List<ItemEntity> items) {
        if (items == null) return;
        if (items.isEmpty()) {
            clearCharts();
            return;
        }
        setupPieChart(items);
        setupBarChart(items);
        setupLineChart(items);
    }

    private void clearCharts() {
        pieChart.clear();
        pieChart.invalidate();
        barChart.clear();
        barChart.invalidate();
        lineChart.clear();
        lineChart.invalidate();
    }

    private void setupPieChart(List<ItemEntity> items) {
        Map<String, Integer> categoryCount = new HashMap<>();
        for (ItemEntity item : items) {
            String cat = item.getCategory();
            categoryCount.put(cat, categoryCount.getOrDefault(cat, 0) + item.getQuantity());
        }
        if (categoryCount.isEmpty()) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> e : categoryCount.entrySet()) {
            entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));
            i++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(CHART_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(true);
        pieChart.setUsePercentValues(false);
        pieChart.animateY(300);
        pieChart.invalidate();
    }

    private void setupBarChart(List<ItemEntity> items) {
        Map<String, Float> categoryValue = new HashMap<>();
        for (ItemEntity item : items) {
            String cat = item.getCategory();
            double val = (item.getPrice() != null ? item.getPrice() : 0) * item.getQuantity();
            categoryValue.put(cat, categoryValue.getOrDefault(cat, 0f) + (float) val);
        }
        if (categoryValue.isEmpty()) return;

        final List<String> labels = new ArrayList<>(categoryValue.keySet());
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            entries.add(new BarEntry(i, categoryValue.get(labels.get(i))));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Value ($)");
        dataSet.setColors(CHART_COLORS);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value > 0 ? String.format(Locale.getDefault(), "$%.0f", value) : "";
            }
        });

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(300);
        barChart.invalidate();
    }

    private void setupLineChart(List<ItemEntity> items) {
        Map<String, Integer> monthCount = new HashMap<>();
        Map<String, String> monthLabels = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (ItemEntity item : items) {
            if (item.getPurchaseDate() == null) continue;
            cal.setTime(item.getPurchaseDate());
            String key = String.format(Locale.getDefault(), "%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
            monthCount.put(key, monthCount.getOrDefault(key, 0) + 1);
            if (!monthLabels.containsKey(key)) {
                monthLabels.put(key, monthNames[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR));
            }
        }

        List<String> sortedMonths = new ArrayList<>(monthCount.keySet());
        java.util.Collections.sort(sortedMonths);
        List<String> labels = new ArrayList<>();
        for (String k : sortedMonths) {
            labels.add(monthLabels.getOrDefault(k, k));
        }
        if (sortedMonths.isEmpty()) return;

        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < sortedMonths.size(); i++) {
            entries.add(new Entry(i, monthCount.get(sortedMonths.get(i))));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Items");
        dataSet.setColor(CHART_COLORS[0]);
        dataSet.setCircleColor(CHART_COLORS[0]);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });

        LineData data = new LineData(dataSet);
        lineChart.setData(data);
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.animateX(300);
        lineChart.invalidate();
    }
}
