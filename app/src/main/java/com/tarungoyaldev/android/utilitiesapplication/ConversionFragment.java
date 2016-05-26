package com.tarungoyaldev.android.utilitiesapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A placeholder fragment containing a simple view.
 */
public class ConversionFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SECTION_TITLE = "section_title";
    private DecimalFormat numberFormat = (DecimalFormat) NumberFormat.getNumberInstance();

    private double firstUnitConversionRatio;
    private double secondUnitConversionRatio;
    private String[] unitConversionRatioArray;
    private View rootView;
    private EditText firstEditText;
    private EditText secondEditText;
    private String title;

    public ConversionFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ConversionFragment newInstance(int sectionNumber, String title) {
        ConversionFragment fragment = new ConversionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_SECTION_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        numberFormat.setGroupingUsed(false);
        numberFormat.setMaximumFractionDigits(5);
        title = getArguments().getString(ARG_SECTION_TITLE);
        rootView = inflater.inflate(R.layout.fragment_unit_converter, container, false);
        final GridLayout gridLayout =
                (GridLayout) rootView.findViewById(R.id.conversion_gridLayout);
        gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        for (int i = 0; i < gridLayout.getChildCount(); i++) {
                            View view = gridLayout.getChildAt(i);
                            GridLayout.LayoutParams params = (GridLayout.LayoutParams) view.getLayoutParams();
                            params.width = (gridLayout.getWidth()/gridLayout.getColumnCount()) -params.rightMargin - params.leftMargin;
                            params.height = (gridLayout.getHeight()/gridLayout.getRowCount()) -params.topMargin - params.bottomMargin;
                            view.setLayoutParams(params);
                        }
                    }
                });
        Spinner firstUnitSpinner = (Spinner) rootView.findViewById(R.id.firstUnit_spinner);
        Spinner secondUnitSpinner = (Spinner) rootView.findViewById(R.id.secondUnit_spinner);
        switch (title) {
            case "Distance":
                unitConversionRatioArray = getResources().getStringArray(R.array.distances_values);
                break;
            case "Mass":
                unitConversionRatioArray = getResources().getStringArray(R.array.mass_vales);
                break;
        }
        firstUnitConversionRatio = Double.valueOf(unitConversionRatioArray[0]);
        secondUnitConversionRatio = Double.valueOf(unitConversionRatioArray[0]);
        initializeSpinner(firstUnitSpinner);
        initializeSpinner(secondUnitSpinner);

        firstEditText = (EditText) rootView.findViewById(R.id.firstValue_editText);
        secondEditText = (EditText) rootView.findViewById(R.id.secondValue_editText);
        firstEditText.setTag(false);
        secondEditText.setTag(false);
        final Boolean isTextChanged = true;
        firstEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double firstValue = Double.valueOf(s.toString().replaceAll(",", ""));
                    if (firstEditText.getTag().equals(false)) {
                        secondEditText.setTag(true);
                        secondEditText.setText(numberFormat.format(
                                firstValue * secondUnitConversionRatio / firstUnitConversionRatio));
                        secondEditText.setTag(false);
                    }
                } catch (NumberFormatException e) {
                    Log.e(getActivity().getLocalClassName(), "String not formatted correctly!!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        secondEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double secondValue = Double.valueOf(s.toString());
                    if (secondEditText.getTag().equals(false)) {
                        firstEditText.setTag(true);
                        firstEditText.setText(numberFormat.format(
                                secondValue * firstUnitConversionRatio / secondUnitConversionRatio));
                        firstEditText.setTag(false);
                    }
                } catch (NumberFormatException e) {
                    Log.e(getActivity().getLocalClassName(), "String not formatted correctly!!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return rootView;
    }

    private void initializeSpinner(Spinner spinner) {
        ArrayAdapter<CharSequence> adapter;
        switch (title) {
            case "Distance":
                adapter = ArrayAdapter.createFromResource(getContext(), R.array.distances_names,
                        android.R.layout.simple_spinner_item);
                break;
            case "Mass":
                adapter = ArrayAdapter.createFromResource(getContext(), R.array.mass_names,
                        android.R.layout.simple_spinner_item);
                break;
            default:
                Log.i(this.getClass().getCanonicalName(), "Invalid state!!!");
                return;
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int spinnerId = parent.getId();
        double firstValue;
        try {
            firstValue = Double.valueOf(firstEditText.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid number for conversion!!!",
                    Toast.LENGTH_LONG);
            firstValue = 0;
        }
        double newSpinnerConversionRatio = Double.valueOf(unitConversionRatioArray[position]);
        switch (spinnerId) {
            case R.id.firstUnit_spinner:
                firstUnitConversionRatio = newSpinnerConversionRatio;
                break;
            case R.id.secondUnit_spinner:
                secondUnitConversionRatio = newSpinnerConversionRatio;
                break;
        }
        secondEditText.setTag(true);
        secondEditText.setText(
                numberFormat.format(firstValue * secondUnitConversionRatio / firstUnitConversionRatio));
        secondEditText.setTag(false);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

