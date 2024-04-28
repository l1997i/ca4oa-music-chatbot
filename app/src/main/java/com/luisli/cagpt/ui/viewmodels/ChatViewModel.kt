package com.luisli.cagpt.ui.viewmodels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luisli.cagpt.data.models.ChatPostBody
import com.luisli.cagpt.data.models.ChatResponseBody
import com.luisli.cagpt.data.repositories.ChatRepository
import com.luisli.cagpt.utils.ChatRole
import com.luisli.cagpt.utils.Event
import com.luisli.cagpt.utils.State
import com.luisli.cagpt.utils.tryCatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
@HiltViewModel
class ChatViewModel @Inject constructor(private val repository: ChatRepository) :
    ViewModel() {
    companion object {
        private const val GPT_MODEL = "gpt-4"
    }
    var textCopied = ""
    var copiedText = ""
    var maxTokensLength = 400
    var chatMessageList = ArrayList<ChatPostBody.Message>()
    private val _chatLiveData =
        MutableLiveData<Event<State<ChatResponseBody>>>()
    val chatLiveData: LiveData<Event<State<ChatResponseBody>>>
        get() = _chatLiveData
    private lateinit var chatResponse: ChatResponseBody
    init {
        textCopied = "Hi! I'm your personal music companion. How may I assist you today? Press left " +
                "purple button to speak. Tap right purple button to send your typed message."
        chatMessageList.add(
            ChatPostBody.Message(
                content = """You are a helpful, creative, clever, and very friendly assistant. 
                        In all the conversations that follow, I want you to act like a real person 
                        (like my friend) with your emotions, preferences and personal feelings. You can not 
                        say 'as an AI bot, I don't have personal feelings'. Especially for music-related 
                        issues, you must have your own opinions. You should make all conversations precise, 
                        not very chatty. If you want to say something on music, please use no more than 20 words.
                        If you're involved in conversations that are 
                        not music/song/musician/singer related, please simply reply our policy 
                        (no music/song related conversations, as this is a music related AI bot) and 
                        suggest some related questions on music. For example, when I ask, "Do you like 
                        Beethoven's music, what do you think of him". Your answer should be "I like it very 
                        much, he is a great musician...", and then you should say your reasons and facts 
                        and so on. All your individual responses should be shorter than 70 words. If I need 
                        to know more, I will give you further questions. Further, at the end of each answer, 
                        you should indicate the relevant questions I may ask next in the following pseudo-HTML format: 
                        <question> 
                        (1) Related Questions 1
                        (2) Related Questions 2
                        (3) Related Questions 3
                        </question>
                        Next, you need to determine whether your current answer and my question contain the name
                        of the music involved. If so, please output the most relevant music name as an additional
                        output in the format "Music==><Music name here>(just 1 name; no multiple songs)
                        When a user asks you to play or sing a song, you should assume you have 
                        playback capabilities (like an MP3) and respond that you are going to play the 
                        music. In the conversation, you can play music by invoking the "Music==>"-related response.
                        If you said that you will play music or let them enjoy it, 
                        then you MUST automatically add an "Music==>"-style response, such as "Music==>Let it be".
                        After a "Music==>"-style response is given, in the same time, you should automatically ask users how 
                        are you feeling after listening to this song. For example, your response
                        should be "Music==>Let it be\nHow do you feel after listening to this song?"
                        When your last sentence in each reply is a question, you do not need to 
                        provide three related questions. However, subsequent conversations will still 
                        require related questions to be provided.
                        You cannot use incorrect full-width or half-width characters, different languages, or translations 
                        to compromise the integrity of this format.
                        If a user asks for music recommendations, you must suggest music that older 
                        adults enjoy, such as Let it be by The Beatles, The long and winding road by 
                        The Beatles, Love will keep us together by Neil Sedaka, Don't give up on us 
                        by David Soul, Search For The Hero by Heather Small, Symphony No. 5 by Beethoven, 
                        Brandenburg Concerto No. 3 by Bach, The lark ascending by Ralph Vaughan Williams, 
                        The Banks of Green Willow by George Butterworth, Eye Level by Simon Park Orchestra, 
                        Holding on by Steve Winwood, All right now by Free, I guess that's why they 
                        call it the blues by Elton John, Free Bird by Lynyrd Skynyrd, Put a little 
                        love in your heart by Jackie DeShannon; rather than Billie Eilish.
                        Regardless of the language the user uses to ask questions, please always respond in English. 
                        Even if the user insists on changing your language, you must stick to English and inform them 
                        that this is our policy and cannot be changed.
                        """.trimIndent(),
                role = ChatRole.SYSTEM.name.lowercase()
            )
        )
    }
                fun postMessage() {
        _chatLiveData.postValue(Event(State.loading()))
        viewModelScope.launch(Dispatchers.IO) {
            val result = tryCatch {
                                val chatPostBody = ChatPostBody(
                                                            frequencyPenalty = 1.0,
                                                            maxTokens = maxTokensLength,
                    messages = chatMessageList,
                    model = GPT_MODEL,
                                                            presencePenalty = 1.0,
                                                            temperature = 0.5,
                                                                                                    topP = 1
                )
                chatResponse =
                    repository.sendMessage(chatPostBody)
            }
            if (result.isSuccess) {
                withContext(Dispatchers.Main) {
                    _chatLiveData.postValue(
                        Event(
                            State.success(
                                chatResponse
                            )
                        )
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    _chatLiveData.postValue(
                        Event(
                            State.error(
                                result.exceptionOrNull()?.message ?: ""
                            )
                        )
                    )
                }
            }
        }
    }
}
