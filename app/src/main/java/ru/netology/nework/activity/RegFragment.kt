package ru.netology.nework.activity

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.RegFragmentBinding
import ru.netology.nework.error.UnknownError
import ru.netology.nework.media.MediaUpload
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.viewmodel.AuthViewModel

@AndroidEntryPoint
class RegFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()
    private var pressBtn = false
    private var upload: MediaUpload? = null
    private var curFrag: CurrentShowFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = RegFragmentBinding.inflate(inflater, container, false)

        binding.btnSignIn.setOnClickListener {
            validateAndRegister(binding)
        }

        viewModel.authState.observe(viewLifecycleOwner) {
            if (pressBtn) {
                findNavController().popBackStack()
            }
            observeDataState(binding)
        }

        registerImagePicker(binding) // Теперь это будет работать

        return binding.root
    }

    private fun registerImagePicker(binding: RegFragmentBinding) { // Функция вынесена на уровень класса
        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    ImagePicker.RESULT_ERROR -> showBar(ImagePicker.getError(result.data))
                    Activity.RESULT_OK -> {
                        val uri: Uri? = result.data?.data
                        upload = MediaUpload(uri?.toFile())
                    }

                    Activity.RESULT_CANCELED -> {
                        upload = null
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent(launcher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent(launcher::launch)
        }
    }

    private fun showBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun validateAndRegister(binding: RegFragmentBinding) {
        if (binding.fieldLogin.editText?.text.isNullOrEmpty() ||
            binding.fieldPass.editText?.text.isNullOrEmpty() ||
            binding.fieldConfirm.editText?.text.isNullOrEmpty() ||
            binding.fieldName.editText?.text.isNullOrEmpty()
        ) {
            showBar(getString(R.string.error_empty_fields))
            return
        }

        if (binding.fieldConfirm.editText?.text.toString() != binding.fieldPass.editText?.text.toString()) {
            showBar(getString(R.string.error_password_mismatch))
            return
        }

        upload?.let {
            val login = binding.fieldLogin.editText?.text.toString()
            val pass = binding.fieldPass.editText?.text.toString()
            val name = binding.fieldName.editText?.text.toString()
            viewModel.getRegFromServer(login, pass, name, it)
            pressBtn = true
        } ?: run {
            showBar(getString(R.string.error_photo_required))
        }
    }

    private fun observeDataState(binding: RegFragmentBinding) {
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FeedModelState.Loading -> {
                    binding.statusReg.isVisible = true
                }

                is FeedModelState.Error -> {
                    showBar(getString(R.string.error_occurred))
                    binding.statusReg.isVisible = false
                }

                is FeedModelState.Success<*> -> {
                    // Для успешного состояния
                    binding.statusReg.isVisible = false
                    // Можете добавить обработку успешной аутентификации здесь
                }

                else -> {
                }
            }
        }
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
        curFrag?.getCurFragmentAttach(getString(R.string.sign_up))
    }
}