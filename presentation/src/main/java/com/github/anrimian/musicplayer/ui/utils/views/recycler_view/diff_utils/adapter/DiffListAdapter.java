/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * {@link RecyclerView.Adapter RecyclerView.Adapter} base class for presenting List data in a
 * {@link RecyclerView}, including computing diffs between Lists on a background thread.
 * <p>
 * This class is a convenience wrapper around {@link AsyncListDiffer} that implements Adapter common
 * default behavior for item access and counting.
 * <p>
 * While using a LiveData&lt;List> is an easy way to provide data to the adapter, it isn't required
 * - you can use {@link #submitList(List)} when new lists are available.
 * <p>
 * A complete usage pattern with Room would look like this:
 * <pre>
 * {@literal @}Dao
 * interface UserDao {
 *     {@literal @}Query("SELECT * FROM user ORDER BY lastName ASC")
 *     public abstract LiveData&lt;List&lt;User>> usersByLastName();
 * }
 *
 * class MyViewModel extends ViewModel {
 *     public final LiveData&lt;List&lt;User>> usersList;
 *     public MyViewModel(UserDao userDao) {
 *         usersList = userDao.usersByLastName();
 *     }
 * }
 *
 * class MyActivity extends AppCompatActivity {
 *     {@literal @}Override
 *     public void onCreate(Bundle savedState) {
 *         super.onCreate(savedState);
 *         MyViewModel viewModel = ViewModelProviders.of(this).get(MyViewModel.class);
 *         RecyclerView recyclerView = findViewById(R.id.user_list);
 *         UserAdapter&lt;User> adapter = new UserAdapter();
 *         viewModel.usersList.observe(this, list -> adapter.submitList(list));
 *         recyclerView.setAdapter(adapter);
 *     }
 * }
 *
 * class UserAdapter extends ListAdapter&lt;User, UserViewHolder> {
 *     public UserAdapter() {
 *         super(User.DIFF_CALLBACK);
 *     }
 *     {@literal @}Override
 *     public void onBindViewHolder(UserViewHolder holder, int position) {
 *         holder.bindTo(getItem(position));
 *     }
 *     public static final DiffUtil.ItemCallback&lt;User> DIFF_CALLBACK =
 *             new DiffUtil.ItemCallback&lt;User>() {
 *         {@literal @}Override
 *         public boolean areItemsTheSame(
 *                 {@literal @}NonNull User oldUser, {@literal @}NonNull User newUser) {
 *             // User properties may have changed if reloaded from the DB, but ID is fixed
 *             return oldUser.getId() == newUser.getId();
 *         }
 *         {@literal @}Override
 *         public boolean areContentsTheSame(
 *                 {@literal @}NonNull User oldUser, {@literal @}NonNull User newUser) {
 *             // NOTE: if you use equals, your object must properly override Object#equals()
 *             // Incorrectly returning false here will result in too many animations.
 *             return oldUser.equals(newUser);
 *         }
 *     }
 * }</pre>
 *
 * Advanced users that wish for more control over adapter behavior, or to provide a specific base
 * class should refer to {@link AsyncListDiffer}, which provides custom mapping from diff events
 * to adapter positions.
 *
 * @param <T> Type of the Lists this Adapter will receive.
 * @param <VH> A class that extends ViewHolder that will be used by the adapter.
 */
public abstract class DiffListAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {
    private final CustomAsyncListDiffer<T> mHelper;

    @SuppressWarnings("unused")
    protected DiffListAdapter(RecyclerView recyclerView,
                              @NonNull DiffUtil.ItemCallback<T> diffCallback) {
        mHelper = new CustomAsyncListDiffer<>(recyclerView,
                new AdapterListUpdateCallback(this),
                new AsyncDifferConfig.Builder<>(diffCallback).build());
    }

    @SuppressWarnings("unused")
    protected DiffListAdapter(RecyclerView recyclerView,
                              @NonNull AsyncDifferConfig<T> config) {
        mHelper = new CustomAsyncListDiffer<>(recyclerView, new AdapterListUpdateCallback(this), config);
    }

    /**
     * Submits a new list to be diffed, and displayed.
     * <p>
     * If a list is already being displayed, a diff will be computed on a background thread, which
     * will dispatch Adapter.notifyItem events on the main thread.
     *
     * @param list The new list to be displayed.
     */
    @SuppressWarnings("WeakerAccess")
    public void submitList(@Nullable List<T> list) {
        mHelper.submitList(list);
    }

    @SuppressWarnings("unused")
    protected T getItem(int position) {
        return mHelper.getCurrentList().get(position);
    }

    @Override
    public int getItemCount() {
        return mHelper.getCurrentList().size();
    }
}

