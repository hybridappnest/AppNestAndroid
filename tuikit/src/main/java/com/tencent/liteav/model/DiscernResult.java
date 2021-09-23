package com.tencent.liteav.model;

import com.tencent.liteav.login.UserModel;

import java.io.Serializable;
import java.util.List;

public class DiscernResult implements Serializable {
    Response Response;

    public Response getResponse() {
        return Response;
    }

    public void setResponse(Response response) {
        this.Response = response;
    }

    public class Response{
        String Result;
        String AudioDuration;
        String RequestId;

        public String getResult() {
            return Result;
        }

        public void setResult(String result) {
            Result = result;
        }

        public String getAudioDuration() {
            return AudioDuration;
        }

        public void setAudioDuration(String audioDuration) {
            AudioDuration = audioDuration;
        }

        public String getRequestId() {
            return RequestId;
        }

        public void setRequestId(String requestId) {
            RequestId = requestId;
        }
    }

}