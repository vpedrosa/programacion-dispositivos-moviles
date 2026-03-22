package com.vpedrosa.smarthome.wearable

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val wearableModule = module {
    single { WearableMessageHandler(androidContext(), get(), get()) }
}
