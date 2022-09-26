package com.example.app.fakecarrotmarket.DataBase

data class GoguMarketDB(
    var productName: String? = null,
    val productImgUrl: String? = null,
    val userUid: String? = null,
    var userName: String? = null,
    val userAddress: String? = null,
    val userTemper: Double? = null,
    val productId: String? = null,
    val category: String? = null,
    val uploadTime: String? = null,
    val content: String? = null,
    val likeCount: Int? = null,
    val price: Int? = null,
    val key: String? = null
) {
    constructor() : this("", "", "", "", "", 0.0, "", "", "", "", 0, 0, "")
    constructor(
        productName: String?,
        userName: String?
    ) : this() {
        this.productName = productName
        this.userName = userName
    }
}



