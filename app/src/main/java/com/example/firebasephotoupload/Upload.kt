package com.example.firebasephotoupload

class Upload {
    var mName: String? = null
    var mImageUrl: String? = null

    constructor() {
        // Empty constructor required for Firebase
    }

    constructor(name: String?, imageUrl: String?) {
        if (name.isNullOrBlank()) {
            mName = "No Name"
        } else {
            mName = name
        }
        mImageUrl = imageUrl
    }

    fun getName(): String? {
        return mName
    }

    fun setName(name: String?) {
        mName = name
    }
}
