package com.example.app.fakecarrotmarket

class ChatData {

    private var msg: String? = null
    private var nickname: String? = null

    fun getMsg(): String? {
        return msg
    }

    fun setMsg(msg: String) {
        this.msg = msg
    }

    fun getNickName(): String? {
        return nickname
    }

    fun setNickName(nickname: String) {
        this.nickname = nickname
    }

}