package com.alekseyvalyakin.roleplaysystem.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.alekseyvalyakin.roleplaysystem.R
import com.alekseyvalyakin.roleplaysystem.utils.*
import com.alekseyvalyakin.roleplaysystem.utils.StringUtils.EMPTY_STRING
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.jetbrains.anko.*


@SuppressLint("ViewConstructor")
class SearchToolbar constructor(
        context: Context,
        private val mode: Mode = Mode.CLASSIC
) : _FrameLayout(context) {

    private lateinit var bgImageView: ImageView
    private lateinit var clearIcon: ImageView
    private lateinit var leftIcon: ImageView
    private lateinit var rightIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvHiddenTitle: TextView
    private lateinit var searchEditText: EditText
    private lateinit var hiddenContainer: RelativeLayout
    private lateinit var searchContainer: ViewGroup
    private var isSearchMode = false
    private val relay = PublishRelay.create<Boolean>()

    init {
        AnkoContext.createDelegate(this).apply {
            elevation = getFloatDimen(R.dimen.dp_16)
            outlineProvider = ViewOutlineProvider.BOUNDS

            frameLayout {
                minimumHeight = getIntDimen(R.dimen.dp_56)

                bgImageView = imageView {
                    id = R.id.toolbar_background_image_view
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    imageResource = R.drawable.top_background
                }.lparams(width = matchParent, height = 0)

                searchContainer = relativeLayout {
                    id = R.id.top_toolbar_container
                    backgroundResource = R.drawable.white_cornered_background
                    clipChildren = false
                    topPadding = getIntDimen(R.dimen.dp_4)
                    bottomPadding = getIntDimen(R.dimen.dp_4)
                    visibility = if (mode == Mode.CLASSIC) View.VISIBLE else View.INVISIBLE


                    leftIcon = imageView {
                        id = R.id.action_icon
                        padding = getIntDimen(R.dimen.dp_8)
                        setBackgroundResource(getSelectableItemBorderless())
                        setImageDrawable(getCompatDrawable(R.drawable.ic_search))
                    }.lparams(width = getIntDimen(R.dimen.dp_40), height = getIntDimen(R.dimen.dp_40)) {
                        centerVertically()
                        leftMargin = getIntDimen(R.dimen.dp_8)
                        marginStart = getIntDimen(R.dimen.dp_8)
                    }

                    linearLayout {
                        orientation = LinearLayout.HORIZONTAL
                        id = R.id.right_container
                        clearIcon = imageView {
                            id = R.id.clear_icon
                            padding = getIntDimen(R.dimen.dp_8)
                            setBackgroundResource(getSelectableItemBorderless())
                            tintImageRes(R.color.grey24Color)
                            visibility = View.GONE
                            setImageDrawable(getCompatDrawable(R.drawable.ic_close))
                        }.lparams(width = getIntDimen(R.dimen.dp_40), height = getIntDimen(R.dimen.dp_40)) {
                            leftMargin = getIntDimen(R.dimen.dp_8)
                            marginStart = getIntDimen(R.dimen.dp_8)
                        }

                        rightIcon = imageView {
                            id = R.id.more
                            padding = getIntDimen(R.dimen.dp_8)
                            setBackgroundResource(getSelectableItemBorderless())
                            tintImageRes(R.color.blackColor54)
                            setImageDrawable(getCompatDrawable(R.drawable.ic_more_vertical))
                        }.lparams(width = getIntDimen(R.dimen.dp_40), height = getIntDimen(R.dimen.dp_40)) {
                        }
                    }.lparams(wrapContent, wrapContent) {
                        alignParentEnd()
                        centerVertically()
                    }

                    tvTitle = textView {
                        id = R.id.tv_title
                        text = resources.getString(R.string.app_name)
                        textColor = getCompatColor(R.color.colorTextSecondary)
                        setTextSizeFromRes(R.dimen.dp_16)
                    }.lparams {
                        centerVertically()
                        leftMargin = getIntDimen(R.dimen.dp_16)
                        marginStart = getIntDimen(R.dimen.dp_16)
                        leftOf(R.id.right_container)
                        rightOf(R.id.action_icon)
                    }

                    searchEditText = editText {
                        id = R.id.search_view
                        backgroundColor = Color.TRANSPARENT
                        hint = resources.getString(R.string.search)
                        textColor = getCompatColor(R.color.colorTextPrimary)
                        setTextSizeFromRes(R.dimen.dp_16)
                        singleLine = true
                        imeOptions = EditorInfo.IME_ACTION_SEARCH
                        visibility = View.INVISIBLE
                        setOnEditorActionListener { _, actionId, _ ->
                            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                hideKeyboard()
                                return@setOnEditorActionListener true
                            }
                            false
                        }
                    }.lparams {
                        centerVertically()
                        leftMargin = getIntDimen(R.dimen.dp_16)
                        marginStart = getIntDimen(R.dimen.dp_16)
                        rightOf(R.id.action_icon)
                        leftOf(R.id.right_container)
                    }
                }.lparams(width = matchParent, height = wrapContent) {
                    val margin = getIntDimen(R.dimen.dp_8)
                    setMargins(margin, context.getStatusBarHeight(), margin, margin)
                }

                hiddenContainer = relativeLayout {
                    visibility = if (mode == Mode.HIDDEN) View.VISIBLE else View.GONE
                    setOnClickListener {
                        isSearchMode = true
                        initSearchMode()
                    }
                    tvHiddenTitle = textView {
                        setSanserifMediumTypeface()
                        textColorResource = R.color.colorWhite
                        textSizeDimen = R.dimen.dp_20
                    }.lparams(wrapContent, wrapContent) {
                        leftMargin = getDoubleCommonDimen()
                        centerVertically()
                    }

                    imageView {
                        imageResource = R.drawable.ic_search
                        tintImageRes(R.color.colorWhite)
                    }.lparams(getIntDimen(R.dimen.dp_24), getIntDimen(R.dimen.dp_24)) {
                        rightMargin = getDoubleCommonDimen()
                        centerVertically()
                        alignParentEnd()
                    }

                }.lparams(matchParent, wrapContent) {
                    topMargin = getStatusBarHeight()
                }
            }.lparams(width = matchParent, height = wrapContent)
        }

        leftIcon.setOnClickListener {
            isSearchMode = !isSearchMode
            if (isSearchMode) {
                initSearchMode()
            } else {
                turnOffSearchMode()
            }
        }
        searchContainer.setOnClickListener {
            if (!isSearchMode) {
                isSearchMode = true
                initSearchMode()
            }
        }

        clearIcon.setOnClickListener {
            searchEditText.setText("")
        }

    }

    fun setTitle(text: CharSequence) {
        tvTitle.text = text
        tvHiddenTitle.text = text
    }

    fun observeRightImageClick(): Observable<Any> {
        return RxView.clicks(rightIcon)
    }

    fun observeSearchInput(): Observable<CharSequence> {
        return RxTextView.textChanges(searchEditText).doOnNext { s ->
            if (s.isNullOrEmpty()) {
                clearIcon.visibility = View.GONE
            } else {
                clearIcon.visibility = View.VISIBLE
            }
        }
    }

    fun hideRightIcon() {
        rightIcon.visibility = View.GONE
    }

    fun showRightIcon() {
        rightIcon.visibility = View.VISIBLE
    }

    fun getPopupViewAnchor(): View {
        return rightIcon
    }

    fun observeSearchModeToggle(): Observable<Boolean> {
        return relay
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (measuredHeight != bgImageView.measuredHeight) {
            bgImageView.post {
                bgImageView.layoutParams.height = measuredHeight
                hiddenContainer.layoutParams.height = measuredHeight - getStatusBarHeight()
                bgImageView.requestLayout()
            }
        }
    }

    private fun turnOffSearchMode() {
        searchEditText.setText("")
        tvTitle.visibility = View.VISIBLE
        searchEditText.visibility = View.INVISIBLE
        leftIcon.hideKeyboard()
        leftIcon.setImageDrawable(getCompatDrawable(R.drawable.ic_search))
        if (mode == Mode.HIDDEN) {
            searchContainer.visibility = View.INVISIBLE
            hiddenContainer.visibility = View.VISIBLE
        }
        relay.accept(false)
    }

    private fun initSearchMode() {
        leftIcon.setImageDrawable(getCompatDrawable(R.drawable.ic_arrow_back))
        searchEditText.visibility = View.VISIBLE
        searchEditText.showSoftKeyboard()
        tvTitle.visibility = View.INVISIBLE
        if (mode == Mode.HIDDEN) {
            searchContainer.visibility = View.VISIBLE
            hiddenContainer.visibility = View.GONE
        }
        relay.accept(true)
    }

    fun clearInput() {
        searchEditText.setText(EMPTY_STRING)
    }

    enum class Mode {
        CLASSIC,
        HIDDEN
    }
}

