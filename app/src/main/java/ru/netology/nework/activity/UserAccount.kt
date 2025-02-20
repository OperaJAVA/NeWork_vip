package ru.netology.nework.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.postArg
import ru.netology.nework.activity.AppActivity.Companion.userArg
import ru.netology.nework.adapter.AdapterJobsList
import ru.netology.nework.adapter.AdapterUserPosts
import ru.netology.nework.adapter.ListenerSelectionJobs
import ru.netology.nework.adapter.OnUserPostsListener
import ru.netology.nework.databinding.UserAccountBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dialog.DialogSelectRemoveJob
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.error.UnknownError
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.AuthViewModel.Companion.myID
import ru.netology.nework.viewmodel.PostsViewModel
import ru.netology.nework.viewmodel.UsersViewModel

const val REMOVE_JOB = 2
const val SELECT_DATE = 1

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class UserAccount : Fragment() {

    private val viewModelUser: UsersViewModel by viewModels()
    private val viewModelPosts: PostsViewModel by viewModels()

    private var nameLoginUser = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = UserAccountBinding.inflate(inflater)
        val idUser = arguments?.userArg?.id!!
        nameLoginUser =
            "${arguments?.userArg?.name.toString()} / ${arguments?.userArg?.login.toString()}"

        fun showBar(txt: String) {
            Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
        }

        lifecycleScope.launch {
            viewModelUser.getUser(idUser)
            viewModelUser.getUserJobs(idUser)
            viewModelPosts.getUserPosts(idUser)
        }

        val adapterJobs = AdapterJobsList(object : ListenerSelectionJobs {
            override fun removeJob(job: Job) {
                job.id?.let {
                    DialogSelectRemoveJob.newInstance(REMOVE_JOB, it)
                        .show(childFragmentManager, "TAG")
                }
            }
        })

        binding.listJobs.adapter = adapterJobs

        val adapterPosts = AdapterUserPosts(object : OnUserPostsListener {
            override fun onLike(post: Post) {
                if (AuthViewModel.userAuth) {
                    viewModelPosts.like(post, !post.likedByMe)
                } else {
                    DialogAuth.newInstance(
                        AuthViewModel.DIALOG_IN,
                        "Для установки лайков нужно авторизоваться"
                    ).show(childFragmentManager, "TAG")
                }
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(intent, "Share Post")
                startActivity(shareIntent)
            }

            override fun onEdit(post: Post) {
                findNavController().navigate(
                    R.id.newPostFrag,
                    Bundle().apply { postArg = post }
                )
            }

            override fun onRemove(post: Post) {
                viewModelPosts.removePost(post)
            }

            override fun openCardPost(post: Post) {
                findNavController().navigate(
                    R.id.postView,
                    Bundle().apply { postArg = post }
                )
            }
        })

        binding.listPosts.adapter = adapterPosts

        fun showListJobs(status: Boolean) {
            binding.listPosts.visibility = if (status) View.GONE else View.VISIBLE
            binding.listJobs.visibility = if (status) View.VISIBLE else View.GONE
        }

        viewModelUser.listUsers.observe(viewLifecycleOwner) { users ->
            viewModelUser.takeUser(users.find { it.id == idUser })
        }

        viewModelUser.userAccount.observe(viewLifecycleOwner) { user ->
            with(binding) {
                Glide.with(imageAvatar)
                    .load(user.avatar)
                    .timeout(35_000)
                    .into(imageAvatar)
            }
        }

        viewModelUser.statusShowListJobs.observe(viewLifecycleOwner) {
            if (it.statusShowListJobs) {
                showListJobs(true)
                if (myID == idUser) {
                    binding.fab.visibility = View.VISIBLE
                }
            } else {
                binding.fab.visibility = View.GONE
                showListJobs(false)
            }
        }

        viewModelUser.userJobs.observe(viewLifecycleOwner) { jobs ->
            adapterJobs.submitList(jobs.filter { it.idUser == idUser })
        }

        viewModelPosts.userWall.observe(viewLifecycleOwner) { posts ->
            if (binding.listPosts.isVisible) {
                adapterPosts.submitList(posts)
            }
        }

        viewModelPosts.receivedPosts.observe(viewLifecycleOwner) { posts ->
            viewModelPosts.takePosts(posts.filter { it.authorId == idUser })
        }

        viewModelUser.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state is FeedModelState.Loading
            when (state) {
                is FeedModelState.Loading -> {
                    // Состояние загрузки
                }
                is FeedModelState.Error -> {
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) { viewModelUser.getUserJobs(idUser) }
                        .show()
                }
                is FeedModelState.NotFound -> {
                    showBar("Пользователь не найден!")
                }
                is FeedModelState.Unauthorized -> {
                    showBar("Ошибка авторизации!")
                }
                is FeedModelState.BadRequest -> {
                    showBar("Неверный запрос!")
                }
                is FeedModelState.NetworkError -> {
                    showBar("Ошибка сети!")
                }
                is FeedModelState.Refreshing -> {
                    // Состояние обновления
                }
                is FeedModelState.Success<*> -> {
                    // Успешное состояние, можно обработать данные, если необходимо
                }
                is FeedModelState.AuthStatus -> {
                    // Обработка статуса аутентификации
                }
                is FeedModelState.State -> {
                    // Обработка состояния
                }
                FeedModelState.UnsupportedMediaType -> {
                    showBar("Неподдерживаемый тип медиа!")
                }
            }
        }

        binding.wallJobsNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.jobs -> {
                    viewModelUser.setStatusShowListJobs(true)
                    showListJobs(true)
                    true
                }
                R.id.wall -> {
                    viewModelUser.setStatusShowListJobs(false)
                    showListJobs(false)
                    adapterPosts.submitList(viewModelPosts.userWall.value)
                    true
                }
                else -> false
            }
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.newJob)
        }

        return binding.root
    }

    private var curFrag: CurrentShowFragment? = null

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
        curFrag?.getCurFragmentAttach(nameLoginUser)
    }

    fun getIdJob(id: Long) {
        viewModelUser.removeJob(id)
    }
}