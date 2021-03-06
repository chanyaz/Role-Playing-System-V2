package com.alekseyvalyakin.roleplaysystem.ribs.game.create

import com.alekseyvalyakin.roleplaysystem.data.firestore.game.Game
import com.alekseyvalyakin.roleplaysystem.di.activity.ActivityListener
import com.alekseyvalyakin.roleplaysystem.ribs.game.create.model.CreateGameProvider
import com.alekseyvalyakin.roleplaysystem.utils.reporter.AnalyticsReporter
import com.alekseyvalyakin.roleplaysystem.utils.subscribeWithErrorLogging
import com.jakewharton.rxrelay2.BehaviorRelay
import com.uber.rib.core.BaseInteractor
import com.uber.rib.core.Bundle
import com.uber.rib.core.RibInteractor
import com.uber.rib.core.getSerializable
import com.uber.rib.core.putSerializable
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.io.Serializable
import javax.inject.Inject

/**
 * Coordinates Business Logic for [CreateGameScope].
 *
 */
@RibInteractor
class CreateGameInteractor : BaseInteractor<CreateGameInteractor.CreateGamePresenter, CreateGameRouter>() {

    @Inject
    lateinit var presenter: CreateGamePresenter
    @Inject
    lateinit var viewModelProvider: CreateGameViewModelProvider
    @Inject
    lateinit var activityListener: ActivityListener
    @Inject
    lateinit var createGameProvider: CreateGameProvider
    @Inject
    lateinit var createGameListener: CreateGameListener
    @Inject
    lateinit var game: Game
    @Inject
    lateinit var analyticsReporter: AnalyticsReporter

    private val screenName = "CreateGame"
    private val model: BehaviorRelay<CreateGameViewModel> = BehaviorRelay.create()

    override fun didBecomeActive(savedInstanceState: Bundle?) {
        super.didBecomeActive(savedInstanceState)
        analyticsReporter.setCurrentScreen(screenName)

        initModel(savedInstanceState)
        presenter.updateFabShowDisposable(model.toFlowable(BackpressureStrategy.LATEST).toObservable())
                .addToDisposables()

        presenter.observeUiEvents()
                .concatMap(this::handleEvent)
                .subscribeWithErrorLogging {}
                .addToDisposables()

        createGameProvider.observeGame(model.toFlowable(BackpressureStrategy.LATEST))
                .subscribeWithErrorLogging { }
                .addToDisposables()

        presenter.updateView(model.value)
    }

    private fun handleEvent(event: CreateGameUiEvent): Observable<*> {
        val value = model.value
        when (event) {
            is CreateGameUiEvent.InputChange -> {
                model.accept(value.copy(inputText = event.text))
            }
            is CreateGameUiEvent.ClickNext -> {
                return handleClickNext(value, event)
            }
            is CreateGameUiEvent.BackPress -> {
                activityListener.backPress()
            }
            is CreateGameUiEvent.DeleteGame -> {
                presenter.showConfirmDeleteDialog()
            }
            is CreateGameUiEvent.ConfirmDeleteGame -> {
                analyticsReporter.logEvent(CreateGameAnalyticsEvent.DeleteGame(game))
                return createGameProvider.deleteGame().doOnComplete {
                    model.accept(model.value.copy(isDeleted = true))
                    activityListener.backPress()
                }.toObservable<Any>()
            }

        }
        return Observable.just(event)
    }

    private fun handleClickNext(value: CreateGameViewModel, event: CreateGameUiEvent.ClickNext): Observable<*> {
        val step = value.step
        val nextStep = step.getNextStep()

        return if (nextStep == CreateGameStep.NONE) {
            analyticsReporter.logEvent(CreateGameAnalyticsEvent.ActivateGame(game))
            createGameProvider.onChangeInfo(step, event.text).toObservable<Any>()
                    .doOnComplete {
                        createGameListener.onCreateGameEvent(CreateGameListener.CreateGameEvent.CompleteCreate(createGameProvider.getGame()))
                    }
        } else {
            analyticsReporter.logEvent(CreateGameAnalyticsEvent.ClickNext(game, step, nextStep))
            val newModel = viewModelProvider.getCreateGameViewModel(nextStep, createGameProvider.getGame())
            model.accept(newModel)
            presenter.updateView(newModel)
            createGameProvider.onChangeInfo(step, event.text).toObservable<Any>()
        }
    }

    override fun handleBackPress(): Boolean {
        val value = model.value
        val previousStep = value.step.getPreviousStep()

        if (value.isDeleted || previousStep == CreateGameStep.NONE) {
            return false
        }

        val newModel = viewModelProvider.getCreateGameViewModel(previousStep, createGameProvider.getGame())
        analyticsReporter.logEvent(CreateGameAnalyticsEvent.ClickBack(game, value.step, previousStep))

        model.accept(newModel)
        presenter.updateView(newModel)
        return true
    }

    private fun initModel(savedInstanceState: Bundle?) {
        model.accept(savedInstanceState?.getSerializable(CreateGameViewModel.KEY)
                ?: viewModelProvider.getCreateGameViewModel(createGameProvider.getGame()))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(CreateGameViewModel.KEY, model.value)
    }

    /**
     * Presenter interface implemented by this RIB's view.
     */
    interface CreateGamePresenter {
        fun updateView(createGameViewModel: CreateGameViewModel)
        fun updateFabShowDisposable(viewModelObservable: Observable<CreateGameViewModel>): Disposable
        fun observeUiEvents(): Observable<CreateGameUiEvent>
        fun showConfirmDeleteDialog()
    }
}
