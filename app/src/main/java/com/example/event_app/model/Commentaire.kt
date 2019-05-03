package com.example.event_app.model

class Commentaire {
    var auteur : String = ""
    var commentaire : String = ""
    var date : String = ""

    constructor(auteur: String, commentaire: String, date:String) {
        this.auteur = auteur
        this.commentaire = commentaire
        this.date=date
    }
    constructor() { }

    override fun toString(): String {
        return "Commentaire(auteur='$auteur', commentaire='$commentaire', date='$date')"
    }


}