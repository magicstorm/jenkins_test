package com.hgxx.whiteboard.network;

import com.hgxx.whiteboard.network.constants.Web;


import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by ly on 04/05/2017.
 */

public class WebClient {
    private static WebClient webClient;
    private Retrofit webRetrofit;


    private WebClient(){
        getRestrofit();
    }

    public static synchronized WebClient getInstance(){
        if(webClient==null){
            webClient = new WebClient();
        }
        return webClient;
    }

    public interface PresentationService{
        @GET("{presentation_folder}/{presentation_name}")
        Observable<ResponseBody> getPresentationImage(
                @Path("presentation_folder") String folderName,
                @Path("presentation_name") String presentationName
        );
    }

    private <T> T getService(Class<T> serviceClass){
        return webRetrofit.create(serviceClass);
    }

    public PresentationService getPresentationService(){
        return getService(PresentationService.class);
    }

    private void getRestrofit(){
        webRetrofit = new Retrofit.Builder()
                .baseUrl(Web.protocol+"://"+Web.address+":"+String.valueOf(Web.port)+"/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

}
