package ru.netology.nework.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.ActivityAppBinding
import ru.netology.nework.date.DateJob
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dialog.ListenerDialogSelectDate
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.util.EventArg
import ru.netology.nework.util.ListUserArg
import ru.netology.nework.util.LongEditArg
import ru.netology.nework.util.PostArg
import ru.netology.nework.util.StringArg
import ru.netology.nework.util.UserArg
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.AuthViewModel.Companion.DIALOG_IN
import ru.netology.nework.viewmodel.AuthViewModel.Companion.DIALOG_OUT
import ru.netology.nework.viewmodel.AuthViewModel.Companion.DIALOG_REG
import ru.netology.nework.viewmodel.AuthViewModel.Companion.userAuth
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

@AndroidEntryPoint
class AppActivity : AppCompatActivity(), DialogAuth.ReturnSelection, CurrentShowFragment,
    ListenerDialogSelectDate {

    private var actionBar: ActionBar? = null
    private var imageView: ImageView? = null
    private val viewModel: AuthViewModel by viewModels()

    companion object {
        var Bundle.idArg: Long by LongEditArg
        var Bundle.uriArg: String? by StringArg
        var Bundle.userArg: UserResponse? by UserArg
        var Bundle.listUserArg: List<UserResponse>? by ListUserArg
        var Bundle.postArg: Post? by PostArg
        var Bundle.eventArg: Event? by EventArg
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupBackButtonHandler()
        setupImageView()
    }

    private fun setupActionBar() {
        actionBar = supportActionBar
        imageView = ImageView(this).apply {
            setImageResource(R.drawable.icon_person_24)
        }

        val lp = ActionBar.LayoutParams(
            ActionBar.LayoutParams.WRAP_CONTENT,
            ActionBar.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END
        }

        actionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setCustomView(imageView, lp)
        }
    }

    private fun setupBackButtonHandler() {
        this@AppActivity.onBackPressedDispatcher.addCallback(this@AppActivity, callback)
    }

    private fun setupImageView() {
        imageView?.setOnClickListener {
            PopupMenu(this, it).apply {
                inflate(R.menu.menu_main)
                menu.setGroupVisible(R.id.authenticated, userAuth)
                menu.setGroupVisible(R.id.unauthenticated, !userAuth)
                setOnMenuItemClickListener { menuItem ->
                    handleMenuItemClick(menuItem.itemId)
                }
            }.show()
        }
    }

    private fun handleMenuItemClick(itemId: Int): Boolean {
        return when (itemId) {
            R.id.signin -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.authFragment)
                true
            }

            R.id.signup -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.regFragment)
                true
            }

            R.id.signout -> {
                DialogAuth.newInstance(DIALOG_OUT, getString(R.string.logout_confirmation))
                    .show(supportFragmentManager, null)
                true
            }

            R.id.account -> {
                if (userAuth) {
                    val myAcc = viewModel.getMyAcc()
                    findNavController(R.id.nav_host_fragment).navigate(
                        R.id.userAccount,
                        Bundle().apply {
                            userArg = myAcc
                        })
                }
                true
            }

            else -> false
        }
    }

    override fun returnDialogValue(select: Int) {
        when (select) {
            DIALOG_OUT -> {
                viewModel.deleteAuth()
                Toast.makeText(
                    this@AppActivity,
                    getString(R.string.logout_message),
                    Toast.LENGTH_LONG
                ).show()
            }

            DIALOG_IN -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.authFragment)
            }

            DIALOG_REG -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.regFragment)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.setDisplayHomeAsUpEnabled(destination.id != R.id.screenPosts)
        }
        invalidateOptionsMenu()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun getCurFragmentAttach(headerTitle: String) {
        actionBar?.title = headerTitle
        imageView?.visibility = View.GONE
    }

    override fun getCurFragmentDetach() {
        actionBar?.title = getString(R.string.app_name)
        imageView?.visibility = View.VISIBLE
    }

    private val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

    override fun returnDateJob(date: DateJob) {
        val currentFragment = supportFragmentManager.currentNavigationFragment
        if (currentFragment is NewJob) currentFragment.getDateJob(date)
    }

    override fun returnIdJob(id: Long) {
        val currentFragment = supportFragmentManager.currentNavigationFragment
        if (currentFragment is UserAccount) currentFragment.getIdJob(id)
    }

    private val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when (val currentFragment = supportFragmentManager.currentNavigationFragment) {
                is EventView -> currentFragment.stopMedia()
                is PostView -> currentFragment.stopMedia()
                is ScreenPosts -> finish()
            }
            findNavController(R.id.nav_host_fragment).navigateUp()
        }
    }
}