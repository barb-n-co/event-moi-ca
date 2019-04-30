package com.example.event_app.adapter

/*
class ChatAdapter(private val idUser: Int, private val context: Context): ListAdapter<Message, ChatAdapter.ChatViewHolder>(DiffCardCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_user, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(message: Message) {
            if(message.idUser == idUser){
                itemView.iv_user_chat_fragment.visibility = View.GONE
                itemView.tv_name_chat_fragment.visibility = View.GONE
                itemView.tv_date_other_user_chat_fragment.visibility = View.INVISIBLE
                itemView.tv_date_user_chat_fragment.visibility = View.VISIBLE
                itemView.cardView_message_chat_fragment.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            } else {
                itemView.iv_user_chat_fragment.visibility = View.VISIBLE
                itemView.tv_name_chat_fragment.visibility = View.VISIBLE
                itemView.tv_date_other_user_chat_fragment.visibility = View.VISIBLE
                itemView.tv_date_user_chat_fragment.visibility = View.INVISIBLE
                itemView.cardView_message_chat_fragment.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
            }

            if (message.imageUrl != null) {
                Picasso.get()
                        .load(message.imageUrl)
                        .transform(CircleTransform())
                        .placeholder(R.drawable.ic_profile)
                        .into(itemView.iv_user_chat_fragment)
            }
            itemView.tv_name_chat_fragment.text = message.firstname + " " + message.lastname
            itemView.tv_date_user_chat_fragment.text = message.sendDate
            itemView.tv_date_other_user_chat_fragment.text = message.sendDate
            itemView.tv_message_chat_fragment.text = URLDecoder.decode(message.messageContent, "utf-8")
        }
    }

    class DiffCardCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}*/
