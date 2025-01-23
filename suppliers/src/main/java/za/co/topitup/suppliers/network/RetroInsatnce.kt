package za.co.topitup.suppliers.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import za.co.topitup.suppliers.models.Retailer

class RetroInsatnce {
    companion object{
        fun getRetroInstance():Retrofit{

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder().addInterceptor(logging)
                .build()
            val gson = GsonBuilder()
                .setLenient()

                .create()



            return Retrofit.Builder()
                .baseUrl(Retailer.endpointBaseURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)

                .build()
        }
    }
}