package com.csl.ams.WebService;

import com.csl.ams.Entity.PhotoAPIResponse;
import com.csl.ams.Request.DisposalAssetsRequest;
import com.csl.ams.Request.ReturnBorrowedAssetRequest;
import com.csl.ams.Request.UpdateWaitingListRequest;
import com.csl.ams.Request.UploadStockTakeRequest;
import com.csl.ams.Response.APIResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface APIService {
    //@Headers("Content-Type: application/json")
    @GET("updateAssetEpc.php")
    Call<APIResponse> modifyAsset(@Query("id") String id, @Query("epc") String epc);

    @POST("borrowAssets.php")
    Call<APIResponse> borrowAsset(@Body UpdateWaitingListRequest body);

    @POST("stockTakeAssets.php")
    Call<APIResponse> stockTakeAssets(@Body UploadStockTakeRequest body);

    @POST("returnBorrowedAssets.php")
    Call<APIResponse> returnAsset(@Body ReturnBorrowedAssetRequest body);

    @GET("disposalAsset.php")
    Call<APIResponse> disposalAsset(@Query("id") String id);

    @POST("disposalAssets.php")
    Call<APIResponse> disposalAssets(@Body DisposalAssetsRequest body);

    @Multipart
    @POST("uploadimage.php")
    Call<PhotoAPIResponse> uploadImage(@Part MultipartBody.Part image);
}
