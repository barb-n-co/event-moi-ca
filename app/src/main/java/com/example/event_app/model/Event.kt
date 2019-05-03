package com.example.event_app.model

class Event {
        var idEvent: String = ""
        var organizer: String = ""
        var name: String = ""
        var description: String? = null
        var dateStart: String = ""
        var dateEnd: String = ""

    constructor() { }

        constructor(idEvent: String, organizer: String, name: String, description: String?, dateStart: String, dateEnd: String) {
                this.idEvent = idEvent
                this.organizer = organizer
                this.name = name
                this.description = description
                this.dateStart = dateStart
                this.dateEnd = dateEnd
        }
}