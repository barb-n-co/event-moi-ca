package com.example.event_app.model

class PhotoList {
    var list: MutableList<Photo>? = null

    constructor(list: MutableList<Photo>) {
        this.list = list
    }

    constructor() {}

    override fun toString(): String {
        return "PhotoList(list=$list)"
    }


}