package com.alekseyvalyakin.roleplaysystem.ribs.game.active

import com.alekseyvalyakin.roleplaysystem.base.model.NavigationId
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.characters.GameCharactersBuilder
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.characters.GameCharactersRouter
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.dice.DiceBuilder
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.dice.DiceRouter
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.log.LogBuilder
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.log.LogRouter
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.model.ActiveGameViewModelProvider
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.photos.PhotoBuilder
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.photos.PhotoRouter
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.photos.fullsizephoto.FullSizePhotoBuilder
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.photos.fullsizephoto.FullSizePhotoModel
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.photos.fullsizephoto.FullSizePhotoRouter
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.settings.GameSettingsBuilder
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.settings.GameSettingsRouter
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.transition.BaseActiveGameInternalAttachTransition
import com.alekseyvalyakin.roleplaysystem.ribs.game.active.transition.DefaultActiveGameInternalDetachTransition
import com.uber.rib.core.RestorableRouter
import com.uber.rib.core.RouterNavigatorFactory
import com.uber.rib.core.RouterNavigatorState
import com.uber.rib.core.ViewRouter
import java.io.Serializable

/**
 * Adds and removes children of {@link ActiveGameBuilder.ActiveGameScope}.
 *
 */
class ActiveGameRouter(
        view: ActiveGameView,
        interactor: ActiveGameInteractor,
        component: ActiveGameBuilder.Component,
        private val diceBuilder: DiceBuilder,
        private val routerNavigatorFactory: RouterNavigatorFactory,
        private val activeGameViewModelProvider: ActiveGameViewModelProvider,
        private val photoBuilder: PhotoBuilder,
        private val gameSettingsBuilder: GameSettingsBuilder,
        private val fullSizePhotoBuilder: FullSizePhotoBuilder,
        private val gameCharactersBuilder: GameCharactersBuilder,
        private val logBuilder: LogBuilder
) : ViewRouter<ActiveGameView, ActiveGameInteractor, ActiveGameBuilder.Component>(view, interactor, component), RestorableRouter {

    private val modernRouter = routerNavigatorFactory.create<State>(this)
    private val dicesAttachTransition = BaseActiveGameInternalAttachTransition(diceBuilder, view)
    private val dicesDetachTransition = object : DefaultActiveGameInternalDetachTransition<DiceRouter, State>(view) {}

    private val photoAttachTransition = BaseActiveGameInternalAttachTransition(photoBuilder, view)
    private val photoDetachTransition = object : DefaultActiveGameInternalDetachTransition<PhotoRouter, State>(view) {}

    private val gameSettingsAttachTransition = BaseActiveGameInternalAttachTransition(gameSettingsBuilder, view)
    private val gameSettingsDetachTransition = object : DefaultActiveGameInternalDetachTransition<GameSettingsRouter, State>(view) {}

    private val gameCharactersAttachTransition = BaseActiveGameInternalAttachTransition(gameCharactersBuilder, view)
    private val gameCharacterssDetachTransition = object : DefaultActiveGameInternalDetachTransition<GameCharactersRouter, State>(view) {}

    private val gameLogAttachTransition = BaseActiveGameInternalAttachTransition(logBuilder, view)
    private val gameLogsDetachTransition = object : DefaultActiveGameInternalDetachTransition<LogRouter, State>(view) {}


    private var canBeClosed = false
    private var fullSizePhotoRouter: FullSizePhotoRouter? = null
    fun attachView(navigationId: NavigationId) {
        when (navigationId) {
            NavigationId.DICES -> {
                modernRouter.pushTransientState(State.DICES, dicesAttachTransition, dicesDetachTransition)
            }

            NavigationId.PICTURES -> {
                modernRouter.pushTransientState(State.PHOTOS, photoAttachTransition, photoDetachTransition)
            }

            NavigationId.MENU -> {
                modernRouter.pushTransientState(State.SETTINGS, gameSettingsAttachTransition, gameSettingsDetachTransition)
            }

            NavigationId.CHARACTERS -> {
                modernRouter.pushTransientState(State.CHARACTERS, gameCharactersAttachTransition, gameCharacterssDetachTransition)
            }
        }
    }

    fun attachFullSizePhoto(fullSizePhotoModel: FullSizePhotoModel) {
        if (fullSizePhotoRouter == null) {
            val fullScreenContainer = view.getFullScreenContainer()
            fullSizePhotoRouter = fullSizePhotoBuilder.build(fullScreenContainer, fullSizePhotoModel)
            attachChild(fullSizePhotoRouter)
            fullScreenContainer.addView(fullSizePhotoRouter!!.view)
        }
    }

    private fun detachFullSizePhoto(): Boolean {
        if (fullSizePhotoRouter != null) {
            val fullScreenContainer = view.getFullScreenContainer()
            detachChild(fullSizePhotoRouter)
            fullScreenContainer.removeView(fullSizePhotoRouter!!.view)
            fullSizePhotoRouter = null
            return true
        }

        return false
    }

    fun backPress(): Boolean {
        if (canBeClosed) {
            return false
        }

        if (modernRouter.peekState() == null) {
            return false
        }

        if (detachFullSizePhoto()) {
            return true
        }

        if (modernRouter.peekRouter()?.handleBackPress() == false) {
            return false
        }

        return true
    }

    fun onDelete() {
        canBeClosed = true
    }

    override fun getRestorableInfo(): Serializable? {
        return activeGameViewModelProvider.getCurrentGame()
    }

    data class State(val name: String) : RouterNavigatorState {

        override fun name(): String {
            return name
        }

        companion object {
            val DICES = State("DICES")
            val PHOTOS = State("PHOTOS")
            val SETTINGS = State("SETTINGS")
            val CHARACTERS = State("CHARACTERS")
            val LOG = State("LOG")
        }
    }
}
