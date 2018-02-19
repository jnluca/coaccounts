// ------------------------------------------------------------------------------
// Copyright (c) 2015 Microsoft Corporation
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
// ------------------------------------------------------------------------------

package com.microsoft.onedrive.apiexplorer;

import com.onedrive.sdk.concurrency.ICallback;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Calendar;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    /**
     * Default constructor
     */
    RadioGroup rg;
    RadioButton rb;
    private EditText label;
    private EditText amount;
    private EditText comment;
    private TextView date;
    private DatePickerDialog.OnDateSetListener mDateSetListerner;
    private String[] fields = new String[7];
    private String jl_vl;
    public PlaceholderFragment() {
    }

    /**
     * Handle creation of the view
     * @param inflater the layout inflater
     * @param container the hosting containing for this fragment
     * @param savedInstanceState saved state information
     * @return The constructed view
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_api_explorer, container, false);

        rg = (RadioGroup) view.findViewById(R.id.rgroup);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            public void onCheckedChanged(RadioGroup radioGroup, int checkedId ){
                rb = (RadioButton)view.findViewById(checkedId);
                jl_vl = (String) rb.getText();
            }
        });
        final Button button = (Button) view.findViewById(R.id.query_vroom);
        label = (EditText) view.findViewById(R.id.label);
        amount = (EditText) view.findViewById(R.id.amount);
        date = (TextView) view.findViewById(R.id.date);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Calendar now = Calendar.getInstance();
                int year = now.get(Calendar.YEAR);
                int month = now.get(Calendar.MONTH); // Note: zero based!
                int day = now.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        getActivity().getBaseContext(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListerner,
                        year, month, day);
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.getWindow().setBackgroundDrawable((new ColorDrawable((Color.TRANSPARENT))));
                dialog.show();
            }
        });
        mDateSetListerner = new DatePickerDialog.OnDateSetListener () {
            public void onDateSet (DatePicker datePicker, int year, int month, int day){
                month = month + 1;
                date.setText(day + "/" + month + "/" + year);
                fields[0] = String.valueOf(day);
                fields[1] = String.valueOf(month);
                fields[2] = String.valueOf(year);
            }
        };

        comment = (EditText) view.findViewById(R.id.comment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                button.setEnabled(false);
                final BaseApplication app = (BaseApplication)getActivity().getApplication();
                final ICallback<Void> serviceCreated = new DefaultCallback<Void>(getActivity()) {
                    @Override
                    public void success(final Void result) {
                        navigateToRoot();
                        button.setEnabled(true);
                    }
                };
                try {
                    app.getOneDriveClient();
                    navigateToRoot();
                    button.setEnabled(true);
                } catch (final UnsupportedOperationException ignored) {
                    app.createOneDriveClient(getActivity(), serviceCreated);
                }
            }
        });

        return view;
    }

    /**
     * Navigate to the root object in the onedrive
     */
    private void navigateToRoot() {
        fields[3] = label.getText().toString();
        fields[6] = comment.getText().toString();
        fields[4] = "";
        fields[5] = "";
        if(jl_vl.equals("jl")){
            fields[5] = amount.getText().toString();
        }
        if(jl_vl.equals("vl")){
            fields[4] = amount.getText().toString();
        }
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, ItemFragment.newInstance("root:/", fields))
                .addToBackStack(null)
                        .commit();
    }
}
