package com.pancho.suscripciones.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Respaldo en JSON, pensado para exportar/importar vía Storage Access Framework
 * (el usuario elige el destino/origen, p. ej. Google Drive). El respaldo incluye
 * las contraseñas en texto plano: la clave de cifrado vive en el Android Keystore
 * del dispositivo y normalmente no sobrevive una reinstalación, así que sin esto
 * el respaldo no sería restaurable.
 */
object BackupManager {
    private const val SCHEMA_VERSION = 1

    suspend fun export(uri: Uri, context: Context, repository: SubscriptionsRepository) {
        val categories = repository.observeCategories().first()
        val subscriptions = repository.observeSubscriptions().first()
        val paymentMethods = repository.observePaymentMethods().first()
        val categoryNameById = categories.associateBy({ it.id }, { it.name })

        val json = JSONObject().apply {
            put("version", SCHEMA_VERSION)
            put(
                "categories",
                JSONArray().apply {
                    categories.forEach { c ->
                        put(
                            JSONObject().apply {
                                put("name", c.name)
                                put("colorHex", c.colorHex)
                                put("mono", c.mono)
                            },
                        )
                    }
                },
            )
            put(
                "paymentMethods",
                JSONArray().apply {
                    paymentMethods.forEach { pm -> put(JSONObject().apply { put("name", pm.name) }) }
                },
            )
            put(
                "subscriptions",
                JSONArray().apply {
                    subscriptions.forEach { s ->
                        put(
                            JSONObject().apply {
                                put("categoryName", categoryNameById[s.categoryId] ?: "")
                                put("name", s.name)
                                put("price", s.price)
                                put("currency", s.currency.name)
                                put("cycle", s.cycle.name)
                                put("nextChargeDateEpochDay", s.nextChargeDateEpochDay)
                                put("paymentMethod", s.paymentMethod)
                                put("website", s.website)
                                put("username", s.username)
                                put("password", repository.decryptPassword(s))
                                put("logoColorHex", s.logoColorHex)
                                put("logoLetter", s.logoLetter)
                            },
                        )
                    }
                },
            )
        }

        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(json.toString(2).toByteArray())
        }
    }

    /** Fusiona el respaldo con lo existente: agrega categorías/medios de pago/suscripciones nuevos por nombre, sin duplicar. */
    suspend fun import(uri: Uri, context: Context, repository: SubscriptionsRepository) {
        val text = context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        } ?: return
        val json = JSONObject(text)

        val categoryIdByName = repository.observeCategories().first()
            .associateBy({ it.name.trim().lowercase() }, { it.id })
            .toMutableMap()

        val categoriesJson = json.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until categoriesJson.length()) {
            val c = categoriesJson.getJSONObject(i)
            val name = c.getString("name")
            val key = name.trim().lowercase()
            if (!categoryIdByName.containsKey(key)) {
                val id = repository.saveCategory(
                    CategoryEntity(name = name, colorHex = c.getString("colorHex"), mono = c.getString("mono")),
                )
                categoryIdByName[key] = id
            }
        }

        val paymentMethodNames = repository.observePaymentMethods().first()
            .map { it.name.trim().lowercase() }
            .toMutableSet()
        val paymentMethodsJson = json.optJSONArray("paymentMethods") ?: JSONArray()
        for (i in 0 until paymentMethodsJson.length()) {
            val name = paymentMethodsJson.getJSONObject(i).getString("name")
            if (paymentMethodNames.add(name.trim().lowercase())) {
                repository.savePaymentMethod(PaymentMethodEntity(name = name))
            }
        }

        val existingSubKeys = repository.observeSubscriptions().first()
            .map { "${it.categoryId}|${it.name.trim().lowercase()}" }
            .toMutableSet()
        val subscriptionsJson = json.optJSONArray("subscriptions") ?: JSONArray()
        for (i in 0 until subscriptionsJson.length()) {
            val s = subscriptionsJson.getJSONObject(i)
            val categoryId = categoryIdByName[s.getString("categoryName").trim().lowercase()] ?: continue
            val name = s.getString("name")
            val key = "$categoryId|${name.trim().lowercase()}"
            if (!existingSubKeys.add(key)) continue

            val entity = SubscriptionEntity(
                categoryId = categoryId,
                name = name,
                price = s.getDouble("price"),
                currency = Currency.valueOf(s.getString("currency")),
                cycle = BillingCycle.valueOf(s.getString("cycle")),
                nextChargeDateEpochDay = s.getLong("nextChargeDateEpochDay"),
                paymentMethod = s.optString("paymentMethod", ""),
                website = s.optString("website", ""),
                username = s.optString("username", ""),
                passwordEncrypted = "",
                logoColorHex = s.optString("logoColorHex", "#5B8CFF"),
                logoLetter = s.optString("logoLetter", "?"),
            )
            repository.saveSubscription(entity, s.optString("password", ""))
        }
    }
}
