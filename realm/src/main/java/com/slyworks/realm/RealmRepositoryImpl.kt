package com.slyworks.realm

import android.util.Log
import com.slyworks.models.CryptoModel
import com.slyworks.repository.RealmRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort


/**
 *Created by Joshua Sylvanus, 5:19 AM, 04-Jun-22.
 */
class RealmRepositoryImpl(private val config: RealmConfiguration) : RealmRepository {
    //region Vars
    private val TAG: String? = RealmRepositoryImpl::class.simpleName
    private val mRealm: Realm = Realm.getInstance(config)
    //endregion

    companion object{
        fun mapModelToRealmModel(model: CryptoModel): CryptoModelRealm {
            return CryptoModelRealm(
                id  = model.id,
                _id = model._id,
                image = model.image,
                symbol = model.symbol,
                name = model.name,
                maxSupply = model.maxSupply ?: 0.0,
                circulatingSupply = model.circulatingSupply ?: 0.0,
                totalSupply = model.totalSupply ?: 0.0,
                cmcRank = model.cmcRank,
                lastUpdated = model.lastUpdated,
                price = model.price,
                marketCap = model.marketCap ?: 0.0,
                dateAdded = model.dateAdded,
                tags = model.tags,
                isFavorite = model.isFavorite )
        }
        fun mapRealmModelToModel(model: CryptoModelRealm): CryptoModel {
            return CryptoModel(
                id = model.id,
                _id = model._id,
                image = model.image,
                symbol = model.symbol,
                name = model.name,
                maxSupply = model.maxSupply,
                circulatingSupply = model.circulatingSupply,
                totalSupply = model.totalSupply,
                cmcRank = model.cmcRank,
                lastUpdated = model.lastUpdated,
                price = model.price,
                priceUnit = model.priceUnit,
                marketCap = model.marketCap,
                dateAdded = model.dateAdded,
                tags = model.tags,
                isFavorite = model.isFavorite )
        }
    }

    override fun getData(): Single<List<CryptoModel>> {
       return Single.create{ emitter ->
           Realm.getInstance(config)
               .executeTransaction(Realm.Transaction {
               val l:List<CryptoModel> = it.where(CryptoModelRealm::class.java)
                   .findAll()
                   .sort("cmcRank", Sort.ASCENDING)
                   .map(::mapRealmModelToModel)

               emitter.onSuccess(l)
           })
       }
    }

    override fun saveData(data: List<CryptoModel>): Completable {
        return Completable.create { emitter ->
            try {
                val l: List<CryptoModelRealm> =
                    data.toMutableList()
                        .map(::mapModelToRealmModel)

                Realm.getInstance(config)
                    .executeTransaction(Realm.Transaction {
                    it.insertOrUpdate(l)

                    emitter.onComplete()
                })
            } catch (e: Exception) {
                Log.e(TAG, "saveData: error occurred", e)
                emitter.onError(e)
            }
        }
    }

    override fun getFavorites(): Single<List<CryptoModel>> {
        return Single.create { emitter ->
            Realm.getInstance(config)
                .executeTransaction(Realm.Transaction {
                val l:List<CryptoModel> = it.where(CryptoModelRealm::class.java)
                    .equalTo("isFavorite", true)
                    .findAll()
                    .sort("name", Sort.ASCENDING)
                    .map(::mapRealmModelToModel)

                emitter.onSuccess(l)
            })
        }
    }

    override fun addToFavorites(vararg data: CryptoModel): Completable {
        return Completable.create { emitter ->

            try {
                val l: List<CryptoModelRealm> =
                    data.toList()
                        .map(::mapModelToRealmModel)
                 Realm.getInstance(config)
                     .executeTransaction {
                    l.forEach { i ->
                        it.where(CryptoModelRealm::class.java)
                            .equalTo("name", i.name)
                            .findFirst()
                            .apply {
                                this?.isFavorite = true
                            }
                    }

                    emitter.onComplete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "addToFavorites: error occurred", e)
                emitter.onError(e)
            }
        }
    }

    override fun removeFromFavorites(vararg data: CryptoModel): Completable {
        return Completable.create { emitter ->
            try {
                val l: List<CryptoModelRealm> = data.toList().map(::mapModelToRealmModel)

                 Realm.getInstance(config)
                     .executeTransaction {
                    l.forEach { i ->
                        it.where(CryptoModelRealm::class.java)
                            .equalTo("name", i.name)
                            .findFirst()
                            .apply {
                                this?.isFavorite = false
                            }
                    }

                    emitter.onComplete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "removeFromFavorites: error occurred", e)
                emitter.onError(e)
            }
        }
    }

    fun deleteData(name:String){
         Realm.getInstance(config)
             .where(CryptoModelRealm::class.java)
            .equalTo("name", name)
            .findFirst()
            ?.deleteFromRealm()
    }
}