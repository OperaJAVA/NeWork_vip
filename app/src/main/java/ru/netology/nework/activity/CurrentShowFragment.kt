package ru.netology.nework.activity

/**
 * Интерфейс для управления состоянием фрагмента,
 * который может отображать заголовок и управлять его видимостью.
 */
interface CurrentShowFragment {

    /**
     * Вызывается при прикреплении фрагмента.
     * @param headerTitle Заголовок, который нужно установить.
     */
    fun getCurFragmentAttach(headerTitle: String)

    /**
     * Вызывается при отсоединении фрагмента.
     */
    fun getCurFragmentDetach()
}