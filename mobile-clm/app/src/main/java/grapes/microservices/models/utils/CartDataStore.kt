package grapes.microservices.models.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// Define Datastore as a Context extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cart_prefs")

class CartDataStore(private val context: Context) {
    companion object {
        private val KEY_ORDER_ID = intPreferencesKey("order_id")
    }

    suspend fun saveOrderId(orderId: Int?) {
        context.dataStore.edit { preferences ->
            if (orderId != null) {
                preferences[KEY_ORDER_ID] = orderId
            } else {
                preferences.remove(KEY_ORDER_ID)
            }
        }
    }

    val orderIdFlow: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ORDER_ID]
    }

    suspend fun getOrderId(): Int? {
        return orderIdFlow.firstOrNull()
    }

    suspend fun clearOrderId() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ORDER_ID)
        }
    }
}