package com.example.androidapp.coreimport com.google.gson.GsonBuilderimport okhttp3.OkHttpClientimport retrofit2.Retrofitimport retrofit2.converter.gson.GsonConverterFactoryobject Api {    private val url = "192.168.43.162:3000"//    private val url = "10.220.20.83:3000"//    private var url = "172.20.10.2:3000"    private val httpUrl = "http://$url/"    val wsUrl = "ws://$url/"    private var gson = GsonBuilder().create()    val retrofit = Retrofit.Builder()        .baseUrl(httpUrl)        .addConverterFactory(GsonConverterFactory.create(gson))        .build()    val okHttpClient = OkHttpClient.Builder()        .build()}