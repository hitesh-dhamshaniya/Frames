/*
 * Copyright (c) 2017. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jahirfiquitiva.libs.frames.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.widget.GridLayoutManager
import android.view.View
import ca.allanwang.kau.utils.dimenPixelSize
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.adapters.WallpapersAdapter
import jahirfiquitiva.libs.frames.extensions.isInHorizontalMode
import jahirfiquitiva.libs.frames.fragments.base.BaseFramesFragment
import jahirfiquitiva.libs.frames.models.Wallpaper
import jahirfiquitiva.libs.frames.models.viewmodels.FavoritesViewModel
import jahirfiquitiva.libs.frames.views.CheckableImageView
import jahirfiquitiva.libs.kauextensions.ui.decorations.GridSpacingItemDecoration
import jahirfiquitiva.libs.kauextensions.ui.views.EmptyViewRecyclerView

class FavoritesFragment:BaseFramesFragment<Wallpaper>() {

    private lateinit var favoritesModel:FavoritesViewModel

    private lateinit var rv:EmptyViewRecyclerView
    private lateinit var adapter:WallpapersAdapter
    private lateinit var fastScroll:RecyclerFastScroller

    override fun initUI(content:View) {
        rv = content.findViewById<EmptyViewRecyclerView>(R.id.list_rv)
        rv.emptyView = content.findViewById(R.id.no_favorites_view)
        rv.textView = content.findViewById(R.id.empty_text)
        rv.emptyTextRes = R.string.no_favorites
        rv.loadingView = content.findViewById(R.id.loading_view)
        rv.loadingTextRes = R.string.loading_section
        val spanCount = if (context.isInHorizontalMode) 3 else 2
        rv.layoutManager = GridLayoutManager(context, spanCount,
                                             GridLayoutManager.VERTICAL, false)
        rv.addItemDecoration(
                GridSpacingItemDecoration(spanCount, context.dimenPixelSize(R.dimen.cards_margin)))
        adapter = WallpapersAdapter({ onItemClicked(it) },
                                    { heart, wall -> onHeartClicked(heart, wall) },
                                    true)
        rv.adapter = adapter
        rv.state = EmptyViewRecyclerView.State.LOADING
        fastScroll = content.findViewById(R.id.fast_scroller)
        fastScroll.attachRecyclerView(rv)
    }

    override fun onItemClicked(item:Wallpaper) {
        // TODO: Start viewer activity
    }

    fun onHeartClicked(heart:CheckableImageView, item:Wallpaper) {
        val isInDB = getDatabase().getFavorites().contains(item)
        if (heart.isChecked) {
            if (isInDB) {
                getDatabase().removeFromFavorites(item)
                heart.isChecked = false
                loadDataFromViewModel()
            }
        } else {
            if (!isInDB) {
                getDatabase().addToFavorites(item)
                heart.isChecked = true
                loadDataFromViewModel()
            }
        }
    }

    override fun getContentLayout():Int = R.layout.section_lists

    override fun initViewModel() {
        favoritesModel = ViewModelProviders.of(activity).get(FavoritesViewModel::class.java)
    }

    override fun registerObserver() {
        favoritesModel.items.observe(this, Observer<ArrayList<Wallpaper>> { data ->
            data?.let {
                adapter.clearAndAddAll(it)
                rv.state = EmptyViewRecyclerView.State.NORMAL
            }
        })
    }

    override fun loadDataFromViewModel() {
        favoritesModel.loadData(getDatabase())
    }

    override fun unregisterObserver() {
        favoritesModel.items.removeObservers(this)
    }
}