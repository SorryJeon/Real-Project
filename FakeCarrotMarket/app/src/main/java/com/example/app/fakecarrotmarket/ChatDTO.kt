package com.example.app.fakecarrotmarket

class ChatDTO {
    var userName: String? = null
    var message: String? = null

    constructor() {}
    constructor(userName: String?, message: String?) {
        this.userName = userName
        this.message = message
    }
}