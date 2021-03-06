package com.alekseyvalyakin.roleplaysystem.ribs.game.active.dice.diceresult

import com.uber.rib.core.ViewRouter

/**
 * Adds and removes children of {@link DiceResultBuilder.DiceResultScope}.
 *
 */
class DiceResultRouter(
        view: DiceResultView,
        interactor: DiceResultInteractor,
        component: DiceResultBuilder.Component
) : ViewRouter<DiceResultView, DiceResultInteractor, DiceResultBuilder.Component>(view, interactor, component)