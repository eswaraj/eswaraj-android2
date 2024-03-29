package com.next.eswaraj.volley;


import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.next.eswaraj.base.BaseClass;
import com.next.eswaraj.config.Constants;
import com.next.eswaraj.datastore.Cache;
import com.next.eswaraj.events.GetCategoriesDataEvent;
import com.next.eswaraj.helpers.NetworkAccessHelper;
import com.eswaraj.web.dto.CategoryWithChildCategoryDto;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class LoadCategoriesDataRequest extends BaseClass {

    @Inject
    EventBus eventBus;
    @Inject
    Cache cache;
    @Inject
    NetworkAccessHelper networkAccessHelper;

    public void processRequest(Context context) {
        StringRequest request = new StringRequest(Constants.GET_CATEGORIES_URL, createSuccessListener(context), createErrorListener(context));
        this.networkAccessHelper.submitNetworkRequest("GetCategories", request);
    }

    private Response.ErrorListener createErrorListener(final Context context) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                GetCategoriesDataEvent getCategoriesDataEvent = new GetCategoriesDataEvent();
                getCategoriesDataEvent.setSuccess(false);
                getCategoriesDataEvent.setError(error.toString());
                eventBus.postSticky(getCategoriesDataEvent);
            }
        };
    }

    private Response.Listener<String> createSuccessListener(final Context context) {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                Gson gson = new Gson();
                try {
                    List<CategoryWithChildCategoryDto> categoryDtoList;
                    GetCategoriesDataEvent getCategoriesDataEvent = new GetCategoriesDataEvent();
                    categoryDtoList = gson.fromJson(json, new TypeToken<ArrayList<CategoryWithChildCategoryDto>>(){}.getType());
                    getCategoriesDataEvent.setSuccess(true);
                    getCategoriesDataEvent.setCategoryList(categoryDtoList);
                    eventBus.postSticky(getCategoriesDataEvent);
                    //Update the cache
                    cache.updateCategoriesData(context, json);
                } catch (JsonParseException e) {
                    GetCategoriesDataEvent getCategoriesDataEvent = new GetCategoriesDataEvent();
                    getCategoriesDataEvent.setSuccess(false);
                    getCategoriesDataEvent.setError("Invalid json");
                    eventBus.postSticky(getCategoriesDataEvent);
                }
            }
        };
    }
}
