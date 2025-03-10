package ru.netology.nework.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.listUserArg
import ru.netology.nework.activity.AppActivity.Companion.postArg
import ru.netology.nework.activity.AppActivity.Companion.uriArg
import ru.netology.nework.adapter.AdapterPostView
import ru.netology.nework.adapter.OnIteractionListenerPostView
import ru.netology.nework.adapter.YaKit
import ru.netology.nework.databinding.PostViewBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dto.Post
import ru.netology.nework.error.UnknownError
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.AuthViewModel.Companion.userAuth
import ru.netology.nework.viewmodel.MediaViewModel
import ru.netology.nework.viewmodel.PostsViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class PostView : Fragment() {

    private val viewModelMedia: MediaViewModel by viewModels()
    private var binding: PostViewBinding? = null
    private var curFrag: CurrentShowFragment? = null

    @Inject
    lateinit var yakit: YaKit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: PostsViewModel by viewModels()
        val viewModelUsers: UsersViewModel by viewModels()
        binding = PostViewBinding.inflate(inflater, container, false)

        // Получаем пост из аргументов
        val post = arguments?.postArg
        val txtShare = (post?.attachment?.url ?: post?.content).toString()

        // Инициализация адаптера
        val adapter = container?.context?.let { _ ->
            AdapterPostView(binding!!, object : OnIteractionListenerPostView {
                override fun onLike(post: Post) {
                    if (userAuth) {
                        viewModel.like(post, !post.likedByMe)
                    } else {
                        DialogAuth.newInstance(
                            AuthViewModel.DIALOG_IN,
                            getString(R.string.need_auth_error) // Локализованная строка
                        ).show(childFragmentManager, null)
                    }
                }


                override fun playAudio(link: String) {
                    if (binding!!.playAudio.isChecked) {
                        viewModelMedia.playAudio(link)
                    } else {
                        viewModelMedia.pauseAudio()
                    }
                }

                override fun playVideo(link: String) {
                    viewModelMedia.playVideo(link, binding!!.videoView)
                }

                override fun openSpacePhoto(post: Post) {
                    findNavController().navigate(
                        R.id.spacePhoto,
                        Bundle().apply {
                            uriArg = post.attachment?.url
                        }
                    )
                }

                override fun showUsers(users: List<Long>?) {
                    val list = users?.let { viewModelUsers.selectUsers(it) }
                    findNavController().navigate(
                        R.id.tmpFrag,
                        Bundle().apply {
                            listUserArg = list
                        }
                    )
                }
            }, yakit)
        }

        // Наблюдение за изменениями списка пользователей
        viewModelUsers.listUsers.observe(viewLifecycleOwner) {}

        // Наблюдение за полученными постами
        viewModel.receivedPosts.observe(viewLifecycleOwner) { posts ->
            val _post = posts.find { it.id == post?.id }
            _post?.let {
                adapter?.bind(_post)
            }
        }

        // Наблюдение за длительностью медиа
        viewModelMedia.duration.observe(viewLifecycleOwner) {
            if (it != "STOP") binding?.duration?.text = it
            else binding?.playAudio?.isChecked = false
        }

        // Добавление меню
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_share, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.share -> {
                        sharePost(txtShare)
                        true
                    }

                    android.R.id.home -> {
                        Log.d("PostView", "Navigating up")
                        viewModelMedia.stopAudio()
                        findNavController().navigateUp()
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)

        return binding!!.root
    }

    private fun sharePost(txtShare: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, txtShare)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(intent, "Share Post")
        startActivity(shareIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    fun stopMedia() {
        viewModelMedia.stopAudio()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            curFrag = context as CurrentShowFragment
        } catch (e: ClassCastException) {
            throw UnknownError
        }
    }

    override fun onDetach() {
        super.onDetach()
        curFrag?.getCurFragmentDetach()
        curFrag = null
    }

    override fun onStart() {
        super.onStart()
        curFrag?.getCurFragmentAttach(getString(R.string.post))
    }

    override fun onStop() {
        yakit.stopMapView()
        super.onStop()
    }
}