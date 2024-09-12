package com.vortex.android.speedtestapp.firebase


import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class OnlineStorage @Inject constructor(
    storage: FirebaseStorage,
) : BaseStorage {

    override suspend fun uploadFile(uri: Uri, callback: () -> Unit) {

    }
}