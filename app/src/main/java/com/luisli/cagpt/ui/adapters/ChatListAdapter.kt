package com.luisli.cagpt.ui.adapters
import android.annotation.SuppressLint
import android.app.Activity
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.luisli.cagpt.R
import com.luisli.cagpt.data.models.ChatPostBody
import com.luisli.cagpt.databinding.ListItemChatMessageReceivedBinding
import com.luisli.cagpt.databinding.ListItemChatMessageReceivedMusicBinding
import com.luisli.cagpt.databinding.ListItemChatMessageSentBinding
import com.luisli.cagpt.ui.activities.MusicData
import com.luisli.cagpt.ui.activities.MusicInterface
import com.luisli.cagpt.ui.viewmodels.ChatViewModel
import com.luisli.cagpt.utils.ChatRole
import com.luisli.cagpt.utils.copyTextToClipboard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
class ChatListAdapter(private val context: Activity, private val mViewModel: ChatViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
        private const val VIEW_TYPE_MESSAGE_RECEIVED_MUSIC = 3
    }
    private var chatList = ArrayList<ChatPostBody.Message>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                val binding: ListItemChatMessageSentBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.list_item_chat_message_sent,
                    parent,
                    false
                )
                MessageSentViewHolder(binding)
            }
            VIEW_TYPE_MESSAGE_RECEIVED -> {
                val binding: ListItemChatMessageReceivedBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.list_item_chat_message_received,
                    parent,
                    false
                )
                MessageReceivedViewHolder(binding)
            }
            else -> {
                val binding: ListItemChatMessageReceivedMusicBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.list_item_chat_message_received_music,
                    parent,
                    false
                )
                MessageReceivedMusicViewHolder(binding)
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MessageSentViewHolder -> {
                chatList[position].let { holder.bindItems(it, position) }
            }
            is MessageReceivedViewHolder -> {
                chatList[position].let { holder.bindItems(it, position) }
            }
            is MessageReceivedMusicViewHolder -> {
                chatList[position].let { holder.bindItems(it, position) }
            }
        }
    }
    override fun getItemCount(): Int = chatList.size
    override fun getItemViewType(position: Int): Int =
        when (chatList[position].role) {
            ChatRole.SYSTEM.name.lowercase() -> VIEW_TYPE_MESSAGE_RECEIVED
            ChatRole.USER.name.lowercase() -> VIEW_TYPE_MESSAGE_SENT
            else -> {
                val messageContent = chatList[position].content
                when {
                    "Music==>" in messageContent -> {
                        VIEW_TYPE_MESSAGE_RECEIVED_MUSIC
                    }
                    else -> VIEW_TYPE_MESSAGE_RECEIVED
                }
            }
        }
    fun addItems(items: List<ChatPostBody.Message>) {
        chatList = items as ArrayList<ChatPostBody.Message>
        notifyDataSetChanged()
    }
    inner class MessageSentViewHolder(private val binding: ListItemChatMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindItems(message: ChatPostBody.Message, itemPosition: Int) {
            binding.apply {
                tvMessageSent.text = message.content.substringAfterLast("\n")
                tvMessageSent.setOnLongClickListener {
                    it.context.copyTextToClipboard(tvMessageSent.text.toString())
                    true
                }
            }
        }
    }
    inner class MessageReceivedViewHolder(private val binding: ListItemChatMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindItems(message: ChatPostBody.Message, itemPosition: Int) {
            binding.apply {
                tvMessageReceived.text =
                    if (itemPosition == 0) mViewModel.textCopied else message.content
                tvMessageReceived.setOnLongClickListener {
                    it.context.copyTextToClipboard(tvMessageReceived.text.toString())
                    true
                }
            }
        }
    }
    inner class MessageReceivedMusicViewHolder(private val binding: ListItemChatMessageReceivedMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var isMusicPlaying = false
        private var mediaPlayer: MediaPlayer? = null
        private fun fetchMusicAndPreparePlayer(musicName: String) {
                        val retrofitBuilder = Retrofit.Builder()
                .baseUrl("https://deezerdevs-deezer.p.rapidapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MusicInterface::class.java)
            val retrofitData = retrofitBuilder.getMusic(musicName)
            retrofitData.enqueue(object : Callback<MusicData?> {
                override fun onResponse(call: Call<MusicData?>, response: Response<MusicData?>) {
                    val dataList = response.body()?.data!!
                    mediaPlayer = MediaPlayer.create(context, dataList[0].preview.toUri()).apply {
                        setOnCompletionListener {
                                                        isMusicPlaying = false
                            binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                        }
                    }
                                        binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                }
                override fun onFailure(call: Call<MusicData?>, t: Throwable) {
                    Timber.tag("TAG: onFailure").d("onFailure%s", t.message)
                }
            })
        }
        private fun toggleMusicPlayback() {
            mediaPlayer?.let { mp ->
                if (isMusicPlaying) {
                                        mp.pause()
                                        binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                                    } else {
                                        mp.start()
                                        binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                                    }
                                isMusicPlaying = !isMusicPlaying
            }
        }
        @SuppressLint("SetTextI18n")
        fun bindItems(message: ChatPostBody.Message, itemPosition: Int) {
                        if (message.content.contains("Music==>NA")) {
                Timber.tag("music.involved").d("NA")
                return
            }
            else {
                val musicName = message.content.substringAfterLast("Music==>").replace("\"", "")
                Timber.tag("music").d(musicName)
                fetchMusicAndPreparePlayer(musicName)
                binding.btnPlayPause.setOnClickListener {
                    toggleMusicPlayback()
                }
                binding.apply {
                    tvMessageReceived.text =
                        if (itemPosition == 0) mViewModel.textCopied else "Click to play: $musicName"
                    tvMessageReceived.setOnLongClickListener {
                        it.context.copyTextToClipboard(tvMessageReceived.text.toString())
                        true
                    }
                }
            }
        }
    }
}
