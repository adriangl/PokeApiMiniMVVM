package com.adriangl.pokeapi_mvvm.di

import android.content.Context
import com.adriangl.pokeapi_mvvm.moves.MovesApi
import com.adriangl.pokeapi_mvvm.network.PokeApi
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = Kodein.Module("network") {
    val cacheSize = 300L * 1024 * 1024 // 300 MB

    bind<OkHttpClient>() with singleton {
        val context: Context = instance()

        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .cache(Cache(context.cacheDir, cacheSize))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    bind<Retrofit>() with singleton {
        val endpoint = "https://pokeapi.co"

        Retrofit.Builder()
            .baseUrl(HttpUrl.parse(endpoint)!!)
            .client(instance())
            .addConverterFactory(MoshiConverterFactory.create(instance()))
            .build()
    }

    bind<PokeApi>() with singleton {
        val retrofit: Retrofit = instance()
        retrofit.create(PokeApi::class.java)
    }

    bind<MovesApi>() with singleton {
        val retrofit: Retrofit = instance()
        retrofit.create(MovesApi::class.java)
    }
}