package ru.netology.nework.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.listUserArg
import ru.netology.nework.activity.AppActivity.Companion.userArg
import ru.netology.nework.adapter.AdapterUsersList
import ru.netology.nework.adapter.ListenerSelectionUser
import ru.netology.nework.databinding.SelectionUsersBinding
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.error.UnknownError

class SelectionUsersFrag : Fragment() {

    private var curFrag: CurrentShowFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SelectionUsersBinding.inflate(inflater, container, false)
        val listUsers = arguments?.listUserArg // Получаем список пользователей из аргументов

        // Инициализация адаптера для списка пользователей
        val adapterUserResponse = AdapterUsersList(object : ListenerSelectionUser {
            override fun selectUser(user: UserResponse?) {
                val id = user?.id
                // Навигация к экрану аккаунта пользователя
                findNavController().navigate(
                    R.id.userAccount,
                    id?.let {
                        Bundle().apply { userArg = user }
                    }
                )
            }

            override fun addUser(idUser: Long?) {
                // Логика добавления пользователя, если нужно
            }

            override fun removeUser(idUser: Long?) {
                // Логика удаления пользователя, если нужно
            }
        }, false)

        // Установка адаптера для RecyclerView
        binding.listUsers.adapter = adapterUserResponse
        adapterUserResponse.submitList(listUsers) // Передача списка пользователей в адаптер

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            curFrag = context as CurrentShowFragment // Привязываем фрагмент к текущему контексту
        } catch (e: ClassCastException) {
            throw UnknownError
        }
    }

    override fun onDetach() {
        super.onDetach()
        curFrag?.getCurFragmentDetach() // Уведомляем текущий фрагмент о отсоединении
        curFrag = null
    }
}