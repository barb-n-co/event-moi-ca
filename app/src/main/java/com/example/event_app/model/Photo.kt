package com.example.event_app.model


class Photo  {

    var id : String? = null
    var url : String? = null
    var auteur : String? = null
    var commentaires: MutableList<Commentaire> = ArrayList()
    var like : Int? = null

    constructor(id: String, auteur: String, like: Int, url: String, commentaires: MutableList<Commentaire>) {
        this.id = id
        this.auteur = auteur
        this.commentaires = commentaires
        this.like = like
        this.url = url
    }

    constructor(url: String, id: String) {
        this.url = url
        this.id = id
    }

    constructor() {}

    override fun toString(): String {
        return "Photo(id= $id, url= $url, auteur= $auteur, like= $like, commentaires=$commentaires)"
    }


}