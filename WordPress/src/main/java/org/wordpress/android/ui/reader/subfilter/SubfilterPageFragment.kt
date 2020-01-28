package org.wordpress.android.ui.reader.subfilter

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.Tab
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.stats_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.wordpress.android.R
import org.wordpress.android.ui.ActionableEmptyView
import org.wordpress.android.ui.pages.PageListFragment
import org.wordpress.android.ui.pages.PagesPagerAdapter
import org.wordpress.android.ui.pages.PagesPagerAdapter.Companion
import org.wordpress.android.ui.reader.ReaderActivityLauncher
import org.wordpress.android.ui.reader.ReaderEvents
import org.wordpress.android.ui.reader.ReaderSubsActivity
import org.wordpress.android.ui.reader.services.update.ReaderUpdateLogic.UpdateTask
import org.wordpress.android.ui.reader.services.update.ReaderUpdateServiceStarter
import org.wordpress.android.ui.reader.subfilter.SubfilterCategory.SITES
import org.wordpress.android.ui.reader.subfilter.SubfilterCategory.TAGS
import org.wordpress.android.ui.reader.subfilter.SubfilterListItem.ItemType
import org.wordpress.android.ui.reader.subfilter.SubfilterListItem.ItemType.SITE
import org.wordpress.android.ui.reader.subfilter.SubfilterListItem.ItemType.TAG
import org.wordpress.android.ui.reader.subfilter.adapters.SubfilterListAdapter
import org.wordpress.android.ui.reader.viewmodels.ReaderPostListViewModel
import org.wordpress.android.ui.stats.refresh.StatsPagerAdapter
import org.wordpress.android.ui.stats.refresh.StatsViewModel
import org.wordpress.android.ui.stats.refresh.lists.StatsListFragment
import org.wordpress.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection
import org.wordpress.android.ui.utils.UiHelpers
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.AppLog.T
import org.wordpress.android.util.NetworkUtils
import org.wordpress.android.util.WPSwipeToRefreshHelper
import org.wordpress.android.viewmodel.pages.PageListViewModel.PageListType
import java.lang.ref.WeakReference
import java.util.EnumSet
import javax.inject.Inject

class SubfilterPageFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ReaderPostListViewModel
    @Inject lateinit var uiHelpers: UiHelpers
    private lateinit var recyclerView: RecyclerView
    //private lateinit var emptyView: ActionableEmptyView

    companion object {
        const val CATEGORY_KEY = "category_key"

        fun newInstance(category: SubfilterCategory): SubfilterPageFragment {
            val fragment = SubfilterPageFragment()
            val bundle = Bundle()
            bundle.putSerializable(CATEGORY_KEY, category)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //setHasOptionsMenu(true)
        return inflater.inflate(R.layout.add_content_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val nonNullActivity = checkNotNull(activity)


        val category = arguments?.getSerializable(CATEGORY_KEY) as SubfilterCategory

        recyclerView = view.findViewById(R.id.content_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = SubfilterListAdapter(uiHelpers)

        //emptyView = view.findViewById(R.id.actionable_empty_view)

        //when(category) {
        //    SITES -> recyclerView.setBackgroundColor(resources.getColor(R.color.yellow))
        //    TAGS -> recyclerView.setBackgroundColor(resources.getColor(R.color.blue))
        //}

        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(/*category.name, */ReaderPostListViewModel::class.java)

        viewModel.subFilters.observe(this, Observer {
           (recyclerView.adapter as? SubfilterListAdapter)?.let { adapter ->
               val items = it?.filter { it.type == category.type } ?: listOf()
               manageEmptyView(items.size == 0, category)

               adapter.update(items)
               viewModel.updateTabTitle(category, items.size)


           }
        })
        performUpdate(/*category.task*/)
        viewModel.loadSubFilters(/*category*/)

       //initializeViewModels(nonNullActivity, savedInstanceState == null, savedInstanceState)
       //initializeViews(nonNullActivity)
    }

    fun setNestedScrollBehavior(enable: Boolean) {
        if (!isAdded) return

        recyclerView.isNestedScrollingEnabled = enable
    }

     @Subscribe(threadMode = ThreadMode.MAIN)
     fun onEventMainThread(event: ReaderEvents.FollowedTagsChanged) {
         AppLog.d(T.READER, "Subfilter bottom sheet > followed tags changed")
         viewModel.loadSubFilters(/*TAGS*/)
     }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ReaderEvents.FollowedBlogsChanged) {
        AppLog.d(T.READER, "Subfilter bottom sheet > followed blogs changed")
        viewModel.loadSubFilters(/*SITES*/)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    private fun performUpdate(/*task: UpdateTask*/) {
        performUpdate(EnumSet.of(
                UpdateTask.TAGS,
                UpdateTask.FOLLOWED_BLOGS
        ))
    }

    private fun performUpdate(tasks: EnumSet<UpdateTask>) {
        if (!NetworkUtils.isNetworkAvailable(activity)) {
            return
        }

        ReaderUpdateServiceStarter.startService(activity, tasks)
    }

    private fun manageEmptyView(isEmpty: Boolean, category: SubfilterCategory) {
        if (!isAdded || view == null) {
            return
        }

        val actionableEmptyView = view?.findViewById<ActionableEmptyView>(R.id.actionable_empty_view) ?: return

        if (isEmpty) {
            actionableEmptyView.apply{
                visibility = View.VISIBLE
                image.visibility = View.GONE
                title.setText(if (category == SITES) R.string.reader_filter_empty_sites_list else R.string.reader_filter_empty_tags_list)
                subtitle.setText(if (category == SITES) R.string.reader_filter_empty_sites_list else R.string.reader_filter_empty_tags_list)
                button.setText(if (category == SITES) R.string.reader_filter_empty_sites_action else R.string.reader_filter_empty_tags_action)
                button.visibility = View.VISIBLE
                actionableEmptyView.button.setOnClickListener {
                    //ReaderActivityLauncher.showReaderSubs(
                    //        requireActivity(),
                    //        if (category == SITES)
                    //            ReaderSubsActivity.TAB_IDX_FOLLOWED_BLOGS
                    //        else
                    //            ReaderSubsActivity.TAB_IDX_FOLLOWED_TAGS
                    //)
                    viewModel.onBottomSheetActionClicked(
                            if (category == SITES)
                                ReaderSubsActivity.TAB_IDX_FOLLOWED_BLOGS
                            else
                                ReaderSubsActivity.TAB_IDX_FOLLOWED_TAGS
                    )
                }
            }
        } else {
            actionableEmptyView.visibility = View.GONE
        }
    }
}


class SubfilterPagerAdapter(val context: Context, val fm: FragmentManager) : FragmentPagerAdapter(fm) {
    private val filterCategory = listOf(SITES, TAGS)
    private val fragments = mutableMapOf<SubfilterCategory, WeakReference<SubfilterPageFragment>>()

    override fun getCount(): Int = filterCategory.size


    override fun getItem(position: Int): Fragment {
        val fragment = SubfilterPageFragment.newInstance(filterCategory[position])
        fragments[filterCategory[position]] = WeakReference(fragment)
        return fragment

        //return when(position) {
        //    1 -> SubfilterPageFragment.newInstance(filterCategory[position])
        //    else -> SubfilterPageFragment.newInstance(filterCategory[position])
        //}

        //return SubfilterPageFragment.newInstance(filterCategory[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(filterCategory[position].titleRes)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        for (i in 0 until fragments.size) {
            val fragment = fragments[filterCategory[i]]?.get()
            fragment?.setNestedScrollBehavior(i == position)
        }
        container.requestLayout()
    }
}

enum class SubfilterCategory(@StringRes val titleRes: Int, val task: UpdateTask, val type: ItemType) {
    SITES(R.string.reader_filter_sites_title, UpdateTask.FOLLOWED_BLOGS, SITE),
    TAGS(R.string.reader_filter_tags_title, UpdateTask.TAGS, TAG)
}


//class SubfilterCategoryListener(val viewModel: ReaderPostListViewModel) : OnTabSelectedListener {
//    override fun onTabReselected(tab: Tab?) {
//    }
//
//    override fun onTabUnselected(tab: Tab?) {
//    }
//
//    override fun onTabSelected(tab: Tab) {
//        //viewModel.loadSubFilters() //.onSectionSelected(statsSections[tab.position])
//    }
//}
