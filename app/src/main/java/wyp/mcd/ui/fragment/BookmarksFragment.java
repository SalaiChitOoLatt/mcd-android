/*
 * Copyright (C) 2018
 *
 * Source code is created by Elissa Software
 * Dictionary data is owned by UCST
 * Database is implemented by Salai Chit Oo Latt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wyp.mcd.ui.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.RelativeLayout;

import com.l4digital.fastscroll.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import lombok.NoArgsConstructor;
import wyp.mcd.R;
import wyp.mcd.component.android.BasicFragment;
import wyp.mcd.component.util.Logger;
import wyp.mcd.infrastructure.entities.BookmarksEntity;
import wyp.mcd.infrastructure.entities.EngToEngEntity;
import wyp.mcd.infrastructure.entities.EngToMmEntity;
import wyp.mcd.infrastructure.repository.EngToEngRepository;
import wyp.mcd.infrastructure.repository.EngToMmRepository;
import wyp.mcd.ui.activity.DetailResultActivity;
import wyp.mcd.ui.adapter.BookmarksListAdapter;
import wyp.mcd.ui.uicomponents.RecyclerViewDividerItemDecoration;
import wyp.mcd.viewmodel.BookmarksViewModel;

@NoArgsConstructor
public class BookmarksFragment extends BasicFragment implements View.OnLongClickListener, View.OnClickListener {

    @BindView(R.id.rlSad)
    RelativeLayout rlSad;

    @BindView(R.id.lblScreenTitle)
    AppCompatTextView lblScreenTitle;

    @BindView(R.id.recyclerViewBookMark)
    FastScrollRecyclerView recyclerViewBookMark;

    @BindView(R.id.ibtnAllDelete)
    AppCompatImageButton btnAllDelete;

    private EngToMmRepository engToMmRepository;
    private EngToEngRepository engToEngRepository;

    private BookmarksListAdapter bookmarksListAdapter = null;
    private BookmarksViewModel bookmarksViewModel;

    private String vocabulary;
    private String type;
    private String meaning;

    @Override
    public void createView() {

        lblScreenTitle.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.title_bookmarks));
        btnAllDelete.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_delete));

        bookmarksListAdapter = new BookmarksListAdapter(new ArrayList<>(), this, this);

        recyclerViewBookMark.setLayoutManager(new LinearLayoutManager(getActivity()));

        /* Instantiate repositories */
        engToMmRepository = new EngToMmRepository(getActivity().getApplication());
        engToEngRepository = new EngToEngRepository(getActivity().getApplication());

        /* Add ItemDecoration */
        recyclerViewBookMark.addItemDecoration(
                new RecyclerViewDividerItemDecoration(getActivity(), R.drawable.view_divider));

        recyclerViewBookMark.setAdapter(bookmarksListAdapter);

        bookmarksViewModel = ViewModelProviders.of(this).get(BookmarksViewModel.class);
        bookmarksViewModel.getItemBookmarksList().observe(this, bookmark -> bookmarksListAdapter.addItems(bookmark));
        /* Count array list */
        bookmarksViewModel.getItemBookmarksList().observe(this, bookmarkEntityList -> {
            assert bookmarkEntityList != null;
            recyclerViewVisibility(bookmarkEntityList.size());
        });
    }

    @Override
    public int getLayoutXmlId() {
        return R.layout.fragment_bookmarks;
    }

    @Override
    public void refresh() {
    }

    private void recyclerViewVisibility(int counter) {
        if (0 == counter) {
            rlSad.setVisibility(View.VISIBLE);
            recyclerViewBookMark.setVisibility(View.GONE);
            btnAllDelete.setVisibility(View.GONE);
        } else {
            rlSad.setVisibility(View.GONE);
            recyclerViewBookMark.setVisibility(View.VISIBLE);
            btnAllDelete.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.ibtnAllDelete)
    public void allBookmarkDelete() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        alertDialog.setCancelable(false);
        alertDialog.setTitle(Objects.requireNonNull(getActivity()).getResources().getString(R.string.confirm_delete_all_dialog_title));
        alertDialog.setMessage(Objects.requireNonNull(getActivity()).getResources().getString(R.string.confirm_delete_all_dialog_message));
        alertDialog.setPositiveButton(Objects.requireNonNull(getActivity()).getResources().getString(R.string.confirm_delete_dialog_positive_button), (dialog, which) -> {
            bookmarksViewModel.deleteAllBookmark();
            recyclerViewBookMark.setVisibility(View.GONE);
            dialog.dismiss();
        });
        alertDialog.setNegativeButton(Objects.requireNonNull(getActivity()).getResources().getString(R.string.confirm_delete_dialog_negative_button), (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {

        BookmarksEntity bookmarksEntity = (BookmarksEntity) view.getTag();
        Logger.d(Objects.requireNonNull(getActivity()).getClass(), bookmarksEntity.getBookmarkWord() + " :Clicked");

        EngToMmEntity engToMmEntity = engToMmRepository.getEngToMm(bookmarksEntity.getBookmarkWord());
        EngToEngEntity engToEngEntity = engToEngRepository.getEngToEng(bookmarksEntity.getBookmarkWord());

        if (engToMmEntity != null) {
            vocabulary = engToMmEntity.getVocabulary();
            type = "( " + engToMmEntity.getType() + " )";
            meaning = engToMmEntity.getMeaning();
        }
        if (engToEngEntity != null) {
            vocabulary = engToEngEntity.getVocabulary();
            type = "";
            meaning = engToEngEntity.getMeaning();
        }

        Intent intent = new Intent(getContext(), DetailResultActivity.class);
        intent.putExtra("vocabularyKey", vocabulary);
        intent.putExtra("typeKey", type);
        intent.putExtra("meaningKey", meaning);
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View view) {

        BookmarksEntity bookmarksEntity = (BookmarksEntity) view.getTag();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        alertDialog.setCancelable(false);
        alertDialog.setTitle(Objects.requireNonNull(getActivity()).getResources().getString(R.string.confirm_delete_dialog_title));
        alertDialog.setMessage(Objects.requireNonNull(getActivity()).getResources().getString(R.string.confirm_delete_dialog_message));
        alertDialog.setPositiveButton(Objects.requireNonNull(getActivity()).getResources().getString(R.string.confirm_delete_dialog_positive_button), (dialog, which) -> {
            bookmarksViewModel.deleteBookmark(bookmarksEntity);
            dialog.dismiss();
        });
        alertDialog.setNegativeButton(Objects.requireNonNull(getActivity()).getResources().getString(R.string.confirm_delete_dialog_negative_button), (dialog, which) -> dialog.dismiss());
        alertDialog.show();
        return true;
    }
}

