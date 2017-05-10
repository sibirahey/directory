package org.jdc.template.model.webservice.individuals;

import org.jdc.template.model.webservice.individuals.dto.DtoIndividuals;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface IndividualService {
    String BASE_URL = "https://ldscdn.org";
    String SUB_URL = "/mobile/interview/directory";
    String FULL_URL = BASE_URL + SUB_URL;

    @GET(SUB_URL)
    Call<DtoIndividuals> individuals();

    @GET
    Call<DtoIndividuals> individualsByFullUrl(@Url String url);

    @Streaming
    @GET(SUB_URL)
    Call<ResponseBody> individualsToFile();
}
