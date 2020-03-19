package org.wordpress.android.ui.reader.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.wordpress.android.R;
import org.wordpress.android.models.ReaderPost;
import org.wordpress.android.util.ColorUtils;

class ReaderRemovedPostViewHolder extends RecyclerView.ViewHolder {
    private final CardView mCardView;

    private final TextView mTxtRemovedPostTitle;
    private final TextView mUndoRemoveAction;

    ReaderRemovedPostViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.reader_cardview_removed_post, parent, false));
        mCardView = itemView.findViewById(R.id.card_view);
        mTxtRemovedPostTitle = itemView.findViewById(R.id.removed_post_title);
        mUndoRemoveAction = itemView.findViewById(R.id.undo_remove);
    }

    void bind(ReaderPost post, Runnable undo) {
        mTxtRemovedPostTitle.setText(createTextForRemovedPostContainer(post, itemView.getContext()));
        Drawable drawable =
                ColorUtils.INSTANCE.applyTintToDrawable(itemView.getContext(), R.drawable.ic_undo_white_24dp, R.color.primary_40);
        mUndoRemoveAction.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        mCardView.setOnClickListener(v -> undo.run());
    }

    /**
     * Creates 'Removed [post title]' text, with the '[post title]' in bold.
     */
    @NonNull
    private SpannableStringBuilder createTextForRemovedPostContainer(ReaderPost post, Context context) {
        String removedString = context.getString(R.string.removed);
        String removedPostTitle = removedString + " " + post.getTitle();
        SpannableStringBuilder str = new SpannableStringBuilder(removedPostTitle);
        str.setSpan(new StyleSpan(Typeface.BOLD), removedString.length(), removedPostTitle.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return str;
    }
}
