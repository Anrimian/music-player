package com.github.anrimian.musicplayer.domain.models.order

import java.io.Serializable

data class Order(val orderType: OrderType, val isReversed: Boolean) : Serializable