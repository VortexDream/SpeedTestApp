package com.vortex.android.speedtestapp.firebase

import android.net.Uri

//Файлохранилище Firebase
interface BaseStorage {

    suspend fun uploadFile(uri: Uri, callback: () -> Unit = { })

}