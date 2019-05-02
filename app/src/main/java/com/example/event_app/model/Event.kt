package com.example.event_app.model

class Event {
        var idEvent: Int = 0
        var name: String = ""
        var description: String? = null
        var dateStart: String = ""
        var dateEnd: String = ""

        constructor() { }

        constructor(idEvent: Int, name: String, description: String?, dateStart: String, dateEnd: String) {
                this.idEvent = idEvent
                this.name = name
                this.description = description
                this.dateStart = dateStart
                this.dateEnd = dateEnd
        }
}