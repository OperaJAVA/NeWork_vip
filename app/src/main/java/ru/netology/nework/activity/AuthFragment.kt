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
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.viewmodel.AuthViewModel

@AndroidEntryPoint
class AuthFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()
    private var binding: AuthFragmentBinding? = null
    private var pressBtn = false
    private var curFrag: CurrentShowFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AuthFragmentBinding.inflate(inflater, container, false)

        viewModel.authState.value?.let {
            binding?.fieldLogin?.editText?.setText(it.login)
            binding?.fieldPass?.editText?.setText(it.pass)
        }

        binding?.btnSignIn?.setOnClickListener {
            if (binding?.fieldLogin?.editText?.text.isNullOrEmpty() ||
                binding?.fieldPass?.editText?.text.isNullOrEmpty()
            ) {
                showSnackbar(getString(R.string.error_empty_fields))
            } else {
                pressBtn = true
                val login = binding?.fieldLogin?.editText?.text.toString()
                val pass = binding?.fieldPass?.editText?.text.toString()
                viewModel.getAuthFromServer(login, pass)
            }
        }

        observeViewModel()

        return binding!!.root
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding!!.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { auth ->
            if (pressBtn) {
                if (auth.login != null && auth.pass != null) {
                    findNavController().popBackStack()
                } else {
                    AuthViewModel.userAuth = false
                    showSnackbar(getString(R.string.error_user_not_found))
                }
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (AuthViewModel.userAuth) {
                showSnackbar(getString(R.string.login_success))
                findNavController().navigateUp()
            }
            if (state is FeedModelState.State) {
                when {
                    state.error400 -> showSnackbar(getString(R.string.error_incorrect_password))
                    state.error404 -> showSnackbar(getString(R.string.error_user_not_registered))
                    state.error -> showSnackbar(getString(R.string.error_check_connection))
                }
                binding?.statusAuth?.isVisible = state.loading
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null // Зануляем binding для предотвращения утечек памяти
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
        curFrag?.getCurFragmentAttach(getString(R.string.sign_in))
    }
}