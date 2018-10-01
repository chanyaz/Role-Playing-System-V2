package com.alekseyvalyakin.roleplaysystem.ribs.game.active.settings

import com.alekseyvalyakin.roleplaysystem.ribs.game.active.settings.classes.GameSettingsClassBuilder
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.settings.classes.GameSettingsClassRouter
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.settings.stats.GameSettingsStatBuilder
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.settings.stats.GameSettingsStatRouter
import com.uber.rib.core.*

/**
 * Adds and removes children of {@link GameSettingsBuilder.GameSettingsScope}.
 *
 */
class GameSettingsRouter(
        view: GameSettingsView,
        interactor: GameSettingsInteractor,
        component: GameSettingsBuilder.Component,
        private val gameSettingsStatBuilder: GameSettingsStatBuilder,
        private val gameSettingsClassBuilder: GameSettingsClassBuilder,
        private val routerNavigatorFactory: RouterNavigatorFactory
) : ViewRouter<GameSettingsView, GameSettingsInteractor, GameSettingsBuilder.Component>(view, interactor, component) {

    private val router = routerNavigatorFactory.create<State>(this)!!
    private val statsAttachTransition = object : DefaultContainerAttachTransition<
            GameSettingsStatRouter,
            State,
            GameSettingsStatBuilder,
            GameSettingsView
            >(
            gameSettingsStatBuilder, view
    ) {}

    private val classesAttachTransition = object : DefaultContainerAttachTransition<
            GameSettingsClassRouter,
            State,
            GameSettingsClassBuilder,
            GameSettingsView
            >(
            gameSettingsClassBuilder, view
    ) {}

    private val statsDetachTransition = DefaultContainerDetachTransition<GameSettingsStatRouter, State, GameSettingsView>(
            view
    )
   private val classesDetachTransition = DefaultContainerDetachTransition<GameSettingsClassRouter, State, GameSettingsView>(
            view
    )

    fun attach(type: GameSettingsViewModel.GameSettingsItemType) {
        when (type) {
            GameSettingsViewModel.GameSettingsItemType.STATS -> {
                router.pushTransientState(State.Stats, statsAttachTransition, statsDetachTransition)
            }
            GameSettingsViewModel.GameSettingsItemType.CLASSES -> {
                router.pushTransientState(State.Classes, classesAttachTransition, classesDetachTransition)
            }

        }
    }

    data class State(val name: String) : RouterNavigatorState {

        override fun name(): String {
            return name
        }

        companion object {
            val Stats = State("Stats")
            val Classes = State("Classes")
        }
    }

    fun onBackPressed(): Boolean {
        val currentRouter = router.peekRouter()
                ?: return false
        if (currentRouter.handleBackPress()) {
            return true
        }

        router.popState()
        return true
    }
}
