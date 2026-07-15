package com.pancho.suscripciones.data

class SubscriptionsRepository(private val db: AppDatabase) {
    fun observeCategories() = db.categoryDao().observeAll()
    fun observeSubscriptions() = db.subscriptionDao().observeAll()
    fun observePaymentMethods() = db.paymentMethodDao().observeAll()

    suspend fun saveCategory(category: CategoryEntity): Long = db.categoryDao().upsert(category)
    suspend fun deleteCategory(category: CategoryEntity) = db.categoryDao().delete(category)

    suspend fun saveSubscription(sub: SubscriptionEntity, plainPassword: String): Long {
        val encrypted = sub.copy(passwordEncrypted = PasswordCrypto.encrypt(plainPassword))
        return db.subscriptionDao().upsert(encrypted)
    }

    suspend fun deleteSubscription(sub: SubscriptionEntity) = db.subscriptionDao().delete(sub)

    fun decryptPassword(sub: SubscriptionEntity): String = PasswordCrypto.decrypt(sub.passwordEncrypted)

    suspend fun savePaymentMethod(method: PaymentMethodEntity): Long = db.paymentMethodDao().upsert(method)
    suspend fun deletePaymentMethod(method: PaymentMethodEntity) = db.paymentMethodDao().delete(method)
}
