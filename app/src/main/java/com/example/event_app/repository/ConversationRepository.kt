package com.example.event_app.repository


/*
class ConversationRepository(private val service: TESService) {

    fun fetchConversation(idUser: Int): Flowable<List<ConversationResponse>> {
        return service.getConversations(idUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .share()
    }

    fun createChatWithUser(idUser: Int, idOtherUser: Int): Observable<NetworkEvent> {
        val conversationData = ConversationData(idUser, idOtherUser)

        return service.createChat(conversationData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map<NetworkEvent> { NetworkEvent.Success }
                .onErrorReturn { NetworkEvent.Error(it) }
                .startWith(NetworkEvent.InProgress)
                .share()
    }

    fun deleteConversation(idConversation: Int): Observable<NetworkEvent> {
        return service.deleteChat(idConversation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map<NetworkEvent> { NetworkEvent.Success }
                .onErrorReturn { NetworkEvent.Error(it) }
                .startWith(NetworkEvent.InProgress)
                .share()
    }

    fun fetchMessagesFromConversation(idConversation: Int): Flowable<List<MessageResponse>> {
        return service.getMessagesFromConversation(idConversation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .share()
    }

    fun createMessage(idConversation: Int?, idUserMessage: Int, messageContent: String, sendDate: String) : Observable<NetworkEvent> {
        val messageData = MessageData(idConversation, idUserMessage, messageContent, sendDate)

        return service.createMessage(messageData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map<NetworkEvent> { NetworkEvent.Success }
                .onErrorReturn { NetworkEvent.Error(it) }
                .startWith(NetworkEvent.InProgress)
                .share()
    }

    */
/*
    fun deleteMessage(idMessage: Int): Observable<NetworkEvent> {
        return service.deleteMessage(idMessage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map<NetworkEvent> { NetworkEvent.Success }
                .onErrorReturn { NetworkEvent.Error(it) }
                .startWith(NetworkEvent.InProgress)
                .share()
    }
    *//*

}*/
