package com.ymy.appnest.fragment

import android.os.Handler
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ymy.appnest.ConfigManager
import com.ymy.appnest.R
import com.ymy.appnest.adapter.BottomNavigationAdapter
import com.ymy.appnest.beans.HomeTabType
import com.ymy.appnest.databinding.FragmentHomeBottomNavigationBinding
import com.ymy.appnest.fragment.mine.TabMineV2Fragment
import com.ymy.core.base.*
import com.ymy.core.router.ARouterHelper
import com.ymy.core.router.RoutersIM
import com.ymy.web.custom.ui.WebViewFragment


/**
 * Created on 2020/7/10 08:46.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:首页Fragment
 */
class HomeTabFragment : BaseFragment(true), Refresher {

    val mBinding: FragmentHomeBottomNavigationBinding by lazy {
        FragmentHomeBottomNavigationBinding.inflate(layoutInflater)
    }

    override fun getBindingView(): View = mBinding.root

    override var needMobClick = false

    private val fragmentList = arrayListOf<Fragment>()

    private val mTabMineV2Fragment: TabMineV2Fragment by lazy {
        TabMineV2Fragment()
    }

    private val mTabEmptyFragment: TabEmptyFragment by lazy {
        TabEmptyFragment.newInstance(
            "",
            R.drawable.shape_main_list_bg,
            0,
            getColorCompat(R.color.white),
            false
        )
    }

    private val mIMFragment: RootFragment by lazy {
        ARouterHelper.getFragmentByPathWithPlaceHolder(RoutersIM.IM_MsgTabFragment,mTabEmptyFragment) as RootFragment
    }

    private val mBottomNavigationAdapter by lazy {
        BottomNavigationAdapter()
    }

    companion object {
        const val MSG_CLEAR_BACK_COUNT = 1
    }

    override fun getLayoutResId() = R.layout.fragment_home_bottom_navigation

    private val mHomeBottomNavigationList = ConfigManager.mainTabList

    private fun buildWebViewFragment(url: String, title: String = ""): Fragment =
        WebViewFragment.newInstance(
            url,
            title,
            true,
            showBackIcon = false,
            addTopPadding = true,
            showRefreshBar = false
        )

    private fun initFragmentList() {
        fragmentList.run {
            mHomeBottomNavigationList.forEach {
                var fragment: Fragment? = null
                fragment = when (it.type) {
                    HomeTabType.web -> {
                        buildWebViewFragment(it.url, it.name)
                    }
                    HomeTabType.mine -> {
                        mTabMineV2Fragment
                    }
                    HomeTabType.IM -> {
                        mIMFragment
                    }
                    else -> {
                        mTabEmptyFragment
                    }
                }
                fragment.run {
                    add(fragment)
                }
            }
        }
    }

    override fun initView() {
        initBottomNavigationBar()
        initFragmentList()
        initViewPager()
        mBinding.mainViewpager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                fragmentList[position].run {
                    if (this is Refresher) {
                        onRefresh()
                    }
                }
                activity?.run {
                    //状态栏文字不为白色时设置为白色
                    val flag =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    if (window.decorView.systemUiVisibility != flag) {
                        window.decorView.systemUiVisibility = flag
                    }
                }
            }
        })
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {

                /**
                 * Callback for handling the [OnBackPressedDispatcher.onBackPressed] event.
                 */
                override fun handleOnBackPressed() {
//                    if (!checkTabContactsCanPop()) {
                    onBackPressed()
//                    }
                }
            })
    }

    private fun initBottomNavigationBar() {
        mHomeBottomNavigationList.forEach {
            it.isSelected = false
        }
        mHomeBottomNavigationList[0].isSelected = true
        mBinding.navView.run {
            mBottomNavigationAdapter.setOnItemClickListener { adapter, view, position ->
                bottomNavigationIndexChange(position)
            }
            adapter = mBottomNavigationAdapter
            mBottomNavigationAdapter.setList(mHomeBottomNavigationList)
            layoutManager = GridLayoutManager(context, 5)
        }
    }

    private fun bottomNavigationIndexChange(
        position: Int
    ) {
        switchFragment(position)
        mBottomNavigationAdapter.data.forEach {
            it.isSelected = false
        }
        val item = mBottomNavigationAdapter.data[position]
        item.isSelected = true
        mBottomNavigationAdapter.notifyDataSetChanged()
    }

    private val handler = Handler {
        when (it.what) {
            MSG_CLEAR_BACK_COUNT -> {
                backCount = 0
            }
        }
        false
    }

    private var backCount = 0

    override fun onBackPressed(): Boolean {
        backCount++
        when (backCount) {
            1 -> {
                toast("再按一次退出")
            }
            else -> {
                activity?.finish()
            }
        }
        handler.removeMessages(MSG_CLEAR_BACK_COUNT)
        handler.sendEmptyMessageDelayed(MSG_CLEAR_BACK_COUNT, 3000)
        return true
    }

    override fun initData() {
//        getMsgTabUnread()
//        mHomeUnReadViewModel.mHomeLiveData.observe(this, Observer {
//            it?.run {
//                val resultCount = noticeUnreadCount + trainUnreadCount
//                setUnReadDot(TabType.tabType_train, resultCount)
//                LauncherBadgeHelper.trainingTabCount = resultCount
//                trainingFragment.setUnReadDot(noticeUnreadCount, trainUnreadCount)
//            }
//        })
    }

    var hasInitView = false
    override fun onResume() {
        super.onResume()
        hasInitView = true
//        mHomeUnReadViewModel.getTrainingTabUnreadCount()
    }

    fun setUnReadDot(index: Int, unRead: Int) {
        mBottomNavigationAdapter.data[index].unRead = unRead
        mBottomNavigationAdapter.notifyItemChanged(index)
    }

    var currentIndex = 0
    private fun switchFragment(position: Int): Boolean {
        currentIndex = position
        mBinding.mainViewpager.setCurrentItem(position, false)
        return true
    }

    private fun initViewPager() {
        mBinding.mainViewpager.isUserInputEnabled = false
        mBinding.mainViewpager.offscreenPageLimit = fragmentList.size
        mBinding.mainViewpager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragmentList[position]

            override fun getItemCount() = fragmentList.size
        }
    }

    override fun onRefresh() {
        fragmentList.forEach {
            if (it is Refresher) {
                it.onRefresh()
            }
        }
    }

    fun changeTabByIndex(index: Int, subIndex: Int) {
        if (!isAdded) {
            return
        }
        if (fragmentList.size > index - 1) {
            bottomNavigationIndexChange(index)
        }
        if (subIndex > -1) {
            val fragment = fragmentList[index]
            if (fragment is ChangeTaber) {
                fragment.changeTab(subIndex)
            }
        }
    }
}


object TabType {
    const val tabType_risk = "risk"
    const val tabType_msg = "msg"
    const val tabType_train = "train"
    const val tabType_publish = "publish"
    const val tabType_work_branch = "work_branch"
    const val tabType_mine = "mine"
}