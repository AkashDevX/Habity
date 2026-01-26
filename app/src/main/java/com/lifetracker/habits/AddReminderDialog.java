package com.lifetracker.habits;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.chip.Chip;

public class AddReminderDialog extends AppCompatDialogFragment {
    private TimePicker timePicker;
    private Chip chipSunday, chipMonday, chipTuesday, chipWednesday, 
                 chipThursday, chipFriday, chipSaturday;
    private OnReminderSavedListener listener;
    
    // Days mask: Sun=1, Mon=2, Tue=4, Wed=8, Thu=16, Fri=32, Sat=64
    private static final int MASK_SUNDAY = 1;
    private static final int MASK_MONDAY = 2;
    private static final int MASK_TUESDAY = 4;
    private static final int MASK_WEDNESDAY = 8;
    private static final int MASK_THURSDAY = 16;
    private static final int MASK_FRIDAY = 32;
    private static final int MASK_SATURDAY = 64;
    
    public interface OnReminderSavedListener {
        void onSaved(int hour, int minute, int daysMask);
    }
    
    private int initialHour = -1;
    private int initialMinute = -1;
    private int initialDaysMask = 127; // All days by default
    
    public static AddReminderDialog newInstance(OnReminderSavedListener listener) {
        AddReminderDialog dialog = new AddReminderDialog();
        dialog.listener = listener;
        return dialog;
    }
    
    public static AddReminderDialog newInstanceForEdit(int hour, int minute, int daysMask, OnReminderSavedListener listener) {
        AddReminderDialog dialog = new AddReminderDialog();
        dialog.listener = listener;
        dialog.initialHour = hour;
        dialog.initialMinute = minute;
        dialog.initialDaysMask = daysMask;
        return dialog;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        
        View view = requireActivity().getLayoutInflater()
            .inflate(R.layout.dialog_add_reminder, null);
        
        timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        
        // Set initial time if editing
        if (initialHour >= 0 && initialMinute >= 0) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                timePicker.setHour(initialHour);
                timePicker.setMinute(initialMinute);
            } else {
                timePicker.setCurrentHour(initialHour);
                timePicker.setCurrentMinute(initialMinute);
            }
        }
        
        chipSunday = view.findViewById(R.id.chipSunday);
        chipMonday = view.findViewById(R.id.chipMonday);
        chipTuesday = view.findViewById(R.id.chipTuesday);
        chipWednesday = view.findViewById(R.id.chipWednesday);
        chipThursday = view.findViewById(R.id.chipThursday);
        chipFriday = view.findViewById(R.id.chipFriday);
        chipSaturday = view.findViewById(R.id.chipSaturday);
        
        // Set chips based on initial days mask or default to all days
        chipSunday.setChecked((initialDaysMask & MASK_SUNDAY) != 0);
        chipMonday.setChecked((initialDaysMask & MASK_MONDAY) != 0);
        chipTuesday.setChecked((initialDaysMask & MASK_TUESDAY) != 0);
        chipWednesday.setChecked((initialDaysMask & MASK_WEDNESDAY) != 0);
        chipThursday.setChecked((initialDaysMask & MASK_THURSDAY) != 0);
        chipFriday.setChecked((initialDaysMask & MASK_FRIDAY) != 0);
        chipSaturday.setChecked((initialDaysMask & MASK_SATURDAY) != 0);
        
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        
        buttonCancel.setOnClickListener(v -> dismiss());
        buttonSave.setOnClickListener(v -> {
            int hour, minute;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
            } else {
                hour = timePicker.getCurrentHour();
                minute = timePicker.getCurrentMinute();
            }
            
            int daysMask = buildDaysMask();
            
            if (listener != null) {
                listener.onSaved(hour, minute, daysMask);
            }
            
            dismiss();
        });
        
        builder.setView(view);
        return builder.create();
    }
    
    private int buildDaysMask() {
        int mask = 0;
        if (chipSunday.isChecked()) mask |= MASK_SUNDAY;
        if (chipMonday.isChecked()) mask |= MASK_MONDAY;
        if (chipTuesday.isChecked()) mask |= MASK_TUESDAY;
        if (chipWednesday.isChecked()) mask |= MASK_WEDNESDAY;
        if (chipThursday.isChecked()) mask |= MASK_THURSDAY;
        if (chipFriday.isChecked()) mask |= MASK_FRIDAY;
        if (chipSaturday.isChecked()) mask |= MASK_SATURDAY;
        
        return mask;
    }
}

