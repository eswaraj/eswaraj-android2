package com.next.eswaraj.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.next.eswaraj.R;
import com.next.eswaraj.adapters.FilterListAdapter;
import com.next.eswaraj.base.BaseFragmentActivity;
import com.next.eswaraj.models.ComplaintFilter;
import com.next.eswaraj.util.GlobalSessionUtil;
import com.eswaraj.web.dto.CategoryWithChildCategoryDto;

import java.util.ArrayList;

import javax.inject.Inject;

public class ComplaintFilterActivity extends BaseFragmentActivity {

    @Inject
    GlobalSessionUtil globalSession;

    private GridView categoryList;
    private GridView statusList;
    private TextView none;

    private ArrayList<ComplaintFilter> categoryFilterItems = new ArrayList<ComplaintFilter>();
    private ArrayList<ComplaintFilter> statusFilterItems = new ArrayList<ComplaintFilter>();

    private FilterListAdapter categoryAdapter;
    private FilterListAdapter statusAdapter;

    private ComplaintFilter selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_filter);

        selected = (ComplaintFilter) getIntent().getSerializableExtra("FILTER");

        categoryList = (GridView) findViewById(R.id.cfCategoryList);
        statusList = (GridView) findViewById(R.id.cfStatusList);
        none = (TextView) findViewById(R.id.cfNone);

        ComplaintFilter filter = new ComplaintFilter();
        filter.setComplaintFilterType(ComplaintFilter.ComplaintFilterType.STATUS);
        filter.setStatus("Pending");
        filter.setDisplayText("Open");
        if(selected != null && selected.getComplaintFilterType() == ComplaintFilter.ComplaintFilterType.STATUS && selected.getStatus().equals("Pending")) {
            filter.setHighlight(true);
        }
        else {
            filter.setHighlight(false);
        }
        statusFilterItems.add(filter);

        filter = new ComplaintFilter();
        filter.setComplaintFilterType(ComplaintFilter.ComplaintFilterType.STATUS);
        filter.setStatus("Done");
        filter.setDisplayText("Closed");
        if(selected != null && selected.getComplaintFilterType() == ComplaintFilter.ComplaintFilterType.STATUS && selected.getStatus().equals("Done")) {
            filter.setHighlight(true);
        }
        else {
            filter.setHighlight(false);
        }
        statusFilterItems.add(filter);

        for(CategoryWithChildCategoryDto categoryDto : globalSession.getCategoryDtoList()) {
            filter = new ComplaintFilter();
            filter.setComplaintFilterType(ComplaintFilter.ComplaintFilterType.CATEGORY);
            filter.setCategoryId(categoryDto.getId());
            filter.setDisplayText(categoryDto.getName());
            if(selected != null && selected.getComplaintFilterType() == ComplaintFilter.ComplaintFilterType.CATEGORY && selected.getCategoryId().equals(categoryDto.getId())) {
                filter.setHighlight(true);
            }
            else {
                filter.setHighlight(false);
            }
            categoryFilterItems.add(filter);
        }

        categoryAdapter = new FilterListAdapter(this, android.R.layout.simple_list_item_1, categoryFilterItems);
        statusAdapter = new FilterListAdapter(this, android.R.layout.simple_list_item_1, statusFilterItems);

        categoryList.setAdapter(categoryAdapter);
        statusList.setAdapter(statusAdapter);

        categoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent data = new Intent();
                data.putExtra("FILTER", categoryAdapter.getItem(position));
                if (getParent() == null) {
                    setResult(Activity.RESULT_OK, data);
                } else {
                    getParent().setResult(Activity.RESULT_OK, data);
                }
                finish();
            }
        });

        statusList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent data = new Intent();
                data.putExtra("FILTER", statusAdapter.getItem(position));
                if (getParent() == null) {
                    setResult(Activity.RESULT_OK, data);
                } else {
                    getParent().setResult(Activity.RESULT_OK, data);
                }
                finish();
            }
        });

        none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComplaintFilter complaintFilter = new ComplaintFilter();
                complaintFilter.setComplaintFilterType(ComplaintFilter.ComplaintFilterType.NONE);
                Intent data = new Intent();
                data.putExtra("FILTER", complaintFilter);
                if (getParent() == null) {
                    setResult(Activity.RESULT_OK, data);
                } else {
                    getParent().setResult(Activity.RESULT_OK, data);
                }
                finish();
            }
        });
    }
}
