package ru.netology.nework.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.idArg
import ru.netology.nework.activity.AppActivity.Companion.listUserArg
import ru.netology.nework.activity.AppActivity.Companion.uriArg
import ru.netology.nework.adapter.AdapterEventView
import ru.netology.nework.adapter.OnEventListener
import ru.netology.nework.adapter.YaKit
import ru.netology.nework.databinding.EventViewBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dto.Event
import ru.netology.nework.error.UnknownError
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventsViewModel
import ru.netology.nework.viewmodel.MediaViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class EventView : Fragment() {

    @Inject
    lateinit var yakit: YaKit
    private var binding: EventViewBinding? = null

    private val viewModelEvents: EventsViewModel by viewModels()
    private val viewModelUsers: UsersViewModel by viewModels()
    private val viewModelMedia: MediaViewModel by viewModels()

    private var curFrag: CurrentShowFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = EventViewBinding.inflate(inflater, container, false)

        setupEventAdapter()
        observeViewModels()
        setupMenu()

        return binding?.root
    }

    private fun setupEventAdapter() {
        val idEvent = arguments?.idArg
        val adapterEventView = context?.let {
            AdapterEventView(binding!!, yakit, object : OnEventListener {
                override fun onLike(event: Event) {
                    handleLikeEvent(event)
                }

                override fun openSpacePhoto(event: Event) {
                    navigateToSpacePhoto(event)
                }

                override fun playVideo(url: String) {
                    viewModelMedia.playVideo(url, binding?.videoView!!)
                }

                override fun playAudio(url: String) {
                    toggleAudioPlayback(url)
                }

                override fun showUsers(users: List<Long>?) {
                    navigateToUserList(users)
                }

                override fun participateEvan(event: Event) {
                    handleParticipationEvent(event)
                }
            })
        }

        viewModelEvents.receivedEvents.observe(viewLifecycleOwner) { events ->
            val event = events.find { it.id == idEvent }
            event?.let {
                adapterEventView?.bind(it)
            }
        }
    }

    private fun handleLikeEvent(event: Event) {
        if (AuthViewModel.userAuth) {
            viewModelEvents.likeEvent(event, !(event.likedByMe ?: false))
        } else {
            showAuthDialog()
        }
    }

    private fun navigateToSpacePhoto(event: Event) {
        findNavController().navigate(
            R.id.spacePhoto,
            Bundle().apply {
                uriArg = event.attachment?.url
            }
        )
    }

    private fun toggleAudioPlayback(url: String) {
        if (binding?.playAudio?.isChecked == true) {
            viewModelMedia.playAudio(url)
        } else {
            viewModelMedia.pauseAudio()
        }
    }

    private fun navigateToUserList(users: List<Long>?) {
        val list = users?.let { viewModelUsers.selectUsers(it) }
        findNavController().navigate(
            R.id.tmpFrag,
            Bundle().apply {
                listUserArg = list
            }
        )
    }

    private fun handleParticipationEvent(event: Event) {
        if (AuthViewModel.userAuth) {
            viewModelEvents.participateEvent(event, !(event.participatedByMe ?: false))
        } else {
            showAuthDialog()
        }
    }

    private fun showAuthDialog() {
        fun handleParticipationEvent(event: Event) {
            if (AuthViewModel.userAuth) {
                viewModelEvents.participateEvent(event, event.participatedByMe != true)
            } else {
                showAuthDialog(R.string.need_auth_error)
            }
        }

    }

    private fun showAuthDialog(@StringRes message: Int) {
        DialogAuth.newInstance(AuthViewModel.DIALOG_IN, getString(message))
            .show(childFragmentManager, null) // тег вы всё равно не используете
    }

    private fun observeViewModels() {
        viewModelMedia.duration.observe(viewLifecycleOwner) {
            if (it != "STOP") {
                binding?.duration?.text = it
            } else {
                binding?.playAudio?.isChecked = false
            }
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_share, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.share -> {
                        shareEvent()
                        true
                    }

                    android.R.id.home -> {
                        viewModelMedia.stopAudio()
                        findNavController().navigateUp()
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)
    }

    private fun shareEvent() {
        val idEvent = arguments?.idArg
        val event = viewModelEvents.receivedEvents.value?.find { it.id == idEvent }
        val txtShare = (event?.attachment?.url ?: event?.content).toString()

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, txtShare)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(intent, "Share Post")
        startActivity(shareIntent)
    }

    fun stopMedia() {
        viewModelMedia.stopAudio()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        curFrag = context as? CurrentShowFragment ?: throw UnknownError
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null // Зануляем binding для предотвращения утечек памяти
    }

    override fun onDetach() {
        super.onDetach()
        curFrag?.getCurFragmentDetach()
        curFrag = null
    }

    override fun onStart() {
        super.onStart()
        curFrag?.getCurFragmentAttach(getString(R.string.event))
    }

    override fun onStop() {
        stopMedia() // Здесь вызываем метод
        yakit.stopMapView()
        super.onStop()
    }
}
