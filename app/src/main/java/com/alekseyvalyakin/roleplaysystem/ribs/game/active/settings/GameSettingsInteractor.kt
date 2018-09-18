package com.alekseyvalyakin.roleplaysystem.ribs.game.active.settings

import com.uber.rib.core.BaseInteractor
import com.uber.rib.core.Bundle
import com.uber.rib.core.RibInteractor
import javax.inject.Inject

/**
 * Coordinates Business Logic for [GameSettingsScope].
 *
 */
@RibInteractor
class GameSettingsInteractor : BaseInteractor<GameSettingsPresenter, GameSettingsRouter>() {

    @Inject
    lateinit var presenter: GameSettingsPresenter
    @Inject
    lateinit var gameSettingsViewModelProvider: GameSettingsViewModelProvider

    override fun didBecomeActive(savedInstanceState: Bundle?) {
        super.didBecomeActive(savedInstanceState)
        presenter.update(gameSettingsViewModelProvider.getSettingsViewModel())

    }

    override fun willResignActive() {
        super.willResignActive()

    }


}