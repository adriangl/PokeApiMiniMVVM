package com.adriangl.pokeapi_mvvm.pokemonlist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.adriangl.pokeapi_mvvm.network.Pokemon
import com.adriangl.pokeapi_mvvm.pokemon.PokeState
import com.adriangl.pokeapi_mvvm.pokemon.PokeStore
import com.adriangl.pokeapi_mvvm.utils.injection.bindViewModel
import mini.*
import mini.rx.flowable
import mini.rx.select
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class PokemonListViewModel(app: Application) : RxAndroidViewModel(app), KodeinAware {
    override val kodein by closestKodein()

    private val pokeStore: PokeStore by instance()
    private val dispatcher: Dispatcher by instance()

    private val pokemonListLiveData = MutableLiveData<PokemonListViewData>()

    init {
        pokeStore.flowable()
            .select { PokemonListViewData.from(it) }
            .subscribe {
                pokemonListLiveData.postValue(PokemonListViewModelState(it))
            }
            .track()

        if (pokeStore.state.pokemonList == null) {
            getPokemonDetails()
        }
    }

    fun getPokemonListLiveData() = pokemonListLiveData

    fun getPokemonDetails() {
        Log.e("AAA", "Dispatcher instance in ViewModel: dispatcher")
        if (pokemonListLiveData.value == null ||
            pokemonListLiveData.value!!.list.isEmpty ||
            pokemonListLiveData.value!!.list.isFailure
        ) {
            dispatcher.dispatch(GetPokemonDetailsListAction())
        }
    }

    fun filterList(query: String) {
        pokemonListLiveData.postValue(pokemonListLiveData.value?.copy(filter = {
            it.name.contains(query, true)
        }))
    }
}

data class PokemonListViewModelState(
    val list: Resource<List<PokemonListItem>>,
    val filter: (PokemonListItem) -> Boolean = { true }
)

data class PokemonListItem(val name: String, val number: Int, val sprite: String?) {
    companion object {
        fun from(pokemon: Pokemon): PokemonListItem {
            return PokemonListItem(pokemon.name, pokemon.order, pokemon.sprites.frontDefault)
        }
    }
}

data class PokemonListViewData(val pokemonListRes: Resource<List<PokemonListItem>>) {
    companion object {
        fun from(pokeState: PokeState): PokemonListViewData {
            var resource: Resource<List<PokemonListItem>> = Resource.empty()
            pokeState.pokemonListTask
                .onLoading { resource = Resource.loading() }
                .onSuccess {
                    resource =
                        Resource.success(pokeState.filteredPokemonList!!.map {
                            PokemonListItem.from(
                                it
                            )
                        })
                }
                .onFailure { resource = Resource.failure() }

            return PokemonListViewData(resource)
        }
    }
}

val pokemonListViewModelModule = Kodein.Module("pokemonListViewModelModule", true) {
    bindViewModel { PokemonListViewModel(instance()) }
}