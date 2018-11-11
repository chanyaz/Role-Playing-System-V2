package com.alekseyvalyakin.roleplaysystem.ribs.game.active.log

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.alekseyvalyakin.roleplaysystem.R
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.log.adapter.LogAdapter
import com.alekseyvalyakin.roleplaysystem.utils.*
import com.alekseyvalyakin.roleplaysystem.views.ButtonsView
import com.alekseyvalyakin.roleplaysystem.views.SearchToolbar
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxrelay2.PublishRelay
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import java.util.concurrent.TimeUnit

class LogView constructor(
        context: Context
) : _LinearLayout(context), LogPresenter {

    private var searchToolbar: SearchToolbar
    private val recyclerView: RecyclerView
    private val relay = PublishRelay.create<LogPresenter.UiEvent>()
    private val flexibleAdapter = LogAdapter(emptyList(), relay)
    private lateinit var textDisposable: Disposable
    private val rxPermissions = RxPermissions(context as FragmentActivity)
    private lateinit
    var input: EditText
    private lateinit var inputActions: ViewGroup
    private lateinit var sendBtn: View
    private lateinit var micBtn: View
    private val smoothScroller = object : LinearSmoothScroller(getContext()) {
        override fun getVerticalSnapPreference(): Int {
            return LinearSmoothScroller.SNAP_TO_START
        }
    }

    init {
        orientation = VERTICAL
        clipChildren = false

        searchToolbar = searchToolbar({
            id = R.id.search_view
            setTitle(getString(R.string.records))
        }, SearchToolbar.Mode.HIDDEN).lparams(width = matchParent, height = wrapContent) {}

        buttonsView({

        }, listOf(ButtonsView.ButtonInfo(getString(R.string.texts), View.OnClickListener {
            relay.accept(LogPresenter.UiEvent.OpenTexts)
        }), ButtonsView.ButtonInfo(getString(R.string.audio), View.OnClickListener {
            Observable.just(LogPresenter.UiEvent.OpenAudio).requestPermissionsExternalReadWriteAndAudioRecord(rxPermissions)
                    .subscribeWithErrorLogging { relay.accept(LogPresenter.UiEvent.OpenAudio) }
        }))).lparams(matchParent, wrapContent) {
            val horMargin = getDoubleCommonDimen()
            val vMargin = getCommonDimen()
            setMargins(horMargin, vMargin, horMargin, vMargin)
        }

        relativeLayout {
            id = R.id.send_form
            backgroundResource = R.drawable.white_cornered_background
            elevation = getFloatDimen(R.dimen.dp_16)
            minimumHeight = getIntDimen(R.dimen.dp_48)
            inputActions = linearLayout {
                orientation = HORIZONTAL
                id = R.id.input_actions
                leftPadding = getCommonDimen()
                rightPadding = getCommonDimen()

                sendBtn = imageView {
                    increaseTouchArea()
                    padding = getCommonDimen()
                    backgroundResource = getSelectableItemBorderless()
                    tintImageRes(R.color.colorTextSecondary)
                    imageResource = R.drawable.ic_send_black_24dp
                }.lparams(width = dimen(R.dimen.dp_40), height = dimen(R.dimen.dp_40)) {
                    gravity = Gravity.CENTER
                }
                micBtn = imageView {
                    increaseTouchArea()
                    padding = getCommonDimen()
                    backgroundResource = getSelectableItemBorderless()
                    tintImageRes(R.color.colorTextSecondary)
                    imageResource = R.drawable.ic_mic
                }.lparams(width = dimen(R.dimen.dp_40), height = dimen(R.dimen.dp_40)) {
                    gravity = Gravity.CENTER
                }
            }.lparams(height = dimen(R.dimen.dp_48)) {
                alignParentRight()
                centerVertically()
            }

            input = editText {
                id = R.id.input
                background = null
                hintResource = R.string.input_something
                maxLines = 3
            }.lparams(width = matchParent) {
                centerVertically()
                leftMargin = getDoubleCommonDimen()
                leftOf(R.id.input_actions)
            }
        }.lparams(width = matchParent, height = wrapContent) {
            leftMargin = getDoubleCommonDimen()
            rightMargin = getDoubleCommonDimen()
        }

        recyclerView = recyclerView {
            id = R.id.recycler_view
            clipToPadding = false
            clipChildren = false
            layoutManager = LinearLayoutManager(context)
            adapter = flexibleAdapter
        }.lparams(width = matchParent, height = matchParent) {
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        textDisposable = RxTextView.textChanges(input).subscribeWithErrorLogging {
            sendBtn.visibility = if (it.toString().isBlank()) View.GONE else View.VISIBLE
            micBtn.visibility = if (it.toString().isBlank()) View.VISIBLE else View.GONE
        }
    }

    override fun onDetachedFromWindow() {
        textDisposable.dispose()
        super.onDetachedFromWindow()
    }

    override fun update(viewModel: LogViewModel) {
        flexibleAdapter.updateWithAnimateToStartOnNewItem(
                recyclerView,
                smoothScroller,
                viewModel.items
        )
    }

    override fun clearSearchInput() {
        searchToolbar.clearInput()
    }

    override fun observeUiEvents(): Observable<LogPresenter.UiEvent> {
        return Observable.merge(sendMessage(), startRecording(), observeSearchInput(), relay)
    }

    private fun sendMessage(): Observable<LogPresenter.UiEvent.SendTextMessage> {
        return RxView.clicks(sendBtn).map {
            val text = input.text.toString()
            input.text = null
            LogPresenter.UiEvent.SendTextMessage(text)
        }
    }

    private fun startRecording(): Observable<LogPresenter.UiEvent.StartRecording> {
        return RxView.clicks(micBtn).map {
            LogPresenter.UiEvent.StartRecording
        }
    }

    private fun observeSearchInput(): Observable<LogPresenter.UiEvent.SearchInput> = searchToolbar.observeSearchInput()
            .debounce(200L, TimeUnit.MILLISECONDS, Schedulers.computation())
            .throttleLast(100L, TimeUnit.MILLISECONDS, Schedulers.computation())
            .map { LogPresenter.UiEvent.SearchInput(it.toString()) }
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())

}
