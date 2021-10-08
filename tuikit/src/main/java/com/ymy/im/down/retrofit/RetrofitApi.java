package com.ymy.im.down.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by taochen on 18-6-21.
 */

public interface RetrofitApi {

    /**
     * 下载文件
     *
     * @param fileUrl
     * @return
     */
    // 大文件时要加不然会OOM
    @Streaming
    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);
}
