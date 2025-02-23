package ru.netology.nework.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.uriArg
import ru.netology.nework.databinding.FragmentSpacePhotoBinding
import ru.netology.nework.error.UnknownError

class SpacePhoto : Fragment() {

    private var curFrag: CurrentShowFragment? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Инфлейтинг разметки фрагмента
        val binding = FragmentSpacePhotoBinding.inflate(inflater, container, false)
        val uri = arguments?.uriArg // Получение URI из аргументов

        // Загрузка изображения с помощью Glide
        Glide.with(binding.viewSpaceFoto)
            .load(uri)
            .error(R.drawable.err_load) // Установка изображения по умолчанию в случае ошибки
            .into(binding.viewSpaceFoto)

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            curFrag = context as CurrentShowFragment // Привязка к текущему контексту
        } catch (e: ClassCastException) {
            throw UnknownError // Обработка ошибки привязки
        }
    }

    override fun onDetach() {
        super.onDetach()
        curFrag?.getCurFragmentDetach() // Уведомление о отсоединении
        curFrag = null
    }

    override fun onStart() {
        super.onStart()
        curFrag?.getCurFragmentAttach("Pic") // Уведомление о присоединении фрагмента
    }
}