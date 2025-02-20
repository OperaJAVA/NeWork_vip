package ru.netology.nework.activity

import android.content.Context
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
import ru.netology.nework.R
import ru.netology.nework.databinding.AuthFragmentBinding
import ru.netology.nework.error.UnknownError
import ru.netology.nework.viewmodel.AuthViewModel

@Suppress("UNUSED_PARAMETER")
@AndroidEntryPoint
class AuthFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()
    private var pressBtn = false
    private var curFrag: CurrentShowFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = AuthFragmentBinding.inflate(inflater, container, false)

        // Инициализация UI
        initializeUI(binding)

        // Наблюдение за состоянием аутентификации
        observeAuthState(binding)
        observeDataState(binding)

        return binding.root
    }

    private fun initializeUI(binding: AuthFragmentBinding) {
        // Заполнение полей входа, если есть предыдущее состояние
        viewModel.authState.value?.let { authState ->
            binding.fieldLogin.editText?.setText(authState.login)
            binding.fieldPass.editText?.setText(authState.pass)
        }

        binding.btnSignIn.setOnClickListener {
            handleSignIn(binding)
        }
    }

    private fun handleSignIn(binding: AuthFragmentBinding) {
        val login = binding.fieldLogin.editText?.text.toString()
        val pass = binding.fieldPass.editText?.text.toString()

        if (login.isBlank() || pass.isBlank()) {
            showSnackbar("Все поля должны быть заполнены!")
        } else {
            pressBtn = true
            viewModel.getAuthFromServer(login, pass)
        }
    }

    private fun observeAuthState(binding: AuthFragmentBinding) {
        viewModel.authState.observe(viewLifecycleOwner) { auth ->
            if (pressBtn) {
                if (auth.login != null && auth.pass != null) {
                    findNavController().popBackStack()
                } else {
                    AuthViewModel.userAuth = false
                    showSnackbar("Такого пользователя нет!")
                }
            }
        }
    }

    private fun observeDataState(binding: AuthFragmentBinding) {
        viewModel.dataState.observe(viewLifecycleOwner) { dataState ->
            binding.statusAuth.isVisible = dataState.loading

            when {
                AuthViewModel.userAuth -> {
                    showSnackbar("Выполнен вход в аккаунт")
                    findNavController().navigateUp()
                }
                dataState.error400 -> showSnackbar("Неправильный пароль!")
                dataState.error404 -> showSnackbar("Пользователь не зарегистрирован!")
                dataState.error -> showSnackbar("Проверьте ваше подключение к сети!")
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        curFrag = context as? CurrentShowFragment ?: throw UnknownError
    }

    override fun onDetach() {
        super.onDetach()
        curFrag?.getCurFragmentDetach()
        curFrag = null
    }

    override fun onStart() {
        super.onStart()
        curFrag?.getCurFragmentAttach(getString(R.string.sign_in))
    }
}