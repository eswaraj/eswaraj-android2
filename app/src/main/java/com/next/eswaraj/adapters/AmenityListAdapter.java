package com.next.eswaraj.adapters;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.next.eswaraj.R;
import com.next.eswaraj.models.ComplaintCounter;
import com.eswaraj.web.dto.CategoryWithChildCategoryDto;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class AmenityListAdapter extends ArrayAdapter<CategoryWithChildCategoryDto>{

    private Context context;
    private int layoutResourceId;
    private List<CategoryWithChildCategoryDto> categoryList;
    private List<ComplaintCounter> complaintCounterList;
    private Long totalComplaints;
    private Map<Long, Integer> colorMap;

    public AmenityListAdapter(Context context, int layoutResourceId, List<CategoryWithChildCategoryDto> categoryList, List<ComplaintCounter> complaintCounterList, Map<Long, Integer> colorMap) {
        super(context, layoutResourceId, categoryList);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.categoryList = categoryList;
        this.complaintCounterList = complaintCounterList;
        this.colorMap = colorMap;

        totalComplaints = 0L;
        if(complaintCounterList != null) {
            for (ComplaintCounter complaintCounter : complaintCounterList) {
                totalComplaints += complaintCounter.getCount();
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CategoryDtoHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new CategoryDtoHolder();
            holder.saIcon = (ImageView)row.findViewById(R.id.saIcon);
            holder.saTitle = (TextView)row.findViewById(R.id.saTitle);
            holder.saStats = (TextView)row.findViewById(R.id.saStats);

            //if(colorMap != null)
            //    holder.saIcon.setBorderColor(colorMap.get(categoryList.get(position).getId()));
            //else {
            //   holder.saIcon.setBorderWidth(0.0f);
            //}

            row.setTag(holder);
        }
        else
        {
            holder = (CategoryDtoHolder)row.getTag();
        }

        CategoryWithChildCategoryDto categoryDto = categoryList.get(position);
        holder.saTitle.setText(categoryDto.getName());
        holder.saIcon.setImageURI(Uri.parse(context.getFilesDir() + "/eSwaraj_" + String.valueOf(categoryDto.getId()) + ".png"));
        if(complaintCounterList != null) {
            for(ComplaintCounter complaintCounter : complaintCounterList) {
                if(complaintCounter.getId().equals(categoryDto.getId())) {
                    if(totalComplaints > 0) {
                        holder.saStats.setText(new DecimalFormat("#.##").format(complaintCounter.getCount().doubleValue() * 100 / totalComplaints) + "%");
                    }
                    else {
                        holder.saStats.setText(new DecimalFormat("#.##").format(0) + "%");
                    }
                    holder.saStats.setVisibility(View.VISIBLE);
                }
            }
        }

        return row;
    }

    static class CategoryDtoHolder
    {
        ImageView saIcon;
        TextView saTitle;
        TextView saStats;
    }
}
