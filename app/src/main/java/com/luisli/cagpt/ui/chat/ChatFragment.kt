package com.luisli.cagpt.ui.chat
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.luisli.cagpt.R
import com.luisli.cagpt.data.models.ChatPostBody
import com.luisli.cagpt.databinding.FragmentChatBinding
import com.luisli.cagpt.ui.adapters.ChatListAdapter
import com.luisli.cagpt.ui.base.BaseFragment
import com.luisli.cagpt.ui.viewmodels.ChatViewModel
import com.luisli.cagpt.utils.ChatRole
import com.luisli.cagpt.utils.EventObserver
import com.luisli.cagpt.utils.SharedPref
import com.luisli.cagpt.utils.State
import com.luisli.cagpt.utils.TTSHelper
import com.luisli.cagpt.utils.addMenuProvider
import com.luisli.cagpt.utils.dismissKeyboard
import com.luisli.cagpt.utils.hide
import com.luisli.cagpt.utils.invisible
import com.luisli.cagpt.utils.navigate
import com.luisli.cagpt.utils.show
import com.luisli.cagpt.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.CountDownLatch
@AndroidEntryPoint
class ChatFragment : BaseFragment<ChatViewModel, FragmentChatBinding>() {
    private lateinit var ttsHelper: TTSHelper
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var editText: TextInputEditText
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something")
        }
        result.launch(intent)
    }
    private fun splitText(text: String): MutableList<Pair<String, String>> {
        val questionRegex = "<question>.*?</question>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val musicRegex = "Music==>[^\\n]*".toRegex()
        val allRegex = "(<question>.*?</question>|Music==>[^\\n]*)".toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))
        var lastIndex = 0
        val matches = allRegex.findAll(text).toList()
        val elements = mutableListOf<Pair<String, String>>()
        matches.forEach { match ->
            val pureText = text.substring(lastIndex, match.range.first).trim()
            if (pureText.isNotEmpty()) {
                elements.add(Pair("pure_text", pureText))
            }
            val matchedText = match.value
            when {
                questionRegex.containsMatchIn(matchedText) -> {
                   val queText = matchedText
                        .replace("\\s*<question>\\s*".toRegex(), "")
                        .replace("\\s*</question>\\s*".toRegex(), "")
                    elements.add(Pair("question_text", "You may also want to know: \n$queText"))
                }
                musicRegex.containsMatchIn(matchedText) -> {
                                        if (matchedText != "NA" && matchedText.isNotBlank()) {
                        elements.add(Pair("music_text", "Music==>$matchedText"))
                    }
                }
            }
            lastIndex = match.range.last + 1
        }
        val finalPureText = text.substring(lastIndex, text.length).trim()
        if (finalPureText.isNotEmpty()) {
            elements.add(Pair("pure_text", finalPureText))
        }
        return elements
    }
    private fun speakAndDisplayContents(allContents: MutableList<Pair<String, String>>) {
        coroutineScope.launch {
            allContents.forEach { content ->
                mViewModel.chatMessageList.add(
                    ChatPostBody.Message(
                        content = content.second,
                        role = ChatRole.ASSISTANT.name.lowercase()
                    )
                )
                chatListAdapter.addItems(mViewModel.chatMessageList)
                mViewBinding.apply {
                    rvChatList.scrollToPosition(chatListAdapter.itemCount.minus(1))
                }
                                val latch = CountDownLatch(1)
                when (content.first) {
                    "question_text" ->
                        ttsHelper.speakOut("You may also want to know the following questions. Type the number to ask.", latch)
                    "music_text" ->
                        ttsHelper.speakOut("Click the play button for the music: ${content.second.replace("Music==>","")}.", latch)
                    else ->
                        ttsHelper.speakOut(content.second, latch)
                }
                withContext(Dispatchers.IO) {
                    try {
                        latch.await()                     } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
                        mViewBinding.apply {
                pbLoading.hide()
                fabSend.show()
            }
        }
    }
    private val chatListAdapter by lazy {
        ChatListAdapter(requireActivity(), mViewModel)
    }
    private val result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result->
        if (result.resultCode == Activity.RESULT_OK){
            val results = result.data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            ) as ArrayList<String>
            editText.setText(results[0])
        }
    }
    override val mViewModel: ChatViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editText = view.findViewById(R.id.et_message)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myActualApiKey = "sk-XXXX"
        val keyTokenLength = "200"
        SharedPref.setStringPref(requireContext(), SharedPref.KEY_API_KEY, myActualApiKey)
        SharedPref.setStringPref(requireContext(), SharedPref.KEY_TOKEN_LENGTH, keyTokenLength)
        mViewModel.maxTokensLength =
            SharedPref.getStringPref(requireContext(), SharedPref.KEY_TOKEN_LENGTH).toInt()
        chatListAdapter.addItems(mViewModel.chatMessageList)
        ttsHelper = TTSHelper(requireContext(), Runnable {
                                            })
    }
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChatBinding {
        return FragmentChatBinding.inflate(inflater, container, false)
    }
    override fun setupUI() {
        addMenuProvider(R.menu.menu) { menuItem ->
            when (menuItem.itemId) {
                R.id.faq -> {
                    navigate(ChatFragmentDirections.actionChatFragmentToFaqFragment())
                    true
                }
                R.id.settings -> {
                    navigate(ChatFragmentDirections.actionChatFragmentToSettingsFragment())
                    true
                }
                else -> false
            }
        }
        mViewBinding.apply {
            lifecycleOwner = viewLifecycleOwner
            rvChatList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = chatListAdapter
            }
            fabMicrophone.setOnClickListener {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.RECORD_AUDIO) -> {
                        startSpeechRecognition()
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
            fabSend.setOnClickListener {
                requireContext().dismissKeyboard(it)
                if (SharedPref.getStringPref(requireContext(), SharedPref.KEY_API_KEY)
                        .isNullOrBlank()
                ) {
                    requireContext().showToast(getString(R.string.message_add_api_key))
                    return@setOnClickListener
                }
                if (etMessage.text.toString().isNullOrBlank()) {
                    requireContext().showToast(getString(R.string.message_enter_some_text))
                    return@setOnClickListener
                }
                mViewModel.chatMessageList.add(
                    ChatPostBody.Message(
                        content = etMessage.text.toString().replace("\n", " "),                         role = ChatRole.USER.name.lowercase()
                    )
                )
                chatListAdapter.addItems(mViewModel.chatMessageList)
                rvChatList.scrollToPosition(
                    chatListAdapter.itemCount.minus(1)
                )
                mViewModel.postMessage()
                etMessage.text?.clear()
            }
        }
    }
    override fun observeAPICall() {
        mViewModel.chatLiveData.observe(viewLifecycleOwner, EventObserver { state ->
            when (state) {
                is State.Loading -> {
                    mViewBinding.apply {
                        pbLoading.show()
                        fabSend.invisible()
                    }
                }
                is State.Success -> {
                    if (state.data.choices.isNotEmpty()) {
                        val content = state.data.choices.first().message.content
                        val content_dict = splitText(content)
                        speakAndDisplayContents(content_dict)
                    }
                }
                is State.Error -> {
                    mViewBinding.apply {
                        pbLoading.hide()
                        fabSend.show()
                    }
                    requireContext().showToast(state.message)
                }
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()      }
}