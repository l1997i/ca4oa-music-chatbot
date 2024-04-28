package com.luisli.cagpt.ui.splash
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.luisli.cagpt.BuildConfig
import com.luisli.cagpt.R
import com.luisli.cagpt.databinding.FragmentSplashBinding
import com.luisli.cagpt.ui.base.BaseFragment
import com.luisli.cagpt.ui.viewmodels.SplashState
import com.luisli.cagpt.ui.viewmodels.SplashViewModel
import com.luisli.cagpt.utils.navigate
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class SplashFragment : BaseFragment<SplashViewModel, FragmentSplashBinding>() {
    override val mViewModel: SplashViewModel by viewModels()
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSplashBinding {
        return FragmentSplashBinding.inflate(inflater, container, false)
    }
    override fun setupUI() {
        mViewBinding.apply {
            lifecycleOwner = viewLifecycleOwner
            tvAppVersion.text = getString(R.string.label_app_version, BuildConfig.VERSION_NAME)
        }
    }
    override fun initializeObserver() {
        mViewModel.splashStateLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is SplashState.SplashScreen -> {
                    navigate(SplashFragmentDirections.actionSplashFragmentToChatFragment())
                }
            }
        }
    }
}