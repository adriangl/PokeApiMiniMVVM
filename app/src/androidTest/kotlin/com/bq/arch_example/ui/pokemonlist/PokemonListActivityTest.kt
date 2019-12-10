package com.bq.arch_example.ui.pokemonlist

import android.content.Intent
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.agoda.kakao.text.KTextView
import com.bq.arch_example.R
import com.bq.arch_example.app
import com.bq.arch_example.fixtures.MoveFixtures
import com.bq.arch_example.utils.test.testActivity
import mini.Resource
import mini.kodein.android.bindViewModel
import mini.testing.TestDispatcherRule
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.kodein.di.direct
import org.kodein.di.generic.instance
import org.mockito.Mockito
import org.mockito.Mockito.mock

class PokemonListScreen : Screen<PokemonListScreen>() {
    val loadingView = KView { withId(R.id.list_loading) }
    val errorView = KView { withId(R.id.error) }
    val searchBar = KView { withId(R.id.search_pokemon_bar) }
    val pokemonList = KRecyclerView(
        builder = { withId(R.id.list_recycler) },
        itemTypeBuilder = {
            itemType(::PokemonListRecyclerItem)
        }
    )

    class PokemonListRecyclerItem(parent: Matcher<View>) :
        KRecyclerItem<PokemonListRecyclerItem>(parent) {
        val sprite: KImageView = KImageView(parent) { withId(R.id.pokemon_sprite) }
        val name: KTextView = KTextView(parent) { withId(R.id.pokemon_name) }
        val number: KTextView = KTextView(parent) { withId(R.id.pokemon_number) }
        val move1: KTextView = KTextView(parent) { withId(R.id.pokemon_move_1) }
        val move2: KTextView = KTextView(parent) { withId(R.id.pokemon_move_2) }
        val move3: KTextView = KTextView(parent) { withId(R.id.pokemon_move_3) }
        val move4: KTextView = KTextView(parent) { withId(R.id.pokemon_move_4) }
    }
}

@RunWith(AndroidJUnit4::class)
class PokemonListActivityTest : MoveFixtures {
    val testActivityRule = testActivity(clazz = PokemonListActivity::class, launchActivity = false)
    val testDispatcherRule = TestDispatcherRule { app.kodein.direct.instance() }

    @get:Rule
    val ruleChain = RuleChain
        .outerRule(testDispatcherRule)
        .around(testActivityRule)

    private lateinit var liveData: MutableLiveData<Resource<PokemonListViewData>>

    @Before
    fun setup() {
        val viewModel = mock(PokemonListViewModel::class.java)
        liveData = MutableLiveData()

        Mockito.`when`(viewModel.getPokemonListLiveData()).thenReturn(liveData)

        app.clearTestModule()

        injectTestDependencies(viewModel)

        testActivityRule.launchActivity(Intent())
    }

    @Test
    fun validate_list_size() {
        val itemList = listOf(
            PokemonListItem(
                "Agumon",
                1,
                "https://placekitten.com/256/256",
                anyMoveList()
            )
        )

        // We have to use postValue instead of setValue because we can't set a value when there's
        // no available observers
        liveData.postValue(Resource.success(PokemonListViewData(itemList)))

        onScreen<PokemonListScreen> {
            loadingView.isNotDisplayed()
            errorView.isNotDisplayed()
            searchBar.isDisplayed()
            pokemonList {
                isDisplayed()

                childAt<PokemonListScreen.PokemonListRecyclerItem>(0) {
                    isDisplayed()
                    name.hasText(itemList[0].name)
                    number.hasText(itemList[0].number.toString())

                    move1.containsText(itemList[0].currentMoves[0].name)
                    move2.containsText(itemList[0].currentMoves[1].name)
                    move3.containsText(itemList[0].currentMoves[2].name)
                    move4.containsText(itemList[0].currentMoves[3].name)
                }
            }
        }
    }

    @Test
    fun validate_list_size_2() {
        val itemList = listOf(
            PokemonListItem(
                "Jibanyan",
                1,
                "https://placekitten.com/256/256",
                anyMoveList()
            ),
            PokemonListItem(
                "Mocchi",
                2,
                "https://placekitten.com/256/256",
                anyMoveList()
            )
        )

        liveData.postValue(Resource.success(PokemonListViewData(itemList)))

        onScreen<PokemonListScreen> {
            loadingView.isNotDisplayed()
            errorView.isNotDisplayed()
            searchBar.isDisplayed()
            pokemonList {
                isDisplayed()

                childAt<PokemonListScreen.PokemonListRecyclerItem>(0) {
                    isDisplayed()
                    name.hasText(itemList[0].name)
                    number.hasText(itemList[0].number.toString())
                }

                childAt<PokemonListScreen.PokemonListRecyclerItem>(1) {
                    isDisplayed()
                    name.hasText(itemList[1].name)
                    number.hasText(itemList[1].number.toString())
                }
            }
        }
    }

    private inline fun <reified T : ViewModel> injectTestDependencies(viewModel: T) {
        app.setTestModule {
            //bind<Dispatcher>() with singleton { testDispatcher }
            bindViewModel { viewModel }
        }

        app.initializeInjection()
    }
}

