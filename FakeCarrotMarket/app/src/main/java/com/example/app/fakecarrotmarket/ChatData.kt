package com.example.app.fakecarrotmarket

import java.io.Serializable

public class ChatData : Serializable {

    private var msg: String? = null
    private var nickname: String? = null

    public fun getMsg(): String? {
        return msg
    }

    public fun setMsg(msg: String): String? {
        this.msg = msg
        return msg
    }

    public fun getNickName(): String? {
        return nickname
    }

    public fun setNickName(nickname: String): String? {
        this.nickname = nickname
        return nickname
    }

}