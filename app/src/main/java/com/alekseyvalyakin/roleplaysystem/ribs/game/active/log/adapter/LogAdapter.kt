package com.alekseyvalyakin.roleplaysystem.ribs.game.active.log.adapter

import com.alekseyvalyakin.roleplaysystem.ribs.game.active.log.LogPresenter
import com.jakewharton.rxrelay2.Relay
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible

class LogAdapter(
        items: List<IFlexible<*>>,
        val relay: Relay<LogPresenter.UiEvent>
) : FlexibleAdapter<IFlexible<*>>(items)