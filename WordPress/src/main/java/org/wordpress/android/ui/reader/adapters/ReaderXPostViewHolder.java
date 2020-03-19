package org.wordpress.android.ui.reader.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.wordpress.android.R;
import org.wordpress.android.models.ReaderPost;
import org.wordpress.android.ui.reader.ReaderInterfaces.OnPostSelectedListener;
import org.wordpress.android.ui.reader.utils.ReaderXPostUtils;
import org.wordpress.android.util.GravatarUtils;
import org.wordpress.android.util.image.ImageManager;
import org.wordpress.android.util.image.ImageType;

/*
 * cross-post
 */
class ReaderXPostViewHolder extends RecyclerView.ViewHolder {
    private final CardView mCardView;
    private final ImageView mImgAvatar;
    private final ImageView mImgBlavatar;
    private final TextView mTxtTitle;
    private final TextView mTxtSubtitle;
    private final ImageManager mImageManager;
    private int mAvatarSzSmall;

    ReaderXPostViewHolder(ViewGroup parent, ImageManager imageManager, int avatarSzSmall) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.reader_cardview_xpost, parent, false));
        mCardView = itemView.findViewById(R.id.card_view);
        mImgAvatar = itemView.findViewById(R.id.image_avatar);
        mImgBlavatar = itemView.findViewById(R.id.image_blavatar);
        mTxtTitle = itemView.findViewById(R.id.text_title);
        mTxtSubtitle = itemView.findViewById(R.id.text_subtitle);
        mImageManager = imageManager;
        mAvatarSzSmall = avatarSzSmall;
    }

    void bind(ReaderPost post, OnPostSelectedListener clickListener) {
        mImageManager
                .loadIntoCircle(mImgAvatar, ImageType.AVATAR,
                        GravatarUtils.fixGravatarUrl(post.getPostAvatar(), mAvatarSzSmall));

        mImageManager.load(mImgBlavatar, ImageType.BLAVATAR,
                GravatarUtils.fixGravatarUrl(post.getBlogImageUrl(), mAvatarSzSmall));
        mTxtTitle.setText(ReaderXPostUtils.getXPostTitle(post));
        mTxtSubtitle.setText(ReaderXPostUtils.getXPostSubtitleHtml(post));
        mCardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPostSelected(post);
            }
        });
    }
}
