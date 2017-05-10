package org.jdc.template.ui.fragment;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.widget.DatePicker;

import org.jdc.template.inject.Injector;
import org.jdc.template.ux.individualedit.IndividualEditPresenter;
import org.threeten.bp.LocalDate;

import javax.inject.Inject;

/**
 * Created by jp945g on 5/7/2017.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Inject
    IndividualEditPresenter presenter;

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Injector.get().inject(this);

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        LocalDate selectedDate = LocalDate.of(year, month, day);
        
        presenter.birthDateSelected(selectedDate);
    }
}