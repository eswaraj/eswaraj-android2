package com.next.eswaraj.volley;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.next.eswaraj.base.BaseClass;
import com.next.eswaraj.config.Constants;
import com.next.eswaraj.events.SavedCommentEvent;
import com.next.eswaraj.helpers.NetworkAccessHelper;
import com.next.eswaraj.models.CommentDto;
import com.next.eswaraj.models.ComplaintDto;
import com.eswaraj.web.dto.UserDto;
import com.eswaraj.web.dto.comment.CommentSaveRequestDto;
import com.google.gson.Gson;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;


public class CommentPostRequest extends BaseClass {

    @Inject
    EventBus eventBus;
    @Inject
    NetworkAccessHelper networkAccessHelper;

    public void processRequest(UserDto userDto, ComplaintDto complaintDto, String comment)
    {
        CommentSaveRequestDto commentSaveRequestDto = new CommentSaveRequestDto();
        commentSaveRequestDto.setCommentText(comment);
        commentSaveRequestDto.setComplaintId(complaintDto.getId());
        commentSaveRequestDto.setPersonId(userDto.getPerson().getId());
        commentSaveRequestDto.setPoliticalAdminId(null);

        GenericPostVolleyRequest request = new GenericPostVolleyRequest(Constants.COMMENT_POST_URL, createErrorListener(), createSuccessListener(userDto), commentSaveRequestDto);
        networkAccessHelper.submitNetworkRequest("PostComment", request);
    }

    private Response.Listener<String> createSuccessListener(final UserDto userDto) {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                CommentDto commentDto = new Gson().fromJson(response, CommentDto.class);
                commentDto.createNewCommenter();
                if(userDto.getPerson() != null) {
                    commentDto.getPostedBy().setExternalId(userDto.getPerson().getExternalId());
                    commentDto.getPostedBy().setGender(userDto.getPerson().getGender());
                    commentDto.getPostedBy().setName(userDto.getPerson().getName());
                    commentDto.getPostedBy().setProfilePhoto(userDto.getPerson().getProfilePhoto());
                }

                SavedCommentEvent event = new SavedCommentEvent();
                event.setSuccess(true);
                event.setCommentDto(commentDto);
                eventBus.postSticky(event);
            }
        };
    }


    private Response.ErrorListener createErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SavedCommentEvent event = new SavedCommentEvent();
                event.setSuccess(false);
                event.setError(error.toString());
                eventBus.postSticky(event);
            }
        };
    }
}
