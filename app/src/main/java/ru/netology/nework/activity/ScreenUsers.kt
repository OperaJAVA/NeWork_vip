package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.userArg
import ru.netology.nework.adapter.AdapterUsersList
import ru.netology.nework.adapter.ListenerSelectionUser
import ru.netology.nework.databinding.ScreenUsersBinding
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.UsersViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class ScreenUsers : Fragment() {
    private var binding: ScreenUsersBinding? = null
    private val viewModelAuth: AuthViewModel by viewModels()
    private val viewModel: UsersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScreenUsersBinding.inflate(inflater)
        binding?.bottomNavigationUsers?.selectedItemId = R.id.menu_users

        val adapter = AdapterUsersList(object : ListenerSelectionUser {
            override fun selectUser(user: UserResponse?) {
                val id = user?.id
                findNavController().navigate(
                    R.id.userAccount,
                    id?.let {
                        Bundle().apply {
                            userArg = user
                        }
                    }
                )
            }


            override fun addUser(idUser: Long?) {
                // Реализация добавления пользователя
            }

            override fun removeUser(idUser: Long?) {
                // Реализация удаления пользователя
            }
        }, false)

        fun showBar(txt: String) {
            Snackbar.make(binding?.root!!, txt, Snackbar.LENGTH_LONG).show()
        }

        binding?.listUsers?.adapter = adapter

        viewModel.listUsers.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding?.progress?.isVisible = state is FeedModelState.Loading
            binding?.swipeRefreshLayout?.isRefreshing = state is FeedModelState.Refreshing

            when (state) {
                is FeedModelState.Error -> {
                    Snackbar.make(binding?.root!!, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) { viewModel.loadUsers() }
                        .show()
                }

                is FeedModelState.Unauthorized -> {
                    showBar("Доступ закрыт, выполните вход")
                }

                else -> {}
            }
        }

        binding?.swipeRefreshLayout?.setOnRefreshListener {
            viewModel.loadUsers()
        }
        binding?.swipeRefreshLayout?.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_red_light,
        )

        viewModelAuth.authState.observe(viewLifecycleOwner) { _ ->
            adapter.submitList(viewModel.listUsers.value)
        }

        binding?.bottomNavigationUsers?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_posts -> {
                    findNavController().navigate(R.id.screenPosts)
                    true
                }

                R.id.menu_events -> {
                    findNavController().navigate(R.id.screenEvents)
                    true
                }

                R.id.menu_users -> true
                else -> false
            }
        }

        return binding?.root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null // Зануляем binding для предотвращения утечек памяти
    }

    override fun onResume() {
        binding?.bottomNavigationUsers?.selectedItemId = R.id.menu_users
        super.onResume()
    }
}
