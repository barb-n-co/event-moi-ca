package com.example.event_app.model

class Event {
        var idEvent: String = ""
        var idOrganizer: String? = null
        var organizer: String = ""
        var name: String = ""
        var description: String? = null
        var dateStart: String = ""
        var dateEnd: String = ""

    constructor() {}

        constructor(idEvent: String, idOrganizer: String?, organizer: String, name: String, description: String?, dateStart: String, dateEnd: String) {
                this.idEvent = idEvent
                this.idOrganizer = idOrganizer
                this.organizer = organizer
                this.name = name
                this.description = description
                this.dateStart = dateStart
                this.dateEnd = dateEnd
        }
}