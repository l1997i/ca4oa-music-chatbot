package com.luisli.cagpt.ui.settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.luisli.cagpt.R
import com.luisli.cagpt.databinding.FragmentFaqBinding
import com.luisli.cagpt.ui.base.BaseFragment
import com.luisli.cagpt.ui.viewmodels.EmptyViewModel
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class FaqFragment : BaseFragment<EmptyViewModel, FragmentFaqBinding>() {
    private lateinit var expandableListViewAdapter: ExpandableListViewAdapter
    private lateinit var listDataGroup: MutableList<String>
    private lateinit var listDataChild: HashMap<String, List<String>>
    override val mViewModel: EmptyViewModel by viewModels()
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFaqBinding {
        return FragmentFaqBinding.inflate(inflater, container, false)
    }
    override fun setupUI() {
        mViewBinding.apply {
            lifecycleOwner = viewLifecycleOwner
            (activity as? AppCompatActivity)?.setSupportActionBar(layoutToolbar.toolbar)
            layoutToolbar.toolbar.setNavigationIcon(R.drawable.ic_back_white)
            layoutToolbar.tvToolbarTitle.text = getString(R.string.toolbar_title_faqs)
            layoutToolbar.toolbar.setNavigationOnClickListener {
                onBackNavigation()
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                onBackNavigation()
            }
                        elvFaq.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
                                false
            }
                        elvFaq.setOnGroupExpandListener { groupPosition ->
                //requireContext().showToast(listDataGroup[groupPosition] + " " + getString(R.string.text_expanded))
            }
                        elvFaq.setOnGroupCollapseListener { groupPosition ->
                //requireContext().showToast(listDataGroup[groupPosition] + " " + getString(R.string.text_collapsed))
            }
                        initObjects()
                        initListData()
        }
    }
    private fun initObjects() {
                listDataGroup = ArrayList()
                listDataChild = HashMap()
                expandableListViewAdapter =
            ExpandableListViewAdapter(requireContext(), listDataGroup, listDataChild)
                mViewBinding.elvFaq.setAdapter(expandableListViewAdapter)
    }
    private fun initListData() {
                listDataGroup.add(getString(R.string.first_row))
        listDataGroup.add(getString(R.string.second_row))
        listDataGroup.add(getString(R.string.third_row))
        listDataGroup.add(getString(R.string.fourth_row))
        listDataGroup.add(getString(R.string.fifth_row))
        listDataGroup.add(getString(R.string.sixth_row))
                val firstRowList: MutableList<String> = ArrayList()
        var mArray: Array<String> = resources.getStringArray(R.array.string_array_first_row)
        for (item in mArray) {
            firstRowList.add(item)
        }
                val secondRowList: MutableList<String> = ArrayList()
        mArray = resources.getStringArray(R.array.string_array_second_row)
        for (item in mArray) {
            secondRowList.add(item)
        }
                val thirdRowList: MutableList<String> = ArrayList()
        mArray = resources.getStringArray(R.array.string_array_third_row)
        for (item in mArray) {
            thirdRowList.add(item)
        }
                val fourthRowList: MutableList<String> = ArrayList()
        mArray = resources.getStringArray(R.array.string_array_fourth_row)
        for (item in mArray) {
            fourthRowList.add(item)
        }
                val fifthRowList: MutableList<String> = ArrayList()
        mArray = resources.getStringArray(R.array.string_array_fifth_row)
        for (item in mArray) {
            fifthRowList.add(item)
        }
                val sixthRowList: MutableList<String> = ArrayList()
        mArray = resources.getStringArray(R.array.string_array_sixth_row)
        for (item in mArray) {
            sixthRowList.add(item)
        }
                listDataChild[listDataGroup[0]] = firstRowList
        listDataChild[listDataGroup[1]] = secondRowList
        listDataChild[listDataGroup[2]] = thirdRowList
        listDataChild[listDataGroup[3]] = fourthRowList
        listDataChild[listDataGroup[4]] = fifthRowList
        listDataChild[listDataGroup[5]] = sixthRowList
                expandableListViewAdapter.notifyDataSetChanged()
    }
    private fun onBackNavigation() {
        findNavController().popBackStack()
    }
}