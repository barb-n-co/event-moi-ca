package com.example.event_app.model

class Commentaire {
    var auteur : String = ""
    var commentaire : String = ""

    constructor(auteur: String, commentaire: String) {
        this.auteur = auteur
        this.commentaire = commentaire
    }
}