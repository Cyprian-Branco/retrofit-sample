package com.atahani.retrofit_sample.utility;

import com.atahani.retrofit_sample.models.ErrorModel;
import com.atahani.retrofit_sample.network.FakeTwitterProvider;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * this class help to parse the response to ErrorModel when using from Retrofit
 */
public class ErrorUtils {

    public static ErrorModel parseError(Response<?> response) {
        FakeTwitterProvider fakeTwitterProvider = new FakeTwitterProvider();

        //using Converter for convert error response body into Error Model
        Converter<ResponseBody, ErrorModel> converter = fakeTwitterProvider.getRetrofitClient().responseBodyConverter(ErrorModel.class, new Annotation[0]);

        ErrorModel errorModel;

        try {
            errorModel = converter.convert(response.errorBody());

        } catch (IOException e) {
            return new ErrorModel();
        }

        return errorModel;
    }
}
