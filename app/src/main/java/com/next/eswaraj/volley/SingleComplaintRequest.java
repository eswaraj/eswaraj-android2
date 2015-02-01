package com.next.eswaraj.volley;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.next.eswaraj.base.BaseClass;
import com.next.eswaraj.config.Constants;
import com.next.eswaraj.datastore.Cache;
import com.next.eswaraj.events.GetSingleComplaintEvent;
import com.next.eswaraj.helpers.NetworkAccessHelper;
import com.next.eswaraj.models.ComplaintDto;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;


public class SingleComplaintRequest extends BaseClass {

    @Inject
    EventBus eventBus;
    @Inject
    Cache cache;
    @Inject
    NetworkAccessHelper networkAccessHelper;

    public void processRequest(Context context, Long id) {
        StringRequest request = new StringRequest(Constants.GET_SINGLE_COMPLAINT_URL + "/" + id, createSuccessListener(context), createErrorListener(context));
        networkAccessHelper.submitNetworkRequest("GetSingleComplaint", request);
    }

    private Response.ErrorListener createErrorListener(final Context context) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                GetSingleComplaintEvent event = new GetSingleComplaintEvent();
                event.setError(error.toString());
                eventBus.postSticky(event);
            }
        };
    }

    private Response.Listener<String> createSuccessListener(final Context context) {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                Gson gson = new Gson();
                try {
                    ComplaintDto complaintDto;
                    GetSingleComplaintEvent event = new GetSingleComplaintEvent();
                    complaintDto = gson.fromJson(json, ComplaintDto.class);
                    event.setSuccess(true);
                    event.setComplaintDto(complaintDto);
                    eventBus.postSticky(event);
                    //Update the cache
                    cache.updateUserComplaints(context, json);
                } catch (JsonParseException e) {
                    GetSingleComplaintEvent event = new GetSingleComplaintEvent();
                    event.setError("Invalid json");
                    eventBus.postSticky(event);
                }
            }
        };
    }
}
