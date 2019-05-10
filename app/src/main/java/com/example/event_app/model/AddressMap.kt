package com.example.event_app.model

class AddressMap {


    var id : String?= null
    var name : String?= null
    var address : String?= null
    var lat : Double?= null
    var lng : Double?= null

    constructor(id: String?, name: String?, address:String?, lat:Double?, lng:Double?) {
        this.id = id
        this.name = name
        this.address=address
        this.lat=lat
        this.lng=lng
    }
    constructor() { }

    override fun toString(): String {
        return "AddressMap(id='$id', name='$name', address='$address', lat='$lat',lng='$lng')"
    }
}
