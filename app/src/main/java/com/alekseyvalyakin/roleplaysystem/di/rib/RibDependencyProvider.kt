package com.alekseyvalyakin.roleplaysystem.di.rib

import com.alekseyvalyakin.roleplaysystem.di.activity.ActivityDependencyProvider
import com.alekseyvalyakin.roleplaysystem.di.singleton.SingletonDependencyProvider

interface RibDependencyProvider : SingletonDependencyProvider, ActivityDependencyProvider {
}